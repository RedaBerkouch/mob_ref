#!/bin/bash
set -e

helm lint src/

helm dep up test/test-pipeline-chart/
helm lint test/test-pipeline-chart/
