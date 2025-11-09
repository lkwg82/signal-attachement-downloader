FROM eclipse-temurin:21.0.9_10-jdk AS builder

#
# build jre
#
RUN jlink --add-modules ALL-MODULE-PATH --output jre \
          --strip-debug --no-man-pages --no-header-files --compress zip-9 \
    && find /jre | xargs -P100 -n1 touch -mt 197001010101 -at 197001010101 \
    && find /jre | xargs -P100 -n1 chmod 0755 \
    && jre/bin/java -version # selftest

#
# build app
#
RUN mkdir /build
WORKDIR /build

COPY .m2_repo .m2_repo
RUN ls -asl .m2_repo
RUN du -sh .m2_repo
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

ARG mvn="./mvnw -Dmaven.repo.local=.m2_repo"

RUN $mvn clean verify
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

COPY docker/attachment-mover-java/entrypoint.sh /entrypoint.sh

RUN useradd --uid 1000  user
USER user

# selftest
RUN java -jar quarkus-run.jar -h

ENTRYPOINT ["/entrypoint.sh"]