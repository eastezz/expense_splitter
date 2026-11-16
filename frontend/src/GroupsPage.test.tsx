import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
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

  it('creates a group and adds it to the list', async () => {
    // First call (GET /api/groups): no groups yet.
    // Second call (POST /api/groups): the backend returns the saved group.
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(Response.json([]))
      .mockResolvedValueOnce(Response.json({ id: 7, name: 'Trip to Berlin' }, { status: 201 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()

    render(<GroupsPage />)
    await screen.findByText('No groups yet.')

    const input = screen.getByLabelText('New group name')
    await user.type(input, 'Trip to Berlin')
    await user.click(screen.getByRole('button', { name: 'Create group' }))

    expect(await screen.findByText('Trip to Berlin')).toBeInTheDocument()
    expect(input).toHaveValue('')
    // The page really sent a POST with the name we typed.
    expect(fetchMock).toHaveBeenLastCalledWith('/api/groups', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name: 'Trip to Berlin' }),
    })
  })
})
