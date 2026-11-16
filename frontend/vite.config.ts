import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // During `npm run dev`, send every request that starts with /api
      // to the Spring Boot backend running on port 8080.
      '/api': 'http://localhost:8080',
    },
  },
})
