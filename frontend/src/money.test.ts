import { describe, expect, it } from 'vitest'
import { formatCents, parseEuroToCents } from './money'

describe('formatCents', () => {
  it('formats cents as euros the German way', () => {
    // \u00a0 is the non-breaking space Intl puts before the € sign.
    expect(formatCents(1234)).toBe('12,34\u00a0€')
    expect(formatCents(5)).toBe('0,05\u00a0€')
    expect(formatCents(123456)).toBe('1.234,56\u00a0€')
  })
})

describe('parseEuroToCents', () => {
  it('accepts a comma or a dot as the decimal separator', () => {
    expect(parseEuroToCents('12,34')).toBe(1234)
    expect(parseEuroToCents('12.34')).toBe(1234)
  })

  it('accepts whole euros, one decimal and spaces around the number', () => {
    expect(parseEuroToCents('12')).toBe(1200)
    expect(parseEuroToCents('12,5')).toBe(1250)
    expect(parseEuroToCents(' 7 ')).toBe(700)
  })

  it('returns null for invalid input', () => {
    for (const text of ['', 'abc', '-5', '12,345', '12,', '1.234,56']) {
      expect(parseEuroToCents(text)).toBeNull()
    }
  })
})
