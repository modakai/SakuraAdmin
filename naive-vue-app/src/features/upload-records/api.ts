import { apiRequest, toRequestParams } from '@/shared/api/request'
import type { PageResponse } from '@/shared/api/types'
import type { UploadRecordItem, UploadRecordQuery } from './model'

// 上传记录接口只负责后台分页查询成功上传审计记录。
export function getUploadRecordPage(query: UploadRecordQuery) {
  return apiRequest<PageResponse<UploadRecordItem>>('/admin/upload-record/page', {
    body: toRequestParams(query),
  })
}
