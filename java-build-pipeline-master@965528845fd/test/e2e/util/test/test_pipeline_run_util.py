import json
from subprocess import CalledProcessError
from unittest.mock import patch, MagicMock

from pipeline_run_util import (
    get_pipelinerun_name_by_event_id,
    wait_for_pipelinerun_status,
    get_pipeline_run_name_by_labels_and_params
)


def test_get_pipelinerun_name_by_event_id_found():
    mock_output = json.dumps({
        "items": [
            {"metadata": {"name": "my-pipelinerun"}}
        ]
    })

    mock_result = MagicMock(stdout=mock_output)

    with patch("subprocess.run", return_value=mock_result), \
            patch("time.sleep", return_value=None):
        name = get_pipelinerun_name_by_event_id("event123", timeout=1, poll_interval=0)
        assert name == "my-pipelinerun"


def test_get_pipelinerun_name_by_event_id_timeout():
    mock_output = json.dumps({"items": []})
    mock_result = MagicMock(stdout=mock_output)

    with patch("subprocess.run", return_value=mock_result), \
            patch("time.sleep", return_value=None):
        name = get_pipelinerun_name_by_event_id("event123", timeout=0.1, poll_interval=0)
        assert name is None


def test_get_pipelinerun_name_by_event_id_subprocess_error():
    error = CalledProcessError(1, "cmd", stderr="boom")

    with patch("subprocess.run", side_effect=error), \
            patch("time.sleep", return_value=None):
        name = get_pipelinerun_name_by_event_id("event123", timeout=0.1, poll_interval=0)
        assert name is None


def test_wait_for_pipelinerun_status_success():
    mock_output = json.dumps({
        "status": {
            "conditions": [
                {"type": "Succeeded", "status": "True", "message": "All good"}
            ]
        }
    })

    mock_result = MagicMock(stdout=mock_output)

    with patch("subprocess.run", return_value=mock_result), \
            patch("time.sleep", return_value=None):
        success, message = wait_for_pipelinerun_status("myrun", timeout=1, poll_interval=0)
        assert success is True
        assert message == "All good"


def test_wait_for_pipelinerun_status_failure():
    mock_output = json.dumps({
        "status": {
            "conditions": [
                {"type": "Succeeded", "status": "False", "message": "Something broke"}
            ]
        }
    })

    mock_result = MagicMock(stdout=mock_output)

    with patch("subprocess.run", return_value=mock_result), \
            patch("time.sleep", return_value=None):
        success, message = wait_for_pipelinerun_status("myrun", timeout=1, poll_interval=0)
        assert success is False
        assert message == "Something broke"


def test_wait_for_pipelinerun_status_timeout():
    mock_output = json.dumps({
        "status": {
            "conditions": [
                {"type": "Succeeded", "status": "Unknown"}
            ]
        }
    })

    mock_result = MagicMock(stdout=mock_output)

    with patch("subprocess.run", return_value=mock_result), \
            patch("time.sleep", return_value=None):
        success, message = wait_for_pipelinerun_status("myrun", timeout=0.1, poll_interval=0)
        assert success is False
        assert "Timeout" in message


def test_wait_for_pipelinerun_status_subprocess_error():
    error = CalledProcessError(1, "cmd", stderr="boom")

    with patch("subprocess.run", side_effect=error), \
            patch("time.sleep", return_value=None):
        success, message = wait_for_pipelinerun_status("myrun", timeout=0.1, poll_interval=0)
        assert success is False
        assert "Timeout" in message


def test_get_pipeline_run_name_by_labels_and_params_found():
    mock_output = json.dumps({
        "items": [
            {
                "metadata": {"name": "run-123"},
                "spec": {
                    "params": [
                        {"name": "env", "value": "prod"},
                        {"name": "version", "value": "1.0"}
                    ]
                }
            }
        ]
    })

    mock_result = MagicMock(stdout=mock_output)

    with patch("subprocess.run", return_value=mock_result), \
            patch("time.sleep", return_value=None):
        name = get_pipeline_run_name_by_labels_and_params(
            appname="myapp",
            pipeline_name="deploy",
            params={"env": "prod"},
            timeout=1,
            poll_interval=0
        )
        assert name == "run-123"


def test_get_pipeline_run_name_by_labels_and_params_no_match():
    mock_output = json.dumps({
        "items": [
            {
                "metadata": {"name": "run-123"},
                "spec": {"params": [{"name": "env", "value": "dev"}]}
            }
        ]
    })

    mock_result = MagicMock(stdout=mock_output)

    with patch("subprocess.run", return_value=mock_result), \
            patch("time.sleep", return_value=None):
        name = get_pipeline_run_name_by_labels_and_params(
            appname="myapp",
            pipeline_name="deploy",
            params={"env": "prod"},
            timeout=0.1,
            poll_interval=0
        )
        assert name is None


def test_get_pipeline_run_name_by_labels_and_params_subprocess_error():
    error = CalledProcessError(1, "cmd", stderr="boom")

    with patch("subprocess.run", side_effect=error), \
            patch("time.sleep", return_value=None):
        name = get_pipeline_run_name_by_labels_and_params(
            appname="myapp",
            pipeline_name="deploy",
            params={"env": "prod"},
            timeout=0.1,
            poll_interval=0
        )
        assert name is None
