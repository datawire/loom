#!/usr/bin/env bash
set -euxo pipefail

./gradlew clean test shadowJar

set +x
docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}
set -x

ci/dockerize.py --push
