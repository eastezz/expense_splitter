import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, expect, it } from 'vitest'
import { isIos, IOS_HINT_DISMISSED_KEY } from './installHint'
import IosInstallHint from './IosInstallHint'

const IPHONE =
  'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Mobile/15E148 Safari/604.1'
const ANDROID = 'Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0 Mobile Safari/537.36'
const MAC = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_0) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.0 Safari/605.1.15'

/** Pretends the test runs on this device. */
function pretendDevice(userAgent: string) {
  Object.defineProperty(navigator, 'userAgent', { value: userAgent, configurable: true })
}

afterEach(() => {
  Reflect.deleteProperty(navigator, 'userAgent')
  localStorage.clear()
})

it('recognizes iPhones and new iPads, but not Android or a Mac', () => {
  expect(isIos(IPHONE, 5)).toBe(true)
  // New iPads introduce themselves like a Mac, but have a touch screen.
  expect(isIos(MAC, 5)).toBe(true)
  expect(isIos(MAC, 0)).toBe(false)
  expect(isIos(ANDROID, 5)).toBe(false)
})

it('explains Add to Home Screen on an iPhone and stays closed after closing it', async () => {
  const user = userEvent.setup()
  pretendDevice(IPHONE)
  const { unmount } = render(<IosInstallHint />)

  expect(screen.getByText('Add to Home Screen')).toBeInTheDocument()
  await user.click(screen.getByRole('button', { name: 'Close the install hint' }))
  expect(screen.queryByText('Add to Home Screen')).not.toBeInTheDocument()
  expect(localStorage.getItem(IOS_HINT_DISMISSED_KEY)).toBe('true')

  // Next visit: it remembers that the hint was closed.
  unmount()
  render(<IosInstallHint />)
  expect(screen.queryByText('Add to Home Screen')).not.toBeInTheDocument()
})

it('shows nothing on Android', () => {
  pretendDevice(ANDROID)
  render(<IosInstallHint />)

  expect(screen.queryByText('Add to Home Screen')).not.toBeInTheDocument()
})
