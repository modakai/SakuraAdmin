import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    // 将 @ 固定到 src，避免深层模块维护脆弱的相对路径。
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
