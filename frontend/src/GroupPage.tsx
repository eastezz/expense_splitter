import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { getGroup, type Group } from './api'

/** Turns the id from the URL into a number, or null if it is not a positive whole number. */
function parseGroupId(text: string | undefined): number | null {
  const id = Number(text)
  return Number.isInteger(id) && id > 0 ? id : null
}

/** The page of one group, at /groups/:groupId. Loads the group and shows its name. */
function GroupPage() {
  // useParams reads the ":groupId" part of the URL, e.g. "7" for /groups/7. It is always text.
  const { groupId } = useParams()
  const id = parseGroupId(groupId)

  // The group once it has loaded, or null before that.
  const [group, setGroup] = useState<Group | null>(null)
  // The error message to show, or null when there is no error.
  const [error, setError] = useState<string | null>(null)

  // Runs when the page appears, and again if the id in the URL changes.
  useEffect(() => {
    if (id === null) {
      return
    }
    async function load(groupIdToLoad: number) {
      try {
        setGroup(await getGroup(groupIdToLoad))
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error')
      }
    }
    load(id)
  }, [id])

  return (
    <section>
      <p>
        <Link to="/">← All groups</Link>
      </p>
      {id === null && <p role="alert" className="error">This is not a valid group address.</p>}
      {id !== null && group === null && error === null && <p>Loading group…</p>}
      {error && <p role="alert" className="error">Could not load group: {error}</p>}
      {group && <h2>{group.name}</h2>}
    </section>
  )
}

export default GroupPage
