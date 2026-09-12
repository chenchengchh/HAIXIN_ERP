import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

/**
 * Vitest 配置
 * - 使用 jsdom 环境以支持浏览器 API（如 window、history）
 */
export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom'
  }
})
