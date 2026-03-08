import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3200,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:3201',
        changeOrigin: true,
      },
    },
  },
})
