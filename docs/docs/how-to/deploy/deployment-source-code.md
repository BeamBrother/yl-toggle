---
sidebar_position: 3
---

# Source-code deployment

This guide builds and deploys each yl-toggle service from source.

## Requirements

- JDK 25
- Maven 3.9+
- Rust 1.99.1
- Node.js 24.13.1 with npm
- MariaDB 11.8.9+
- NGINX for the portal

## Example topology

| Host | Service | Port |
|---|---|---|
| 10.100.1.1 | Management API | 4008 |
| 10.100.1.2 | Evaluation server | 4007, 4011 |
| 10.100.1.3 | Analysis API | 4006 |
| 10.100.1.1 | Portal / NGINX | 4009 |
| 10.100.1.4 | MariaDB | 13306 |

## Create databases

```sql
CREATE DATABASE IF NOT EXISTS `yl-toggle`;
CREATE DATABASE IF NOT EXISTS `yl-toggle-events`;
```

Flyway creates and migrates the management API and Analysis API schemas on first startup.

## Management API

Build and run:

```bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle/api
mvn verify
java -jar admin/target/api.jar \
  --server.port=4008 \
  --spring.datasource.jdbc-url='jdbc:mariadb://10.100.1.4:13306/yl-toggle?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
  --spring.datasource.username=root \
  --spring.datasource.password=root \
  --app.analysis-base-url=http://10.100.1.3:4006 \
  --app.server-base-urls=http://10.100.1.2:4007
```

Verify:

```bash
curl http://10.100.1.1:4008/actuator/health
```

## Analysis API

Build and run:

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

Verify:

```bash
curl http://10.100.1.3:4006/actuator/health
```

## Evaluation server

The repository pins Rust 1.99.1 in `server/rust-toolchain.toml`.

Build:

```bash
cd yl-toggle/server
cargo build --release --locked
```

Run:

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

Verify:

```bash
curl http://10.100.1.2:4007/
```

## Portal

Build production assets:

```bash
cd yl-toggle/ui
npm ci
npm run build
```

The output is generated in `build/`.

### Optional base path

For a deployment below a path such as `/yl-toggle/`, add `base` to `vite.config.ts`:

```ts
export default defineConfig({
  base: '/yl-toggle/',
  // ...
});
```

For a CDN, set `base` to the CDN URL. Re-run `npm run build`.

### NGINX

Create `/etc/nginx/conf.d/yl-toggle.conf`. Adjust the upstream addresses for your environment.

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

Copy `build/` to `/usr/share/nginx/html/` and reload NGINX:

```bash
sudo nginx -t
sudo nginx -s reload
```

## Verify the installation

Open `http://10.100.1.1:4009/` and sign in with:

- username: `Admin`
- password: `Pass1234`

Server-side SDKs use the NGINX host and the `/server` path, for example `http://10.100.1.1:4009/server`.
