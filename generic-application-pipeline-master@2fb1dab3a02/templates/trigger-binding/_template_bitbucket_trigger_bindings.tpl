{{- define "generic-application-pipeline.trigger-binding.template-bitbucket-trigger-bindings" -}}
{{- $ := .root -}}
---
{{- /* Trigger binding for generic Bitbucket push events*/ -}}
{{- include "generic-application-pipeline.trigger-binding.template-api-version-kind-and-metadata" .}}
spec:
  {{- include "generic-application-pipeline.trigger-binding.template-bitbucket-trigger-binding-parameters" . | indent 2 }}

---
{{- /* Trigger binding for Bitbucket PR events (new PR, source branch updates, etc.) */ -}}
{{- $params := . | merge (dict "enablePullRequestMode" true) -}}
{{- include "generic-application-pipeline.trigger-binding.template-api-version-kind-and-metadata" $params }}
spec:
  {{- include "generic-application-pipeline.trigger-binding.template-bitbucket-trigger-binding-parameters" $params | indent 2 }}

{{ end -}}