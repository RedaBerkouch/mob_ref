{{- define "generic-application-pipeline.event-listener.template-trigger-bindings" -}}
  {{- $baseName := .baseName }}
  {{- $repoType := .repoType -}}
  {{- $specializedNameDescriptor := "" }}
  {{- $enablePullRequestMode := .enablePullRequestMode -}}

  {{- if $enablePullRequestMode -}}
    {{- $specializedNameDescriptor = "-pr"  }}
  {{- end }}

  bindings:
    - kind: TriggerBinding
      ref: {{ $baseName }}-{{ $repoType }}{{ $specializedNameDescriptor }}-tb
{{- end -}}