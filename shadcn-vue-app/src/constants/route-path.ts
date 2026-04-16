import type { RouteLocationRaw } from 'vue-router'

export const RouterPath: Record<string, RouteLocationRaw> = {
  HOME: '/',
  USER_LOGIN: '/login',
  ADMIN_HOME: '/dashboard',
  ADMIN_LOGIN: '/login',
} as const
