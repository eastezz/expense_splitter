import { useEffect, useState, type ReactNode } from 'react'
import { getCurrentUser, type User } from './api'
import { AuthContext } from './auth'

/**
 * Wraps the whole app. When the app starts, it asks the backend who is logged in
 * (the browser sends the session cookie by itself), and shares the answer with every page.
 */
function AuthProvider({ children }: { children: ReactNode }) {
  // undefined until the backend has answered.
  const [user, setUser] = useState<User | null | undefined>(undefined)

  useEffect(() => {
    getCurrentUser()
      .then(setUser)
      // If the backend can't be reached, we treat it like "not logged in".
      .catch(() => setUser(null))
  }, [])

  return <AuthContext value={{ user, setUser }}>{children}</AuthContext>
}

export default AuthProvider
