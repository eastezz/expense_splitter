import { avatarColorIndex, initialOf } from './avatarColor'
import './Avatar.css'

/**
 * A colored circle with the first letter of a member's name, shown next to the name.
 * aria-hidden: screen readers skip it, because they already read the name itself.
 */
function Avatar({ name }: { name: string }) {
  return (
    <span
      className={`avatar avatar-color-${avatarColorIndex(name)}`}
      // The letter is drawn by CSS from this attribute (see Avatar.css).
      data-initial={initialOf(name)}
      aria-hidden="true"
    />
  )
}

export default Avatar