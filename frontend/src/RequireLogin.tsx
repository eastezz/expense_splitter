import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuth } from './auth'
import LoadingCard from './LoadingCard'

/**
 * Shows its page only to a logged-in user. Anyone else is sent to /login,
 * and the address they wanted is remembered, so the login can bring them back there.
 */
function RequireLogin({ children }: { children: ReactNode }) {
  const { user } = useAuth()
  const location = useLocation()

  if (user === undefined) {
    // Still asking the backend: show the grey placeholder instead of jumping to /login too early.
    return <LoadingCard label="Checking login…" rows={2} />
  }
  if (user === null) {
    // replace: the login page takes the place of this address in the browser history,
    // so the Back button doesn't lead straight back to the page that sent us away.
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  return children
}

export default RequireLogin
