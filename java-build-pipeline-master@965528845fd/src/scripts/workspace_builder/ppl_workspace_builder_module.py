# ppl_workspace_builder_module.py
import os
import sys
import time
import uuid
from pathlib import Path
from shutil import rmtree

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util', '../workspace_builder'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
from ppl_logging_module import log
from ppl_maven_workspace_builder_module import create_settings_xml
from ppl_node_workspace_builder_module import generate_npmrc

# ----- Modul parameter -----
workspace_max_age = 60 * 60 * 2  # 2h


def build_workspace(nexus_workspace_path, ws_workspace_path, run_directory, nexus_npm_repository_url,
                    nexus_release_repo, nexus_snapshot_repo, build_tool="maven"):
    clean_old_workspaces(ws_workspace_path)

    generate_npmrc(nexus_workspace_path, ws_workspace_path, run_directory, nexus_npm_repository_url)
    if build_tool == "maven":
        create_settings_xml(nexus_workspace_path, ws_workspace_path, run_directory, nexus_release_repo,
                            nexus_snapshot_repo)


def clean_old_workspaces(ws_workspace_path: str):
    ws_directory = Path(ws_workspace_path)
    log.warning("\ncleaning up workspaces in PVC (only for RXW volumes)")
    for sub_path in ws_directory.iterdir():
        if sub_path.is_dir() and is_valid_uuid(sub_path.name):
            ws_stats = Path(sub_path).stat()
            now_seconds = int(time.time())
            ws_age_in_h = (now_seconds - ws_stats.st_ctime) / 60 / 60
            if ws_stats.st_ctime + workspace_max_age < now_seconds:
                log.warning(f"deleting {sub_path} because it is {ws_age_in_h:.2f}h old")
                rmtree(sub_path)
            else:
                log.warning(f"keeping {sub_path} because it is only {ws_age_in_h:.2f}h old")


def is_valid_uuid(val: str):
    try:
        uuid.UUID(val)
        return True
    except ValueError:
        return False
