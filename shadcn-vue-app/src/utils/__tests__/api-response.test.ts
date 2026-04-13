import { describe, expect, it } from 'vitest'

import {
  createApiBusinessError,
  isApiSuccess,
  isAuthExpiredCode,
  resolveAuthRedirectPath,
} from '@/utils/api-response'

describe('api-response', () => {
  it('仅根据业务码判断响应是否成功', () => {
    expect(isApiSuccess({ code: 0, message: 'ok', data: { id: 1 } })).toBe(true)
    expect(isApiSuccess({ code: 50000, message: 'error', data: null })).toBe(false)
  })

  it('40100 和 40101 都视为登录失效或无权限', () => {
    expect(isAuthExpiredCode(40100)).toBe(true)
    expect(isAuthExpiredCode(40101)).toBe(true)
    expect(isAuthExpiredCode(40300)).toBe(false)
  })

  it('能根据当前访问路径推断对应登录页', () => {
    expect(resolveAuthRedirectPath('/dashboard/user-management')).toBe('/auth/sign-in')
    expect(resolveAuthRedirectPath('/login')).toBe('/login')
    expect(resolveAuthRedirectPath('/profile')).toBe('/login')
  })

  it('业务异常对象会保留后端 code 和 message', () => {
    const error = createApiBusinessError({
      code: 40100,
      message: '未登录',
      data: null,
    })

    expect(error.name).toBe('ApiBusinessError')
    expect(error.code).toBe(40100)
    expect(error.message).toBe('未登录')
  })
})
