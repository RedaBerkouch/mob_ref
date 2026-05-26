{{- define "generic-application-pipeline.event-listener.template-monorepo-triggers" -}}
  {{- $ := .root -}}
  {{- $baseName := include "generic-application-pipeline.releaseName" $ -}}
  {{- $applications := .applications -}}
  {{- $repoType := "azure" }}
  {{- $uniqueReposPerType := (include "generic-application-pipeline.event-listener.evaluate-unique-repos-per-type-by-applications" $applications | fromYaml) -}}
  {{- $triggerBranchPattern := $.Values.defaultTriggerBranchPattern -}}
  {{- $isMonorepo := true -}}

  {{- range $repoType, $typedUniqueRepos := $uniqueReposPerType -}}
    {{- range $index, $repoName := (keys $typedUniqueRepos) -}}
      {{- $params := dict "root" $ "repoName" $repoName "index" $index "repoType" $repoType "triggerBranchPattern" $triggerBranchPattern "isMonorepo" $isMonorepo -}}
      
      {{- if eq $repoType "azure" -}}
        {{- include "generic-application-pipeline.event-listener.template-monorepo-azure-trigger" $params }}
      {{ else -}}
        {{- include "generic-application-pipeline.event-listener.template-monorepo-bitbucket-trigger" $params }}
      {{ end }}
    {{ end -}}
  {{ end -}}
{{- end -}}
