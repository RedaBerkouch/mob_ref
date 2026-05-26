import base64
import json
from subprocess import CalledProcessError
from unittest.mock import patch, MagicMock

import pytest

from kubernetes_operations import (
    get_kubernetes_secret_value,
    get_kubernetes_ssh_auth_secret
)


def test_get_kubernetes_secret_value_success():
    encoded_value = base64.b64encode(b"my-secret-value").decode("utf-8")

    mock_output = json.dumps({
        "data": {
            "mykey": encoded_value
        }
    })

    mock_result = MagicMock()
    mock_result.stdout = mock_output

    with patch("subprocess.run", return_value=mock_result):
        value = get_kubernetes_secret_value("mysecret", "mykey")
        assert value == "my-secret-value"


def test_get_kubernetes_secret_value_missing_key():
    mock_output = json.dumps({"data": {}})

    mock_result = MagicMock()
    mock_result.stdout = mock_output

    with patch("subprocess.run", return_value=mock_result):
        with pytest.raises(RuntimeError) as exc:
            get_kubernetes_secret_value("mysecret", "missing")

        assert "Key 'missing' not found" in str(exc.value)


def test_get_kubernetes_secret_value_subprocess_error():
    error = CalledProcessError(
        returncode=1,
        cmd="oc get secret",
        stderr="something went wrong"
    )

    with patch("subprocess.run", side_effect=error):
        with pytest.raises(RuntimeError) as exc:
            get_kubernetes_secret_value("mysecret", "mykey")

        assert "Failed to fetch secret" in str(exc.value)


def test_get_kubernetes_ssh_auth_secret_success():
    encoded_key = base64.b64encode(b"PRIVATE_KEY").decode("utf-8")
    encoded_hosts = base64.b64encode(b"KNOWN_HOSTS").decode("utf-8")

    mock_output = json.dumps({
        "data": {
            "ssh-privatekey": encoded_key,
            "known_hosts": encoded_hosts
        }
    })

    mock_result = MagicMock()
    mock_result.stdout = mock_output

    with patch("subprocess.run", return_value=mock_result):
        data = get_kubernetes_ssh_auth_secret("ssh-secret")

        assert data["ssh-privatekey"] == "PRIVATE_KEY"
        assert data["known_hosts"] == "KNOWN_HOSTS"


def test_get_kubernetes_ssh_auth_secret_missing_keys():
    encoded_key = base64.b64encode(b"PRIVATE_KEY").decode("utf-8")

    mock_output = json.dumps({
        "data": {
            "ssh-privatekey": encoded_key
            # known_hosts fehlt absichtlich
        }
    })

    mock_result = MagicMock()
    mock_result.stdout = mock_output

    with patch("subprocess.run", return_value=mock_result):
        data = get_kubernetes_ssh_auth_secret("ssh-secret")

        assert data["ssh-privatekey"] == "PRIVATE_KEY"
        assert data["known_hosts"] is None


def test_get_kubernetes_ssh_auth_secret_no_data():
    mock_output = json.dumps({})

    mock_result = MagicMock()
    mock_result.stdout = mock_output

    with patch("subprocess.run", return_value=mock_result):
        with pytest.raises(RuntimeError) as exc:
            get_kubernetes_ssh_auth_secret("ssh-secret")

        assert "No data found in secret" in str(exc.value)


def test_get_kubernetes_ssh_auth_secret_subprocess_error():
    error = CalledProcessError(
        returncode=1,
        cmd="oc get secret",
        stderr="boom"
    )

    with patch("subprocess.run", side_effect=error):
        with pytest.raises(RuntimeError) as exc:
            get_kubernetes_ssh_auth_secret("ssh-secret")

        assert "Failed to fetch secret" in str(exc.value)
