import { describe, expect, it } from 'vitest'

import { normalizeEntityId } from '../admin-query'

describe('entity-id', () => {
  it('should keep long string ids unchanged for admin mutations', () => {
    const longId = '95681430025000134'

    expect(normalizeEntityId(longId)).toBe(longId)
  })

  it('should convert numeric ids to string payloads', () => {
    expect(normalizeEntityId(123456)).toBe('123456')
  })
})
