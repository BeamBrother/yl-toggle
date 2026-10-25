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
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/react') || id.includes('node_modules/react-dom') || id.includes('node_modules/react-router')) {
            return 'react';
          }
          if (id.includes('node_modules/chart.js') || id.includes('node_modules/chartjs-plugin-annotation') || id.includes('node_modules/react-chartjs-2')) {
            return 'chart';
          }
          if (id.includes('node_modules/@uiw/react-codemirror') || id.includes('node_modules/@codemirror')) {
            return 'codemirror';
          }
          return undefined;
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
