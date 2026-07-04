import { apiRequest, cleanParams } from '@/shared/api/request'
import type { EntityId, PageResponse } from '@/shared/api/types'
import type { DictItemItem, DictItemQuery, DictTypeItem, DictTypeQuery } from './model'

// 字典接口集中处理类型和条目，页面不直接拼接后端路径。
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
