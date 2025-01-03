#!/bin/bash

set -e

if [[ -n $DEBUG ]]; then
  set -x
fi

function info() {
  echo -n "[${app:unknown-app}] $*"
}
function ok() {
  echo "ok"
}


# signal-cli
(
  app="signal-cli"
  docker build -t signal-cli docker/signal-cli

  info "testing ... "
  log=$(mktemp)
  if ! docker run --rm -t --entrypoint signal-cli -w /tmp signal-cli --version >"$log"; then
    echo "ERROR signal-cli failed" >&2
    sed -e 's#^#\t#' "$log"
    exit 1
  fi
  ok

  info "exit code testing ... "
  log=$(mktemp)
  if docker run --rm -t --entrypoint signal-cli signal-cli -u +49123456 receive >"$log" 2>&1; then
    echo "ERROR signal-cli failed" >&2
    sed -e 's#^#\t#' "$log"
    exit 1
  fi
  ok
)


if [[ -z "$CI" ]]; then
  mvnd -Dmaven.repo.local=.m2_repo clean verify
else
  mkdir -p .m2_repo
fi

docker build -t attachment-mover -f docker/attachment-mover-java/Dockerfile .

log=$(mktemp)
if ! docker run --rm -t --entrypoint java attachment-mover -jar quarkus-run.jar -h >"$log"; then
  echo "ERROR app failed" >&2
  cat "$log"
  exit 1
fi

if [[ -z "$CI" ]]; then
  docker compose up --build bot
  docker compose up --build attachment-mover
fi

