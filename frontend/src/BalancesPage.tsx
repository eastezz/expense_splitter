import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { getGroup, listBalances, type Balance, type Group } from './api'
import { parseGroupId } from './groupId'
import { formatCents } from './money'

/** Shows the sign of a balance: 5000 -> "+50,00 €", -1000 -> "-10,00 €", 0 -> "0,00 €". */
function formatSignedCents(cents: number): string {
  // formatCents already writes the minus sign; we only add the plus sign ourselves.
  return cents > 0 ? `+${formatCents(cents)}` : formatCents(cents)
}

/** Says in words what a balance means for the member. */
function balanceWording(cents: number): string {
  if (cents > 0) {
    return 'gets back'
  }
  if (cents < 0) {
    return 'owes'
  }
  return 'is settled up'
}

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
  // Every member's balance. Starts as an empty list.
  const [balances, setBalances] = useState<Balance[]>([])
  // The error message to show, or null when there is no error.
  const [error, setError] = useState<string | null>(null)

  // Runs when the page appears, and again if the id in the URL changes.
  useEffect(() => {
    if (id === null) {
      return
    }
    async function load(groupIdToLoad: number) {
      try {
        // Promise.all starts both requests at the same time and waits until both have answered.
        const [loadedGroup, loadedBalances] = await Promise.all([
          getGroup(groupIdToLoad),
          listBalances(groupIdToLoad),
        ])
        setGroup(loadedGroup)
        setBalances(loadedBalances)
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
      {group && (
        <>
          <h2>Balances in {group.name}</h2>
          {balances.length === 0 && <p>No members yet.</p>}
          <ul className="balance-list">
            {balances.map((balance) => (
              <li key={balance.memberId}>
                <span className="balance-name">{balance.memberName}</span>{' '}
                <span className="balance-wording">{balanceWording(balance.balanceCents)}</span>{' '}
                <span className="balance-amount">{formatSignedCents(balance.balanceCents)}</span>
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  )
}

export default BalancesPage
