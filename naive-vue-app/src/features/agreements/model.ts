import type { PageQuery } from '@/shared/api/types'

// agreements 模块描述协议管理页的协议内容和查询条件。
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
