import hashlib
import hmac
from unittest.mock import patch, MagicMock

import pytest

from pipeline_trigger import (
    generate_payload,
    generate_hmac_header,
    trigger_build_pipeline
)


def test_generate_payload_structure():
    payload = generate_payload(
        branch="develop",
        commit_hash="abc123",
        actor_name="tester",
        email="tester@example.com",
        display_name="Test User",
        repository_name="myrepo",
        project_key="MYPROJ"
    )

    # Basic structural checks
    assert payload["eventKey"] == "repo:refs_changed"
    assert payload["actor"]["name"] == "tester"
    assert payload["repository"]["slug"] == "myrepo"
    assert payload["repository"]["project"]["key"] == "MYPROJ"
    assert payload["changes"][0]["ref"]["displayId"] == "develop"
    assert payload["changes"][0]["toHash"] == "abc123"


def test_generate_hmac_header():
    secret = "mysecret"
    body = b'{"test": 1}'

    expected_digest = hmac.new(secret.encode(), body, hashlib.sha256).hexdigest()
    header = generate_hmac_header(secret, body)

    assert header == f"X-Hub-Signature: sha256={expected_digest}"


@patch("pipeline_trigger.requests.post")
def test_trigger_build_pipeline_success(mock_post):
    mock_response = MagicMock()
    mock_response.ok = True
    mock_response.json.return_value = {"eventID": "event123"}
    mock_response.status_code = 200
    mock_post.return_value = mock_response

    event_id = trigger_build_pipeline(
        commit_hash="abc",
        repository_name="repo",
        project_key="PROJ",
        secret=None
    )

    assert event_id == "event123"
    mock_post.assert_called_once()


@patch("pipeline_trigger.requests.post")
def test_trigger_build_pipeline_no_event_id(mock_post):
    mock_response = MagicMock()
    mock_response.ok = True
    mock_response.json.return_value = {}  # No eventID
    mock_response.status_code = 200
    mock_post.return_value = mock_response

    event_id = trigger_build_pipeline(
        commit_hash="abc",
        repository_name="repo",
        project_key="PROJ"
    )

    assert event_id is None


@patch("pipeline_trigger.requests.post")
def test_trigger_build_pipeline_http_error(mock_post):
    mock_response = MagicMock()
    mock_response.ok = False
    mock_response.status_code = 500
    mock_response.text = "Internal Server Error"
    mock_post.return_value = mock_response

    with pytest.raises(Exception):
        trigger_build_pipeline(
            commit_hash="abc",
            repository_name="repo",
            project_key="PROJ"
        )


@patch("pipeline_trigger.requests.post", side_effect=Exception("Network down"))
def test_trigger_build_pipeline_request_exception(mock_post):
    with pytest.raises(Exception):
        trigger_build_pipeline(
            commit_hash="abc",
            repository_name="repo",
            project_key="PROJ"
        )


@patch("pipeline_trigger.requests.post")
def test_trigger_build_pipeline_hmac_header(mock_post):
    mock_response = MagicMock()
    mock_response.ok = True
    mock_response.json.return_value = {"eventID": "event123"}
    mock_post.return_value = mock_response

    secret = "supersecret"

    trigger_build_pipeline(
        commit_hash="abc",
        repository_name="repo",
        project_key="PROJ",
        secret=secret
    )

    # Extract headers passed to requests.post
    _, kwargs = mock_post.call_args
    headers = kwargs["headers"]

    # Validate HMAC header exists
    assert "X-Hub-Signature" in headers
    assert headers["X-Hub-Signature"].startswith("sha256=")
