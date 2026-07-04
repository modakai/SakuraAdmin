import type { EntityId, PageQuery } from '@/shared/api/types'

// dicts 模块同时管理字典类型和字典项，两者共享同一个功能边界。
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
