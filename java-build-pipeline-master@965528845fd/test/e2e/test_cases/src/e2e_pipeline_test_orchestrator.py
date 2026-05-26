import os
import sys
import argparse
from concurrent.futures import ThreadPoolExecutor, as_completed

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../../util/src')))

import test_1_jme_rhos_cicd_example_build_success_deploy_success_verification_success_automatically_staged
import test_2_jme_cdct_examples_build_success_pact_verify_success
import test_3_jme_rhos_example_build_success_deploy_fail
from ssh_environment import setup
from logger import get_logger

# Constants
SSH_KEY_SECRET_NAME = 'bit-bs-pipelines-d-pipelines-tp-bitbucket-ssh-key'


def run_all_test_cases(test_commit_sha: str, test_branch_name: str):
    """
    Run all test cases with the given commit SHA and branch name.

    Args:
        test_commit_sha (str): The commit SHA to test.
        test_branch_name (str): The branch name to test.

    Returns:
        None
    """
    logger = get_logger()
    logger.info(f"Starting test cases for commit: {test_commit_sha}, branch: {test_branch_name}")
    # Setup SSH environment once
    setup(secret_name=SSH_KEY_SECRET_NAME)

    # Define test cases as functions with descriptive names
    test_cases = {
        "Test 1: Build Success, Deploy Success, Verification Success, Automatically staged [dev -> ref -> prod] (RHOS CICD Example)": test_1_jme_rhos_cicd_example_build_success_deploy_success_verification_success_automatically_staged.run,
        "Test 2: Build Success, Pact verification ok (RHOS CDCT Consumer and Provider Example)": test_2_jme_cdct_examples_build_success_pact_verify_success.run,
        "Test 3: Build Success, Deploy Fail (RHOS Example)": test_3_jme_rhos_example_build_success_deploy_fail.run
    }

    test_results = {}

    # Execute test cases in parallel
    with ThreadPoolExecutor() as executor:
        future_to_test = {executor.submit(test, test_commit_sha, test_branch_name): name for name, test in test_cases.items()}
        for future in as_completed(future_to_test):
            test_name = future_to_test[future]
            try:
                result = future.result()
                test_results[test_name] = result
            except Exception as e:
                logger.error(f"Test case '{test_name}' raised an exception: {e}")
                test_results[test_name] = False

    summarize_test_results(test_results)


def summarize_test_results(test_results: dict[str, bool]) -> None:
    """
    Summarize the test results, showing which test cases succeeded and which failed.

    Args:
        test_results (dict[str, bool]): A dictionary where keys are test names and values are booleans indicating success (True) or failure (False).
    """
    logger = get_logger("Summary")
    logger.info("--------------------------------------------------------------")
    logger.info("Test Results Summary:")
    logger.info("--------------------------------------------------------------")
    for test_name, result in test_results.items():
        status = "SUCCEEDED" if result else "FAILED"
        logger.info(f"Test Case: {test_name} - {status}")

    succeeded_tests = [name for name, result in test_results.items() if result]
    failed_tests = [name for name, result in test_results.items() if not result]
    logger.info("")
    logger.info("Summary:")
    logger.info(f"Total Tests: {len(test_results)}")
    logger.info(f"Succeeded: {len(succeeded_tests)}")
    logger.info(f"Failed: {len(failed_tests)}")
    logger.info("--------------------------------------------------------------")

    if failed_tests:
        sys.exit(1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--test_commit_sha", type=str, help="Commit hash")
    parser.add_argument("--test_branch_name", type=str, help="Branch name")
    args = parser.parse_args()

    run_all_test_cases(args.test_commit_sha, args.test_branch_name)
