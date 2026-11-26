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

it('gives the browser bar the color of the chosen theme', async () => {
  const user = userEvent.setup()
  // The two theme-color tags from index.html: one for a light, one for a dark system setting.
  document.head.innerHTML =
    '<meta name="theme-color" content="#f6f8fa" media="(prefers-color-scheme: light)">' +
    '<meta name="theme-color" content="#0d1117" media="(prefers-color-scheme: dark)">'
  document.documentElement.dataset.theme = 'light'
  render(<ThemeToggle />)

  await user.click(screen.getByRole('button', { name: 'Switch to dark theme' }))

  const colors = [...document.querySelectorAll('meta[name="theme-color"]')].map((meta) =>
    meta.getAttribute('content'),
  )
  expect(colors).toEqual(['#0d1117', '#0d1117'])
  document.head.innerHTML = ''
})