/** Where we remember that the user closed the iPhone install hint. */
export const IOS_HINT_DISMISSED_KEY = 'ios-install-hint-dismissed'

/**
 * True on an iPhone or iPad. New iPads say "Macintosh" in their user agent like a Mac,
 * but unlike a Mac they have a touch screen, which is how we tell them apart.
 */
export function isIos(userAgent: string, maxTouchPoints: number): boolean {
  return /iPhone|iPad|iPod/.test(userAgent) || (userAgent.includes('Macintosh') && maxTouchPoints > 1)
}

/** True when the app already runs as an installed app (opened from the home screen). */
export function isInstalledApp(): boolean {
  // navigator.standalone only exists on iPhones; display-mode works everywhere else.
  const iosStandalone = (navigator as Navigator & { standalone?: boolean }).standalone === true
  return iosStandalone || window.matchMedia?.('(display-mode: standalone)').matches === true
}

/** Whether the iPhone hint should be shown: on iOS, not installed yet, and not closed before. */
export function shouldShowIosHint(): boolean {
  let dismissed = false
  try {
    dismissed = localStorage.getItem(IOS_HINT_DISMISSED_KEY) === 'true'
  } catch {
    // localStorage can be blocked (private mode); then the hint simply shows again next time.
  }
  return isIos(navigator.userAgent, navigator.maxTouchPoints) && !isInstalledApp() && !dismissed
}
