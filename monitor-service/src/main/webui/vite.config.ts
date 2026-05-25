import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Base URL of the monitoring service.
// Serves: /api/devices, /api/history, etc.
// Change this if the monitoring service runs on a different port.
const MONITORING_URL = 'https://localhost:8086';

// Base URL of the configuration service.
// Serves: /api/thresholds, /api/edge, /api/producer
// If both services share one Quarkus process, leave this equal to MONITORING_URL.
// If the configuration service runs separately, update this value.
const CONFIG_URL = MONITORING_URL;

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'dist',
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      // ── Configuration service endpoints ────────────────────────────────────
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
