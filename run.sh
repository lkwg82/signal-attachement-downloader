#!/bin/bash

set -e

docker build -t signal-cli docker/signal-cli
# selftest
docker run --rm -ti signal-cli -v >/dev/null

docker build -t attachment-mover-java -f docker/attachment-mover-java/Dockerfile .
# selftest
docker run --rm -ti attachment-mover-java -h >/dev/null

cid=$(docker run --rm -ti \
  --user $(id -u):$(id -g) \
  --volume "$HOME"/.local/share/signal-cli:/.local/share/signal-cli \
  --detach \
  signal-cli -u"${USERNAME}" -o json receive -t -1)

function finish {
  echo "cleanup"
  echo -n " killing ${cid} ... "
  docker kill "${cid}" > /dev/null && echo "killed"
}
trap finish EXIT

docker logs -f "${cid}" | docker run --rm -i \
    -u $(id -u):$(id -g) \
    -v /home/lars/.local/share/signal-cli/attachments:/signal-source \
    -v "$PWD"/moved_attachments:/moved_attachments \
    attachment-mover-java -d /signal-source -t /moved_attachments --debug