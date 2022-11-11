FROM debian:stable-slim

RUN apt-get update && apt-get install -y curl

ENV SIGNAL_CLI_VERSION=0.11.5.1
ENV SIGNAL_CLI_URL="https://github.com/AsamK/signal-cli/releases/download/v${SIGNAL_CLI_VERSION}/signal-cli-${SIGNAL_CLI_VERSION}-Linux-native.tar.gz"
#ENV PATH=/signal-cli-${SIGNAL_CLI_VERSION}/bin:$PATH
RUN curl --silent --location $SIGNAL_CLI_URL | tar -xvzf - -C /usr/bin \
    && touch -mt 197001010101 -at 197001010101 /usr/bin/signal-cli

ADD entrypoint.sh /entrypoint.sh
RUN touch -mt 197001010101 -at 197001010101 /entrypoint.sh

RUN useradd --uid 1000 -m user
USER user

# selftests
RUN signal-cli --version
RUN testNumber=+4912345689 \
    && signal-cli -u $testNumber receive 2>&1 | grep "not registered"

ENTRYPOINT ["/entrypoint.sh"]
