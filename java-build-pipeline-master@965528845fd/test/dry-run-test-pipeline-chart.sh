#!/bin/bash
set -e
export HELM_BURST_LIMIT=250

rm -f test-pipeline-chart/*.lock
rm -rf test-pipeline-chart/charts
helm dependency update test-pipeline-chart/
helm install --dry-run --debug test-pipeline-chart test-pipeline-chart/
