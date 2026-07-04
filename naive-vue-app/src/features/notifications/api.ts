import { apiRequest, toRequestParams } from '@/shared/api/request'
import type { PageResponse } from '@/shared/api/types'
import type { NotificationItem, NotificationQuery, NotificationReceiverType } from './model'

// 通知接口覆盖后台公告管理和当前用户通知中心读取。
export function getNotificationPage(query: NotificationQuery) {
  return apiRequest<PageResponse<NotificationItem>>('/notification/list/page', {
    body: toRequestParams(query),
  })
}

export function createNotification(form: Partial<NotificationItem>) {
  return apiRequest<number>('/notification/add', { body: form })
}

export function updateNotification(form: Partial<NotificationItem>) {
  return apiRequest<boolean>('/notification/update', { body: form })
}

export function runNotificationAction(id: number, action: 'publish' | 'revoke' | 'archive') {
  return apiRequest<boolean>(`/notification/${action}`, { body: { id } })
}

export function getClientNotifications(receiverType: NotificationReceiverType) {
  return apiRequest<NotificationItem[]>('/notification/client/messages', { query: { receiverType } })
}

export function getUnreadCount(receiverType: NotificationReceiverType) {
  return apiRequest<number>('/notification/client/unread/count', { query: { receiverType } })
}

export function markAllNotificationsRead(receiverType: NotificationReceiverType) {
  return apiRequest<boolean>('/notification/client/read/all', {
    method: 'POST',
    query: { receiverType },
  })
}

export function markNotificationRead(id: number, receiverType: NotificationReceiverType) {
  return apiRequest<boolean>('/notification/client/read', {
    query: { receiverType },
    body: { id },
  })
}
