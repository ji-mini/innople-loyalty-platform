import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return

          if (id.includes('react') || id.includes('scheduler') || id.includes('react-router')) {
            return 'vendor-react'
          }

          if (id.includes('antd') || id.includes('@ant-design') || id.includes('rc-')) {
            return 'vendor-antd'
          }

          if (id.includes('recharts') || id.includes('d3-') || id.includes('internmap')) {
            return 'vendor-charts'
          }

          if (id.includes('@tanstack')) {
            return 'vendor-query'
          }

          if (id.includes('axios')) {
            return 'vendor-http'
          }
        },
      },
    },
  },
  server: {
    port: 3200,
    strictPort: true,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET ?? 'http://localhost:3201',
        changeOrigin: true,
      },
    },
  },
})
