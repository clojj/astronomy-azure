#!/usr/bin/env bash

mkdir -p deploy
envsubst < 1-demo-service.yaml > deploy/1-demo-service.yaml
envsubst < 2-astronomy-service.yaml > deploy/2-astronomy-service.yaml