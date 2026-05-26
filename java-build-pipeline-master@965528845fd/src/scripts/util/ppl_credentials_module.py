# ppl_credentials_module.py
import os
import sys

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
from ppl_logging_module import log


def read_credentials(workspace_path: str):
    try:
        username = open(f'{workspace_path}/username', 'r').readline().replace('\r', '').replace('\n', '')
        password = open(f'{workspace_path}/password', 'r').readline().replace('\r', '').replace('\n', '')
        return username, password
    except Exception as e:
        log.error(f"An error occurred: {e}")
        return None, None


def read_sonar_token(sonar_workspace_path: str):
    token = open(f'{sonar_workspace_path}/token', 'r').readline().replace('\r', '').replace('\n', '')
    host_url = open(f'{sonar_workspace_path}/hostUrl', 'r').readline().replace('\r', '').replace('\n', '')
    return token, host_url
