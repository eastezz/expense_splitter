import { render, screen } from '@testing-library/react'
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

describe('GroupPage', () => {
  it('shows the group name, its members and its expenses', async () => {
    mockBackend()

    renderGroupPage()

    expect(await screen.findByRole('heading', { level: 2, name: 'Trip to Berlin' })).toBeInTheDocument()
    // \u00a0 is the non-breaking space Intl puts before the € sign.
    expect(listRows()).toEqual(['Anna', 'Ben', 'Dinner 90,00\u00a0€ paid by Anna Delete'])
    expect(screen.getByRole('link', { name: '← All groups' })).toHaveAttribute('href', '/')
  })
})
