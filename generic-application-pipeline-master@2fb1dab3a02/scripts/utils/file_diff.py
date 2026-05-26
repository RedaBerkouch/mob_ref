import json
from logging import Logger
import os
import sys
from typing import Dict
from urllib.error import HTTPError, URLError
from urllib.request import HTTPBasicAuthHandler, Request, urlopen

import requests

DEFAULT_CONTAINERFILE_PATH = "Dockerfile"
DEFAULT_BUILD_IMAGE_CONTEXT_PATH = "."
DEFAULT_EMPTY_BUILD_IMAGE_CONTEXT_PATH = ""

def load_all_apps_json_by_cli_args(args: Dict[str, str]):
    return json.loads(args.all_applications_as_json)

def reduce_all_apps_json_to_build_image_context_paths(args: Dict[str, str]):
    all_apps_as_json = load_all_apps_json_by_cli_args(args)
    return [app_json.get('buildImageContextPath', DEFAULT_BUILD_IMAGE_CONTEXT_PATH) for app_json in all_apps_as_json]

def reduce_all_apps_json_to_app_names(args: Dict[str, str]):
    all_apps_as_json = load_all_apps_json_by_cli_args(args)
    return [app_json.get('appName', DEFAULT_BUILD_IMAGE_CONTEXT_PATH) for app_json in all_apps_as_json]

def reduce_all_apps_json_to_container_file_paths(args: Dict[str, str]):
    all_apps_as_json = load_all_apps_json_by_cli_args(args)
    return [ app_json.get('buildImageContextPath', DEFAULT_EMPTY_BUILD_IMAGE_CONTEXT_PATH) + app_json.get('containerFilePath', DEFAULT_CONTAINERFILE_PATH) for app_json in all_apps_as_json]

def get_containerfile_app_mapping_by_args(args) -> Dict[str, str]:
    containerfile_paths = reduce_all_apps_json_to_container_file_paths(args)
    app_names = reduce_all_apps_json_to_app_names(args)

    return dict(zip(containerfile_paths, app_names))

def get_containerfile_app_mapping(containerfile_paths: list[str], app_names: list[str]) -> Dict[str, str]:
    return dict(zip(containerfile_paths, app_names))

def get_changed_containerfiles(args: str,
                               log: Logger) -> list[str]:
    app_names = reduce_all_apps_json_to_app_names(args)
    containerfile_paths = reduce_all_apps_json_to_container_file_paths(args)
    containerfile_image_build_context_paths = reduce_all_apps_json_to_build_image_context_paths(args)

    #if len(args.app_names) != len(args.containerfile_paths):
    if len(app_names) != len(containerfile_paths) != len(containerfile_image_build_context_paths):
        log.error("The number of app_names must match the number of containerfile_paths and containerfile_image_build_context_paths.")
        sys.exit(1)

    # Create a mapping between containerfile_paths and app_names
    #TODO: could be refactored to use only all_apps_as_json for whole script
    containerfile_app_mapping = get_containerfile_app_mapping(containerfile_paths=containerfile_paths, app_names=app_names)
    log.info(f"Application to Containerfile mapping: {containerfile_app_mapping}")

    #TODO: could be refactored to use only all_apps_as_json for whole script
    return get_file_diff(
                                        git_from_hash=args.git_from_hash,
                                        git_rev=args.git_rev,
                                        git_host_url=args.git_host_url,
                                        git_project_name=args.git_project_name,
                                        git_repo_slug=args.git_repo_slug,
                                        containerfile_paths=containerfile_paths,
                                        log=log)


def get_file_diff(git_from_hash: str,
                  git_rev: str,
                  git_host_url: str,
                  git_project_name: str,
                  git_repo_slug: str,
                  containerfile_paths: list[str],
                  log: Logger) -> list[str]:
    
    try:
        bb_access_token = os.getenv('BITBUCKET_HTTP_ACCESS_TOKEN')
        azure_access_token = os.getenv('AZURE_HTTP_ACCESS_TOKEN')

        if not bb_access_token and not azure_access_token:
            log.error("No access token found. Make sure that either 'BITBUCKET_HTTP_ACCESS_TOKEN' or 'AZURE_HTTP_ACCESS_TOKEN' is set.")
            sys.exit(1)

        file_diffs = []
        # there is a possibility, that both access token are available
        if "bitbucket" in git_host_url:
            file_diffs += get_file_diff_bitbucket(git_from_hash, git_rev, git_host_url, git_project_name, git_repo_slug, containerfile_paths, bb_access_token, log)
        if "devops" in git_host_url:
            file_diffs += get_file_diff_azure(git_from_hash, git_rev, git_host_url, git_project_name, git_repo_slug, containerfile_paths, azure_access_token, log)

        return file_diffs
    
    except Exception as e:
        log.error(f"Error while getting file diff: {e}")
        sys.exit(1)

def get_file_diff_bitbucket(git_from_hash: str,
                            git_rev: str,
                            git_host_url: str,
                            git_project_name: str,
                            git_repo_slug: str,
                            containerfile_paths: list[str],
                            bb_access_token: str,
                            log: Logger) -> list[str]:

    # Construct the Bitbucket compare API URL
    # Bitbucket offers a compare API to get file changes between commits
    # https://developer.atlassian.com/server/bitbucket/rest/v819/api-group-repository/#api-api-latest-projects-projectkey-repos-repositoryslug-compare-diff-path-get
    # Example: https://bitbucket.bit.admin.ch/rest/api/latest/projects/STIRHOS/repos/sti-gap-example/compare/diff?from=c258912234d7c5ffeee7781da978c210eaccee93&to=0dedea8f1076aac7560afbe499790ef4bc73204f&contextLines=0
    bb_compare_hash_url = (
        f"{git_host_url}/rest/api/latest/projects/{git_project_name}/repos/{git_repo_slug}/compare/diff?from={git_rev}&to={git_from_hash}&contextLines=0")
    log.info(f"Getting file diff from: {bb_compare_hash_url}")

    headers = {"Authorization": f"Bearer {bb_access_token}"}
    http_request = Request(bb_compare_hash_url, headers=headers)

    try:
        http_result = urlopen(http_request).read().decode('utf-8')
    except HTTPError as e:
        log.error(f"HTTPError: {e.code} {e.reason}")
        sys.exit(1)
    except URLError as e:
        log.error(f"URLError: {e.reason}")
        sys.exit(1)
    except Exception as e:
        log.error(f"Unexpected error: {e}")
        sys.exit(1)

    try:
        json_data = json.loads(http_result)
    except json.JSONDecodeError as e:
        log.error(f"Failed to decode JSON response: {e}")
        sys.exit(1)

    try:
        destination_toStrings = [diff['destination']['toString'] for diff in json_data['diffs'] if diff['destination']]
    except KeyError as e:
        log.error(f"KeyError in JSON response: {e}")
        sys.exit(1)

    # Filter destination_toStrings based on containerfile_paths
    filtered_paths = [path for path in destination_toStrings if any(item in path for item in containerfile_paths)]

    log.info(f"Detected changed paths in Bitbucket: {filtered_paths}")
    return filtered_paths

def get_file_diff_azure(git_from_hash: str,
                            git_rev: str,
                            git_host_url: str,
                            git_project_name: str,
                            git_repo_slug: str,
                            containerfile_paths: list[str],
                            azure_access_token: str,
                            log: Logger) -> list[str]:

    # Construct the Azure DevOps compare API URL
    # Azure DevOps offers a compare API to get file changes between commits
    # https://learn.microsoft.com/en-us/rest/api/azure/devops/git/diffs/get?view=azure-devops-server-rest-7.0&tabs=HTTP#uri-parameters
    # Example: curl -u :${YOUR_PERSONAL_ACCESS_TOKEN} https://devops-server.admin.ch/DefaultCollection/BIT_FMZ_APP/_apis/git/repositories/BIT_FMZ_APP/diffs/commits?baseVersion=11e6275a91144fa299e031a51cc76407f76171f1&baseVersionType=commit&targetVersion=fc22802766e75ce5df8b6ce8e6512c19b7faf225&targetVersionType=commit&api-version=7.1
    azure_devops_compare_hash_url = (
        #f"{git_host_url}/DefaultCollection/{git_project_name}/_apis/git/repositories/{git_repo_slug}/diffs/commits?baseVersion={git_from_hash}&baseVersionType=commit&targetVersion={git_rev}&targetVersionType=commit&api-version=7.1"
        f"{git_host_url}/DefaultCollection/{git_project_name}/_apis/git/repositories/{git_repo_slug}/commits/{git_from_hash}/changes?api-version=7.0"
    )
    log.info(f"Getting file diff from: {azure_devops_compare_hash_url}")

    try:
        http_result = requests.get(azure_devops_compare_hash_url, auth = HTTPBasicAuthHandler('', azure_access_token))
    except requests.HTTPError as e:
        log.error(f"HTTPError: {e.code} {e.reason}")
        sys.exit(1)
    except URLError as e:
        log.error(f"URLError: {e.reason}")
        sys.exit(1)
    except Exception as e:
        log.error(f"Unexpected error: {e}")
        sys.exit(1)

    try:
        json_data = json.loads(http_result.content)
    except json.JSONDecodeError as e:
        log.error(f"Failed to decode JSON response: {e}")
        sys.exit(1)

    try:
        # Note: changes.item.path returned by yAzure Devops are slash prefixed, removing it in order to match later
        destination_toStrings = [diff['item']['path'].lstrip('/') for diff in json_data['changes']]
    except KeyError as e:
        log.error(f"KeyError in JSON response: {e}")
        sys.exit(1)

    # Filter destination_toStrings based on containerfile_paths
    filtered_paths = [path for path in destination_toStrings if any(item in path for item in containerfile_paths)]

    log.info(f"Detected changed paths in Azure: {filtered_paths}")
    return filtered_paths
