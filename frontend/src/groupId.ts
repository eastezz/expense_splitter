/** Turns the id from the URL into a number, or null if it is not a positive whole number. */
export function parseGroupId(text: string | undefined): number | null {
  const id = Number(text)
  return Number.isInteger(id) && id > 0 ? id : null
}
