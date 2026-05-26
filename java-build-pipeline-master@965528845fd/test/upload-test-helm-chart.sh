#!/bin/bash

set -e
set -o pipefail

chartLocation=../src
repositoryName=team-jeap-helm-hosted-test
repositoryUrl=https://nexus.bit.admin.ch/repository/${repositoryName}

echo "Pushing helm chart at ${chartLocation} to ${repositoryUrl}"
cd ${chartLocation}
chartName=$(helm show chart . | grep -m1 ^name: | awk -F':' '{print $2}' | tr -d ' ')
chartVersion=$(helm show chart . | grep -m1 ^version: | awk -F':' '{print $2}' | tr -d ' ')

if [ -z "${chartVersion}" ] || [ -z "${chartName}" ]; then
    echo "Unable to parse chart name (${chartName}) and/or chart version (${chartVersion})"
    exit 1
fi

echo "Chart name: '${chartName}'"
echo "Chart version: '${chartVersion}'"
TAGNAME="${chartVersion}"
REPO_DIR=`pwd`

echo "Creating chart package for chart ${chartName}-${chartVersion} from ${chartLocation}"

helm package -u .

echo "Add helm repo ${repositoryName}"

helm repo add ${repositoryName} ${repositoryUrl} --username ${NEXUS_USERNAME} --password ${NEXUS_PASSWORD}
echo "Tagging current release as ${TAGNAME}"

ARTIFACT=$(ls | grep *.tgz)
echo "Upload helm package ${ARTIFACT} to ${repositoryUrl}"
echo "curl --user ${NEXUS_USERNAME}:redacted --upload-file ${ARTIFACT} ${repositoryUrl}/"
curl --user ${NEXUS_USERNAME}:${NEXUS_PASSWORD} --upload-file ${ARTIFACT} ${repositoryUrl}/
rm ${ARTIFACT}