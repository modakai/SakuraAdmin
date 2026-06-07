export type StatusNumber = 0 | 1
export type RecordId = number

export interface UserItem {
  id: RecordId
  userAccount: string
  userName: string
  userRole: 'admin' | 'user'
  status: StatusNumber
  lastLoginIp: string
  createTime: string
}

export interface OnlineUserItem {
  id: RecordId
  userAccount: string
  userName: string
  userRole: string
  loginIp: string
  loginLocation: string
  loginTime: string
}

export interface DictTypeItem {
  id: RecordId
  dictCode: string
  dictName: string
  status: StatusNumber
  remark: string
}

export interface DictItemItem {
  id: RecordId
  dictTypeId: RecordId
  dictLabel: string
  dictValue: string
  status: StatusNumber
}

export interface AgreementItem {
  id: RecordId
  agreementType: string
  title: string
  status: StatusNumber
  sortOrder: number
  updateTime: string
}

export interface NotificationItem {
  id: RecordId
  type: 'message' | 'announcement'
  title: string
  receiverType: 'all' | 'admin' | 'app'
  level: 'info' | 'warning' | 'error'
  status: 'draft' | 'published' | 'archived'
  pinned: StatusNumber
  updateTime: string
}

export interface TemplateItem {
  id: RecordId
  templateCode: string
  eventType: string
  receiverType: 'all' | 'admin' | 'app'
  titleTemplate: string
  enabled: StatusNumber
}

export interface AuditLogItem {
  id: RecordId
  logType: 'login' | 'admin_operation'
  accountIdentifier: string
  ipAddress: string
  httpMethod: string
  requestPath: string
  operationDescription: string
  result: 'success' | 'failure'
  durationMillis: number
  auditTime: string
}

export interface ObservabilityEventItem {
  id: RecordId
  requestPath: string
  httpMethod: string
  statusCode: number
  durationMillis: number
  ipAddress: string
  accountIdentifier: string
  eventTime: string
}

export interface SecurityEventItem {
  id: RecordId
  eventType: string
  severity: 'low' | 'medium' | 'high'
  accountIdentifier: string
  ipAddress: string
  description: string
  eventTime: string
}

export const users: UserItem[] = [
  { id: 1, userAccount: 'sakura', userName: '系统管理员', userRole: 'admin', status: 1, lastLoginIp: '127.0.0.1', createTime: '2026-06-01 09:10:00' },
  { id: 2, userAccount: 'operator', userName: '运营同学', userRole: 'admin', status: 1, lastLoginIp: '10.0.0.18', createTime: '2026-06-02 11:30:00' },
  { id: 3, userAccount: 'viewer', userName: '只读用户', userRole: 'user', status: 0, lastLoginIp: '10.0.0.31', createTime: '2026-06-03 14:05:00' },
]

export const onlineUsers: OnlineUserItem[] = [
  { id: 1, userAccount: 'sakura', userName: '系统管理员', userRole: 'admin', loginIp: '127.0.0.1', loginLocation: '本机', loginTime: '2026-06-07 19:40:12' },
  { id: 2, userAccount: 'operator', userName: '运营同学', userRole: 'admin', loginIp: '10.0.0.18', loginLocation: '内网', loginTime: '2026-06-07 19:22:08' },
]

export const dictTypes: DictTypeItem[] = [
  { id: 1, dictCode: 'user_status', dictName: '用户状态', status: 1, remark: '启用和停用' },
  { id: 2, dictCode: 'agreement', dictName: '协议分类', status: 1, remark: '用户协议、隐私政策' },
  { id: 3, dictCode: 'notify_level', dictName: '通知级别', status: 1, remark: '消息强度' },
]

export const dictItems: DictItemItem[] = [
  { id: 1, dictTypeId: 1, dictLabel: '启用', dictValue: '1', status: 1 },
  { id: 2, dictTypeId: 1, dictLabel: '停用', dictValue: '0', status: 1 },
  { id: 3, dictTypeId: 2, dictLabel: '用户协议', dictValue: 'user_agreement', status: 1 },
  { id: 4, dictTypeId: 2, dictLabel: '隐私政策', dictValue: 'privacy_policy', status: 1 },
]

export const agreements: AgreementItem[] = [
  { id: 1, agreementType: 'user_agreement', title: '用户服务协议', status: 1, sortOrder: 10, updateTime: '2026-06-03 10:00:00' },
  { id: 2, agreementType: 'privacy_policy', title: '隐私政策', status: 1, sortOrder: 20, updateTime: '2026-06-04 16:12:00' },
]

export const notifications: NotificationItem[] = [
  { id: 1, type: 'announcement', title: '系统维护通知', receiverType: 'all', level: 'warning', status: 'published', pinned: 1, updateTime: '2026-06-07 10:00:00' },
  { id: 2, type: 'message', title: '账号安全提醒', receiverType: 'admin', level: 'info', status: 'draft', pinned: 0, updateTime: '2026-06-06 18:20:00' },
  { id: 3, type: 'message', title: '异常登录已归档', receiverType: 'app', level: 'error', status: 'archived', pinned: 0, updateTime: '2026-06-05 09:30:00' },
]

export const templates: TemplateItem[] = [
  { id: 1, templateCode: 'user_disabled', eventType: 'user_disabled', receiverType: 'app', titleTemplate: '账户封禁通知', enabled: 1 },
  { id: 2, templateCode: 'admin_login_warning', eventType: 'admin_login', receiverType: 'admin', titleTemplate: '后台登录提醒', enabled: 1 },
]

export const auditLogs: AuditLogItem[] = [
  { id: 1, logType: 'login', accountIdentifier: 'sakura', ipAddress: '127.0.0.1', httpMethod: 'POST', requestPath: '/api/auth/login', operationDescription: '后台登录', result: 'success', durationMillis: 42, auditTime: '2026-06-07 19:40:12' },
  { id: 2, logType: 'admin_operation', accountIdentifier: 'operator', ipAddress: '10.0.0.18', httpMethod: 'PUT', requestPath: '/api/user/update', operationDescription: '更新用户状态', result: 'success', durationMillis: 88, auditTime: '2026-06-07 18:12:06' },
  { id: 3, logType: 'admin_operation', accountIdentifier: 'unknown', ipAddress: '10.0.0.44', httpMethod: 'DELETE', requestPath: '/api/dict/type/delete', operationDescription: '删除字典失败', result: 'failure', durationMillis: 122, auditTime: '2026-06-07 17:05:20' },
]

export const slowApis: ObservabilityEventItem[] = [
  { id: 1, requestPath: '/api/user/list/page', httpMethod: 'GET', statusCode: 200, durationMillis: 830, ipAddress: '10.0.0.18', accountIdentifier: 'operator', eventTime: '2026-06-07 18:22:00' },
  { id: 2, requestPath: '/api/audit/log/export', httpMethod: 'POST', statusCode: 200, durationMillis: 1280, ipAddress: '10.0.0.21', accountIdentifier: 'sakura', eventTime: '2026-06-07 18:40:00' },
]

export const securityEvents: SecurityEventItem[] = [
  { id: 1, eventType: 'login_failure', severity: 'medium', accountIdentifier: 'unknown', ipAddress: '203.0.113.8', description: '同一 IP 多次登录失败', eventTime: '2026-06-07 18:03:00' },
  { id: 2, eventType: 'permission_denied', severity: 'high', accountIdentifier: 'viewer', ipAddress: '10.0.0.31', description: '尝试访问管理员接口', eventTime: '2026-06-07 16:21:00' },
]

export const errorTrend = [
  { bucket: '10:00', clientErrorCount: 3, serverErrorCount: 1, exceptionCount: 0 },
  { bucket: '12:00', clientErrorCount: 5, serverErrorCount: 2, exceptionCount: 1 },
  { bucket: '14:00', clientErrorCount: 2, serverErrorCount: 1, exceptionCount: 0 },
  { bucket: '16:00', clientErrorCount: 7, serverErrorCount: 3, exceptionCount: 2 },
  { bucket: '18:00', clientErrorCount: 4, serverErrorCount: 2, exceptionCount: 1 },
]

export function nextId<T extends { id: number }>(rows: T[]) {
  // 新增 mock 记录时避免重复 ID。
  return Math.max(0, ...rows.map(row => row.id)) + 1
}
