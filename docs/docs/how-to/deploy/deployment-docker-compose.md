---
sidebar_position: 1
---

# Docker Compose Deploy Guide

> This document describes how to deploy the yl-toggle service.


## Requirements

* Docker 17+
* Recommendation: 2-core CPU/4G memory and above

## Deploy yl-toggle Service

### 一、Standalone

Get up and running on Linux/Unix/Mac quickly with docker-compose.

**Setps：**

1. Make sure you have [`git`](https://git-scm.com/) and [`docker`](https://www.docker.com/) installed.
2. For users in Mainland China, please refer to [configure mirror](https://github.com/yl-toggle/yl-toggle/blob/main/DOCKER_HUB.md) to speed up your download.
3. Clone the repository to your server and boot up the services
   ```shell
   git clone https://gitee.com/yl-toggle/yl-toggle.git
   cd yl-toggle
   docker compose up
   ```
4. Go to UI/Portal at `http://localhost:4009` and use the default credentials to log in.
   - username: `admin`
   - password: `Pass1234`
