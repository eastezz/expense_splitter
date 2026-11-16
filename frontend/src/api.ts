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

/** The error body Spring Boot sends (a "ProblemDetail"). We only need two fields. */
type ProblemDetail = {
  title?: string
  detail?: string
}

/**
 * Does nothing if the response is OK (status 200-299).
 * Otherwise throws an Error with the backend's message.
 */
export async function throwIfNotOk(response: Response): Promise<void> {
  if (response.ok) {
    return
  }
  let message = `Request failed with status ${response.status}`
  try {
    const problem: ProblemDetail = await response.json()
    message = problem.detail ?? problem.title ?? message
  } catch {
    // The body was not JSON, so we keep the default message.
  }
  throw new Error(message)
}
