#!/bin/bash
set -ex
env | sort
id

# shellcheck disable=SC2068
signal-cli -u"${USERNAME}" -o json receive -t -1 \
  | java -jar quarkus-run.jar -t /moved_attachments --debug