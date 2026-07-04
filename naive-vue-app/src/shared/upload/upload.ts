import { ApiError, TOKEN_STORAGE_KEY, buildApiUrl } from '@/shared/api/request'
import type { ApiResponse } from '@/shared/api/types'

export interface UploadProgressEvent {
  percent: number
}

export interface UploadedFileResult {
  url: string
  name: string
  size: number
  type: string
}

export interface UploadFileOptions {
  action?: string
  fieldName?: string
  data?: Record<string, string | number | boolean | undefined>
  onProgress?: (event: UploadProgressEvent) => void
}

function resolveUploadResult(data: unknown, file: File): UploadedFileResult {
  // 后端当前返回字符串 URL；这里兼容未来返回对象，组件层不用跟着后端响应形态变化。
  if (typeof data === 'string') {
    return {
      url: data,
      name: file.name,
      size: file.size,
      type: file.type,
    }
  }

  if (data && typeof data === 'object') {
    const record = data as Record<string, unknown>
    const url = String(record.url ?? record.fileUrl ?? record.path ?? '')
    if (url) {
      return {
        url,
        name: String(record.originalName ?? record.filename ?? record.name ?? file.name),
        size: Number(record.fileSize ?? record.size ?? file.size),
        type: String(record.contentType ?? record.type ?? file.type),
      }
    }
  }

  throw new ApiError('上传接口响应格式不正确')
}

export function uploadFile(file: File, options: UploadFileOptions = {}) {
  return new Promise<UploadedFileResult>((resolve, reject) => {
    const formData = new FormData()
    const fieldName = options.fieldName || 'file'
    const xhr = new XMLHttpRequest()

    formData.append(fieldName, file)
    Object.entries(options.data ?? {}).forEach(([key, value]) => {
      if (value !== undefined) {
        formData.append(key, String(value))
      }
    })

    xhr.open('POST', buildApiUrl(options.action || '/file/upload'))
    xhr.responseType = 'json'
    xhr.setRequestHeader('Accept-Language', 'zh-CN')

    const token = localStorage.getItem(TOKEN_STORAGE_KEY)
    if (token) {
      xhr.setRequestHeader('Authorization', `Bearer ${token}`)
      xhr.setRequestHeader('token', token)
    }

    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) {
        options.onProgress?.({ percent: Math.round((event.loaded / event.total) * 100) })
      }
    }

    xhr.onload = () => {
      const payload = xhr.response as ApiResponse<unknown> | null
      if (xhr.status < 200 || xhr.status >= 300) {
        reject(new ApiError(payload?.message || xhr.statusText || '上传失败', { status: xhr.status }))
        return
      }
      if (!payload || payload.code !== 0) {
        reject(new ApiError(payload?.message || '上传失败', { code: payload?.code }))
        return
      }
      try {
        resolve(resolveUploadResult(payload.data, file))
      }
      catch (error) {
        reject(error)
      }
    }

    xhr.onerror = () => reject(new ApiError('上传请求失败'))
    xhr.onabort = () => reject(new ApiError('上传已取消'))
    xhr.send(formData)
  })
}
