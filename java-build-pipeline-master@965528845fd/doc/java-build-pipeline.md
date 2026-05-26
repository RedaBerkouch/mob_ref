# Java Build Pipeline

The java build pipeline is a Tekton-based CI/CD pipeline designed for building and deploying Java applications.

## Steps

This pipeline consists of the following steps:

| **Step Name**                                           | **Description**                                                                                 | **When Clause**                                                                                                |
|---------------------------------------------------------|-------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `git-clone`                                             | Clones the Git repository.                                                                      | Always                                                                                                         |
| `checkout-branch`                                       | Checks out branch if it is defined, this is necessary for some plugins used within the pipeline | If branch is defined                                                                                           |
| `setup`                                                 | Sets up the pipeline with necessary configurations and parameters.                              | Always                                                                                                         |
| `execute-after-setup-script`                            | Executes the defined script after setup                                                         | Executes if hookScripts.afterSetup is set, see [Hook scripts](#hook-scripts)                                   |
| `notify-bitbucket-inprogress`                           | Notifies Bitbucket that the build is in progress.                                               | Always                                                                                                         |
| `credential-scan`                                       | Scans the repository for credentials using Trufflehog.                                          | Always                                                                                                         |
| `maven-build`                                           | Builds and tests the application using Maven.                                                   | Executes if the build tool is set to Maven.                                                                    |                                                            
| `execute-after-build-script`                            | Executes the defined script after build                                                         | Executes if hookScripts.afterBuild is set, see [Hook scripts](#hook-scripts)                                   |
| `quality-check`                                         | Performs a quality check using SonarQube.                                                       | Executes if qualityCheck is set to true on branch configuration.                                               |
| `execute-after-quality-check-script`                    | Executes the defined script after build                                                         | Executes if hookScripts.afterQualityCheck is set, see [Hook scripts](#hook-scripts)                            |
| `validate-quality-gate`                                 | Validates the SonarQube quality gate.                                                           | Executes if qualityGate is set to true on branch configuration.                                                |
| `maven-deploy`                                          | Deploys the Maven artifacts to the repository.                                                  | Executes if deploying Maven artifacts is enabled.                                                              |
| `git-push-tag`                                          | Pushes a Git tag for the version.                                                               | Executes if publishVersionTag is set to true and publishGitTag is not set to false.                            |
| `execute-after-push-tag-script`                         | Executes the defined script after build                                                         | Executes if hookScripts.afterPushTag is set, see [Hook scripts](#hook-scripts)                                 |
| `udeploy-push`                                          | Pushes the artifact to UrbanCode Deploy.                                                        | Executes if pushing to UrbanCode Deploy is enabled.                                                            |
| `build-image`                                           | Builds a container image using Buildah.                                                         | Executes if deployStage is set or publishVersionTag is set to true and appType is not library and not udeploy. |
| `trivy-image-scan`                                      | Scans the container image for vulnerabilities using Trivy.                                      | Executes if scanning the container image with Trivy is enabled.                                                |
| `acs-image-scan`                                        | Scans the container image with ACS (Advanced Container Security).                               | Executes if ACS image scanning is enabled and a deployment stage is specified.                                 |
| `promote-image`                                         | Promotes the container image to an external registry.                                           | Always                                                                                                         |
| `trigger-deployment-pipeline-with-digest-tagged-image`  | Triggers the deployment pipeline with a digest-tagged image.                                    | Executes if a deployment stage is specified and pushing a version tag is not enabled.                          |
| `trigger-deployment-pipeline-with-version-tagged-image` | Triggers the deployment pipeline with a version-tagged image.                                   | Executes if a deployment stage is specified and pushing a version tag is enabled.                              |
| `logging`                                               | Aggregates logs for the pipeline run.                                                           | Always                                                                                                         |
| `cleanup`                                               | Cleans up the workspace after the pipeline run.                                                 | Always                                                                                                         |
| `notify-bitbucket`                                      | Notifies Bitbucket of the build result.                                                         | Always                                                                                                         |
| `notify-failure`                                        | Sends a failure notification.                                                                   | Always                                                                                                         |
| `mail-failure`                                          | Sends a failure email notification.                                                             | `.sendNotification` is `true`                                                                                  |

## Configuration

The configuration of the java build pipeline is done via a `values.yaml` file. This file contains all the necessary
parameters for the pipeline.

**Note:** The configuration is hierarchical, meaning that parameters can be set at different levels, such
as `global`, `java-build-pipeline`, `java-build-pipeline.applications.<appName>`
and `java-build-pipeline.applications.<appName>.branchConfig.<branchName>`.
Parameters at `java-build-pipeline.applications.<appName>` level override those at `java-build-pipeline` level.
Parameters at `java-build-pipeline.applications.<appName>.branchConfig.<branchName>` level override those
at `java-build-pipeline.applications.<appName>` level.

### General

| **Name**                               | **Level in values.yaml**                                               | **Description**                                                                                                                                                                                                                  | **Optional** | **Default**                                | **Example**                                                          |
|----------------------------------------|------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------|--------------------------------------------|----------------------------------------------------------------------|
| `gitOpsRepo`                           | `global`                                                               | The SSH URL of the GitOps Repo.                                                                                                                                                                                                  | No           |                                            | `ssh://git@bitbucket.bit.admin.ch/iewtool/toolchain-demo-gitops.git` |
| `cluster`                              | `global`                                                               | The cluster to use.                                                                                                                                                                                                              | No           |                                            | `p-szb-ros-shrd-npr-01`                                              |
| `nexusImageRegistryBaseName`           | `global`                                                               | Prefix of the Nexus registries used in the pipeline. Will be suffixed with: `jeap-docker-hosted`, `jeap-docker-hosted-incoming`, `jeap-docker-hosted-test`, documented here: https://confluence.bit.admin.ch/x/a_GXGg            | No           |                                            | `jeap-docker-hosted`                                                 |
| `overrideJbpReleaseName`               | `global`                                                               | The name under which the java build chart should be released.                                                                                                                                                                    | Yes          | `.Release.Name`                            | `test-jbp-pipeline-chart`                                            |
| `overrideJbpReleaseName`               | `global`                                                               | The name under which the cd chart is released. Is needed, if the deployment pipeline should be triggered.                                                                                                                        | Yes          | `""`                                       | `test-cdp-pipeline-chart`                                            |
| `enableWebhookCreation`                | `global`                                                               | if set to false disable the Job that creates the webhooks in Bitbucket.                                                                                                                                                          | Yes          | `"true"`                                   | `false`                                                              |
| `gitOpsRepoBranch`                     | `java-build-pipeline`                                                  | The branch inside the GitOps Repo. Needs to be set if different from default. Is needed, if the deployment pipeline should be triggered.                                                                                         | Yes          | `nonprod`                                  | `main`                                                               |
| `appName`                              | `java-build-pipeline.applications`                                     | The name of the application being deployed.                                                                                                                                                                                      | No           |                                            | `toolchain-demo`                                                     |
| `bitBucketRepo`                        | `java-build-pipeline.applications.<appName>`                           | The Bitbucket project of the application repository to be used for automatic webhook creation. Only required if it differs from the Bitbucket project of the GitOps repository. Use the name from the URL, not the display name. | Yes          | Bitbucket project of the gitops repository | `JMERHOS`                                                            |
| `appType`                              | `java-build-pipeline.applications.<appName>`                           | The type of the application being deployed. `application`: deployment to a docker-repo; `library`: deployment to a maven-repo instead of docker-repo, `udeploy`; library build with publish to UCD, instead of maven-repo.       | Yes          | `application`                              | `library`                                                            |
| `buildVersionTemplate`                 | `java-build-pipeline.applications.<appName>`                           | Template for the build version, see [CNCICD - Versioning](https://confluence.bit.admin.ch/display/CNCICD/CNCICD+%7C+Java+%7C+Versioning)                                                                                         | Yes          | `<POM_VERSION>`                            | `1.0.0`                                                              |
| `buildVersionDatePattern`              | `java-build-pipeline.applications.<appName>`                           | Date pattern for the build version.                                                                                                                                                                                              | Yes          | `""`                                       | `%Y-%m-%d`                                                           |
| `mavenReleaseRepo`                     | `java-build-pipeline.applications.<appName>`                           | Maven release repository URL.                                                                                                                                                                                                    | Yes          | `.defaultMavenReleaseRepo`                 | `https://nexus.example.com/release`                                  |
| `mavenSnapshotRepo`                    | `java-build-pipeline.applications.<appName>`                           | Maven snapshot repository URL.                                                                                                                                                                                                   | Yes          | `.defaultMavenSnapshotRepo`                | `https://nexus.example.com/snapshot`                                 |
| `npmRepo`                              | `java-build-pipeline.applications.<appName>`                           | NPM repository URL.                                                                                                                                                                                                              | Yes          | `.defaultNpmRepo`                          | `https://npm.example.com`                                            |
| `buildTool`                            | `java-build-pipeline.applications.<appName>`                           | Build tool used in the pipeline.                                                                                                                                                                                                 | Yes          | `maven`                                    | `gradle`                                                             |
| `trufflehogImage`                      | `java-build-pipeline.applications.<appName>`                           | Trufflehog image used for credential scanning.                                                                                                                                                                                   | Yes          | `.defaultTrufflehogImage`                  | `trufflehog:latest`                                                  |
| `trufflehogScanDepth`                  | `java-build-pipeline.applications.<appName>`                           | Depth of the Trufflehog scan.                                                                                                                                                                                                    | Yes          | `.defaultTrufflehogScanDepth`              | `100`                                                                |
| `mavenBuildJavaImage`                  | `java-build-pipeline.applications.<appName>`                           | Java image used for Maven builds.                                                                                                                                                                                                | Yes          | `.defaultMavenBuildJavaImage`              | `openjdk:11`                                                         |
| `mavenSubModule`                       | `java-build-pipeline.applications.<appName>`                           | Maven submodule to build.                                                                                                                                                                                                        | Yes          | `""`                                       | `module-name`                                                        |
| `includeMavenSubModules`               | `java-build-pipeline.applications.<appName>`                           | Additional Maven submodules to include.                                                                                                                                                                                          | Yes          | `[]`                                       | `- module1 - module2`                                                |
| `excludedMavenSubModulesFromSonarScan` | `java-build-pipeline.applications.<appName>`                           | Maven submodules excluded from SonarQube scans.                                                                                                                                                                                  | Yes          | `[]`                                       | `- module3`                                                          |
| `mavenBuildGoal`                       | `java-build-pipeline.applications.<appName>`                           | Maven build goal for general builds.                                                                                                                                                                                             | Yes          | `.defaultMavenBuildGoal`                   | `install`                                                            |
| `jarMavenDeployGoal`                   | `java-build-pipeline.applications.<appName>`                           | Maven deploy goal for JAR artifacts.                                                                                                                                                                                             | Yes          | `.defaultJarMavenDeployGoal`               | `jar:jar deploy:deploy`                                              |
| `ucdArtifactPath`                      | `java-build-pipeline.applications.<appName>`                           | Path at which the maven build puts the files that need to be pushed to UCD.                                                                                                                                                      | Yes          | `""`                                       | `./target/classes/Solution`                                          |
| `buildahImage`                         | `java-build-pipeline.applications.<appName>`                           | Buildah image used for container builds.                                                                                                                                                                                         | Yes          | `.defaultBuildahImage`                     | `buildah:latest`                                                     |
| `httpProxy`                            | `java-build-pipeline.applications.<appName>`                           | HTTP proxy URL.                                                                                                                                                                                                                  | Yes          | `.defaultHttpProxy`                        | `http://proxy.example.com`                                           |
| `buildImageFormat`                     | `java-build-pipeline.applications.<appName>`                           | Format for the build image.                                                                                                                                                                                                      | Yes          | `.defaultBuildImageFormat`                 | `docker`                                                             |
| `publishVersionTag`                    | `java-build-pipeline.applications.<appName>`                           | Flag to publish a version tag.                                                                                                                                                                                                   | Yes          | `""`                                       | `true`                                                               |
| `trivyIgnoreUnfixed`                   | `java-build-pipeline.applications.<appName>`                           | Flag to ignore unfixed vulnerabilities during Trivy scans.                                                                                                                                                                       | Yes          | `.defaultTrivyIgnoreUnfixed`               | `true`                                                               |
| `publishVersionTag`                    | `java-build-pipeline.applications.<appName>.branchConfig.<branchName>` | Will publish a "version" if true.                                                                                                                                                                                                | Yes          | `false`                                    | `true`                                                               |
| `publishGitTag`                        | `java-build-pipeline.applications.<appName>.branchConfig.<branchName>` | Will publish a Git tag if true.                                                                                                                                                                                                  | Yes          | Value of `publishVersionTag`               | `true`                                                               |
| `deployStage`                          | `java-build-pipeline.applications.<appName>.branchConfig.<branchName>` | If set to any value, the pipeline will try to deploy the image and use the values in the specified path.                                                                                                                         | Yes          | `none`                                     | `d`                                                                  |
| `deployApps`                          | `java-build-pipeline.applications.<appName>` | If not set a deploy pipeline with the same name as the build pipeline will be called. You can add a list of deploy pipeline app names to be called instead of the build app name. This is used if one image is used in multiple microservices                                     | Yes          | `none`                                     | `deployApps:` </br> `- <deployAppName1>` </br> `  - <deployAppName2>`                                                                |
| `qualityCheck`                         | `java-build-pipeline.applications.<appName>.branchConfig.<branchName>` | Optionally disable SonarQube scan.                                                                                                                                                                                               | Yes          | `true`                                     | `false`                                                              |
| `qualityGate`                          | `java-build-pipeline.applications.<appName>.branchConfig.<branchName>` | Optionally disable SonarQube quality gate (potentially breaking the build).                                                                                                                                                      | Yes          | `true`                                     | `false`                                                              |
| `verifyDeployment`                     | `java-build-pipeline.applications.<appName>.branchConfig.<branchName>` | When set to true, the build pipeline will trigger the verification pipeline.                                                                                                                                                     | Yes          | `false`                                    | `true`                                                               |
| `buildVersionTemplate`                 | `java-build-pipeline.applications.<appName>.branchConfig.<branchName>` | Option to overwrite the version template in the app settings.                                                                                                                                                                    | Yes          | `<POM_VERSION>`                            | `<TIMESTAMP>`                                                        |

### Pipeline / Storage

| **Name**                                        | **Level in values.yaml**                                               | **Description**                                                                                      | **Optional** | **Default**                            | **Example**         |
|-------------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|--------------|----------------------------------------|---------------------|
| `pipelineStorage.accessMode`                    | `java-build-pipeline` and `java-build-pipeline.applications.<appName>` | The access mode for pipeline storage.                                                                | Yes          | `.defaultPipelineStorage.accessMode`   | `ReadWriteOnce`     |
| `pipelineStorage.size`                          | `java-build-pipeline` and `java-build-pipeline.applications.<appName>` | The size of the pipeline storage.                                                                    | Yes          | `.defaultPipelineStorage.size`         | `2Gi`               |
| `pipelineStorage.storageClass`                  | `java-build-pipeline` and `java-build-pipeline.applications.<appName>` | The storage class for the pipeline storage.                                                          | Yes          | `.defaultPipelineStorage.storageClass` | `iscsi-ssd-encrypt` |
| `pipelineTimeout`                               | `java-build-pipeline.applications.<appName>`                           | Timeout for the pipeline execution.                                                                  | Yes          | `1h0m0s`                               | `3600s`             |
| `targetWorkloadResourceKind`                    | `java-build-pipeline.applications.<appName>`                           | Kind of the target workload resource, supported values are: `deployment`, `deploy`, `cronjob`, `cj`. | Yes          | `.defaultTargetWorkloadResourceKind`   | `deployment`        |
| `testcontainers.kubedock.sidecar.memoryRequest` | `java-build-pipeline`                                                  | Memory request for the kubedock sidecar container.                                                   | Yes          | `128Mi`                                | `128Mi`             |
| `testcontainers.kubedock.sidecar.memoryLimit`   | `java-build-pipeline`                                                  | Memory limit for the kubedock sidecar container.                                                     | Yes          | `512Mi`                                | `512Mi`             |
| `testcontainers.kubedock.sidecar.cpuRequest`    | `java-build-pipeline`                                                  | CPU request for the kubedock sidecar container.                                                      | Yes          | `150m`                                 | `150m`              |
| `testcontainers.kubedock.sidecar.cpuLimit`      | `java-build-pipeline`                                                  | CPU limit for the kubedock sidecar container.                                                        | Yes          | `500m`                                 | `500m`              |
| `testcontainers.kubedock.pod.memoryRequest`     | `java-build-pipeline`                                                  | Memory request for the kubedock pod.                                                                 | Yes          | `128Mi`                                | `128Mi`             |
| `testcontainers.kubedock.pod.memoryLimit`       | `java-build-pipeline`                                                  | Memory limit for the kubedock pod.                                                                   | Yes          | `512Mi`                                | `512Mi`             |
| `testcontainers.kubedock.pod.cpuRequest`        | `java-build-pipeline`                                                  | CPU request for the kubedock pod.                                                                    | Yes          | `250m`                                 | `250m`              |
| `testcontainers.kubedock.pod.cpuLimit`          | `java-build-pipeline`                                                  | CPU limit for the kubedock pod.                                                                      | Yes          | `700m`                                 | `700m`              |

### Pipeline Step Resource Allocation

Resource allocation for pipeline steps follows a **priority-based fallback mechanism**. This ensures that each step gets
appropriate CPU and memory resources, even if explicit values are not defined.

> Currently, resources for shared task over [CNP](https://bitbucket.bit.admin.ch/projects/CNT) cannot be defined yet.

#### Fallback Order

1. **Step-level resources**  
   If a step defines its own resources (CPU/memory requests and limits), these values are used.

   **Configuration path:**
   ```yaml
   resources.<taskName>.steps:
     - name: <step-name>
       resources:
         requests:
           cpu: <value>
           memory: <value>
         limits:
           cpu: <value>
           memory: <value>
   ```

2. **Task-level resources**  
   If the step does not define resources, the system checks the parent task for resources and applies them.

   **Configuration path:**
   ```yaml
   resources.<taskName>.resources:
     requests:
       cpu: <value>
       memory: <value>
     limits:
       cpu: <value>
       memory: <value>
   ```

3. **Default resource profile**  
   If neither the step nor the task specifies resources, a predefined profile is applied.
    - The profile is selected based on the `defaultProfile` parameter of the step or defaults to **`small`**.
    - Profiles are defined under `defaultResourceProfiles` and include standard CPU and memory requests/limits.

#### Tasks and Steps Overview

Below is the list of tasks and their associated steps:

| **Task**                | **Steps**                                |
|-------------------------|------------------------------------------|
| `build-setup`           | `setup-build-pipeline`, `aggregate-logs` |
| `exec-hook-script`      | `run-script`                             |
| `maven`                 | `init-version`, `mvn-goals`              |
| `mail-failure`          | `send`                                   |
| `udeploy-push`          | `udeploy-push`                           |
| `validate-quality-gate` | `check-quality-gate-status`              |

### Notification

| **Name**                 | **Level in values.yaml**                     | **Description**                                                      | **Optional** | **Default**                                              | **Example**              |
|--------------------------|----------------------------------------------|----------------------------------------------------------------------|--------------|----------------------------------------------------------|--------------------------|
| `sendNotification`       | `global`                                     | Boolean indicating whether to send notifications on failure (email). | Yes          | `false`                                                  | `true`                   |
| `notificationSender`     | `global`                                     | The sender of the email.                                             | Yes          | `.defaultNotificationSender`                             | `tekton@bit.admin.ch`    |
| `notificationServerHost` | `global`                                     | The mail server host URL.                                            | Yes          | `.defaultNotificationServerHost`                         | `mailhost.mail.admin.ch` |
| `notificationServerPort` | `global`                                     | The mail server host port.                                           | Yes          | `.defaultNotificationServerPort`                         | `25`                     |
| `notificationRecipients` | `global`                                     | Email recipients for all notifications.                              | Yes          |                                                          | `team@example.com`       |
| `notificationRecipients` | `java-build-pipeline`                        | Email recipients for build notifications.                            | Yes          | `global.notificationRecipients`                          | `team@example.com`       |
| `notificationRecipients` | `java-build-pipeline.applications.<appName>` | Email recipients for build notifications per application.            | Yes          | `java-build-pipeline.verificationNotificationRecipients` | `team@example.com`       |

### Message Contract

For more details, refer to the [documentation](https://confluence.bit.admin.ch/display/JEAP/Message+Contract+Service).

| **Name**                                        | **Level in values.yaml**                     | **Description**                                                                         | **Optional** | **Default**                                             | **Example**                                                      |
|-------------------------------------------------|----------------------------------------------|-----------------------------------------------------------------------------------------|--------------|---------------------------------------------------------|------------------------------------------------------------------|
| `messageContractApiUrl`                         | `global`                                     | See on application level. Be aware this parameter can get overwritten on deeper levels. | Yes          |                                                         | `https://dev-jme-internal.bit.admin.ch/message-contract-service` |
| `messageContractUser`                           | `global`                                     | See on application level. Be aware this parameter can get overwritten on deeper levels. | Yes          |                                                         | `write`                                                          |
| `messageContractApiUrl`                         | `java-build-pipeline`                        | The message contract service URL.                                                       | Yes          | `.messageContractApiUrl` on pipeline level or else `""` | `https://dev-jme-internal.bit.admin.ch/message-contract-service` |
| `messageContractUser`                           | `java-build-pipeline`                        | The username to access the message contract service.                                    | Yes          | `.messageContractUser` on pipeline level or else `""`   | `write`                                                          |
| `messageContractCompatibilityCheckEnvironments` | `java-build-pipeline.applications.<appName>` | On these environments the message contract compatibility check will be executed         | Yes          |                                                         | `dev,ref`                                                        |

### Hook scripts

To execute custom scripts at certain points in the pipeline, hook scripts can be defined.
The shell scripts have to be lay down in your application's repository.
So for each hook script, two parameters can be defined: `path` and `image`.
Define the path to your script and automatically activate the pipeline step.
The image parameter is optional. If not defined, a default toolbox image will be used.

To run maven commands within the hook scripts, use a java image.
Furthermore, there are some Maven args preconfigured in the tasks that can be used.
You can access them via the environment variable `MAVEN_ARGS`. For example:

```bash
./mvnw $MAVEN_ARGS test
```

| **Name**                  | **Level in values.yaml**                                 | **Description**                                       | **Optional** | **Default**         | **Example**                                                                  |
|---------------------------|----------------------------------------------------------|-------------------------------------------------------|--------------|---------------------|------------------------------------------------------------------------------|
| `afterSetup.path`         | `java-build-pipeline.applications.<appName>.hookScripts` | Script that is executed after the setup step.         | Yes          |                     | `./after-setup.sh`                                                           |
| `afterSetup.image`        | `java-build-pipeline.applications.<appName>.hookScripts` | Image used to execute the afterSetup script.          | Yes          | defaultToolboxImage | `toolchain-docker.nexus.bit.admin.ch/bit/ubi-java-build:java-21.x_node-20.x` |
| `afterBuild.path`         | `java-build-pipeline.applications.<appName>.hookScripts` | Script that is executed after the build step.         | Yes          |                     | `./after-build.sh`                                                           |
| `afterBuild.image`        | `java-build-pipeline.applications.<appName>.hookScripts` | Image used to execute the afterBuild script.          | Yes          | defaultToolboxImage | `toolchain-docker.nexus.bit.admin.ch/bit/ubi-java-build:java-21.x_node-20.x` |
| `afterQualityCheck.path`  | `java-build-pipeline.applications.<appName>.hookScripts` | Script that is executed after the quality check step. | Yes          |                     | `./after-quality-check.sh`                                                   |
| `afterQualityCheck.image` | `java-build-pipeline.applications.<appName>.hookScripts` | Image used to execute the afterQualityCheck script.   | Yes          | defaultToolboxImage | `toolchain-docker.nexus.bit.admin.ch/bit/ubi-java-build:java-21.x_node-20.x` |
| `afterPushTag.path`       | `java-build-pipeline.applications.<appName>.hookScripts` | Script that is executed after the push tag step.      | Yes          |                     | `./after-push-tag.sh`                                                        |
| `afterPushTag.image`      | `java-build-pipeline.applications.<appName>.hookScripts` | Image used to execute the afterPushTag script.        | Yes          | defaultToolboxImage | `toolchain-docker.nexus.bit.admin.ch/bit/ubi-java-build:java-21.x_node-20.x` |

### Example

```yaml
global:
  # Base
  nexusImageRegistryBaseName: bit-jme-docker-hosted
  imageRegistryDomain: nexus.bit.admin.ch/jme
  gitOpsRepo: ssh://git@bitbucket.bit.admin.ch/bit_jme/jme-gitops.git

  # Overrides the helm release name of the pipeline charts
  overrideJbpReleaseName: test-jb-ppl-chart

  # Notification
  sendNotification: true
  notificationSender: tekton@bit.admin.ch
  notificationServerHost: mailhost.mail.admin.ch
  notificationServerPort: 25

  # Message Contract
  messageContractApiUrl: https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/message-contract-service
  messageContractUser: write

  #Secrets
  secrets:
    enabled: true
    paths:
      test: { }
    externalStore:
      enabled: true
      vaults:
        - name: bit-jme-vault
          path: p-szb-ros-shrd-npr-01
          server: "https://vault-bfdcbaf9-e8b9-47c7-b296-5b0b0381aeb8.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch"
      paths:
        bitbucketAccessToken:
          id: 'bit-jme-pipelines-d/bitbucket'
          property: 'accesstoken'
          vault: bit-jme-vault
        nexusUserPw:
          id: 'bit-jme-pipelines-d/default'
          property: 'nexusUserPw'
          vault: bit-jme-vault
        nexus:
          id: 'bit-jme-pipelines-d/default'
          property: 'dockerconfig'
          vault: bit-jme-vault
        deploymentLogUserPw:
          id: 'bit-jme-pipelines-d/deploymentLog'
          property: 'deploymentLogUserPw'
          vault: bit-jme-vault
        messageContractAuth:
          id: 'bit-jme-pipelines-d/message-contract-auth'
          property: 'secret'
          vault: bit-jme-vault
        archRepoUserPw:
          id: 'bit-jme-pipelines-d/archRepo'
          property: 'archRepoUserPw'
          vault: bit-jme-vault
        tektonWebhookAccessToken:
          id: 'bit-jme-pipelines-d/webendpoint'
          property: 'tektonWebhookAccessToken'
          vault: bit-jme-vault
        tektonDeploymentWebhookAccessToken:
          id: 'bit-jme-pipelines-d/webendpoint'
          property: 'tektonUndeploymentWebhookAccessToken'
          vault: bit-jme-vault

java-build-pipeline:
  pipelineStorage:
    size: 3Gi
    accessMode: ReadWriteOnce
    storageClass: iscsi-ssd-encrypt

  resources:
    maven_build:
      - name: mvn-goals
        resources:
          limits:
            cpu: 1500m
            memory: 4Gi
          requests:
            cpu: 500m
            memory: 2Gi
    quality_check:
      - name: mvn-goals
        resources:
          limits:
            cpu: '1'
            memory: 2Gi
          requests:
            cpu: '0.5'
            memory: 2Gi

  applications:
    - appName: jme-swagger-service
      bitBucketRepo: jme-swagger-example
      mavenBuildJavaImage: 'toolchain-docker.nexus.bit.admin.ch/bit/ubi-java-build:java-21.x_node-20.x'
      mavenSubModule: 'jme-swagger-service'
      hookScripts:
        afterSetup:
          path: "./after-setup.sh"
        afterBuild:
          path: "./after-build.sh"
          image: 'toolchain-docker.nexus.bit.admin.ch/bit/ubi-java-build:java-21.x_node-20.x'
        afterQualityCheck:
          path: "./after-quality-check.sh"
          image: 'toolchain-docker.nexus.bit.admin.ch/bit/ubi-java-build:java-21.x_node-20.x'
        afterPushTag:
          path: "./after-push-tag.sh"
      excludedMavenSubModulesFromSonarScan:
        - 'jme-swagger-webflux-service'
        - 'jme-swagger-auth-scs'
      branchConfig:
        feature:
          publishVersionTag: false
          deployStage: "d"
          qualityCheck: false
          qualityGate: false
          buildVersionTemplate: <TIMESTAMP>_<BRANCH_HASH>-SNAPSHOT
        master:
          publishVersionTag: true
          publishGitTag: true
          deployStage: "d"
          qualityCheck: true
          qualityGate: ture
      pipelineStorage:
        size: 2Gi
```

### Inputs Configuration

For triggering the pipeline the following input parameters can be set:

| **Input Name**                 | **Description**                                       | **Optional** | **Default**     |
|--------------------------------|-------------------------------------------------------|--------------|-----------------|
| `GIT_REPO`                     | SSH-URL of the git repo                               | No           |                 |
| `GIT_REPO_NAME`                | Git repository name                                   | No           |                 |
| `GIT_BRANCH`                   | Revision/Branch of git repo                           | Yes          | none            |
| `GIT_PUSHER_DISPLAYNAME`       | Displayname of account, who pushed the Git repository | Yes          | OpenShift UI    |
| `GIT_PUSHER_NAME`              | Name of account, who pushed the Git repository        | Yes          | OpenShift UI    |
| `GIT_PUSHER_EMAIL`             | Email of account, who pushed the Git repository       | Yes          | OpenShift UI    |
| `GIT_COMMIT`                   | Commit, who pushed the Git repository                 | Yes          | OpenShift UI    |
| `CREDENTIAL_SCAN_SKIP_FAILURE` | Skip failure for credential scan                      | Yes          | true            |
| `SONAR_SECRET_NAME`            | Name of key/value secret containing sonar host/token  | Yes          | cop-sonar-token |
| `VERBOSE`                      | Debug mode                                            | Yes          | false           |

### Default Secrets

Those secrets will be created automatically - no app-team configuration needed.

| **Secret Name**           | **Description**                                                                 |
|---------------------------|---------------------------------------------------------------------------------|
| `dependencytrack-api-key` | Authentication of the pipeline to the DependencyTrack Service.                  |
| `sonar-token`             | Authentication of the pipeline to the SonarQube Service.                        |
| `rox-api-token`           | Authentication of the pipeline to the RedHat Advanced Cluster Security Service. |

### Secrets

Those secrets need to be provided by the application team. Use the `cicd-customer-pipelines` vault or define them in
your unmanaged vault.
Use the external secret store. To see more details about the external secret store and how to configure it, refer to
the [documentation](https://bitbucket.bit.admin.ch/projects/CNP/repos/commons-pipeline/browse/doc/commons-pipeline.md).

| **Secret Name**                   | **Description**                                                                                                                                   | **Vault Key**                        | **Default Property**                 |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|--------------------------------------|
| `bitbucket-secret`                | SSH access for pulls and Git tag pushes on Bitbucket.                                                                                             | `bitbucket`                          | `ssh`                                |
| `bitbucket-accesstoken`           | HTTP access token for webhooks and notifications on Bitbucket.                                                                                    | `bitbucketAccessToken`               | `accesstoken`                        |
| `nexus-dockerconfig`              | Docker login configuration for access to Nexus registry.                                                                                          | `nexus`                              | `dockerconfig`                       |
| `nexus-user-pw`                   | Credentials (username/password) for Maven/NPM repositories.                                                                                       | `nexusUserPw`                        | `username_password`                  |
| `message-contract-service-secret` | Password for Message Contract Service (optional).                                                                                                 | `messageContractAuth`                | `secret`                             |
| `webhook-accesstoken`             | Token to trigger the build pipeline (optional, admin rights required).                                                                            | `tektonWebhookAccessToken`           | `tektonWebhookAccessToken`           |
| `webhook-deployment-accesstoken`  | Token to trigger the deployment pipeline (optional).                                                                                              | `tektonDeploymentWebhookAccessToken` | `tektonDeploymentWebhookAccessToken` |
| `maven-task-auth-secrets`         | The key/value pairs will be avialable as env variables in the maven task                                                                          | one per secret, named by its key     | n/a                                  |
| `maven-task-file-secrets`         | The key/value pairs are used as filename and content (with automatic base64 decoding). The files will be available in the maven tasks in /secrets | one per secret, named by its key     | n/a                                  |

## Tasks in pipeline

| **Task Name**           | **Description**                                                                      |
|-------------------------|--------------------------------------------------------------------------------------|
| `build-setup`           | Task to provide inputs for all the the following tasks.                              |
| `mail-failure`          | Task to send mails when pipeline failed.                                             |
| `maven`                 | Task to execute maven goals.                                                         |
| `maven-ksc`             | Task to execute maven goals with a kubedock sidecar to execute Testcontainers tests. |
| `notify-failure`        | Task to fail pipeline if at least one task failed.                                   |
| `udeploy-push`          | Task to push application to udeploy.                                                 |
| `validate-quality-gate` | Task to validate Sonar quality gate.                                                 |
