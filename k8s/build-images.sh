#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

VERSION=1.0.0

eval $(minikube docker-env)

docker build -t task-tracker-backend:$VERSION -f backend/Dockerfile .
docker build -t task-tracker-frontend:$VERSION frontend

minikube image load task-tracker-backend:$VERSION
minikube image load task-tracker-frontend:$VERSION

minikube image ls | grep task-tracker
