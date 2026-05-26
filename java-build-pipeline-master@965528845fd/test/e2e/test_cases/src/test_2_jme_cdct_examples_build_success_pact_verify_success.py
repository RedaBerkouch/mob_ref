import sys
import os
from typing import Optional

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../../util/src')))

from git_operations import commit_and_push_changes, delete_branch
from file_operations import replace_in_file
from pipeline_test_util import prepare_repository_for_test, trigger_and_wait_for_build_pipeline, \
    get_and_wait_for_pact_verify_pipeline
from logger import get_logger


# Constants
TMP_REPO_DIR="tmp-cdct-consumer-repo-test-2"
REPOSITORY_URL = "ssh://git@bitbucket.bit.admin.ch/jmerhos/jme-rhos-cdct-consumer-example.git"
TEST_DESCRIPTION = "The Build Workflow completes successfully, the pact verify of jme-rhos-cdct-provider-example is triggered, which also completes successfully."


def run(test_commit_sha: str, test_branch_name: str):
    logger = get_logger("Test 2")
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
        logger.info("Updating pact test to trigger the pact verification pipeline.")
        jeap_pact_class_path = os.path.join(TMP_REPO_DIR, "src/test/java/ch/admin/bit/jeap/jme/cdct/consumer/web/api/gateway/TaskClientConsumerPactTest.java")
        replace_in_file(
          file_path=jeap_pact_class_path,
          old_text='A GET request to',
          new_text=f"(E2E Pipeline-Test {os.urandom(6).hex()}) A GET request to"
        )

        last_commit_sha = commit_and_push_changes(
          repo_dir=TMP_REPO_DIR,
          commit_message=f"Update pact test to trigger pact verification pipeline for e2e test: {TEST_DESCRIPTION}"
        )

        # WHEN triggering the build pipeline and waiting for its completion
        logger.info("Triggering build pipeline and waiting for completion...")
        build_pipeline_run_name, build_success = trigger_and_wait_for_build_pipeline(
            commit_hash=last_commit_sha,
            branch_name=unique_branch_name,
            project_key="JMERHOS",
            repository_name="jme-rhos-cdct-consumer-example"
        )

        # THEN build pipeline succeeds
        if build_pipeline_run_name is None:
            logger.error("Build pipeline was not triggered.")
            return False
        if not build_success:
            logger.error(f"Build pipeline {build_pipeline_run_name} failed.")
            return False
        logger.info(f"Build pipeline {build_pipeline_run_name} succeeded.")

        # THEN the pact verify pipeline of jme-rhos-cdct-provider-service is triggered and succeeds
        logger.info("Waiting for pact verify pipeline of jme-rhos-cdct-provider-service to complete...")
        pact_verify_pipeline_run_name, pact_verify_success = get_and_wait_for_pact_verify_pipeline(
            appname="jme-rhos-cdct-provider-service",
            pipeline_name="cd-pipelines",
            params={
              "CONSUMER_NAME": "bit-jme-rhos-cdct-consumer-service"
            }
        )
        if pact_verify_pipeline_run_name is None:
            logger.error("Pact verify pipeline was not triggered.")
            return False
        if not pact_verify_success:
            logger.error(f"Pact verify pipeline {pact_verify_pipeline_run_name} failed.")
            return False
        logger.info(f"Pact verify pipeline {pact_verify_pipeline_run_name} succeeded.")

        return True

    except Exception as e:
        logger.error(f"Test failed: {e}")
        raise
    finally:
        delete_branch(repo_dir=TMP_REPO_DIR, branch_name=unique_branch_name)
        logger.info(f"Cleaned up branch: {unique_branch_name}")


def main(commit_hash, branch_name):
    run(commit_hash, branch_name)
