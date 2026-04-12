import { storeToRefs } from 'pinia'

import type { LoginPayload } from '@/constants/mock-auth'

import { demoAccounts } from '@/constants/mock-auth'
import { useAuthStore } from '@/stores/auth'
import {
  canAccessAdmin,
  getDefaultRedirectPath,
} from '@/utils/auth-routing'

export function useAuth() {
  const router = useRouter()

  const authStore = useAuthStore()
  const { hasAdminAccess, isLogin, session } = storeToRefs(authStore)
  const loading = ref(false)

  function logout() {
    authStore.clearSession()
    router.push({ path: '/login' })
  }

  async function login(payload: LoginPayload) {
    loading.value = true
    await new Promise(resolve => setTimeout(resolve, 50))

    const matchedAccount = demoAccounts[payload.email.trim().toLowerCase()]
    if (!matchedAccount || matchedAccount.password !== payload.password) {
      loading.value = false
      throw new Error('账号或密码错误，请使用模板预置账号登录。')
    }

    // 登录入口只影响默认回跳，不替代角色判断。
    const nextSession = {
      ...matchedAccount.session,
      loginEntry: payload.entry,
    }

    authStore.setSession(nextSession)
    loading.value = false

    const redirect = router.currentRoute.value.query.redirect as string | undefined
    if (redirect && !redirect.startsWith('//')) {
      router.push(redirect)
      return
    }

    router.push(getDefaultRedirectPath(payload.entry, canAccessAdmin(nextSession)))
  }

  return {
    loading,
    logout,
    login,
    isLogin,
    hasAdminAccess,
    session,
  }
}
