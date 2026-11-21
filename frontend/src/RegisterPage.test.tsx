import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { expect, it, vi } from 'vitest'
import RegisterPage from './RegisterPage'

/** Shows the register page at /register, and a simple stand-in for the start page at /. */
function renderRegisterPage() {
  render(
    <MemoryRouter initialEntries={['/register']}>
      <Routes>
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/" element={<p>Start page</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

it('creates the account, logs in with it and goes to the start page', async () => {
  // The parameters tell TypeScript what the fake fetch is called with (the "_" means: not used here).
  const fetchMock = vi.fn(async (_url: string, _init?: RequestInit) =>
    Response.json({ id: 1, email: 'anna@example.de' }),
  )
  vi.stubGlobal('fetch', fetchMock)
  const user = userEvent.setup()
  renderRegisterPage()

  await user.type(screen.getByLabelText('Email'), 'anna@example.de')
  await user.type(screen.getByLabelText('Password'), 'secret123')
  await user.type(screen.getByLabelText('Repeat password'), 'secret123')
  await user.click(screen.getByRole('button', { name: 'Create account' }))

  expect(await screen.findByText('Start page')).toBeInTheDocument()
  // First the account is created, then the same email and password log in.
  expect(fetchMock.mock.calls.map((call) => call[0])).toEqual(['/api/auth/register', '/api/auth/login'])
})

it('warns about a short password and different passwords', async () => {
  const user = userEvent.setup()
  renderRegisterPage()

  await user.type(screen.getByLabelText('Email'), 'anna@example.de')
  await user.type(screen.getByLabelText('Password'), 'short')
  await user.type(screen.getByLabelText('Repeat password'), 'shorx')

  expect(screen.getByText('The passwords are not the same.')).toBeInTheDocument()
  expect(screen.getByRole('button', { name: 'Create account' })).toBeDisabled()
})

it('shows the backend message when the email is taken', async () => {
  vi.stubGlobal(
    'fetch',
    vi.fn(async () =>
      Response.json({ detail: "An account with the email 'anna@example.de' already exists" }, { status: 409 }),
    ),
  )
  const user = userEvent.setup()
  renderRegisterPage()

  await user.type(screen.getByLabelText('Email'), 'anna@example.de')
  await user.type(screen.getByLabelText('Password'), 'secret123')
  await user.type(screen.getByLabelText('Repeat password'), 'secret123')
  await user.click(screen.getByRole('button', { name: 'Create account' }))

  expect(await screen.findByRole('alert')).toHaveTextContent('already exists')
})
