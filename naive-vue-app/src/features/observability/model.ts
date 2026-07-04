import type { PageQuery } from '@/shared/api/types'

// observability 模块描述系统状态、接口监控和安全事件的监控数据。
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

export interface DependencyStatus {
  name: string
  status: ObservabilityStatus
  message?: string
  latencyMillis?: number
  activeConnections?: number
  idleConnections?: number
  totalConnections?: number
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
