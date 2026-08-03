import type { EntityId, PageQuery } from '@/shared/api/types'

export interface RoleItem {
  id: EntityId
  roleCode?: string
  roleName?: string
  isSuperadmin?: number
  status?: number
  sortOrder?: number
  remark?: string
  createTime?: string
  permissionIds?: EntityId[]
}

export interface RoleQuery extends PageQuery {
  roleCode?: string
  roleName?: string
  status?: number
}
