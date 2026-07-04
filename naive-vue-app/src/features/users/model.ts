import type { EntityId, PageQuery } from '@/shared/api/types'

// users 模块描述后台用户管理页使用的用户实体与查询条件。
export interface UserItem {
  id: EntityId
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface UserQuery extends PageQuery {
  userName?: string
  userRole?: string
  status?: number | null
}

export interface UserForm {
  id?: EntityId
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  status: number
}
