import { defineConfig } from 'vite'
import { createVuePlugin } from 'vite-plugin-vue2'
import path from 'path'

export default defineConfig({
  plugins: [
    createVuePlugin()
  ],
  resolve: {
    dedupe: ['vue'],
    alias: {
      'vue': path.resolve(__dirname, 'node_modules/vue/dist/vue.esm.js'),
      '@': path.resolve(__dirname, './src')
    }
  },
  build: {
    outDir: './dist',
    sourcemap: false
  },
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        ws: true,
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api/, '')
      }
    }
  }
})
