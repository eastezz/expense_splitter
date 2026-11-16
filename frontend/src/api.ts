// Shapes of the data the backend sends us, and (soon) the functions that call it.

/** A group as the backend returns it from /api/groups. */
export type Group = {
  id: number
  name: string
}
