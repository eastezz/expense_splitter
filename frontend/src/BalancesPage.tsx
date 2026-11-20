import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import {
  createPayment,
  getGroup,
  listBalances,
  listPayments,
  listSettlements,
  type Balance,
  type Group,
  type Payment,
  type Transfer,
} from './api'
import { parseGroupId } from './groupId'
import { formatCents } from './money'
import './BalancesPage.css'
import LoadingCard from './LoadingCard'
import { useToast } from './toast'

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
 * Loads the balances, the suggested transfers and the payments of a group at the same time.
 * Used when the page appears and again after a payment, because a payment changes all three.
 */
function loadSettlement(groupId: number): Promise<[Balance[], Transfer[], Payment[]]> {
  return Promise.all([listBalances(groupId), listSettlements(groupId), listPayments(groupId)])
}

/** Shows the day of a payment the German way: "2026-11-17T15:00:00Z" -> "17.11.2026". */
function formatDate(isoText: string): string {
  return new Date(isoText).toLocaleDateString('de-DE')
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
  // The payments recorded so far, oldest first.
  const [payments, setPayments] = useState<Payment[]>([])
  // The error message to show, or null when there is no error.
  const [error, setError] = useState<string | null>(null)
  // The error from the last "Mark as paid" click, or null.
  const [paymentError, setPaymentError] = useState<string | null>(null)
  const showToast = useToast()

  // Runs when the page appears, and again if the id in the URL changes.
  useEffect(() => {
    if (id === null) {
      return
    }
    async function load(groupIdToLoad: number) {
      try {
        // Promise.all starts both requests at the same time and waits until both have answered.
        const [loadedGroup, [loadedBalances, loadedTransfers, loadedPayments]] = await Promise.all([
          getGroup(groupIdToLoad),
          loadSettlement(groupIdToLoad),
        ])
        setGroup(loadedGroup)
        setBalances(loadedBalances)
        setTransfers(loadedTransfers)
        setPayments(loadedPayments)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error')
      }
    }
    load(id)
  }, [id])

  // Records that one suggested transfer has been paid, by saving it as a payment.
  // Then asks the backend again, because the payment changes the balances and transfers.
  async function handleMarkPaid(transfer: Transfer) {
    if (group === null) {
      return
    }
    setPaymentError(null)
    try {
      await createPayment(group.id, {
        fromMemberId: transfer.fromMemberId,
        toMemberId: transfer.toMemberId,
        amountCents: transfer.amountCents,
      })
      const [newBalances, newTransfers, newPayments] = await loadSettlement(group.id)
      setBalances(newBalances)
      setTransfers(newTransfers)
      setPayments(newPayments)
      showToast(`${transfer.fromMemberName} paid ${transfer.toMemberName} ${formatCents(transfer.amountCents)}`)
    } catch (err) {
      setPaymentError(err instanceof Error ? err.message : 'Unknown error')
    }
  }

  return (
    <section>
      <p>
        <Link to={`/groups/${groupId}`}>← Back to the group</Link>
      </p>
      {id === null && <p role="alert" className="error">This is not a valid group address.</p>}
      {id !== null && group === null && error === null && <LoadingCard label="Loading balances…" />}
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
          {/* No transfers left means every balance is 0. Without members there is nothing to settle. */}
          {transfers.length === 0 && balances.length > 0 && (
            <p className="settled">Everyone is settled up. 🎉</p>
          )}
          <ul className="transfer-list">
            {transfers.map((transfer) => (
              // A pair of members appears at most once, so their two ids make a unique key.
              <li key={`${transfer.fromMemberId}-${transfer.toMemberId}`}>
                <span className="transfer-text">
                  {transfer.fromMemberName} pays {transfer.toMemberName}
                </span>{' '}
                <span className="transfer-amount">{formatCents(transfer.amountCents)}</span>{' '}
                <button type="button" className="pay-button" onClick={() => handleMarkPaid(transfer)}>
                  Mark as paid
                </button>
              </li>
            ))}
          </ul>
          {paymentError && (
            <p role="alert" className="error">
              Could not record payment: {paymentError}
            </p>
          )}
          <h3>Recorded payments</h3>
          {payments.length === 0 && <p>No payments yet.</p>}
          <ul className="payment-list">
            {payments.map((payment) => (
              <li key={payment.id}>
                <span className="payment-text">
                  {payment.fromMemberName} paid {payment.toMemberName}
                </span>{' '}
                <span className="payment-amount">{formatCents(payment.amountCents)}</span>{' '}
                <span className="payment-date">{formatDate(payment.createdAt)}</span>
              </li>
            ))}
          </ul>
        </>
      )}
    </section>
  )
}

export default BalancesPage
