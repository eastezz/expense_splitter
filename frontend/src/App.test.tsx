import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { expect, it, vi } from 'vitest'
import App from './App'

/**
 * Replaces fetch with a fake backend, so no real network is used:
 * /api/auth/me answers with the given user (or 401 when null), every other address with "no groups".
 */
function fakeBackend(user: { id: number; email: string } | null) {
  vi.stubGlobal(
    'fetch',
    vi.fn(async (url: string) => {
      if (url === '/api/auth/me') {
        return user ? Response.json(user) : Response.json({ detail: 'Not logged in' }, { status: 401 })
      }
      return Response.json([])
    }),
  )
}

it('shows the app heading and the groups of a logged-in user', async () => {
  fakeBackend({ id: 1, email: 'anna@example.de' })

  // Routes only work inside a router. MemoryRouter keeps the URL in memory and starts at "/".
  render(
    <MemoryRouter>
      <App />
    </MemoryRouter>,
  )

  expect(screen.getByRole('heading', { level: 1, name: 'expense_splitter' })).toBeInTheDocument()
  // Wait until loading has finished, so the test ends in a calm state.
  expect(await screen.findByText('No groups yet.')).toBeInTheDocument()
  expect(screen.getByText('anna@example.de')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Log out' })).toBeInTheDocument()
})

it('sends a visitor who is not logged in to the login page', async () => {
  fakeBackend(null)

  render(
    <MemoryRouter initialEntries={['/groups/3']}>
      <App />
    </MemoryRouter>,
  )

  expect(await screen.findByRole('heading', { level: 2, name: 'Log in' })).toBeInTheDocument()
  expect(screen.queryByText('No groups yet.')).not.toBeInTheDocument()
})
