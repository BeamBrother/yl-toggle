---
sidebar_position: 6
---

# 初始化数据库

yl-toggle 3.0.0 使用 MariaDB 11.8.9。

管理 API 与 Analysis API 使用两个独立数据库：

- `yl-toggle`
- `yl-toggle-events`

## 创建数据库

连接 MariaDB 后执行：

```sql
CREATE DATABASE IF NOT EXISTS `yl-toggle`;
CREATE DATABASE IF NOT EXISTS `yl-toggle-events`;
```

## Flyway 自动迁移

两个服务均使用 Flyway，启动时会自动执行数据库迁移，无需手工建表。

- 管理 API migration：`api/admin/src/main/resources/db/migration`
- Analysis API migration：`analysis/src/main/resources/db/migration`

Flyway 会在 `flyway_schema_history` 表中记录已执行版本。

## 手工导入

不推荐手工导入，因为这样会导致 Flyway 无法自动追踪后续表结构升级。如确需手工导入，请先禁用 Flyway：

```bash
--spring.flyway.enabled=false
```

生成管理 API SQL：

```bash
cd api/admin/src/main/resources/db/migration
find . -name 'V*.sql' | sort -V | xargs cat > rollup.sql
mariadb --host {DATABASE_HOST} --port {DATABASE_PORT} --user root --password yl-toggle < rollup.sql
```

生成 Analysis SQL：

```bash
cd analysis/src/main/resources/db/migration
find . -name 'V*.sql' | sort -V | xargs cat > rollup.sql
mariadb --host {DATABASE_HOST} --port {DATABASE_PORT} --user root --password yl-toggle-events < rollup.sql
```

验证表：

```sql
SHOW TABLES;
```
