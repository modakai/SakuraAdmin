import type { ApiResponse } from './types'

export const TOKEN_STORAGE_KEY = 'naive-admin-token'
export const USER_STORAGE_KEY = 'naive-admin-user'

const API_ORIGIN = String(import.meta.env.VITE_SERVER_API_URL || 'http://localhost:8101').replace(/\/$/, '')
const API_PREFIX = String(import.meta.env.VITE_SERVER_API_PREFIX || '/api').replace(/\/$/, '')
const API_TIMEOUT = Number(import.meta.env.VITE_SERVER_API_TIMEOUT || 10000)

export class ApiError extends Error {
  code?: number
  status?: number

  constructor(message: string, options: { code?: number, status?: number } = {}) {
    super(message)
    this.name = 'ApiError'
    this.code = options.code
    this.status = options.status
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST'
  query?: Record<string, unknown>
  body?: unknown
  responseType?: 'json' | 'blob'
}

/**
 * 清理会话缓存，避免 token 失效后继续显示已登录状态。
 */
function clearStoredSession() {
  localStorage.removeItem(TOKEN_STORAGE_KEY)
  localStorage.removeItem(USER_STORAGE_KEY)
}

/**
 * 删除空字符串、null 和 undefined，避免后端按空条件误筛选。
 */
export function cleanParams<T extends Record<string, unknown>>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => {
      if (value === undefined || value === null || value === '') {
        return false
      }
      if (typeof value === 'string' && value.trim() === '') {
        return false
      }
      return true
    }),
  ) as Partial<T>
}

export function buildApiUrl(path: string, query?: Record<string, unknown>) {
  // 对外暴露统一 URL 拼装，上传等非 JSON 请求也必须复用同一后端前缀。
  const url = new URL(`${API_ORIGIN}${API_PREFIX}${path.startsWith('/') ? path : `/${path}`}`)
  Object.entries(cleanParams(query ?? {})).forEach(([key, value]) => {
    url.searchParams.set(key, String(value))
  })
  return url
}

function buildHeaders(hasBody: boolean) {
  const headers = new Headers()
  headers.set('Accept-Language', 'zh-CN')
  if (hasBody) {
    headers.set('Content-Type', 'application/json')
  }

  const token = localStorage.getItem(TOKEN_STORAGE_KEY)
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
    headers.set('token', token)
  }
  return headers
}

/**
 * 项目内统一请求入口，负责 token、业务码和网络异常处理。
 */
export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const controller = new AbortController()
  const timeout = window.setTimeout(() => controller.abort(), API_TIMEOUT)
  const hasBody = options.body !== undefined

  try {
    const response = await fetch(buildApiUrl(path, options.query), {
      method: options.method ?? (hasBody ? 'POST' : 'GET'),
      headers: buildHeaders(hasBody),
      body: hasBody ? JSON.stringify(cleanParams(options.body as Record<string, unknown>)) : undefined,
      signal: controller.signal,
    })

    if (options.responseType === 'blob') {
      if (!response.ok) {
        throw new ApiError(response.statusText || '导出失败', { status: response.status })
      }
      return await response.blob() as T
    }

    const payload = await response.json().catch(() => null) as ApiResponse<T> | null
    if (!response.ok) {
      if (response.status === 401) {
        clearStoredSession()
      }
      throw new ApiError(payload?.message || response.statusText || '请求失败', { status: response.status })
    }

    if (!payload || typeof payload.code !== 'number') {
      throw new ApiError('接口响应格式不正确')
    }
    if (payload.code !== 0) {
      if (payload.code === 40100 || payload.code === 40101) {
        clearStoredSession()
      }
      throw new ApiError(payload.message || '请求失败', { code: payload.code })
    }

    return payload.data
  }
  finally {
    window.clearTimeout(timeout)
  }
}
