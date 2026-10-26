---
sidebar_position: 1
---

# UI

## 环境要求

- Node.js 24.13.1
- npm 10 或更新版本

## 安装依赖

```shell
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle/ui
npm ci
```

## 启动本地服务

```shell
npm run dev
```

访问 [http://localhost:3000/](http://localhost:3000/) 查看 UI。

## 开发环境代理 API 请求

Vite 开发代理配置位于 `vite.config.ts`：

```ts
server: {
  proxy: {
    '/api': {
      target: 'https://yl-toggle.io',
      changeOrigin: true,
      secure: false,
    },
  },
},
```

将 `target` 修改为自己的 API 地址，例如 `http://localhost:8080`。

## 类型检查、测试与构建

```shell
npm run typecheck
npm run lint
npm test
npm run build
```

生产构建产物输出到 `build/`。

## 私有化部署

如需单独部署 UI，请参考 [UI Service](../deploy/deployment-source-code#portal)。
