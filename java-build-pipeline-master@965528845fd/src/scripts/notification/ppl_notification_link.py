# ppl_notification_link.py
import sys
import os

module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))



def get_notification_link(cluster_console_url: str,
                          pipeline_run_namespace: str,
                          pipeline_run_name: str):
    return f"{cluster_console_url.strip()}/k8s/ns/{pipeline_run_namespace}/tekton.dev~v1~PipelineRun/{pipeline_run_name}\n"


