# The jar is prebuilt by Maven; keeping source out of the runtime image makes
# builds deterministic and avoids apk/network access. BusyBox wget is used by
# the Compose health check.
FROM openjdk:8-jre-alpine

ENV JVM_ARGS=

COPY admin/target/api.jar /yl-toggle-api.jar

ENTRYPOINT ["sh", "-c", "exec java $JVM_ARGS -jar /yl-toggle-api.jar"]

