# Helm Testing for Java Pipelines

This folder contains scripts to support testing, installing, and publishing Helm charts for pipeline components. The default OpenShift namespace is used for all operations.

## Prerequisites

- Helm v3.x installed
- Access to OpenShift CLI (`oc`)
- Logged into the target OpenShift cluster
- All scripts assume the chart is located in `test-pipeline-chart/`.
- The Nexus repository URL is: https://nexus.bit.admin.ch/repository/team-jeap-helm-hosted-test

### Login to OpenShift

Before running any script, make sure you're logged in:

```bash
oc login https://<your-cluster-url> --token=<your-access-token>
```

You can verify your current namespace with:

```bash
oc project
```

### Local Development Setup
This test setup is intended for local development of java build pipelines in combination with cd pipelines and the common pipelines helm charts. 
Therefore, the test helm chart expects the following folder structure for its dependencies, with all referenced repositories ideally located in sibling directories:
```
root/
├── java-build-pipeline/  
│   ├── src/
│   │   └── Chart.yaml
│   └── test/
│       └── test-pipeline-chart/
│           └── Chart.yaml (TEST)
│           └── values.yaml (TEST)
│           └── values-build.yaml (TEST)
│           └── values-deploy.yaml (TEST)
├── commons-pipeline/
│   └── src/
│       └── Chart.yaml
└── cd-pipeline/
    └── src/
        └── Chart.yaml
```

**Make sure the relative paths are correct based on your local folder structure.**

### Adding Secrets to Container Application Environments (CAEs)

Due to breaking changes resulting from the deprecation of the cluster-wide secret store, certain secrets are now only provided for dedicated CI/CD namespaces.
This change affects local development within Container Application Environments (CAEs), which correspond to individual application namespaces.

As a result, developers must manually create the required secrets within their respective CAEs.
These manually created secrets are currently **not subject to automatic rotation** and are **not removed during helm install operations**.
Therefore, the creation of these secrets is a one-time setup process.

The following secrets with the listed keys must be added manually:

- **cop-rox-api-token** - Keys: rox_api_token

- **cop-sonar-token** - Keys: hostUrl, token

- **argocd-api-tokens** - Keys: non-prd-api-token, prd-api-token

- **remedy-soap-credentials** - Keys: username, password

It is recommended to document the creation process and ensure that access to these secrets is properly controlled.
Future improvements may include automation or integration with a namespace-specific secret management solution to streamline this setup.

## Script Overview

### dry-run-test-pipeline.sh
Prepares the Helm chart by cleaning up lock files and dependencies. Does not execute the dry-run install.
```Shell
./dry-run-test-pipeline.sh
```

### dry-run-test-pipeline-chart.sh
Same as above, but also performs a Helm dry-run install with debug output. Useful for validating chart structure and templates.
```Shell
./dry-run-test-pipeline-chart.sh
```

### install-test-pipeline-chart.sh
Performs a full cleanup and reinstallation of the Helm chart:

- Deletes succeeded/failed pods
- Uninstalls the chart
- Patches PVCs to allow deletion
- Reinstalls the chart with multiple values files

```Shell
./install-test-pipeline-chart.sh
```
**Note:** The namespace is not explicitly set in this script. It uses the current default namespace from oc.

### execute-test-pipeline-runs.sh
Deletes all Helm test pods (identified via annotations) and runs the Helm tests defined in the chart.
```Shell
./execute-test-pipeline-runs.sh
```

### upload-test-helm-chart.sh
Packages the Helm chart and uploads it to the Nexus repository `team-jeap-helm-hosted-test`.

```Shell
./upload-test-helm-chart.sh
```

Make sure the following environment variables are set:

```Shell
export NEXUS_USERNAME=<your-username>
export NEXUS_PASSWORD=<your-password>
```

## How to write Helm Tests

Helm allows you to define test hooks that validate the functionality of a chart after installation. 
These tests are typically defined as Kubernetes resources (usually Pods) and placed under the `templates/tests/` directory in your chart.
As base the [test chart](test-pipeline-chart/Chart.yaml) and the `values.yaml` of the [test-pipeline-chart directory](test-pipeline-chart) are used.

### Test Annotations
To mark a resource as a Helm test, you need to add the following annotations:
```YAML
annotations:
  "helm.sh/hook": test
  "helm.sh/hook-delete-policy": hook-succeeded
```
These annotations allow automated cleanup and lifecycle management of test pods.
- helm.sh/hook: test tells Helm to treat the resource as a test.
- helm.sh/hook-delete-policy: hook-succeeded ensures the test pod is automatically deleted after successful execution.

### Example - Test build PipelineRun 
The following example shows a Helm test that triggers a Tekton PipelineRun and waits for it to complete.
The pipeline run is configured for the application `jme-rhos-cicd-example` and uses the [test chart](test-pipeline-chart/Chart.yaml) and the `values.yaml` of the [test-pipeline-chart directory](test-pipeline-chart).
The test resource is located under [src/templates/tests/cicd-example-build-pipeline-webhook-trigger-test.yaml](../src/templates/tests/cicd-example-build-pipeline-webhook-trigger-test.yaml).
