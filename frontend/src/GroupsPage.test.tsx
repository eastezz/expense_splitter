import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import GroupsPage from './GroupsPage'

/** Replaces the browser's fetch with a fake that always answers with `body` and `status`. */
function mockFetch(body: unknown, status = 200) {
  const fetchMock = vi.fn(async () => Response.json(body, { status }))
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('GroupsPage', () => {
  it('lists the groups from the backend', async () => {
    mockFetch([
      { id: 1, name: 'Trip to Berlin' },
      { id: 2, name: 'Flat share' },
    ])

    render(<GroupsPage />)

    // findBy... waits until the element shows up (the fetch is asynchronous).
    expect(await screen.findByText('Trip to Berlin')).toBeInTheDocument()
    expect(screen.getByText('Flat share')).toBeInTheDocument()
    expect(screen.getAllByRole('listitem')).toHaveLength(2)
  })

  it('shows a message when there are no groups', async () => {
    mockFetch([])

    render(<GroupsPage />)

    expect(await screen.findByText('No groups yet.')).toBeInTheDocument()
    // queryBy... returns null instead of failing, so we can check something is NOT there.
    expect(screen.queryByText('Loading groups…')).not.toBeInTheDocument()
    expect(screen.queryAllByRole('listitem')).toHaveLength(0)
  })

  it('shows the backend error when loading fails', async () => {
    // The same kind of body Spring Boot sends for an error (a ProblemDetail).
    mockFetch(
      { title: 'Internal Server Error', status: 500, detail: 'Database is down' },
      500,
    )

    render(<GroupsPage />)

    const alert = await screen.findByRole('alert')
    expect(alert).toHaveTextContent('Could not load groups: Database is down')
    expect(screen.queryByText('No groups yet.')).not.toBeInTheDocument()
  })
})
