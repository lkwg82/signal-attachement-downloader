FROM debian:stretch-slim as builder
RUN apt-get update && apt-get upgrade -y
RUN apt-get install -qq -y curl unzip zip

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

RUN curl -s "https://get.sdkman.io" | bash
RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
  sdk install maven && \
    sdk list java
RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
 sdk install java $(sdk list java | grep r16-grl | sed -e 's#.*| ##')
RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
 GRAALVM_HOME=$JAVA_HOME && \
 "${GRAALVM_HOME}"/bin/gu install native-image && \
 export PATH=${GRAALVM_HOME}/bin:$PATH && \
 native-image --help

RUN apt-get install -yy -y gcc
RUN apt-get install -yy -y upx
RUN apt-get install -yy -y zlib1g-dev

ADD docker/attachment-mover/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

ADD . /src
WORKDIR /src

FROM builder as release
RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
    mvn package -P native && \
    mv -v target/*-runner app && \
    ./app -h && \
    upx -9v app && \
    ./app -h


