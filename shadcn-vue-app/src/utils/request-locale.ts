import type { Language } from '@/plugins/i18n'

/**
 * 将前端语言值转换为后端统一识别的请求头语言。
 *
 * @param locale 前端语言值
 * @returns 后端请求头语言
 */
export function resolveRequestLanguage(locale?: string): string {
  return locale === 'zh' ? 'zh-CN' : 'en-US'
}

/**
 * 构建统一的接口请求头。
 *
 * @param locale 前端语言值
 * @param token 登录 token
 * @returns 标准请求头对象
 */
export function buildApiRequestHeaders(locale?: Language | string, token?: string): Headers {
  const headers = new Headers()

  headers.set('Accept-Language', resolveRequestLanguage(locale))

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
    headers.set('token', token)
  }

  return headers
}
