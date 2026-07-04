import { apiRequest, cleanParams } from '@/shared/api/request'
import type { PageResponse } from '@/shared/api/types'
import type { AgreementItem, AgreementQuery } from './model'

// 协议管理接口只归属 agreements 模块。
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
