/** The two color themes of the app. */
export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'theme'

/** The theme that is active right now (index.html sets it before the app starts). */
export function currentTheme(): Theme {
  return document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light'
}

/** Switches the whole app to the given theme and remembers the choice for the next visit. */
export function setTheme(theme: Theme): void {
  document.documentElement.dataset.theme = theme
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch {
    // localStorage can be blocked (for example in private mode); the theme still changes.
  }
}