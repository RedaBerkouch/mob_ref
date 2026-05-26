# ppl_maven_workspace_builder_module.py
import sys
import os
from pathlib import Path
from typing import TextIO

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
import ppl_logging_module
from ppl_logging_module import log
import ppl_credentials_module


def create_settings_xml(nexus_workspace_path: str,
                        ws_workspace_path: str,
                        run_directory: str,
                        nexus_release_repo: str,
                        nexus_snapshot_repo: str):
    log.info("creating settings xml")
    if module_paths == '.':
        input_file = open('/scripts/template-settings.xml', 'r')
    else:
        input_file = open('./template-settings.xml', 'r')

    settings_file = Path(f'{ws_workspace_path}/{run_directory}/maven-home/settings.xml')
    generate_build_config_file(input_file=input_file,
                               nexus_release_repo=nexus_release_repo,
                               nexus_snapshot_repo=nexus_snapshot_repo,
                               nexus_workspace_path=nexus_workspace_path,
                               settings_file=settings_file)


def generate_build_config_file(input_file: TextIO,
                               nexus_release_repo: str,
                               nexus_snapshot_repo: str,
                               nexus_workspace_path: str,
                               settings_file: Path):
    input_lines = input_file.readlines()
    nexus_user, nexus_password = ppl_credentials_module.read_credentials(nexus_workspace_path)
    settings_file.parent.mkdir(parents=True, exist_ok=True)
    output_file = settings_file.open('w+')
    for curr_line in input_lines:
        curr_line = curr_line.replace('@USERNAME@', nexus_user)
        curr_line = curr_line.replace('@PASSWORD@', nexus_password)
        curr_line = curr_line.replace('@RELEASEREPO@', nexus_release_repo)
        curr_line = curr_line.replace('@SNAPSHOTREPO@', nexus_snapshot_repo)
        output_file.writelines([curr_line])
    output_file.close()
    log.warning(f"created {output_file.name}")
    with settings_file.open('r') as f:
        log.debug(f"  {f.readlines()}\n")
