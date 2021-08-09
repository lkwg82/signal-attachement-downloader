FROM adoptopenjdk:16-openj9 as builder

RUN mkdir /build
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw clean  || echo "ok, warmup cache"
RUN ./mvnw compile  || echo "ok, warmup cache"
RUN ./mvnw test  || echo "ok, warmup cache"
RUN ./mvnw verify  || echo "ok, warmup cache"

COPY src src
RUN ./mvnw clean verify
RUN java -jar target/quarkus-app/quarkus-run.jar -h

WORKDIR /build/target

RUN find | xargs -P100 -n1 touch -mt 197001010101 -at 197001010101
RUN find | xargs -P100 -n1 chmod 0755

FROM adoptopenjdk:16-jre-openj9

#
# signal-cli
#
ARG SIGNAL_CLI_VERSION=0.8.5

RUN curl -sSL https://github.com/AsamK/signal-cli/releases/download/v${SIGNAL_CLI_VERSION}/signal-cli-${SIGNAL_CLI_VERSION}.tar.gz | tar -xvzf -
ENV PATH=/signal-cli-${SIGNAL_CLI_VERSION}/bin:$PATH
RUN signal-cli -v # selftest

#
# attachment-mover
#

RUN useradd --uid 1000  user

WORKDIR /app

COPY --from=builder /build/target/quarkus-app/quarkus-run.jar .
COPY --from=builder /build/target/quarkus-app/quarkus quarkus
COPY --from=builder /build/target/quarkus-app/lib lib
COPY --from=builder /build/target/quarkus-app/app app

ADD docker/attachment-mover-java/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

USER user

# selftest
RUN java -jar quarkus-run.jar -h

ENTRYPOINT ["/entrypoint.sh"]