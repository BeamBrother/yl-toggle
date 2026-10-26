---
sidebar_position: 6
---

# Database setup

yl-toggle 3.0.0 uses MariaDB 11.8.9.

The management API and Analysis API use separate databases:

- `yl-toggle`
- `yl-toggle-events`

## Create databases

Connect to MariaDB and run:

```sql
CREATE DATABASE IF NOT EXISTS `yl-toggle`;
CREATE DATABASE IF NOT EXISTS `yl-toggle-events`;
```

## Automatic Flyway migration

Both services use Flyway and migrate their schemas automatically on startup. No manual table creation is required.

- Management API migrations: `api/admin/src/main/resources/db/migration`
- Analysis API migrations: `analysis/src/main/resources/db/migration`

Flyway records applied versions in `flyway_schema_history`.

## Manual import

Manual migration is not recommended because it prevents Flyway from tracking schema history automatically. If you must import manually, first disable Flyway:

```bash
--spring.flyway.enabled=false
```

Generate the management API SQL rollup:

```bash
cd api/admin/src/main/resources/db/migration
find . -name 'V*.sql' | sort -V | xargs cat > rollup.sql
mariadb --host {DATABASE_HOST} --port {DATABASE_PORT} --user root --password yl-toggle < rollup.sql
```

Generate the Analysis SQL rollup:

```bash
cd analysis/src/main/resources/db/migration
find . -name 'V*.sql' | sort -V | xargs cat > rollup.sql
mariadb --host {DATABASE_HOST} --port {DATABASE_PORT} --user root --password yl-toggle-events < rollup.sql
```

Verify tables:

```sql
SHOW TABLES;
```
