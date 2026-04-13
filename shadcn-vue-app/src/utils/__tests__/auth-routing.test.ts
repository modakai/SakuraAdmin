import { describe, expect, it } from 'vitest'

import type { AuthEntry, AuthSession } from '@/utils/auth-routing'

import {
  canAccessAdmin,
  createGuestSession,
  getDefaultRedirectPath,
  getLoginRoute,
  resolveProtectedRedirect,
} from '@/utils/auth-routing'

describe('auth-routing', () => {
  it('用户端登录后默认跳到首页', () => {
    expect(getDefaultRedirectPath('user', true)).toBe('/')
    expect(getDefaultRedirectPath('user', false)).toBe('/')
  })

  it('后台入口登录后仅管理员进入后台', () => {
    expect(getDefaultRedirectPath('admin', true)).toBe('/dashboard')
    expect(getDefaultRedirectPath('admin', false)).toBe('/')
  })

  it('只有 admin 角色可以访问后台', () => {
    const adminSession: AuthSession = {
      isLogin: true,
      loginEntry: 'admin',
      token: 'token-admin',
      user: {
        id: 1,
        name: '系统管理员',
        email: 'admin@example.com',
        roles: ['admin', 'user'],
      },
    }

    expect(canAccessAdmin(adminSession)).toBe(true)
    expect(canAccessAdmin(createGuestSession())).toBe(false)
  })

  it('未登录访问用户端受保护页时跳到 /login', () => {
    expect(resolveProtectedRedirect({ auth: true, section: 'user' }, createGuestSession())).toBe('/login')
  })

  it('未登录访问后台受保护页时跳到 /auth/sign-in', () => {
    expect(resolveProtectedRedirect({ auth: true, section: 'admin' }, createGuestSession())).toBe('/auth/sign-in')
  })

  it('已登录但无后台权限访问后台页时回到首页', () => {
    const session: AuthSession = {
      isLogin: true,
      loginEntry: 'user',
      token: 'token-user',
      user: {
        id: 2,
        name: '普通用户',
        email: 'user@example.com',
        roles: ['user'],
      },
    }

    expect(resolveProtectedRedirect({ auth: true, section: 'admin', requiresAdmin: true }, session)).toBe('/')
  })

  it('已登录访问登录页时按入口回跳', () => {
    const loginEntry: AuthEntry = 'admin'
    const session: AuthSession = {
      isLogin: true,
      loginEntry,
      token: 'token-admin',
      user: {
        id: 3,
        name: '系统管理员',
        email: 'admin@example.com',
        roles: ['admin'],
      },
    }

    expect(resolveProtectedRedirect({ guestOnly: true, authEntry: loginEntry }, session)).toBe('/dashboard')
  })

  it('可以根据分区返回对应登录页', () => {
    expect(getLoginRoute('user')).toBe('/login')
    expect(getLoginRoute('admin')).toBe('/auth/sign-in')
  })
})
