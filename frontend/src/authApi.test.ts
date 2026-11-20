import { afterEach, expect, it, vi } from 'vitest'
import { getCurrentUser, login, withCsrf } from './api'

afterEach(() => {
  // Deletes the test cookie again: a cookie with a date in the past is removed by the browser.
  document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT'
})

it('leaves a request unchanged when there is no CSRF cookie', () => {
  const init = { method: 'DELETE' }
  expect(withCsrf(init)).toBe(init)
})

it('adds the CSRF token from the cookie as a header', () => {
  document.cookie = 'XSRF-TOKEN=abc123'

  const result = withCsrf({ method: 'POST', headers: { 'Content-Type': 'application/json' } })

  expect(result.headers).toEqual({ 'Content-Type': 'application/json', 'X-XSRF-TOKEN': 'abc123' })
})

it('logs in with email and password and returns the user', async () => {
  const fetchMock = vi.fn(async () => Response.json({ id: 1, email: 'anna@example.de' }))
  vi.stubGlobal('fetch', fetchMock)

  const user = await login('anna@example.de', 'secret123')

  expect(user).toEqual({ id: 1, email: 'anna@example.de' })
  expect(fetchMock).toHaveBeenCalledWith('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email: 'anna@example.de', password: 'secret123' }),
  })
})

it('throws the backend message when the login fails', async () => {
  vi.stubGlobal(
    'fetch',
    vi.fn(async () => Response.json({ detail: 'Wrong email or password' }, { status: 401 })),
  )

  await expect(login('anna@example.de', 'wrong')).rejects.toThrow('Wrong email or password')
})

it('returns null as the current user when nobody is logged in', async () => {
  vi.stubGlobal('fetch', vi.fn(async () => Response.json({ detail: 'Not logged in' }, { status: 401 })))

  expect(await getCurrentUser()).toBeNull()
})
