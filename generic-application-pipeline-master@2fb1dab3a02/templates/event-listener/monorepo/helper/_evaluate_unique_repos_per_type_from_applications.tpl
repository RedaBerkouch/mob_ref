{{- define "generic-application-pipeline.event-listener.evaluate-unique-repos-per-type-by-applications" }}
  {{- $applications := . -}}
  {{- $azureRepositories := dict -}}
  {{- $bitbucketRepositories := dict -}}

  {{- range $applications }}
    {{- $repoName := eq .repoType "azure" | ternary (last (regexSplit "_git\\/" .srcRepo -1)) (.srcRepo | regexFind "[^\\/]+\\.git$" | replace ".git" "") -}}
    {{- $uniqueRepoToAdd := dict $repoName true }}

    {{- if eq .repoType "azure" -}}
      {{- $azureRepositories := merge $azureRepositories $uniqueRepoToAdd -}}
    {{- else -}}
      {{- $bitbucketRepositories := merge $bitbucketRepositories $uniqueRepoToAdd -}}
    {{- end -}}
  {{- end -}}

azure:
{{ $azureRepositories | toYaml |indent 2 }}

bitbucket:
{{ $bitbucketRepositories | toYaml | indent 2 }}

{{- end }}