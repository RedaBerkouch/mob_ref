set -e
echo "[INFO] --- SCRIPT START ---"

git config --global --add safe.directory "$(pwd)"
echo "[INFO] Safe directory set."

COMMIT_HASH=$(git rev-parse HEAD)
echo "[INFO] Commit hash: $COMMIT_HASH"
BRANCH_NAME=$(git branch -r --contains "$COMMIT_HASH" | grep -v HEAD | head -n1 | sed 's@origin/@@' | xargs)
echo "[INFO] Branch name: $BRANCH_NAME"

if [ -z "$BRANCH_NAME" ]; then
  echo "[ERROR] Branch name could not be determined."
  exit 1
fi

echo "[INFO] Installing Python requirements"
pip install --index-url https://repo.bit.admin.ch/repository/pypi-org/simple --trusted-host repo.bit.admin.ch -r test/e2e/requirements.lock.txt
echo "[INFO] Python requirements installation completed"

echo "[INFO] Starting Python E2E test"
python3 test/e2e/test_cases/src/e2e_pipeline_test_orchestrator.py --test_commit_sha "$COMMIT_HASH" --test_branch_name "$BRANCH_NAME"
echo "[INFO] Python E2E test completed"