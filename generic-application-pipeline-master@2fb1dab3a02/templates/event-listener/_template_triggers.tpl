{{- define "generic-application-pipeline.event-listener.template-triggers" -}}
{{- $parameters := dict "root" $ "applications" .Values.applications -}}

  triggers:
  {{- if .Values.enableMonorepo -}}
    {{- include "generic-application-pipeline.event-listener.template-monorepo-triggers" $parameters | nindent 2 -}}
  {{- else -}}
    {{- include "generic-application-pipeline.event-listener.template-default-triggers" $parameters | nindent 2 -}}
  {{- end -}}
{{- end -}}