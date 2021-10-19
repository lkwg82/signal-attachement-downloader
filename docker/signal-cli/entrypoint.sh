#!/bin/sh
set -ex

signal-cli -u "$USERNAME" --output json receive \
  | tee --append /output/messages.log