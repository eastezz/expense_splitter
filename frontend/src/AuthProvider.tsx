import { useEffect, useState, type ReactNode } from 'react'
import { getAuthProviders, getCurrentUser, type User } from './api'
import { AuthContext } from './auth'

/**
 * Wraps the whole app. When the app starts, it asks the backend who is logged in
 * (the browser sends the session cookie by itself) and whether "Sign in with Google" is available,
 * and shares the answers with every page.
 */
function AuthProvider({ children }: { children: ReactNode }) {
  // undefined until the backend has answered.
  const [user, setUser] = useState<User | null | undefined>(undefined)
  const [googleLogin, setGoogleLogin] = useState(false)

  useEffect(() => {
    getCurrentUser()
      .then(setUser)
      // If the backend can't be reached, we treat it like "not logged in".
      .catch(() => setUser(null))
    getAuthProviders().then((providers) => setGoogleLogin(providers.google))
  }, [])

  return <AuthContext value={{ user, setUser, googleLogin }}>{children}</AuthContext>
}

export default AuthProvider
