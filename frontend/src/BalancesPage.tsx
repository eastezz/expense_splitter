import { Link, useParams } from 'react-router'

/**
 * The balances page of one group, at /groups/:groupId/balances.
 * For now it only shows a heading; the next pieces load and show the balances.
 */
function BalancesPage() {
  // useParams reads the ":groupId" part of the URL, e.g. "7" for /groups/7/balances.
  const { groupId } = useParams()

  return (
    <section>
      <p>
        <Link to={`/groups/${groupId}`}>← Back to the group</Link>
      </p>
      <h2>Balances</h2>
    </section>
  )
}

export default BalancesPage
