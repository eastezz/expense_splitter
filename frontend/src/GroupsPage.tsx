import { useEffect, useState } from 'react'
import { createGroup, listGroups, type Group } from './api'

/** The start page: loads all groups from the backend and lists them. */
function GroupsPage() {
  // The groups we have loaded so far. Starts as an empty list.
  const [groups, setGroups] = useState<Group[]>([])
  // True until the first answer from the backend arrives.
  const [loading, setLoading] = useState(true)
  // The error message to show, or null when there is no error.
  const [error, setError] = useState<string | null>(null)
  // What the user has typed into the "New group name" box.
  const [name, setName] = useState('')
  // The error from the last "Create group" attempt, or null.
  const [createError, setCreateError] = useState<string | null>(null)

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

  // Sends the new group to the backend, then shows it in the list.
  async function handleCreate() {
    setCreateError(null)
    try {
      const created = await createGroup(name.trim())
      setGroups([...groups, created])
      setName('')
    } catch (err) {
      setCreateError(err instanceof Error ? err.message : 'Unknown error')
    }
  }

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
      <form
        onSubmit={(event) => {
          event.preventDefault()
          handleCreate()
        }}
      >
        <label htmlFor="group-name">New group name</label>
        <input
          id="group-name"
          value={name}
          onChange={(event) => setName(event.target.value)}
        />
        <button type="submit" disabled={name.trim() === ''}>
          Create group
        </button>
      </form>
      {createError && <p role="alert">Could not create group: {createError}</p>}
    </section>
  )
}

export default GroupsPage
