# ppl_git_repo_module.py
import subprocess
import sys
import os

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
from ppl_logging_module import log


def trust_current_git_repo(source_workspace_path: str):
    git_command_result = subprocess.run(['git', 'config', '--global', '--add', 'safe.directory', source_workspace_path],
                                        stdout=subprocess.PIPE,
                                        cwd=source_workspace_path)
    git_log = git_command_result.stdout.decode("utf-8")
    log.info(f"trust directory result: {git_log}")
