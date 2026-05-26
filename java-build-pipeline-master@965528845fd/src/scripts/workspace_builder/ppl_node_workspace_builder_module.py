# ppl_node_workspace_builder_module.py
import base64
import sys
import os
from pathlib import Path

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
from ppl_logging_module import log
import ppl_credentials_module


def generate_npmrc(nexus_workspace_path: str,
                   ws_workspace_path: str,
                   run_directory: str,
                   nexus_npm_repository_url: str):
    if module_paths == '.':
        input_file = open('/scripts/template-npmrc', 'r')
    else:
        input_file = open('./template-npmrc', 'r')

    target_file = Path(f'{ws_workspace_path}/{run_directory}/source/etc/npmrc')
    input_lines = input_file.readlines()
    nexus_user, nexus_password = ppl_credentials_module.read_credentials(nexus_workspace_path)
    user_pass_encoded = base64.encodebytes(bytes(f'{nexus_user}:{nexus_password}', 'ascii')).decode('ascii').strip()
    target_file.parent.mkdir(parents=True, exist_ok=True)
    output_file = target_file.open('w+')
    for curr_line in input_lines:
        curr_line = curr_line.replace('@NPMREPO@', nexus_npm_repository_url)
        curr_line = curr_line.replace('@USERNAME@', nexus_user)
        curr_line = curr_line.replace('@PASSWORD@', nexus_password)
        curr_line = curr_line.replace('@USERPASSENCODED@', user_pass_encoded)
        output_file.write(curr_line)
    output_file.close()
    log.warning(f"created {output_file.name}")
    with target_file.open('r') as f:
        log.debug(f"  {f.readlines()}")
