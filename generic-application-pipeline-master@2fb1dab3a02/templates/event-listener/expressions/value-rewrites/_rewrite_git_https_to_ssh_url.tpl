{{- define "generic-application-pipeline.event-listener.rewrite-git-https-to-ssh-url" -}}
  {{- /* Value rewrite template for eventlistener interceptors */ -}}
  {{- /* Takes the HTTPS Git URL from the webhook payload and rewrites it to an SSH URL */ -}}
  {{- $repoType := .repoType }}
  {{- $customIntermediatePath := "" -}}
  {{- $enablePullRequestMode := .enablePullRequestMode | default false -}}

  {{- if $enablePullRequestMode -}}
    {{- $customIntermediatePath := ".pullRequest.fromRef" -}}
  {{- end -}}

  {{- if eq $repoType "azure" }}
- expression: "body.uri.replace('https://', 'ssh://').replace(':22', '')"
  key: repo_url_ssh

  {{- else }}
- expression: "body{{ $customIntermediatePath }}.repository.links.clone.filter(x, x.name == 'ssh')[0].href"
  key: repo_url_ssh

  {{- end -}}
{{- end -}}