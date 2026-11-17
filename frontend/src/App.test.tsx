import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { expect, it, vi } from 'vitest'
import App from './App'

it('shows the app heading', async () => {
  // GroupsPage calls fetch('/api/groups') when it appears.
  // We replace fetch with a fake that answers "no groups", so no real network is used.
  vi.stubGlobal('fetch', vi.fn(async () => Response.json([])))

  // Routes only work inside a router. MemoryRouter keeps the URL in memory and starts at "/".
  render(
    <MemoryRouter>
      <App />
    </MemoryRouter>,
  )

  expect(screen.getByRole('heading', { level: 1, name: 'expense_splitter' })).toBeInTheDocument()
  // Wait until loading has finished, so the test ends in a calm state.
  expect(await screen.findByText('No groups yet.')).toBeInTheDocument()
})
