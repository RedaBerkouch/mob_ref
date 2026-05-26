# ppl_build_setup.py
import os
import sys

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util', '../workspace_builder', '../branch_management'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
import ppl_logging_module
from ppl_logging_module import log
from ppl_workspace_builder_module import build_workspace
from ppl_build_branch_config_module import do_branch_configuration

# ----- Modul parameter -----
compliant_mode = True


def main(arguments):
    # first argument is read in __main__
    default_branch_configuration_string = arguments[2]
    app_branch_configuration_string = arguments[3]
    current_branch = arguments[4]
    app_type = arguments[5]
    nexus_workspace_path = arguments[6]
    ws_workspace_path = arguments[7]
    version_template = arguments[8]
    run_directory = arguments[9]
    date_pattern = arguments[10]
    nexus_release_repo = arguments[11]
    nexus_snapshot_repo = arguments[12]
    build_tool = arguments[13]
    nexus_npm_repository_url = arguments[14]
    run_name = arguments[15]
    app_name = arguments[16]

    log.info("Setup the Build pipeline...")

    if not date_pattern:
        date_pattern = "%Y-%m-%dT%H%M%S"

    build_workspace(nexus_workspace_path=nexus_workspace_path,
                    ws_workspace_path=ws_workspace_path,
                    run_directory=run_directory,
                    nexus_npm_repository_url=nexus_npm_repository_url,
                    nexus_release_repo=nexus_release_repo,
                    nexus_snapshot_repo=nexus_snapshot_repo,
                    build_tool=build_tool)

    do_branch_configuration(default_branch_configuration_string=default_branch_configuration_string,
                            app_branch_configuration_string=app_branch_configuration_string,
                            current_branch=current_branch, app_type=app_type, build_tool=build_tool,
                            version_template=version_template, ws_workspace_path=ws_workspace_path,
                            run_directory=run_directory, run_name=run_name, date_pattern=date_pattern,
                            app_name=app_name, compliant_mode=compliant_mode)


if __name__ == "__main__":
    args = sys.argv
    verbose_log = args[1]
    ppl_logging_module.setup_logger(verbose_log)
    ppl_logging_module.log_compliant_mode_warning(compliant_mode)
    main(args)
