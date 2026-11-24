import { createContext, useContext } from 'react'
import type { User } from './api'

/**
 * Who is logged in, shared with the whole app.
 * user: undefined = still asking the backend, null = nobody is logged in, otherwise the account.
 * googleLogin: true when the backend offers "Sign in with Google".
 */
export type AuthState = {
  user: User | null | undefined
  setUser: (user: User | null) => void
  googleLogin: boolean
}

// The default is "nobody is logged in, no Google" with a setUser that does nothing, so a page
// rendered without an AuthProvider (for example in its own tests) still works.
export const AuthContext = createContext<AuthState>({ user: null, setUser: () => {}, googleLogin: false })

/** Returns who is logged in and a function to change it (after log in or log out). */
export function useAuth(): AuthState {
  return useContext(AuthContext)
}
