FROM alpine:3.13 as builder

ADD target/quarkus-app /app
WORKDIR /app

RUN find | xargs -P100 -n1 touch -mt 197001010101 -at 197001010101
RUN find | xargs -P100 -n1 chmod 0755

FROM adoptopenjdk:16-jre-openj9

RUN adduser --uid 1000  user

WORKDIR /app

COPY --from=builder /app/quarkus-run.jar .
COPY --from=builder /app/quarkus quarkus
COPY --from=builder /app/lib lib
COPY --from=builder /app/app app

USER user

# selftest
RUN java -jar quarkus-run.jar -h

ENTRYPOINT ["java","-jar","quarkus-run.jar"]