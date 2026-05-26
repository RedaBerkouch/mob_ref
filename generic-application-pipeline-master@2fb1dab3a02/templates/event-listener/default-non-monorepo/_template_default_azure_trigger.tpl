{{- define "generic-application-pipeline.event-listener.template-default-azure-trigger" -}}
  {{- $ := .root -}}
  {{- $baseName := include "generic-application-pipeline.releaseName" $ -}}
  {{- $repoName := .repoName -}}
  {{- $repoType := .repoType -}}
  {{- $appName := .appName -}}
  {{- $triggerBranchPattern := .triggerBranchPattern -}}
  {{- $pipelineSuffix := include "generic-application-pipeline.pipelineSuffix" $ -}}

  {{- $params := dict "appName" $appName "pipelineSuffix" $pipelineSuffix "baseName" $baseName "repoType" $repoType "repoName" $repoName "triggerBranchPattern" $triggerBranchPattern "appName" $appName }}

  {{- $matchesRepoName := include "generic-application-pipeline.event-listener.match-repository-name" $params -}}
  {{- $matchAppName := include "generic-application-pipeline.event-listener.azure.match-app-name" $params -}}

- name: {{ $appName }}-trigger
  interceptors:
    - params:
        - name: filter
          value: "{{ $matchesRepoName }} && {{ $matchAppName }}"

        - name: overlays
          value:
            {{- include "generic-application-pipeline.event-listener.rewrite-git-https-to-ssh-url" $params | indent 12 }}
            {{- include "generic-application-pipeline.event-listener.null-git-host-url-value" $params | indent 12 }}

      {{- include "generic-application-pipeline.event-listener.template-interceptor-reference" . | indent 4 }}

  {{- include "generic-application-pipeline.event-listener.template-trigger-bindings" $params -}}
  {{- include "generic-application-pipeline.event-listener.template-trigger-template" $params }}
{{ end }}