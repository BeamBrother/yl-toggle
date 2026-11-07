---
sidebar_position: 1
---

# Docker Compose deployment

This guide starts the complete yl-toggle stack from source with Docker Compose.

## Requirements

- Docker Engine 24+ with Compose v2
- 2-core CPU / 4 GB RAM or better

## Deploy

```bash
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle
docker compose up -d --build
```

Compose builds these local images:

- `yl-toggle/api:3.0.0`
- `yl-toggle/server:3.0.0`
- `yl-toggle/ui:3.0.0`
- `yl-toggle/analysis:3.0.0`

It also starts MariaDB 11.8.9 and Redoc.

## Scale the API and evaluation server

```bash
docker compose up -d --build --scale backendAPI=2 --scale serverAPI=2
```

## Verify

```bash
docker compose ps --all
curl http://127.0.0.1:4009/
curl http://127.0.0.1:4006/actuator/health
curl http://127.0.0.1:4009/api/v3/api-docs.yaml
# Redoc UI: http://127.0.0.1:4009/api-docs
```

Open [http://127.0.0.1:4009/](http://127.0.0.1:4009/) and sign in with:

- username: `Admin`
- password: `Pass1234`

## Reset the local environment

The local volume contains database data. To recreate it:

```bash
docker compose down -v
docker compose up -d --build
```

:::caution
`docker compose down -v` deletes the local MariaDB volume and all data in it.
:::
