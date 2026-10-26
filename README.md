# yl-toggle

yl-toggle is a feature flag and experimentation platform consisting of a management API, evaluation server, analysis API, and web portal.

## Current stack

| Component | Runtime / framework | Version |
|---|---|---|
| Management API | Java + Spring Boot | 25 / 4.1.2 |
| Analysis API | Kotlin + Spring Boot | 2.4.21 / 4.1.2 |
| Evaluation server | Rust + Axum | 1.99.1 / 0.8.9 |
| Portal | React + Vite | 19.3.1 / 8.3.5 |
| Documentation | Docusaurus | 3.10.2 |
| Database | MariaDB | 11.8.9 |

All application components are versioned together as `3.0.0`.

## Quick start

```bash
docker compose up -d --build
```

Open <http://127.0.0.1:4009/> and sign in with:

- username: `Admin`
- password: `Pass1234`

For a two-instance API and server deployment:

```bash
docker compose up -d --build --scale backendAPI=2 --scale serverAPI=2
```

## Verification

```bash
docker compose ps --all
curl http://127.0.0.1:4009/
curl http://127.0.0.1:4006/actuator/health
curl http://127.0.0.1:4009/api/v3/api-docs.yaml
```

## Local development

```bash
# Management API
cd api
mvn verify

# Analysis API
cd ../analysis
mvn verify

# Evaluation server
cd ../server
cargo fmt --check
cargo clippy --all-targets -- -D warnings
cargo test --locked
cargo build --release --offline

# Portal
cd ../ui
npm ci
npm run typecheck
npm run lint
npm test
npm run build

# Documentation
cd ../docs
npm ci
npm run build
```

## Deployment

- [Docker Compose deployment](docs/docs/how-to/deploy/deployment-docker-compose.md)
- [Independent Docker deployment](docs/docs/how-to/deploy/deployment-docker-image.md)
- [Source-code deployment](docs/docs/how-to/deploy/deployment-source-code.md)
- [Helm chart](helm/yl-toggle/)

## Configuration change in v3

The management API and Analysis API now use the MariaDB JDBC scheme by default:

```text
jdbc:mariadb://host:port/database
```

Existing deployments that used `jdbc:mysql://` must update this URL before upgrading.

## License

Apache License 2.0. See [LICENSE](LICENSE) for details.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for the 3.0.0 upgrade details and breaking changes.
