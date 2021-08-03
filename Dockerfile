FROM adoptopenjdk:16-jre-openj9-focal
ARG SIGNAL_CLI_VERSION=0.8.4.1

RUN curl -sSL https://github.com/AsamK/signal-cli/releases/download/v${SIGNAL_CLI_VERSION}/signal-cli-${SIGNAL_CLI_VERSION}.tar.gz | tar -xvzf -
RUN ls
ENV PATH=signal-cli-${SIGNAL_CLI_VERSION}/bin:$PATH
RUN signal-cli -v # selftest