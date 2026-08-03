import type { EntityId } from '@/shared/api/types'

// 后端下发的权限点树节点，前端据此渲染导航、注册路由与按钮权限。
export interface PermissionNode {
  id: EntityId
  parentId: EntityId
  type: 'menu' | 'button' | 'api'
  title: string
  permissionCode?: string
  path?: string
  component?: string
  icon?: string
  sortOrder?: number
  children?: PermissionNode[]
}

// auth 模块只描述登录态接口返回的用户快照。
export interface LoginUser {
  id: EntityId
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  roles?: string[]
  permissions?: string[]
  menuTree?: PermissionNode[]
  token?: string
  createTime?: string
  updateTime?: string
}
