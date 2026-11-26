/** The two color themes of the app. */
export type Theme = 'light' | 'dark'

const STORAGE_KEY = 'theme'

/** The theme that is active right now (index.html sets it before the app starts). */
export function currentTheme(): Theme {
  return document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light'
}

/** The page background of each theme, also used for the browser bar and the phone's status bar. */
const BAR_COLORS: Record<Theme, string> = { light: '#f6f8fa', dark: '#0d1117' }

/**
 * Gives the browser bar and the phone's status bar the color of the chosen theme.
 * index.html has two theme-color tags (one for a light, one for a dark system setting);
 * after a choice with the theme button, both get the chosen color.
 */
export function applyBarColor(theme: Theme): void {
  document.querySelectorAll<HTMLMetaElement>('meta[name="theme-color"]').forEach((meta) => {
    meta.content = BAR_COLORS[theme]
  })
}

/** Switches the whole app to the given theme and remembers the choice for the next visit. */
export function setTheme(theme: Theme): void {
  document.documentElement.dataset.theme = theme
  applyBarColor(theme)
  try {
    localStorage.setItem(STORAGE_KEY, theme)
  } catch {
    // localStorage can be blocked (for example in private mode); the theme still changes.
  }
}