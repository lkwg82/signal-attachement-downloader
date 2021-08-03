#!/bin/bash
set -e

if [[ -z $USERNAME ]]; then
  echo "ERROR please pass USERNAME environment variable"
  exit 1
fi

signal-cli -u${USERNAME} -o json receive -t -1