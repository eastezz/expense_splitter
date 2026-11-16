// Shapes of the data the backend sends us, and (soon) the functions that call it.

/** A group as the backend returns it from /api/groups. */
export type Group = {
  id: number
  name: string
}

/** Asks the backend for all groups (GET /api/groups). */
export async function listGroups(): Promise<Group[]> {
  const response = await fetch('/api/groups')
  await throwIfNotOk(response)
  return response.json()
}

/** The error body Spring Boot sends (a "ProblemDetail"). We only need two fields. */
type ProblemDetail = {
  title?: string
  detail?: string
}

/**
 * Does nothing if the response is OK (status 200-299).
 * Otherwise throws an Error with the backend's message.
 */
export async function throwIfNotOk(response: Response): Promise<void> {
  if (response.ok) {
    return
  }
  let message = `Request failed with status ${response.status}`
  try {
    const problem: ProblemDetail = await response.json()
    message = problem.detail ?? problem.title ?? message
  } catch {
    // The body was not JSON, so we keep the default message.
  }
  throw new Error(message)
}

/** Creates a new group (POST /api/groups) and returns it as the backend saved it. */
export async function createGroup(name: string): Promise<Group> {
  const response = await fetch('/api/groups', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  })
  await throwIfNotOk(response)
  return response.json()
}

/** A member of a group as the backend returns it. */
export type Member = {
  id: number
  name: string
  groupId: number
}

/** Asks the backend for all members of one group (GET /api/groups/{groupId}/members). */
export async function listMembers(groupId: number): Promise<Member[]> {
  const response = await fetch(`/api/groups/${groupId}/members`)
  await throwIfNotOk(response)
  return response.json()
}

/**
 * Adds a member to a group (POST /api/groups/{groupId}/members) and returns it.
 * If the name is already taken in this group, the backend answers 409 and we throw its message.
 */
export async function addMember(groupId: number, name: string): Promise<Member> {
  const response = await fetch(`/api/groups/${groupId}/members`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name }),
  })
  await throwIfNotOk(response)
  return response.json()
}

/** How an expense is divided: EQUAL = same share for everyone, EXACT = each amount typed by hand. */
export type SplitType = 'EQUAL' | 'EXACT'

/** One member's part of an expense, as the backend returns it. */
export type Share = {
  memberId: number
  memberName: string
  shareCents: number
}

/** An expense as the backend returns it. All money is in whole cents (1234 = 12,34 €). */
export type Expense = {
  id: number
  groupId: number
  description: string
  amountCents: number
  paidByMemberId: number
  splitType: SplitType
  createdAt: string
  shares: Share[]
}

/** Asks the backend for all expenses of one group (GET /api/groups/{groupId}/expenses). */
export async function listExpenses(groupId: number): Promise<Expense[]> {
  const response = await fetch(`/api/groups/${groupId}/expenses`)
  await throwIfNotOk(response)
  return response.json()
}

/**
 * One member taking part in a new expense.
 * amountCents is only sent for an EXACT split; for EQUAL the backend calculates it.
 */
export type NewShare = {
  memberId: number
  amountCents?: number
}

/** What we send to create an expense (the backend's CreateExpenseRequest). */
export type NewExpense = {
  description: string
  amountCents: number
  paidByMemberId: number
  splitType: SplitType
  shares: NewShare[]
}

/** Creates an expense in a group (POST /api/groups/{groupId}/expenses) and returns it. */
export async function createExpense(groupId: number, expense: NewExpense): Promise<Expense> {
  const response = await fetch(`/api/groups/${groupId}/expenses`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(expense),
  })
  await throwIfNotOk(response)
  return response.json()
}

/**
 * Deletes one expense (DELETE /api/groups/{groupId}/expenses/{expenseId}).
 * The backend answers 204 No Content, so there is no body to read and we return nothing.
 */
export async function deleteExpense(groupId: number, expenseId: number): Promise<void> {
  const response = await fetch(`/api/groups/${groupId}/expenses/${expenseId}`, {
    method: 'DELETE',
  })
  await throwIfNotOk(response)
}
