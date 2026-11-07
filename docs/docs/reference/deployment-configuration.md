---
sidebar_position: 8
---

# Deployment configuration

## Management API

| Environment variable | Default | Required | Description |
|---|---:|---:|---|
| `server.port` | `8080` | No | HTTP port |
| `TZ` | `Asia/Shanghai` | No | Container timezone |
| `spring.datasource.jdbc-url` | - | Yes | MariaDB JDBC URL, for example `jdbc:mariadb://database:13306/yl-toggle` |
| `spring.datasource.username` | - | Yes | Database username |
| `spring.datasource.password` | - | Yes | Database password |
| `spring.profiles.active` | `online` | No | Active Spring profile |
| `logging.level.root` | `INFO` | No | Root log level |
| `spring.jpa.show-sql` | `false` | No | Print SQL statements |
| `app.analysis-base-url` | `http://127.0.0.1:4006` | No | Analysis API base URL |
| `app.server-base-urls` | `http://127.0.0.1:4007` | No | Comma-separated evaluation server URLs |
| `app.security.jwt.keystore-location` | `./jwt.jks` | No | JWT keystore path |
| `app.security.jwt.keystore-password` | `password` | No | Keystore password |
| `app.security.jwt.private-key-passphrase` | `password` | No | Private-key passphrase |

### JWT keystore

The management API uses JWT with an RSA key. Generate a production-specific key:

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

Mount the keystore and override the `app.security.jwt.*` values.

## Analysis API

| Environment variable | Default | Required | Description |
|---|---:|---:|---|
| `server.port` | `4006` | No | HTTP port |
| `app.datasource.jdbcUrl` | - | Yes | MariaDB JDBC URL for `yl-toggle-events` |
| `app.datasource.username` | - | Yes | Database username |
| `app.datasource.password` | - | Yes | Database password |
| `spring.profiles.active` | `online` | No | Active Spring profile |
| `JVM_ARGS` | - | No | JVM arguments, such as `-Xmx2048m -Xms2048m` |
| `TZ` | `Asia/Shanghai` | No | Container timezone |

## Evaluation server

| Environment variable | Default | Required | Description |
|---|---:|---:|---|
| `YT_SERVER_PORT` | `4007` | No | HTTP port |
| `YT_REALTIME_PORT` | `4011` | No | Realtime port |
| `YT_TOGGLES_URL` | - | Yes | Management API toggle endpoint |
| `YT_EVENTS_URL` | - | Yes | Management API event endpoint |
| `YT_KEYS_URL` | - | Yes | Management API SDK-key endpoint |
| `YT_ANALYSIS_URL` | - | Yes | Analysis API event endpoint |
| `YT_REFRESH_SECONDS` | `3` | No | Configuration refresh interval |
| `RUST_LOG` | `info` | No | Log level |
| `YT_EVENTS_BUFFER_CAPACITY` | `100000` | No | Maximum queued events |
| `YT_EVENTS_MAX_BATCH` | `500` | No | Maximum events per upstream batch |
| `YT_EVENTS_FLUSH_MS` | `200` | No | Event flush interval |
| `YT_EVENTS_RETRIES` | `3` | No | Upstream event retry attempts |

When Docker or a process supervisor is used, pass these values as environment variables. When running a JAR directly, pass the Spring properties with `--property=value`.
