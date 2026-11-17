import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import {
  addMember,
  createExpense,
  deleteExpense,
  getGroup,
  listExpenses,
  listMembers,
  type Expense,
  type Group,
  type Member,
  type SplitType,
} from './api'
import { formatCents, parseEuroToCents } from './money'
import './GroupPage.css'

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
  // The expenses of the group. Starts as an empty list.
  const [expenses, setExpenses] = useState<Expense[]>([])
  // The error message to show, or null when there is no error.
  const [error, setError] = useState<string | null>(null)
  // What the user has typed into the "New member name" box.
  const [memberName, setMemberName] = useState('')
  // The error from the last "Add member" attempt, or null.
  const [memberError, setMemberError] = useState<string | null>(null)
  // The fields of the "New expense" form, as typed by the user.
  const [description, setDescription] = useState('')
  const [amountText, setAmountText] = useState('')
  // The id of the member who paid, as text from the <select>. '' means nobody chosen yet.
  const [payerId, setPayerId] = useState('')
  // The ids of members whose box is NOT ticked. Everyone else shares the expense.
  // We store the unticked ones, so a newly added member is ticked automatically.
  const [excludedIds, setExcludedIds] = useState<number[]>([])
  // EQUAL = the backend divides the amount; EXACT = the user types each member's amount.
  const [splitType, setSplitType] = useState<SplitType>('EQUAL')
  // For EXACT: the amount typed per member, as text, looked up by member id.
  const [exactAmounts, setExactAmounts] = useState<Record<number, string>>({})
  // The error from the last "Add expense" attempt, or null.
  const [expenseError, setExpenseError] = useState<string | null>(null)
  // The error from the last "Delete" click, or null.
  const [deleteError, setDeleteError] = useState<string | null>(null)

  // Runs when the page appears, and again if the id in the URL changes.
  useEffect(() => {
    if (id === null) {
      return
    }
    async function load(groupIdToLoad: number) {
      try {
        // Promise.all starts all requests at the same time and waits until all have answered.
        const [loadedGroup, loadedMembers, loadedExpenses] = await Promise.all([
          getGroup(groupIdToLoad),
          listMembers(groupIdToLoad),
          listExpenses(groupIdToLoad),
        ])
        setGroup(loadedGroup)
        setMembers(loadedMembers)
        setExpenses(loadedExpenses)
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

  // The amount in cents, or null while the amount box holds no valid amount.
  const amountCents = parseEuroToCents(amountText)
  // The members whose box is ticked: they share the expense.
  const participants = members.filter((member) => !excludedIds.includes(member.id))

  // The amount typed for one member in an EXACT split, in cents, or null if it is not valid.
  function exactCentsOf(memberId: number): number | null {
    return parseEuroToCents(exactAmounts[memberId] ?? '')
  }
  // For EXACT: every ticked member needs a valid amount, and together they must match the total.
  const allExactValid = participants.every((member) => exactCentsOf(member.id) !== null)
  const assignedCents = participants.reduce(
    (sum, member) => sum + (exactCentsOf(member.id) ?? 0),
    0,
  )
  const remainingCents = (amountCents ?? 0) - assignedCents

  // The form may only be sent when every field holds something useful.
  const canAddExpense =
    description.trim() !== '' &&
    amountCents !== null &&
    amountCents > 0 &&
    payerId !== '' &&
    participants.length > 0 &&
    (splitType === 'EQUAL' || (allExactValid && remainingCents === 0))

  // Ticks or unticks one member's "shared by" box.
  function toggleParticipant(memberId: number) {
    if (excludedIds.includes(memberId)) {
      setExcludedIds(excludedIds.filter((excludedId) => excludedId !== memberId))
    } else {
      setExcludedIds([...excludedIds, memberId])
    }
  }

  // Sends the new expense to the backend, then shows it in the list and empties the form.
  // Only the ticked members share the cost.
  // On an error the form keeps what the user typed, so they can fix it and try again.
  async function handleAddExpense() {
    if (group === null || amountCents === null) {
      return
    }
    setExpenseError(null)
    try {
      const created = await createExpense(group.id, {
        description: description.trim(),
        amountCents,
        paidByMemberId: Number(payerId),
        splitType,
        // EQUAL: only the member ids (the backend calculates the amounts).
        // EXACT: each member id together with the amount the user typed.
        shares: participants.map((member) =>
          splitType === 'EQUAL'
            ? { memberId: member.id }
            : { memberId: member.id, amountCents: exactCentsOf(member.id) ?? 0 },
        ),
      })
      setExpenses([...expenses, created])
      setDescription('')
      setAmountText('')
      setPayerId('')
      setExcludedIds([])
      setSplitType('EQUAL')
      setExactAmounts({})
    } catch (err) {
      setExpenseError(err instanceof Error ? err.message : 'Unknown error')
    }
  }

  // Deletes one expense in the backend, then removes it from the list on screen.
  async function handleDeleteExpense(expenseId: number) {
    if (group === null) {
      return
    }
    setDeleteError(null)
    try {
      await deleteExpense(group.id, expenseId)
      setExpenses(expenses.filter((expense) => expense.id !== expenseId))
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Unknown error')
    }
  }

  // Finds a member's name by id, e.g. to show who paid an expense.
  function nameOf(memberId: number): string {
    const member = members.find((m) => m.id === memberId)
    return member ? member.name : 'Unknown member'
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
          <h3>Expenses</h3>
          {expenses.length === 0 && <p>No expenses yet.</p>}
          <ul className="expense-list">
            {expenses.map((expense) => (
              <li key={expense.id}>
                <span className="expense-description">{expense.description}</span>{' '}
                <span className="expense-amount">{formatCents(expense.amountCents)}</span>{' '}
                <span className="expense-payer">paid by {nameOf(expense.paidByMemberId)}</span>{' '}
                {/* aria-label tells screen readers (and tests) which expense this button deletes. */}
                <button
                  type="button"
                  className="delete-button"
                  aria-label={`Delete ${expense.description}`}
                  onClick={() => handleDeleteExpense(expense.id)}
                >
                  Delete
                </button>
              </li>
            ))}
          </ul>
          {deleteError && (
            <p role="alert" className="error">
              Could not delete expense: {deleteError}
            </p>
          )}
          <h3>New expense</h3>
          {members.length === 0 ? (
            <p>Add members before adding expenses.</p>
          ) : (
            <form
              className="expense-form"
              onSubmit={(event) => {
                event.preventDefault()
                handleAddExpense()
              }}
            >
              <label htmlFor="expense-description">Description</label>
              <input
                id="expense-description"
                value={description}
                onChange={(event) => setDescription(event.target.value)}
              />
              <label htmlFor="expense-amount">Amount (€)</label>
              {/* type="text" with inputMode="decimal": phones show a number keyboard,
                  but the user may still type a comma, like "12,34". */}
              <input
                id="expense-amount"
                type="text"
                inputMode="decimal"
                placeholder="12,34"
                value={amountText}
                onChange={(event) => setAmountText(event.target.value)}
              />
              <label htmlFor="expense-payer">Paid by</label>
              <select
                id="expense-payer"
                value={payerId}
                onChange={(event) => setPayerId(event.target.value)}
              >
                <option value="">Choose who paid</option>
                {members.map((member) => (
                  <option key={member.id} value={member.id}>
                    {member.name}
                  </option>
                ))}
              </select>
              {/* A fieldset groups related inputs; its legend is read out as their common title. */}
              <fieldset className="expense-split">
                <legend>Split</legend>
                {/* Radio buttons with the same name: only one of them can be chosen. */}
                <label>
                  <input
                    type="radio"
                    name="split-type"
                    checked={splitType === 'EQUAL'}
                    onChange={() => setSplitType('EQUAL')}
                  />{' '}
                  Equal
                </label>
                <label>
                  <input
                    type="radio"
                    name="split-type"
                    checked={splitType === 'EXACT'}
                    onChange={() => setSplitType('EXACT')}
                  />{' '}
                  Exact amounts
                </label>
              </fieldset>
              {/* A fieldset groups related inputs; its legend is read out as their common title. */}
              <fieldset className="expense-shares">
                <legend>Shared by</legend>
                {members.map((member) => {
                  const ticked = !excludedIds.includes(member.id)
                  return (
                    <div key={member.id} className="share-row">
                      <label>
                        <input
                          type="checkbox"
                          checked={ticked}
                          onChange={() => toggleParticipant(member.id)}
                        />{' '}
                        {member.name}
                      </label>
                      {splitType === 'EXACT' && ticked && (
                        <input
                          aria-label={`Amount for ${member.name}`}
                          type="text"
                          inputMode="decimal"
                          placeholder="0,00"
                          value={exactAmounts[member.id] ?? ''}
                          onChange={(event) =>
                            // Copy the old object and change only this member's entry.
                            setExactAmounts({ ...exactAmounts, [member.id]: event.target.value })
                          }
                        />
                      )}
                    </div>
                  )
                })}
                {splitType === 'EXACT' && (
                  <p className="remaining">Still to assign: {formatCents(remainingCents)}</p>
                )}
              </fieldset>
              <button type="submit" disabled={!canAddExpense}>
                Add expense
              </button>
            </form>
          )}
          {expenseError && (
            <p role="alert" className="error">
              Could not add expense: {expenseError}
            </p>
          )}
        </>
      )}
    </section>
  )
}

export default GroupPage
