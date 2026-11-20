import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, expect, it } from 'vitest'
import ThemeToggle from './ThemeToggle'

afterEach(() => {
  delete document.documentElement.dataset.theme
  localStorage.clear()
})

it('switches from light to dark and remembers the choice', async () => {
  const user = userEvent.setup()
  document.documentElement.dataset.theme = 'light'
  render(<ThemeToggle />)

  await user.click(screen.getByRole('button', { name: 'Switch to dark theme' }))

  expect(document.documentElement.dataset.theme).toBe('dark')
  expect(localStorage.getItem('theme')).toBe('dark')
  // The button now offers the way back.
  expect(screen.getByRole('button', { name: 'Switch to light theme' })).toBeInTheDocument()
})

it('switches from dark to light', async () => {
  const user = userEvent.setup()
  document.documentElement.dataset.theme = 'dark'
  render(<ThemeToggle />)

  await user.click(screen.getByRole('button', { name: 'Switch to light theme' }))

  expect(document.documentElement.dataset.theme).toBe('light')
  expect(localStorage.getItem('theme')).toBe('light')
})