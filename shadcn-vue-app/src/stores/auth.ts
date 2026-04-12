import { defineStore } from 'pinia'

import type { AuthSession } from '@/utils/auth-routing'

import {

  canAccessAdmin,
  createGuestSession,
} from '@/utils/auth-routing'

export const useAuthStore = defineStore('user', () => {
  // 使用统一 session 结构承载登录入口、用户信息和角色。
  const session = ref<AuthSession>(createGuestSession())

  const isLogin = computed(() => session.value.isLogin)
  const hasAdminAccess = computed(() => canAccessAdmin(session.value))

  function setSession(value: AuthSession) {
    session.value = value
  }

  function clearSession() {
    session.value = createGuestSession()
  }

  return {
    session,
    isLogin,
    hasAdminAccess,
    setSession,
    clearSession,
  }
}, {
  // 登录态需要在刷新后保留，满足混合模板体验。
  persist: true,
})
