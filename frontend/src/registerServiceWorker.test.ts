import { afterEach, expect, it, vi } from 'vitest'
import { registerServiceWorker } from './registerServiceWorker'

/** jsdom has no service worker support, so the test gives the browser a fake one. */
function fakeServiceWorker() {
  const register = vi.fn(async () => ({}))
  Object.defineProperty(navigator, 'serviceWorker', { value: { register }, configurable: true })
  return register
}

afterEach(() => {
  vi.unstubAllEnvs()
  // Removes the fake again, so other tests see the normal jsdom navigator.
  Reflect.deleteProperty(navigator, 'serviceWorker')
})

it('registers /sw.js after the page has loaded in the real build', () => {
  vi.stubEnv('PROD', true)
  const register = fakeServiceWorker()

  registerServiceWorker()
  expect(register).not.toHaveBeenCalled()
  window.dispatchEvent(new Event('load'))

  expect(register).toHaveBeenCalledWith('/sw.js')
})

it('does nothing during development', () => {
  vi.stubEnv('PROD', false)
  const register = fakeServiceWorker()

  registerServiceWorker()
  window.dispatchEvent(new Event('load'))

  expect(register).not.toHaveBeenCalled()
})
