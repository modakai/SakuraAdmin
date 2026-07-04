import type { EntityId, PageQuery } from '@/shared/api/types'

// upload-records 模块描述成功上传审计记录，而不是业务附件绑定关系。
export type UploadRecordType = 'image' | 'file'
export type UploadRecordBiz = 'user_avatar' | 'photo_wall' | 'image' | 'attachment' | 'document' | 'import_file'

export interface UploadRecordItem {
  id: number
  userId?: EntityId
  uploadType: UploadRecordType
  biz?: UploadRecordBiz | string
  originalName?: string
  objectName?: string
  url?: string
  fileSuffix?: string
  contentType?: string
  fileSize?: number
  createTime?: string
}

export interface UploadRecordQuery extends PageQuery {
  userId?: EntityId | ''
  uploadType?: UploadRecordType | ''
  biz?: UploadRecordBiz | ''
  startTime?: string
  endTime?: string
}
