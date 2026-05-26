import sys
import os

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../../util/src')))

from file_operations import override_file
from git_operations import delete_branch, commit_and_push_changes
from pipeline_test_util import prepare_repository_for_test, trigger_and_wait_for_build_pipeline, get_and_wait_for_deploy_pipeline
from logger import get_logger


# Constants
TMP_REPO_DIR = "tmp-rhos-test-3"
REPOSITORY_URL = "ssh://git@bitbucket.bit.admin.ch/bit_jme/jme-rhos-example.git"
TEST_DESCRIPTION = "The Build Workflow completes successfully and triggers the Deployment Pipeline, which fails due to an error in the after deploy task."

def run(test_commit_sha, test_branch_name):
    logger = get_logger("Test 3")
    logger.info(f"Starting test: {TEST_DESCRIPTION} for commit {test_commit_sha} on branch {test_branch_name}")

    # GIVEN
    unique_branch_name, commit_sha = prepare_repository_for_test(
        repository_url=REPOSITORY_URL,
        repo_dir=TMP_REPO_DIR,
        test_branch_name=test_branch_name,
        test_commit_sha=test_commit_sha,
        test_description=TEST_DESCRIPTION
    )
    logger.info(f"Prepared repository and created branch: {unique_branch_name}")
    try:
        logger.info("Introducing failure in the deployment pipeline by overriding the 'after-deploy.sh' script to exit with an error.")
        override_file(file_path=TMP_REPO_DIR + "/after-deploy.sh",
                      content="exit 1")
        last_commit_sha = commit_and_push_changes(repo_dir=TMP_REPO_DIR, commit_message="Introduce failure in deployment pipeline by making after-deploy.sh exit with error")

        # WHEN
        logger.info("Triggering build pipeline and waiting for completion...")
        build_pipeline_run_name, build_success = trigger_and_wait_for_build_pipeline(
            commit_hash=last_commit_sha,
            branch_name=unique_branch_name,
            project_key="BIT_JME",
            repository_name="jme-rhos-example"
        )
        if build_pipeline_run_name is None:
            logger.error("Build pipeline was not triggered.")
            return False
        if not build_success:
            logger.error(f"Build pipeline {build_pipeline_run_name} failed.")
            return False
        logger.info(f"Build pipeline {build_pipeline_run_name} succeeded.")

        logger.info("Waiting for deploy pipeline to complete...")
        deploy_pipeline_run_name, deploy_success = get_and_wait_for_deploy_pipeline(
            params={
                "TRIGGER_PIPELINE_RUN_NAME": build_pipeline_run_name
            },
            appname="jme-rhos-example",
            pipeline_name="cd-pipelines"
        )
        if deploy_pipeline_run_name is None:
            logger.error("Deploy pipeline was not triggered.")
            return False
        if deploy_success:
            logger.error(f"Deploy pipeline {deploy_pipeline_run_name} succeeded, but was expected to fail due to an error in the after deploy task.")
            return False

        logger.info(f"Deploy pipeline {deploy_pipeline_run_name} failed as expected due to an error in the after deploy task.")
        return True

    except Exception as e:
        logger.error(f"Test failed: {e}")
        raise
    finally:
        delete_branch(repo_dir=TMP_REPO_DIR, branch_name=unique_branch_name)
        logger.info(f"Cleaned up branch: {unique_branch_name}")


def main(commit_hash, branch_name):
    run(commit_hash, branch_name)
