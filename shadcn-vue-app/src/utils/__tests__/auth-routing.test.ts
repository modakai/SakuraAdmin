import { describe, expect, it } from 'vitest'

import type { AuthSession } from '../auth-routing'

import {
  getDefaultRedirectPath,
  getLoginRoute,
  resolveProtectedRedirect,
} from '../auth-routing'

const adminSession: AuthSession = {
  isLogin: true,
  loginEntry: 'user',
  token: 'admin-token',
  user: {
    id: 1,
    name: '管理员',
    email: 'admin@example.com',
    roles: ['admin', 'user'],
  },
}

const userSession: AuthSession = {
  isLogin: true,
  loginEntry: 'user',
  token: 'user-token',
  user: {
    id: 2,
    name: '普通用户',
    email: 'student@example.com',
    roles: ['user'],
  },
}

describe('auth-routing', () => {
  it('should redirect by actual role instead of login entry', () => {
    expect(getDefaultRedirectPath(adminSession)).toBe('/dashboard')
    expect(getDefaultRedirectPath(userSession)).toBe('/')
  })

  it('should use the unified login route for every protected section', () => {
    expect(getLoginRoute('admin')).toBe('/login')
    expect(getLoginRoute('user')).toBe('/login')
  })

  it('should redirect signed-in guest-only pages by actual role', () => {
    expect(resolveProtectedRedirect({ guestOnly: true }, adminSession)).toBe('/dashboard')
    expect(resolveProtectedRedirect({ guestOnly: true }, userSession)).toBe('/')
  })
})
