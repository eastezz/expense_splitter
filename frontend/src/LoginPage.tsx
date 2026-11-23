import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import { login } from './api'
import { useAuth } from './auth'
import './AuthPage.css'
import { useToast } from './toast'

/**
 * The page at /login: email and password, then back to the page the user wanted
 * (RequireLogin remembers it), or else to the start page.
 */
function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { setUser } = useAuth()
  const showToast = useToast()
  // The address RequireLogin sent us away from, for example "/groups/3".
  const from = (location.state as { from?: string } | null)?.from ?? '/'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  // The error from the last attempt (for example "Wrong email or password"), or null.
  const [error, setError] = useState<string | null>(null)
  // True while the request is on its way, so the button can't be clicked twice.
  const [busy, setBusy] = useState(false)

  async function handleSubmit() {
    setError(null)
    setBusy(true)
    try {
      const user = await login(email.trim(), password)
      setUser(user)
      showToast(`Logged in as ${user.email}`)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error')
      setBusy(false)
    }
  }

  return (
    <section className="auth-page">
      <h2>Log in</h2>
      <form
        className="auth-form"
        onSubmit={(event) => {
          event.preventDefault()
          handleSubmit()
        }}
      >
        <label htmlFor="login-email">Email</label>
        {/* autoComplete lets the browser and password managers fill in the saved login. */}
        <input
          id="login-email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
        />
        <label htmlFor="login-password">Password</label>
        <input
          id="login-password"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />
        <button type="submit" disabled={busy || email.trim() === '' || password === ''}>
          {busy ? 'Logging in…' : 'Log in'}
        </button>
      </form>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      <p className="auth-switch">
        No account yet? <Link to="/register">Create one</Link>
      </p>
    </section>
  )
}

export default LoginPage
