---
sidebar_position: 2
---

# API

## Requirements

- JDK 25
- Maven 3.9+
- MariaDB 11.8.9+

## Get the source code

```shell
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle/api
```

## Configure the database

Create a MariaDB database named `yl-toggle`. Flyway creates and migrates the schema when the API starts.

Update `admin/src/main/resources/config/application.yml`, or provide Spring environment variables:

```shell
export SPRING_DATASOURCE_JDBC_URL='jdbc:mariadb://127.0.0.1:3306/yl-toggle?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
```

The default API port is `8080`.

## Compile and test

The API is a multi-module Maven project. The web application is in the `admin` module.

```shell
mvn verify
```

Maven no longer builds the UI; frontend development and builds are performed in `../ui`.

## Run locally

```shell
mvn -pl admin -am spring-boot:run
```

Open [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) to verify startup.

For a production-style package:

```shell
mvn -pl admin -am package
java -jar admin/target/api.jar
```
