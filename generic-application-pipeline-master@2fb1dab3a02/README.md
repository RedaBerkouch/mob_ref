# generic-application-pipeline

CNCICD Generic Application Pipeline

**Homepage:** <https://bitbucket.bit.admin.ch/projects/CNP/repos/generic-application-pipeline/browse/README.md>

## Introduction

This pipeline build container based on the Containerfile. Additionally it supports monorepo setups.

See images from the pipelines
* [Build Pipeline](images/pipeline-build.png)
* [Verification pipeline](images/pipeline-verification.png)
* [Monorepo pipeline which triggers the build pipeline](images/pipeline-monorepo.png)

## Installation

The pipeline chart must be used as dependency of an umbrella chart.
It requires the [**commons-pipeline**](https://bitbucket.bit.admin.ch/projects/CNP/repos/commons-pipeline/browse) and can optionally be combined with the [**cd-pipeline**](https://bitbucket.bit.admin.ch/projects/CNP/repos/cd-pipeline/browse), which handles the deployment process.

```
dependencies:
    - name: commons-pipeline
      version: "2.0.2"
      repository: https://nexus.bit.admin.ch/repository/bit-pipelines-helm-hosted
    - name: generic-application-pipeline
      repository: https://nexus.bit.admin.ch/repository/bit-pipelines-helm-hosted
      version: "^9.x"
    # Optional
    - name: cd-pipelines
      version: "7.7.0"
      repository: https://nexus.bit.admin.ch/repository/bit-pipelines-helm-hosted
```

Please reference [https://helm.sh/docs/chart_best_practices/dependencies/](https://helm.sh/docs/chart_best_practices/dependencies/) for referencing the pipeline version. We currently do not make a recommendation for which versioning pattern to use.

## Configuration

The table with all the configurable parameters of the chart and the default values is at the bottom of the Readme.

### Credentials required by the pipeline

If you are using the `global.secrets.externalStore.enabled` provided in commons-pipeline Helm chart you will have to provide the following secret keys in your ExternalSecretStore

| Key (in Vault)                                                                 | Example Value                                                                         | Notes                                                                                                                                                           |
|--------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| global.secrets.externalStore.paths.nexus.property                              | `{"auths":{"my-repo.nexus.bit.admin.ch":{"auth":"cGFzc3dvcmQK"},}}`                   | Optional but required for image registry access (DockerConfigJSON)                                                                                              |
| global.secrets.externalStore.paths.netrc.property                              | `default login anonymous password user@domain`                                        | Optional and only required if global.secrets.externalStore.paths.netrc is set                                                                                   |
| global.secrets.externalStore.paths.bitbucket.property                          | `-----BEGIN OPENSSH PRIVATE KEY-----\n<SNIPPED>\n-----END OPENSSH PRIVATE KEY-----\n` | Optional but required for Bitbucket interactions. Please note, the SSH key must be in a valid PEM format. Make sure it contains an ending new-line character    |
| global.secrets.externalStore.paths.bitbucketAccessToken.property               | `AadfW23ef3rvscSFGSFdsg4ergfsFGSDR9SDFSDFSDFd`                                        | Optional but required for Bitbucket interactions                                                                                                                |
| global.secrets.externalStore.paths.azure.property                        | `-----BEGIN OPENSSH PRIVATE KEY-----\n<SNIPPED>\n-----END OPENSSH PRIVATE KEY-----\n` | Optional but required for Azure DevOps interactions. Please note, the SSH key must be in a valid PEM format. Make sure it contains an ending new-line character |
| global.secrets.externalStore.paths.azureDevopsAccessToken.property             | `AadfW23ef3rvscSFGSFdsg4ergfsFGSDR9SDFSDFSDFd`                                        | Optional but required for Azure DevOps interactions                                                                                                             |
| global.secrets.externalStore.paths.tektonDeploymentWebhookAccessToken.property | `AadfW23ef3rvscSFGSFdsg4ergfsFGSDR9SDFSDFSDFd`                                        | Optional and only required to trigger the deployment pipeline

## Using Campus datacenter

The pipeline can be hosted on Campus datacenter and can be configured to use Campus datacenter resources

### Referring pipeline chart hosted on Campus

With Helm Chart dependency pointing to nexus-campus

```
dependencies:
  ...
  - name: generic-application-pipeline
    repository: https://nexus-campus.bit.admin.ch/repository/bit-pipelines-helm-hosted
    version: "^6.x"
  ...
```

### Hosting the pipeline on Campus

This must be configured during the CI/CD offering creation, requiring the selection of the appropriate Campus RHOS cluster.

### Configure the pipeline to use Campus datacenter resources

By default, the pipeline use Primus datacenter resources, to use Campus datacenter resources, add the following values in your values file.

```
generic-application-pipeline:
  ...
  datacenter:
    useDatacenter: campus
```

See `datacenter.useDatacenter` key for details. Additionally consider changing proxy `defaultHttpProxy` as well.

## Pipeline usage

### Prerequisites

* RHOS Namespaces and the team are set up.
* The application's source code repositories and GitOps repository are available.
* A Vault instance is available to synchronize secrets with OpenShift.

### Trigger build with Bitbucket webhooks

See [Configure Bitbucket Webhook](https://docs.bcp.bit.admin.ch/services/ci-cd/how-to/general/set-up-webhooks-for-tekton-and-argo-cd/#configure-bitbucket-webhook)

The webhook needs to trigger on the following events:
* Events: Repository `Push`

Furthermore the following list of events is supported:
* Events: Repository `Push`
* Events: Pull request `Opened`
* Events: Pull request `Source branch updated`
* Events: Pull request `Merged`

### Trigger build with Azure DevOps Server

See [Configure Azure DevOps Webhook](https://docs.bcp.bit.admin.ch/services/ci-cd/how-to/general/set-up-webhooks-for-tekton-and-argo-cd/#configure-azure-devops-webhook)

### Create RoleBinding in target application namespace

A RoleBinding allowing pipeline namespace to view target application namespaces

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: rb-pipeline-can-view
  namespace: <target application namespace>
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: view
subjects:
  - kind: ServiceAccount
    name: pipeline
    namespace: <pipeline namespace>
```

Relevant related error message:

```
Error from server (Forbidden): deployments.apps "<application>" is forbidden: User "system:serviceaccount:<pipeline namespace>:pipeline"
cannot get resource "deployments" in API group "apps" in the namespace "<target application namespace>"
```

### Monorepo support

For project using a monorepo setup, the default behaviour is that all images specified in the values file will be built on a commit event. This is even the case when the Containerfile did not change.
If you do not desire this behaviour, you can enable the monorepo pipeline behaviour by setting `enableMonorepo: true`. With this option set, a monorepo pipeline will be created
which evaluates the changed Containerfiles (defined by `containerFilePath`) in your repository and triggers a PipelineRun only for changed Containerfiles.

In monorepo mode a specific branch build pattern defined in `applications.triggerBranchPattern` will be ignored, only `defaultTriggerBranchPattern` is evaluated.

### Image tagging

The pipeline supports image tagging based upon a user definable pattern defined in `buildVersionTemplate`.

#### The following tagging strategies are supported by the pipeline

| Name                 | Description                                                                      | Example                                  |
|----------------------|----------------------------------------------------------------------------------|------------------------------------------|
| <TEKTONFILE_VERSION> | Read from json file `buildVersionFilePath` which must contain key `imageVersion` | 1.0.0                                    |
| \<TIMESTAMP>         | Timestamp. The format can be updated by `buildVersionDatePattern`                | 2023-10-17T164551                        |
| <BRANCH_NAME>        | Name of the build branch                                                         | develop                                  |
| <COMMIT_HASH_SHORT>  | Git commit hash short                                                            | c66d2a6                                  |
| <COMMIT_HASH>        | Git commit hash                                                                  | c66d2a6a31ee268b24132ab3cbcaa20542b09302 |

The `buildVersionTemplate` can be freely defined by the user as long as it is compliant with [Docker tag restrictions](https://docs.docker.com/engine/reference/commandline/tag/).

#### Examples:
| Examples                                                         | Resulting image tag                        |
|------------------------------------------------------------------|--------------------------------------------|
| `buildVersionTemplate: <TEKTONFILE_VERSION>-<COMMIT_HASH_SHORT>` | `1.0.0-c66d2a6`                            |
| `buildVersionTemplate: <COMMIT_HASH>`                            | `c66d2a6a31ee268b24132ab3cbcaa20542b09302` |
| `buildVersionTemplate: <TIMESTAMP>_<COMMIT_HASH_SHORT>`          | `2023-10-17T164551_c66d2a6`                |

### Migration to GAP version 9.0.0

Starting with version 9.0.0, the generic-application-pipeline can no longer be used standalone.
GAP now depends on [commons-pipeline](https://bitbucket.bit.admin.ch/projects/CNP/repos/commons-pipeline/browse) and must be used as part of an umbrella chart.
Optionally, it can be used together with the [cd-pipeline](https://bitbucket.bit.admin.ch/projects/CNP/repos/cd-pipeline/browse), which replaces the deployment process previously handled by GAP.

#### Umbrella chart example
```
---
apiVersion: v2
name: pipeline
description: CI/CD-Pipeline
type: application
version: 0.0.0
dependencies:
    - name: commons-pipeline
      version: "2.0.2"
      repository: https://nexus.bit.admin.ch/repository/bit-pipelines-helm-hosted
    - name: generic-application-pipeline
      repository: https://nexus.bit.admin.ch/repository/bit-pipelines-helm-hosted-test
      version: "^9.x"
    - name: cd-pipelines
      version: "7.10.0"
      repository: https://nexus.bit.admin.ch/repository/bit-pipelines-helm-hosted
```

#### Values example (our test setup)
```
---
# Base
global:
  gitOpsRepo: ssh://git@bitbucket.bit.admin.ch/stirhos/sti-gap-example-gitops.git
  gitOpsRepoBranch: dev
  nexusImageRegistryBaseName: bit-sti-docker-hosted
  overrideCdpReleaseName: cd-ppl-chart
  imageRegistryDomain: nexus.bit.admin.ch/sti
  pipelineCleanup:
    enabled: false
  secrets:
    enabled: true
    externalStore:
      enabled: true
      vaults:
        - name: gap-test-externalstore
          path: p-szb-ros-shrd-npr-01
          server: https://mav.bit.admin.ch
      paths:
        bitbucket:
          id: bit-amber-pipelines-d/default
          name: bitbucket-ssh-key
          property: bitbucket-ssh-key
          vault: gap-test-externalstore
        azure:
          id: bit-amber-pipelines-d/default
          name: azure-ssh-key
          property: azure-ssh-key
          vault: gap-test-externalstore
        bitbucketAccessToken:
          id: bit-amber-pipelines-d/default
          name: bitbucket-http-access-token
          property: bitbucket-http-access-token
          vault: gap-test-externalstore
        azureDevopsAccessToken:
          id: bit-amber-pipelines-d/default
          name: azure-http-access-token
          property: azure-http-access-token
          vault: gap-test-externalstore
        nexus:
          id: bit-amber-pipelines-d/default
          name: dockerconfig
          property: dockerconfig
          vault: gap-test-externalstore
        netrc:
          id: bit-amber-pipelines-d/default
          name: netrc
          property: netrc
          vault: gap-test-externalstore
        dependencyTrackApiKey:
          id: bit-amber-pipelines-d/default
          name: dependency-track-test-api-key
          property: dependency-track-test-api-key
          vault: gap-test-externalstore
        tektonDeploymentWebhookAccessToken:
          id: bit-amber-pipelines-d/default
          name: webhook-deployment-accesstoken
          property: webhook-deployment-accesstoken
          vault: gap-test-externalstore

# Documentation: https://bitbucket.bit.admin.ch/projects/CNP/repos/cd-pipeline/browse/doc/deployment-pipeline.md
cd-pipelines:
   gitAppRepoHost: ssh://git@bitbucket.bit.admin.ch
   gitAppRepoProject: stirhos
   gitOpsRepoBranch: dev
   customValuesPathTemplate: <APP_NAME>/helm/values.yaml
   targetNamespacePrefix: bit-amber
   cluster: p-szb-ros-shrd-npr-01
   prodCluster: p-szb-ros-shrd-prd-01
   applications:
     - appName: gap-example2
       bitBucketRepo: sti-gap-example
       zapTestImage: 'bit-base-images-docker-hosted.nexus.bit.admin.ch/bit/ubi9/owasp-zap:2.15.0'
       # Doku: https://bitbucket.bit.admin.ch/projects/CNP/repos/cd-pipeline/browse/doc/verification-pipeline.md
       verificationPipeline:
         tests:
           zap:
             enabled: true
             # The command to be run inside the ZAP image. see: https://bitbucket.bit.admin.ch/projects/CBI/repos/ubi9-owasp-zap/browse
             command: >-
               echo BEFORE-ZAP; ls -la;
               ./zap-baseline.py -d -t https://gap-example2-bit-amber-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch -I -c /home/zap/.ZAP/policies/zap-bit-default.policy -z '
               -config network.connection.httpProxy.host=prxp01.admin.ch
               -config network.connection.httpProxy.port=8080
               -config network.connection.httpProxy.enabled=true
               -config network.connection.httpProxy.exclusions.exclusion\(0\).host=localhost
               -config network.connection.httpProxy.exclusions.exclusion\(0\).enabled=true
               -config network.connection.httpProxy.exclusions.exclusion\(1\).host=.admin.ch
               -config network.connection.httpProxy.exclusions.exclusion\(1\).enabled=true';
               echo AFTER-ZAP;

generic-application-pipeline:
  applications:
  ...
```

#### Replace deployment branch task flags with `deployStage` and `verifyDeployment`

Since gap no longer performs deployments, the following task flags are no longer needed and must be replaced with the new ones that trigger the deployment pipeline and the verification pipeline:

- `updateGitops`
- `waitForDeploymentRollout`
- `triggerVerificationPipeline`
- `zapTest`

Use `deployStage` and `verifyDeployment` instead.

See [values.yaml](https://bitbucket.bit.admin.ch/projects/CNP/repos/generic-application-pipeline/browse/values.yaml) for more details

Old configuration example:

```
defaultBranchTaskConfiguration:
  develop:
    updateGitops: true
    waitForDeploymentRollout: true
    triggerVerificationPipeline: true
    zapTest: false
    ...

applications:
  - appName: my-app
    ...
    branchTaskConfiguration:
      develop:
        updateGitops: true
        waitForDeploymentRollout: true
        triggerVerificationPipeline: true
        zapTest: false
```

New configuration example:
```
defaultBranchTaskConfiguration:
  develop:
    deployStage: dev
    verifyDeployment: true
    ...

applications:
  - appName: my-app
    ...
    branchTaskConfiguration:
      develop:
        deployStage: dev
        verifyDeployment: true
```

#### Replace pipeline storage configuration

The following values were renamed:

defaultPipelinePvcSize → defaultPipelineStorage.size
applications[].pipelinePvcSize → applications[].pipelineStorage.size

Old configuration example:
```
defaultPipelinePvcSize: 5Gi

applications:
  - appName: my-app
    ...
    pipelinePvcSize: 5Gi
```
New configuration example:
```
defaultPipelineStorage:
  size: 5Gi

applications:
  - appName: my-app
    ...
    pipelineStorage:
      size: 5Gi
```

### Branch-based task execution | introduced in version 7.0.0
Starting with version 7.0.0, the pipeline uses a *branch-based* configuration to decide which tasks run on which branches.

#### What changed compared to earlier versions (< 7.0.0)

Example pipeline defaults and per application configuration

```
defaultPipelineTasks:
  downloadFiles: false
  downloadFilesFileName: Downloadfile
  ...
applications:
  - appName: my-app
    ...
    pipelineTasksConfig:
      downloadFiles: false
      downloadFilesFileName: Downloadfile
      ...
```

With branch-based task execution these switches move to:

- **Global defaults per branch type** `defaultBranchTaskConfiguration` defines which tasks are executed for given branch types `feature`, `develop`,
`hotfix`, `release`, `master`.

- **Optional per-application overrides** `applications[].branchTaskConfiguration` allows overriding the global defaults for a given application and branch type.

The old switches under `pipelineTasksConfig` are no longer used to decide whether a task runs.

#### Do I have to change anything?

If you never customized `pipelineTasksConfig.*` in your project:

→ You don’t have to do anything. Predefined defaults for `defaultBranchTaskConfiguration` will lead to same behaviour as before

If you overrode task switches in `applications[].pipelineTasksConfig`:

→ Copy your intent into `applications[].branchTaskConfiguration` for the branches where it should apply.

#### Migrate an existing setup
Old configuration example:
```
defaultPipelineTasks:
  downloadFiles: false
  downloadFilesFileName: Downloadfile
  ...
applications:
  - appName: my-app
    ...
    pipelineTasksConfig:
      downloadFiles: false
      downloadFilesFileName: Downloadfile
      ...
```

New configuration example:
```
defaultBranchTaskConfiguration:
  develop:
    downloadFiles: false
    ...
defaultPipelineTasksConfig:
  downloadFilesFileName: Downloadfile
  ...
applications:
  - appName: my-app
    ...
    branchTaskConfiguration:
      develop:
        downloadFiles: false
        ...
    pipelineTasksConfig:
      downloadFilesFileName: Downloadfile
```

### Branch type detection

| Branch pattern              | Detected branch type |
| --------------------------- | -------------------- |
| `feature/*`                 | `feature`            |
| `hotfix/*`                  | `hotfix`             |
| `release/*`                 | `release`            |
| `develop*`                  | `develop`            |
| `master`, `main`            | `master`             |
| any other branch (fallback) | `feature`            |

The detected branch type determines which configuration block from `defaultBranchTaskConfiguration`
is applied, additionally it will be overriden by `applications[].branchTaskConfiguration` if given.

For full reference of available branch types and task flags, see `defaultBranchTaskConfiguration.feature` key in Configuration section

#### Task dependencies

Flag inter-dependencies are documented under `defaultBranchTaskConfiguration.feature` as subkeys like `defaultBranchTaskConfiguration.feature.<taskName>` in Configuration section

### Examples of the generic-application-pipelines

* **GitOps Project Example** - For an example integration of the pipeline, see [sti-gap-example-gitops/pipeline](https://bitbucket.bit.admin.ch/projects/STIRHOS/repos/sti-gap-example-gitops/browse/pipeline)
* **Project Example** - For an example application project building with this pipeline, see [sti-gap-example](https://bitbucket.bit.admin.ch/projects/STIRHOS/repos/sti-gap-example)

## Additional information

### Optional download-files task

The pipeline contains an optional task `download-files`. The intention of the task is to provide the user the capability to download files, which need to be added to the Containerfile, from an endpoint requiring authentication.
In its default configuration the tasks reads the `downloadFilesFileName` line by line and downloads the files to the pipelines workspace.
Alternatively users can change the tasks behaviour by setting `downloadFilesExecuteCustomScript: true`. This will execute the script provided by `downloadFilesFileName`.

An authentication method for the endpoints contained in the `downloadFilesFileName` is provided with a [.netrc file](https://www.gnu.org/software/inetutils/manual/html_node/The-_002enetrc-file.html). For providing the .netrc file to the pipeline, please see the secrets section.
If you use `downloadFilesExecuteCustomScript: true` and require authentication, use curls' `--netrc` or -`-netrc-optional` option.

### Optional malware-scan task

The pipeline contains an optional task `m̀alware-scan`. In case the task is activated, it will scan the built image with the [Antivirus service from the BIT Container Platform](https://docs.bcp.bit.admin.ch/services/antivirus/).
Once you ordered the service, you have to create a binding as follows:
* Account Name: "<pipeline-namespace name>/<serviceAccountName of the pipeline>"
  (i.e. bit-amber-pipelines-d/pipeline)
* Account Type: "service-account"

In your service binding you will find the vault path to your specific secret in the `vault-annotation`.
Check the line starting with `vault.hashicorp.com/agent-inject-secret-` to find your path.

Enter this information as follows in your values.yaml file:

```
generic-application-pipeline:
  applications:
    - appName: malware-scan-test-app
      malwareScannerCredentialsFilePath: "creds/static/data/services/<uuid>/instances/<uuid>/bindings/<uuid>/service-account/<pipeline-namespace name>/<serviceAccountName of the pipeline>"
      # Define for which branch you want to run the malware scan
      branchTaskConfiguration:
        develop:
          runMalwareScan: true
```
(Example configuration: [values.add-malware-scan-task.yaml](test/values.add-malware-scan-task.yaml))

### ArgoCD Sync Webhook

We recommend to use ArgoCD Sync Webhooks configured on your GitOps repository. This avoids having to wait for next ArgoCD sync window to occur.
See [ArgoCD Git Webhook Configuration](https://argo-cd.readthedocs.io/en/stable/operator-manual/webhook/) for details.

### Additional dedicated tasks

This pipelines use the following dedicated tasks. Find more information in the tasks itself.
* [task-run-validation.yaml](templates/task-run-validation.yaml)
* [task-setup.yaml](templates/task-setup.yaml)
* [task-setup-monorepo.yaml](templates/task-setup-monorepo.yaml)
* [task-download-files.yaml](templates/task-download-files.yaml)

## Known Limitations

### RHOS (Openshift, Kubernetes) is subject to length limitations on object names

* RHOS (Openshift, Kubernetes) is subject to length limitations on object names ([DNS names RFC, 2.3.4. Size limits](https://www.ietf.org/rfc/rfc1035.txt))
* Therefore, we will truncate some resource names to stay within this limitation
* Additionally, you can overwrite resource name by defining `overrideReleaseName`

### GitOps repository structure

The pipeline is intended to be used with the below GitOps repository structure. While we assume the pipeline configuration to be flexible enough, we can not guarantee it to be compatible with all structure types.

```
bit-myproject-gitops/
├── argocd  # Contains ArgoCD ApplicationSet definition for namespaces managed by ArgoCD excluding the pipeline
│   └── application-set.yaml
├── pipeline # Contains the reference to the pipeline definition as dependency in the Chart.yaml
│   ├── Chart.yaml
│   └── values.yaml
├── README.md
├── <app-1> # Application app-1 helm chart
│   └── helm
│       ├── Chart.yaml
│       ├── templates
│       │   └── ...
│       ├── values-<stage-1>.yaml # Values for stage-1
│       ├── values-<stage-n>.yaml # Values for stage-n
│       └── values.yaml # Common values for all stages
└── <app-2> # Application app-1 helm chart
    └── helm
        ├── Chart.yaml
        ├── templates
        │   └── ...
        ├── values-<stage-1>.yaml
        ├── values-<stage-n>.yaml
        └── values.yaml
```

Please note: you will have to create or delete additional `values-<stage-x>.yaml` files depending on your applications needs and stages.

You can also see [bit-scsss-gitops](https://bitbucket.bit.admin.ch/projects/STI/repos/bit-scsss-gitops/browse) as an example for the gitops repository setup.

### Azure DevOps Server and Monorepo configuration

Using Azure DevOps Server and monorepo setup of the Generic-Application-Pipeline will not notify the Azure DevOps Server Pipeline about the individual status of the triggered Tekton PipelineRuns.

## Configurable parameters and default values

<table>
	<thead>
		<th>Key</th>
		<th>Type</th>
		<th>Default</th>
		<th>Description</th>
	</thead>
	<tbody>
		<tr>
			<td>applications</td>
			<td>list</td>
			<td><pre lang="json">
[]
</pre>
</td>
			<td>Application specific configuration for the pipeline</td>
		</tr>
		<tr>
			<td>applications[0].additionalImageRegistries</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Optional: Overrides `defaultAdditionalImageRegistries`</td>
		</tr>
		<tr>
			<td>applications[0].appName</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Application name will be used for resource name creation in Openshift</td>
		</tr>
		<tr>
			<td>applications[0].branchTaskConfiguration</td>
			<td>object</td>
			<td><pre lang="json">
{
  "develop": {
    "buildNotifications": null,
    "deployStage": null,
    "downloadFiles": null,
    "promoteImage": null,
    "runMalwareScan": null,
    "runScaCheck": null,
    "verifyDeployment": null
  },
  "feature": {
    "buildNotifications": null,
    "deployStage": null,
    "downloadFiles": null,
    "promoteImage": null,
    "runMalwareScan": null,
    "runScaCheck": null,
    "verifyDeployment": null
  },
  "hotfix": {
    "buildNotifications": null,
    "deployStage": null,
    "downloadFiles": null,
    "promoteImage": null,
    "runMalwareScan": null,
    "runScaCheck": null,
    "verifyDeployment": null
  },
  "master": {
    "buildNotifications": null,
    "deployStage": null,
    "downloadFiles": null,
    "promoteImage": null,
    "runMalwareScan": null,
    "runScaCheck": null,
    "verifyDeployment": null
  },
  "release": {
    "buildNotifications": null,
    "deployStage": null,
    "downloadFiles": null,
    "promoteImage": null,
    "runMalwareScan": null,
    "runScaCheck": null,
    "verifyDeployment": null
  }
}
</pre>
</td>
			<td>Optional: Overrides `defaultBranchTaskConfiguration` For branch matching and flag dependencies, see `defaultBranchTaskConfiguration`</td>
		</tr>
		<tr>
			<td>applications[0].buildAddLabels</td>
			<td>bool</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides defaultBuildAddLabels</td>
		</tr>
		<tr>
			<td>applications[0].buildImageContextPath</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides: `defaultBuildImageContextPath`</td>
		</tr>
		<tr>
			<td>applications[0].buildImageFormat</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides: `defaultBuildImageFormat`</td>
		</tr>
		<tr>
			<td>applications[0].buildVersionAdditionalTags</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides: `defaultBuildVersionAdditionalTags`</td>
		</tr>
		<tr>
			<td>applications[0].buildVersionDatePattern</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultBuildVersionDatePattern`</td>
		</tr>
		<tr>
			<td>applications[0].buildVersionFilePath</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultBuildVersionFilePath`</td>
		</tr>
		<tr>
			<td>applications[0].buildVersionReplacementString</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultBuildVersionReplacementString`</td>
		</tr>
		<tr>
			<td>applications[0].buildVersionTemplate</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultBuildVersionTemplate`</td>
		</tr>
		<tr>
			<td>applications[0].containerFilePath</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultContainerFilePath`</td>
		</tr>
		<tr>
			<td>applications[0].credentialScanAllowsSkipOnFailure</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultCredentialScanAllowsSkipOnFailure`</td>
		</tr>
		<tr>
			<td>applications[0].customValuesPath</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultCustomValuePath`</td>
		</tr>
		<tr>
			<td>applications[0].dependencyTrackParentName</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultDependencyTrackParentName`</td>
		</tr>
		<tr>
			<td>applications[0].dependencyTrackParentVersion</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultDependencyTrackParentVersion`</td>
		</tr>
		<tr>
			<td>applications[0].gitCommitterEmail</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides: `defaultGitCommitterEmail`</td>
		</tr>
		<tr>
			<td>applications[0].gitCommitterName</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides: `defaultGitCommitterName`</td>
		</tr>
		<tr>
			<td>applications[0].httpProxy</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Allows to specify application specific HTTP proxy to use in Buildah Task</td>
		</tr>
		<tr>
			<td>applications[0].imageName</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Name of the image the pipeline will produce without registry URL. Examples: `bit/application1`</td>
		</tr>
		<tr>
			<td>applications[0].imageRegistry</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Image registry on which the pipeline will push resulting images</td>
		</tr>
		<tr>
			<td>applications[0].imageRegistryStaging</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Image registry on which the pipeline will push images for temporary purpose during pipeline runs. See `imageRegistry` for resulting images.</td>
		</tr>
		<tr>
			<td>applications[0].malwareScanAllowsSkipOnFailure</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultMalwareScanAllowsSkipOnFailure`</td>
		</tr>
		<tr>
			<td>applications[0].malwareScanVaultSecretPath</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: No default value. Required for pipelines with `runMalwareScan=true`. Value is taken directly from the antivirus service binding.</td>
		</tr>
		<tr>
			<td>applications[0].pipelineStorage</td>
			<td>object</td>
			<td><pre lang="json">
{
  "accessMode": null,
  "size": null,
  "storageClass": null
}
</pre>
</td>
			<td>Optional: Overrides `defaultPipelineStorage`</td>
		</tr>
		<tr>
			<td>applications[0].pipelineTasksConfig</td>
			<td>object</td>
			<td><pre lang="json">
{
  "dependencyTrackApiUrl": null,
  "downloadFilesExecuteCustomScript": null,
  "downloadFilesFileName": null
}
</pre>
</td>
			<td>Optional: Overrides `defaultPipelineTasksConfig`</td>
		</tr>
		<tr>
			<td>applications[0].pipelineTasksConfig.dependencyTrackApiUrl</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: The dependency track apiserver URL to upload the SBOM. Only required together with `runScaCheck`.</td>
		</tr>
		<tr>
			<td>applications[0].pipelineTasksConfig.downloadFilesExecuteCustomScript</td>
			<td>bool</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultPipelineTasksConfig.downloadFilesExecuteCustomScript`</td>
		</tr>
		<tr>
			<td>applications[0].pipelineTasksConfig.downloadFilesFileName</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultPipelineTasksConfig.downloadFilesFileName`</td>
		</tr>
		<tr>
			<td>applications[0].repoType</td>
			<td>string</td>
			<td><pre lang="json">
"bitbucket"
</pre>
</td>
			<td>Repository type. For instance: 'bitbucket' or 'azure'</td>
		</tr>
		<tr>
			<td>applications[0].srcRepo</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Application repository SSH URL</td>
		</tr>
		<tr>
			<td>applications[0].taskComputeResources</td>
			<td>object</td>
			<td><pre lang="json">
{
  "acsImageScan": null,
  "buildImage": null,
  "cleanup": null,
  "credentialScan": null,
  "downloadFiles": null,
  "gitClone": null,
  "logging": null,
  "malwareScan": null,
  "notifyBitbucket": null,
  "notifyBitbucketInprogress": null,
  "notifyFailure": null,
  "promoteImage": null,
  "scaCheck": null,
  "setup": null,
  "trivyImageScan": null
}
</pre>
</td>
			<td>Optional: Overrides `defaultTaskComputeResources`</td>
		</tr>
		<tr>
			<td>applications[0].triggerBranchPattern</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides: `defaultTriggerBranchPattern`. Note this is not available in monorepo mode of GAP.</td>
		</tr>
		<tr>
			<td>applications[0].trivyIgnorePath</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Optional: Overrides `defaultTrivyIgnorePath`</td>
		</tr>
		<tr>
			<td>applications[0].trivyIgnoreUnfixed</td>
			<td>bool</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultTrivyIgnoreUnfixed`</td>
		</tr>
		<tr>
			<td>applications[0].usePipelinePvcForBuild</td>
			<td>bool</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Overrides `defaultUsePipelinePvcForBuild`. Only use if your build process requires over 20GB of space and together with `pipelineStorage`. This slows down your build! Does not work if your pipeline PVC has accessModes `ReadWriteMany`.</td>
		</tr>
		<tr>
			<td>customLabels</td>
			<td>list</td>
			<td><pre lang="json">
[]
</pre>
</td>
			<td>Add customer labels to the kubernetes artefacts with key: value pairs. See test/values.custom-labels.yaml for more information. The keys `pipeline.bit.admin.ch/*` are blocked. The syntax and length according the [Kubernetes Documentation](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#syntax-and-character-set) needs to be fulfilled.</td>
		</tr>
		<tr>
			<td>datacenter.transformConfigs</td>
			<td>object</td>
			<td><pre lang="json">
{
  "campus": [
    {
      "pattern": "nexus\\.bit\\.admin\\.ch",
      "replace": "nexus-campus.bit.admin.ch"
    },
    {
      "pattern": "bitbucket\\.bit\\.admin.ch",
      "replace": "bitbucket-campus.bit.admin.ch"
    },
    {
      "pattern": "central-stackrox\\.apps\\.p-svz-ros-mgmt-prd-01\\.cloud\\.admin\\.ch",
      "replace": "central-stackrox.apps.c-svz-ros-mgmt-prd-01.cloud.admin.ch"
    }
  ],
  "primus": []
}
</pre>
</td>
			<td>Available datacenter specific transformations, each configuration has a name and contains a list of pattern+replacement to apply on specific values, Helm's `regexReplaceAll` is used.</td>
		</tr>
		<tr>
			<td>datacenter.transformConfigs.campus</td>
			<td>list</td>
			<td><pre lang="json">
[
  {
    "pattern": "nexus\\.bit\\.admin\\.ch",
    "replace": "nexus-campus.bit.admin.ch"
  },
  {
    "pattern": "bitbucket\\.bit\\.admin.ch",
    "replace": "bitbucket-campus.bit.admin.ch"
  },
  {
    "pattern": "central-stackrox\\.apps\\.p-svz-ros-mgmt-prd-01\\.cloud\\.admin\\.ch",
    "replace": "central-stackrox.apps.c-svz-ros-mgmt-prd-01.cloud.admin.ch"
  }
]
</pre>
</td>
			<td>Campus datacenter transformations, list of transformations needed for Campus</td>
		</tr>
		<tr>
			<td>datacenter.transformConfigs.primus</td>
			<td>list</td>
			<td><pre lang="json">
[]
</pre>
</td>
			<td>Primus datacenter transformations, is considered as pipeline default (all defaults points to Primus, no transformations needed), this entry is present for documentation purpose</td>
		</tr>
		<tr>
			<td>datacenter.useDatacenter</td>
			<td>string</td>
			<td><pre lang="json">
"primus"
</pre>
</td>
			<td>Optional: Name of the datacenter `transformConfigs` to apply. The pipeline depends on various resources (Bitbucket repositories, Nexus image registries for example) to work properly, these resources are located in a specific datacenter (by default on Primus, see `useDatacenter`), but it is possible to override specific resources references to point to a different datacenter - Campus for example. `transformConfigs` are set of patterns and replacements used to transform specific datacenter dependant resources references to another datacenter.</td>
		</tr>
		<tr>
			<td>defaultAdditionalImageRegistries</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Space separated list of additional image registries for produced image, example: additional-repo.nexus-campus.bit.admin.ch registry.access.redhat.com</td>
		</tr>
		<tr>
			<td>defaultArgoCDPreSyncHookDeletePVC</td>
			<td>bool</td>
			<td><pre lang="json">
true
</pre>
</td>
			<td>Whether to enable the ArgoCD Presync hook to delete eventual remaining RWM PVC created in previous releases</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration</td>
			<td>object</td>
			<td><pre lang="json">
{
  "develop": {
    "buildNotifications": true,
    "deployStage": "none",
    "downloadFiles": false,
    "promoteImage": true,
    "runMalwareScan": false,
    "runScaCheck": false,
    "verifyDeployment": false
  },
  "feature": {
    "buildNotifications": true,
    "deployStage": "none",
    "downloadFiles": false,
    "promoteImage": true,
    "runMalwareScan": false,
    "runScaCheck": false,
    "verifyDeployment": false
  },
  "hotfix": {
    "buildNotifications": true,
    "deployStage": "none",
    "downloadFiles": false,
    "promoteImage": true,
    "runMalwareScan": false,
    "runScaCheck": false,
    "verifyDeployment": false
  },
  "master": {
    "buildNotifications": true,
    "deployStage": "none",
    "downloadFiles": false,
    "promoteImage": true,
    "runMalwareScan": false,
    "runScaCheck": false,
    "verifyDeployment": false
  },
  "release": {
    "buildNotifications": true,
    "deployStage": "none",
    "downloadFiles": false,
    "promoteImage": true,
    "runMalwareScan": false,
    "runScaCheck": false,
    "verifyDeployment": false
  }
}
</pre>
</td>
			<td>Task configuration per branch. Defines which pipeline tasks are executed for a given branch type. If no branches match, falls back to values from `feature`.</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.develop</td>
			<td>object</td>
			<td><pre lang="json">
{
  "buildNotifications": true,
  "deployStage": "none",
  "downloadFiles": false,
  "promoteImage": true,
  "runMalwareScan": false,
  "runScaCheck": false,
  "verifyDeployment": false
}
</pre>
</td>
			<td>Applied when committing to branches like `develop*`</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.feature</td>
			<td>object</td>
			<td><pre lang="json">
{
  "buildNotifications": true,
  "deployStage": "none",
  "downloadFiles": false,
  "promoteImage": true,
  "runMalwareScan": false,
  "runScaCheck": false,
  "verifyDeployment": false
}
</pre>
</td>
			<td>Applied when committing to branches like `feature/*`</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.feature.buildNotifications</td>
			<td>bool</td>
			<td><pre lang="json">
true
</pre>
</td>
			<td>Defines whether build notifications are sent to source code repository</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.feature.deployStage</td>
			<td>string</td>
			<td><pre lang="json">
"none"
</pre>
</td>
			<td>Defines whether the pipeline should trigger a deployment for the image using the values from the specified path</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.feature.downloadFiles</td>
			<td>bool</td>
			<td><pre lang="json">
false
</pre>
</td>
			<td>Defines whether download files task is executed</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.feature.promoteImage</td>
			<td>bool</td>
			<td><pre lang="json">
true
</pre>
</td>
			<td>Defines whether built image is promoted to image registry</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.feature.runMalwareScan</td>
			<td>bool</td>
			<td><pre lang="json">
false
</pre>
</td>
			<td>Defines whether the malware scan task is executed</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.feature.runScaCheck</td>
			<td>bool</td>
			<td><pre lang="json">
false
</pre>
</td>
			<td>Defines whether SCA check task is executed</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.feature.verifyDeployment</td>
			<td>bool</td>
			<td><pre lang="json">
false
</pre>
</td>
			<td>Defines whether the build pipeline triggers the verification pipeline</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.hotfix</td>
			<td>object</td>
			<td><pre lang="json">
{
  "buildNotifications": true,
  "deployStage": "none",
  "downloadFiles": false,
  "promoteImage": true,
  "runMalwareScan": false,
  "runScaCheck": false,
  "verifyDeployment": false
}
</pre>
</td>
			<td>Applied when committing to branches like `hotfix/*`</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.master</td>
			<td>object</td>
			<td><pre lang="json">
{
  "buildNotifications": true,
  "deployStage": "none",
  "downloadFiles": false,
  "promoteImage": true,
  "runMalwareScan": false,
  "runScaCheck": false,
  "verifyDeployment": false
}
</pre>
</td>
			<td>Applied when committing to branches `master` or `main`</td>
		</tr>
		<tr>
			<td>defaultBranchTaskConfiguration.release</td>
			<td>object</td>
			<td><pre lang="json">
{
  "buildNotifications": true,
  "deployStage": "none",
  "downloadFiles": false,
  "promoteImage": true,
  "runMalwareScan": false,
  "runScaCheck": false,
  "verifyDeployment": false
}
</pre>
</td>
			<td>Applied when committing to branches like `release/*`</td>
		</tr>
		<tr>
			<td>defaultBuildAddLabels</td>
			<td>bool</td>
			<td><pre lang="json">
true
</pre>
</td>
			<td>Whether to add build related FOITT labels to the image</td>
		</tr>
		<tr>
			<td>defaultBuildImageContextPath</td>
			<td>string</td>
			<td><pre lang="json">
"."
</pre>
</td>
			<td>The path used as build context for the image build. Useful when your Containerfile is not in the repository root and needs to be built from a specific subdirectory (e.g. app1/). Pipeline will preferrably try to use defaultBuildImageContextPath/defaultContainerFilePath and fallback to defaultContainerFilePath if file is not found Examples: `application1/`, `.`</td>
		</tr>
		<tr>
			<td>defaultBuildImageFormat</td>
			<td>string</td>
			<td><pre lang="json">
"oci"
</pre>
</td>
			<td>The format of the built container, oci or docker</td>
		</tr>
		<tr>
			<td>defaultBuildVersionAdditionalTags</td>
			<td>string</td>
			<td><pre lang="json">
"latest"
</pre>
</td>
			<td>Additional tags for the produced image</td>
		</tr>
		<tr>
			<td>defaultBuildVersionDatePattern</td>
			<td>string</td>
			<td><pre lang="json">
"%Y-%m-%dT%H%M%S"
</pre>
</td>
			<td>The date pattern used for <TIMESTAMP> in `buildVersionTemplate`</td>
		</tr>
		<tr>
			<td>defaultBuildVersionFilePath</td>
			<td>string</td>
			<td><pre lang="json">
"Tektonfile.json"
</pre>
</td>
			<td>Path to the Tektonfile containing the `imageVersion` value. Used to generate image tag</td>
		</tr>
		<tr>
			<td>defaultBuildVersionReplacementString</td>
			<td>string</td>
			<td><pre lang="json">
"_"
</pre>
</td>
			<td>Replacement string to replace non-OCI compliant characters in the generated image tag. This will replace e.g. Bitbuckets "/" character in the branch prefix with the specified string to generate an OCI compliant image tag. See OCI specification https://github.com/opencontainers/distribution-spec/blob/main/spec.md#pulling-manifests.</td>
		</tr>
		<tr>
			<td>defaultBuildVersionTemplate</td>
			<td>string</td>
			<td><pre lang="json">
"\u003cCOMMIT_HASH\u003e"
</pre>
</td>
			<td>The version template to be used for the image tag</td>
		</tr>
		<tr>
			<td>defaultContainerFilePath</td>
			<td>string</td>
			<td><pre lang="json">
"Dockerfile"
</pre>
</td>
			<td>Default path including the name of your Containerfile in your repository containing the Containerfile/Dockerfile. see defaultBuildImageContextPath for related additional informations. Examples: `application1/Containerfile`, `application2/image/Dockerfile`.</td>
		</tr>
		<tr>
			<td>defaultCredentialScanAllowsSkipOnFailure</td>
			<td>bool</td>
			<td><pre lang="json">
true
</pre>
</td>
			<td>The task `credential-scan` is skipped in case of a finding. Set to 'false' in case the pipeline should fail.</td>
		</tr>
		<tr>
			<td>defaultCustomValuePath</td>
			<td>string</td>
			<td><pre lang="json">
"helm/values.yaml"
</pre>
</td>
			<td>Default relative path to locate Helmchart values file in `srcRepo` file structure. The full path is concat with  `<appName>` + `/helm/values.yaml`</td>
		</tr>
		<tr>
			<td>defaultDependencyTrackParentName</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Optional: The parentName of the dependency track project to where the sbom file is uploaded. The parent (name&version) must already exist. Use together with `dependencyTrackParentVersion`. Be aware that dependency track only uses projectName and projectVersion to identify the project. The parentName is not an identifying factor of a project.</td>
		</tr>
		<tr>
			<td>defaultDependencyTrackParentVersion</td>
			<td>string</td>
			<td><pre lang="json">
""
</pre>
</td>
			<td>Optional: The parentVersion of the dependency track project to where the sbom file is uploaded. The parent (name&version) must already exist. Use together with `dependencyTrackParentName`. Be aware that dependency track only uses projectName and projectVersion to identify the project. The parentVersion is not an identifying factor of a project.</td>
		</tr>
		<tr>
			<td>defaultFailureNotificationRecipients</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Comma-space-separated list of email addresses used as recipients in failure notification, additionally, `pipeline.bit.admin.ch/actor-email` PipelineRun annotation will be added to recipient list if available, empty value will prevent notification to be sent.</td>
		</tr>
		<tr>
			<td>defaultFailureNotificationSender</td>
			<td>string</td>
			<td><pre lang="json">
"tekton@bit.admin.ch"
</pre>
</td>
			<td>Email address used as sender in failure notification, empty value will prevent notification to be sent</td>
		</tr>
		<tr>
			<td>defaultFailureNotificationServerHost</td>
			<td>string</td>
			<td><pre lang="json">
"mailhost.mail.admin.ch"
</pre>
</td>
			<td>SMTP server used in failure notification</td>
		</tr>
		<tr>
			<td>defaultFailureNotificationServerPort</td>
			<td>int</td>
			<td><pre lang="json">
25
</pre>
</td>
			<td>SMTP port used in failure notification</td>
		</tr>
		<tr>
			<td>defaultGitCommitterEmail</td>
			<td>string</td>
			<td><pre lang="json">
"openshift@bit.admin.ch"
</pre>
</td>
			<td>Default committer email used for Git commits</td>
		</tr>
		<tr>
			<td>defaultGitCommitterName</td>
			<td>string</td>
			<td><pre lang="json">
"Tekton"
</pre>
</td>
			<td>Default committer name used for Git commits</td>
		</tr>
		<tr>
			<td>defaultHttpProxy</td>
			<td>string</td>
			<td><pre lang="json">
"http://prxp01.admin.ch:8080"
</pre>
</td>
			<td>Allows to specify HTTP proxy to use in all applications Buildah Task</td>
		</tr>
		<tr>
			<td>defaultMalwareScanAllowsSkipOnFailure</td>
			<td>bool</td>
			<td><pre lang="json">
false
</pre>
</td>
			<td>The task `malware-scan` fails in case of a finding. Set to 'true' in case the pipeline should continue anyway.</td>
		</tr>
		<tr>
			<td>defaultPipelineStorage</td>
			<td>object</td>
			<td><pre lang="json">
{
  "accessMode": "ReadWriteOnce",
  "size": "5Gi",
  "storageClass": "iscsi-ssd-encrypt"
}
</pre>
</td>
			<td>Configure storage for pipeline Size of the PVC used for temporary storage during pipeline run execution. Be aware of the PVC storageclass request limit of your namespace.</td>
		</tr>
		<tr>
			<td>defaultPipelineTasksConfig</td>
			<td>object</td>
			<td><pre lang="json">
{
  "dependencyTrackApiUrl": "none",
  "downloadFilesExecuteCustomScript": false,
  "downloadFilesFileName": "Downloadfile"
}
</pre>
</td>
			<td>Pipeline task configuration used to configure additional task-specific parameters like file names or API URLs.</td>
		</tr>
		<tr>
			<td>defaultPipelineTasksConfig.dependencyTrackApiUrl</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>The dependency track apiserver URL to upload the SBOM. Only used together with `runScaCheck` and has to be overridden.</td>
		</tr>
		<tr>
			<td>defaultPipelineTasksConfig.downloadFilesExecuteCustomScript</td>
			<td>bool</td>
			<td><pre lang="json">
false
</pre>
</td>
			<td>If `false` `downloadFilesFileName` will be used a list of artifacts given as URLs to download. If `true` the file will be executed as script</td>
		</tr>
		<tr>
			<td>defaultPipelineTasksConfig.downloadFilesFileName</td>
			<td>string</td>
			<td><pre lang="json">
"Downloadfile"
</pre>
</td>
			<td>The name of the file to be used a source for the download-files task</td>
		</tr>
		<tr>
			<td>defaultTaskComputeResources</td>
			<td>object</td>
			<td><pre lang="json">
{
  "acsImageScan": null,
  "buildImage": null,
  "cleanup": null,
  "credentialScan": null,
  "downloadFiles": null,
  "gitClone": null,
  "logging": null,
  "malwareScan": null,
  "notifyBitbucket": null,
  "notifyBitbucketInprogress": null,
  "notifyFailure": null,
  "promoteImage": null,
  "scaCheck": null,
  "setup": null,
  "trivyImageScan": null
}
</pre>
</td>
			<td>To override each Task's computeResources in pipelineRun context triggered through EventListener, each entry refers a task name (CamelCase format) and can contain a `computeResource` key, specification follows Tekton's [Specifying taskRunSpecs](https://tekton.dev/docs/pipelines/pipelineruns/#specifying-taskrunspecs) specifications, usage documented in [Specifying Task-level ComputeResources](https://tekton.dev/docs/pipelines/pipelineruns/#specifying-task-level-computeresources). Please note that for a namespace with a applied ResourceQuota that requests and limits are not calculated in the same way. Please refer to [ResourceQuota Support](https://tekton.dev/docs/pipelines/compute-resources/#resourcequota-support) in Tekton documentation.</td>
		</tr>
		<tr>
			<td>defaultTriggerBranchPattern</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Regex expression for which branches a build will be triggered. The expression must follow the [RE2 syntax](https://github.com/google/cel-spec/blob/master/doc/langdef.md#regular-expressions)</td>
		</tr>
		<tr>
			<td>defaultTrivyIgnorePath</td>
			<td>string</td>
			<td><pre lang="json">
".trivyignore"
</pre>
</td>
			<td>Path to .trivyignore file, path is not affected by `defaultBuildImageContextPath` or `buildImageContextPath`</td>
		</tr>
		<tr>
			<td>defaultTrivyIgnoreUnfixed</td>
			<td>bool</td>
			<td><pre lang="json">
true
</pre>
</td>
			<td>This will disable policy violation in the `prescan-image` check performed by Trivy for unpatched/unfixed vulnerabilities</td>
		</tr>
		<tr>
			<td>defaultUsePipelinePvcForBuild</td>
			<td>bool</td>
			<td><pre lang="json">
false
</pre>
</td>
			<td>Use the Pipeline PVC instead of the ephemeral emptyDir for the bit-buildah task</td>
		</tr>
		<tr>
			<td>enableMonorepo</td>
			<td>bool</td>
			<td><pre lang="json">
false
</pre>
</td>
			<td>Optional: Enable monorepo support for the pipeline. This will start a preprocessing PipelineRun before the image pipelines are started.</td>
		</tr>
		<tr>
			<td>eventListener.replicas</td>
			<td>int</td>
			<td><pre lang="json">
1
</pre>
</td>
			<td>Number of containers created by the EventListener</td>
		</tr>
		<tr>
			<td>eventListener.resources</td>
			<td>object</td>
			<td><pre lang="json">
{
  "limits": {
    "cpu": "25m",
    "memory": "128Mi"
  },
  "requests": {
    "cpu": "1m",
    "memory": "64Mi"
  }
}
</pre>
</td>
			<td>Resource definition of the EventListener containers</td>
		</tr>
		<tr>
			<td>overrideReleaseName</td>
			<td>string</td>
			<td><pre lang="json">
null
</pre>
</td>
			<td>Optional: Name which will be used to override the default `.Release.Name` to generate resource names in this chart. You can for example specify a shorter name here if resource creation fails due to character length restrictions. The overrideReleaseName must not be longer than 53 characters.</td>
		</tr>
		<tr>
			<td>triggerTemplate.defaultEmailAddress</td>
			<td>string</td>
			<td><pre lang="json">
"tekton@bit.admin.ch"
</pre>
</td>
			<td>email address used to allow test to be triggered within pipeline run</td>
		</tr>
	</tbody>
</table>

## Source Code

* <https://bitbucket.bit.admin.ch/projects/CNP/repos/generic-application-pipeline/browse>

## Maintainers

| Name | Email | Url |
| ---- | ------ | --- |
| System Team IRS (PS-IRS-STI) | <sti@bit.admin.ch> | <https://docs.bcp.bit.admin.ch/support/> |