import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import {
  getGroup,
  listBalances,
  listSettlements,
  type Balance,
  type Group,
  type Transfer,
} from './api'
import { parseGroupId } from './groupId'
import { formatCents } from './money'
import './BalancesPage.css'

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

/** The CSS class that colors a balance: green when positive, red when negative. */
function balanceClass(cents: number): string {
  if (cents > 0) {
    return 'balance-positive'
  }
  if (cents < 0) {
    return 'balance-negative'
  }
  return 'balance-zero'
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
  // The suggested transfers: who should pay whom so that everyone ends at 0.
  const [transfers, setTransfers] = useState<Transfer[]>([])
  // The error message to show, or null when there is no error.
  const [error, setError] = useState<string | null>(null)

  // Runs when the page appears, and again if the id in the URL changes.
  useEffect(() => {
    if (id === null) {
      return
    }
    async function load(groupIdToLoad: number) {
      try {
        // Promise.all starts all requests at the same time and waits until all have answered.
        const [loadedGroup, loadedBalances, loadedTransfers] = await Promise.all([
          getGroup(groupIdToLoad),
          listBalances(groupIdToLoad),
          listSettlements(groupIdToLoad),
        ])
        setGroup(loadedGroup)
        setBalances(loadedBalances)
        setTransfers(loadedTransfers)
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
              <li key={balance.memberId} className={balanceClass(balance.balanceCents)}>
                <span className="balance-name">{balance.memberName}</span>{' '}
                <span className="balance-wording">{balanceWording(balance.balanceCents)}</span>{' '}
                <span className="balance-amount">{formatSignedCents(balance.balanceCents)}</span>
              </li>
            ))}
          </ul>
          <h3>Settle up</h3>
          <ul className="transfer-list">
            {transfers.map((transfer) => (
              // A pair of members appears at most once, so their two ids make a unique key.
              <li key={`${transfer.fromMemberId}-${transfer.toMemberId}`}>
                <span className="transfer-text">
                  {transfer.fromMemberName} pays {transfer.toMemberName}
                </span>{' '}
                <span className="transfer-amount">{formatCents(transfer.amountCents)}</span>
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  )
}

export default BalancesPage
