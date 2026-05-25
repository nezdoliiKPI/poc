import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// Base URL of the monitoring service.
// Serves: /api/devices, /api/history, etc.
const MONITORING_URL = process.env.VITE_MONITORING_URL ?? 'http://localhost:8080';

// Base URL of the configuration service.
// Serves: /api/thresholds, /api/edge, /api/producer
// If both services share one Quarkus process, leave this equal to MONITORING_URL.
// If the configuration service runs separately, set VITE_CONFIG_URL in .env.local
const CONFIG_URL = process.env.VITE_CONFIG_URL ?? MONITORING_URL;

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    // SPA fallback: unknown routes return index.html
    // (prevents 404 on page refresh in dev mode)
    historyApiFallback: true,
    proxy: {
      // ── Configuration service endpoints ────────────────────────────────────
      // More-specific rules must appear before the catch-all /api rule.
      '/api/thresholds': {
        target: CONFIG_URL,
        changeOrigin: true,
      },
      '/api/edge': {
        target: CONFIG_URL,
        changeOrigin: true,
      },
      '/api/producer': {
        target: CONFIG_URL,
        changeOrigin: true,
      },
      // ── Monitoring service — catch-all ──────────────────────────────────────
      '/api': {
        target: MONITORING_URL,
        changeOrigin: true,
      },
    },
  },
});
