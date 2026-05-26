#!/bin/bash
export HELM_BURST_LIMIT=250

# Default namespace of the oc cli is used
oc delete pod --field-selector=status.phase==Succeeded
oc delete pod --field-selector=status.phase==Failed

helm  uninstall test-pipeline-chart
## Patch is required for PVCs to be deleted successfully
oc patch pvc jme-rhos-cicd-example-nino-ja-pipelines -p '{"metadata":{"finalizers":null}}'
sleep 13

set -e
rm -f test-pipeline-chart/*.lock
rm -rf test-pipeline-chart/charts
helm dep up test-pipeline-chart/
helm install test-pipeline-chart test-pipeline-chart/ \
 -f test-pipeline-chart/values.yaml \
 -f test-pipeline-chart/values-build.yaml \
 -f test-pipeline-chart/values-deploy.yaml
