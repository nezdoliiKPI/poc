import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    // SPA-fallback: будь-який невідомий маршрут повертає index.html
    // (виправляє 404 при оновленні сторінки у dev-режимі)
    historyApiFallback: true,
    proxy: {
      // У dev-режимі проксюємо /api/* на Quarkus вручну.
      // У prod-збірці Quinoa робить це автоматично.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
