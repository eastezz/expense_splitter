/**
 * Switches on the service worker (public/sw.js), so the app can be installed and opens offline.
 * Only in the real build: during `npm run dev` a service worker would keep serving old files
 * while you change the code.
 */
export function registerServiceWorker(): void {
  if (!import.meta.env.PROD || !('serviceWorker' in navigator)) {
    return
  }
  // After the page has loaded, so registering doesn't slow down the first visit.
  // once: true removes the listener after it ran, because it is only needed one time.
  window.addEventListener(
    'load',
    () => {
      navigator.serviceWorker.register('/sw.js').catch((error: unknown) => {
        // The app still works without it, just not offline.
        console.warn('Service worker registration failed:', error)
      })
    },
    { once: true },
  )
}
