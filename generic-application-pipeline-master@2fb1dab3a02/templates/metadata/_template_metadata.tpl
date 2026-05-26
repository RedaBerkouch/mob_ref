{{- define "generic-application-pipeline.metadata.template-metadata" }}
{{- $defaultName := include "generic-application-pipeline.releaseName" $ }}
{{- $name := .Values.customName | default $defaultName }}
{{- $dontUseAnySuffix := "" }}
{{- $suffix := (hasKey .Values "customSuffix") | ternary (printf "-%s" .Values.customSuffix) $dontUseAnySuffix }}
metadata:
  name: {{ $name }}{{ $suffix }}
  labels:
    {{- include "generic-application-pipeline.metadata.template-labels" $ | nindent 4 }}
  annotations:
    {{- include "generic-application-pipeline.metadata.template-annotations" $ | nindent 4 }}
{{- end }}