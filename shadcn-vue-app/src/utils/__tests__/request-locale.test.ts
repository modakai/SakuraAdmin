import { describe, expect, it } from 'vitest'

import { buildApiRequestHeaders, resolveRequestLanguage } from '@/utils/request-locale'

describe('request-locale', () => {
  it('能将前端语言值转换为后端请求头语言', () => {
    expect(resolveRequestLanguage('zh')).toBe('zh-CN')
    expect(resolveRequestLanguage('en')).toBe('en-US')
    expect(resolveRequestLanguage('unknown')).toBe('en-US')
  })

  it('会在公共请求头中附加 Accept-Language 和 token', () => {
    const headers = buildApiRequestHeaders('zh', 'token-123')

    expect(headers.get('Accept-Language')).toBe('zh-CN')
    expect(headers.get('Authorization')).toBe('Bearer token-123')
    expect(headers.get('token')).toBe('token-123')
  })
})
