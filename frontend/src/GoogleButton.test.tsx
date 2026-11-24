import { render, screen } from '@testing-library/react'
import { expect, it, vi } from 'vitest'
import { AuthContext } from './auth'
import GoogleButton from './GoogleButton'

/** Shows the button with the given answer to "does the backend offer Google?". */
function renderButton(googleLogin: boolean) {
  render(
    <AuthContext value={{ user: null, setUser: vi.fn(), googleLogin }}>
      <GoogleButton />
    </AuthContext>,
  )
}

it('links to the Google login of the backend when it is switched on', () => {
  renderButton(true)

  expect(screen.getByRole('link', { name: 'Continue with Google' })).toHaveAttribute(
    'href',
    '/oauth2/authorization/google',
  )
})

it('shows nothing when the backend has no Google login', () => {
  renderButton(false)

  expect(screen.queryByRole('link', { name: 'Continue with Google' })).not.toBeInTheDocument()
})
