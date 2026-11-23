import { Link, useNavigate } from 'react-router'
import { logout } from './api'
import { useAuth } from './auth'
import { useToast } from './toast'

/** The right part of the header: the logged-in email with "Log out", or a "Log in" link. */
function UserMenu() {
  const { user, setUser } = useAuth()
  const navigate = useNavigate()
  const showToast = useToast()

  async function handleLogout() {
    try {
      await logout()
    } finally {
      // Even if the request failed, the app forgets the user, so nothing stays visible.
      setUser(null)
      showToast('Logged out')
      navigate('/login')
    }
  }

  if (user === undefined) {
    // Still asking the backend: show nothing yet instead of a "Log in" link that flashes away.
    return null
  }
  if (user === null) {
    return (
      <Link to="/login" className="user-menu-login">
        Log in
      </Link>
    )
  }
  return (
    <div className="user-menu">
      <span className="user-menu-email">{user.email}</span>
      <button type="button" className="user-menu-logout" onClick={handleLogout}>
        Log out
      </button>
    </div>
  )
}

export default UserMenu
