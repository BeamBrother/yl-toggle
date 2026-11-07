---
sidebar_position: 3
---

# 源码编译部署

本文介绍如何从源码编译并部署 yl-toggle 各服务。

## 环境要求

- JDK 25
- Maven 3.9+
- Rust 1.99.1
- Node.js 24.13.1 及 npm
- MariaDB 11.8.9+
- Portal 使用 NGINX

## 示例部署拓扑

| 机器 | 服务 | 端口 |
|---|---|---|
| 10.100.1.1 | 管理 API | 4008 |
| 10.100.1.2 | 评估服务 | 4007、4011 |
| 10.100.1.3 | Analysis API | 4006 |
| 10.100.1.1 | Portal / NGINX | 4009 |
| 10.100.1.4 | MariaDB | 13306 |

## 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS `yl-toggle`;
CREATE DATABASE IF NOT EXISTS `yl-toggle-events`;
```

管理 API 与 Analysis API 首次启动时，Flyway 会自动创建并迁移表结构。

## 管理 API

编译并运行：

```bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle/api
./mvnw verify  # Windows：.\\mvnw.cmd verify
java -jar admin/target/api.jar \
  --server.port=4008 \
  --spring.datasource.jdbc-url='jdbc:mariadb://10.100.1.4:13306/yl-toggle?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
  --spring.datasource.username=root \
  --spring.datasource.password=root \
  --app.analysis-base-url=http://10.100.1.3:4006 \
  --app.server-base-urls=http://10.100.1.2:4007
```

验证：

```bash
curl http://10.100.1.1:4008/actuator/health
```

## Analysis API

编译并运行：

```bash
cd yl-toggle/analysis
mvn verify
java -jar target/analysis.jar \
  --server.port=4006 \
  --app.datasource.jdbcUrl='jdbc:mariadb://10.100.1.4:13306/yl-toggle-events' \
  --app.datasource.username=root \
  --app.datasource.password=root \
  --spring.profiles.active=online
```

验证：

```bash
curl http://10.100.1.3:4006/actuator/health
```

## 评估服务

仓库通过 `server/rust-toolchain.toml` 锁定 Rust 1.99.1。

编译：

```bash
cd yl-toggle/server
cargo build --release --locked
```

运行：

```bash
export YT_SERVER_PORT=4007
export YT_REALTIME_PORT=4011
export YT_TOGGLES_URL=http://10.100.1.1:4008/internal/server/toggles
export YT_EVENTS_URL=http://10.100.1.1:4008/internal/server/events
export YT_KEYS_URL=http://10.100.1.1:4008/internal/server/sdk_keys
export YT_ANALYSIS_URL=http://10.100.1.3:4006/events
export YT_REFRESH_SECONDS=3
./target/release/yl-toggle-server
```

验证：

```bash
curl http://10.100.1.2:4007/
```

## Portal

构建生产产物：

```bash
cd yl-toggle/ui
npm ci
npm run build
```

构建产物输出到 `build/`。

### 可选 base path

如需部署在 `/yl-toggle/` 这类子路径下，在 `vite.config.ts` 中增加 `base`：

```ts
export default defineConfig({
  base: '/yl-toggle/',
  // ...
});
```

如使用 CDN，可将 `base` 配置为 CDN 地址。修改后重新执行 `npm run build`。

### NGINX

创建 `/etc/nginx/conf.d/yl-toggle.conf`，并按实际环境调整 upstream 地址。

```nginx
upstream yl_toggle_api {
    server 10.100.1.1:4008;
}

upstream yl_toggle_server {
    server 10.100.1.2:4007;
}

upstream yl_toggle_realtime {
    ip_hash;
    server 10.100.1.2:4011;
}

server {
    listen 4009;

    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri /index.html;
    }

    location /api {
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Host $http_host;
        proxy_pass http://yl_toggle_api;
    }

    location /server/api {
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Host $http_host;
        proxy_pass http://yl_toggle_server;
    }

    location /server/realtime {
        proxy_pass http://yl_toggle_realtime;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "Upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

将 `build/` 复制到 `/usr/share/nginx/html/`，并重载 NGINX：

```bash
sudo nginx -t
sudo nginx -s reload
```

## 验证安装

打开 `http://10.100.1.1:4009/`，使用以下账号登录：

- username: `Admin`
- password: `Pass1234`

Server Side SDK 使用 NGINX 机器地址加 `/server` 路径，例如 `http://10.100.1.1:4009/server`。
