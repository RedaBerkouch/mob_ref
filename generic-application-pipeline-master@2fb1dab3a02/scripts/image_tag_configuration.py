import json
import logging
import re
import subprocess
import sys
from datetime import datetime


def main():
    args = sys.argv
    current_branch = args[1]
    build_version = args[2]
    build_directory = args[3]
    date_pattern = args[4]
    version_file = args[5]
    version_replacement_string = args[6]

    if not date_pattern:
        date_pattern = "%Y-%m-%dT%H%M%S"

    determine_build_version(build_version,
                            build_directory,
                            current_branch,
                            date_pattern,
                            version_file,
                            version_replacement_string)


def write_result(content, path):
    if isinstance(content, bool):
        str_content = ("false", "true")[content]
    else:
        str_content = content

    logging.info(path + ": " + str_content)
    open(path, "w").write(str_content)


def determine_build_version(build_version_string, workspace_path, branch_name, date_pattern, version_file, version_replacement_string):
    logging.info(f"Determine build version for template string: {build_version_string}")
    # get_image_version_from_tektonfile(version_file, "imageVersion")
    version_string = generate_version_string(build_version_string, workspace_path, branch_name,
                                             date_pattern, version_file, version_replacement_string)
    check_version_validity(version_string)
    write_result(version_string, "/tekton/results/imageTag")


def check_version_validity(build_version_string):
    # DOCKER The tag must be valid ASCII and can contain lowercase and uppercase letters, digits, underscores,
    # periods, and hyphens. It cannot start with a period or hyphen and must be no longer than 128 characters.

    docker_validation_regex = (r"^(?:(?=[^:\/]{1,253})(?!-)[a-zA-Z0-9-]{1,63}(?<!-)(?:\.(?!-)[a-zA-Z0-9-]{1,"
                               r"63}(?<!-))*(?::[0-9]{1,5})?/)?((?![._-])(?:[a-z0-9._-]*)(?<![._-])(?:/(?![._-])["
                               r"a-z0-9._-]*(?<![._-]))*)(?::(?![.-])[a-zA-Z0-9_.-]{1,128})?$")

    return is_valid_to_regex(build_version_string, docker_validation_regex)


def is_valid_to_regex(str, pattern):
    regex = re.compile(pattern, re.I)
    return bool(regex.match(str))


def generate_version_string(template_string, workspace_path, branch_name, date_pattern, version_file, version_replacement_string):
    image_version_pattern = "<TEKTONFILE_VERSION>"
    timestamp_pattern = "<TIMESTAMP>"
    name_pattern = "<BRANCH_NAME>"
    hash_pattern_short = "<COMMIT_HASH_SHORT>"
    hash_pattern = "<COMMIT_HASH>"

    date_time = datetime.now().strftime(date_pattern)
    version_string = update_version_part(template_string, timestamp_pattern, date_time)

    if image_version_pattern in template_string:
        image_version = get_image_version_from_tektonfile(workspace_path, version_file, "imageVersion")
        logging.info(f"Setting IMAGE TAG: {image_version}")
        version_string = update_version_part(version_string, image_version_pattern, image_version)

    if name_pattern in template_string:
        logging.info(f"Setting BRANCH NAME: {branch_name}")
        version_string = update_version_part(version_string, name_pattern, branch_name)

    if hash_pattern_short in template_string:
        commit_hash = get_current_commit_hash(workspace_path, "short")
        logging.info(f"Setting BRANCH HASH SHORT: {commit_hash}")
        version_string = update_version_part(version_string, hash_pattern_short, commit_hash)

    if hash_pattern in template_string:
        commit_hash = get_current_commit_hash(workspace_path, "long")
        logging.info(f"Setting BRANCH HASH: {commit_hash}")
        version_string = update_version_part(version_string, hash_pattern, commit_hash)

    # Convert version according to OCI spec: The tag must be valid ASCII and can contain lowercase and uppercase letters, digits, underscores, periods, and hyphens
    oci_sanitized_version_string = re.sub("[^a-zA-Z0-9-._]", version_replacement_string, version_string)
    if oci_sanitized_version_string != version_string:
        logging.info(f"Replaced non-OCI compliant characters with '{version_replacement_string}': {version_string} -> {oci_sanitized_version_string}")

    return oci_sanitized_version_string


def update_version_part(version_string, pattern, new_value):
    logging.debug(f"In: {version_string} replacing {pattern}->{new_value}")
    version_string = version_string.replace(pattern, new_value)
    logging.debug(f"  -> {version_string}")
    return version_string


def get_current_commit_hash(source_workspace_path, sha_length):
    trust_current_git_repo(source_workspace_path)
    if sha_length == "short":
        git_cmd = ['git', 'rev-parse', '--short', 'HEAD']
    elif sha_length == "long":
        git_cmd = ['git', 'rev-parse', 'HEAD']
    result = subprocess.run(git_cmd, stdout=subprocess.PIPE, cwd=source_workspace_path)
    commit_hash = result.stdout.decode("utf-8").strip()
    logging.debug(f"current branch hash: {commit_hash}")
    return commit_hash


def trust_current_git_repo(source_workspace_path):
    git_command_result = subprocess.run(['git', 'config', '--global', '--add', 'safe.directory', source_workspace_path],
                                        stdout=subprocess.PIPE, cwd=source_workspace_path)
    git_log = git_command_result.stdout.decode("utf-8")
    logging.debug(f"Trust directory result: {git_log}")


def get_image_version_from_tektonfile(source_workspace_path, version_file, key):
    try:
        f = open(source_workspace_path + "/" + version_file)
        data = json.load(f)
        image_version = data.get(key)
        logging.debug(f"Image tag: {image_version}")
    except Exception as e:
        logging.error(f"Could not load or parse image version from Tektonfile")
        logging.error(e)
        image_version = ""
    finally:
        return image_version


if __name__ == "__main__":
    logging.basicConfig(encoding='utf-8', level=logging.INFO)
    main()
