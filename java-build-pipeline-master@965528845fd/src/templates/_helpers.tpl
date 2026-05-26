{{/* Checks if the total Char Number of Release Name and Release Namespace (as route host is
     created by route name and namespace) is higher then 63 chars.
     If total chars are higher then allowed the name of the route will be truncated */}}
{{- define "route.name" -}}
{{- $chars := add (len .Release.Namespace) (len .Release.Name) -}}
{{- if (gt $chars 63) -}}
{{- $allowed := sub 59 (len .Release.Namespace) | int -}}
  {{- .Release.Name | trunc $allowed -}}-ja
{{- else -}}
  {{- .Release.Name -}}-ja
{{- end -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "java-pipeline.labels" -}}
{{- $chart := .Chart -}}
{{- $appName := .appName -}}
pipeline.bit.admin.ch/name: {{ $chart.Name }}
pipeline.bit.admin.ch/version: {{ $chart.Version }}
pipeline.bit.admin.ch/pipeline-checksum: {{ toJson $chart | sha1sum }}
pipeline.bit.admin.ch/appname: {{ $appName | default "not-app-specific" }}
{{- end -}}

{{- define "java-pipeline.annotations" -}}
pipeline.bit.admin.ch/maintainers-email: iewtoolchain@bit.admin.ch
pipeline.bit.admin.ch/maintainers-url: https://confluence.bit.admin.ch/x/WWAXIQ
{{- end -}}

{{/*
Gives the user the possibility to overwrite the release name (default = ArogCD Application name)
*/}}
{{- define "java-pipeline.resourceBaseName" -}}
{{- $baseName := (.Values.global).overrideJbpReleaseName | default .Release.Name -}}
{{/** we allow the resourceBaseName to be 35 charcters long */}}
{{- trunc 35 $baseName -}}
{{- end -}}

{{/*
Gives the user the possibility to overwrite the deployment pipeline release name (default = ArogCD Application name)
*/}}
{{- define "cd-pipeline.resourceBaseName" -}}
{{- $baseName := (.Values.global).overrideCdpReleaseName | default .Release.Name -}}
{{/** we allow the resourceBaseName to be 35 charcters long */}}
{{- trunc 35 $baseName -}}
{{- end -}}

{{/*
defines the pipeline type prefix
*/}}
{{- define "java-pipeline.pipelinePrefix" -}}
jbp
{{- end -}}

{{- define "joinListWithSquoteAndComma" -}}
{{- $local := dict "first" true -}}
{{- range $k, $v := . -}}{{- if not $local.first -}},{{- end -}}{{- $v | squote -}}{{- $_ := set $local "first" false -}}{{- end -}}
{{- end -}}


{{- define "pipeline.stepResources" -}}
{{- $name := .name -}}
{{- $resources := .Values.resources | default dict -}}
{{- $task := get $resources .nameSuffix | default dict -}}
{{- $stepOverride := "" -}}
{{- if $task.steps }}
  {{- range $task.steps }}
    {{- if eq .name $name }}
      {{- $stepOverride = .resources -}}
    {{- end }}
  {{- end }}
{{- end }}
{{- if $stepOverride }}
{{- $stepOverride | toYaml | nindent 8 }}
{{- else if $task.resources }}
{{- $task.resources | toYaml | nindent 8 }}

{{- else }}
  {{- $profiles := $.Values.defaultResourceProfiles | default dict -}}
  {{- $profile := .defaultProfile | default "small" -}}
  {{- if hasKey $profiles $profile }}
    {{- index $profiles $profile | toYaml | nindent 8 }}
  {{- else }}
    {}
  {{- end }}
{{- end }}
{{- end }}

{{/*
compute the additional image registries based on the domain-list provided and the constant subdomain.

Input: {domains: "nexus-campus.bit.admin.ch/team nexus-whatever.bit.admin.ch/team", baseName: our-repo }
Output: our-repo.nexus-campus.bit.admin.ch/team our-repo.nexus-whatever.bit.admin.ch/team
*/}}
{{- define "java-pipeline.additionalImageRegistries" -}}
    {{- $additionalImageRegistriesList := list -}}
    {{- $additionalDomains := .domains -}}
    {{- $baseName := .baseName -}}
    {{- if $additionalDomains -}}
      {{- $domainList := splitList " " $additionalDomains -}}
      {{- range $domainList -}}
        {{- $newRegistryItem := printf "%s.%s" $baseName . -}}
        {{- $additionalImageRegistriesList = append $additionalImageRegistriesList $newRegistryItem -}}
      {{- end -}}
    {{- end -}}
    {{- join " " $additionalImageRegistriesList -}}
{{ end }}