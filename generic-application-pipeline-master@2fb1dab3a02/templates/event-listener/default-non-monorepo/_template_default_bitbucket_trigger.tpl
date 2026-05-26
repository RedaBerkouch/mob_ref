{{- define "generic-application-pipeline.event-listener.template-default-bitbucket-trigger" -}}
  {{- $ := .root -}}
  {{- $baseName := include "generic-application-pipeline.releaseName" $ -}}
  {{- $repoName := .repoName -}}
  {{- $repoType := .repoType -}}
  {{- $appName := .appName -}}
  {{- $triggerBranchPattern := .triggerBranchPattern -}}
  {{- $pipelineSuffix := include "generic-application-pipeline.pipelineSuffix" $ -}}

  {{- $params := dict "appName" $appName "baseName" $baseName "pipelineSuffix" $pipelineSuffix "repoType" $repoType "repoName" $repoName "triggerBranchPattern" $triggerBranchPattern -}}

  {{- $matchesRepoName := include "generic-application-pipeline.event-listener.match-repository-name" $params -}}
  {{- $matchesBranchName := include "generic-application-pipeline.event-listener.match-branch-name" $params -}}
  {{- $doesntMatchOnDeleteEvents := include "generic-application-pipeline.event-listener.bitbucket.doesnt-match-on-delete-events" . -}}
  {{- $doesntMatchOnTagEvents := include "generic-application-pipeline.event-listener.bitbucket.doesnt-match-on-tag-events" . -}}
  {{- $isPullRequestEvent := include "generic-application-pipeline.event-listener.bitbucket.is-pull-request-event" . -}}
  {{- $isNotPullRequestEvent := include "generic-application-pipeline.event-listener.bitbucket.is-pull-request-event" (dict "invertCondition" true) -}}

  {{- /* Matches on generic push events */}}

- name: {{ $appName }}-trigger
  interceptors:
    - params:
        - name: filter
          value: "{{ $isNotPullRequestEvent }} && {{ $matchesRepoName }} && {{ $matchesBranchName }} && {{ $doesntMatchOnTagEvents }} && {{ $doesntMatchOnDeleteEvents }}"

        - name: overlays
          value:
            {{- include "generic-application-pipeline.event-listener.bitbucket.get-ssh-url-from-webhook" $params | indent 12 -}}
            {{- include "generic-application-pipeline.event-listener.null-git-host-url-value" $params | indent 12 }}

      {{- include "generic-application-pipeline.event-listener.template-interceptor-reference" . | indent 4 }}

  {{- include "generic-application-pipeline.event-listener.template-trigger-bindings" $params -}}
  {{- include "generic-application-pipeline.event-listener.template-trigger-template" $params }}


  {{- $params := dict "appName" $appName "baseName" $baseName "pipelineSuffix" $pipelineSuffix "repoType" $repoType "repoName" $repoName "triggerBranchPattern" $triggerBranchPattern "enablePullRequestMode" true }}

  {{- $matchesRepoName := include "generic-application-pipeline.event-listener.match-repository-name" $params -}}
  {{- $matchesBranchName := include "generic-application-pipeline.event-listener.match-branch-name" $params -}}
  {{- /* Matches PR events (new PR, source branch updates, etc.) */}}

- name: {{ $appName }}-pr-trigger
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
{{ end }}