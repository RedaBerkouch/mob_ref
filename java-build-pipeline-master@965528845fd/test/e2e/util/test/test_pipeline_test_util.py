import hashlib
from unittest.mock import patch

from pipeline_test_util import (
    generate_unique_branch_name,
    prepare_repository_for_test,
    trigger_and_wait_for_build_pipeline,
    get_and_wait_for_deploy_pipeline
)


def test_generate_unique_branch_name_feature_branch():
    name = generate_unique_branch_name(
        test_branch_name="feature/login",
        test_commit_sha="abcdef123456",
        test_description="Test login flow"
    )

    short_sha = "abcdef1"
    desc_hash = hashlib.md5("Test login flow".encode()).hexdigest()[:8]

    assert name == f"feature/E2E-Test-{desc_hash}-for-login-{short_sha}"


def test_generate_unique_branch_name_non_feature_branch():
    name = generate_unique_branch_name(
        test_branch_name="hotfix",
        test_commit_sha="1234567890",
        test_description="Fix issue"
    )

    short_sha = "1234567"
    desc_hash = hashlib.md5("Fix issue".encode()).hexdigest()[:8]

    assert name == f"feature/E2E-Test-{desc_hash}-for-hotfix-{short_sha}"


@patch("pipeline_test_util.clone_repository")
@patch("pipeline_test_util.branch_exists", return_value=True)
@patch("pipeline_test_util.get_current_commit_id", return_value="abc123")
def test_prepare_repository_branch_exists(mock_commit, mock_exists, mock_clone):
    branch, commit = prepare_repository_for_test(
        repository_url="url",
        repo_dir="/tmp/repo",
        test_branch_name="feature/test",
        test_commit_sha="abcdef123456",
        test_description="desc"
    )

    assert branch.startswith("feature/E2E-Test-")
    assert commit == "abc123"

    # create_and_checkout_branch and commit_and_push_changes must NOT be called
    with patch("pipeline_test_util.create_and_checkout_branch") as mock_create:
        mock_create.assert_not_called()


@patch("pipeline_test_util.clone_repository")
@patch("pipeline_test_util.branch_exists", return_value=False)
@patch("pipeline_test_util.create_and_checkout_branch")
@patch("pipeline_test_util.commit_and_push_changes")
@patch("pipeline_test_util.get_current_commit_id", return_value="deadbeef")
def test_prepare_repository_creates_branch(
        mock_commit_id, mock_push, mock_create, mock_exists, mock_clone
):
    branch, commit = prepare_repository_for_test(
        repository_url="url",
        repo_dir="/tmp/repo",
        test_branch_name="feature/test",
        test_commit_sha="abcdef123456",
        test_description="desc"
    )

    assert branch.startswith("feature/E2E-Test-")
    assert commit == "deadbeef"

    mock_create.assert_called_once()
    mock_push.assert_called_once()


@patch("pipeline_test_util.trigger_build_pipeline", return_value="event123")
@patch("pipeline_test_util.get_pipelinerun_name_by_event_id", return_value="run-1")
@patch("pipeline_test_util.wait_for_pipelinerun_status", return_value=(True, "ok"))
def test_trigger_and_wait_for_build_pipeline_success(mock_wait, mock_get, mock_trigger):
    name, success = trigger_and_wait_for_build_pipeline(
        commit_hash="abc",
        branch_name="main",
        project_key="proj",
        repository_name="repo"
    )

    assert name == "run-1"
    assert success is True


@patch("pipeline_test_util.trigger_build_pipeline", return_value=None)
def test_trigger_and_wait_for_build_pipeline_no_event_id(mock_trigger):
    name, success = trigger_and_wait_for_build_pipeline(
        commit_hash="abc",
        branch_name="main",
        project_key="proj",
        repository_name="repo"
    )

    assert name is None
    assert success is False


@patch("pipeline_test_util.trigger_build_pipeline", return_value="event123")
@patch("pipeline_test_util.get_pipelinerun_name_by_event_id", return_value=None)
def test_trigger_and_wait_for_build_pipeline_no_pipelinerun(mock_get, mock_trigger):
    name, success = trigger_and_wait_for_build_pipeline(
        commit_hash="abc",
        branch_name="main",
        project_key="proj",
        repository_name="repo"
    )

    assert name is None
    assert success is False


@patch("pipeline_test_util.trigger_build_pipeline", return_value="event123")
@patch("pipeline_test_util.get_pipelinerun_name_by_event_id", return_value="run-1")
@patch("pipeline_test_util.wait_for_pipelinerun_status", return_value=(False, "failed"))
def test_trigger_and_wait_for_build_pipeline_failure(mock_wait, mock_get, mock_trigger):
    name, success = trigger_and_wait_for_build_pipeline(
        commit_hash="abc",
        branch_name="main",
        project_key="proj",
        repository_name="repo"
    )

    assert name == "run-1"
    assert success is False


@patch("pipeline_test_util.get_pipeline_run_name_by_labels_and_params", return_value="deploy-1")
@patch("pipeline_test_util.wait_for_pipelinerun_status", return_value=(True, "ok"))
def test_get_and_wait_for_deploy_pipeline_success(mock_wait, mock_get):
    name, success = get_and_wait_for_deploy_pipeline(
        params={"TRIGGER_PIPELINE_RUN_NAME": "build-1", "DEPLOY_STAGE": "prod"},
        appname="myapp",
        pipeline_name="deploy"
    )

    assert name == "deploy-1"
    assert success is True


@patch("pipeline_test_util.get_pipeline_run_name_by_labels_and_params", return_value=None)
def test_get_and_wait_for_deploy_pipeline_no_run(mock_get):
    name, success = get_and_wait_for_deploy_pipeline(
        params={"TRIGGER_PIPELINE_RUN_NAME": "build-1", "DEPLOY_STAGE": "prod"},
        appname="myapp",
        pipeline_name="deploy"
    )

    assert name is None
    assert success is False


@patch("pipeline_test_util.get_pipeline_run_name_by_labels_and_params", return_value="deploy-1")
@patch("pipeline_test_util.wait_for_pipelinerun_status", return_value=(False, "failed"))
def test_get_and_wait_for_deploy_pipeline_failure(mock_wait, mock_get):
    name, success = get_and_wait_for_deploy_pipeline(
        params={"TRIGGER_PIPELINE_RUN_NAME": "build-1", "DEPLOY_STAGE": "prod"},
        appname="myapp",
        pipeline_name="deploy"
    )

    assert name == "deploy-1"
    assert success is False
