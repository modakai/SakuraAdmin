import { apiRequest, toRequestParams } from '@/shared/api/request'
import type { PageResponse } from '@/shared/api/types'
import type { OnlineUserItem, OnlineUserQuery } from './model'

// 在线用户接口只承载会话列表和强制下线操作。
export function getOnlineUserPage(query: OnlineUserQuery) {
  return apiRequest<PageResponse<OnlineUserItem>>('/online/user/list/page', {
    body: toRequestParams(query),
  })
}

export function forceLogoutOnlineUser(sessionId: string) {
  return apiRequest<boolean>('/online/user/force-logout', { body: { sessionId } })
}
