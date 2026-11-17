// Helpers to turn cents into text and text into cents.
// We keep money as whole cents (numbers like 1234) so we never add up rounded decimals.

/** Formats euros the German way. Created once and reused, because creating it is slow. */
const euroFormat = new Intl.NumberFormat('de-DE', { style: 'currency', currency: 'EUR' })

/** Turns cents into text for the screen: 1234 -> "12,34 €". */
export function formatCents(cents: number): string {
  return euroFormat.format(cents / 100)
}

/**
 * Turns what the user typed into cents: "12,34" or "12.34" -> 1234, "12" -> 1200, "12,5" -> 1250.
 * Returns null if the text is not a valid amount (empty, letters, minus sign, more than 2 decimals).
 */
export function parseEuroToCents(text: string): number | null {
  // Digits, then optionally a comma or dot followed by 1 or 2 digits. Nothing else is allowed.
  const match = /^(\d+)(?:[.,](\d{1,2}))?$/.exec(text.trim())
  if (match === null) {
    return null
  }
  const euros = Number(match[1])
  // "5" means 50 cents and "05" means 5 cents: padEnd adds the missing zero at the end.
  const cents = Number((match[2] ?? '').padEnd(2, '0'))
  return euros * 100 + cents
}
