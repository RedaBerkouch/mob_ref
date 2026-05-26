{{- define "generic-application-pipeline.event-listener.bitbucket.doesnt-match-on-delete-events" -}}
  {{- /* Condition template for eventlistener interceptors */ -}}
  {{- /* Only relevant for Bitbucket: Matches NOT on delete events */ -}}

  body.changes[0].type != 'DELETE'
{{- end -}}