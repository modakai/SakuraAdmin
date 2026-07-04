import { apiRequest } from '@/shared/api/request'
import type { ProfileUpdateForm } from './model'

// 个人中心接口只处理当前用户自己的资料和密码。
export function updateMyUser(form: ProfileUpdateForm) {
  return apiRequest<boolean>('/user/update/my', { body: form })
}

export function updateMyPassword(oldPassword: string, newPassword: string, checkPassword: string) {
  return apiRequest<boolean>('/user/password/update', {
    body: { oldPassword, newPassword, checkPassword },
  })
}
