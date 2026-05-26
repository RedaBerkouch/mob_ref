import base64
import subprocess
import json

def get_kubernetes_secret_value(secret_name: str, key: str, namespace: str='bit-bs-pipelines-d') -> str:
    """
    Fetches and decodes a value from a Kubernetes secret using the oc CLI.

    Args:
        namespace (str): The namespace where the secret resides.
        secret_name (str): The name of the secret.
        key (str): The key in the secret's data dict.

    Returns:
        str: The decoded value, or raises an Exception if not found.
    """
    try:
        result = subprocess.run(
            [
                'oc', 'get', 'secret', secret_name,
                '-n', namespace,
                '-o', 'json'
            ],
            capture_output=True,
            check=True,
            text=True
        )
        secret_json = json.loads(result.stdout)
        if 'data' not in secret_json or key not in secret_json['data']:
            raise KeyError(f"Key '{key}' not found in secret '{secret_name}' in namespace '{namespace}'.")
        value_b64 = secret_json['data'][key]
        return base64.b64decode(value_b64).decode("utf-8")
    except subprocess.CalledProcessError as e:
        raise RuntimeError(f"Failed to fetch secret '{secret_name}' in namespace '{namespace}': {e.stderr}")
    except Exception as e:
        raise RuntimeError(f"Error fetching secret: {str(e)}")


def get_kubernetes_ssh_auth_secret(secret_name: str, namespace: str='bit-bs-pipelines-d') -> dict:
    """
    Fetches and decodes a Kubernetes secret of type 'kubernetes.io/ssh-auth'.

    Args:
        namespace (str): The namespace where the secret resides.
        secret_name (str): The name of the secret.

    Returns:
        dict: A dictionary containing the decoded 'ssh-privatekey' and 'known_hosts' values.
    """
    try:
        result = subprocess.run(
            [
                'oc', 'get', 'secret', secret_name,
                '-n', namespace,
                '-o', 'json'
            ],
            capture_output=True,
            check=True,
            text=True
        )
        secret_json = json.loads(result.stdout)
        if 'data' not in secret_json:
            raise KeyError(f"No data found in secret '{secret_name}' in namespace '{namespace}'.")

        ssh_auth_data = {}
        for key in ['ssh-privatekey', 'known_hosts']:
            if key in secret_json['data']:
                ssh_auth_data[key] = base64.b64decode(secret_json['data'][key]).decode("utf-8")
            else:
                ssh_auth_data[key] = None

        return ssh_auth_data
    except subprocess.CalledProcessError as e:
        raise RuntimeError(f"Failed to fetch secret '{secret_name}' in namespace '{namespace}': {e.stderr}")
    except Exception as e:
        raise RuntimeError(f"Error fetching secret: {str(e)}")
