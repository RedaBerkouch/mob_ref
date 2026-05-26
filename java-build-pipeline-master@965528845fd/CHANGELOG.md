# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.7.0] - 2026-04-02

### Changed
- move git push tag after maven deploy to avoid tagging failed releases

### Added

## [3.6.0] - 2026-02-25

### Added
- ability to push container images to an additional image registries. (e.g. Campus) provide registry domains as a space separated list to `.additionalImageRegistries` on global or app level.
- missing default for imageRegistryDomain.

## [3.5.0] - 2026-02-24
### Added
- support triggering deployment of multiple apps from one build. Add a list of apps under 'deployApps' to deploy instead of the image application name.

## [3.4.0] - 2026-02-23
### Added
- optional secrets mavenTaskAuthSecrets and mavenTaskFileSecrets. They are linked to the maven pod to be able to provide user/password or key file information is neeed for some tests.

## [3.3.4] - 2026-02-19

### Fixed
- The pusher-email parameter is now optional and no longer needed for triggering the pipeline via the event listener. 

## [3.3.3] - 2026-02-17

### Added
- E2E test for automated staging deployment of jme-rhos-cicd-example.

## [3.3.2] - 2026-02-17

### Added 
- Added E2E pipeline test base for automated testing of pipeline changes.

## [3.3.1] - 2026-02-04

### Changed
- Adjust external-secrets.io api version from v1beta1 to v1.

## [3.3.0] - 2026-01-22

### Added
- added global optional option `.enableWebhookCreation` that if set to false disable the Job that creates the webhooks in Bitbucket. Defaults to true.


## [3.2.3] - 2026-01-21

### Fixed
- Fetch all branches in checkout branch to enable the use of jeap-messaging-registry-maven-plugin and jeap-messaging-avro-maven-plugin

## [3.2.2] - 2026-01-20

### Fixed
- remove unused configMap branch-config from Trigger Template 
- use configMap instead of configmap in Trigger Template

## [3.2.1] - 2026-01-15

### Added
- Prevent job from adding another webhook to the repository when fetching the information about existing webhooks fails.

## [3.2.0] - 2026-01-06

### Added
- Added support for specifying a different Bitbucket project on app level by using `bitBucketProject`, for the application repository used in automatic webhook creation. Only required if it differs from the Bitbucket project of the GitOps repository.

## [3.1.3] - 2026-01-06

### Changed
- Updated documentation

## [3.1.2] - 2025-12-09

### Fixed
- Check out branch if it is defined. This is necessary for some plugins used within the pipeline (git-commit-id-plugin and then pact).

## [3.1.1] - 2025-12-05

### Fixed
- Fix CPU parameter to make ArgoCD happy

## [3.1.0] - 2025-11-25

### Added

- Update notification system to include git pusher email and enhance recipient handling.
- Application level overrides pipeline level. While pipeline level overrides global level.
- `notificationRecipients` on `global` level, for all pipelines
- `notificationRecipients` on `java-build-pipeline` level, for the verification pipeline notification and all applications.
- `notificationRecipients` on `java-build-pipeline.applications.<appName>` level, for the verification pipeline notification and a specific application.

### Changed

- Adapted default values for the resource profile `small`

## [3.0.0] - 2025-11-21

### Breaking changes

- `defaultMavenBuildJavaImage` changed from `toolchain-docker-hosted.nexus.bit.admin.ch/bit/ubi-java-build:java-17.0.8.1_node-18` to `defaultMavenBuildJavaImage: 'toolchain-docker-hosted.nexus.bit.admin.ch/bit/ubi-java-build:java-21.x_node-20.x`

- Migrate existing resource configurations from:
  - `resources.maven_build[- name: mvn-goals].resources.requests.cpu`      
  - `resources.maven_build[- name: mvn-goals].resources.requests.memory`   
  - `resources.maven_build[- name: mvn-goals].resources.limits.cpu`        
  - `resources.maven_build[- name: mvn-goals].resources.limits.memory`     
  - `resources.quality_check[- name: mvn-goals].resources.requests.cpu`    
  - `resources.quality_check[- name: mvn-goals].resources.requests.memory` 
  - `resources.quality_check[- name: mvn-goals].resources.limits.cpu`      
  - `resources.quality_check[- name: mvn-goals].resources.limits.memory`  

  To: 
  - `resources.maven.steps[- name: mvn-goals].resources.requests.cpu`
  - `resources.maven.steps[- name: mvn-goals].resources.requests.memory`
  - `resources.maven.steps[- name: mvn-goals].resources.limits.cpu`
  - `resources.maven.steps[- name: mvn-goals].resources.limits.memory`

### Added

Resource allocation for pipeline steps follows a priority-based fallback mechanism:

- Step-level resources
  If the step defines its own resources (CPU/memory requests and limits), these values are used.
  You can define resources directly for a step under:
  - `resources.<taskName>.steps[- name: <step-name>].resources.requests.cpu`
  - `resources.<taskName>.steps[- name: <step-name>].resources.requests.memory`
  - `resources.<taskName>.steps[- name: <step-name>].resources.limits.cpu`
  - `resources.<taskName>.steps[- name: <step-name>].resources.limits.memory`

- Task-level resources
  If the step does not define resources, the system checks the parent task for resources and applies them.
  - `resources.<taskName>.resources.requests.cpu`
  - `resources.<taskName>.resources.requests.memory`
  - `resources.<taskName>.resources.limits.cpu`
  - `resources.<taskName>.resources.limits.memory`

- Default resource profile
  If neither the step nor the task specifies resources, a predefined profile (e.g., small, medium, large) is applied. The profile is selected based on the defaultProfile parameter of the step or defaults to small.

Profiles are defined under `defaultResourceProfiles` and include standard CPU and memory requests/limits.

### Removed 

- Removed notify-failure tasks

## [2.1.2] - 2025-11-17

### Changed

- Fixed missing param type for ArgoCD sync. 

## [2.1.1] - 2025-11-11

### Added

- Added the option to configure hook scrips on application level after the setup, build, quality check and push tag steps. 

## [2.0.1] - 2025-09-16

### Changed
- added quote pipe to fix an issue where the pipeline would not deploy unless `overrideCdpReleaseName` is set.

## [2.0.0] - 2025-08-25

### Changed

**Breaking change:**
- Only use `SecretStore`. `ClusterSecretStore` is no longer supported.
- Default secret paths have been updated.

### Migration Notes

If you had one of these secrets retrieved from `bit-cicd-vault-global`, they now need to be updated.  
Use `.Values.global.secrets.externalStore` instead of `.Values.global.secrets.storeName` to retrieve secrets from the namespace secret store `cicd-customer-pipelines`, rather than from the deprecated cluster-wide secret Store `bit-cicd-vault-global`.

| Old secret path                 | New secret path                                                 |
|---------------------------------|-----------------------------------------------------------------|
| `kv/data/cicd/common/bitbucket` | `kv/data/cicd/shared-between-cicd-mgmt-and-customers/bitbucket` |
| `kv/data/cicd/common/nexus`     | `kv/data/cicd/shared-between-cicd-mgmt-and-customers/nexus`     |
 
## [1.2.5] - 2025-08-18
### Changed
- add possibility configure storage on application level

## [1.2.4] - 2025-08-13
### Changed
- adapted various information to meet guidelines and requirements after review

## [1.2.3] - 2025-07-28
### Changed
- correct pipeline annotation

## [1.2.2] - 2025-07-23
### Changed
- adapted various information to meet guidelines and requirements

## [1.2.1] - 2025-07-10
### Fixed
- add lock on webhook creation to prevent creation of more than one webhook on a bitbucket repository

## [1.2.0] - 2025-06-05
### Added
- Option to overwrite the mavenBuildGoal (TOOLCHNTKT-8126)

## [1.1.0] - 2025-05-26
### Changed
- Trigger deployment pipeline is now a shared task.

## [1.0.0] - 2025-05-22
### Added
- Added templates for java build pipeline.

