import { apiRequest, cleanParams } from '@/shared/api/request'
import type { PageResponse } from '@/shared/api/types'
import type { NotificationTemplateItem, NotificationTemplateQuery } from './model'

// 消息模板接口独立于通知公告接口，避免两个功能模块互相耦合。
export function getNotificationTemplatePage(query: NotificationTemplateQuery) {
  return apiRequest<PageResponse<NotificationTemplateItem>>('/notification/template/list/page', {
    body: cleanParams({ ...query, enabled: query.enabled ?? undefined }),
  })
}

export function createNotificationTemplate(form: Partial<NotificationTemplateItem>) {
  return apiRequest<number>('/notification/template/add', { body: form })
}

export function updateNotificationTemplate(form: Partial<NotificationTemplateItem>) {
  return apiRequest<boolean>('/notification/template/update', { body: form })
}

export function toggleNotificationTemplate(id: number, enabled: boolean) {
  return apiRequest<boolean>(`/notification/template/${enabled ? 'enable' : 'disable'}`, { body: { id } })
}
