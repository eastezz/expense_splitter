import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it, vi } from 'vitest'
import type { Balance, Payment, Transfer } from './api'
import BalancesPage from './BalancesPage'

// Fake data for group 1, shaped like the backend's JSON.
// Anna paid 90 € and Ben 30 €, both shared equally by Anna, Ben and Cem.
const group = { id: 1, name: 'Weekend trip', createdAt: '2026-11-01T10:00:00Z' }

/** What the fake backend currently answers. A test can change it, e.g. after a payment. */
type BackendData = {
  balances: Balance[]
  transfers: Transfer[]
  payments: Payment[]
}

/** The state before anybody has paid back: Anna gets 50 €, Ben owes 10 € and Cem owes 40 €. */
function unsettledData(): BackendData {
  return {
    balances: [
      { memberId: 1, memberName: 'Anna', balanceCents: 5000 },
      { memberId: 2, memberName: 'Ben', balanceCents: -1000 },
      { memberId: 3, memberName: 'Cem', balanceCents: -4000 },
    ],
    transfers: [
      { fromMemberId: 3, fromMemberName: 'Cem', toMemberId: 1, toMemberName: 'Anna', amountCents: 4000 },
      { fromMemberId: 2, fromMemberName: 'Ben', toMemberId: 1, toMemberName: 'Anna', amountCents: 1000 },
    ],
    payments: [],
  }
}

/** Cem paid Anna 40 €, as the backend returns it. Noon UTC is the same day in every time zone near us. */
const cemPaidAnna: Payment = {
  id: 7,
  groupId: 1,
  fromMemberId: 3,
  fromMemberName: 'Cem',
  toMemberId: 1,
  toMemberName: 'Anna',
  amountCents: 4000,
  createdAt: '2026-11-17T12:00:00Z',
}

/** Ben paid Anna 10 €. */
const benPaidAnna: Payment = {
  ...cemPaidAnna,
  id: 8,
  fromMemberId: 2,
  fromMemberName: 'Ben',
  amountCents: 1000,
}

/** The state after both payments: every balance is 0 and nothing is left to transfer. */
function settledData(): BackendData {
  return {
    balances: [
      { memberId: 1, memberName: 'Anna', balanceCents: 0 },
      { memberId: 2, memberName: 'Ben', balanceCents: 0 },
      { memberId: 3, memberName: 'Cem', balanceCents: 0 },
    ],
    transfers: [],
    payments: [cemPaidAnna, benPaidAnna],
  }
}

/**
 * Replaces fetch with a fake backend. GET requests are answered from `data`.
 * POST requests are answered by `onWrite`, which each test can choose.
 */
function mockBackend(data: BackendData, onWrite?: (url: string, init: RequestInit) => Response) {
  const fetchMock = vi.fn(async (url: string, init?: RequestInit) => {
    if (init?.method !== undefined && init.method !== 'GET' && onWrite) {
      return onWrite(url, init)
    }
    if (url === '/api/groups/1') {
      return Response.json(group)
    }
    // We read `data` here, at request time, so a change made by onWrite shows up on reload.
    if (url === '/api/groups/1/balances') {
      return Response.json(data.balances)
    }
    if (url === '/api/groups/1/settlements') {
      return Response.json(data.transfers)
    }
    if (url === '/api/groups/1/payments') {
      return Response.json(data.payments)
    }
    return Response.json({ title: 'Not Found', detail: 'Unexpected request' }, { status: 404 })
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

/** Renders the balances page as if the browser were at /groups/1/balances. */
function renderBalancesPage() {
  render(
    <MemoryRouter initialEntries={['/groups/1/balances']}>
      <Routes>
        <Route path="/groups/:groupId/balances" element={<BalancesPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

/** The text of every row in the three lists, from top to bottom. */
function listRows(): (string | null)[] {
  return screen.getAllByRole('listitem').map((item) => item.textContent)
}

describe('BalancesPage', () => {
  it('shows every balance and the suggested transfers', async () => {
    mockBackend(unsettledData())

    renderBalancesPage()

    expect(await screen.findByRole('heading', { level: 2, name: 'Balances in Weekend trip' })).toBeInTheDocument()
    // \u00a0 is the non-breaking space Intl puts before the € sign.
    expect(listRows()).toEqual([
      'Anna gets back +50,00\u00a0€',
      'Ben owes -10,00\u00a0€',
      'Cem owes -40,00\u00a0€',
      'Cem pays Anna 40,00\u00a0€ Mark as paid',
      'Ben pays Anna 10,00\u00a0€ Mark as paid',
    ])
    expect(screen.getByText('No payments yet.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: '← Back to the group' })).toHaveAttribute('href', '/groups/1')
  })

  it('shows a message and the payments when everyone is settled up', async () => {
    mockBackend(settledData())

    renderBalancesPage()

    expect(await screen.findByText('Everyone is settled up. 🎉')).toBeInTheDocument()
    expect(listRows()).toEqual([
      'Anna is settled up 0,00 €',
      'Ben is settled up 0,00 €',
      'Cem is settled up 0,00 €',
      'Cem paid Anna 40,00 € 17.11.2026',
      'Ben paid Anna 10,00 € 17.11.2026',
    ])
    expect(screen.queryByRole('button', { name: 'Mark as paid' })).not.toBeInTheDocument()
  })
})
