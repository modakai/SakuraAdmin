// shared/api 只保留跨业务稳定复用的后端协议类型。
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
