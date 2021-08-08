#FROM debian:stretch-slim as builder
#RUN apt-get update && apt-get upgrade -y
#
#RUN apt-get install -qq -y curl unzip zip && curl -s "https://get.sdkman.io" | bash
#
#RUN rm /bin/sh && ln -s /bin/bash /bin/sh
#RUN apt-get install -qq -y g++ gcc upx zlib1g-dev # native-image
#RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
#    sdk install maven && \
#    sdk install java $(sdk list java | grep r16-grl | sed -e 's#.*| ##')
#RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
#    GRAALVM_HOME=$JAVA_HOME && \
#    "${GRAALVM_HOME}"/bin/gu install native-image && \
#    export PATH=${GRAALVM_HOME}/bin:$PATH && \
#    native-image --help
#
#
#ADD docker/attachment-mover/entrypoint.sh /entrypoint.sh
#RUN chmod +x /entrypoint.sh
#ENTRYPOINT ["/entrypoint.sh"]
#
#COPY src /src/
#COPY pom.xml /src/
#WORKDIR /src
#
#FROM builder as release-not-working
#RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && \
#    mvn package -P native && \
#    mvn package
#RUN source "$HOME/.sdkman/bin/sdkman-init.sh" && java -version && \
#    java -jar target/quarkus-app/quarkus-run.jar
#RUN mv -v target/*-runner app
#RUN ./app -h
#RUN upx -9v app
#RUN ./app -h

FROM debian:stretch-slim as release
COPY target/app /bin/app

ADD docker/attachment-mover/entrypoint-app.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
