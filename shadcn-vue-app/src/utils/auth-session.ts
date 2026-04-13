import type { LoginUser } from '@/services/types/auth.type'
import type { AuthEntry, AuthSession, UserRole } from '@/utils/auth-routing'

/**
 * 将后端单角色字段转换为前端角色数组，兼容用户端与后台共用导航。
 */
function resolveRoles(userRole: string | undefined): UserRole[] {
  if (userRole === 'admin') {
    return ['admin', 'user']
  }

  return ['user']
}

/**
 * 将后端登录返回值映射为前端统一 session。
 */
export function buildAuthSessionFromLoginUser(loginUser: LoginUser, entry: AuthEntry): AuthSession {
  return {
    isLogin: true,
    loginEntry: entry,
    token: loginUser.token ?? null,
    user: {
      id: loginUser.id,
      name: loginUser.userName || loginUser.userAccount || '未命名用户',
      email: loginUser.userAccount || '',
      avatar: loginUser.userAvatar,
      profile: loginUser.userProfile,
      roles: resolveRoles(loginUser.userRole),
    },
  }
}
