import { apiRequest, toRequestParams } from '@/shared/api/request'
import type { PageResponse } from '@/shared/api/types'
import type {
  ApiSummary,
  ErrorTrendBucket,
  ObservabilityEventItem,
  ObservabilityEventQuery,
  SystemStatus,
} from './model'

// 可观测性接口集中服务运维监控分组下的多个页面。
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
