{{- define "generic-application-pipeline.event-listener.bitbucket.get-ssh-url-from-webhook" -}}
  {{- /* Value rewrite template for eventlistener interceptors */ -}}
  {{- /* Only relevant for Bitbucket: Takes the HTTPS Git URL from the webhook payload and rewrites it to an SSH URL */ -}}
  {{- $customIntermediatePath := "" -}}
  {{- $enablePullRequestMode := .enablePullRequestMode -}}

  {{- if $enablePullRequestMode -}}
    {{- $customIntermediatePath = ".pullRequest.fromRef" -}}
  {{- end }}

- expression: "body{{ $customIntermediatePath }}.repository.links.clone.filter(x, x.name == 'ssh')[0].href"
  key: repo_url_ssh
{{- end -}}