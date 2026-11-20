/** How many different circle colors there are (avatar-color-0 … avatar-color-5 in Avatar.css). */
export const AVATAR_COLOR_COUNT = 6

/**
 * Picks a color number for a name. The same name always gets the same color,
 * on every page and after every reload.
 * It uses the small hash function FNV-1a: it mixes every character into a number with XOR
 * and a multiplication, so similar names like "Ben" and "Cem" usually end up far apart.
 * (Simply adding up the character codes gives both 277, so they would share a color.)
 */
export function avatarColorIndex(name: string): number {
  let hash = 2166136261
  for (const char of name) {
    hash ^= char.codePointAt(0) ?? 0
    // Math.imul multiplies like a 32-bit number; >>> 0 keeps the result positive.
    hash = Math.imul(hash, 16777619) >>> 0
  }
  return hash % AVATAR_COLOR_COUNT
}

/** The first letter of a name in upper case, for example "anna" -> "A". */
export function initialOf(name: string): string {
  // [...text] splits into real characters, so an emoji or "Ö" is not cut in half.
  const first = [...name.trim()][0]
  return first ? first.toUpperCase() : '?'
}