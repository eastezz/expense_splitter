import { useCallback, useRef, useState, type ReactNode } from 'react'
import { ToastContext } from './toast'
import './ToastProvider.css'

type Toast = { id: number; message: string }

/** How long a message stays on screen, in milliseconds. */
const TOAST_DURATION_MS = 3000

/**
 * Wraps the whole app. Any page can call useToast() to show a short message;
 * this component keeps the visible messages and removes each one after a few seconds.
 */
function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  // A counter for unique ids. useRef keeps the value between renders without causing a new render.
  const nextId = useRef(1)

  // useCallback keeps the same function between renders, so pages can use it safely.
  const showToast = useCallback((message: string) => {
    const id = nextId.current++
    setToasts((current) => [...current, { id, message }])
    setTimeout(() => {
      setToasts((current) => current.filter((toast) => toast.id !== id))
    }, TOAST_DURATION_MS)
  }, [])

  return (
    <ToastContext value={showToast}>
      {children}
      {/* aria-live: screen readers read new messages aloud without moving the focus. */}
      <div className="toast-container" aria-live="polite">
        {toasts.map((toast) => (
          <div key={toast.id} className="toast">
            <span className="toast-icon" aria-hidden="true">
              ✓
            </span>
            {toast.message}
          </div>
        ))}
      </div>
    </ToastContext>
  )
}

export default ToastProvider