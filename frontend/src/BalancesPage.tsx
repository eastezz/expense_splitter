import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { getGroup, type Group } from './api'
import { parseGroupId } from './groupId'

/**
 * The balances page of one group, at /groups/:groupId/balances.
 * Shows who gets money back, who owes money, and who should pay whom to settle up.
 */
function BalancesPage() {
  // useParams reads the ":groupId" part of the URL, e.g. "7" for /groups/7/balances.
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
        <Link to={`/groups/${groupId}`}>← Back to the group</Link>
      </p>
      {id === null && <p role="alert" className="error">This is not a valid group address.</p>}
      {id !== null && group === null && error === null && <p>Loading balances…</p>}
      {error && <p role="alert" className="error">Could not load balances: {error}</p>}
      {group && <h2>Balances in {group.name}</h2>}
    </section>
  )
}

export default BalancesPage
