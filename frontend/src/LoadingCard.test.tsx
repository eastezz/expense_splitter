import { render, screen } from '@testing-library/react'
import { expect, it } from 'vitest'
import LoadingCard from './LoadingCard'

it('tells screen readers what is loading', () => {
  render(<LoadingCard label="Loading groups…" />)

  expect(screen.getByRole('status')).toHaveTextContent('Loading groups…')
})