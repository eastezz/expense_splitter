import { useState } from 'react'
import { IOS_HINT_DISMISSED_KEY, shouldShowIosHint } from './installHint'
import './IosInstallHint.css'

/**
 * iPhones and iPads have no "Install app" button for websites; you install them yourself
 * with Share → "Add to Home Screen". This small card explains that, only on iOS and only
 * until it is closed or the app is installed.
 */
function IosInstallHint() {
  // Decided once when the page opens.
  const [visible, setVisible] = useState(shouldShowIosHint)

  if (!visible) {
    return null
  }

  function close() {
    setVisible(false)
    try {
      localStorage.setItem(IOS_HINT_DISMISSED_KEY, 'true')
    } catch {
      // localStorage can be blocked; then it is only closed until the next visit.
    }
  }

  return (
    <aside className="ios-hint" aria-label="Install on iPhone">
      <p>
        <strong>Install expense_splitter:</strong> tap the Share button{' '}
        <span className="ios-hint-symbol" aria-hidden="true">
          ⬆
        </span>{' '}
        and then <strong>Add to Home Screen</strong>.
      </p>
      <button type="button" className="ios-hint-close" aria-label="Close the install hint" onClick={close}>
        ×
      </button>
    </aside>
  )
}

export default IosInstallHint
