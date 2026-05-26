# ppl_git_branch_module.py
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
import ppl_git_repo_module


def get_current_branch_hash(source_workspace_path: str):
    ppl_git_repo_module.trust_current_git_repo(source_workspace_path=source_workspace_path)
    result = subprocess.run(['git', 'rev-parse', '--short', 'HEAD'],
                            stdout=subprocess.PIPE,
                            cwd=source_workspace_path)
    branch_hash = result.stdout.decode("utf-8").strip()
    log.info(f"current branch hash: {branch_hash}")
    return branch_hash

