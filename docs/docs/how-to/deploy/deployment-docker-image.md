---
sidebar_position: 2
---

# Independent Docker deployment

This guide deploys each yl-toggle service with its own container.

## Requirements

- Docker Engine 24+
- MariaDB 11.8.9+
- Production environments should use at least three nodes
- 2-core CPU / 4 GB RAM or better per node

## Deployment topology

| Sample host | Service | Port |
|---|---|---|
| 10.100.1.1 | yl-toggle API | 4008 |
| 10.100.1.1 | yl-toggle UI | 4009 |
| 10.100.1.2 | yl-toggle Server | 4007, 4011 |
| 10.100.1.3 | yl-toggle Analysis | 4006 |
| 10.100.1.4 | MariaDB | 13306 |

## 1. Start MariaDB

```bash
docker run -p 13306:13306 \
  -e MYSQL_TCP_PORT=13306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=yl-toggle \
  --name database -d mariadb:11.8.9
```

Create the Analysis database:

```sql
CREATE DATABASE IF NOT EXISTS `yl-toggle-events`;
```

## 2. Start the management API

```bash
docker run -p 4008:4008 \
  -e server.port=4008 \
  -e spring.datasource.jdbc-url='jdbc:mariadb://10.100.1.4:13306/yl-toggle?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
  -e spring.datasource.username=root \
  -e spring.datasource.password=root \
  -e app.analysis-base-url=http://10.100.1.3:4006 \
  -e app.server-base-urls=http://10.100.1.2:4007 \
  --name yl-toggle-api -d yl-toggle/api:3.0.0
```

## 3. Start the evaluation server

```bash
docker run -p 4007:4007 -p 4011:4011 \
  -e YT_SERVER_PORT=4007 \
  -e YT_REALTIME_PORT=4011 \
  -e YT_TOGGLES_URL=http://10.100.1.1:4008/internal/server/toggles \
  -e YT_EVENTS_URL=http://10.100.1.1:4008/internal/server/events \
  -e YT_KEYS_URL=http://10.100.1.1:4008/internal/server/sdk_keys \
  -e YT_ANALYSIS_URL=http://10.100.1.3:4006/events \
  --name yl-toggle-server -d yl-toggle/server:3.0.0
```

## 4. Start the analysis API

```bash
docker run -p 4006:4006 \
  -e server.port=4006 \
  -e app.datasource.jdbcUrl='jdbc:mariadb://10.100.1.4:13306/yl-toggle-events' \
  -e app.datasource.username=root \
  -e app.datasource.password=root \
  -e spring.profiles.active=online \
  -e JVM_ARGS='-Xmx2048m -Xms2048m' \
  -e TZ=Asia/Shanghai \
  --name yl-toggle-analysis -d yl-toggle/analysis:3.0.0
```

## 5. Start the UI

Mount an NGINX configuration that proxies `/api`, `/server/api`, and `/server/realtime` to the correct services. You can start from [`nginx_configs/default.conf`](https://github.com/yl-toggle/yl-toggle/blob/main/nginx_configs/default.conf).

```bash
docker run -p 4009:4009 \
  -v /my_custom/default.conf:/etc/nginx/conf.d/default.conf \
  --name yl-toggle-ui -d yl-toggle/ui:3.0.0
```

## Verify

```bash
curl http://{API_HOST}:4008/actuator/health
curl http://{ANALYSIS_HOST}:4006/actuator/health
curl http://{UI_HOST}:4009/
```

Open `http://{UI_HOST}:4009/` and sign in with:

- username: `Admin`
- password: `Pass1234`
