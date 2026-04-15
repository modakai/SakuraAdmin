import { describe, expect, it } from 'vitest'

import {
  isDetailQueryEnabled,
  normalizeDictItemQuery,
  normalizeDictTypeQuery,
  normalizeUserQuery,
} from '../admin-query'

describe('admin-query', () => {
  it('should remove empty user filters before request', () => {
    expect(normalizeUserQuery({
      page: 1,
      pageSize: 10,
      userName: '',
      userRole: '',
      status: '',
    })).toEqual({
      page: 1,
      pageSize: 10,
      userName: undefined,
      userRole: undefined,
      status: undefined,
    })
  })

  it('should remove empty dict type filters before request', () => {
    expect(normalizeDictTypeQuery({
      page: 2,
      pageSize: 20,
      dictCode: '',
      dictName: '',
      status: '',
    })).toEqual({
      page: 2,
      pageSize: 20,
      dictCode: undefined,
      dictName: undefined,
      status: undefined,
    })
  })

  it('should keep dict type id and clear empty dict item filters', () => {
    expect(normalizeDictItemQuery({
      page: 1,
      pageSize: 10,
      dictTypeId: 1001,
      dictLabel: '',
      dictValue: '',
      status: '',
    })).toEqual({
      page: 1,
      pageSize: 10,
      dictTypeId: 1001,
      dictLabel: undefined,
      dictValue: undefined,
      status: undefined,
    })
  })

  it('should only enable detail query when both id and dialog state are ready', () => {
    expect(isDetailQueryEnabled('95681430025000134', true)).toBe(true)
    expect(isDetailQueryEnabled('95681430025000134', false)).toBe(false)
    expect(isDetailQueryEnabled(undefined, true)).toBe(false)
  })
})
