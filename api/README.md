# yl-toggle API

[![codecov](https://codecov.io/gh/yl-toggle/yl-toggle/api/branch/main/graph/badge.svg?token=JVJWQUUIJH)](https://app.codecov.io/github/yl-toggle/yl-toggle/tree/main/api)
[![Docker Pulls](https://img.shields.io/docker/pulls/yl-toggle/api)](https://hub.docker.com/u/yl-toggle)
[![Apache-2.0 license](https://img.shields.io/github/license/yl-toggle/yl-toggle)](https://github.com/yl-toggle/yl-toggle/blob/main/LICENSE)

The yl-toggle management API exposes the REST API used by the portal and administrative clients. Version 3.0.0 targets Java 25 and Spring Boot 4.1.2.

## [中文文档](https://docs.yl-toggle.io/zh-CN/)

## Requirements

- Java 25
- Maven 3.9+
- MariaDB 11.8.9+
- Docker Engine 24+ with Compose v2 for the example deployment

## Run all services with Docker Compose

```bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle
docker compose up -d --build
```

The complete stack includes the management API, analysis API, evaluation server, portal, MariaDB, and Redoc.

## Run the API image independently

```bash
docker run -d -p 4008:4008 \
  -e spring.datasource.jdbc-url='jdbc:mariadb://database:13306/yl-toggle?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
  -e spring.datasource.username=root \
  -e spring.datasource.password=root \
  -e app.analysis-base-url=http://analysis-api:4006 \
  -e app.server-base-urls=http://server-api:4007 \
  --name yl-toggle-api \
  yl-toggle/api:3.0.0
```

Replace the database and service URLs with values for your environment.

## Build and run from source

```bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle/api
mvn verify
java -jar admin/target/api.jar
```

The default local port is `8080`. Override it with `--server.port=4008` if needed.

## Build a custom image

```bash
docker build --tag my-yl-toggle-api:3.0.0 api
```

## API reference

After starting Compose, open:

- Redoc UI: <http://127.0.0.1:4009/api-docs>
- OpenAPI YAML: <http://127.0.0.1:4009/api/v3/api-docs.yaml>

## License

This project is licensed under the Apache 2.0 License. See the repository [LICENSE](../LICENSE) file for details.
