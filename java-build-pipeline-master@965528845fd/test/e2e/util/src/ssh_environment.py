import os
import tempfile
from typing import Tuple
from kubernetes_operations import get_kubernetes_ssh_auth_secret

def setup(secret_name: str, ssh_key_secret_key: str = 'ssh-privatekey', known_hosts_secret_key: str = 'known_hosts') -> Tuple[str, str]:
    """
    Set up the SSH environment by retrieving the SSH private key and known hosts from Kubernetes secrets.

    Args:
        secret_name (str): The name of the Kubernetes secret containing the SSH configuration.
        ssh_key_secret_key (str): The key for the SSH private key in the secret.
        known_hosts_secret_key (str): The key for the known hosts in the secret.

    Returns:
        Tuple[str, str]: Paths to the temporary files containing the SSH private key and known hosts.
    """
    ssh_auth_data = get_kubernetes_ssh_auth_secret(secret_name=secret_name)

    ssh_key_content = ssh_auth_data.get(ssh_key_secret_key)
    known_hosts_content = ssh_auth_data.get(known_hosts_secret_key)

    # Write SSH key and known_hosts content to temporary files
    with tempfile.NamedTemporaryFile(delete=False) as ssh_key_file, tempfile.NamedTemporaryFile(delete=False) as known_hosts_file:
        ssh_key_file.write(ssh_key_content.encode())
        ssh_key_file_path = ssh_key_file.name

        known_hosts_file.write(known_hosts_content.encode())
        known_hosts_file_path = known_hosts_file.name

    os.environ['GIT_SSH_COMMAND'] = f"ssh -i {ssh_key_file_path} -o UserKnownHostsFile={known_hosts_file_path}"
    return ssh_key_file_path, known_hosts_file_path