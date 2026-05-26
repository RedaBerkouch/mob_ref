{{- define "generic-application-pipeline.event-listener.template-interceptor-reference" }}
  ref:
    kind: ClusterInterceptor
    name: cel
{{- end -}}