import os
from unittest.mock import patch, MagicMock
import pytest

from ssh_environment import setup


@pytest.fixture
def mock_secret_data():
    return {
        "ssh-privatekey": "FAKE_PRIVATE_KEY",
        "known_hosts": "FAKE_KNOWN_HOSTS"
    }


def test_setup_creates_temp_files_and_sets_env(mock_secret_data):
    with patch("ssh_environment.get_kubernetes_ssh_auth_secret", return_value=mock_secret_data):

        # Fake temp files
        fake_ssh_file = MagicMock()
        fake_ssh_file.name = "/tmp/fake_ssh_key"
        fake_ssh_file.__enter__.return_value = fake_ssh_file

        fake_known_hosts_file = MagicMock()
        fake_known_hosts_file.name = "/tmp/fake_known_hosts"
        fake_known_hosts_file.__enter__.return_value = fake_known_hosts_file

        with patch("tempfile.NamedTemporaryFile", side_effect=[fake_ssh_file, fake_known_hosts_file]) as mock_tmp:

            ssh_path, known_hosts_path = setup("my-secret")

            assert ssh_path == "/tmp/fake_ssh_key"
            assert known_hosts_path == "/tmp/fake_known_hosts"

            fake_ssh_file.write.assert_called_once_with(b"FAKE_PRIVATE_KEY")
            fake_known_hosts_file.write.assert_called_once_with(b"FAKE_KNOWN_HOSTS")

            assert os.environ["GIT_SSH_COMMAND"] == (
                "ssh -i /tmp/fake_ssh_key -o UserKnownHostsFile=/tmp/fake_known_hosts"
            )

            assert mock_tmp.call_count == 2


def test_setup_uses_custom_secret_keys():
    mock_data = {
        "mykey": "KEYDATA",
        "myhosts": "HOSTDATA"
    }

    with patch("ssh_environment.get_kubernetes_ssh_auth_secret", return_value=mock_data):

        fake_ssh_file = MagicMock()
        fake_ssh_file.name = "/tmp/custom_ssh"
        fake_ssh_file.__enter__.return_value = fake_ssh_file

        fake_hosts_file = MagicMock()
        fake_hosts_file.name = "/tmp/custom_hosts"
        fake_hosts_file.__enter__.return_value = fake_hosts_file

        with patch("tempfile.NamedTemporaryFile", side_effect=[fake_ssh_file, fake_hosts_file]):

            ssh_path, hosts_path = setup(
                secret_name="my-secret",
                ssh_key_secret_key="mykey",
                known_hosts_secret_key="myhosts"
            )

            assert ssh_path == "/tmp/custom_ssh"
            assert hosts_path == "/tmp/custom_hosts"

            fake_ssh_file.write.assert_called_once_with(b"KEYDATA")
            fake_hosts_file.write.assert_called_once_with(b"HOSTDATA")
