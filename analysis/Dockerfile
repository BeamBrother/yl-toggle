# The jar is prebuilt by Maven; no package installation is needed at build time.
FROM openjdk:8-jre-alpine

ENV JVM_ARGS=

COPY target/analysis.jar /yl-toggle-analysis.jar

ENTRYPOINT ["sh", "-c", "exec java $JVM_ARGS -jar /yl-toggle-analysis.jar"]

