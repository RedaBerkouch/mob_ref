# ppl_http_module.py
import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry
import os
import sys

# Specify the relative paths of the modules to be imported in order to develop locally with them.
# For more, take a lock at the README.md.
module_paths = os.getenv('MODULE_PATHS', ['../util'])
for path in module_paths:
    sys.path.append(os.path.abspath(path))

# ----- Modul imports -----
from ppl_logging_module import log


def request(url, method, json, auth, headers, fail_on_failure: bool = True):
    log.info(f"Calling {url}")
    try:
        retry_strategy = create_retry_strategy()
        session = create_session(retry_strategy)
        session.auth = auth
        session.headers.update(headers)
        response = session.request(method=method, url=url, json=json)
        log.info(f"Response: {response}")
        if fail_on_failure:
          response.raise_for_status()  # Raise an exception for 4xx or 5xx errors
        return response
    except requests.exceptions.RequestException as e:
        log.error(f"{method} failed: {e}")
        log.error("Exiting with sys.exit(1)")
        sys.exit(1)


def create_session(retry_strategy):
    adapter = HTTPAdapter(max_retries=retry_strategy)
    session = requests.Session()
    session.mount("http://", adapter)
    session.mount("https://", adapter)
    return session


def create_retry_strategy():
    return Retry(
        total=3,  # Number of retries
        backoff_factor=2,  # Delay between retries (backoff_factor * (2 ** (number_of_attempts - 1)))
        status_forcelist=[500, 502, 503, 504],  # Retry only on these HTTP status codes
        allowed_methods=["HEAD", "GET", "OPTIONS", "POST", "PUT"],  # Retry on these HTTP methods
        raise_on_status=False  # Don't raise exceptions for failed requests
    )
