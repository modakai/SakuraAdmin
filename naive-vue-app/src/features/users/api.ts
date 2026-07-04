import { apiRequest, cleanParams } from '@/shared/api/request'
import type { EntityId, PageResponse } from '@/shared/api/types'
import type { UserForm, UserItem, UserQuery } from './model'

// 用户管理接口集中在 users 模块，避免恢复全局 api.ts 大杂烩。
export function getUserPage(query: UserQuery) {
  return apiRequest<PageResponse<UserItem>>('/user/list/page', {
    body: cleanParams({
      ...query,
      status: query.status ?? undefined,
      userName: query.userName?.trim(),
      userRole: query.userRole?.trim(),
    }),
  })
}

export function createUser(form: UserForm) {
  return apiRequest<number>('/user/add', { body: form })
}

export function updateUser(form: UserForm) {
  return apiRequest<boolean>('/user/update', { body: form })
}

export function deleteUserById(id: EntityId) {
  return apiRequest<boolean>('/user/delete', { body: { id: String(id) } })
}

export function resetUserPassword(id: EntityId) {
  return apiRequest<boolean>('/user/reset/password', { body: { id: String(id) } })
}
