import { describe, expect, it } from 'vitest'

import { buildApiRequestHeaders, resolveRequestLanguage } from '@/utils/request-locale'

describe('request-locale', () => {
  it('能将前端语言值转换为后端请求头语言', () => {
    expect(resolveRequestLanguage('zh')).toBe('zh-CN')
    expect(resolveRequestLanguage('en')).toBe('en-US')
    expect(resolveRequestLanguage('unknown')).toBe('en-US')
  })

  it('会在公共请求头中附加可配置的 Accept-Language 和 token', () => {
    const headers = buildApiRequestHeaders('zh', 'token-123', {
      tokenHeaderName: 'X-Access-Token',
      tokenHeaderPrefix: 'Token ',
      compatibilityTokenHeaderName: 'token',
      compatibilityTokenHeaderEnabled: true,
    })

    expect(headers.get('Accept-Language')).toBe('zh-CN')
    expect(headers.get('X-Access-Token')).toBe('Token token-123')
    expect(headers.get('token')).toBe('token-123')
  })

  it('关闭兼容请求头后只发送主请求头', () => {
    const headers = buildApiRequestHeaders('en', 'token-456', {
      tokenHeaderName: 'Authorization',
      tokenHeaderPrefix: 'Bearer ',
      compatibilityTokenHeaderName: 'token',
      compatibilityTokenHeaderEnabled: false,
    })

    expect(headers.get('Accept-Language')).toBe('en-US')
    expect(headers.get('Authorization')).toBe('Bearer token-456')
    expect(headers.has('token')).toBe(false)
  })
})
