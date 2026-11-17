import { useParams } from 'react-router'

/** The page of one group, at /groups/:groupId. For now it only shows the id from the URL. */
function GroupPage() {
  // useParams reads the ":groupId" part of the URL, e.g. "7" for /groups/7. It is always text.
  const { groupId } = useParams()

  return (
    <section>
      <h2>Group {groupId}</h2>
    </section>
  )
}

export default GroupPage
