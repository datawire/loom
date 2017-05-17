#!/usr/bin/env bash

curl -v -X POST -d '@data/model.json'  localhost:7000/api/models
curl -v -X POST -d '@data/config.json' localhost:7000/api/fabrics
curl -v -X POST -d '@terraform/rds-postgresql-standalone-v1/loom-module.json' localhost:7000/api/resource-models
