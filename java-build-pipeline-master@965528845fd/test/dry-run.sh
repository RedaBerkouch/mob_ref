#!/bin/bash
set -e

cd ..
echo " --- linting java-pipeline ---"
helm lint src

cd test
rm -f test-pipeline-chart/*.lock
rm -rf test-pipeline-chart/charts
cd test-pipeline-chart/

echo " --- test Helm Chart: dep update ---"
helm dep up 
echo " --- test Helm Chart: lint ---"
helm lint 

echo " --- test Helm Chart: create k8s objects ---"
helm install --dry-run --debug po-test-pipeline . \
 -f values.yaml \
 -f values-build.yaml \
 -f values-deploy.yaml
