{{- define "generic-application-pipeline.event-listener.template-default-triggers" -}}
  {{- $ := .root -}}
  {{- range $.Values.applications -}}
    {{- $appName := printf  "%s" (.appName | default "no-app-defined") -}}
    {{- $repoName := eq .repoType "azure" | ternary (last (regexSplit "_git\\/" .srcRepo -1)) (.srcRepo | regexFind "[^\\/]+\\.git$" | replace ".git" "") -}}
    {{- $triggerBranchPattern := .triggerBranchPattern | default $.Values.defaultTriggerBranchPattern -}}
    {{- $params := (dict "root" $ "repoName" $repoName "repoType" .repoType "triggerBranchPattern" $triggerBranchPattern "appName" $appName) }}

      {{- if eq .repoType "azure" -}}
        {{- include "generic-application-pipeline.event-listener.template-default-azure-trigger" $params -}}
      {{- else -}}
        {{- include "generic-application-pipeline.event-listener.template-default-bitbucket-trigger" $params -}}
      {{- end }}
  {{- end }}
{{- end }}
