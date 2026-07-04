import { apiRequest, cleanParams, toRequestParams } from '@/shared/api/request'
import type { PageResponse } from '@/shared/api/types'
import type { AuditLogItem, AuditLogQuery } from './model'

// 审计日志接口集中处理列表、详情和导出，页面不直接接触通用 request。
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
