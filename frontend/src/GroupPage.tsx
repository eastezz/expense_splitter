import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { addMember, getGroup, listMembers, type Group, type Member } from './api'

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
  // The members of the group. Starts as an empty list.
  const [members, setMembers] = useState<Member[]>([])
  // The error message to show, or null when there is no error.
  const [error, setError] = useState<string | null>(null)
  // What the user has typed into the "New member name" box.
  const [memberName, setMemberName] = useState('')
  // The error from the last "Add member" attempt, or null.
  const [memberError, setMemberError] = useState<string | null>(null)

  // Runs when the page appears, and again if the id in the URL changes.
  useEffect(() => {
    if (id === null) {
      return
    }
    async function load(groupIdToLoad: number) {
      try {
        // Promise.all starts both requests at the same time and waits until both have answered.
        const [loadedGroup, loadedMembers] = await Promise.all([
          getGroup(groupIdToLoad),
          listMembers(groupIdToLoad),
        ])
        setGroup(loadedGroup)
        setMembers(loadedMembers)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unknown error')
      }
    }
    load(id)
  }, [id])

  // Sends the new member to the backend, then shows it in the list.
  async function handleAddMember() {
    if (group === null) {
      return
    }
    setMemberError(null)
    try {
      const created = await addMember(group.id, memberName.trim())
      setMembers([...members, created])
      setMemberName('')
    } catch (err) {
      setMemberError(err instanceof Error ? err.message : 'Unknown error')
    }
  }

  return (
    <section>
      <p>
        <Link to="/">← All groups</Link>
      </p>
      {id === null && <p role="alert" className="error">This is not a valid group address.</p>}
      {id !== null && group === null && error === null && <p>Loading group…</p>}
      {error && <p role="alert" className="error">Could not load group: {error}</p>}
      {group && (
        <>
          <h2>{group.name}</h2>
          <h3>Members</h3>
          {members.length === 0 && <p>No members yet.</p>}
          <ul className="member-list">
            {members.map((member) => (
              <li key={member.id}>{member.name}</li>
            ))}
          </ul>
          <form
            className="member-form"
            onSubmit={(event) => {
              event.preventDefault()
              handleAddMember()
            }}
          >
            <label htmlFor="member-name">New member name</label>
            <input
              id="member-name"
              value={memberName}
              onChange={(event) => setMemberName(event.target.value)}
            />
            <button type="submit" disabled={memberName.trim() === ''}>
              Add member
            </button>
          </form>
          {memberError && <p role="alert" className="error">Could not add member: {memberError}</p>}
        </>
      )}
    </section>
  )
}

export default GroupPage
