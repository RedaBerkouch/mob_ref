{{- define "generic-application-pipeline.event-listener.template-monorepo-azure-trigger" }}
  {{- $ := .root -}}
  {{- $baseName :=  include "generic-application-pipeline.releaseName" $ -}}
  {{- $repoName := .repoName -}}
  {{- $index := .index -}}
  {{- $repoType := "azure" -}}
  {{- $triggerBranchPattern := .triggerBranchPattern -}}
  {{- $isMonorepo := .isMonorepo -}}

  {{- $params := dict "baseName" $baseName "repoType" $repoType "repoName" $repoName "triggerBranchPattern" $triggerBranchPattern "isMonorepo" $isMonorepo -}}

  {{- $matchesRepoName := include "generic-application-pipeline.event-listener.match-repository-name" $params -}}
  {{- /* Remove trimming for working output */}}

- name: {{ $baseName }}-trigger-monorepo-{{ $repoType }}-{{ $index }}
  interceptors:
    - params:
        - name: filter
          value: "{{ $matchesRepoName }}"

        - name: overlays
          value:
            {{- include "generic-application-pipeline.event-listener.rewrite-git-https-to-ssh-url" $params | indent 12 }}
            {{- include "generic-application-pipeline.event-listener.build-git-host-url-from-parameters" $params | indent 12 }}

      {{- include "generic-application-pipeline.event-listener.template-interceptor-reference" . | indent 4 }}

  {{- include "generic-application-pipeline.event-listener.template-trigger-bindings" $params -}}
  {{- include "generic-application-pipeline.event-listener.template-trigger-template" $params }}
{{- end }}