---
sidebar_position: 3
---

# All In One Deploy Guide

> This document describes how to deploy the yl-toggle service using an All-In-One deployment method, where all services are contained within a single container.

## Environment Preparation

* Docker 17+
* Mysql 5.7+
* Recommendation: 2-core CPU/4G memory and above

## Deploy

Running with Docker on Linux/Unix/Mac

**Setps:**

1. Creating yl-toggle Database

   Before deploying the All-In-One yl-toggle service, you need to create the required database in the prepared database service.
   
   ```bash
   CREATE DATABASE IF NOT EXISTS yl-toggle;
   CREATE DATABASE IF NOT EXISTS yl-toggle-events;
   ```

2. Starting yl-toggle Container

   ```bash
   docker run  -p 4008:4008 -p  4006:4006 -p 4007:4007 -p 4011:4011 \
   -e api_server_port=4008 \
   -e api_spring_profiles_active=online \
   -e API_JVM_ARGS='-Xmx2048m -Xms2048m' \
   -e spring.datasource.jdbc-url=jdbc:mysql://172.24.141.155:13306/yl-toggle \
   -e spring.datasource.username=root \
   -e spring.datasource.password=root \
   -e app.server-base-urls=http://127.0.0.1:4007 \
   -e app.analysis-base-url=http://127.0.0.1:4006 \
   -e analysis_server_port=4006 \
   -e app.datasource.jdbcUrl=jdbc:mysql://172.24.141.155:13306/yl-toggle-events \
   -e app.datasource.username=root \
   -e app.datasource.password=root \
   -e analysis_spring_profiles_active=online \
   -e ANALYSIS_JVM_ARGS='-Xmx2048m -Xms2048m' \
   -e RUST_LOG=info \
   -e FP_SERVER_PORT=4007 \
   -e FP_TOGGLES_URL=http://127.0.0.1:4008/internal/server/toggles \
   -e FP_EVENTS_URL=http://127.0.1.0:4008/internal/server/events \
   -e FP_KEYS_URL=http://127.0.0.1:4008/internal/server/sdk_keys \
   -e FP_ANALYSIS_URL=http://127.0.0.1:4006/events \
   -e FP_REFRESH_SECONDS=10 \
   -e FP_REALTIME_PORT=4011 \
   -e TZ=Asia/Shanghai \
   --name ylToggleAll -d yl-toggle/yl-toggle 
   ```

- ***Service Configuration Details***

| Configuration   | Module            | Description              |
| ---------- | ------------------- |-----------------|
| api_server_port                  | yl-toggle API    | The port on which the API server will be started |
| api_spring_profiles_active      | yl-toggle API    | The active profile(s) to be loaded after the configuration file is read |
| API_JVM_ARGS                    | yl-toggle API    | JVM arguments for the API server |
| spring.datasource.jdbc-url      | yl-toggle API    | The URL of the database to be used by the API server |
| spring.datasource.username      | yl-toggle API    | The username to be used to connect to the database for the API server
spring.datasource.password		|
| spring.datasource.password      | yl-toggle API    | The password to be used to connect to the database for the API server |
| app.server-base-urls            | yl-toggle API    | The address(es) of the server(s) on which the API will be deployed. Multiple addresses can be separated by commas. (For multi-instance deployment, it is recommended to configure the gateway address)            |
| app.analysis-base-url           | yl-toggle API    | The address of the analysis service, currently only supports a single address. (For multi-instance deployment, it is recommended to configure the gateway address)           |
|      |    |          |
| analysis_server_port            | yl-toggle Analysis         | The port on which the analysis server will be started             |
| app.datasource.jdbcUrl          | yl-toggle Analysis         | The URL of the database to be used by the analysis server             |
| app.datasource.username         | yl-toggle Analysis         | The username to be used to connect to the database for the analysis server           |
| app.datasource.password         | yl-toggle Analysis         | The password to be used to connect to the database for the analysis server            |
| analysis_spring_profiles_active | yl-toggle Analysis         | The active profile(s) to be loaded after the configuration file is read        |
| ANALYSIS_JVM_ARGS               | yl-toggle Analysis         | JVM arguments for the analysis server               |
|      |    |          |
| RUST_LOG                        | yl-toggle Server         | Log level             |
| FP_SERVER_PORT                  | yl-toggle Server         | The port on which the Server will be started             |
| FP_TOGGLES_URL                  | yl-toggle Server         | toggle configuration acquisition url (API) |
| FP_EVENTS_URL                   | yl-toggle Server         | Event reporting url (API)     |
| FP_KEYS_URL                     | yl-toggle Server         | Key acquisition url (API)    |
| FP_ANALYSIS_URL                 | yl-toggle Server         | Metric events reporting url (Analysis)             |
| FP_REFRESH_SECONDS              | yl-toggle Server         | toggle refresh time             |
| FP_REALTIME_PORT                | yl-toggle Server         | Long connection server port            |
| TZ                              | All                         | Container time zone            |


3. Go to UI/Portal at `http://localhost:4008` and use the default credentials to log in.
   - username: `admin`
   - password: `Pass1234`