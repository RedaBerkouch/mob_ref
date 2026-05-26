{{- define "generic-application-pipeline.event-listener.template-trigger-template" -}}
  {{- $isMonorepo := .isMonorepo -}}
  {{- $baseName := .baseName -}}
  {{- $repoType := .repoType -}}
  {{- $appName := .appName -}}
  {{- $pipelineSuffix := .pipelineSuffix }}

  template:
  {{- if $isMonorepo }}
    ref: {{ $baseName }}-monorepo-tt
  {{- else }}
    ref: {{ $appName }}-{{ $pipelineSuffix }}-tt
  {{- end -}}
{{- end -}}