{{- define "generic-application-pipeline.trigger-binding.template-api-version-kind-and-metadata" -}}
  {{- $ := .root -}}
  {{- $baseName := .baseName -}}
  {{- $repoType := .repoType -}}
  {{- $enablePullRequestMode := .enablePullRequestMode -}}
  {{- $specializedNameDescriptor := "" -}}

  {{- if $enablePullRequestMode -}}
    {{ $specializedNameDescriptor = "-pr" -}}
  {{- end }}

apiVersion: triggers.tekton.dev/v1beta1
kind: TriggerBinding
metadata:
  name: {{ $baseName }}-{{ $repoType }}{{ $specializedNameDescriptor }}-tb
  labels:
    {{- include "generic-application-pipeline.metadata.template-labels" $ | nindent 4 }}
  annotations:
    {{- include "generic-application-pipeline.metadata.template-annotations" $ | nindent 4 }}
{{- end -}}