import { defineConfig } from 'vite'

// The product-composite service listens on 8080 under docker-compose and on
// 7000 when run locally outside docker. Override with VITE_API_TARGET if needed,
// e.g.  VITE_API_TARGET=http://localhost:7000 npm run dev
const API_TARGET = process.env.VITE_API_TARGET || 'http://localhost:8080'

export default defineConfig({
  server: {
    port: 5173,
    // Proxy API calls to the backend so the browser stays same-origin (no CORS).
    proxy: {
      '/product-composite': {
        target: API_TARGET,
        changeOrigin: true
      }
    }
  },
  build: { outDir: 'dist' }
})
