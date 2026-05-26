# ppl_values_config_module.py
import json
import sys
import os

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util', '../branch_management'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
from ppl_logging_module import log
import ppl_branch_type_module
import ppl_branch_config_module


# Replaces placeholders in the template with actual values.
#
# :param template: The template string with placeholders
# :param app_name: The name of the application
# :param stage_short: The short form of the stage
# :param stage_long: The long form of the stage
# :return: The customized path as a string
def create_custom_value_path(template, app_name, stage_short, stage_long):
    custom_value_path = template.replace("<APP_NAME>", app_name)
    custom_value_path = custom_value_path.replace("<STAGE_SHORT>", stage_short)
    custom_value_path = custom_value_path.replace("<STAGE_LONG>", stage_long)
    return custom_value_path


# Gets the branch configuration, which is defined in the values.yaml
def get_branch_configuration(default_branch_configuration_string, app_branch_configuration_string, current_branch,
                             app_type, version_template=None, deployment_log_ready_for_deploy_check: bool = False):
    default_branch_configuration = json.loads(default_branch_configuration_string)
    app_branch_configuration = retrieve_app_branch_configuration(app_branch_configuration_string)

    log.warning("\nbranch-based configuration:")
    branch_type = ppl_branch_type_module.get_branch_type_from_branch_name(current_branch)
    log.warning(f"Current branch: {current_branch}")
    log.warning(f"applicationType: {app_type}")
    log.warning(f"branchType: {branch_type}")

    # 1. The default configuration of the current branch type
    config = default_branch_configuration.get(branch_type)
    if version_template is not None:
        config['buildVersionTemplate'] = version_template
    config['deploymentLogReadyForDeployCheck'] = deployment_log_ready_for_deploy_check
    log.warning("1. The default configuration of the current branch type")
    log.warning(json.dumps(config, indent=4, sort_keys=True))

    if app_branch_configuration:
        # 2. The given configuration of the current branch type
        if branch_type in app_branch_configuration.keys():
            config.update(app_branch_configuration.get(branch_type))
            log.warning("2. The given configuration of the current branch type")
            log.warning(json.dumps(config, indent=4, sort_keys=True))

        # 2b. If HOTFIX and no HOTFIX config given, use FEATURE
        if (branch_type == "hotfix"
                and "hotfix" not in app_branch_configuration.keys()
                and "feature" in app_branch_configuration.keys()):
            config.update(app_branch_configuration.get("feature"))
            log.warning("2b. If HOTFIX and no HOTFIX config given, use FEATURE")
            log.warning(json.dumps(config, indent=4, sort_keys=True))

        # 3. Find matching branch by prefix pattern (xyz/*)
        config_for_prefix_pattern = ppl_branch_config_module.get_config_for_prefix_pattern(app_branch_configuration,
                                                                                           current_branch)
        if config_for_prefix_pattern:
            config.update(config_for_prefix_pattern)
            log.warning("3. Find matching branch by prefix pattern (xyz/*)")
            log.warning(json.dumps(config, indent=4, sort_keys=True))

        # 4. Special configuration for the current branch with exact name match
        if current_branch in app_branch_configuration.keys():
            config.update(app_branch_configuration.get(current_branch))
            log.warning("4. Special configuration for the current branch with exact name match")
            log.warning(json.dumps(config, indent=4, sort_keys=True))

    else:
        log.warning("2. no app-branch configuration provided, using defaults.")

    return config


def retrieve_app_branch_configuration(app_branch_configuration_string: str):
    if not app_branch_configuration_string:
        return {}
    return json.loads(app_branch_configuration_string)
