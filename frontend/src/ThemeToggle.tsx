import { useState } from 'react'
import { currentTheme, setTheme, type Theme } from './theme'

/** The round button in the header that switches between the light and the dark theme. */
function ThemeToggle() {
  const [theme, setThemeState] = useState<Theme>(currentTheme)
  const next: Theme = theme === 'dark' ? 'light' : 'dark'

  function toggle() {
    setTheme(next)
    setThemeState(next)
  }

  return (
    <button
      type="button"
      className="theme-toggle"
      onClick={toggle}
      // Screen readers read this label instead of the emoji.
      aria-label={`Switch to ${next} theme`}
      title={`Switch to ${next} theme`}
    >
      {theme === 'dark' ? '☀️' : '🌙'}
    </button>
  )
}

export default ThemeToggle