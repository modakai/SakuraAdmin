import type { AuthEntry, AuthSession } from '@/utils/auth-routing'

interface DemoAccount {
  password: string
  session: AuthSession
}

// 模板阶段用固定账号模拟统一登录接口返回。
export const demoAccounts: Record<string, DemoAccount> = {
  'student@example.com': {
    password: '123456',
    session: {
      isLogin: true,
      loginEntry: 'user',
      user: {
        name: '林知夏',
        email: 'student@example.com',
        roles: ['user'],
      },
    },
  },
  'admin@example.com': {
    password: '123456',
    session: {
      isLogin: true,
      loginEntry: 'admin',
      user: {
        name: '系统管理员',
        email: 'admin@example.com',
        roles: ['admin', 'user'],
      },
    },
  },
}

export interface LoginPayload {
  email: string
  password: string
  entry: AuthEntry
}
