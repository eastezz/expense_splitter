import { useEffect, useState } from 'react'
import { listGroups, type Group } from './api'

/** The start page: loads all groups from the backend and lists them. */
function GroupsPage() {
  // The groups we have loaded so far. Starts as an empty list.
  const [groups, setGroups] = useState<Group[]>([])
  // True until the first answer from the backend arrives.
  const [loading, setLoading] = useState(true)

  // Runs once, after the page first appears on screen.
  useEffect(() => {
    async function load() {
      const loaded = await listGroups()
      setGroups(loaded)
      setLoading(false)
    }
    load()
  }, [])

  return (
    <section>
      <h2>Groups</h2>
      {loading && <p>Loading groups…</p>}
      <ul>
        {groups.map((group) => (
          <li key={group.id}>{group.name}</li>
        ))}
      </ul>
    </section>
  )
}

export default GroupsPage
