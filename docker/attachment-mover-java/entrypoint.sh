#!/bin/bash
set -e

source "$HOME/.sdkman/bin/sdkman-init.sh"

export GRAALVM_HOME=$JAVA_HOME

"${GRAALVM_HOME}"/bin/gu install native-image
export PATH=${GRAALVM_HOME}/bin:$PATH

echo "-----"
env | sort -i
echo "-----"

java --version
mvn --version

$@