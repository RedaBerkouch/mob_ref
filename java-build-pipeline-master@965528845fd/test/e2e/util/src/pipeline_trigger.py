import hmac
import hashlib
import json
import os
import time
from typing import Optional

import requests

from logger import get_logger

logger = get_logger()

def generate_payload(branch, commit_hash, actor_name, email, display_name, repository_name, project_key):
    """
    Generate the JSON payload for the pipeline trigger.
    """
    payload = {
        "eventKey": "repo:refs_changed",
        "date": time.strftime("%Y-%m-%dT%H:%M:%S%z"),
        "actor": {
            "name": actor_name,
            "emailAddress": email,
            "id": 20453,
            "displayName": display_name,
            "active": True,
            "slug": actor_name,
            "type": "NORMAL",
            "links": {
                "self": [
                    {"href": f"https://bitbucket.bit.admin.ch/users/{actor_name}"}
                ]
            }
        },
        "repository": {
            "slug": repository_name,
            "id": 13550,
            "name": repository_name,
            "description": "Example to show CI CD on RHOS",
            "hierarchyId": "c04488d170236fddc012",
            "scmId": "git",
            "state": "AVAILABLE",
            "statusMessage": "Available",
            "forkable": True,
            "project": {
                "key": project_key,
                "id": 1550,
                "name": project_key,
                "description": "Jeap Microservice Examples, Beispiele zur Verwendung der jEAP Library",
                "public": True,
                "type": "NORMAL",
                "links": {
                    "self": [
                        {"href": f"https://bitbucket.bit.admin.ch/projects/{project_key}"}
                    ]
                }
            },
            "public": False,
            "links": {
                "clone": [
                    {"href": f"https://bitbucket.bit.admin.ch/scm/{project_key.lower()}/{repository_name}.git", "name": "http"},
                    {"href": f"ssh://git@bitbucket.bit.admin.ch/{project_key.lower()}/{repository_name}.git", "name": "ssh"}
                ],
                "self": [
                    {"href": f"https://bitbucket.bit.admin.ch/projects/{project_key}/repos/{repository_name}/browse"}
                ]
            }
        },
        "changes": [
            {
                "ref": {
                    "id": f"refs/heads/{branch}",
                    "displayId": branch,
                    "type": "BRANCH"
                },
                "refId": f"refs/heads/{branch}",
                "fromHash": "0000000000000000000000000000000000000000",
                "toHash": commit_hash,
                "type": "ADD"
            }
        ]
    }
    return payload

def generate_hmac_header(secret, body):
    """
    Generate the HMAC header for the request.
    """
    hmac_digest = hmac.new(secret.encode(), body, hashlib.sha256).hexdigest()
    return f"X-Hub-Signature: sha256={hmac_digest}"

def trigger_build_pipeline(
    commit_hash,
    repository_name,
    project_key,
    endpoint='https://jbp-el-bit-jme-test-pipelines-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/',
    branch='develop',
    actor_name='automated-e2e-test-agent',
    email='auto.agent@bit.admin.ch',
    display_name='Automated E2E Test Agent',
    secret=None
) -> Optional[str]:
    """
    Trigger the build pipeline by sending a POST request to the specified endpoint with the generated payload and HMAC header.

    :param commit_hash: The commit hash to include in the payload.
    :param repository_name: The name of the repository.
    :param project_key: The key of the project.
    :param endpoint: The URL of the pipeline trigger endpoint (default: 'https://jbp-el-bit-jme-test-pipelines-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/').
    :param branch: The branch name to include in the payload (default: 'develop').
    :param actor_name: The name of the actor to include in the payload (default: 'automated-e2e-test-agent').
    :param email: The email of the actor to include in the payload (default: 'auto.agent@bit.admin.ch').
    :param display_name: The display name of the actor to include in the payload (default: 'Automated E2E Test Agent').
    :param secret: The secret key for HMAC header generation (default: None).
    :return: str | None: The eventID from the response if successful, otherwise None.
    """
    logger.debug("Triggering build pipeline...")
    payload = generate_payload(branch, commit_hash, actor_name, email, display_name, repository_name, project_key)
    logger.debug(f"Payload: {json.dumps(payload, indent=2)}")
    body = json.dumps(payload).encode("utf-8")
    headers = {
        "Content-Type": "application/json",
        "x-event-key": "repo:refs_changed"
    }
    if secret is not None:
        hmac_header = generate_hmac_header(secret, body)
        header_key, header_value = hmac_header.split(": ") if ": " in hmac_header else hmac_header.split(":")
        headers[header_key] = header_value
        logger.debug(f"HMAC header set: {header_key}: {header_value}")
    else:
        logger.debug("No secret provided, HMAC header not set.")

    try:
        response = requests.post(endpoint, data=body, headers=headers, verify="/tekton-custom-certs/ca-bundle.crt")
        logger.debug(f"Pipeline trigger response status: {response.status_code}")
        if response.ok:
            logger.debug("Pipeline triggered successfully.")
            response_json = response.json()
            event_id = response_json.get("eventID")
            if event_id:
                logger.debug(f"eventID returned: {event_id}")
                return event_id
            else:
                logger.warning("No eventID found in response JSON.")
                return None
        else:
            logger.error(f"Pipeline trigger failed with status {response.status_code}: {response.text}")
            raise requests.exceptions.HTTPError(f"Pipeline trigger failed with status {response.status_code}: {response.text}")
    except requests.exceptions.RequestException as e:
        logger.error(f"Request to pipeline endpoint failed: {e}")
        raise
    except Exception as e:
        logger.error(f"Unexpected error during pipeline trigger: {e}")
        raise
