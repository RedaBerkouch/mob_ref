# ppl_branch_config_module.py

"""
This module is responsible for configuring branch-specific build and deployment settings
based on the current branch, application type, and other parameters. It integrates various
modules to handle logging, versioning, namespace determination, and writing results for
use in Tekton pipeline steps.
"""

import os
import sys

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util', '../config_management', '../build_management'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
from ppl_logging_module import log
from ppl_values_config_module import get_branch_configuration
from ppl_common_module import write_result
from ppl_version_module import determine_build_version
from ppl_version_module import determine_build_tool
from ppl_namespace_stage_module import determine_target_namespace


def do_branch_configuration(
        default_branch_configuration_string: str,
        app_branch_configuration_string: str,
        current_branch: str,
        app_type: str,
        build_tool: str,
        app_name: str,
        version_template: str,
        ws_workspace_path: str,
        run_directory: str,
        run_name: str,
        date_pattern: str,
        compliant_mode: bool
):
    """
    Configures branch-specific build and deployment tasks based on the provided parameters.

    This function sets the configuration for builds, including the build tool, versioning,
    scanning options, and whether to deploy to specific environments based on the branch
    and app type. It writes several results to the Tekton pipeline results, which guide
    downstream tasks.

    Parameters:
    -----------
    default_branch_configuration_string : str
        Default configuration for branches not specifically defined.
    app_branch_configuration_string : str
        Application-specific configuration string for branches.
    current_branch : str
        The name of the current branch being built (e.g., "main", "develop", "feature/xyz").
    app_type : str
        The type of the application being built (e.g., "library", "udeploy").
    build_tool : str
        The build tool to use (e.g., "maven", "gradle").
    app_name : str
        The name of the application.
    version_template : str
        Template string used for generating the version.
    ws_workspace_path : str
        Path to the workspace for this build.
    run_directory : str
        Directory for the current run.
    run_name : str
        The name of the current run or build process.
    date_pattern : str
        Date pattern used for versioning.
    namespace_prefix : str
        Namespace prefix used for determining the target namespace.
    ppl_namespace_stage : str
        Stage for determining the namespace based on the deployment environment.
    compliant_mode : bool
        Whether the build is running in compliant mode, which enables additional scans and checks.

    Returns:
    --------
    None

    Side Effects:
    -------------
    - Writes various configuration values to Tekton results (e.g., deploy stage, build image, versioning).
    - Determines and logs build tools and namespace configuration.
    - Adjusts configuration settings for library or udeploy applications.
    """

    config = get_branch_configuration(default_branch_configuration_string=default_branch_configuration_string,
                                      app_branch_configuration_string=app_branch_configuration_string,
                                      current_branch=current_branch,
                                      app_type=app_type,
                                      version_template=version_template)

    build_image = False
    build_tool = determine_build_tool(build_tool)
    log.warning(f"using build tool: {build_tool}")

    if config.get("deployStage") != "none" or config.get("publishVersionTag"):
        build_image = True

    trivy_image_scan = False
    if build_image and config.get("scanImage"):
        trivy_image_scan = True

    acs_image_scan = trivy_image_scan
    if build_image and config.get("scanImageWithAcs"):
        acs_image_scan = True

    # disabling image related task when we only build a library
    maven_deploy = False
    ucd_deploy = False
    if app_type == "library":
        build_image = False
        trivy_image_scan = False
        config.update({"deployStage": "none"})
        maven_deploy = True
        ucd_deploy = False

    if app_type == "udeploy":
        build_image = False
        trivy_image_scan = False
        config.update({"deployStage": "none"})
        maven_deploy = False
        ucd_deploy = True

    if compliant_mode:
        trivy_image_scan = True
        acs_image_scan = True

    publish_version_tag = config.get("publishVersionTag")
    version_template_string = config.get('buildVersionTemplate')

    publish_git_tag = publish_version_tag
    if publish_git_tag and config.get("publishGitTag") == False:
        publish_git_tag = False

    version_string = determine_build_version(version_template_string=version_template_string,
                                             ws_workspace_path=ws_workspace_path,
                                             run_directory=run_directory,
                                             branch_name=current_branch,
                                             date_pattern=date_pattern,
                                             publish_version_tag=publish_version_tag,
                                             build_tool=build_tool,
                                             run_name=run_name)

    log.warning("\nConfiguration results for this build:")
    write_result(publish_git_tag, "/tekton/results/push_tag")
    write_result(publish_version_tag, "/tekton/results/push_image_tag")
    write_result(config.get("deployStage"), "/tekton/results/deploy_stage")
    write_result(build_image, "/tekton/results/build_image")
    write_result(build_tool, "/tekton/results/build_tool")
    write_result(trivy_image_scan, "/tekton/results/trivy_image_scan")
    write_result(acs_image_scan, "/tekton/results/acs_image_scan")
    write_result(config.get("qualityCheck"), "/tekton/results/quality_check")
    write_result(config.get("qualityGate"), "/tekton/results/quality_gate")
    write_result(maven_deploy, "/tekton/results/maven_deploy")
    write_result(ucd_deploy, "/tekton/results/ucd_deploy")

    # snapshot-version contains the version even if publishing is disabled.
    # this will contain a release-version IF publishing is enabled.
    write_result(version_string, "/tekton/results/snapshot_version")

    if publish_version_tag:
        image_with_version = f"{app_name}:{version_string}"
    else:
        image_with_version = app_name
        version_string = ""

    write_result(version_string, "/tekton/results/version")
    write_result(image_with_version, "/tekton/results/image_with_version")

    verify_deployment = False
    if config.get("verifyDeployment"):
        verify_deployment = True
    write_result(verify_deployment, "/tekton/results/verify_deployment")
    
    return config