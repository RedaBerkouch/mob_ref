#!/bin/bash
set -e

testDirectory="tmp"
testBranch="develop" # Overriden for 2nd test, see below
gitProjectName="STIRHOS"
gitRepoName="sti-gap-example"
clusterProjectName="bit-amber-pipelines-d"
bitbucketBaseURL="https://bitbucket.bit.admin.ch"

_getLatestWebhookEventId() {
  curl -s \
    -H "Authorization: Bearer ${BITBUCKET_ACCESS_TOKEN}" \
    "${bitbucketBaseURL}/rest/api/latest/projects/${gitProjectName}/repos/${gitRepoName}/webhooks/1005/latest" \
    | jq -r '.result.body | fromjson | .eventID'
}

_getPipelineRunNameByEventId() {
  local eventID="$1"

  oc get pipelineruns \
    -l "triggers.tekton.dev/triggers-eventid=${eventID}" \
    --sort-by='{.metadata.creationTimestamp}' \
    -o jsonpath='{.items[-1].metadata.name}'
}

_pipelineRunStatus() {
  oc get pipelinerun "$1" -o jsonpath='{.status.conditions[0].status}'
}

_pipelineRunResults() {
  oc get pipelinerun "$1" -o yaml | yq -r '.status.conditions[]'
}

_waitForPipelineRun() {
  local eventID="$1"
  local pipelineRunName=""
  local timeout=900
  local runStatus=""

  until [[ -n "${pipelineRunName}" ]]; do
    echo "INFO: Searching PipelineRun for eventID ${eventID}"
    sleep 2
    pipelineRunName="$(_getPipelineRunNameByEventId "${eventID}")"
  done

  echo "INFO: Found PipelineRun ${pipelineRunName}"

  SECONDS=0
  while [ $SECONDS -lt $timeout ]; do
    sleep 5
    runStatus="$(_pipelineRunStatus "${pipelineRunName}")"
    if [[ "${runStatus}" == "False" ]]; then
      echo "ERROR: PipelineRun failed with status condition"
      _pipelineRunResults "${pipelineRunName}"
      exit 1
    elif [[ "${runStatus}" == "True" ]]; then
      echo "INFO: PipelineRun succeeded"
      _pipelineRunResults "${pipelineRunName}"
      break
    else
      echo "INFO: PipelineRun status is ${runStatus}"
    fi
  done

  if [ $SECONDS -gt $timeout ]; then
    echo "ERROR: Test run timed out. Exiting test"
    exit 1
  fi
}

helpFunction()
{
   echo ""
   echo "Usage: $0 -c testCoverage -m testMode"
   echo -e "\t-c Specify test coverage mode - build|all"
   echo -e "\t   build - Will run the pipeline in build mode only"
   echo -e "\t   all - Will run the pipeline in build and release mode"
   echo -e "\t-m Specify run mode - tekton|local"
   echo -e "\t   tekton - Intended to be used if the test is executed trough Tekton"
   echo -e "\t   local - Execute the test locally"
   exit 1 # Exit script after printing help
}

while getopts m:c: opt
do
    case "${opt}" in
        m) mode=${OPTARG};;
        c) coverage=${OPTARG};;
    esac
done

if [ -z "$mode" ] || [ -z "$coverage" ] || [[ ! "$mode" =~ (tekton|local) ]] || [[ ! "$coverage" =~ (build|all) ]]; then
  helpFunction
  exit
fi

oc project "${clusterProjectName}"

if [[ -z "${BITBUCKET_ACCESS_TOKEN}" ]]; then # The access token is provided by the test task within the Tekton pipeline
  echo "Error: The 'BITBUCKET_ACCESS_TOKEN' variable is not set."
  echo "More information on how to set a http access token: https://confluence.atlassian.com/bitbucketserver0721/http-access-tokens-1115665626.html"
  exit 1
fi

#
# Bump trigger test build in Dockerfile to trigger pipeline run
#

if test -d "$testDirectory"; then
    rm -rf "$testDirectory"
fi
mkdir -p "$testDirectory"
git clone "ssh://git@bitbucket.bit.admin.ch/${gitProjectName}/${gitRepoName}.git" "${testDirectory}"
cd "${testDirectory}"
git checkout "${testBranch}"

perl -i -p -e 's/^(# trigger test build )([0-9]+)$/$1.($2+1)/ge' Dockerfile

if ! git status --short Dockerfile | grep -q .; then
  echo "ERROR: No changes in Dockerfile"
  exit 1
fi

if [ "${mode}" == "tekton" ]; then
# GIT config block required if test is run within Tekton pipeline
  git config --global user.email "tekton@bit.admin.ch"
  git config --global user.name "Tekton TechUser"
fi

git commit -m "build: update image version" Dockerfile
git push origin "${testBranch}"

sleep 60
eventID="$(_getLatestWebhookEventId)"
if [[ -z "${eventID}" || "${eventID}" == "null" ]]; then
  echo "ERROR: Could not determine webhook eventID"
  exit 1
fi

_waitForPipelineRun "${eventID}"

##
## Additional test: Bump trigger test build in Dockerfile to trigger pipeline run on vulnerable branch
##
#
#testBranch="develop-vulnerable"
#argoCDAppName="gap-example2"
#
#if test -d "$testDirectory"; then
#    rm -rf $testDirectory
#fi
#mkdir -p $testDirectory
#git clone ssh://git@bitbucket.bit.admin.ch/${gitProjectName}/${gitRepoName}.git $testDirectory
#cd $testDirectory
#git checkout $testBranch
#
#perl -i -p -e 's/^(# trigger test build )([0-9]+)$/$1.($2+1)/ge' Dockerfile;
#git status Dockerfile | grep modified
#if [ $? -eq 0 ]
#then
#  set -e
#  if [ "$mode" == "tekton" ]; then
#  # GIT config block required if test is run within Tekton pipeline
#    git config --global user.email "tekton@bit.admin.ch"
#    git config --global user.name "Tekton TechUser"
#  fi
#  git commit -m "build: update image version" Dockerfile
#  git push origin $testBranch
#else
#  echo "No changes since last run"
#  exit 1
#fi
#
## Check pipeline run status and exit/continue according to pipeline run results
#_getPipelineRunResults

# Clean up
if test -d "$testDirectory"; then
    rm -rf "$testDirectory"
fi