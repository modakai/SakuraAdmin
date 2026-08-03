import { defineStore } from 'pinia'
import { getLoginUser, loginByPassword, logoutRequest } from '@/features/auth/api'
import type { LoginUser, PermissionNode } from '@/features/auth/model'
import { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from '@/shared/api/request'

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
  // RBAC 后以角色集合判断；兼容旧 userRole 字段（如用户管理页直接新建的管理员）。
  const isAdmin = (user?.roles?.includes('admin') ?? false) || user?.userRole === 'admin'
  return {
    id: user?.id ?? '',
    account: user?.userAccount ?? '',
    name: user?.userName || user?.userAccount || 'Sakura Admin',
    email: user?.userAccount ?? '',
    role: isAdmin ? '超级管理员' : '普通用户',
    rawRole: user?.userRole ?? '',
    profile: user?.userProfile ?? '',
    avatar: user?.userAvatar ?? '',
  }
}

export const useSessionStore = defineStore('session', {
  state: () => {
    const stored = readStoredUser()
    return {
      token: localStorage.getItem(TOKEN_STORAGE_KEY) || '',
      user: toDisplayUser(stored),
      roles: (stored?.roles ?? []) as string[],
      permissions: (stored?.permissions ?? []) as string[],
      menuTree: (stored?.menuTree ?? []) as PermissionNode[],
    }
  },
  getters: {
    /**
     * 是否已登录。实时读取 localStorage，token 被清除（如 401 处理）后立即判定未登录。
     */
    isAuthenticated: () => Boolean(localStorage.getItem(TOKEN_STORAGE_KEY)),
    /**
     * 判断当前用户是否具备某权限码。
     */
    hasPermission: (state) => (code?: string) => {
      if (!code) {
        return true
      }
      return state.permissions.includes(code)
    },
  },
  actions: {
    setSession(user: LoginUser, token = user.token) {
      // 后端登录响应携带 token；刷新登录态接口不一定重新返回 token，所以保留现有 token。
      const nextToken = token || this.token
      this.token = nextToken
      this.user = toDisplayUser(user)
      this.roles = user.roles ?? []
      this.permissions = user.permissions ?? []
      this.menuTree = user.menuTree ?? []
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
      this.user = toDisplayUser(null)
      this.roles = []
      this.permissions = []
      this.menuTree = []
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
