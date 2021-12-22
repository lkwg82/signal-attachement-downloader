#!/bin/sh
set -e
set -o pipefail

if [ -z "$USERNAME" ]; then
  echo "missing USERNAME variable"
  exit 1
fi

signal-cli -u "$USERNAME" --output json receive \
  | tee --append /output/messages.log