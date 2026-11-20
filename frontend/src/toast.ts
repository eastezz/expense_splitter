import { createContext, useContext } from 'react'

/** Shows a short message at the bottom of the screen, for example "Expense added". */
export type ShowToast = (message: string) => void

// The default does nothing, so a page rendered without a ToastProvider
// (for example in its own tests) still works.
export const ToastContext = createContext<ShowToast>(() => {})

/** Returns the function that shows a short message at the bottom of the screen. */
export function useToast(): ShowToast {
  return useContext(ToastContext)
}