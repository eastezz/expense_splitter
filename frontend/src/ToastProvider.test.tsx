import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, it, vi } from 'vitest'
import { useToast } from './toast'
import ToastProvider from './ToastProvider'

/** A tiny component that shows a toast when its button is clicked. */
function SaveButton() {
  const showToast = useToast()
  return <button onClick={() => showToast('Expense added')}>Save</button>
}

it('shows a message and removes it after a few seconds', async () => {
  // Fake timers let the test jump 3 seconds ahead instead of really waiting.
  vi.useFakeTimers({ shouldAdvanceTime: true })
  const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime })
  render(
    <ToastProvider>
      <SaveButton />
    </ToastProvider>,
  )

  await user.click(screen.getByRole('button', { name: 'Save' }))
  expect(screen.getByText('Expense added')).toBeInTheDocument()

  act(() => {
    vi.advanceTimersByTime(3000)
  })
  expect(screen.queryByText('Expense added')).not.toBeInTheDocument()
  vi.useRealTimers()
})