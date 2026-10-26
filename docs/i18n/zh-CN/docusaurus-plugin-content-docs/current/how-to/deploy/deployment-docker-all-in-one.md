---
sidebar_position: 3
---

# All-in-one 后端部署

根目录 Dockerfile 会构建一个包含管理 API、Analysis API 和评估服务的单一后端镜像。Portal 需要单独部署。

## 环境要求

- Docker Engine 24+
- MariaDB 11.8.9+
- 建议 2 核 CPU / 4 GB 内存及以上

## 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS `yl-toggle`;
CREATE DATABASE IF NOT EXISTS `yl-toggle-events`;
```

## 2. 构建镜像

```bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle
docker build --tag yl-toggle/yl-toggle:3.0.0 .
```

## 3. 启动容器

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

## 验证

```bash
curl http://127.0.0.1:4008/actuator/health
curl http://127.0.0.1:4006/actuator/health
curl http://127.0.0.1:4007/
```

## 部署 Portal

All-in-one 后端镜像不包含 NGINX 和 Portal。请单独部署 `yl-toggle/ui:3.0.0`，并将 `/api` 代理到 4008，将 `/server/api` 和 `/server/realtime` 代理到 4007 与 4011。仓库中的 `nginx_configs/default.conf` 是完整示例。

推荐的全栈部署方式是 Docker Compose。
