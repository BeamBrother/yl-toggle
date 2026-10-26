import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';
import path from 'node:path';
import { viteStaticCopy } from 'vite-plugin-static-copy';

export default defineConfig({
  plugins: [
    react(),
    viteStaticCopy({
      targets: [
        { src: 'node_modules/semantic-ui-less/themes/default/assets/fonts/*', dest: 'themes/default/assets/fonts', rename: { stripBase: true } },
        { src: 'node_modules/semantic-ui-less/themes/default/assets/images/*', dest: 'themes/default/assets/images', rename: { stripBase: true } },
        { src: 'src/images/archived.png', dest: 'images', rename: { stripBase: true } },
      ],
    }),
  ],
  resolve: {
    tsconfigPaths: true,
    alias: [
      { find: /^~semantic-ui-less\/?(.*)$/, replacement: path.resolve(import.meta.dirname, 'node_modules/semantic-ui-less/$1') },
      { find: /^..\/..\/theme.config$/, replacement: path.resolve(import.meta.dirname, 'src/semantic-ui/theme.config') },
    ],
  },
  css: {
    preprocessorOptions: {
      less: {
        math: 'always',
      },
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'https://yl-toggle.io',
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: 'build',
    sourcemap: false,
    cssMinify: 'esbuild',
    rolldownOptions: {
      output: {
        // Match exact package directories: a loose `id.includes('node_modules/react')`
        // pulled every react-* package (syntax-highlighter's full language registry,
        // moment, joyride, ...) into the eager react chunk.
        advancedChunks: {
          groups: [
            // babel helpers are shared by nearly every package; listed first so
            // they are captured before (and kept out of) feature chunks
            { name: 'babel-runtime', test: /[\\/]node_modules[\\/]@babel[\\/]runtime[\\/]/ },
            { name: 'react', test: /[\\/]node_modules[\\/](react|react-dom|react-router|react-router-dom|scheduler)[\\/]/ },
            { name: 'chart', test: /[\\/]node_modules[\\/](chart\.js|chartjs-plugin-annotation|react-chartjs-2|@kurkle)[\\/]/ },
            { name: 'codemirror', test: /[\\/]node_modules[\\/](@uiw[\\/]react-codemirror|@uiw[\\/]codemirror-extensions-basic-setup|@codemirror|@lezer|@marijn|style-mod|w3c-keyname|crelt)[\\/]/ },
          ],
        },
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/setupTests.ts'],
    css: false,
    restoreMocks: false,
  },
});
