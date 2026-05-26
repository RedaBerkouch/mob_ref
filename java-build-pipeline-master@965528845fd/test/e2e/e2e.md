# jEAP java-build-pipeline E2E Tests

End-to-end tests that validate the pipeline flow against a test environment. These tests are executed when a push 
to the pipeline repository is detected, this as part of the pipelines-pipelines. 


Each test creates a temporary feature branch, triggers the build pipeline via a commit, and asserts the expected 
downstream behavior. On completion (success or failure), the branch is cleaned up automatically.


The pipelines-pipelines writes the actual pipeline version to the GitOps 
repository [`jme-test-gitops`](https://bitbucket.bit.admin.ch/projects/JMERHOS/repos/jme-test-gitops/browse), which ArgoCD uses to 
reconcile the target namespaces on the cluster.

**Note**: At the moment, it is not guaranteed that ArgoCD sync has completed when the test starts. We are working on resolving this. 

## Test Cases

### Test 1 — Happy Path: Full Automated Staging (jme-rhos-cicd-example)

**Repository:** `bit_jme/jme-rhos-cicd-example`

**Description:**
The build pipeline completes successfully and triggers the deployment pipeline for DEV. After a successful DEV deployment, automated staging promotes the release to REF, and then to PROD. After each deployment the verification pipeline runs and must complete successfully.

**Flow:**
```
Build Pipeline
  └─► Deploy DEV  (triggered by build pipeline run)
        └─► Deploy REF  (automated staging: dev → ref)
              └─► Deploy PROD  (automated staging: ref → prod)
```

**Expected outcome:** All pipelines succeed. `jme-test-gitops` is updated at each stage by the CD pipeline.

---

### Test 2 — Happy Path: Pact Consumer Triggers Provider Verification (jme-rhos-cdct-consumer-example)

**Repository:** `JMERHOS/jme-rhos-cdct-consumer-example`

**Description:**
A change to the Pact consumer test (`TaskClientConsumerPactTest`) is committed and pushed. The build pipeline for the consumer completes successfully. This publishes a new pact to the Pact Broker, which triggers the provider verification pipeline for `jme-rhos-cdct-provider-service`. The provider verification pipeline must also complete successfully.

**Flow:**
```
Modify consumer Pact test
  └─► Build Pipeline (consumer)
        └─► Pact Verify Pipeline (provider: jme-rhos-cdct-provider-service)
```

**Expected outcome:** Both pipelines succeed. The pact contract between `bit-jme-rhos-cdct-consumer-service` and `bit-jme-rhos-cdct-provider-service` is verified on the Pact Broker.

---

### Test 3 — Failing Deployment: After-Deploy Hook Exits with Error (jme-rhos-example)

**Repository:** `BIT_JME/jme-rhos-example`

**Description:**
The `after-deploy.sh` script in the repository is intentionally overridden to `exit 1` before the build is triggered. The build pipeline must complete successfully, but the subsequent deployment pipeline on DEV must fail due to the error in the after-deploy hook.

**Flow:**
```
Override after-deploy.sh → exit 1
  └─► Build Pipeline  ✔ (expected to succeed)
        └─► Deploy Pipeline  ✘ (expected to fail at after-deploy task)
```

**Expected outcome:** Build pipeline succeeds, deploy pipeline fails. Test passes only if the deploy pipeline failure is confirmed.

---

## Infrastructure

| Component                  | Value                                          | Links                                                                                                                                                                                        |
|----------------------------|------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| GitOps repository          | `jmerhos/jme-test-gitops` (branch: `master`)   | [GitOps repo](https://bitbucket.bit.admin.ch/projects/JMERHOS/repos/jme-test-gitops/browse)                                                                                                  |
| Cluster (non-prod)         | `p-szb-ros-shrd-npr-01`                        |                                                                                                                                                                                              |
| Cluster (prod)             | `p-szb-ros-shrd-prd-01`                        |                                                                                                                                                                                              |
| Test pipeline namespace    | `bit-jme-test-pipelines-d`                     | [Test pipeline](https://console-openshift-console.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/pipelines/ns/bit-jme-test-pipelines-d/pipeline-runs?rowFilter-pipeline-data-source=cluster-data) |
| Test ArgoCD namespace      | `bit-jme-test-gitops-d`                        | [Test ArgoCD](https://argocd-server-bit-jme-test-gitops-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/)                                                                                        |
| Test application namespace | `bit-jme-test-d`                               | [Test application](https://console-openshift-console.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/k8s/ns/bit-jme-test-d/core~v1~Pod)                                                            |
| Pipelines Pipelines        | `bit-bs-pipelines-d`                           | [Pipelines pipelines](https://console-openshift-console.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/pipelines/ns/bit-bs-pipelines-d/pipeline-runs?rowFilter-pipeline-data-source=cluster-data) |
| Pipelines used             | Tekton (`cd-pipelines`, `java-build-pipeline`) |                                                                                                                                                                                              |
