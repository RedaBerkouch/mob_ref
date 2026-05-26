{{- define "generic-application-pipeline.event-listener.build-git-host-url-from-parameters" -}}
  {{- /* Value rewrite template for eventlistener interceptors */ -}}
  {{- /* Takes the HTTPS Git URL from the webhook payload and rewrites it to an SSH URL */ -}}

  {{- $repoType := .repoType -}}
  {{- $customIntermediatePath := "" -}}
  {{- $enablePullRequestMode := .enablePullRequestMode | default false -}}

  {{- if $enablePullRequestMode -}}
    {{- $customIntermediatePath = ".pullRequest.fromRef" -}}
  {{- end -}}

  {{- if eq $repoType "azure" }}
- expression: "[body.uri.parseURL().scheme, '://', body.uri.parseURL().host].join('')"
  key: git_host_url
  {{- else }}
- expression: "[body{{ $customIntermediatePath }}.repository.project.links.self[0].href.parseURL().scheme, '://', body{{ $customIntermediatePath }}.repository.project.links.self[0].href.parseURL().host].join('')"
  key: git_host_url
  {{- end -}}
{{- end -}}