import { RouterPath } from '@/constants/route-path'

/**
 * 需要前端强制回登录页的业务码。
 */
const AUTH_EXPIRED_CODES = new Set([40100, 40101])

/**
 * 最小响应约束，只要求具备业务码和消息。
 */
interface ApiResponseLike {
  code: number
  message?: string
  data?: unknown
}

/**
 * 前端统一业务异常，保留后端业务码，便于页面层判断。
 */
export class ApiBusinessError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiBusinessError'
    this.code = code
  }
}

/**
 * 判断响应是否为业务成功。
 */
export function isApiSuccess(response: ApiResponseLike): boolean {
  return response.code === 0
}

/**
 * 判断业务码是否表示登录失效或权限不足，需要重新登录。
 */
export function isAuthExpiredCode(code: number): boolean {
  return AUTH_EXPIRED_CODES.has(code)
}

/**
 * 根据当前路径推断应该跳转到哪个登录页。
 */
export function resolveAuthRedirectPath(currentPath: string | undefined): string {
  if (currentPath?.startsWith('/dashboard')) {
    return String(RouterPath.ADMIN_LOGIN)
  }

  return String(RouterPath.USER_LOGIN)
}

/**
 * 将后端业务失败包装成统一异常对象。
 */
export function createApiBusinessError(response: ApiResponseLike): ApiBusinessError {
  return new ApiBusinessError(response.code, response.message || '请求失败')
}
