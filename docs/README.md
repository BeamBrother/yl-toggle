# Website

This website is built with [Docusaurus 3.10.2](https://docusaurus.io/).

## Requirements

- Node.js 24.13.1
- npm 10 or newer

## Install dependencies

```bash
npm ci
```

## Local development for the English version

```bash
npm run start
```

Most edits are reflected live without restarting the development server.

## Local development for the Chinese version

```bash
npm run start -- --locale zh-CN
```

## Typecheck and build

```bash
npm run typecheck
npm run build
```

The production site is generated in the `build` directory.
