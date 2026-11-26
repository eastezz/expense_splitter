import { useEffect, useState } from 'react'

/**
 * The event Chrome, Edge and Android send when the app can be installed.
 * TypeScript doesn't know it yet, so we describe the two parts we use.
 */
type BeforeInstallPromptEvent = Event & {
  /** Opens the browser's own "Install app?" dialog. */
  prompt: () => Promise<void>
  /** Tells whether the user said yes or no in that dialog. */
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>
}

/**
 * "Install app" in the header. It only appears when the browser says the app can be installed
 * (Chrome, Edge, Android) and disappears again after installing.
 * iPhones never send this event; they get their own hint (see IosInstallHint).
 */
function InstallButton() {
  // The saved event, or null while the browser hasn't offered an install (or it was used up).
  const [installEvent, setInstallEvent] = useState<BeforeInstallPromptEvent | null>(null)

  useEffect(() => {
    function handleInstallable(event: Event) {
      // Stops the browser's own small banner, so our button decides when to ask.
      event.preventDefault()
      setInstallEvent(event as BeforeInstallPromptEvent)
    }
    function handleInstalled() {
      setInstallEvent(null)
    }
    window.addEventListener('beforeinstallprompt', handleInstallable)
    window.addEventListener('appinstalled', handleInstalled)
    // Removes the listeners when the header disappears, so they don't pile up.
    return () => {
      window.removeEventListener('beforeinstallprompt', handleInstallable)
      window.removeEventListener('appinstalled', handleInstalled)
    }
  }, [])

  if (installEvent === null) {
    return null
  }

  async function handleClick() {
    if (installEvent === null) {
      return
    }
    await installEvent.prompt()
    await installEvent.userChoice
    // The browser allows only one prompt() per event, so the button goes away either way.
    setInstallEvent(null)
  }

  return (
    <button type="button" className="install-button" onClick={handleClick}>
      Install app
    </button>
  )
}

export default InstallButton
