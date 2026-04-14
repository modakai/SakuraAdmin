/**
 * ofetch: https://github.com/unjs/ofetch
 */
import { ofetch } from 'ofetch'
import { toast } from 'vue-sonner'

import { API_BASE_URL, API_TIMEOUT } from '@/constants/app-config'
import { appLocale } from '@/plugins/i18n'
import pinia from '@/plugins/pinia/setup'
import { useAuthStore } from '@/stores/auth'
import {
  createApiBusinessError,
  isApiSuccess,
  isAuthExpiredCode,
  resolveHttpErrorAction,
} from '@/utils/api-response'
import { buildApiRequestHeaders } from '@/utils/request-locale'

/**
 * 根据异常动作统一执行前端跳转或提示。
 */
function handleHttpError(status: number, message?: string) {
  const currentPath = `${window.location.pathname}${window.location.search}`
  const action = resolveHttpErrorAction(status, message, currentPath)

  if (action.type === 'redirect') {
    const targetUrl = new URL(action.path, window.location.origin)
    if (action.query) {
      Object.entries(action.query).forEach(([key, value]) => {
        targetUrl.searchParams.set(key, value)
      })
    }
    window.location.replace(targetUrl.toString())
    return
  }

  toast.error(action.message)
}

const apiFetch = ofetch.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT ?? false,
  onRequest: ({ options }) => {
    const authStore = useAuthStore(pinia)
    const token = authStore.session.token
    const headers = buildApiRequestHeaders(appLocale.value, token ?? undefined)

    // 保留调用方显式传入的请求头，再补充统一鉴权和语言信息。
    if (options.headers) {
      const customHeaders = new Headers(options.headers)
      customHeaders.forEach((value, key) => headers.set(key, value))
    }

    options.headers = headers
  },
  onRequestError: (_error) => {},
  onResponse: ({ response }) => {
    const body = response._data
    if (!body || typeof body.code !== 'number') {
      return
    }

    if (isApiSuccess(body)) {
      return
    }

    if (isAuthExpiredCode(body.code) && typeof window !== 'undefined') {
      // 登录失效时先清理本地会话，再统一进入 401 错误页。
      const authStore = useAuthStore(pinia)
      authStore.clearSession()
      handleHttpError(401, body.message || '登录已失效，请重新登录。')
      return
    }

    throw createApiBusinessError(body)
  },
  onResponseError: ({ response }) => {
    if (typeof window === 'undefined') {
      return
    }

    if (!response) {
      toast.error('网络异常，请稍后重试。')
      return
    }

    if (response.status === 401) {
      const authStore = useAuthStore(pinia)
      authStore.clearSession()
    }

    handleHttpError(response.status, response._data?.message || response.statusText)
  },
})

export function useApiFetch() {
  return {
    apiFetch,
  }
}
