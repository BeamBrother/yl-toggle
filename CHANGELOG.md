# Changelog

## 3.0.0 - 2026-10-26

yl-toggle 3.0.0 is a unified runtime and dependency modernization release. Application functionality, REST paths, SDK key semantics, and database table structures are unchanged.

### Added

- Source-based multi-stage Docker builds for the management API, Analysis API, evaluation server, and portal.
- A complete Docker Compose stack with MariaDB 11.8.9, Redoc, health checks, and scalable API/server services.
- GitHub Actions CI for API, Analysis, Rust, UI, docs, Helm, and Compose validation.
- Vite 8.3.5 configuration and a fresh React 19 test/snapshot setup.
- A thin local Java SDK artifact that avoids conflicting embedded Jackson classes.
- Bounded, retrying event buffering between the evaluation server and ingestion services.
- Helm chart `appVersion` alignment and image-tag defaults based on the chart app version.

### Changed

- Unified all application release versions to `3.0.0`.
- Upgraded the management API to Java 25 and Spring Boot 4.1.2.
- Upgraded Analysis to Kotlin 2.4.21 and Spring Boot 4.1.2.
- Upgraded the evaluation server to Rust 1.99.1 and Axum 0.8.9.
- Upgraded the portal to React 19.3.1 and Vite 8.3.5.
- Upgraded docs to Docusaurus 3.10.2.
- Replaced CRA/CRaco with Vite.
- Migrated JVM packages and Spring Security configuration to Jakarta and Spring Security 7 APIs.
- Migrated JDBC URLs from `jdbc:mysql://` to `jdbc:mariadb://`.
- Updated Docker base images to Java 25, Rust 1.99.1 Trixie, Node 24.13.1, NGINX Alpine 3.24, and MariaDB 11.8.9.
- Refreshed Rust vendored dependencies for offline release builds.
- Updated deployment, database, local-development, configuration, and Helm documentation for the v3 stack.

### Fixed

- Flyway is explicitly enabled under Spring Boot 4 modular dependencies.
- Kotlin data classes deserialize correctly with the Jackson 3 Kotlin module.
- The evaluation server now emits the distinct payload shapes expected by backendAPI and Analysis.
- Rust tests use ephemeral localhost ports instead of collision-prone fixed ports.
- UI snapshots no longer depend on random UUID values.
- The API integration-test database and pinned Maven wrapper now match the v3 toolchain.
- Portal syntax-highlighting dependencies are split into bounded chunks instead of one large shared asset.
- Documentation anchors and Docusaurus markdown configuration are upgraded, and blog posts define stable truncation excerpts.
- Redoc is served through the portal at `/api-docs`, and NGINX now proxies the Redoc container's internal port correctly.

### Breaking

- Custom deployments must change JDBC URLs from `jdbc:mysql://` to `jdbc:mariadb://`.
- Java services now require Java 25.
- Portal development requires Node.js 24.13.1.
- The evaluation server uses `YT_*` environment variable names.

### Validation

- API: 198 tests passed.
- Analysis: 10 tests passed.
- Rust: fmt, clippy, tests, and offline release build passed.
- UI: typecheck, lint, 81 tests, production build, and high/critical audit passed.
- Docs: typecheck, audit, and bilingual production build passed.
- Compose: config, source builds, scaled healthy deployment, API/UI/Analysis/OpenAPI/realtime checks, and event persistence passed.
- Helm: lint and template rendering passed.
