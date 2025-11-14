FROM alpine/curl:8.14.1 AS signal_cli_download

# based on https://www.redcube.de/update-version-in-variables-or-buildargs-in-dockerfiles-with-renovatebot/
# renovate: datasource=github-releases depName=AsamK/signal-cli extractVersion=^v(?<version>.*)$
ENV SIGNAL_CLI_VERSION=0.13.22
ENV SIGNAL_CLI_URL="https://github.com/AsamK/signal-cli/releases/download/v${SIGNAL_CLI_VERSION}/signal-cli-${SIGNAL_CLI_VERSION}-Linux-native.tar.gz"

RUN curl --silent --location $SIGNAL_CLI_URL | tar -xvzf - -C /usr/bin \
    && touch -mt 197001010101 -at 197001010101 /usr/bin/signal-cli

FROM debian:13-slim AS qrencode

RUN apt-get update && apt-get install -y  qrencode

FROM debian:13-slim

COPY --from=signal_cli_download /usr/bin/signal-cli /usr/bin
COPY --from=qrencode /usr/bin/qrencode /usr/bin
COPY --from=qrencode /lib/x86_64-linux-gnu/libqrencode.so.4 /lib/x86_64-linux-gnu/
COPY --from=qrencode /lib/x86_64-linux-gnu/libpng16.so.16 /lib/x86_64-linux-gnu/


COPY entrypoint.sh /entrypoint.sh
RUN touch -mt 197001010101 -at 197001010101 /entrypoint.sh

RUN useradd --uid 1000 -m user
USER user

# selftests
RUN signal-cli --version
RUN date | qrencode -t utf8
RUN testNumber=+4912345689 \
    && signal-cli -u $testNumber receive 2>&1 | grep "not registered"

ENTRYPOINT ["/entrypoint.sh"]
