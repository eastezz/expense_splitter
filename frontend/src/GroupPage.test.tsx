import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import GroupPage from './GroupPage'

// Fake data for group 1: two members and one expense, shaped like the backend's JSON.
const group = { id: 1, name: 'Trip to Berlin', createdAt: '2026-11-01T10:00:00Z' }
const anna = { id: 10, name: 'Anna', groupId: 1 }
const ben = { id: 11, name: 'Ben', groupId: 1 }
const dinner = {
  id: 100,
  groupId: 1,
  description: 'Dinner',
  amountCents: 9000,
  paidByMemberId: 10,
  splitType: 'EQUAL',
  createdAt: '2026-11-01T20:00:00Z',
  shares: [
    { memberId: 10, memberName: 'Anna', shareCents: 4500 },
    { memberId: 11, memberName: 'Ben', shareCents: 4500 },
  ],
}

/**
 * Replaces fetch with a fake backend. GET requests are answered with the data above.
 * POST and DELETE requests are answered by `onWrite`, which each test can choose.
 */
function mockBackend(onWrite?: (url: string, init: RequestInit) => Response) {
  const fetchMock = vi.fn(async (url: string, init?: RequestInit) => {
    if (init?.method !== undefined && init.method !== 'GET' && onWrite) {
      return onWrite(url, init)
    }
    if (url === '/api/groups/1') {
      return Response.json(group)
    }
    if (url === '/api/groups/1/members') {
      return Response.json([anna, ben])
    }
    if (url === '/api/groups/1/expenses') {
      return Response.json([dinner])
    }
    return Response.json({ title: 'Not Found', detail: 'Unexpected request' }, { status: 404 })
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

/** Renders the group page as if the browser were at /groups/1. */
function renderGroupPage() {
  render(
    <MemoryRouter initialEntries={['/groups/1']}>
      <Routes>
        <Route path="/groups/:groupId" element={<GroupPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

/** The text of every row in the member and expense lists, from top to bottom. */
function listRows(): (string | null)[] {
  return screen.getAllByRole('listitem').map((item) => item.textContent)
}

/** Reads the JSON body that the page sent with the last fetch call. */
function lastSentBody(fetchMock: ReturnType<typeof mockBackend>): unknown {
  const init = fetchMock.mock.lastCall?.[1]
  return JSON.parse(String(init?.body))
}

describe('GroupPage', () => {
  it('shows the group name, its members and its expenses', async () => {
    mockBackend()

    renderGroupPage()

    expect(await screen.findByRole('heading', { level: 2, name: 'Trip to Berlin' })).toBeInTheDocument()
    // \u00a0 is the non-breaking space Intl puts before the € sign.
    expect(listRows()).toEqual(['Anna', 'Ben', 'Dinner 90,00\u00a0€ paid by Anna Delete'])
    expect(screen.getByRole('link', { name: '← All groups' })).toHaveAttribute('href', '/')
  })

  it('adds a member and shows it in the list', async () => {
    // The backend answers the POST with the saved member.
    const fetchMock = mockBackend(() =>
      Response.json({ id: 12, name: 'Cem', groupId: 1 }, { status: 201 }),
    )
    const user = userEvent.setup()

    renderGroupPage()
    await screen.findByRole('heading', { level: 2, name: 'Trip to Berlin' })

    const input = screen.getByLabelText('New member name')
    await user.type(input, 'Cem')
    await user.click(screen.getByRole('button', { name: 'Add member' }))

    // waitFor repeats the check until it passes (or times out), because saving is asynchronous.
    await waitFor(() => expect(listRows()).toContain('Cem'))
    expect(input).toHaveValue('')
    expect(fetchMock).toHaveBeenLastCalledWith('/api/groups/1/members', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: 'Cem' }),
    })
  })

  it('adds an equal expense shared by the ticked members', async () => {
    const taxi = {
      id: 101,
      groupId: 1,
      description: 'Taxi',
      amountCents: 1250,
      paidByMemberId: 11,
      splitType: 'EQUAL',
      createdAt: '2026-11-02T08:00:00Z',
      shares: [{ memberId: 11, memberName: 'Ben', shareCents: 1250 }],
    }
    const fetchMock = mockBackend(() => Response.json(taxi, { status: 201 }))
    const user = userEvent.setup()

    renderGroupPage()
    await screen.findByRole('heading', { level: 2, name: 'Trip to Berlin' })

    await user.type(screen.getByLabelText('Description'), 'Taxi')
    await user.type(screen.getByLabelText('Amount (€)'), '12,50')
    await user.selectOptions(screen.getByLabelText('Paid by'), 'Ben')
    // Only Ben takes the taxi, so we untick Anna.
    await user.click(screen.getByRole('checkbox', { name: 'Anna' }))
    await user.click(screen.getByRole('button', { name: 'Add expense' }))

    await waitFor(() => expect(listRows()).toHaveLength(4))
    expect(fetchMock.mock.lastCall?.[0]).toBe('/api/groups/1/expenses')
    // An EQUAL split sends only member ids; the backend calculates the amounts.
    expect(lastSentBody(fetchMock)).toEqual({
      description: 'Taxi',
      amountCents: 1250,
      paidByMemberId: 11,
      splitType: 'EQUAL',
      shares: [{ memberId: 11 }],
    })
    // After success the form is empty again.
    expect(screen.getByLabelText('Description')).toHaveValue('')
  })
})
