{{- define "generic-application-pipeline.event-listener.template-monorepo-bitbucket-trigger" }}
  {{- $ := .root -}}
  {{- $baseName :=  include "generic-application-pipeline.releaseName" $ -}}
  {{- $repoName := .repoName -}}
  {{- $index := .index -}}
  {{- $repoType := "bitbucket" -}}
  {{- $triggerBranchPattern := .triggerBranchPattern -}}
  {{- $isMonorepo := .isMonorepo }}

  {{- $params := dict "baseName" $baseName "repoType" $repoType "repoName" $repoName "triggerBranchPattern" $triggerBranchPattern "isMonorepo" $isMonorepo -}}

  {{- $matchesRepoName := include "generic-application-pipeline.event-listener.match-repository-name" $params -}}
  {{- $matchesBranchName := include "generic-application-pipeline.event-listener.match-branch-name" $params -}}
  {{- $doesntMatchOnDeleteEvents := include "generic-application-pipeline.event-listener.bitbucket.doesnt-match-on-delete-events" $params -}}
  {{- $doesntMatchOnTagEvents := include "generic-application-pipeline.event-listener.bitbucket.doesnt-match-on-tag-events" $params -}}
  {{- $isPullRequestEvent := include "generic-application-pipeline.event-listener.bitbucket.is-pull-request-event" . -}}
  {{- $isNotPullRequestEvent := include "generic-application-pipeline.event-listener.bitbucket.is-pull-request-event" (dict "invertCondition" true) -}}
  
{{- /* Matches on generic push events */}}
- name: {{ $baseName }}-trigger-monorepo-{{ $repoType }}-{{ $index }}
  interceptors:
    - params:
        - name: filter
          value: "{{ $isNotPullRequestEvent }} && {{ $matchesRepoName }} && {{ $matchesBranchName }} && {{ $doesntMatchOnTagEvents }} && {{ $doesntMatchOnDeleteEvents }}"

        - name: overlays
          value:
            {{- include "generic-application-pipeline.event-listener.bitbucket.get-ssh-url-from-webhook" . | indent 12 }}
            {{- include "generic-application-pipeline.event-listener.build-git-host-url-from-parameters" $params | indent 12 }}

      {{- include "generic-application-pipeline.event-listener.template-interceptor-reference" . | indent 4 }}

  {{- include "generic-application-pipeline.event-listener.template-trigger-bindings" $params -}}
  {{- include "generic-application-pipeline.event-listener.template-trigger-template" $params }}

  {{- /* Matches PR events (new PR, source branch updates, etc.) */}}
  {{- $params := dict "baseName" $baseName "repoType" $repoType "repoName" $repoName "triggerBranchPattern" $triggerBranchPattern "isMonorepo" $isMonorepo "enablePullRequestMode" true -}}
  {{- $matchesRepoName := include "generic-application-pipeline.event-listener.match-repository-name" $params -}}
  {{- $matchesBranchName := include "generic-application-pipeline.event-listener.match-branch-name" $params }}

- name: {{ $baseName }}-trigger-monorepo-{{ $repoType }}-2-{{ $index }}
  interceptors:
    - params:
        - name: filter
          value: "{{ $isPullRequestEvent }} && {{ $matchesRepoName }} && {{ $matchesBranchName }}"

        - name: overlays
          value:
            {{- include "generic-application-pipeline.event-listener.bitbucket.get-ssh-url-from-webhook" $params | indent 12 }}
            {{- include "generic-application-pipeline.event-listener.build-git-host-url-from-parameters" $params | indent 12 }}

      {{- include "generic-application-pipeline.event-listener.template-interceptor-reference" . | indent 4 }}

  {{- include "generic-application-pipeline.event-listener.template-trigger-bindings" $params -}}
  {{- include "generic-application-pipeline.event-listener.template-trigger-template" $params }}

{{- end }}