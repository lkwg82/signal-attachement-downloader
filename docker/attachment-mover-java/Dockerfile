FROM eclipse-temurin:17-jdk as builder

#
# build jre
#
RUN jlink --add-modules ALL-MODULE-PATH --output jre \
          --strip-debug --no-man-pages --no-header-files --compress=2
RUN jre/bin/java -version # selftest

RUN find /jre | xargs -P100 -n1 touch -mt 197001010101 -at 197001010101
RUN find /jre | xargs -P100 -n1 chmod 0755

#
# build app
#
RUN mkdir /build
WORKDIR /build

COPY .m2_repo .m2_repo

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

ARG mvn="./mvnw -Dmaven.repo.local=.m2_repo"

RUN $mvn -Dmaven.repo.local=.m2_repo clean verify
RUN java -jar target/quarkus-app/quarkus-run.jar -h

WORKDIR /build/target

RUN find | xargs -P100 -n1 touch -mt 197001010101 -at 197001010101
RUN find | xargs -P100 -n1 chmod 0755

FROM debian:stable-slim

COPY --from=builder /jre /jre
ENV JAVA_HOME=/jre
ENV PATH=$PATH:/jre/bin
RUN java -version # selftest


WORKDIR /app

COPY --from=builder /build/target/quarkus-app/quarkus-run.jar .
COPY --from=builder /build/target/quarkus-app/quarkus quarkus
COPY --from=builder /build/target/quarkus-app/lib lib
COPY --from=builder /build/target/quarkus-app/app app

ADD docker/attachment-mover-java/entrypoint.sh /entrypoint.sh

RUN useradd --uid 1000  user
USER user

# selftest
RUN java -jar quarkus-run.jar -h

ENTRYPOINT ["/entrypoint.sh"]