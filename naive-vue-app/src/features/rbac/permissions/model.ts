import type { EntityId } from '@/shared/api/types'

// 权限点表单，兼容新增与编辑。
export interface PermissionForm {
  id?: EntityId
  parentId: EntityId
  type: 'menu' | 'button' | 'api'
  title: string
  permissionCode?: string
  path?: string
  component?: string
  icon?: string
  sortOrder?: number
  status?: number
  visible?: number
  remark?: string
}
