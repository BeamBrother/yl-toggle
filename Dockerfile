# Legacy all-in-one source build for API, Analysis, and Rust Server.
FROM rust:1.99.1-slim-trixie AS server-build
WORKDIR /src
COPY server/ ./
RUN cargo build --release --offline --bin yl-toggle-server

FROM maven:3.9-eclipse-temurin-25 AS api-build
WORKDIR /src
COPY api/ ./
RUN mvn -B -DskipTests clean package

FROM maven:3.9-eclipse-temurin-25 AS analysis-build
WORKDIR /src
COPY analysis/ ./
RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y --no-install-recommends bash curl tzdata && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY scripts/start.sh /usr/local/bin/start.sh
COPY --from=server-build /src/target/release/yl-toggle-server /usr/local/bin/yl-toggle-server
COPY --from=api-build /src/admin/target/api.jar /usr/local/bin/api.jar
COPY --from=analysis-build /src/target/analysis.jar /usr/local/bin/analysis.jar
ENV TZ=Asia/Shanghai
ENTRYPOINT ["/usr/local/bin/start.sh"]
