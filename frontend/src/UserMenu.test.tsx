import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { expect, it, vi } from 'vitest'
import type { User } from './api'
import { AuthContext } from './auth'
import UserMenu from './UserMenu'

/** Shows the menu for the given user, with a stand-in for the login page at /login. */
function renderUserMenu(user: User | null, setUser = vi.fn()) {
  render(
    <AuthContext value={{ user, setUser, googleLogin: false }}>
      <MemoryRouter>
        <UserMenu />
        <Routes>
          <Route path="/" element={<p>Start page</p>} />
          <Route path="/login" element={<p>Login page</p>} />
        </Routes>
      </MemoryRouter>
    </AuthContext>,
  )
  return setUser
}

it('shows a log in link when nobody is logged in', () => {
  renderUserMenu(null)

  expect(screen.getByRole('link', { name: 'Log in' })).toHaveAttribute('href', '/login')
})

it('logs out: tells the backend, forgets the user and goes to the login page', async () => {
  const fetchMock = vi.fn(async () => new Response(null, { status: 204 }))
  vi.stubGlobal('fetch', fetchMock)
  const user = userEvent.setup()
  const setUser = renderUserMenu({ id: 1, email: 'anna@example.de' })

  expect(screen.getByText('anna@example.de')).toBeInTheDocument()
  await user.click(screen.getByRole('button', { name: 'Log out' }))

  expect(await screen.findByText('Login page')).toBeInTheDocument()
  expect(fetchMock).toHaveBeenCalledWith('/api/auth/logout', { method: 'POST' })
  expect(setUser).toHaveBeenCalledWith(null)
})
