FROM adoptopenjdk:16-jre-openj9

ENV SIGNAL_CLI_VERSION=0.9.1
ENV URL="https://github.com/AsamK/signal-cli/releases/download/v${SIGNAL_CLI_VERSION}/signal-cli-${SIGNAL_CLI_VERSION}.tar.gz"

ENV PATH=signal-cli-${SIGNAL_CLI_VERSION}/bin:$PATH
RUN curl -sSL $URL | tar -xvzf - \
    && find signal-cli* | xargs -P100 -n1 touch -mt 197001010101 -at 197001010101 \
    && find signal-cli* | xargs -P100 -n1 chmod 0755

ADD entrypoint.sh /entrypoint.sh
RUN touch -mt 197001010101 -at 197001010101 /entrypoint.sh \
    && chmod 0755 /entrypoint.sh


RUN useradd --uid 1000  user
USER user

RUN signal-cli -v # selftest

ENTRYPOINT ["/entrypoint.sh"]
