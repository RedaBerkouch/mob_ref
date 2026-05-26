{{- define "generic-application-pipeline.event-listener.null-git-host-url-value" -}}
  {{- /* Value rewrite template for eventlistener interceptors */ -}}
  {{- /* Sets the value for git_host_url to an empty string */}}
- expression: "''"
  key: git_host_url
{{- end -}}