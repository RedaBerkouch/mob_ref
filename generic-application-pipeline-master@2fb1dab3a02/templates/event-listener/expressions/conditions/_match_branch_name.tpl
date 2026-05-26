{{- define "generic-application-pipeline.event-listener.match-branch-name" -}}
  {{- /* Condition template for eventlistener interceptors */ -}}
  {{- /* Matches if the branch name of $branchName matchesthe branch pattern of $triggerBranchPattern */ -}}

  {{- $repoName := .repoName -}}
  {{- $customIntermediateBranchPath := "[0].ref" -}}
  {{- $customBranchPropertyBaseName := ".changes" -}}
  {{- $repoType := .repoType -}}
  {{- $triggerBranchPattern := .triggerBranchPattern -}}
  {{- $enablePullRequestMode := .enablePullRequestMode -}}


  {{- if $enablePullRequestMode -}}
    {{- $customBranchPropertyBaseName = ".pullRequest" -}}
    {{- $customIntermediateBranchPath = ".fromRef" -}}
  {{- end }}

  {{- $objectContainingBranchNameProperty := printf "body%s" $customBranchPropertyBaseName }}
  
  {{- $branchNameProperty := printf "body%s%s.displayId" $customBranchPropertyBaseName $customIntermediateBranchPath -}}

  has({{ $objectContainingBranchNameProperty }}) && matches({{ $branchNameProperty }},'{{- $triggerBranchPattern -}}')

{{- end -}}