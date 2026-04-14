/**
 * ofetch: https://github.com/unjs/ofetch
 */
import { ofetch } from 'ofetch'

import { API_BASE_URL, API_TIMEOUT } from '@/constants/app-config'
import { appLocale } from '@/plugins/i18n'
import pinia from '@/plugins/pinia/setup'
import { useAuthStore } from '@/stores/auth'
import {
  createApiBusinessError,
  isApiSuccess,
  isAuthExpiredCode,
  resolveAuthRedirectPath,
} from '@/utils/api-response'
import { buildApiRequestHeaders } from '@/utils/request-locale'

/**
 * 统一处理登录失效，清空本地登录态后跳回对应登录页。
 */
function redirectToLogin() {
  const authStore = useAuthStore(pinia)
  const currentPath = window.location.pathname
  const redirectPath = resolveAuthRedirectPath(currentPath)
  const loginUrl = new URL(redirectPath, window.location.origin)

  authStore.clearSession()
  if (currentPath !== redirectPath) {
    loginUrl.searchParams.set('redirect', `${currentPath}${window.location.search}`)
  }
  window.location.replace(loginUrl.toString())
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
      redirectToLogin()
    }

    throw createApiBusinessError(body)
  },
  onResponseError: (_error) => {},
})

export function useApiFetch() {
  return {
    apiFetch,
  }
}
