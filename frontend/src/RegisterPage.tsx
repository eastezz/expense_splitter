import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { login, register } from './api'
import { useAuth } from './auth'
import './AuthPage.css'
import GoogleButton from './GoogleButton'
import { useToast } from './toast'

/** The backend needs at least this many characters (see RegisterRequest.java). */
const MIN_PASSWORD_LENGTH = 8

/** The page at /register: creates an account and logs in with it right away. */
function RegisterPage() {
  const navigate = useNavigate()
  const { setUser } = useAuth()
  const showToast = useToast()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  // The password typed a second time, to catch typos before the account exists.
  const [repeated, setRepeated] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const tooShort = password.length > 0 && password.length < MIN_PASSWORD_LENGTH
  const mismatch = repeated.length > 0 && repeated !== password
  const canSubmit =
    !busy && email.trim() !== '' && password.length >= MIN_PASSWORD_LENGTH && repeated === password

  async function handleSubmit() {
    setError(null)
    setBusy(true)
    try {
      await register(email.trim(), password)
      // Nobody wants to type the same password again right after choosing it.
      const user = await login(email.trim(), password)
      setUser(user)
      showToast(`Account created for ${user.email}`)
      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unknown error')
      setBusy(false)
    }
  }

  return (
    <section className="auth-page">
      <h2>Create an account</h2>
      <form
        className="auth-form"
        onSubmit={(event) => {
          event.preventDefault()
          handleSubmit()
        }}
      >
        <label htmlFor="register-email">Email</label>
        <input
          id="register-email"
          type="email"
          autoComplete="email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
        />
        <label htmlFor="register-password">Password</label>
        {/* "new-password" tells password managers to suggest a strong new password here. */}
        <input
          id="register-password"
          type="password"
          autoComplete="new-password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
        />
        <p className={tooShort ? 'auth-hint auth-hint-warning' : 'auth-hint'}>
          At least {MIN_PASSWORD_LENGTH} characters.
        </p>
        <label htmlFor="register-repeat">Repeat password</label>
        <input
          id="register-repeat"
          type="password"
          autoComplete="new-password"
          value={repeated}
          onChange={(event) => setRepeated(event.target.value)}
        />
        {mismatch && <p className="auth-hint auth-hint-warning">The passwords are not the same.</p>}
        <button type="submit" disabled={!canSubmit}>
          {busy ? 'Creating account…' : 'Create account'}
        </button>
        <GoogleButton />
      </form>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      <p className="auth-switch">
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </section>
  )
}

export default RegisterPage
