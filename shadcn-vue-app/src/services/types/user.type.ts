import type { IPaginationRequestQuery } from '@/services/types/response.type'

/**
 * 后台用户列表项。
 */
export interface UserItem {
  id: number
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  status?: number
  createTime?: string
  updateTime?: string
}

/**
 * 用户分页查询参数。
 */
export interface UserQuery extends IPaginationRequestQuery {
  id?: number
  unionId?: string
  mpOpenId?: string
  userName?: string
  userProfile?: string
  userRole?: string
  status?: number | ''
}

/**
 * 新增用户请求。
 */
export interface UserAddForm {
  userAccount: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  status: number
}

/**
 * 更新用户请求。
 */
export interface UserUpdateForm {
  id: number
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  status: number
}

/**
 * 更新个人信息请求。
 */
export interface UserUpdateMyForm {
  userName?: string
  userAvatar?: string
  userProfile?: string
}

/**
 * 用户状态选项。
 */
export interface UserStatusOption {
  label: string
  value: number
  variant: 'default' | 'secondary' | 'destructive' | 'outline'
}
