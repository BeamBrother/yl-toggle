---
sidebar_position: 4
---

# 源码编译部署

本文档介绍如何通过源码编译方式部署各模块。假定按照如下链接方式部署：

![deploy](/yl-toggle_deploy_from_code.png)

需要编译部署的模块主要有三个：

:::info
从 v1.11.0 及之后版本, API服务会集成UI服务，启动API服务后，访问API服务根路径即为yl-toggle管理页面。
可根据部署架构需要，选择是否需要单独部署UI服务。
:::

| 示例机器   | 部署模块            | 端口              |
| ---------- | ------------------- |-----------------|
| 10.100.1.1 | yl-toggle API    | 4008            |
| 10.100.1.1 | yl-toggle UI     | 4009（Nginx）（可选） |
| 10.100.1.2 | yl-toggle Server | 4007            |

## 创建数据库

1. 环境准备

   - MySQL 5.7+

2. 创建 `yl-toggle ` 数据库：

   ```sql
   CREATE DATABASE yl-toggle; 
   ```

:::note
无须手工创建表结构。初次启动 API 服务时会自动创建所有表和初始化数据。如果希望手工导入 DML/DDL 请参考[初始化数据库](/reference/database-setup)文档。
:::



## 编译部署 API 服务

### 编译步骤 

1. 编译环境

   - JDK 1.8+
   - Maven 2.0+ : [how to install](https://maven.apache.org/install.html)

    

2. 获取源码并编译出部署包：

```bash
git clone https://gitee.com/yl-toggle/yl-toggle.git
cd yl-toggle/api
mvn clean package
```

  完成编译后会在当前目录生成以版本命名的 jar 部署文件，如 ` target/api-1.1.0.jar`。

### 部署步骤

1. 部署环境

   - JDK 1.8+

2. 将 `api-1.1.0.jar` 放置部署服务器中，填入数据库链接配置，并以 `4008` 端口启动：

   :::caution
   以下脚本中数据库相关信息需要替换成您实际使用的数据库信息。
   :::

   ```bash
    java -jar api-1.1.0.jar --server.port=4008 \
         --spring.datasource.jdbc-url=jdbc:mysql://{MYSQL_DATABASE_IP}:{MYSQL_PORT}/yl-toggle \  # 数据库 IP/端口和库名
         --spring.datasource.username={MYSQL_USERNAME} \
         --spring.datasource.password={MYSQL_PASSWORD} 
   ```
   
   :::info
   API 服务更详细的启动参数说明见 [yl-toggle API 参数说明文档](../../reference/deployment-configuration#yl-toggle-api)
   :::

3. 检查服务是否运行成功

   在部署机运行：
   ```bash
   curl "http://localhost:4008/actuator/health"
   ```

   显示如下信息则表示启动成功：

   ```json
   {
   	status: "UP"
   }
   ```

## 编译部署 Server 服务

### 编译步骤

1. 环境准备

   * Rust
     * [官网安装](https://www.rust-lang.org/tools/install)
     ~~~bash
     $ curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
     ~~~

     * 或者，从[国内镜像](https://rsproxy.cn/)安装
     
       1. 修改~/.zshrc 或 ~/.bashrc:
       ~~~bash
       export RUSTUP_DIST_SERVER="https://rsproxy.cn"
       export RUSTUP_UPDATE_ROOT="https://rsproxy.cn/rustup"
       ~~~
       2. 安装
       ~~~bash
       $ curl --proto '=https' --tlsv1.2 -sSf https://rsproxy.cn/rustup-init.sh | sh
       ~~~
   
   :::caution
   注意安装之后最后一行提示，按提示命令重新加载环境变量，安装才会生效。
   :::

   * 配置[依赖下载镜像](https://rsproxy.cn/), 创建文件： `~/.cargo/config`
     ~~~yaml
     [source.crates-io]
     # To use sparse index, change 'rsproxy' to 'rsproxy-sparse'
     replace-with = 'rsproxy'
     
     [source.rsproxy]
     registry = "https://rsproxy.cn/crates.io-index"
     [source.rsproxy-sparse]
     registry = "sparse+https://rsproxy.cn/index/"
     
     [registries.rsproxy]
     index = "https://rsproxy.cn/crates.io-index"
     
     [net]
     git-fetch-with-cli = true
     ~~~
   
2. 获取源码并编译出部署包：

   ```bash
   git clone https://gitee.com/yl-toggle/yl-toggle.git
   ```
   
   在源码目录中编译：
   ```bash
   cd server
   cargo build --release --verbose
   ```

   完成编译后会在 `target/release/` 目录下生成可执行的二制文件：

   ```bash
   $ ls target/release/ 
   yl-toggle-server
   ```


### 部署步骤

1. 环境准备

   - 无

2. 将生成的 `yl-toggle-server` 文件放在服务器上，并创建启动脚本 `start-yl-toggle-server.sh`：
  
   :::caution
   实际使用中，需要将下面脚本中的ip和端口修改为你API服务实际部署环境的的ip和端口。
   :::
   
   ```bash
   #/bin/bash
   
   export FP_SERVER_PORT=4007  # yl-toggle Server 端口
   export FP_TOGGLES_URL=http://10.100.1.1:4008/internal/server/toggles  # yl-toggle API IP 和端口号
   export FP_EVENTS_URL=http://10.100.1.1:4008/internal/server/events
   export FP_KEYS_URL=http://10.100.1.1:4008/internal/server/sdk_keys
   export FP_REFRESH_SECONDS=1
   
   ./yl-toggle-server 
   ```
   
   :::info
   Server 服务更详细启动参数说明详见 [yl-toggle Server 参数说明文档](../../reference/deployment-configuration#yl-toggle-server)
   :::

3. 执行启动脚本运行服务：`sh ./start-yl-toggle-server.sh`

4. 检查服务是否运行成功，在部署机运行：
   ```bash
   curl "http://localhost:4007/"
   ```

   显示如下信息则表示启动成功：

   ```json
   <h1>yl-toggle Server</h1>
   ```

## 编译部署 UI 服务

### 编译步骤 

1. 环境准备

   * Node.js 16.13+ : [下载](https://nodejs.org/zh-cn/download/)
   
   <!-- * yarn
     * 安装： `npm install -g yarn`
   * python3
     * [安装](https://realpython.com/installing-python/#) -->
     
   :::info
   国内建议切换为 npm 中国镜像站：`npm config set registry https://registry.npmmirror.com/`
   :::

2. 获取源码并编译出可部署的静态文件：

   ```bash
   git clone https://gitee.com/yl-toggle/yl-toggle.git
   cd yl-toggle/ui
   npm install
   npm run build
   ```
   
   完成编译后会在 `build` 目录下生成可部署的静态文件。如下所示：
   
   ```bash
   $ ls build 
   asset-manifest.json favicon.ico         index.html          static/
   ```

3. 【可选】自定义配置产出物路径：
   
   生产环境部署，在域名配置时可以有两个选择：
   * 将yl-toggle服务单独部署成为一个独立的域名，比如：通过 https://your.domain.com 的方式直接访问yl-toggle入口页面
   * 和几个服务共用一个域名，以路径（/path）的形式来进行服务区分，比如：通过 https://your.domain.com/yl-toggle 的方式访问yl-toggle入口页面

   目前我们默认支持第一种方式，如果你是以第二种方式进行部署，需要将源码做一些修改：

   (1) 打开 `craco.config.js` 文件

   ```bash
   cd ui
   vi craco.config.js
   ```

   (2) 在原有配置的基础上，在webpack - configure - output 对象中添加 `publicPath` 字段，比如设置值为：/yl-toggle/

   ```js
   module.exports = {
      webpack: {
         configure: {
            output: { 
               publicPath: '/yl-toggle/'
            },
         }
      }
   };
   ```

   (3) 执行完 `yarn build` 命令后，通过 `vi build/index.html` 查看index.html文件，可以发现文件中的静态资源js、css文件均已添加了上了publicPath中配置的前缀

   ![yarn build](/yarn_build.png)

   (4) 为了加快访问速度，你可以选择将静态资源（js、css等）文件上传到CDN服务器，在编译阶段也可以将publicPath配置成CDN的地址：

   ```js
   module.exports = {
      webpack: {
         configure: {
            output: { 
               publicPath: 'https://cdn.domain.com/'
            },
         }
      }
   };
   ```

   编译后html中的静态资源（js、css等）都是携带CDN地址的路径：

   ![yarn build CND](/yarn_build_cdn.png)


### 部署步骤

1. 环境准备

   - Nginx

2. 将编译生成的 `build` 下的所有文件和文件夹复制到 `/usr/share/nginx/html/` 目录下。

## 配置 Nginx

1. 创建 Nginx 配置：`/etc/nginx/conf.d/yl-toggle.conf`

   :::caution
   实际使用中，需要将下面脚本中的ip和端口修改为服务实际部署环境的的ip和端口。
   :::

   ~~~bash
   upstream ylToggleAPI {
       server 10.100.1.1:4008; # YlToggleAPI IP和端口
   }
   
   
   upstream ylToggleServer {
       server 10.100.1.2:4007; # yl-toggle Server IP和端口
   }
   
   server {
     listen 4009;  # UI 端口
   
     location / {
       index  index.html index.htm;
       root /usr/share/nginx/html;  # UI 静态文件目录
       try_files $uri /index.html;
     }
   
      location /api { # 访问 /api 时统一转发到 ylToggleAPI 服务
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-NginX-Proxy true;
       proxy_pass http://ylToggleAPI/api;
       proxy_ssl_session_reuse off;
       proxy_set_header Host $http_host;
       proxy_cache_bypass $http_upgrade;
       proxy_redirect off;
     }
   
     location /server/api { # 访问 /server/api 时统一转发到 ylToggleServer 服务
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       proxy_set_header X-NginX-Proxy true;
       proxy_pass http://ylToggleServer/api;
       proxy_ssl_session_reuse off;
       proxy_set_header Host $http_host;
       proxy_cache_bypass $http_upgrade;
       proxy_redirect off;
     }
   }
   ~~~

   :::info【可选】
   实际使用中，如果配置了自定义产出物路径，比如：将`publicPath`配置成`/yl-toggle/`，需要将上述nginx配置中的`location /`更改为`location /yl-toggle/`，才能正确匹配到html、js和css等静态文件。

   ~~~
   location /yl-toggle/ {
      index  index.html index.htm;
      root /usr/share/nginx/html;  # UI 静态文件目录
      try_files $uri /index.html;
   }
   ~~~

   :::
   

2. 执行 `reload nginx` 配置，使上述配置生效：

   ```
   nginx -s reload
   ```

## 验证安装

### 平台使用

:::caution
实际使用中，需要将下面的ip和端口修改为你ngnix服务实际部署环境的的ip和端口。
:::

在浏览器中访问 `http://10.100.1.1:4009` 并使用如下账号密码登录来验证是否部署成功：
- username: `admin`
- password: `Pass1234`

### Server Side SDK 访问

SDK连接使用的服务端 `remote_uri` 地址为上述配置的的 ngnix 机器地址和服务端口，加上路径 `/server` ，例如：以上例子中为 `http://10.100.1.1:4009/server`

具体步骤可以参考[百分比灰度接入](../../tutorials/rollout_tutorial/index.md)
