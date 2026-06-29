import { defineStore } from 'pinia'
import { getLoginUser, loginByPassword, logoutRequest } from '../services/api'
import { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from '../services/request'
import type { LoginUser } from '../services/types'

function readStoredUser(): LoginUser | null {
  const raw = localStorage.getItem(USER_STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as LoginUser
  }
  catch {
    localStorage.removeItem(USER_STORAGE_KEY)
    return null
  }
}

function toDisplayUser(user: LoginUser | null) {
  return {
    id: user?.id ?? '',
    account: user?.userAccount ?? '',
    name: user?.userName || user?.userAccount || 'Sakura Admin',
    email: user?.userAccount ?? '',
    role: user?.userRole === 'admin' ? '超级管理员' : '普通用户',
    rawRole: user?.userRole ?? '',
    profile: user?.userProfile ?? '',
    avatar: user?.userAvatar ?? '',
  }
}

export const useSessionStore = defineStore('session', {
  state: () => ({
    token: localStorage.getItem(TOKEN_STORAGE_KEY) || '',
    isAuthenticated: Boolean(localStorage.getItem(TOKEN_STORAGE_KEY)),
    user: toDisplayUser(readStoredUser()),
  }),
  actions: {
    setSession(user: LoginUser, token = user.token) {
      // 后端登录响应携带 token；刷新登录态接口不一定重新返回 token，所以保留现有 token。
      const nextToken = token || this.token
      this.token = nextToken
      this.isAuthenticated = true
      this.user = toDisplayUser(user)
      if (nextToken) {
        localStorage.setItem(TOKEN_STORAGE_KEY, nextToken)
      }
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user))
    },
    async login(userAccount: string, userPassword: string) {
      const user = await loginByPassword(userAccount, userPassword)
      this.setSession(user, user.token)
      return user
    },
    async refreshCurrentUser() {
      const user = await getLoginUser()
      this.setSession(user)
      return user
    },
    clearSession() {
      this.token = ''
      this.isAuthenticated = false
      this.user = toDisplayUser(null)
      localStorage.removeItem(TOKEN_STORAGE_KEY)
      localStorage.removeItem(USER_STORAGE_KEY)
    },
    async logout() {
      // 注销接口失败时仍清理前端状态，避免本地继续误判为已登录。
      try {
        await logoutRequest()
      }
      finally {
        this.clearSession()
      }
    },
  },
})
