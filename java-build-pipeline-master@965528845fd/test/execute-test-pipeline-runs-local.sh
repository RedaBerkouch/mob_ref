#!/bin/bash
echo "Start automated helm testing"
set -e

echo "Clean up old test pods..."
oc get pods -o json | \
jq -r '.items[] | select(.metadata.annotations["helm.sh/hook"] == "test") | .metadata.name' | \
xargs -r oc delete pod

echo "Run helm tests..."
helm test test-pipeline-chart
