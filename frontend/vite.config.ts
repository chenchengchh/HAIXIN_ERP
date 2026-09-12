import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { createMockApiPlugin } from './vite.mock'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, __dirname, '')
  const proxyTarget = env.VITE_PROXY_TARGET || 'http://localhost:9000'
  const enableMock = (env.VITE_ENABLE_MOCK || '').toLowerCase() === 'true'

  return {
    plugins: [createMockApiPlugin({ enabled: enableMock }), vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: 3000,
      host: '0.0.0.0',
      proxy: {
        '/ai-voice': {
          target: proxyTarget,
          changeOrigin: true,
          ws: true
        },
        '/ws': {
          target: proxyTarget,
          changeOrigin: true,
          ws: true
        },
        '/api': {
          target: proxyTarget,
          changeOrigin: true
        }
      }
    },
    build: {
      target: ['es2020', 'edge88', 'firefox78', 'chrome87', 'safari14'],
      minify: 'esbuild',
      cssCodeSplit: true,
      sourcemap: false,
      assetsDir: 'assets',
      rollupOptions: {
        output: {
          manualChunks: {
            'element-plus': ['element-plus', '@element-plus/icons-vue'],
            'vue-vendor': ['vue', 'vue-router', 'pinia'],
            'echarts': ['echarts'],
            'xlsx': ['xlsx']
          }
        }
      }
    }
  }
})
