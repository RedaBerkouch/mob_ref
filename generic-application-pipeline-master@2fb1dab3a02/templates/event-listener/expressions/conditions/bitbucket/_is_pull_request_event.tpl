{{- define "generic-application-pipeline.event-listener.bitbucket.is-pull-request-event" -}}
  {{- /* Condition template for eventlistener interceptors */ -}}
  {{- /* Only relevant for Bitbuckets: Matches if the event was triggered by a pull request */ -}}
  {{- $invertCondition := .invertCondition | default false  -}}

  {{- if $invertCondition -}}
  !(body.eventKey in ['pr:opened', 'pr:merged', 'pr:from_ref_updated'])
  {{- else -}}
  body.eventKey in ['pr:opened', 'pr:merged', 'pr:from_ref_updated']
  {{- end -}}
{{- end -}}