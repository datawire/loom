#!/usr/bin/env bash

./gradlew clean shadowJar
virtualenv venv
venv/bin/pip install docopt semantic_version
DOCKER_REPO=datawire/loom venv/bin/python ci/dockerize.py --no-push
