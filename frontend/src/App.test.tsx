import { render, screen } from '@testing-library/react'
import { expect, it, vi } from 'vitest'
import App from './App'

it('shows the app heading', async () => {
  // GroupsPage calls fetch('/api/groups') when it appears.
  // We replace fetch with a fake that answers "no groups", so no real network is used.
  vi.stubGlobal('fetch', vi.fn(async () => Response.json([])))

  render(<App />)

  expect(screen.getByRole('heading', { level: 1, name: 'expense_splitter' })).toBeInTheDocument()
  // Wait until loading has finished, so the test ends in a calm state.
  expect(await screen.findByText('No groups yet.')).toBeInTheDocument()
})
