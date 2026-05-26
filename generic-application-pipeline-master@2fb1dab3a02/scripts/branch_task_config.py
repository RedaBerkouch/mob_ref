"""Resolves branch-based pipeline task configuration."""

import json
import os

PIPELINE_RESULT_PATH = "/tekton/results"

# ---------- Utility functions ----------

def normalize_branch_name(ref: str) -> str:
    """Convert full Git ref (e.g. 'refs/heads/develop') to plain branch name ('develop')."""
    ref = (ref or "").strip()
    for prefix in ("refs/heads/", "refs/", "origin/"):
        if ref.startswith(prefix):
            return ref[len(prefix):]
    return ref

def load_json_from_env(env_name: str) -> dict:
    """Load a JSON object from an environment variable."""
    raw = os.environ.get(env_name, "")

    if not raw.strip():
        print(f"[INFO] Environment variable '{env_name}' is empty – using {{}}")
        return {}

    try:
        data = json.loads(raw)
    except json.JSONDecodeError as exc:
        raise ValueError(f"Invalid JSON in environment variable '{env_name}': {exc}")

    if not isinstance(data, dict):
        raise ValueError(f"Environment variable '{env_name}' must contain a JSON object")

    return data

def detect_branch_type(branch_name: str) -> str:
    if branch_name in ("master", "main"): return "master"
    if branch_name.startswith("feature/"): return "feature"
    if branch_name.startswith("hotfix/"):  return "hotfix"
    if branch_name.startswith("release/"): return "release"
    if branch_name.startswith("develop"):  return "develop"
    return "feature" # fallback

def resolve_branch_config(default_config_per_branch: dict, app_config_per_branch: dict, branch_name: str) -> dict:
    """Combine defaults + app config based on matching rules."""
    branch_type_name = detect_branch_type(branch_name)
    print(f"[INFO] Branch '{branch_name}' detected as type '{branch_type_name}'")

    # 1) Base: defaults for inferred branch type
    resolved = dict(default_config_per_branch.get(branch_type_name, {}))

    # 2) App override for same branch type
    if branch_type_name in app_config_per_branch:
        resolved.update(app_config_per_branch.get(branch_type_name) or {})

    print(f"[INFO] Final resolved config: {json.dumps(resolved, sort_keys=True)}")
    return resolved

def bool_to_str(value) -> str:
    """Tekton expects string 'true'/'false'."""
    return "true" if bool(value) else "false"

# ---------- Main ----------

current_branch = normalize_branch_name(os.environ.get("GIT_REV", ""))
print(f"[INFO] Processing branch: {current_branch}")

default_config_per_branch = load_json_from_env("DEFAULT_BRANCH_TASK_CONFIGURATION")
app_config_per_branch     = load_json_from_env("APP_BRANCH_TASK_CONFIGURATION")

resolved_config = resolve_branch_config(default_config_per_branch, app_config_per_branch, current_branch)

# ---------- Task flags (branch-based) ----------

task_flags = {
    "PROMOTE_IMAGE":       bool_to_str(resolved_config.get("promoteImage", False)),
    "DOWNLOAD_FILES":      bool_to_str(resolved_config.get("downloadFiles", False)),
    "RUN_SCA_CHECK":       bool_to_str(resolved_config.get("runScaCheck", False)),
    "BUILD_NOTIFICATIONS": bool_to_str(resolved_config.get("buildNotifications", False)),
    "DEPLOY_STAGE":        resolved_config.get("deployStage", "none"),
    "VERIFY_DEPLOYMENT":   bool_to_str(resolved_config.get("verifyDeployment", False)),
    "RUN_MALWARE_SCAN":     bool_to_str(resolved_config.get("runMalwareScan", False)),
}

# ---------- Write result files ----------

os.makedirs(PIPELINE_RESULT_PATH, exist_ok=True)

for name, value in task_flags.items():
    with open(os.path.join(PIPELINE_RESULT_PATH, name), "w") as f:
        f.write(value)
    print(f"[INFO] Wrote result {name}={value}")
