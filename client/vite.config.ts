import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const rootEnvDir = resolve(dirname(fileURLToPath(import.meta.url)), '..')

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, rootEnvDir, '')

  return {
    envDir: rootEnvDir,
    plugins: [react()],
    server: {
      proxy: {
        '/api': env.VITE_API_PROXY_TARGET,
      },
    },
  }
})
