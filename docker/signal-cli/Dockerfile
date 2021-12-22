FROM eclipse-temurin:17-jdk as jre-builder

RUN jlink --add-modules ALL-MODULE-PATH --output jre \
          --strip-debug --no-man-pages --no-header-files --compress=2
RUN jre/bin/java -version # selftest


RUN find /jre | xargs -P100 -n1 touch -mt 197001010101 -at 197001010101
RUN find /jre | xargs -P100 -n1 chmod 0755

RUN env | sort

FROM debian:stable-slim

COPY --from=jre-builder /jre /jre
ENV JAVA_HOME=/jre
ENV PATH=$PATH:/jre/bin
RUN java -version # selftest

RUN apt-get update && apt-get install -y curl

ENV SIGNAL_CLI_VERSION=0.8.5
ENV SIGNAL_CLI_URL="https://github.com/AsamK/signal-cli/releases/download/v${SIGNAL_CLI_VERSION}/signal-cli-${SIGNAL_CLI_VERSION}.tar.gz"
ENV PATH=/signal-cli-${SIGNAL_CLI_VERSION}/bin:$PATH
RUN curl --silent --location $SIGNAL_CLI_URL | tar -xvzf - \
    && find signal-cli* | xargs -P100 -n1 touch -mt 197001010101 -at 197001010101 \
    && find signal-cli* | xargs -P100 -n1 chmod 0755

ADD entrypoint.sh /entrypoint.sh
RUN touch -mt 197001010101 -at 197001010101 /entrypoint.sh \
    && chmod 0755 /entrypoint.sh

RUN useradd --uid 1000 -m user
USER user

# selftests
RUN signal-cli -v
RUN testNumber=+4912345689 \
    && signal-cli -u $testNumber receive 2>&1 | grep "not registered"

ENTRYPOINT ["/entrypoint.sh"]
