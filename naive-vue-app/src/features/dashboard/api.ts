import { apiRequest } from '@/shared/api/request'
import type { DashboardStatistics } from './model'

// 首页统计接口只服务 dashboard 功能模块。
export function getDashboardStatistics() {
  return apiRequest<DashboardStatistics>('/dashboard/statistics')
}
