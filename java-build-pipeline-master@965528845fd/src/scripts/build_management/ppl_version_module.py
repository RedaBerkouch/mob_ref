# version.py
import os
import re
import sys
from datetime import datetime
import xml.etree.ElementTree as elementTree


# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util', '../git_operations'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
from ppl_logging_module import log
from ppl_git_branch_module import get_current_branch_hash


# ----- Modul variables -----
supported_build_tools = ["maven"]


def determine_build_version(version_template_string: str,
                            ws_workspace_path: str,
                            run_directory: str,
                            branch_name: str,
                            date_pattern: str,
                            publish_version_tag: bool,
                            build_tool: str,
                            run_name: str) -> str:
    log.warning("\ndetermine build version:")
    log.warning(f"...for template string: {version_template_string}")
    build_version = "1.0.0"
    source_path = f'{ws_workspace_path}/{run_directory}/source'
    os.system(f'touch {ws_workspace_path}/{run_directory}/.logging')

    if build_tool == "maven":
        parser = elementTree.XMLParser(encoding="utf-8")
        ns = {'p': 'http://maven.apache.org/POM/4.0.0'}
        project = elementTree.parse(f"{source_path}/pom.xml", parser=parser).getroot()
        build_version = project.find('p:version', ns).text
        if publish_version_tag:
            build_version = build_version.replace("-SNAPSHOT", "")

    version_string = generate_version_string(template_string=version_template_string,
                                             source_path=source_path,
                                             branch_name=branch_name,
                                             date_pattern=date_pattern,
                                             build_version=build_version,
                                             plr_uid=run_name[-5:])
    check_version_validity(version_string)

    return version_string


def determine_build_tool(tool: str):
    log.warning(f"build tooling is specified as: {tool}")
    if tool in supported_build_tools:
        return tool
    else:
        return "maven"


def check_version_validity(version_template_string: str):
    # DOCKER
    # The tag must be valid ASCII and can contain lowercase and uppercase letters, digits, underscores, periods,
    # and hyphens. It cannot start with a period or hyphen and must be no longer than 128 characters.
    # MAVEN
    # The string MUST be comprised of only alphanumerics plus dash [0-9A-Za-z-] and MUST begin with an alpha
    # character [A-Za-z].

    docker_validation_regex = r"^(?:(?=[^:\/]{1,253})(?!-)[a-zA-Z0-9-]{1,63}(?<!-)(?:\.(?!-)[a-zA-Z0-9-]{1,63}(?<!-))*(?::[0-9]{1,5})?/)?((?![._-])(?:[a-z0-9._-]*)(?<![._-])(?:/(?![._-])[a-z0-9._-]*(?<![._-]))*)(?::(?![.-])[a-zA-Z0-9_.-]{1,128})?$"

    # This regex pattern accommodates various versioning styles:
    #
    # Basic numeric versions: 1, 1.2, 1.2.3
    # Versions with suffixes: 1-beta, 1.2-beta, 1.2.3-alpha
    maven_validation_regex = r"^(\d+)(\.\d+)(\.\d+)$|^(\d+)(\.\d+)$|^(\d+)$|^(\d+)(\.\d+)(-.+)$|^(\d+)(-.+)$|^(\d+)(\.\d+)(\.\d+)(-.+)$"

    return (is_valid_to_regex(input_string=version_template_string,
                              pattern=docker_validation_regex)
            and is_valid_to_regex(input_string=version_template_string,
                                  pattern=maven_validation_regex))


def is_valid_to_regex(input_string: str, pattern: str):
    regex = re.compile(pattern, re.I)
    return bool(regex.match(input_string))


def generate_version_string(template_string: str,
                            source_path: str,
                            branch_name: str,
                            date_pattern: str,
                            build_version: str,
                            plr_uid: str):
    timestamp_pattern = "<TIMESTAMP>"
    name_pattern = "<BRANCH_NAME>"
    hash_pattern = "<BRANCH_HASH>"
    pom_pattern = "<POM_VERSION>"
    app_pattern = "<APP_VERSION>"
    plr_pattern = "<PLR_UID>"

    date_time = datetime.now().strftime(date_pattern)
    version_string = update_version_part(version_string=template_string, pattern=timestamp_pattern, new_value=date_time)

    if name_pattern in template_string:
        branch_name = branch_name.replace("/", "-")
        log.warning(f"setting BRANCH NAME: {branch_name}")
        version_string = update_version_part(version_string=version_string, pattern=name_pattern, new_value=branch_name)

    if hash_pattern in template_string:
        branch_hash = get_current_branch_hash(source_path)
        log.warning(f"setting BRANCH HASH: {branch_hash}")
        version_string = update_version_part(version_string=version_string, pattern=hash_pattern, new_value=branch_hash)

    if pom_pattern in template_string:
        log.warning(f"setting POM VERSION: {build_version}")
        version_string = update_version_part(version_string=version_string, pattern=pom_pattern,
                                             new_value=build_version)

    if app_pattern in template_string:
        log.warning(f"setting APP VERSION: {build_version}")
        version_string = update_version_part(version_string=version_string, pattern=app_pattern,
                                             new_value=build_version)

    if plr_pattern in template_string:
        log.warning(f"setting PLR UID: {plr_uid}")
        version_string = update_version_part(version_string=version_string, pattern=plr_pattern, new_value=plr_uid)

    return version_string


def update_version_part(version_string: str,
                        pattern: str,
                        new_value: str):
    log.info(f"In: {version_string} replacing {pattern}->{new_value}")
    version_string = version_string.replace(pattern, new_value)
    log.info(f"  -> {version_string}")
    return version_string
