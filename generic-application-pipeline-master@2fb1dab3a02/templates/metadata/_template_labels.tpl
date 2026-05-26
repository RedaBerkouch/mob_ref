{{/*
Common labels and possible customer labels
*/}}
{{- define "generic-application-pipeline.metadata.template-labels" -}}
{{- $chart := .Chart -}}
pipeline.bit.admin.ch/name: {{ $chart.Name}}
pipeline.bit.admin.ch/version: {{ $chart.Version }}
pipeline.bit.admin.ch/pipeline-checksum: {{ toJson $.Chart | sha1sum }}
pipeline.bit.admin.ch/appname: {{ .appName | default "not-app-specific" }}
{{- if .customLabels }}
# custom labels
{{- $customLabels := .customLabels }}
{{- range $key, $value := $customLabels }}
{{- if not (hasPrefix "pipeline.bit.admin.ch" $key ) }}
{{ $key }}: {{ $value | quote }}
{{- end }}
{{- end }}
{{- end }}
{{- end -}}