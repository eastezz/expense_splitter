import { useEffect, useState } from 'react'
import { listGroups, type Group } from './api'

/** The start page: loads all groups from the backend and lists them. */
function GroupsPage() {
  // The groups we have loaded so far. Starts as an empty list.
  const [groups, setGroups] = useState<Group[]>([])
  // True until the first answer from the backend arrives.
  const [loading, setLoading] = useState(true)
  // The error message to show, or null when there is no error.
  const [error, setError] = useState<string | null>(null)

  // Runs once, after the page first appears on screen.
  useEffect(() => {
    async function load() {
      try {
        const loaded = await listGroups()
        setGroups(loaded)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  return (
    <section>
      <h2>Groups</h2>
      {loading && <p>Loading groups…</p>}
      {error && <p role="alert">Could not load groups: {error}</p>}
      {!loading && !error && groups.length === 0 && <p>No groups yet.</p>}
      <ul>
        {groups.map((group) => (
          <li key={group.id}>{group.name}</li>
        ))}
      </ul>
    </section>
  )
}

export default GroupsPage
