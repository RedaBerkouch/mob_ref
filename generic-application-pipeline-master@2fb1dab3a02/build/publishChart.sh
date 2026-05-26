#!/bin/bash

# fail at error
set -e

# $1 = State
# $2 = Message
_echo () {
  echo "[$1] $2"
}

helmRepoTarget=$1

if [ -z "$helmRepoTarget" ] || [[ ! "$helmRepoTarget" =~ (test|prod) ]]; then
  _echo "ERROR" "Specify Helm chart repository to publish - Valid: prod | test"
  exit 1
fi

if [ -z $nexusUser ]; then
  read -s -p "Enter Nexus User: " nexusUser
  printf "\n"
fi

# to follow best practices
if [[ "$nexusUser" =~ ^[uUxX][[:digit:]]{8}$ ]]; then
  _echo "ERROR" "Not allowed to use U/X-Account for upload"
  _echo "ERROR" "Please generate a token via https://nexus.bit.admin.ch/#user/usertoken for uploads"
  _echo "ERROR" "variables are set to null"
  unset nexusUser nexusPass
  exit 1
fi

if [ -z $nexusPass ]; then
  read -s -p "Enter Nexus Password: " nexusPass
  printf "\n"
fi

chartName=$(helm show chart . | grep -m1 ^name: | awk -F':' '{print $2}' | tr -d ' ')
export chartNameUpload=$chartName
chartVersion=$(helm show chart . | grep -m1 ^version: | awk -F':' '{print $2}' | tr -d ' ')
helmComponent=bit-sti-helm-hosted
helmRepoName=sti-helm

case $helmRepoTarget in
test)
  _echo "INFO" "Publish for test"
  helmRepo=https://nexus.bit.admin.ch/repository/${helmComponent}-test/
  ;;
prod)
  _echo "INFO" "Publish for prod"
  helmRepo=https://nexus.bit.admin.ch/repository/bit-pipelines-helm-hosted/
  ;;
esac

# check if login is working
_echo "INFO" "Check if given credentials ($nexusUser/***) works"
#helm repo add --force-update $helmRepoName $helmRepo --username $nexusUser --password $nexusPass
helm repo add $helmRepoName $helmRepo --username $nexusUser --password $nexusPass

# search for version
countOfReleases=$(helm search repo $chartNameUpload --version ^${chartVersion} | grep -v ${chartNameUpload}-test | grep -o $helmRepoName/$chartNameUpload -c || true)

if [[ $countOfReleases -gt 0 ]]; then
  _echo "ERROR" "Already found a release for Chart: $chartName Version: $chartVersion"
  exit 1
fi

chartDir=$PWD

_echo "INFO" "Verify following information:"
_echo "-" "Chart to upload: $chartDir"
_echo "-" "Repo to upload: $helmRepo"
_echo "-" "Chartname: $chartName"
_echo "-" "Chartname to upload: $chartNameUpload"
_echo "-" "Chartversion: $chartVersion"

read -s -p "Is everything correct?"
echo ""

tmpDir=/tmp/tekton-publish-helm-pipeline

test -d $chartDir && _echo "INFO" "Found/Exists" || _echo "ERROR" "Chart directory does not exist"

if [[ -d $tmpDir ]]; then
  rm -rf $tmpDir
fi

mkdir -p $tmpDir/$chartNameUpload
cp -R ./* $tmpDir/$chartNameUpload

cd $tmpDir
yq e '.name=env(chartNameUpload)' $chartDir/Chart.yaml > $chartNameUpload/Chart.yaml
helm lint $chartDir -f $chartDir/values.yaml -f $chartDir/test/values.test.yaml
helm package -u $chartNameUpload

helmpackage=$(ls | grep *.tgz)
_echo "INFO" "Upload helm package to Nexus: $helmpackage"

curl -f -u $nexusUser:$nexusPass $helmRepo/ --upload-file $tmpDir/$helmpackage
echo "INFO" "Succesfully uploaded helm chart to Nexus"

_echo "INFO" "Delete tempfolder $tmpDir"
rm -rf $tmpDir