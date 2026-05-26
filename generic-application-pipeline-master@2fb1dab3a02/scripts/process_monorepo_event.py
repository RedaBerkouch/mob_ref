"""Script to generate PipelineRun k8s manifests and store those in a file. """

import logging
import sys
from utils.config import configure_logging
from utils.file_diff import get_changed_containerfiles
from utils.parser import get_cli_argument_parser
from utils.pipeline_run import generate_pipeline_run_for_changed_containerfile, save_pipeline_run

# Usage:
#python scripts/process_monorepo_event.py \
# --git_rev develop-monorepo \
# --git_from_hash 2b59a0639d8330ce7c14f10653a7054b26c3354d \
# --git_host_url https://bitbucket.bit.admin.ch \
# --git_project_name STIRHOS \
# --git_repo_slug sti-gap-example \
# --git_pusher u80844897 \
# --git_pusher_displayname "Molon Patrick BIT" \
# --git_pusher_email patrick.molon@bit.admin.ch \
# --resource_prefix sti-gap-example-test-monorepo-gp \
# --run_namespace bit-amber-pipelines-d \
# --log_level DEBUG \
# --pipeline_runs_file monorepo-pipeline-runs.yaml \
# --chart_version 9.0 \
# --chart_checksum 12345677 \
# --all_applications_as_json '[{"appName":"app1-with-context-paths","buildImageContextPath":"app1/","containerFilePath":"Containerfile","gitOpsBaseReleaseStage":"dev","gitOpsRepo":"ssh://git@bitbucket.bit.admin.ch/stirhos/sti-gap-example-gitops.git","imageName":"bit/app1","imageRegistry":"bit-sti-docker-hosted.nexus.bit.admin.ch","imageRegistryStaging":"bit-sti-docker-hosted-incoming.nexus.bit.admin.ch","repoType":"bitbucket","srcRepo":"ssh://git@bitbucket.bit.admin.ch/stirhos/sti-gap-example.git","taskComputeResources":{},"taskRunSpecs":{"taskRunSpecs":[]}},{"appName":"app2","containerFilePath":"app2/Dockerfile","gitOpsBaseReleaseStage":"dev","gitOpsRepo":"ssh://git@bitbucket.bit.admin.ch/stirhos/sti-gap-example-gitops.git","imageName":"bit/app2","imageRegistry":"bit-sti-docker-hosted.nexus.bit.admin.ch","imageRegistryStaging":"bit-sti-docker-hosted-incoming.nexus.bit.admin.ch","repoType":"bitbucket","srcRepo":"ssh://git@bitbucket.bit.admin.ch/stirhos/sti-gap-example.git","taskComputeResources":{},"taskRunSpecs":{"taskRunSpecs":[]},"trivyIgnorePath":"app2/.trivyignore_custom_in_app2"},{"appName":"app3","buildImageContextPath":"app3/","gitOpsBaseReleaseStage":"dev","gitOpsRepo":"ssh://git@bitbucket.bit.admin.ch/stirhos/app3.git","imageName":"bit/app3","imageRegistry":"bit-sti-docker-hosted.nexus.bit.admin.ch","imageRegistryStaging":"bit-sti-docker-hosted-incoming.nexus.bit.admin.ch","repoType":"bitbucket","srcRepo":"ssh://git@bitbucket.bit.admin.ch/stirhos/app3.git","taskComputeResources":{},"taskRunSpecs":{"taskRunSpecs":[]}},{"appName":"app4","buildImageContextPath":"app4/","containerFilePath":"Dockerfile","gitOpsBaseReleaseStage":"dev","gitOpsRepo":"ssh://git@bitbucket.bit.admin.ch/stirhos/sti-gap-example-gitops.git","imageName":"bit/app4","imageRegistry":"bit-sti-docker-hosted.nexus.bit.admin.ch","imageRegistryStaging":"bit-sti-docker-hosted-incoming.nexus.bit.admin.ch","repoType":"bitbucket","srcRepo":"ssh://git@bitbucket.bit.admin.ch/stirhos/sti-gap-example.git","taskComputeResources":{},"taskRunSpecs":{"taskRunSpecs":[]}}]'


def main():
    """Main function for execution of application logic."""
    #
    # Get data and configure application
    #

    args = get_cli_argument_parser().parse_args()
    log = logging.getLogger()
    configure_logging(args, log)

    #
    # Initialize variables for later usage
    #

    pipeline_runs = []
    pipeline_runs_file = args.pipeline_runs_file

    #
    # Perform core application logic
    #

    changed_containerfiles = get_changed_containerfiles(args, log)

    for containerfile_path in changed_containerfiles:
        generate_pipeline_run_for_changed_containerfile(containerfile_path,
                                                        args,
                                                        pipeline_runs,
                                                        log)


    save_pipeline_run(plx_texts=pipeline_runs, pipeline_runs_file=pipeline_runs_file, log=log)


if __name__ == "__main__":
    main()
