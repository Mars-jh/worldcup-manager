import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 配置 - 开发服务器代理和别名
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 代理后端 API 请求，避免跨域问题
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 代理 WebSocket 端点
      '/ws': {
        target: 'http://localhost:8080',
        ws: true
      }
    }
  },
  resolve: {
    alias: {
      '@': '/src'
    }
  }
})