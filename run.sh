#!/bin/bash

set -e

docker build -t signal-cli docker/signal-cli
# selftest
docker run --rm -ti signal-cli -v >/dev/null

docker build -t attachment-mover-java -f docker/attachment-mover-java/Dockerfile .
# selftest
docker run --rm -ti attachment-mover-java -h >/dev/null

# binary not working, dont know why :(
#docker build --target=builder -t attachment-mover -f docker/attachment-mover/Dockerfile .
#docker run --rm -ti \
#  -v "$PWD/.m2":/src/.m2 \
#  -v "$PWD/target-c":/src/target \
#  attachment-mover \
#  mvn -Dmaven.repo.local=.m2 verify -P native

#if [[ -n $USE_NATIVE ]]; then
#  mvn clean verify -P native
#  cp target/*-runner target/app
#  upx -1v target/app
#  target/app -h
#  docker build --target=release -t attachment-mover -f docker/attachment-mover/Dockerfile .
#  docker run --rm -ti attachment-mover -h
#else
#mvn clean verify
#java -jar target/quarkus-app/quarkus-run.jar -h
#fi

exit
fifo=$(mktemp)
echo "fifo: $fifo"
docker run --rm -ti \
  --user $(id -u):$(id -g) \
  --volume "$HOME"/.local/share/signal-cli:/.local/share/signal-cli \
  -e USERNAME="${USERNAME}" \
  signal-cli | tee "${fifo}"°

# docker run --rm -i \
#    -u $(id -u):$(id -g) \
#    -v /home/lars/.local/share/signal-cli/attachments:/signal-source \
#    -v "$PWD"/moved_attachments:/moved_attachments \
#    attachment-mover -d /signal-source -t /moved_attachments
