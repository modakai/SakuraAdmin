import { apiRequest } from '@/shared/api/request'
import type { EntityId } from '@/shared/api/types'
import type { PermissionNode } from '@/features/auth/model'
import type { PermissionForm } from './model'

export function getPermissionTree() {
  return apiRequest<PermissionNode[]>('/permission/tree')
}

export function createPermission(form: Partial<PermissionForm>) {
  return apiRequest<number>('/permission/add', { body: form })
}

export function updatePermission(form: Partial<PermissionForm>) {
  return apiRequest<boolean>('/permission/update', { body: form })
}

export function deletePermissionById(id: EntityId) {
  return apiRequest<boolean>('/permission/delete', { body: { id: String(id) } })
}
