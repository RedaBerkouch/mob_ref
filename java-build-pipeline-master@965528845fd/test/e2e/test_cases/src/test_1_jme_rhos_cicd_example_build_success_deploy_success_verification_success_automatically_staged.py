import sys
import os
from typing import Optional

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../../util/src')))

from git_operations import delete_branch
from pipeline_test_util import prepare_repository_for_test, trigger_and_wait_for_build_pipeline, \
    get_and_wait_for_deploy_pipeline
from logger import get_logger


# Constants
TMP_REPO_DIR = "tmp-cicd-repo-test-1"
REPOSITORY_URL = "ssh://git@bitbucket.bit.admin.ch/bit_jme/jme-rhos-cicd-example.git"
TEST_DESCRIPTION = "The Build Workflow completes successfully, triggers the Deployment Pipeline, which also completes successfully, and the Verification Pipeline runs and completes successfully with automated staging from dev to ref to prod."

def run(test_commit_sha: str, test_branch_name: str):
    logger = get_logger("Test 1")
    logger.info(f"Starting test: {TEST_DESCRIPTION} for commit {test_commit_sha} on branch {test_branch_name}")

    # GIVEN
    unique_branch_name, commit_sha = prepare_repository_for_test(
        repository_url=REPOSITORY_URL,
        repo_dir=TMP_REPO_DIR,
        test_branch_name=test_branch_name,
        test_commit_sha=test_commit_sha,
        test_description=TEST_DESCRIPTION
    )

    try:
        # WHEN triggering the build pipeline and waiting for its completion
        logger.info("Triggering build pipeline and waiting for completion...")
        build_pipeline_run_name, build_success = trigger_and_wait_for_build_pipeline(
            commit_hash=commit_sha,
            branch_name=unique_branch_name,
            project_key="BIT_JME",
            repository_name="jme-rhos-cicd-example"
        )

        # THEN build pipeline succeeds
        if build_pipeline_run_name is None:
            logger.error("Build pipeline was not triggered.")
            return False
        if not build_success:
            logger.error(f"Build pipeline {build_pipeline_run_name} failed.")
            return False
        logger.info(f"Build pipeline {build_pipeline_run_name} succeeded.")

        # THEN deployment pipeline on DEV is triggered and succeeds
        dev_deploy_pipeline_run_name, dev_deploy_success = then_validate_and_wait_for_deploy_pipeline(
            stage="d",
            trigger_pipeline_run_name=build_pipeline_run_name,
            appname="jme-rhos-cicd-example",
            pipeline_name="cd-pipelines"
        )
        if not dev_deploy_success:
            logger.error("Deployment pipeline on DEV failed")
            return False

        # THEN deployment pipeline on REF is triggered and succeeds
        ref_deploy_pipeline_run_name, ref_deploy_success = then_validate_and_wait_for_deploy_pipeline(
            stage="ref",
            trigger_pipeline_run_name=dev_deploy_pipeline_run_name,
            appname="jme-rhos-cicd-example",
            pipeline_name="cd-pipelines"
        )
        if not ref_deploy_success:
            logger.error("Deployment pipeline on REF failed")
            return False

        # THEN deployment pipeline on PROD is triggered and succeeds
        prod_deploy_pipeline_run_name, prod_deploy_success = then_validate_and_wait_for_deploy_pipeline(
            stage="prod",
            trigger_pipeline_run_name=ref_deploy_pipeline_run_name,
            appname="jme-rhos-cicd-example",
            pipeline_name="cd-pipelines"
        )
        if not prod_deploy_success:
            logger.error("Deployment pipeline on PROD failed")
            return False


        return True

    except Exception as e:
        logger.error(f"Test failed: {e}")
        raise
    finally:
        delete_branch(repo_dir=TMP_REPO_DIR, branch_name=unique_branch_name)
        logger.info(f"Cleaned up branch: {unique_branch_name}")


def then_validate_and_wait_for_deploy_pipeline(stage: str, trigger_pipeline_run_name: str, appname: str, pipeline_name: str) -> tuple[Optional[str], bool]:
    """
    Validates and waits for the deploy pipeline to complete for a given stage.

    Args:
        stage (str): The deployment stage (e.g., "dev", "ref", "prod").
        trigger_pipeline_run_name (str): The name of the triggering pipeline run.
        appname (str): The application name in the CI system.
        pipeline_name (str): The pipeline name in the CI system.

    Returns:
        tuple[Optional[str], bool]: A tuple containing the deploy PipelineRun name (or None) and the success status.
    """
    logger = get_logger("Test 2")
    logger.info(f"Waiting for deploy pipeline on stage {stage} to complete...")
    deploy_pipeline_run_name, deploy_success = get_and_wait_for_deploy_pipeline(
        params={
            "TRIGGER_PIPELINE_RUN_NAME": trigger_pipeline_run_name,
            "DEPLOY_STAGE": stage
        },
        appname=appname,
        pipeline_name=pipeline_name
    )
    if deploy_pipeline_run_name is None:
        logger.error(f"Deploy pipeline on stage {stage} was not triggered.")
        return None, False
    if not deploy_success:
        logger.error(f"Deploy pipeline {deploy_pipeline_run_name} for stage {stage} failed.")
        return deploy_pipeline_run_name, False
    logger.info(f"Deploy pipeline {deploy_pipeline_run_name} for stage {stage} succeeded.")
    return deploy_pipeline_run_name, True


def main(commit_hash, branch_name):
    run(commit_hash, branch_name)
