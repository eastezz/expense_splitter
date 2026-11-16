import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config.ts'

// Test settings for `npm test`. They build on top of vite.config.ts,
// so tests see the same React plugin as the real app.
export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      // jsdom is a fake browser (document, window, ...) that runs inside Node.
      environment: 'jsdom',
      // Runs before every test file: adds matchers like toBeInTheDocument().
      setupFiles: ['./src/test/setup.ts'],
      include: ['src/**/*.test.{ts,tsx}'],
    },
  }),
)
