import type { EntityId, PageQuery } from '@/shared/api/types'

// online-users 模块描述在线会话管理的列表数据。
export interface OnlineUserItem {
  sessionId: string
  userId?: EntityId
  userAccount?: string
  userName?: string
  userRole?: string
  loginIp?: string
  loginLocation?: string
  clientInfo?: string
  loginTime?: string
  lastAccessTime?: string
  expireTime?: string
}

export interface OnlineUserQuery extends PageQuery {
  userId?: EntityId
  userAccount?: string
  userName?: string
  userRole?: string
  loginIp?: string
  loginStartTime?: string
  loginEndTime?: string
}
