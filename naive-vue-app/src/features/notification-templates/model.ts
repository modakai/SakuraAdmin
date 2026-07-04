import type { PageQuery } from '@/shared/api/types'

// notification-templates 模块只描述消息模板管理，不复用通知列表的表单类型。
export type TemplateReceiverType = 'admin' | 'app' | 'all'

export interface NotificationTemplateItem {
  id: number
  templateCode: string
  eventType: string
  titleTemplate: string
  contentTemplate: string
  variableSchema?: string
  receiverType: TemplateReceiverType
  enabled: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface NotificationTemplateQuery extends PageQuery {
  templateCode?: string
  eventType?: string
  enabled?: number | null
}
