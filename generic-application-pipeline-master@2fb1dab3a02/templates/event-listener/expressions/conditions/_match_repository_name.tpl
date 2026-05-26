{{- define "generic-application-pipeline.event-listener.match-repository-name" -}}
  {{- /* Condition template for eventlistener interceptors */ -}}
  {{- /* Matches if the repository name of $repoName equals to the repo name of the webhook */ -}}

  {{- $repoName := .repoName -}}
  {{- $customIntermediatePath := "" -}}
  {{- $repoType := .repoType -}}
  {{- $repoNameProperty := "" -}}
  {{- $enablePullRequestMode := .enablePullRequestMode -}}

  {{- if $enablePullRequestMode -}}
    {{- $customIntermediatePath = ".pullRequest.fromRef" -}}
  {{- end -}}

  {{- if eq $repoType "azure" -}}
    {{- $repoNameProperty := "body.repoName" -}}
    has({{- $repoNameProperty }}) && {{ $repoNameProperty -}}.lowerAscii() == '{{- $repoName -}}'.lowerAscii()
  {{- else -}}
    {{- $repoNameProperty := printf "body%s.repository" $customIntermediatePath -}}
    has({{- $repoNameProperty }}) && {{ $repoNameProperty -}}.name.lowerAscii() == '{{- $repoName -}}'.lowerAscii()
  {{- end -}}
{{- end -}}