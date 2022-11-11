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
  cd docker/signal-cli
  signalCliVersion=$(grep ^ARG Dockerfile | grep "SIGNAL_CLI_VERSION" | cut -d= -f2)
  docker build -t signal-cli .

  info "testing ... "
  log=$(mktemp)
  if ! docker run --rm -ti --entrypoint signal-cli -w /tmp signal-cli --version >"$log"; then
    echo "ERROR signal-cli failed" >&2
    cat "$log" | sed -e 's#^#\t#'
    exit 1
  fi
  ok

  info "exit code testing ... "
  log=$(mktemp)
  if docker run --rm -ti --entrypoint signal-cli signal-cli -u +49123456 receive >"$log" 2>&1; then
    echo "ERROR signal-cli failed" >&2
    cat "$log"  | sed -e 's#^#\t#'
    exit 1
  fi
  ok
)

mvnd -Dmaven.repo.local=.m2_repo clean verify
docker build -t attachment-mover-java -f docker/attachment-mover-java/Dockerfile .

log=$(mktemp)
if ! docker run --rm -ti --entrypoint java attachment-mover-java -jar quarkus-run.jar -h >"$log"; then
  echo "ERROR app failed" >&2
  cat "$log"
  exit 1
fi

if [[ -n $RELEASE ]]; then
  timestamp=$(date "+%Y%m%d-%H%M%S")

  signalCliVersion=$(docker history --no-trunc signal-cli | grep -v ^$ | grep SIGNAL_CLI_VERSION | cut -d= -f2 | cut -d\  -f1 | xargs)
  function tag_and_push(){
    app="release"
    docker tag "$1" "$2"
    info "pushing $2"
    echo
    docker push "$2"
  }
  tag_and_push signal-cli lkwg82/signal-attachment-mover:signal-cli-"$signalCliVersion"
  tag_and_push signal-cli lkwg82/signal-attachment-mover:signal-cli

  tag_and_push attachment-mover-java lkwg82/signal-attachment-mover
  tag_and_push attachment-mover-java lkwg82/signal-attachment-mover:"$timestamp"
else
  docker-compose up --build bot
  docker-compose up --build attachment-mover
fi
