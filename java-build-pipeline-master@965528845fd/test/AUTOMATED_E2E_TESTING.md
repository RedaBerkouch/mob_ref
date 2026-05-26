# Automated E2E Testing for Pipeline Quality Assurance

This process prevents faulty pipeline versions from impacting production deployments.

## Proposed Microservice Examples for Testing
To validate pipeline changes in a realistic manner, existing example microservices should be used.

## Implementation Considerations
New pipeline versions should be:

1. Automatically built
2. Written to a GitOps repository
3. Synchronized via ArgoCD
4. Validated by executing example build and deployment pipelines

### Key Component: tekton-pipelines-pipeline
The central component is the `tekton-pipelines-pipeline` in the `bit-bs-pipelines` namespace. This pipeline:

- Builds the Helm charts of the pipelines
- Can write the new version directly to a GitOps repository (`update-chart-dep-in-git`)
- Provides a step for ArgoCD synchronization (`argocd-sync`)
- Allows a custom test script to execute example pipelines (`run-tests`)

## Orchestration of Microservice Pipelines
The orchestrating test script triggers the relevant microservice pipelines.

### Monitoring Pipeline Runs
The orchestrating test script monitors the microservice pipeline runs by:

- Waiting for completion
- Evaluating results
- Validating expected behavior:
  - Happy Paths (successful build/deploy)
  - Non-Happy Paths (intentional validation failures, error handling, timeouts)

### Result Aggregation
Finally, the test script aggregates the results. Notifications on Bitbucket (PR checks) are set by the pipeline itself. Teams or email notifications are currently unavailable and would need to be implemented additionally.

### Test cases
- Build Success, Deploy Fail (RHOS Example)
- Build Success, Deploy Success, Verification Success, Automatically staged [dev -> ref -> prod] (RHOS CICD Example)
  
