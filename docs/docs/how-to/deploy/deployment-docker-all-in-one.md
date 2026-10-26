---
sidebar_position: 3
---

# All-in-one backend deployment

The root Dockerfile builds a single backend image containing the management API, Analysis API, and evaluation server. The portal is deployed separately.

## Requirements

- Docker Engine 24+
- MariaDB 11.8.9+
- 2-core CPU / 4 GB RAM or better

## 1. Create databases

```sql
CREATE DATABASE IF NOT EXISTS `yl-toggle`;
CREATE DATABASE IF NOT EXISTS `yl-toggle-events`;
```

## 2. Build the image

```bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle
docker build --tag yl-toggle/yl-toggle:3.0.0 .
```

## 3. Start the container

```bash
docker run -p 4008:4008 -p 4006:4006 -p 4007:4007 -p 4011:4011 \
  -e api_server_port=4008 \
  -e api_spring_profiles_active=online \
  -e API_JVM_ARGS='-Xmx2048m -Xms2048m' \
  -e spring.datasource.jdbc-url='jdbc:mariadb://172.24.141.155:13306/yl-toggle?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
  -e spring.datasource.username=root \
  -e spring.datasource.password=root \
  -e app.server-base-urls=http://127.0.0.1:4007 \
  -e app.analysis-base-url=http://127.0.0.1:4006 \
  -e analysis_server_port=4006 \
  -e app.datasource.jdbcUrl='jdbc:mariadb://172.24.141.155:13306/yl-toggle-events' \
  -e app.datasource.username=root \
  -e app.datasource.password=root \
  -e analysis_spring_profiles_active=online \
  -e ANALYSIS_JVM_ARGS='-Xmx2048m -Xms2048m' \
  -e RUST_LOG=info \
  -e YT_SERVER_PORT=4007 \
  -e YT_TOGGLES_URL=http://127.0.0.1:4008/internal/server/toggles \
  -e YT_EVENTS_URL=http://127.0.0.1:4008/internal/server/events \
  -e YT_KEYS_URL=http://127.0.0.1:4008/internal/server/sdk_keys \
  -e YT_ANALYSIS_URL=http://127.0.0.1:4006/events \
  -e YT_REFRESH_SECONDS=10 \
  -e YT_REALTIME_PORT=4011 \
  -e TZ=Asia/Shanghai \
  --name yl-toggle-all-in-one -d yl-toggle/yl-toggle:3.0.0
```

## Verify

```bash
curl http://127.0.0.1:4008/actuator/health
curl http://127.0.0.1:4006/actuator/health
curl http://127.0.0.1:4007/
```

## Deploy the portal

The all-in-one backend image does not include NGINX or the portal. Deploy `yl-toggle/ui:3.0.0` separately and proxy `/api` to port 4008, `/server/api` and `/server/realtime` to ports 4007 and 4011. The repository's `nginx_configs/default.conf` is a complete example.

For the recommended full-stack deployment, use Docker Compose instead.
