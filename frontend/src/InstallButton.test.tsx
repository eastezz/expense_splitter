import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { expect, it, vi } from 'vitest'
import InstallButton from './InstallButton'

/** A fake of the event the browser sends when the app can be installed. */
function fakeInstallEvent(outcome: 'accepted' | 'dismissed') {
  const event = new Event('beforeinstallprompt', { cancelable: true })
  const prompt = vi.fn(async () => {})
  Object.assign(event, { prompt, userChoice: Promise.resolve({ outcome }) })
  return { event, prompt }
}

it('shows nothing while the browser has not offered an install', () => {
  render(<InstallButton />)

  expect(screen.queryByRole('button', { name: 'Install app' })).not.toBeInTheDocument()
})

it('appears when the app can be installed and opens the browser dialog on click', async () => {
  const user = userEvent.setup()
  render(<InstallButton />)
  const { event, prompt } = fakeInstallEvent('accepted')

  act(() => {
    window.dispatchEvent(event)
  })
  // Our button replaces the browser's own banner.
  expect(event.defaultPrevented).toBe(true)
  await user.click(screen.getByRole('button', { name: 'Install app' }))

  expect(prompt).toHaveBeenCalled()
  expect(screen.queryByRole('button', { name: 'Install app' })).not.toBeInTheDocument()
})

it('disappears when the app was installed', () => {
  render(<InstallButton />)

  act(() => {
    window.dispatchEvent(fakeInstallEvent('accepted').event)
  })
  expect(screen.getByRole('button', { name: 'Install app' })).toBeInTheDocument()
  act(() => {
    window.dispatchEvent(new Event('appinstalled'))
  })

  expect(screen.queryByRole('button', { name: 'Install app' })).not.toBeInTheDocument()
})
