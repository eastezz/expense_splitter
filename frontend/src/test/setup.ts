// Loaded before every test file (see vitest.config.ts).
import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach, vi } from 'vitest'

// After each test: remove what the test rendered and undo any mocks
// (for example a fake fetch), so tests can't affect each other.
afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
  vi.restoreAllMocks()
})
