#!/bin/bash

set -e

docker build -t signal-cli docker/signal-cli
docker run --rm -ti \
  --user $(id -u):$(id -g) \
  --volume "$HOME"/.local/share/signal-cli:/.local/share/signal-cli \
  -e USERNAME="${USERNAME}" \
  signal-cli