import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { expect, it, vi } from 'vitest'
import LoginPage from './LoginPage'

/** Shows the login page at /login, and a simple stand-in for the start page at /. */
function renderLoginPage() {
  render(
    <MemoryRouter initialEntries={['/login']}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<p>Start page</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

it('logs in and goes to the start page', async () => {
  const fetchMock = vi.fn(async () => Response.json({ id: 1, email: 'anna@example.de' }))
  vi.stubGlobal('fetch', fetchMock)
  const user = userEvent.setup()
  renderLoginPage()

  await user.type(screen.getByLabelText('Email'), 'anna@example.de')
  await user.type(screen.getByLabelText('Password'), 'secret123')
  await user.click(screen.getByRole('button', { name: 'Log in' }))

  expect(await screen.findByText('Start page')).toBeInTheDocument()
  expect(fetchMock).toHaveBeenCalledWith('/api/auth/login', expect.objectContaining({ method: 'POST' }))
})

it('goes back to the page the user wanted before the login', async () => {
  vi.stubGlobal('fetch', vi.fn(async () => Response.json({ id: 1, email: 'anna@example.de' })))
  const user = userEvent.setup()
  // RequireLogin sends the user here with the wanted address in the "state".
  render(
    <MemoryRouter initialEntries={[{ pathname: '/login', state: { from: '/groups/3' } }]}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/groups/:groupId" element={<p>Group page</p>} />
      </Routes>
    </MemoryRouter>,
  )

  await user.type(screen.getByLabelText('Email'), 'anna@example.de')
  await user.type(screen.getByLabelText('Password'), 'secret123')
  await user.click(screen.getByRole('button', { name: 'Log in' }))

  expect(await screen.findByText('Group page')).toBeInTheDocument()
})

it('shows the error and stays on the page when the login fails', async () => {
  vi.stubGlobal(
    'fetch',
    vi.fn(async () => Response.json({ detail: 'Wrong email or password' }, { status: 401 })),
  )
  const user = userEvent.setup()
  renderLoginPage()

  await user.type(screen.getByLabelText('Email'), 'anna@example.de')
  await user.type(screen.getByLabelText('Password'), 'wrong')
  await user.click(screen.getByRole('button', { name: 'Log in' }))

  expect(await screen.findByRole('alert')).toHaveTextContent('Wrong email or password')
  // The button is usable again, so the user can correct the password and try again.
  expect(screen.getByRole('button', { name: 'Log in' })).toBeEnabled()
})

it('explains a failed Google login', () => {
  // The backend sends the browser to /login?error=google when Google didn't work out.
  render(
    <MemoryRouter initialEntries={['/login?error=google']}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
      </Routes>
    </MemoryRouter>,
  )

  expect(screen.getByRole('alert')).toHaveTextContent('Signing in with Google did not work')
})

it('keeps the button disabled until both fields are filled in', async () => {
  const user = userEvent.setup()
  renderLoginPage()

  expect(screen.getByRole('button', { name: 'Log in' })).toBeDisabled()
  await user.type(screen.getByLabelText('Email'), 'anna@example.de')
  expect(screen.getByRole('button', { name: 'Log in' })).toBeDisabled()
  await user.type(screen.getByLabelText('Password'), 'x')
  expect(screen.getByRole('button', { name: 'Log in' })).toBeEnabled()
})
