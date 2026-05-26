import subprocess
import json
import time
from typing import Optional, Tuple

from logger import get_logger

logger = get_logger()

def get_pipelinerun_name_by_event_id(event_id: str, namespace: str = 'bit-jme-test-pipelines-d', timeout: int = 1200, poll_interval: int = 10) -> Optional[str]:
    """
    Searches for the PipelineRun name with the given event_id in the specified namespace.

    Args:
        event_id (str): The triggers.tekton.dev/triggers-eventid label value.
        namespace (str, Optional): The namespace to search in. Default is 'bit-jme-test-pipelines-d'.
        timeout (int, optional): Maximum time to wait in seconds. Default is 1200.
        poll_interval (int, optional): Poll interval in seconds. Default is 10.

    Returns:
        str | None: The PipelineRun name if found, otherwise None.
    """
    label = f"triggers.tekton.dev/triggers-eventid={event_id}"
    start_time = time.time()
    logger.debug(f"Searching for PipelineRun with event_id {event_id} in namespace {namespace}...")
    while time.time() - start_time < timeout:
        try:
            result = subprocess.run([
                "oc", "get", "pipelinerun", "-n", namespace, "-l", label, "-o", "json"
            ], capture_output=True, text=True, check=True)
            runs = json.loads(result.stdout)
            items = runs.get("items", [])
            if items:
                pipelinerun_name = items[0]["metadata"]["name"]
                logger.debug(f"Found PipelineRun: {pipelinerun_name}")
                return pipelinerun_name
        except subprocess.CalledProcessError as e:
            logger.error(f"Error querying PipelineRun: {e.stderr}")
        time.sleep(poll_interval)
    logger.error("Timeout: No PipelineRun found for event_id.")
    return None

def wait_for_pipelinerun_status(pipelinerun_name: str, namespace: str = 'bit-jme-test-pipelines-d', timeout: int = 1200, poll_interval: int = 10) -> Tuple[bool, Optional[str]]:
    """
    Waits for the PipelineRun with the given name in the specified namespace to complete and returns its success status.

    Args:
        pipelinerun_name (str): The name of the PipelineRun.
        namespace (str, Optional): The namespace to search in. Default is 'bit-jme-test-pipelines-d'.
        timeout (int, optional): Maximum time to wait in seconds. Default is 1200.
        poll_interval (int, optional): Poll interval in seconds. Default is 10.

    Returns:
        (bool, str|None): (True if succeeded, status message) or (False, message)
    """
    start_time = time.time()
    logger.debug(f"Waiting for PipelineRun {pipelinerun_name} to complete...")
    while time.time() - start_time < timeout:
        try:
            result = subprocess.run([
                "oc", "get", "pipelinerun", pipelinerun_name, "-n", namespace, "-o", "json"
            ], capture_output=True, text=True, check=True)
            run = json.loads(result.stdout)
            conditions = run.get("status", {}).get("conditions", [])
            logger.debug(f"PipelineRun conditions: {conditions}")
            for cond in conditions:
                if cond.get("type") == "Succeeded":
                    status = cond.get("status")
                    message = cond.get("message", "")
                    if status == "True":
                        logger.debug(f"PipelineRun {pipelinerun_name} succeeded.")
                        return True, message
                    elif status == "False":
                        logger.error(f"PipelineRun {pipelinerun_name} failed: {message}")
                        return False, message
                    # If status is Unknown, keep polling
        except subprocess.CalledProcessError as e:
            logger.error(f"Error querying PipelineRun status: {e.stderr}")
        time.sleep(poll_interval)
    logger.error(f"Timeout: PipelineRun {pipelinerun_name} did not complete in time.")
    return False, "Timeout: PipelineRun did not complete"


def get_pipeline_run_name_by_labels_and_params(appname: str, pipeline_name: str, namespace: str = 'bit-jme-test-pipelines-d', params: Optional[dict] = None, timeout: int = 1200, poll_interval: int = 10) -> Optional[str]:
    """
    Searches for the PipelineRun name with the given labels and input parameters in the specified namespace.

    Args:
        namespace (str): The namespace to search in.
        appname (str): The value of the label 'pipeline.bit.admin.ch/appname'.
        pipeline_name (str): The value of the label 'pipeline.bit.admin.ch/name'.
        params (dict, optional): A dictionary of input parameters to filter by. Default is None.
        timeout (int, optional): Maximum time to wait in seconds. Default is 1200.
        poll_interval (int, optional): Poll interval in seconds. Default is 10.

    Returns:
        str | None: The PipelineRun name if found, otherwise None.
    """
    label_appname = f"pipeline.bit.admin.ch/appname={appname}"
    label_pipeline_name = f"pipeline.bit.admin.ch/name={pipeline_name}"

    start_time = time.time()
    logger.debug(f"Searching for PipelineRun with labels {label_appname}, {label_pipeline_name} and parameters {params} in namespace {namespace}...")

    while time.time() - start_time < timeout:
        try:
            command = [
                "oc", "get", "pipelinerun", "-n", namespace, "-l", label_appname, "-l", label_pipeline_name, "-o", "json"
            ]

            logger.debug(f"Executing command: {' '.join(command)}")

            result = subprocess.run(command, capture_output=True, text=True, check=True)
            runs = json.loads(result.stdout)
            items = runs.get("items", [])

            for item in items:
                params_in_run = {param.get("name"): param.get("value") for param in item.get("spec", {}).get("params", [])}
                if params:
                    # Check if all provided params match the params in the PipelineRun
                    if all(params_in_run.get(key) == value for key, value in params.items()):
                        pipelinerun_name = item["metadata"]["name"]
                        logger.debug(f"Found PipelineRun: {pipelinerun_name}")
                        return pipelinerun_name
        except subprocess.CalledProcessError as e:
            logger.error(f"Error querying PipelineRun: {e.stderr}")
        time.sleep(poll_interval)

    logger.error("Timeout: No PipelineRun found for the specified labels and parameters.")
    return None
