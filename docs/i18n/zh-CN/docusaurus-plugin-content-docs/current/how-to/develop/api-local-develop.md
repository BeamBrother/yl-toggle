---
sidebar_position: 2
---

# API

## 环境要求

- JDK 25
- Maven Wrapper（已内置，会下载 Maven 3.9.14）
- MariaDB 11.8.9+

## 获取源码

```shell
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle/api
```

## 配置数据库

创建名为 `yl-toggle` 的 MariaDB 数据库。API 启动时 Flyway 会自动创建并迁移表结构。

修改 `admin/src/main/resources/config/application.yml`，或通过 Spring 环境变量覆盖配置：

```shell
export SPRING_DATASOURCE_JDBC_URL='jdbc:mariadb://127.0.0.1:3306/yl-toggle?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=root
```

API 默认端口为 `8080`。

## 编译与测试

API 是多模块 Maven 项目，Web 服务位于 `admin` 模块。

```shell
./mvnw verify
```

Windows 请使用 `.mvnw.cmd verify`。

Maven 构建不再耦合 UI；前端开发与构建请在 `../ui` 中执行。

## 本地运行

```shell
./mvnw -pl admin -am spring-boot:run
```

访问 [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) 验证服务状态。

如需生产样式包：

```shell
./mvnw -pl admin -am package
java -jar admin/target/api.jar
```
