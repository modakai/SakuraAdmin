import { apiRequest, cleanParams } from '@/shared/api/request'
import type { EntityId, PageResponse } from '@/shared/api/types'
import type { PermissionNode } from '@/features/auth/model'
import type { RoleItem, RoleQuery } from './model'

export function getRolePage(query: RoleQuery) {
  return apiRequest<PageResponse<RoleItem>>('/role/list/page', { body: cleanParams({ ...query }) })
}

export function listAllRoles() {
  return apiRequest<RoleItem[]>('/role/list/all')
}

export function createRole(form: Partial<RoleItem>) {
  return apiRequest<number>('/role/add', { body: form })
}

export function updateRole(form: Partial<RoleItem>) {
  return apiRequest<boolean>('/role/update', { body: form })
}

export function deleteRoleById(id: EntityId) {
  return apiRequest<boolean>('/role/delete', { body: { id: String(id) } })
}

export function getRolePermissionIds(roleId: EntityId) {
  return apiRequest<EntityId[]>(`/role/permissions/${roleId}`)
}

export function assignPermissions(roleId: EntityId, permissionIds: EntityId[]) {
  return apiRequest<boolean>('/role/assign-permission', { body: { roleId, permissionIds } })
}

export function getPermissionTree() {
  return apiRequest<PermissionNode[]>('/permission/tree')
}
