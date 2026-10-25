# Multi-stage source build for the Kotlin analysis API.
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /src
COPY . ./
RUN --mount=type=cache,target=/root/.m2/repository mvn -B -DskipTests clean package

FROM eclipse-temurin:25-jre-alpine
ENV JVM_ARGS=
WORKDIR /app
COPY --from=build /src/target/analysis.jar /app/analysis.jar
EXPOSE 4006
ENTRYPOINT ["sh", "-c", "exec java $JVM_ARGS -jar /app/analysis.jar"]
