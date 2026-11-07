---
sidebar_position: 8
---

# 部署配置

## 管理 API

| 环境变量 | 默认值 | 必填 | 说明 |
|---|---:|---:|---|
| `server.port` | `8080` | 否 | HTTP 端口 |
| `TZ` | `Asia/Shanghai` | 否 | 容器时区 |
| `spring.datasource.jdbc-url` | - | 是 | MariaDB JDBC URL，例如 `jdbc:mariadb://database:13306/yl-toggle` |
| `spring.datasource.username` | - | 是 | 数据库用户名 |
| `spring.datasource.password` | - | 是 | 数据库密码 |
| `spring.profiles.active` | `online` | 否 | Spring profile |
| `logging.level.root` | `INFO` | 否 | 根日志级别 |
| `spring.jpa.show-sql` | `false` | 否 | 是否打印 SQL |
| `app.analysis-base-url` | `http://127.0.0.1:4006` | 否 | Analysis API 基础地址 |
| `app.server-base-urls` | `http://127.0.0.1:4007` | 否 | 评估服务地址，多个地址用逗号分隔 |
| `app.security.jwt.keystore-location` | `./jwt.jks` | 否 | JWT keystore 路径 |
| `app.security.jwt.keystore-password` | `password` | 否 | Keystore 密码 |
| `app.security.jwt.private-key-passphrase` | `password` | 否 | 私钥密码 |

### JWT keystore

管理 API 使用 JWT 与 RSA 密钥。生产环境请生成专用密钥：

```bash
keytool -genkey \
  -alias my-yl-toggle-jwt \
  -keyalg RSA \
  -keysize 2048 \
  -keystore yl-toggle-jwt.jks \
  -validity 365 \
  -keypass YOUR_PRIVATE_KEY_PASSPHRASE \
  -storepass YOUR_KEYSTORE_PASSWORD
```

挂载该 keystore，并覆盖 `app.security.jwt.*` 配置。

## Analysis API

| 环境变量 | 默认值 | 必填 | 说明 |
|---|---:|---:|---|
| `server.port` | `4006` | 否 | HTTP 端口 |
| `app.datasource.jdbcUrl` | - | 是 | `yl-toggle-events` 的 MariaDB JDBC URL |
| `app.datasource.username` | - | 是 | 数据库用户名 |
| `app.datasource.password` | - | 是 | 数据库密码 |
| `spring.profiles.active` | `online` | 否 | Spring profile |
| `JVM_ARGS` | - | 否 | JVM 参数，例如 `-Xmx2048m -Xms2048m` |
| `TZ` | `Asia/Shanghai` | 否 | 容器时区 |

## 评估服务

| 环境变量 | 默认值 | 必填 | 说明 |
|---|---:|---:|---|
| `YT_SERVER_PORT` | `4007` | 否 | HTTP 端口 |
| `YT_REALTIME_PORT` | `4011` | 否 | Realtime 端口 |
| `YT_TOGGLES_URL` | - | 是 | 管理 API 开关配置 endpoint |
| `YT_EVENTS_URL` | - | 是 | 管理 API 事件上报 endpoint |
| `YT_KEYS_URL` | - | 是 | 管理 API SDK key endpoint |
| `YT_ANALYSIS_URL` | - | 是 | Analysis API 事件 endpoint |
| `YT_REFRESH_SECONDS` | `3` | 否 | 配置刷新间隔 |
| `RUST_LOG` | `info` | 否 | 日志级别 |
| `YT_EVENTS_BUFFER_CAPACITY` | `100000` | 否 | 事件队列最大容量 |
| `YT_EVENTS_MAX_BATCH` | `500` | 否 | 单批上报最大事件数 |
| `YT_EVENTS_FLUSH_MS` | `200` | 否 | 事件 flush 间隔 |
| `YT_EVENTS_RETRIES` | `3` | 否 | 上游上报重试次数 |

使用 Docker 或进程管理器时，通过环境变量传入。直接运行 JAR 时，通过 `--property=value` 传入 Spring 配置。
