import hashlib
from typing import Optional
from git_operations import clone_repository, branch_exists, create_and_checkout_branch, commit_and_push_changes, get_current_commit_id
from pipeline_run_util import get_pipelinerun_name_by_event_id, wait_for_pipelinerun_status, get_pipeline_run_name_by_labels_and_params
from pipeline_trigger import trigger_build_pipeline
from logger import get_logger

# Initialize logger
logger = get_logger()


def generate_unique_branch_name(test_branch_name: str, test_commit_sha: str, test_description: str) -> str:
    """
    Generate a unique branch name for the test case.

    Args:
        test_branch_name (str): The base branch name for the test.
        test_commit_sha (str): The commit SHA associated with the test.
        test_description (str): A description of the test case.

    Returns:
        str: A unique branch name combining the test description, branch name, and commit SHA.
    """
    short_commit_sha = test_commit_sha[:7]
    description_hash = hashlib.md5(test_description.encode()).hexdigest()[:8]  # Hash the test description
    if test_branch_name.startswith("feature/"):
        return f"feature/E2E-Test-{description_hash}-for-{test_branch_name[len('feature/'):]}-{short_commit_sha}"
    return f"feature/E2E-Test-{description_hash}-for-{test_branch_name}-{short_commit_sha}"


def prepare_repository_for_test(repository_url: str, repo_dir: str, test_branch_name: str, test_commit_sha: str, test_description: str) -> tuple[str, str]:
    """
    Prepare the repository for the test by cloning it, creating a unique branch, and checking it out.

    Args:
        repository_url (str): The URL of the Git repository.
        repo_dir (str): The directory where the repository should be cloned.
        test_branch_name (str): The base branch name for the test.
        test_commit_sha (str): The commit SHA associated with the test.
        test_description (str): A description of the test case.

    Returns:
        tuple[str, str]: A tuple containing the unique branch name and the current commit SHA.
    """
    unique_branch_name = generate_unique_branch_name(test_branch_name, test_commit_sha, test_description)

    # Clone the repository
    clone_repository(repository_url, repo_dir)

    # Check if branch already exists
    if branch_exists(repo_dir=repo_dir, branch_name=unique_branch_name):
        logger.warning(f"Branch '{unique_branch_name}' already exists. "
                       f"This may indicate that a previous test for commit '{test_commit_sha}' is still running or failed to clean up.")
        current_commit_sha = get_current_commit_id(repo_dir)
        return unique_branch_name, current_commit_sha

    # Create and checkout the unique branch
    create_and_checkout_branch(repo_dir=repo_dir, branch_name=unique_branch_name)

    commit_message = f"Prepare repository for e2e test '{test_description}' on branch '{test_branch_name}' with the commit '{test_commit_sha}'."
    commit_and_push_changes(commit_message=commit_message, repo_dir=repo_dir)

    # Get the current commit SHA
    current_commit_sha = get_current_commit_id(repo_dir)

    return unique_branch_name, current_commit_sha


def trigger_and_wait_for_build_pipeline(commit_hash: str, branch_name: str, project_key: str, repository_name: str) -> tuple[Optional[str], bool]:
    """
    Trigger the build pipeline and wait for its completion.

    Args:
        commit_hash (str): The commit hash to trigger the pipeline for.
        branch_name (str): The branch name to trigger the pipeline for.
        project_key (str): The project key in the CI system.
        repository_name (str): The repository name in the CI system.

    Returns:
        tuple[Optional[str], bool]: A tuple containing the build PipelineRun name (or None) and the success status.
    """
    event_id = trigger_build_pipeline(commit_hash=commit_hash, branch=branch_name, project_key=project_key, repository_name=repository_name)
    if event_id is None:
        logger.error("No event_id returned. Test failed.")
        return None, False

    build_pipelinerun_name = get_pipelinerun_name_by_event_id(event_id)
    if not build_pipelinerun_name:
        logger.error("Timeout: No Build PipelineRun found")
        return None, False

    success, _ = wait_for_pipelinerun_status(build_pipelinerun_name)
    return build_pipelinerun_name, success


def get_and_wait_for_deploy_pipeline(appname: str, pipeline_name: str, params: Optional[dict] = None) -> tuple[Optional[str], bool]:
    """
    Get the deploy pipeline triggered by the build pipeline and wait for its completion.

    Args:
        appname (str): The application name in the CI system.
        pipeline_name (str): The pipeline name in the CI system.
        params (Optional[dict]): Optional parameters to find the corresponding deploy PipelineRun.

    Returns:
        tuple[Optional[str], bool]: A tuple containing the deploy PipelineRun name (or None) and the success status.
    """
    if params is None:
        params = {}

    deploy_pipelinerun_name = get_pipeline_run_name_by_labels_and_params(
        appname=appname,
        pipeline_name=pipeline_name,
        params=params
    )
    if not deploy_pipelinerun_name:
        logger.error("Timeout: No Deploy PipelineRun found")
        return None, False

    success, _ = wait_for_pipelinerun_status(deploy_pipelinerun_name)
    return deploy_pipelinerun_name, success

def get_and_wait_for_pact_verify_pipeline(appname: str, pipeline_name: str, params: Optional[dict] = None) -> tuple[Optional[str], bool]:
    """
    Get the pact verify pipeline triggered by the pact broker and wait for its completion.

    Args:
        appname (str): The application name in the CI system.
        pipeline_name (str): The pipeline name in the CI system.
        params (Optional[dict]): Optional parameters to find the corresponding deploy PipelineRun.

    Returns:
        tuple[Optional[str], bool]: A tuple containing the deploy PipelineRun name (or None) and the success status.
    """
    if params is None:
        params = {}

    pact_verify_pipelinerun_name = get_pipeline_run_name_by_labels_and_params(
        appname=appname,
        pipeline_name=pipeline_name,
        params=params
    )
    if not pact_verify_pipelinerun_name:
        logger.error("Timeout: No Pact verify PipelineRun found")
        return None, False

    success, _ = wait_for_pipelinerun_status(pact_verify_pipelinerun_name)
    return pact_verify_pipelinerun_name, success
