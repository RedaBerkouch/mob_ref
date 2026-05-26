{{- define "generic-application-pipeline.event-listener.azure.match-app-name" -}}
  {{- /* Condition template for eventlistener interceptors */ -}}
  {{- /* Only relevant for Azure DevOps: Matches if the app name of $appName equals to the app name of the webhook */ -}}

  {{- $appName := .appName -}}
  {{- $appNameProperty := "body.appName" -}}

  has({{ $appNameProperty }}) && {{ $appNameProperty }} == '{{ $appName }}'
{{- end -}}