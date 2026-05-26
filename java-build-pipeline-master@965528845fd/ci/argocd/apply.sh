/bin/bash

oc apply -f pipelines-project.yml
oc apply -f java-build-pipeline-application.yml
