#!/bin/bash

set -e

docker build -t attachment-mover-java -f docker/attachment-mover-java/Dockerfile .

# selftests
docker run --rm -ti --entrypoint signal-cli attachment-mover-java -v >/dev/null
docker run --rm -ti --entrypoint java attachment-mover-java -jar quarkus-run.jar -h >/dev/null

if [[ -n $RELEASE ]]; then
  timestamp=$(date "+%Y%m%d-%H%M%S")
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
      attachment-mover-java -d /signal-source -t /moved_attachments --debug
fi