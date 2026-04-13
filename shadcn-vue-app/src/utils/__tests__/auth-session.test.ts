import { describe, expect, it } from 'vitest'

import { buildAuthSessionFromLoginUser } from '@/utils/auth-session'

describe('auth-session', () => {
  it('能把后端登录用户映射为前端 session', () => {
    const session = buildAuthSessionFromLoginUser({
      id: 1,
      userName: '系统管理员',
      userAccount: 'admin@example.com',
      userAvatar: 'https://example.com/avatar.png',
      userProfile: '管理员简介',
      userRole: 'admin',
      token: 'token-123',
    }, 'admin')

    expect(session.isLogin).toBe(true)
    expect(session.loginEntry).toBe('admin')
    expect(session.token).toBe('token-123')
    expect(session.user?.name).toBe('系统管理员')
    expect(session.user?.email).toBe('admin@example.com')
    expect(session.user?.roles).toEqual(['admin', 'user'])
  })

  it('普通用户只保留 user 角色', () => {
    const session = buildAuthSessionFromLoginUser({
      id: 2,
      userName: '普通用户',
      userAccount: 'user@example.com',
      userRole: 'user',
      token: 'token-456',
    }, 'user')

    expect(session.user?.roles).toEqual(['user'])
  })
})
