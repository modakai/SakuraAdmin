import type { PageQuery } from '@/shared/api/types'

// audit-logs 模块描述后台审计日志查询和导出所需的数据。
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
