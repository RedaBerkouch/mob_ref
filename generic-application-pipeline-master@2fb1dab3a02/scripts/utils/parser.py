import argparse

def get_cli_argument_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description='Get file diffs from a repository')
    parser.add_argument('--git_from_hash', required=True, help='Commit hash to start the diff')
    parser.add_argument('--git_rev', required=True, help='Commit hash to compare against')
    parser.add_argument('--git_host_url', required=True, help='Bitbucket host URL including protocol, example: https://bitbucket.bit.admin.ch')
    parser.add_argument('--git_project_name', required=True, help='Name of the project in Bitbucket')
    parser.add_argument('--git_repo_slug', required=True, help='Slug of the repository in Bitbucket')
    parser.add_argument('--git_pusher', required=True, help='Username of the git user')
    parser.add_argument('--git_pusher_displayname', required=True, help='User display name of the git user')
    parser.add_argument('--git_pusher_email', required=True, help='E-Mail address of the git user')
    parser.add_argument('--resource_prefix', required=True, help='Kubernetes resource prefix, needed for identification')
    parser.add_argument('--run_namespace', required=True, help='The run namespace for the PipelineRun')
    parser.add_argument('--log_level', required=False, default='INFO', help='The pipeline name to be referenced')
    parser.add_argument('--pipeline_runs_file', required=False, default='/doesnot/monorepo-pipeline-runs.yaml', help='Name of the generated PipelineRuns defintion file')
    parser.add_argument('--pipeline_runs_template_path', required=False, default='scripts/template-gap-run.yaml', help='Path to the template file to generate the PipelineRuns')
    parser.add_argument('--build_id', required=False, help='Build ID of an ongoing Azure DevOps Pipeline, such as: https://devops-server.admin.ch/DefaultCollection/BIT_FMZ_APP/_build/results?buildId=197742&view=results')
    parser.add_argument('--all_applications_as_json', required=True, help='All applications as known by Helm in Json format')
    parser.add_argument('--chart_version', required=True, help='The version of the Helm chart. Will be passed to PipelineRun as label')
    parser.add_argument('--chart_checksum', required=True, help='The checksum of the Helm chart. Will be passed to PipelineRun as label')

    return parser
