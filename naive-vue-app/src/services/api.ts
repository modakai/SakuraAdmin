import { apiRequest, cleanParams } from './request'
import type {
  AgreementItem,
  AgreementQuery,
  ApiSummary,
  AuditLogItem,
  AuditLogQuery,
  DashboardStatistics,
  DictItemItem,
  DictItemQuery,
  DictTypeItem,
  DictTypeQuery,
  EntityId,
  ErrorTrendBucket,
  LoginUser,
  NotificationItem,
  NotificationQuery,
  NotificationReceiverType,
  NotificationTemplateItem,
  NotificationTemplateQuery,
  ObservabilityEventItem,
  ObservabilityEventQuery,
  OnlineUserItem,
  OnlineUserQuery,
  PageResponse,
  SystemStatus,
  UserForm,
  UserItem,
  UserQuery,
} from './types'

function toRequestParams<T extends object>(query: T) {
  // 统一把强类型查询对象交给 cleanParams 处理，避免各接口散落不安全类型断言。
  return cleanParams(query as unknown as Record<string, unknown>)
}

export function loginByPassword(userAccount: string, userPassword: string) {
  return apiRequest<LoginUser>('/user/login', {
    body: { userAccount, userPassword },
  })
}

export function logoutRequest() {
  return apiRequest<boolean>('/user/logout', { method: 'POST' })
}

export function getLoginUser() {
  return apiRequest<LoginUser>('/user/get/login')
}

export function getDashboardStatistics() {
  return apiRequest<DashboardStatistics>('/dashboard/statistics')
}

export function getUserPage(query: UserQuery) {
  return apiRequest<PageResponse<UserItem>>('/user/list/page', {
    body: cleanParams({
      ...query,
      status: query.status ?? undefined,
      userName: query.userName?.trim(),
      userRole: query.userRole?.trim(),
    }),
  })
}

export function createUser(form: UserForm) {
  return apiRequest<number>('/user/add', { body: form })
}

export function updateUser(form: UserForm) {
  return apiRequest<boolean>('/user/update', { body: form })
}

export function deleteUserById(id: EntityId) {
  return apiRequest<boolean>('/user/delete', { body: { id: String(id) } })
}

export function resetUserPassword(id: EntityId) {
  return apiRequest<boolean>('/user/reset/password', { body: { id: String(id) } })
}

export function updateMyUser(form: Pick<UserItem, 'userName' | 'userAvatar' | 'userProfile'>) {
  return apiRequest<boolean>('/user/update/my', { body: form })
}

export function updateMyPassword(oldPassword: string, newPassword: string, checkPassword: string) {
  return apiRequest<boolean>('/user/password/update', {
    body: { oldPassword, newPassword, checkPassword },
  })
}

export function getDictTypePage(query: DictTypeQuery) {
  return apiRequest<PageResponse<DictTypeItem>>('/dict/type/list/page', {
    body: cleanParams({
      ...query,
      dictCode: query.dictCode?.trim(),
      dictName: query.dictName?.trim(),
      status: query.status ?? undefined,
    }),
  })
}

export function createDictType(form: Partial<DictTypeItem>) {
  return apiRequest<number>('/dict/type/add', { body: form })
}

export function updateDictType(form: Partial<DictTypeItem>) {
  return apiRequest<boolean>('/dict/type/update', { body: form })
}

export function deleteDictTypeById(id: EntityId) {
  return apiRequest<boolean>('/dict/type/delete', { body: { id: String(id) } })
}

export function getDictItemPage(query: DictItemQuery) {
  return apiRequest<PageResponse<DictItemItem>>('/dict/item/list/page', {
    body: cleanParams({
      ...query,
      dictLabel: query.dictLabel?.trim(),
      dictValue: query.dictValue?.trim(),
      status: query.status ?? undefined,
    }),
  })
}

export function createDictItem(form: Partial<DictItemItem>) {
  return apiRequest<number>('/dict/item/add', { body: form })
}

export function updateDictItem(form: Partial<DictItemItem>) {
  return apiRequest<boolean>('/dict/item/update', { body: form })
}

export function deleteDictItemById(id: EntityId) {
  return apiRequest<boolean>('/dict/item/delete', { body: { id: String(id) } })
}

export function getAgreementPage(query: AgreementQuery) {
  return apiRequest<PageResponse<AgreementItem>>('/agreement/list/page', {
    body: cleanParams({ ...query, status: query.status ?? undefined }),
  })
}

export function createAgreement(form: Partial<AgreementItem>) {
  return apiRequest<number>('/agreement/add', { body: form })
}

export function updateAgreement(form: Partial<AgreementItem>) {
  return apiRequest<boolean>('/agreement/update', { body: form })
}

export function deleteAgreementById(id: number) {
  return apiRequest<boolean>('/agreement/delete', { body: { id } })
}

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

export function getOnlineUserPage(query: OnlineUserQuery) {
  return apiRequest<PageResponse<OnlineUserItem>>('/online/user/list/page', {
    body: toRequestParams(query),
  })
}

export function forceLogoutOnlineUser(sessionId: string) {
  return apiRequest<boolean>('/online/user/force-logout', { body: { sessionId } })
}

export function getAuditLogPage(query: AuditLogQuery) {
  return apiRequest<PageResponse<AuditLogItem>>('/audit/log/list/page', {
    body: toRequestParams(query),
  })
}

export function getAuditLogDetail(id: number) {
  return apiRequest<AuditLogItem>('/audit/log/get', { query: { id } })
}

export function exportAuditLogs(query: AuditLogQuery) {
  return apiRequest<Blob>('/audit/log/export', {
    body: cleanParams({ ...query, exportLimit: query.exportLimit ?? 5000 }),
    responseType: 'blob',
  })
}

export function getSystemStatus() {
  return apiRequest<SystemStatus>('/admin/observability/status')
}

export function getApiSummary(query: ObservabilityEventQuery) {
  return apiRequest<ApiSummary>('/admin/observability/api/summary', {
    body: toRequestParams(query),
  })
}

export function getSlowApiPage(query: ObservabilityEventQuery) {
  return apiRequest<PageResponse<ObservabilityEventItem>>('/admin/observability/api/slow/page', {
    body: toRequestParams(query),
  })
}

export function getErrorTrend(query: ObservabilityEventQuery) {
  return apiRequest<ErrorTrendBucket[]>('/admin/observability/api/errors/trend', {
    body: toRequestParams(query),
  })
}

export function getSecurityEventPage(query: ObservabilityEventQuery) {
  return apiRequest<PageResponse<ObservabilityEventItem>>('/admin/observability/security/events/page', {
    body: toRequestParams(query),
  })
}
