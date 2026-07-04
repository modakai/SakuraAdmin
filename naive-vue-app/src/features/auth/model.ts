import type { EntityId } from '@/shared/api/types'

// auth 模块只描述登录态接口返回的用户快照。
export interface LoginUser {
  id: EntityId
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  token?: string
  createTime?: string
  updateTime?: string
}
