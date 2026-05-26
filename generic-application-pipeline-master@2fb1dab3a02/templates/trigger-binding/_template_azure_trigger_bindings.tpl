{{- define "generic-application-pipeline.trigger-binding.template-azure-trigger-bindings" -}}
{{- $ := .root -}}
---
{{- include "generic-application-pipeline.trigger-binding.template-api-version-kind-and-metadata" .}}
spec:
  params:
    # This corresponds to Azure's branch display name or tag name
    - name: git-revision
      value: $(body.branchName)
    - name: gitrepo-url
      value: $(extensions.repo_url_ssh)
    - name: git-repo-name
      value: $(body.repoName)
    - name: pusher-displayname
      value: $(body.displayName)
    - name: pusher-name
      value: $(body.displayName)
    - name: git-repo-slug
      value: $(body.repoName)
    - name: pusher-email
      value: $(body.email)
    - name: git-host-url
      # Note: cel's "url" tekton's "parseURL" are not available in current context, moved code to EventListener where it is available
      value: '$(extensions.git_host_url)'
    - name: git-project-name
      value: $(body.project)
    - name: git-from-hash
      value: $(body.commitId)
    - name: azure-devops-uri
      value: $(body.uri)
    - name: azurerepo-build-id
      value: $(body.buildId)
    - name: azurerepo-event-type
      value: $(body.eventType)

{{ end -}}