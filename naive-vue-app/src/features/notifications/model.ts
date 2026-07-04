import type { PageQuery } from '@/shared/api/types'

// notifications 模块描述通知公告和顶栏通知中心共用的数据形态。
export type NotificationType = 'message' | 'announcement'
export type NotificationReceiverType = 'admin' | 'app' | 'all'
export type NotificationTargetType = 'all' | 'role' | 'user'
export type NotificationStatus = 'draft' | 'published' | 'revoked' | 'archived'

export interface NotificationItem {
  id: number
  type: NotificationType
  title: string
  summary?: string
  content: string
  level?: string
  status: NotificationStatus
  receiverType: NotificationReceiverType
  targetType: NotificationTargetType
  targetRoles?: string[]
  targetUserIds?: number[]
  pinned?: number
  popup?: number
  read?: boolean
  linkUrl?: string
  effectiveTime?: string
  expireTime?: string
  publishTime?: string
  createTime?: string
  updateTime?: string
}

export interface NotificationQuery extends PageQuery {
  type?: NotificationType | ''
  title?: string
  status?: NotificationStatus | ''
  receiverType?: NotificationReceiverType | ''
  targetType?: NotificationTargetType | ''
}
