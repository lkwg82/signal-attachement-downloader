#!/bin/bash

set -ex

if [[ -z "$DRY_RUN" ]]; then
  echo "info: to actual run prepend DRY_RUN=0 to $0"
  dry_run='echo DRY_RUN '
else
  if [[ $DRY_RUN == 0 ]]; then
    dry_run=""
  else
    echo "ERROR: not working"
    echo "$DRY_RUN"
    exit 1
  fi
fi

if [[ ${#dry_run} == 0 ]]; then
  ./run.sh
fi

timestamp=${RELEASE_TIMESTAMP:-$(date "+%Y%m%d-%H%M%S")}
$dry_run git tag "$timestamp" || echo "already tagged"
$dry_run git push --tags

signalCliVersion=$(docker history --no-trunc signal-cli | grep -v ^$ | grep SIGNAL_CLI_VERSION | cut -d= -f2 | cut -d\  -f1 | xargs)

docker_assets=$(mktemp)

function tag_and_push(){
  cat <<EOF >> "$docker_assets"
\`docker pull $2\` ($(docker images "$1" --format "{{.Size}}"))
EOF
  $dry_run docker tag "$1" "$2"
  $dry_run echo "pushing $2"
  $dry_run echo
  $dry_run docker push "$2"

}

tag_and_push signal-cli lkwg82/signal-attachment-mover:signal-cli-"$signalCliVersion"
tag_and_push signal-cli lkwg82/signal-attachment-mover:signal-cli

tag_and_push attachment-mover lkwg82/signal-attachment-mover:mover
tag_and_push attachment-mover lkwg82/signal-attachment-mover:mover-"$timestamp"

function log_commits(){
  if [[ ${#dry_run} == 0 ]]; then
    git log --oneline  --pretty=format:"%h: %s" "$(git tag | head -n2 | xargs | sed -e 's| |..|')"
  else
    git log --oneline --pretty=format:"%h: %s" "$(git tag | head -n1 )"
  fi
}

release="test"
if [[ ${#dry_run} == 0 ]]; then
  release="$timestamp"
  gh release create "$release" --prerelease --generate-notes
else
  if gh release list --json tagName | jq -e '.[].tagName | select(. == "'"$release"'")'; then
    gh release delete "$release" --cleanup-tag --yes \
    ||  gh release delete "$release" --yes || echo "egal"
  fi
  gh release create "$release" --draft --generate-notes
fi

echo "waiting some time"
sleep 15


body_file="$(mktemp).md"
gh release view "$release" --json body | jq -r '.body' > "$body_file"

cat << EOF >> "$body_file"

-----------------

$(cat "$docker_assets")

see https://hub.docker.com/r/lkwg82/signal-attachment-mover/tags
EOF
gh release edit "$release" --notes-file "$body_file"

if [[ ${#dry_run} == 0 ]]; then
  gh release edit "$release" --latest --prerelease=false
fi