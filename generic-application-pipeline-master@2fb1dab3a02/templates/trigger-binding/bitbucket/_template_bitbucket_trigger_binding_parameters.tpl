{{- define "generic-application-pipeline.trigger-binding.template-bitbucket-trigger-binding-parameters" -}}
{{- $customIntermediatePath := "" -}}
{{- $fromHashPropertyPath := "$(body.changes[0].fromHash)" -}}
{{- $gitRevisionPropertyPath := "$(body.changes[0].ref.displayId)" -}}
{{- $enablePullRequestMode := .enablePullRequestMode -}}

{{- if $enablePullRequestMode -}}
  {{- $customIntermediatePath = ".pullRequest.fromRef" -}}

  {{- /* ATTENTION: The variable is named >>fromHash<<PropertyPath but in case of PRs we need to use the destination in order to be able to calculate a difference. Renaming the variable doesn't work as it is used across this whole repo. What to do now? */ -}}
  {{- $fromHashPropertyPath = "$(body.pullRequest.toRef.latestCommit)" -}}
  
  {{- $gitRevisionPropertyPath = "$(body.pullRequest.fromRef.displayId)" -}}
{{- end }}

params:
  # This corresponds to Bitbucket's branch display name or tag name
  - name: git-revision
    value: {{ $gitRevisionPropertyPath }}
  - name: gitrepo-url
    value: $(extensions.repo_url_ssh)
  - name: git-repo-name
    value: $(body{{$customIntermediatePath}}.repository.name)
  - name: pusher-name
    value: $(body.actor.name)
  - name: pusher-email
    value: $(body.actor.emailAddress)
  - name: pusher-displayname
    value: $(body.actor.displayName)
  - name: git-repo-slug
    value: $(body{{$customIntermediatePath}}.repository.slug)
  - name: git-host-url
    # Note: cel's "url" tekton's "parseURL" are not available in current context, moved code to EventListener where it is available
    value: '$(extensions.git_host_url)'
  - name: git-project-name
    value: $(body{{$customIntermediatePath}}.repository.project.key)
  - name: git-from-hash
    value: {{ $fromHashPropertyPath }}
{{ end -}}