// Shapes of the data the backend sends us, and (soon) the functions that call it.

/** A group as the backend returns it from /api/groups. */
export type Group = {
  id: number
  name: string
}

/** Asks the backend for all groups (GET /api/groups). */
export async function listGroups(): Promise<Group[]> {
  const response = await fetch('/api/groups')
  return response.json()
}
