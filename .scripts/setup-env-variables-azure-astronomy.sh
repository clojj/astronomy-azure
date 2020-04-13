#!/usr/bin/env bash

# ====== astronomy Azure Coordinates =====
export RESOURCE_GROUP=astronomy-resource-group
export REGION=westus2
export AKS_CLUSTER=astronomy-aks-cluster
export CONTAINER_REGISTRY=astronomycontainerregistry

export IMAGE_TAG=dev

# ====== Docker image port configuration ======
export DEMO_SERVICE_PORT=8080
export ASTRONOMY_SERVICE_PORT=8080
