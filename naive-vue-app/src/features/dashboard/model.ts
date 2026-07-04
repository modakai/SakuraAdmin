// dashboard 模块聚合后台首页展示所需的统计快照。
export interface DashboardStatistics {
  summary: {
    userTotalCount: number
    todayNewUserCount: number
    notificationCount: number
    operationLogCount: number
  }
  loginTrend: Array<{
    label: string
    startTime: string
    endTime: string
    loginCount: number
  }>
  recentOperations: Array<{
    id: number
    operator: string
    action: string
    module: string
    operationType: string
    result: string
    ipAddress: string
    operationTime: string
  }>
  sampleTime: string
}
