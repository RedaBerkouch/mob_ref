{{/*
Gives the user the possibility to overwrite the release name (default = ArogCD Application name)
*/}}
{{- define "generic-application-pipeline.resourceBaseName" -}}
  {{- .Values.overrideReleaseName | default $.Release.Name }}
{{- end -}}

{{/*
defines the pipeline type suffix
*/}}
{{- define "generic-application-pipeline.pipelineSuffix" -}}
  gp
{{- end -}}

{{- define "generic-application-pipeline.releaseName" }}
  {{- $suffix := include "generic-application-pipeline.pipelineSuffix" . }}
  {{- $xyt := .Values.overrideReleaseName -}}
  {{- $resourceBaseName := include "generic-application-pipeline.resourceBaseName" . }}
  {{- $releaseName := printf "%s-%s" $resourceBaseName $suffix -}}
  {{- $releaseName -}}
{{- end -}}

{{- define "generic-application-pipeline.secretStore" }}
{{- $secretStore := "secretstore" }}
{{- $releaseName := include "generic-application-pipeline.releaseName" . }}
{{- $externalSecretStoreName := printf "%s-%s" $releaseName $secretStore -}}
{{- $externalSecretStoreName -}}
{{- end -}}

{{/*
Checks if the total Char Number of Release Name and Release Namespace (as route host is
created by route name and namespace) is higher then 63 chars. Note: We check for 60 chars here since we suffix our
routes with "-el". If total chars are higher then allowed the name of the route will be truncated.
*/}}
{{- define "generic-application-pipeline.routeName" }}
{{- $suffix := include "generic-application-pipeline.pipelineSuffix" . }}
{{- $resourceBaseName := include "generic-application-pipeline.resourceBaseName" $ }}
{{- $releaseName := printf "%s-%s" $resourceBaseName $suffix -}}
{{- $chars := add (len .Release.Namespace) (len $releaseName) -}}
{{- if (gt $chars 60) -}}
  {{- $allowed := sub 56 (len .Release.Namespace) | int -}}
  {{- $releaseName | trunc $allowed -}}-{{ $suffix }}
{{- else -}}
  {{- $releaseName -}}
{{- end -}}
{{- end -}}

{{/*
Checks if the total Char Number of Release Name and provided resource name (as Tekton Task's metadata.name for
example is subject to DNS naming spec) is higher then 63 chars.
 If total chars are higher then allowed the name of the name will be truncated.
*/}}
{{- define "generic-application-pipeline.resourceName" }}
  {{- $ := index . 0 }}
  {{- $resourceName := index . 2 }}
  {{- with index . 0 }}
    {{- $suffix := include "generic-application-pipeline.pipelineSuffix" . }}
    {{- $resourceBaseName := include "generic-application-pipeline.resourceBaseName" $ }}
    {{- $taskPrefix := printf "%s-%s" $resourceBaseName $suffix -}}
    {{- $chars := add (len $taskPrefix) (len $resourceName ) -}}
    {{- if (gt $chars 63) -}}
      {{- $allowed := sub 62 (len $resourceName) | int -}}
      {{- trunc $allowed $taskPrefix }}-{{ $resourceName }}
    {{- else -}}
      {{- printf "%s-%s" $taskPrefix $resourceName -}}
    {{- end -}}
  {{- end -}}
{{- end -}}

{{/*
apply replacement on datacenter related resources if configured so
usage: {{ include "generic-application-pipeline.replaceDcResource" (list $ . "string-to-eventually-apply-replacement") }}
*/}}
{{- define "generic-application-pipeline.replaceDcResource" -}}
    {{- $val := index . 2 -}}
    {{- with index . 0 -}}
        {{- $replacementDone := false -}}
        {{- if (hasKey .Values "datacenter") -}}
            {{- if (hasKey .Values.datacenter "useDatacenter") -}}
                {{- $useDatacenter := index .Values.datacenter "useDatacenter" -}}
                {{- if ($useDatacenter) -}}
                    {{- $transformConfigs := index .Values.datacenter "transformConfigs" -}}
                    {{- if (hasKey $transformConfigs $useDatacenter) -}}
                        {{- $trConf := index $transformConfigs $useDatacenter -}}
                        {{- if ($trConf) -}}
                            {{- range $trConf -}}
                                {{- if (not $replacementDone) -}}
                                    {{- $return := regexReplaceAll .pattern $val .replace -}}
                                    {{- if (ne $return $val) -}}
                                        {{- $replacementDone = true -}}
                                        {{- $return -}}
                                    {{- end -}}
                                {{- end -}}
                            {{- end -}}
                        {{- end -}}
                    {{- else -}}
                        {{- fail "error: value for .Values.datacenter.useDatacenter not present in .Values.datacenter.transformConfigs" -}}
                    {{- end -}}
                {{- end -}}
            {{- end -}}
         {{- end -}}
        {{- if (not $replacementDone) -}}
            {{- $val -}}
        {{- end -}}
    {{- end -}}
{{- end -}}

{{/*
set
*/}}
{{- define "generic-application-pipeline.applyApplicationDefaults" }}
    {{- $ := index . 1 -}}
    {{- with index . 0 -}}
        {{- $application := . -}}
        {{- $application = merge $application (dict "repoType" "bitbucket") -}}
        {{- $application = merge $application (dict "taskComputeResources" (merge (default dict .taskComputeResources) $.Values.defaultTaskComputeResources)) -}}
        {{- $taskRunSpecsList := list -}}
        {{ range $tk, $tv := .taskComputeResources -}}
            {{- if $tv -}}
                {{- if and (hasKey $tv "computeResources") $tv.computeResources -}}
                    {{- $taskRunSpecsList = append $taskRunSpecsList (dict "pipelineTaskName" (kebabcase $tk) "computeResources" $tv.computeResources) -}}
                {{- end -}}
            {{- end -}}
        {{- end }}
        {{- if gt (len $taskRunSpecsList) 0 -}}
            {{- $application = merge $application (dict "taskRunSpecs" (dict "taskRunSpecs" $taskRunSpecsList)) -}}
        {{- else -}}
            {{- $application = merge $application (dict "taskRunSpecs" (dict "taskRunSpecs" list)) -}}
        {{- end -}}
        {{- $application | toJson -}}
    {{- end -}}
{{- end -}}

{{/*
defines a template that returns true when at least one application is hosted on azure devops
*/}}
{{- define "generic-application-pipeline.hasAzureDevopsRepo" -}}
{{- $list := list }}
{{- range .Values.applications }}
{{- $list = append $list .repoType }}
{{- end }}
{{- $result := has "azure" $list }}
{{- $result -}}
{{- end -}}

{{/*
defines a template that returns true when at least one application is hosted on bitbucket
*/}}
{{- define "generic-application-pipeline.hasBitbucketRepo" -}}
{{- $list := list }}
{{- range .Values.applications }}
  {{- $repoType := default "bitbucket" .repoType }}
  {{- $list = append $list $repoType }}
{{- end }}
{{- $result := has "bitbucket" $list }}
{{- $result -}}
{{- end -}}
