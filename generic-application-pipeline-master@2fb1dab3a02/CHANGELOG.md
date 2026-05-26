# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [9.1.0] - 2026-05-20

### Added
- STI-3873 Introduced new optional task `malware-scan` to perform a malware-scan on your built image.

## [9.0.2] - 2026-05-06

### Fixed
- STI-3997 Pipeline related labels are now always passed down to PipelineRun in monorepo mode

## [9.0.1] - 2026-04-28

### Fixed
- STI-3990 Fixed secret ref in template-gap-run.yaml

### Removed
- STI-3990 Removed default value for defaultBranchTaskConfiguration.<branch>.deployStage

## [9.0.0] - 2026-04-23

### Added 
- STI-3636 Introduced new task `trigger-deployment-pipeline` for triggering deployment-pipeline.

### Changed
- **Breaking** STI-3636 Harmonized GAP with commons and cd-pipeline.
- **Breaking** STI-3636 GAP no longer handles the deployment process. This is now handled by the cd-pipeline.
- **Breaking** STI-3636 Replaced `updateGitops`, `waitForDeploymentRollout`, `triggerVerificationPipeline` and `zapTest` with `deployStage` and `verifyDeployment`.
- **Breaking** STI-3636 Replaced `defaultPipelinePvcSize` / `pipelinePvcSize` with `defaultPipelineStorage` / `pipelineStorage`.
- **Breaking** STI-3636 Changed rollout verification behavior. Verification is now handled by the cd-pipeline and based on `.appName` and `targetDeploymentName`. Support for `targetDeploymentName` in GAP was removed, as rollout verification is no longer performed by GAP.

### Removed
- **Breaking** STI-3636 Removed `update-gitops-repo` and `wait-for-rollout` tasks because the deployment is now handled by the cd-pipeline.
- **Breaking** STI-3636 Removed secret configuration from GAP values. Secrets must now be defined in the GitOps project using `global.secrets.externalStore.*`.
- **Breaking** STI-3636 Removed verification pipeline configuration from GAP values.
- **Breaking** STI-3636 Removed obsolete GitOps and rollout configuration from GAP values.

See [README](https://bitbucket.bit.admin.ch/projects/CNP/repos/generic-application-pipeline/browse/README.md) section "Migration to GAP version 9.0.0" in for detailed migration instructions.

## [8.0.1] - 2026-01-20

### Changed

- STI-3638 Replaces external-secrets.io/v1beta1 with v1 in ExternalSecrets and SecretStores

## [8.0.0] - 2025-12-29

### Changed

- **Breaking** STI-3609 Upgrade ACS task to latest v4, task output format changed from "json" to "table"

## [7.0.0] - 2025-11-07

### Added

- STI-3481 Introduced branch-based task configuration to enable/disable tasks based on the branch.

### Changed

- STI-3481 Renamed defaultPipelineTasks → defaultPipelineTasksConfig.
- STI-3481 Task enable flags were moved to the new defaultBranchTaskConfiguration.
- **Breaking** STI-3481 Applications using task flags under defaultVerificationPipeline or
    defaultPipelineTasks must migrate to the new branch-based configuration model. 
- STI-3481 defaultVerificationPipeline and defaultPipelineTasks now only hold task settings (e.g., ZAP command, file names, API URLs) and no longer enable or disable tasks.


## [6.4.0] - 2025-10-07

### Added
- STI-3480 Support for `buildImageContextPath` in standard (non-monorepo) GAP
- STI-3480 Allows multiple containerfiles or subdir builds in single-repo setups

### Changed
- STI-3495 Upgrade notify-failure task to v3

## [6.3.2] - 2025-10-06

### Fixed
- STI-3199 Corrected small typo which prevented push events on pull requests from being processed

## [6.3.1] - 2025-09-17

### Fixed
- STI-3199 Add checking if intermediate object exists as this was causing errors during the web hook evaluation

## [6.3.0] - 2025-09-16

### Added
- STI-3199 Add support for triggering the pipeline based on PR events
- STI-3262 Remove branch filter for Azure DevOps repositories

### Changed
- STI-3292 Rearranged `credentials-scan` task for execution speedup

## [6.2.0] - 2025-08-25

### Added
- STI-3210 capability added to provide the optional dependencyTrackParentName and dependencyTrackParentVersion to upload the sca scan results, if sca-check is activated

## [6.1.1] - 2025-08-21

### Fixed 
- STI-3291 Monorepo Pipeline Runs now also support Breaking Change. This fixes failing `acs-image-scan` steps due to missing secrets in monorepo pipelineruns

## [6.1.0] - 2025-08-18

### Added
- STI-3129 Option added to use the pipeline PVC instead of the default emptyDir storage to run the build-image task. This is usually slower but allows to configure the `pipelinePvcSize`.
  - Activate through `usePipelinePvcForBuild` 
  - Supported by pipeline PVC with accessMode readWriteOnce

## [6.0.0] - 2025-07-29

### Changed

- **Breaking** STI-3087: Refactored CI/CD Vault access from ClusterSecretStore to namespaced SecretStores and adapted paths due to secret reorganization. After the upgrade to this version, using "rerun" on existing pipeline run will fail. A new pipeline run has to be triggered (usually achieved with a bitbucket webhook).
- STI-3227: Align documentation according the guidelines

## [5.5.0] - 2025-07-02

### Added
- STI-3042 capability to run an SCA check and upload the SBOM to a custom DependencyTrack instance. 
- Activate through `pipelineTasks.runScaCheck` and configure 
  - the externalSecret `secrets.externalStore.paths.dependencyTrackApiKey`
  - the url to the apiserver `pipelineTasks.dependencyTrackApiUrl`

## [5.4.1] - 2025-06-10

### Changed
- STI-3112: Increase hardcoded timeout for download-files to 30min

## [5.4.0] - 2025-04-01

### Added
- STI-2771: capability to enforce credential-scan to fail in case of a finding. Overridable through `defaultCredentialScanAllowsSkipOnFailure` as well as per application with `credentialScanAllowsSkipOnFailure`.

## [5.3.0] - 2025-03-11

### Added
- STI-2781: capability to replace non-OCI chars in generated image version, replacement string is overridable through `defaultBuildVersionReplacementString` as well as per application with `buildVersionReplacementString`.

## [5.2.0] - 2025-02-27

### Added
- STI-2468: capability to override default `.trivyignore` expected location (`defaultTrivyIgnorePath`) as well as per application with `trivyIgnorePath`.
- STI-2708: capability to disable ArgoCD the PreSync hooks to delete pvc through `defaultArgoCDPreSyncHookDeletePVC` key, in order to improve ArgoCD sync experience on long run.

### Fixed
- STI-2706: documentation to mention `buildImageContextPath` and `defaultBuildImageContextPath` are only available in monorepo mode.
- STI-2706: when in monorepo mode, prevent pipeline to crash when evaluating diffs with deleted files.

## [5.1.0] - 2025-01-29

### Added
- STI-2642: Add option in order to allow the configuration of container image build context paths using `buildImageContextPath`, only available in monorepo mode.

## [5.0.4] - 2025-01-07

### Removed
- the task 'sca-check' from the Pipeline. Scanning images with Dependency Track is no longer required. See https://confluence.bit.admin.ch/x/n5qGMg

## [5.0.3] - 2024-12-25

### Changed
- STI-2606: Change to storage class iscsi-ssd-encrypt (rwo) to prevent Metro cluster limitations. To ease migration to the new storage class a presync hook will delete the old PVC.

## [5.0.2] - 2024-11-25

### Fixed
- STI-2556: Git commiter name and email address could no longer be parametrized after 5.0.0 release

## [5.0.1] - 2024-11-25

### Fixed
- STI-2554: Git resolver task bit-notify-azure-devops pointing to a branch revision instead of a Git tag

## [5.0.0] - 2024-11-21

### Changed
- **Breaking** STI-2245: Use updated OWASP ZAP task as part of the verification pipeline. The user now is required to provide the complete ZAP command in `applications.verificationPipeline.tests.zap.command` to the pipeline. This allows more flexibility on user side to customize ZAP's behaviour. With this change any existing user configuration in the value file for `applications.verificationPipeline.tests.zap.scans[]` can therefore be also removed.
- **Breaking** STI-2288: Removed `secrets.externalStore.name` from default values file. Users can no longer customize the name of the ExternalSecretStore resource. The ExternalSecretStore name will be created as `RELEASE_NAME-gp-secretstore`. Users who previously customized `secrets.externalStore.name` may need to update their configurations by removing any references in `secrets.externalStore.paths.<secret-name>.vault` to the overridden value from the earlier `secrets.externalStore.name` setting. However, users can still configure a dedicated ExternalSecretStore if needed.

### Added 
- STI-2288: capability to override secret property name (`remoteRef.property` in Secrets resources in Kubernetes context), used to address specific "Key" inside corresponding KV Secrets in Vault context. Keys `secrets.externalStore.paths.bitbucketAccessToken.property`, `secrets.externalStore.paths.bitbucketSSH.property`, `secrets.externalStore.paths.dockerConfigJSON.property` and `secrets.externalStore.paths.netrc.property` in values file can now be used for desired customization, it was previously hard-coded, default values are previously hard-coded values.
- STI-2323: ACS scan results to Vulnerability column in GUI
- STI-2315: capability to override each task's computeResources in pipelineRun context triggered through EventListener. Available on application level (`taskComputeResources` key) and globally (`defaultTaskComputeResources` key), see corresponding keys in [README.md](README.md).
- STI-2335: capability to be triggered from and notify an Azure DevOps build pipeline with [bit-notify-azure-devops](https://bitbucket.bit.admin.ch/projects/CNT/repos/bit-notify-azure-devops/browse)BIT task, required setup documented in [bit-notify-azure-devops - required Azure Devops setup](https://confluence.bit.admin.ch/x/t12ZLg)

## [4.3.2] - 2025-01-07

### Changed
- STI-2606 Change to storage class iscsi-ssd-encrypt (rwo) to prevent Metro cluster limitations. To ease migration to the new storage class a presync hook will delete the old PVC.

## [4.3.1] - 2024-12-12

### Changed
- STI-2606 Change to storage class iscsi-ssd-encrypt (rwo) to prevent Metro cluster limitations. To ease migration to the new storage class a presync hook will delete the old PVC.

## [4.3.0] - 2024-10-25

### Changed
- Previously with `defaultPipelineTasks.updateGitops` both Pipeline tasks `update-gitops-repo` and `wait-for-deployment-rollout` were enabled/disabled. Disabling the `wait-for-deployment-rollout` has now been moved to a separate key `defaultPipelineTasks.waitForDeploymentRollout` or on application config level `applications[0].pipelineTasks.waitForDeploymentRollout` to offer more fine-grained control over the tasks' execution.

## [4.2.0] - 2024-09-13

### Added
- You can now change the committer name and email address used in the `update-gitops-repo` step by specifying `defaultGitCommitterEmail` and `defaultGitCommitterName` through the value file

## [4.1.2] - 2024-08-19

### Fixed
- STI-2291: notify-bitbucket is not dc dependant, missing `bitbucket-host` parameter

## [4.1.1] - 2024-08-19

### Fixed
- STI-2261: Adapted task using git resolvers `revision` parameter to tag specific format `refs/tags/xxx` to prevent a commitID starting with given value to be addressed

## [4.1.0] - 2024-08-13

### Added
- STI-2154: Optional capability to be hosted on Campus, see chapter "Hosting on Campus datacenter" in README.md
- STI-2123: Add customer labels support with value `customLabels`. The keys `pipeline.bit.admin.ch/*` are blocked.
- STI-2270: Add possibility to overwrite the default values for the EventListener Container

### Fixed
- Default value for `secrets.externalStore.server` was missing scheme
- STI-2218: Container `FROM scratch` do not fail during the setup tasks
- STI-2213: Disabled `defaultPipelineTasks.updateGitops` or `applications[0].pipelineTasks.updateGitops` now do not require `applications[0].gitOpsRepo` anymore
- STI-2228: Using `<BRANCH_NAME>` in `defaultBuildVersionTemplate` or `buildVersionTemplate` the commit hash is returned instead of the branch display name

### Changed
- STI-2270: New default values for the EventListener Container
  - ```yaml
    resources:  
       limits:
        memory: 128Mi
        cpu: 25m
       requests:
        memory: 64Mi
        cpu: 1m
    ```

## [4.0.5] - 2024-07-15

### Fixed
- STI-2201: ACS rox-image-scan step fails if no found vulnerabilities are returned. Updated ACS git resolver task to 3.0.1

## [4.0.4] - 2024-07-05

### Removed
- ArgoCD PreSync Job to clean up old RWO PVCs present in GAP v2 prior to switch to RWM PVCs in GAP v3. As consequence, any GAP v2 user upgrading directly to GAP v4 will have to clean that up manually in ArgoCD by removing `metadata.finalizers` in the concerned PVC

## [4.0.3] - 2024-06-26

### Fixed
- `bitbucketAccessToken` Secret's SecretStore reference (`bitbucketAccessToken.vault`) was ignored and `netrc.vault` was wrongly used instead. `bitbucketAccessToken.vault` is now used correctly in corresponding secret.

## [4.0.2] - 2024-06-20

### Fixed
- Webhook validation error in triggers.tekton.dev

## [4.0.1] - 2024-06-20

### Fixed
- Chart templating issue if no additional value file is provided and no `.applications.appName` is given

## [4.0.0] - 2024-06-10

### Changed
- **Breaking** Secret `secrets.paths.bitbucketAccessToken` is not anymore provided by default from `bit-cicd-vault-global` vault and must be explicitly provided in value file with key `secrets.externalStore.paths.bitbucketAccessToken`, see corresponding entry in [README.md](README.md). As `defaultPipelineTasks.buildNotifications` is active by default such token must be created in Bitbucket (see [Using HTTP access tokens](https://confluence.atlassian.com/bitbucketserver0721/http-access-tokens-1115665626.html?utm_campaign=in-app-help&utm_medium=in-app-help&utm_source=stash#HTTPaccesstokens-UsingHTTPaccesstokens)) and present in vault for the pipeline to go on working properly.
- **Breaking** The option to specify `branchConfigs` to trigger PipelineRuns has been removed in favour of `defaultTriggerBranchPattern` and `triggerBranchPattern` respectively. You can now specify a regex expression for which branches a build should be triggered. Please see README.md for details.
- Migrated pipeline-api from `tekton.dev/v1beta1` to stable `tekton.dev/v1`
- Migrated from ClusterTask to task resolved with Git resolvers on CNT Project
- Reduced default `refreshInterval` on `ExternalSecret` resources to `5m0s`

### Added
- Capability to enable monorepo mode for the pipeline, for details see README.md

### Fixed
- STI-2073 EventListener checks Git repository name in a case-insensitive manner

## [3.1.0] - 2024-05-29

### Added
- Capability to specify additional image registries to push resulting image to for bit-skopeo-copy task, with `defaultAdditionalImageRegistries` root key and with application specific `additionalImageRegistries` key in `applications` array, `defaultAdditionalImageRegistries` is used as default when `additionalImageRegistries` is omitted.

### Changed
- Updated bit-skopeo-copy task to version 3-0

## [3.0.1] - 2024-05-22

### Fixed
- STI-2069: Generated event listener route name might still be to long for DNS resource name length restriction

## [3.0.0] - 2024-02-22

### Added
- Support pinned digests https://docs.renovatebot.com/docker/#digest-pinning
- `bit-notify-failure-1-1` ClusterTask, as `finally` step to send notification in case at least one task fails in the pipeline run
  - feature might be configured in values file using `defaultFailureNotificationSender`, `defaultFailureNotificationRecipients`, `defaultFailureNotificationServerHost` and `defaultFailureNotificationServerPort`
- Add support to build images in docker format. The default format is oci. You can overwrite the default behaviour with `defaultBuildImageFormat` or on an application level with `buildImageFormat`
- Add capability to execute a custom script in the `download-files` task and to configure the used file name. Therefore, `downloadFilesFileName` and `downloadFilesExecuteCustomScript` have been added to the values.
- Add capability to wait for `cronjob` kind resource (instead of `deployment`, by default). Therefore, `defaultTargetWorkloadResourceKind` and `targetWorkloadResourceKind` have been added to the values.

### Changed
- **Breaking** the download-files task is now disabled by default. If you still require this task enable it by setting `defaultPipelineTasks.downloadFiles: true` or on application level by `applications[0].pipelineTasks.downloadFiles: true`
- **Breaking** creation of the netrc secret is now disabled by default. If you still require the netrc secret for the download-files task, you can enable it by setting `secrets.externalStore.paths.netrc.create: true`
- Updated git-clone task to version 1-12-0 and reduced log output
- Parallelize `sca-check`(DependencyTrack), `scan-image`(ACS) and `prescan-image`(Trivy) tasks
- Upgraded to `bit-trivy-scan-and-check-3-0` ClusterTask, task does not anymore persist its database for further usage by `sca-check`(DependencyTrack)
- Upgraded to `bit-dependency-scan-3-0` ClusterTask, command changed from `rootfs` to `image`, it does not require anymore Trivy database to be present
- Upgraded to `bit-aggregate-logs-2-5` ClusterTask, including support for PVC in ReadWriteMany access mode with `securityContext.runAsUser: 65532` and fixed implementation for `taskRuns` not anymore available
- Upgraded to `buildah-2-4` ClusterTask, including support for PVC in ReadWriteMany access mode with `securityContext.runAsUser: 65532`, moved logging part to additional dedicated step
- Upgraded to `bit-cleanup-workspace-2-3` ClusterTask, including support for PVC in ReadWriteMany access mode with `securityContext.runAsUser: 65532`
- Upgraded to `bit-trufflehog-scan-2-3` ClusterTask, including support for PVC in ReadWriteMany access mode with `securityContext.runAsUser: 65532`
- With this release GAP will use RWM instead of RWO PVCs. This will prevent tasks mounting the same PVC to be delayed in execution and will globally improve pipeline performance. The old RWO PVCs will be deleted by ArgoCD during synchronization.
- Updated `download-files` Task, including support for PVC in ReadWriteMany access mode with `securityContext.runAsUser: 65532`
- Updated `update-gitops-repo` Task, including support for PVC in ReadWriteMany access mode with `securityContext.runAsUser: 65532`
- STI-1869: The Tekton EventListener Route name will be truncated to stay within DNS naming specification
- STI-1869: The Tekton Tasks metadata.name will be truncated to stay within DNS naming specification

### Fixed
- Fails to sync with `applications[0].branchConfigs[0].triggerBuildPipeline` empty or set to false
- `applications[].verificationPipeline.tests.zap.enabled: false` prevents verification pipeline creation instead of disabling `zap-test` task, causing pipeline run to fail trying to call non-existing pipeline.

### Removed
- `nexusUserPw` from values and corresponding usages in chart (not anymore needed by `bit-trivy-scan-and-check-3-0` ClusterTask)

## [2.0.1] - 2024-01-31

### Fixed
- Timeout in `setup-image-labels` when last image reference in a multi-stage Containerfile used alias instead of a full image reference

## [2.0.0] - 2023-12-29

### Changed
- **Breaking** STI-1617/AMBOSSCICD-921 Follow naming convention from https://confluence.bit.admin.ch/x/4jp8IQ
- - Eventlistener name is changing from el-{{ .Release.Name }}-el to el-{{ include "generic-application-pipeline.releaseName" . }}-el
- - Example from `https://sti-gap-example-test-el-*` to `https://sti-gap-example-test-gp-el-*`

### Fixed
- Small improvement in the test

## [1.1.2] - 2023-11-25

### Fixed
- STI-1649: ArgoCD remained in unknown state and could not sync new resources because of empty labels, also see https://github.com/argoproj/argo-cd/issues/15025

## [1.1.1] - 2023-11-13

### Changed
- Updated `wait-for-deployment-rollout` ClusterTask to version 2-2

### Fixed
- STI-1620: The `wait-for-deployment-rollout` step expected the deployment to use image digest in the '.spec.template.containers.image' key. The fix now allows using digest, tag or pinned digest format for the image reference.

## [1.1.0] - 2023-11-08

### Added
- Capability to override the default name of the deployment which is queried in `wait-for-deployment-rollout` step with `targetDeploymentName`
- STI-1595: Annotations to Pipeline resource to identify maintainer

### Changed
- `sca-check` will no longer create a new version of the project and now always uses `latest` as version identifier

## [1.0.0] - 2023-10-26

### Added
- Capability to specify proxy for buildah task, with `defaultHttpProxy` root key and with application specific `httpProxy` key in `applications` array, `defaultHttpProxy` is used as default when `httpProxy` is omitted.
- Documentation for `bit-wait-for-deployment-rollout` and RoleBinding in README.md
- Capability to skip the download-files, update-gitops-repo and notify-bitbucket steps in the pipeline through value file configuration. Please see the [Pipeline usage in README.md](README.md) for details
- Name and path of the Dockerfile is now configurable through value file configuration. Please see the [Pipeline usage in README.md](README.md) for details
- The update-gitops-repo step will write image digest and image tag to the applications helm chart value file
- Capability to specify custom YAML path to update specific node in the deployment helm chart value file by specifying `helmImageNameKey`, `helmImageDigestKey`, `helmImageTagKey`
- Capability to generate image tags. Please see the [Pipeline usage in README.md](README.md) for details
- Capability to skip adding FOITT labels to a produced image
- Capability to add additional tags to an image

### Changed
- Various hardcoded default values have been moved to Helm values
- Verification PipelineRun and ZAP tests are disabled by default
- Use buildah-2-2 as ClusterTask, removed buildah Tasks
- **Breaking:** Image name will no longer be produced by `departmentName` and `appName`. A new value `imageName` has been introduced to specify the name of the image without the registry URL. The key `departmentName` can be removed from the value files
- **Breaking:** The default reference key in the update-gitops-repo step has changed from `.app.name` to `.image.image`. Users will have to adopt their application helm chart accordingly or overwrite the reference keys by providing `helmImageNameKey`
- Updated prescan-image step to  ClusterTask bit-trivy-scan-and-check-2-5 which allows the task to run in T-Shirt S sized CI/CD offering namespaces and generally reduces resource consumption
- Updated sca-check step to ClusterTask bit-dependency-scan-2-5 which allows the task to run in T-Shirt S sized CI/CD offering namespaces and generally reduces resource consumption

### Removed
- Property `bitBucketRepo` removed from required values. The value is now calculated

## [0.13.5] - 2023-08-24

### Added
- Added ability to override the generated resource name by providing the optional parameter `overrideReleaseName` in the value file. This might be necessary for exceptionally long project names where resource creation fails because of character length restriction of various identifiers and names in Kubernetes.

## [0.13.4] - 2023-08-24

### Changed
- Changed tekton.dev/v1alpha1 to use tekton.dev/v1beta1 API to comply with BIT OCP 4.12.26-12 upgrade

## [0.13.3] - 2023-08-17

### Changed
- bit-trivy-scan-and-check-2-3 now uses cluster task
- bit-dependency-scan-2-3 now uses cluster task
- bit-aggregate-logs-2-2 now uses cluster task
- Update ubi9-toolbox image in tasks

## [0.13.2] - 2023-08-03

### Changed
- rhacs-image-analyse-2-0 upgraded to rhacs-image-analyse-2-1
- bit-zap-scan-2-0 upgraded to bit-zap-scan-2-1
- bit-trivy-scan-and-check-2-3 does not use anymore netrc file to authenticate to Nexus for DB download, but use common kv/cicd/jeap/nexus secret

### Added
- triggerTemplate.defaultEmailAddress in values to allow test to be triggered within pipeline run
- task-trivy-scan-2-3.yaml(...-bit-trivy-scan-and-check-2-3-sti Task) as proposal for ClusterTask, based on task-trivy-scan-2-3.yaml(bit-trivy-scan-and-check-2-2 Task), which supports optional workspace for dockerconfig, fixed trivy-java-db.tar.gz filename usage, optional parameter NEXUS_CRED_SECRET_NAME for secret name customizable, defaulting to original value, optional parameter TRIVY_IGNORE_PATH to provide a file containing accepted CVEs, optional parameter TRIVY_IGNORE_UNFIXED to allow ignore of unfixed vulnerabilities (defaulting to true), optional parameter TRIVY_PERSIST_DB_PATH_IN_WS to specify path in 'source' ws where Trivy DB is copied for further usage.
- task-dependency-scan-2-3.yaml(...-bit-dependency-scan-2-3-sti Task) as proposal for ClusterTask, based on task-dependency-scan-2-2.yaml(bit-dependency-scan-2-2 Task), which has a customizable secret name for Dependency Track API Key (defaulting to original name), optional parameter TRIVY_COMMAND to specify Trivy command to use (defaulting to fs), optional parameter TRIVY_DB_PATH_IN_WS to specify Trivy DB path in 'ws' workspace, for use with TRIVY_COMMAND=rootfs (defaulting to '')
- task-buildah-2-1.yaml(...-buildah-2-1-sti) as proposal for ClusterTask, based on task-buildah-2-0.yaml(buildah-2-0 Task)
- task-aggregate-logs-2-2.yaml(...-bit-aggregate-logs-2-2-sti) as proposal for ClusterTask, based on task-aggregate-logs-2-1.yaml(bit-aggregate-logs-2-1 Task)
- Implemented automated tests

## [0.13.1] - 2023-07-04

### Fixed
- Labels `bit.image.documentation` and `bit.image.source` use HTTPS URL again instead of SSH URL

## [0.13.0] - 2023-06-30

### Changed
- Default size for PVCs increased to 5Gi
- Pipeline consistently uses digest instead of tags to perform quality gate checks against it
- Policy enforcement in `prescan-image` through Trivy. The pipeline will now fail if policy violations are found

### Added
- Verification pipeline run which performs OWASP ZAP test
- Possibility to provide .trivyignore file to exclude false positives from quality gate enforcement, see Trivy documentation for details
- Possibility to ignore unfixed policy violations for trivy quality gate enforcement by setting `trivyIgnoreUnfixed` in value file
- wait-for-deployment-rollout task to verify deployment status
- **Breaking:*** The following keys have to be provided
  - `targetNamespacePrefix` key in `.Values.applications`
  ~~- `buildtargetNamespaceStage` key in `.Values.applications`~~
  - `trivyIgnoreUnfixed` key in `.Values.applications`
  - `defaultZapTestImage` key in `.Values`
  - `zapTestImage` key in `.Values.applications`
  - `verificationPipeline` key in `.Values.applications` and related sub-keys. See README.md for details

### Removed
- `qualityGate` key in `.Values`

## [0.12.0] - 2023-06-22

### Added
- `applications` key in `.Values` to hold multiple applications
- `bitBucketRepo` key in `.Values.applications`, to address given application's GIT repository
- `customValuesPath` key in `.Values.applications`, to address given application's Value file, optional

### Changed
- **Breaking:** `sti-rhos-pipelines` renamed to `generic-application-pipeline`
- **Breaking:** Introduced multi applications support from Values file, existing keys (except `secrets` - that are common to all applications) have to be moved in a sub-key, from `.Values` to `.Values.applications`, see example in `./values.yaml`
- One PVC per application will be created
- One Pipeline per application will be created
- One TriggerTemplate per application will be created
- Created common to all application object names are changed from `.Values.appName` to `.Release.Name`
- In `scan-image` task, `PATH_TO_VALUES_YAML` parameter uses `customValuesPath` key from value and defaults to `{{ .Values.appName }}/helm/values.yaml`
- In `sca-check` task, `PROJECT_NAME` parameter is set to Pipeline parameter `IMAGE_NAME` instead of `{{ .Values.departmentName }}-{{ .Values.appName }}`
- In `prescan-image` task, reduced requested memory from 2Gi to 1Gi

### Deleted
- Unused `projectNamespacePrefix` key in `.Values`

## [0.11.0] - 2023-06-19

### Changed
- Updated all tasks to use 9.2 UBI base images
- Updated all cluster tasks to use version 2.0 or newer
- Secret for bitbucket-notify steps now uses HTTP access token instead of username + password. The secret is provided by the Cluster Secret Store now.
  - You can remove `bitbucket-user` and `bitbucket-password` from your vault instance if they are not otherwise used
- **Breaking:*** secret key reference format has been updated, see [values.yaml](values.yaml) for reference. Shared secrets provided by the Cluster Secret Store use path `secrets.paths.<name>` while project specific secrets use `secrets.externalStore.paths.<name>`. Update you value file for this chart accordingly.

## [0.10.0] - 2023-06-16

### Added
- STI-1232 - aggregate logging task 
- STI-1232 - Scan image with ACS task and corresponding secrets as workspaces
- STI-1232 - credential-scan task and corresponding secrets as workspaces
- STI-1232 - sca-check(bit-dependency-scan Task) Dependency Track task and corresponding ExternalSecret, workspace

### Change
- STI-1232 - prescan-image ClusterTask as Task, corresponding secrets as workspaces
- STI-1232 - buildah-build ClusterTask as Task
- STI-1232 - increased pvc default size to 2Gi
- STI-1232 - trigger-binding and trigger-template adapted accordingly
- STI-1232 - logging task - does not us workspace anymore

## [0.9.3] - 2023-05-23

### Fixed
- STI-1301 - netrc external secret is optional and take the correct value

### Changed
- STI-1301 - improve output during setup task

## [0.9.2] - 2023-05-16

### Changed
- Use buildah-1-8-0-1 as ClusterTask again

## [0.9.1] - 2023-05-15

### Fixed
- Authentication issues during image build and copy tasks
- Incorrect secret references in webhook config and pipeline definition
- EventListener using git repo HTTP URL instead if SSH URL. The EventListener now extracts SSH URL

### Changed
- Pipeline now pushes to internal Openshift registry instead of local filesystem for the initial image build
- Changed buildah task from ClusterTask `buildah-build-local` to pipeline task
- Require `dockerconfig` as pipeline parameter

## [0.9.0] - 2023-05-09

### Removed
- The pipeline task `wait-for-deployment-rollout` has been deleted from the pipeline

## [0.8.0] - 2023-05-09

### Added
- Download-Files task to the pipeline
- The user can now create and use ExternalSecretStore resources to create secrets necessary for the pipeline

### Changed
- Pipeline workspace `git-auth` renamed to `bitbucket-ssh-key`

### Removed
- Unused parameter `gitOpsProjectName` removed from pipeline configuration

## [0.7.4] - 2023-04-03

### Fixed
- Event listener route to service not correctly being created

## [0.7.2] - 2023-02-28

###
- Added labels with pipeline version and name

## [0.7.0] - 2023-02-23

### Changed
- **Breaking:** Pipeline now requires `imageRegistry` to be provided in the values file
- **Breaking:** Pipeline now requires `qualityGate` to be provided in the values file. This governs whether quality gate steps will make the pipeline fail
- Unused parameters in the setup task have been removed
- Reference to the value file containing the image digest to be deployed has been updated to conform with the new GitOps repository structure

## [publishChart.sh] - 2023-01-26
## Changed
- change repo to upload to Nexus PRO (nexus.bit.admin.ch)
- enhanced output
- verify, that chart not already exist 

## [0.6.0] - 2023-01-17

### Changed
- **Breaking:** buildah-build-local and skopeo-copy task requires secrets of type [kubernetes.io/dockerconfigjson](https://tekton.dev/vault/pipelines-v0.15.2/auth/#kubernetess-docker-registrys-secret) assigned to your pipeline service account for pulling and pushing secrets. Multiple secrets might be required. The previouly created secret `docker-repo-secret` can be deleted.
- Changed build, upload to registry, scan with trivy, git clone and wait for deployment tasks to ClusterTask kind
- Parameter `POLICY_CHECK` changed to `TRIVY_POLICY_CHECK`
- `TRIVY_POLICY_CHECK` defaults to `true`

### Fixed
- [STI-1040](https://jira.bit.admin.ch/browse/STI-1040) http_proxy variable not set in some pipelines tasks

### Removed
- Option to perform a full clean on the PVC with `FULL_CLEAN` parameter

## [0.5.8] - 2022-11-28

## Changed
- fixed `FULL_CLEAN` parameter description

## [0.5.7] - 2022-11-25

## Changed
- add flag `FULL_CLEAN`. If `true` will during cleanup delete all the files on the pvc

## [0.5.6] - 2022-11-25

## Changed
- set possibility to set the PVC size with `pipelinePvcSize`

## [0.5.5] - 2022-11-24

## Changed
- correct cleanup in task of all subfolder

## [0.5.4] - 2022-11-24

## Changed
- correct cleanup in task of all subfolder
- set POLICY_CHECK default to false

### Removed  
- removed tags for BIT_IMAGE_DESCRIPTION, BIT_IMAGE_TITLE, BIT_IMAGE_VENDOR
- removed setting appVersion in helm chart during updating gitops repo

## [0.5.3] - 2022-11-24

### Changed  
- bump trivy scanner to latest version 0.34.0 and print trivy version 
- cleanup task deletes all the data inside the volume

## [0.5.2] - 2022-11-23

### Changed  
- correct cleanup in task of all subfolder

## [0.5.1] - 2022-11-23

### Changed  
- **Breaking:** removed variable to declare: policyCheck
- add possibility to skip trivy check

### Manual changes for Version from 0.3.0 to 0.5.1
- change all the references from irs-pipelines to sti-rhos-pipelines (Chart.yaml + argocd-pipeline-application.yaml )
- bump the pipelineVersion if it's not in a range
- set variable departmentName in the values files
- remove variables tektonNameSpace, gitOpsRepoName in the values files
- change variable appName and remove the departement name from it (example: bak-fpf to fpf)
- change name of Chart in ArgoCD
- remove merge webhook in bitbucket
- replace in `argocd-applications.yaml` the variable `$appName` with `$projectNamespacePrefix` and remove it
- replace in `argocd-pipeline-application.yaml` the variable `{{ .Values.irsPipelines.tektonNameSpace }}` with `{{ .Values.irsPipelines.projectNamespacePrefix }}-pipelines`
- remove in `argocd-pipeline-application.yaml` in the the value `.spec.source.helm.values.tektonNameSpace` and `.spec.source.helm.values.gitOpsRepoName`
- replace in `argocd-applications.yaml` the variable `{{ $tektonNameSpace }}` with `{{ $projectNamespacePrefix }}-pipelines` and remove it
- add in `argocd-pipeline-application.yaml` the value `departmentName: {{ .Values.irsPipelines.departmentName }}`

## [0.5.0] - 2022-11-23

### Changed  
- **Breaking:** Remove merge pipeline and all references (images, eventlistener, tasks)
- **Breaking:** Renamed project/chart from irs-pipelines to sti-rhos-pipelines and updated Chart.yaml
- **Breaking:** Variables deprecated: tektonNameSpace, gitOpsRepoName
- **Breaking:** New Variables to declare: departmentName, policyCheck
- applies the new guidelines following https://confluence.bit.admin.ch/x/XROFG (v18)
    - uploads now in the Nexus Repository (repo.bit.admin.ch) and path changed 
    - new task setup, which defines all the variable
    - in the gitops repository the `.app.image` too is replaced
- adding _helpers.tpl for templating
    - fullAppName is been set together with `printf "%s-%s" .departmentName .appName`
    - gitOpsRepoName is been set together with `printf "%s-%s-gitops" .departmentName .appName`  
- updated values.yaml in test and small improvement for the tests
- added workaround for enabling tls verification during `git clone`

### Manual changes
- change all the references from irs-pipelines to sti-rhos-pipelines (Chart.yaml + argocd-pipeline-application.yaml )
- bump the pipelineVersion if it's not in a range
- set variable departmentName in the values files
- remove variables tektonNameSpace, gitOpsRepoName in the values files
- change variable appName and remove the departement name from it (example: bak-fpf to fpf)
- change name of Chart in ArgoCD
- remove merge webhook in bitbucket
- replace in `argocd-applications.yaml` the variable `$appName` with `$projectNamespacePrefix` and remove it
- replace in `argocd-pipeline-application.yaml` the variable `{{ .Values.irsPipelines.tektonNameSpace }}` with `{{ .Values.irsPipelines.projectNamespacePrefix }}-pipelines`
- remove in `argocd-pipeline-application.yaml` in the the value `.spec.source.helm.values.tektonNameSpace` and `.spec.source.helm.values.gitOpsRepoName`
- replace in `argocd-applications.yaml` the variable `{{ $tektonNameSpace }}` with `{{ $projectNamespacePrefix }}-pipelines` and remove it
- add in `argocd-pipeline-application.yaml` the value `departmentName: {{ .Values.irsPipelines.departmentName }}`

### Deprecated
- 0.3.0 is deprecated and should be upgraded to latest 0.5.0

## [0.3.0] - 2022-09-23

### Changed
- **Breaking:** Event listeners have been updated to trigger build pipeline on specific source code repository branches only. See README.md and define `branchConfigs`.
  You will also have to update your irs-gitops-template to correctly reflect the changes on the pipeline trough branchConfigs.

## [0.2.1] - 2022-09-08

### Fixed
- STI-790: Fix service reference in el routes

## [0.2.0] - 2022-09-08

### Changed
- **Breaking:** The pipeline event listener routes have been updated to reduce risk of not respecting dns RFC for Domain label names (63 chars). You will have to recreate the Bitbucket webhooks in your source code and GitOps repositories to respect the new naming of the event listener routes. See Readme how to get the URL of the routes for the webhooks.

## [0.2.0] - 2022-07-01

### Added
- init
