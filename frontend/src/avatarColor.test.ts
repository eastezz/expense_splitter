import { expect, it } from 'vitest'
import { AVATAR_COLOR_COUNT, avatarColorIndex, initialOf } from './avatarColor'

it('gives the same name always the same color', () => {
  expect(avatarColorIndex('Anna')).toBe(avatarColorIndex('Anna'))
  expect(avatarColorIndex('Anna')).toBeGreaterThanOrEqual(0)
  expect(avatarColorIndex('Anna')).toBeLessThan(AVATAR_COLOR_COUNT)
})

it('gives the example members three different colors', () => {
  const colors = ['Anna', 'Ben', 'Cem'].map(avatarColorIndex)
  expect(new Set(colors).size).toBe(3)
})

it('takes the first letter in upper case', () => {
  expect(initialOf('anna')).toBe('A')
  expect(initialOf('  Özge')).toBe('Ö')
  expect(initialOf('')).toBe('?')
})