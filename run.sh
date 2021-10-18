#!/bin/bash

set -e


# signal-cli
(
  cd docker/signal-cli
  signalCliVersion=$(grep ^ARG Dockerfile | grep "SIGNAL_CLI_VERSION" | cut -d= -f2 )
  docker build -t signal-cli .

  log=$(tempfile)
  if ! docker run --rm -ti signal-cli -v > "$log" ; then
    echo "ERROR signal-cli failed" >&2
    cat "$log"
    exit 1
  fi
)
  exit

docker build -t attachment-mover-java -f docker/attachment-mover-java/Dockerfile .
log=$(tempfile)
if ! docker run --rm -ti --entrypoint java attachment-mover-java -jar quarkus-run.jar -h >"$log"; then
  echo "ERROR app failed" >&2
  cat "$log"
  exit 1
fi

if [[ -n $RELEASE ]]; then
  timestamp=$(date "+%Y%m%d-%H%M%S")

  signalCliVersion=$(docker history --no-trunc signal-cli | grep -v ^$ | grep SIGNAL_CLI_VERSION | cut -d= -f2 | cut -d\  -f1| xargs)
  docker tag signal-cli lkwg82/signal-attachment-mover:signal-cli-"$signalCliVersion"
  docker push lkwg82/signal-attachment-mover:signal-cli-"$signalCliVersion"
  docker tag signal-cli lkwg82/signal-attachment-mover:signal-cli
  docker push lkwg82/signal-attachment-mover:signal-cli

  docker tag attachment-mover-java lkwg82/signal-attachment-mover
  docker tag attachment-mover-java lkwg82/signal-attachment-mover:"$timestamp"
  docker push lkwg82/signal-attachment-mover:"$timestamp"
  docker push lkwg82/signal-attachment-mover
else
  if [[ -z $USERNAME ]]; then
    echo "ERROR pass USERNAME as env var"
    exit 1;
  fi

  docker run --rm -it \
      -u $(id -u):$(id -g) \
      --volume "$HOME"/.local/share/signal-cli:/home/user/.local/share/signal-cli \
      --volume /home/lars/.local/share/signal-cli/attachments:/signal-source \
      --volume "$PWD"/moved_attachments:/moved_attachments \
      -e USERNAME="${USERNAME}" \
      attachment-mover-java
fi