import { describe, expect, it } from 'vitest'

import { getAuthEntryConfig } from '../auth-entry-config'

describe('auth-entry-config', () => {
  it('should expose user portal copy and redirect for user entry', () => {
    expect(getAuthEntryConfig('user')).toMatchObject({
      entry: 'user',
      defaultAccount: 'student@example.com',
      redirectPath: '/',
      titleKey: 'pages.authPortal.user.title',
      submitKey: 'pages.authPortal.user.submit',
    })
  })

  it('should expose admin console copy and redirect for admin entry', () => {
    expect(getAuthEntryConfig('admin')).toMatchObject({
      entry: 'admin',
      defaultAccount: 'admin@example.com',
      redirectPath: '/dashboard',
      titleKey: 'pages.authPortal.admin.title',
      submitKey: 'pages.authPortal.admin.submit',
    })
  })
})
