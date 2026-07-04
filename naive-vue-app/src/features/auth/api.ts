import { apiRequest } from '@/shared/api/request'
import type { LoginUser } from './model'

// 认证接口集中在 auth 模块，避免登录态逻辑散落到通用 request 层。
export function loginByPassword(userAccount: string, userPassword: string) {
  return apiRequest<LoginUser>('/user/login', {
    body: { userAccount, userPassword },
  })
}

export function logoutRequest() {
  return apiRequest<boolean>('/user/logout', { method: 'POST' })
}

export function getLoginUser() {
  return apiRequest<LoginUser>('/user/get/login')
}
