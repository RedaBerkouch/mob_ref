# ppl_notify_failure.py
import os
import sys
import subprocess

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
import ppl_logging_module
from ppl_logging_module import log
from ppl_notification_link import get_notification_link

# ----- Modul parameter -----
compliant_mode = True


def get_failed_tasks(pipeline_run_name: str):
    log.info("Get failed tasks from PipelineRun object")

    pipeline_tasks = (subprocess.run(f"""oc get pr {pipeline_run_name} -o json \
                    | jq -r '.status.childReferences[].name' \
                """, shell=True, stdout=subprocess.PIPE).stdout.splitlines())
    log.info(f"Pipeline tasks: {pipeline_tasks}")

    failed_tasks = []
    for task in pipeline_tasks:
        task_name = task.decode("utf-8")
        task_output = (subprocess.check_output(f"""oc get taskrun {task_name} -o json \
                    | jq -r '.status.conditions[].status' \
                """, shell=True)).decode("utf-8")
        task_status = task_output.strip()
        log.info(f"Status of {task_name} is: {task_status}")

        if task_status == 'False':
            failed_tasks.append(task_name)

    return failed_tasks

def get_notification_texts(failed_tasks: [], cluster_console_url: str,
                           pipeline_run_namespace: str, pipeline_run_name: str):
    if failed_tasks:
        subjectText = "failed"
        failed_tasks_logs = ""
        for failed_task in failed_tasks:
            log.debug(f"Getting logs for failed task: {failed_task}")
            pod_name_output = (subprocess.check_output(f"""oc get taskrun {failed_task} -o json \
                    | jq -r '.status.podName' \
                """, shell=True)).decode("utf-8")
            pod_name = pod_name_output.strip()
            task_logs = (subprocess.check_output(f"oc logs {pod_name} --all-containers", shell=True)).decode("utf-8")
            failed_tasks_logs += f"{failed_task}: {task_logs}"

        log.info(f"Failed pipeline tasks: {failed_tasks}")

        text = f"Failed tasks: {failed_tasks}\n"
        text += f"\n"
        text += get_notification_link(cluster_console_url=cluster_console_url, pipeline_run_namespace=pipeline_run_namespace, pipeline_run_name=pipeline_run_name)
        text += f"\n"
        text += f"Logs:\n"
        text += f"{failed_tasks_logs}\n"

        return subjectText, text

    return None, None


def get_cluster_console_url():
    return (subprocess.check_output('oc whoami --show-console', shell=True)).decode("utf-8")
