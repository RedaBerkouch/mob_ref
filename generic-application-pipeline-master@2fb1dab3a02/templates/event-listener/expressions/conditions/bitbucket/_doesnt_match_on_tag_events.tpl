{{- define "generic-application-pipeline.event-listener.bitbucket.doesnt-match-on-tag-events" -}}
  {{- /* Condition template for eventlistener interceptors */ -}}
  {{- /* Only relevant for Bitbuckets: Matches if the event ref if NOT a tag */ -}}

  body.changes[0].ref.type != 'TAG'
{{- end -}}