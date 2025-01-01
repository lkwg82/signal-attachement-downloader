#!/bin/bash
set -ex

export JAVA_TOOL_OPTIONS="-Dfile.encoding=utf8"

java -jar quarkus-run.jar \
  --moved-attachment-dir /moved_attachments \
  --signal-attachment-dir /signal_attachments \
  --messages-log "${MESSAGES_LOG:-/output/messages.log}" \
  --debug