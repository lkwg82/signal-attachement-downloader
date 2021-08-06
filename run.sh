#!/bin/bash

set -e

docker build -t signal-cli docker/signal-cli

docker build --target=builder -t attachment-mover -f docker/attachment-mover/Dockerfile .
docker run --rm -ti \
  -v "$PWD/.m2":/src/.m2 \
  attachment-mover \
  mvn -Dmaven.repo.local=.m2 verify -P native
exit
docker run --rm -ti \
  --user $(id -u):$(id -g) \
  --volume "$HOME"/.local/share/signal-cli:/.local/share/signal-cli \
  -e USERNAME="${USERNAME}" \
  signal-cli
