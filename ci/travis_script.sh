#!/usr/bin/env bash
set -euxo pipefail

./gradlew clean test shadowJar

docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}
ci/dockerize.py --push

if [ "${TRAVIS_BRANCH}" = "master" ]; then
    packer build \
        -var "build_number=${TRAVIS_BUILD_NUMBER}" \
        -var "branch=${TRAVIS_BRANCH}" \
        -var "commit=${TRAVIS_COMMIT}" \
        src/ami/centos-7.json
fi
