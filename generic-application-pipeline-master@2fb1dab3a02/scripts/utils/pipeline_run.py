"""PipelineRun utility"""

import json
from logging import Logger
import os
import sys
from jinja2 import Environment, FileSystemLoader, TemplateNotFound
import yaml

from utils.file_diff import get_containerfile_app_mapping_by_args, load_all_apps_json_by_cli_args

DEFAULT_BUILD_CONTEXT_PATH = "."
NO_MAPPING_FOR_CONTAINERFILE_PATH_FOUND = None

def save_pipeline_run(plx_texts: list[str], pipeline_runs_file: str, log: Logger):
    try:
        log.info(f"Generating file: {pipeline_runs_file}")
        try:
            with open(pipeline_runs_file, 'w') as file:
                for curr_pipeline_run in plx_texts:
                    file.write(curr_pipeline_run)
                    file.write("\n---\n")
        except IOError as e:
            log.error(f"Failed to write to file {pipeline_runs_file}: {e}")
            sys.exit(1)

        try:
            with open(pipeline_runs_file, 'r') as file:
                lines = file.readlines()
                for line in lines:
                    log.debug(f">> {line.rstrip()}")
        except IOError as e:
            log.error(f"Failed to read from file {pipeline_runs_file}: {e}")
    except Exception as e:
        log.error("Error while saving pipeline_run: %s", e)
        sys.exit(1)

def generate_pipeline_run(containerfile_path: str,
                          build_image_context_path: str,
                          resource_prefix: str,
                          git_rev: str,
                          git_pusher: str,
                          git_pusher_email: str,
                          git_pusher_displayname: str,
                          run_namespace: str,
                          app_name: str,
                          pipeline_runs_template_path: str,
                          build_id: str,
                          repo_type: str,
                          task_run_specs: dict,
                          chart_version: str,
                          chart_checksum: str,
                          log: Logger) -> str:
    
    template_abs_path = pipeline_runs_template_path
    template_dir = os.path.dirname(template_abs_path)
    template_file = os.path.basename(template_abs_path)
    environment = Environment(loader=FileSystemLoader(template_dir))

    try:
        template = environment.get_template(template_file)
    except TemplateNotFound as e:
        log.error(f"Template file not found: {e}")
        sys.exit(1)

    task_run_specs_as_str = yaml.safe_dump(yaml.safe_load(json.dumps(task_run_specs)), default_flow_style=False)
    # Python's builtin yaml lib does not support initial indentation for yaml fragment, workaround is to indent manually with hardcode
    #TODO: function could be refactored to such pseudo code: yaml->json->template->yaml to avoid this workaround
    task_run_specs_as_str_indentation_fixed = '\n'.join([' ' * 2 + line for line in task_run_specs_as_str.splitlines()])
    try:
        content = template.render(
            app_name=app_name,
            containerfile_path=containerfile_path,
            build_image_context_path=build_image_context_path,
            resource_prefix=resource_prefix,
            git_rev=git_rev,
            git_pusher=git_pusher,
            git_pusher_email=git_pusher_email,
            git_pusher_displayname=git_pusher_displayname,
            run_namespace=run_namespace,
            build_id=build_id,
            repo_type=repo_type,
            task_run_specs=task_run_specs_as_str_indentation_fixed,
            chart_version=chart_version,
            chart_checksum=chart_checksum
        )
    except Exception as e:
        log.error(f"Failed to render template: {e}")
        sys.exit(1)

    return content

def get_app_name_by_containerfile_path(found_containerfile_path: str, containerfile_app_mappings: list[any]) -> str:
    # Determine the corresponding app_name for the changed container file

    for containerfile_path_to_compare, app_name in containerfile_app_mappings.items():
        if found_containerfile_path == containerfile_path_to_compare:
            return app_name

    return NO_MAPPING_FOR_CONTAINERFILE_PATH_FOUND

def generate_pipeline_run_for_changed_containerfile(containerfile_path: str,
                                                    args: str,
                                                    pipeline_runs: list[any],
                                                    log: Logger) -> None:
    containerfile_app_mappings = get_containerfile_app_mapping_by_args(args)

    # Determine the corresponding app_name for the changed container file
    app_name = get_app_name_by_containerfile_path(containerfile_path, containerfile_app_mappings)
    log.info("\"%s\" is being mapped to application \"%s\"", containerfile_path, app_name)

    if app_name is None:
        log.warning(f"No matching app_name found for container file: {containerfile_path}")
        return
    try:
        app_as_json = (next(app for app in load_all_apps_json_by_cli_args(args) if app['appName'] == app_name))
        #TODO: could be refactored to use only all_applications_as_json for whole script

        # set bitbucket as default repo, if it is not set in repoType
        app_as_json.setdefault('repoType', 'bitbucket')

        log.debug(f"Repo Type for this app is: {app_as_json['repoType']}")

        pipeline_run_text = generate_pipeline_run(app_name=app_name,
                                containerfile_path=containerfile_path,
                                build_image_context_path=app_as_json.get('buildImageContextPath', DEFAULT_BUILD_CONTEXT_PATH),
                                resource_prefix=args.resource_prefix,
                                git_rev=args.git_from_hash if app_as_json['repoType'] == 'azure' else args.git_rev,
                                git_pusher=args.git_pusher,
                                git_pusher_displayname=args.git_pusher_displayname,
                                git_pusher_email=args.git_pusher_email,
                                run_namespace=args.run_namespace,
                                pipeline_runs_template_path=args.pipeline_runs_template_path,
                                build_id=args.build_id,
                                repo_type=app_as_json['repoType'],
                                task_run_specs=app_as_json['taskRunSpecs'],
                                chart_version=args.chart_version,
                                chart_checksum=args.chart_checksum,
                                log=log)
        pipeline_runs.append(pipeline_run_text)
    except Exception as e:
        log.error(f"Error while generating pipeline_run for {containerfile_path}: {e}")
        return

