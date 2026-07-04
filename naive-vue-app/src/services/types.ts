export type EntityId = string | number

export interface ApiResponse<T> {
  data: T
  extra?: Record<string, unknown>
  code: number
  message: string
  success: boolean
}

export interface PageResponse<T> {
  records: T[]
  totalRow: number
  pageSize: number
  pageNumber: number
}

export interface PageQuery {
  page: number
  pageSize: number
  sortField?: string
  sortOrder?: string
}

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

export interface DictTypeItem {
  id: EntityId
  dictCode: string
  dictName: string
  status: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface DictItemItem {
  id: EntityId
  dictTypeId: EntityId
  dictLabel: string
  dictValue: string
  sortOrder: number
  status: number
  tagType?: string
  remark?: string
  extJson?: string
  createTime?: string
  updateTime?: string
}

export interface DictTypeQuery extends PageQuery {
  dictCode?: string
  dictName?: string
  status?: number | null
}

export interface DictItemQuery extends PageQuery {
  dictTypeId?: EntityId
  dictLabel?: string
  dictValue?: string
  status?: number | null
}

export interface AgreementItem {
  id: number
  agreementType: string
  title: string
  content: string
  status: number
  sortOrder: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface AgreementQuery extends PageQuery {
  agreementType?: string
  title?: string
  status?: number | null
}

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

export interface NotificationTemplateItem {
  id: number
  templateCode: string
  eventType: string
  titleTemplate: string
  contentTemplate: string
  variableSchema?: string
  receiverType: NotificationReceiverType
  enabled: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface NotificationTemplateQuery extends PageQuery {
  templateCode?: string
  eventType?: string
  enabled?: number | null
}

export type AuditLogType = 'login' | 'admin_operation'
export type AuditLogResult = 'success' | 'failure'

export interface AuditLogItem {
  id: number
  logType: AuditLogType
  userId?: number
  accountIdentifier?: string
  ipAddress?: string
  clientInfo?: string
  requestPath?: string
  httpMethod?: string
  operationDescription?: string
  businessModule?: string
  operationType?: string
  costMillis?: number
  durationMillis?: number
  result: AuditLogResult
  statusCode?: number
  failureReason?: string
  exceptionSummary?: string
  requestSummary?: string
  responseSummary?: string
  traceId?: string
  auditTime?: string
}

export interface AuditLogQuery extends PageQuery {
  logType?: AuditLogType | ''
  accountIdentifier?: string
  ipAddress?: string
  requestPath?: string
  httpMethod?: string
  result?: AuditLogResult | ''
  operationDescription?: string
  businessModule?: string
  operationType?: string
  auditStartTime?: string
  auditEndTime?: string
  exportLimit?: number
}

export type UploadRecordType = 'image' | 'file'
export type UploadRecordBiz = 'user_avatar' | 'photo_wall' | 'image' | 'attachment' | 'document' | 'import_file'

export interface UploadRecordItem {
  id: number
  userId?: EntityId
  uploadType: UploadRecordType
  biz?: UploadRecordBiz | string
  originalName?: string
  objectName?: string
  url?: string
  fileSuffix?: string
  contentType?: string
  fileSize?: number
  createTime?: string
}

export interface UploadRecordQuery extends PageQuery {
  userId?: EntityId | ''
  uploadType?: UploadRecordType | ''
  biz?: UploadRecordBiz | ''
  startTime?: string
  endTime?: string
}

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

export type ObservabilityStatus = 'up' | 'degraded' | 'down' | 'unknown'

export interface MetricSnapshot {
  name: string
  value: number
  unit: string
  used?: number
  total?: number
  usagePercent?: number
  status: ObservabilityStatus
}

export interface SystemStatus {
  sampleTime: string
  overallStatus: ObservabilityStatus
  jvm: {
    heapMemory: MetricSnapshot
    nonHeapMemory: MetricSnapshot
    threadCount: number
    daemonThreadCount: number
    gcCount: number
    gcTimeMillis: number
    status: ObservabilityStatus
  }
  os: {
    systemCpu: MetricSnapshot
    processCpu: MetricSnapshot
    memory: MetricSnapshot
    disk: MetricSnapshot
    status: ObservabilityStatus
  }
  database: DependencyStatus
  redis: DependencyStatus
}

export interface DependencyStatus {
  name: string
  status: ObservabilityStatus
  message?: string
  latencyMillis?: number
  activeConnections?: number
  idleConnections?: number
  totalConnections?: number
}

export type ObservabilityEventType =
  | 'slow_api'
  | 'api_error'
  | 'login_failure'
  | 'abnormal_ip'
  | 'force_logout'
  | 'security_alert'

export interface ObservabilityEventItem {
  id: number
  eventType: ObservabilityEventType
  eventLevel?: string
  title?: string
  subject?: string
  requestPath?: string
  httpMethod?: string
  statusCode?: number
  durationMillis?: number
  userId?: number
  accountIdentifier?: string
  ipAddress?: string
  exceptionSummary?: string
  detail?: string
  auditLogId?: number
  notificationId?: number
  eventTime?: string
}

export interface ObservabilityEventQuery extends PageQuery {
  eventType?: ObservabilityEventType | ''
  eventLevel?: string
  requestPath?: string
  ipAddress?: string
  accountIdentifier?: string
  startTime?: string
  endTime?: string
}

export interface ApiSummary {
  slowApiCount: number
  errorCount: number
  averageSlowDurationMillis: number
}

export interface ErrorTrendBucket {
  bucket: string
  clientErrorCount: number
  serverErrorCount: number
  exceptionCount: number
}

export interface DashboardStatistics {
  summary: {
    userTotalCount: number
    todayNewUserCount: number
    notificationCount: number
    operationLogCount: number
  }
  loginTrend: Array<{
    label: string
    startTime: string
    endTime: string
    loginCount: number
  }>
  recentOperations: Array<{
    id: number
    operator: string
    action: string
    module: string
    operationType: string
    result: string
    ipAddress: string
    operationTime: string
  }>
  sampleTime: string
}
