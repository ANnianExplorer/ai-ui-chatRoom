import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import viteCompression from 'vite-plugin-compression'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
    // 插件配置
    plugins: [vue(), viteCompression()],
    // 服务配置
    server: {
      host: '0.0.0.0',
      port: env.VITE_APP_PORT || 3000,
      open: JSON.parse(env.VITE_APP_OPEN),
      hmr: true
    },
    // 设置别名
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src')
      }
    },
    // 打包配置
    build: {
      outDir: 'dist', // 输出路径
      minify: 'terser', // 混淆器
      terserOptions: {
        // terser 压缩配置
        compress: {
          drop_console: true, // 删除console
          drop_debugger: true // 删除debugger
        },
        format: {
          comments: false // 删除注释
        }
      },
      rollupOptions: {
        output: {
          // 输出配置
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
          manualChunks(id) {
            // 分块配置
            if (id.includes('node_modules')) {
              const match = id.match(/\/node_modules\/(?!.pnpm)(?<moduleName>[^\/]*)\//)
              return match?.groups?.moduleName ?? 'vendor'
            }
          }
        }
      }
    },
    // 解决 legacy-js-api warning
    css: {
      preprocessorOptions: {
        scss: {
          api: 'modern-compiler',
          silenceDeprecations: ['legacy-js-api']
        }
      }
    }
  }
})
