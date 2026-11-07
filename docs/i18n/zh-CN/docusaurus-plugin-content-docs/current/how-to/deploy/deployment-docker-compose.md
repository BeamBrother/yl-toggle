---
sidebar_position: 1
---

# Docker Compose 部署

本文介绍如何使用 Docker Compose 从源码启动完整的 yl-toggle 服务。

## 环境要求

- Docker Engine 24+ 及 Compose v2
- 建议 2 核 CPU / 4 GB 内存及以上

## 部署

```bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle
docker compose up -d --build
```

Compose 会构建以下本地镜像：

- `yl-toggle/api:3.0.0`
- `yl-toggle/server:3.0.0`
- `yl-toggle/ui:3.0.0`
- `yl-toggle/analysis:3.0.0`

同时会启动 MariaDB 11.8.9 和 Redoc。

## 扩容 API 与评估服务

```bash
docker compose up -d --build --scale backendAPI=2 --scale serverAPI=2
```

## 验证

```bash
docker compose ps --all
curl http://127.0.0.1:4009/
curl http://127.0.0.1:4006/actuator/health
curl http://127.0.0.1:4009/api/v3/api-docs.yaml
# Redoc UI: http://127.0.0.1:4009/api-docs
```

打开 [http://127.0.0.1:4009/](http://127.0.0.1:4009/)，使用以下账号登录：

- username: `Admin`
- password: `Pass1234`

## 重置本地环境

本地数据库数据保存在 named volume 中。如需重建：

```bash
docker compose down -v
docker compose up -d --build
```

:::caution
`docker compose down -v` 会删除本地 MariaDB volume 及其中的全部数据。
:::
