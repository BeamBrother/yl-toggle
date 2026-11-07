---
sidebar_position: 1
---

# UI

## Requirements

- Node.js 24.13.1
- npm 10 or newer

## Install dependencies

```shell
git clone https://github.com/yl-toggle/yl-toggle.git
cd yl-toggle/ui
npm ci
```

## Start the development server

```shell
npm run dev
```

The portal is available at [http://localhost:3000/](http://localhost:3000/).

## Proxy API requests in development

The Vite development proxy is configured in `vite.config.ts`:

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

Change `target` to your own API endpoint, such as `http://localhost:8080`.

## Typecheck, test, and build

```shell
npm run typecheck
npm run lint
npm test
npm run build
```

Production assets are generated in `build/`.

## Deployment

To deploy the UI independently, see [UI Service](../deploy/deployment-source-code#portal).
