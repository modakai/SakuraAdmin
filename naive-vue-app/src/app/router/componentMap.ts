import type { Component } from 'vue'

type LazyView = () => Promise<{ default: Component }>

// 后端 permission.component 标识 → 前端组件加载器。动态菜单的组件来源。
// 新增后台页面时必须同时在此登记，否则动态菜单无法加载对应组件。
export const componentMap: Record<string, LazyView> = {
  'dashboard/DashboardPage': () => import('@/features/dashboard/pages/DashboardPage.vue'),
  'users/UsersPage': () => import('@/features/users/pages/UsersPage.vue'),
  'online-users/OnlineUsersPage': () => import('@/features/online-users/pages/OnlineUsersPage.vue'),
  'dicts/DictsPage': () => import('@/features/dicts/pages/DictsPage.vue'),
  'agreements/AgreementsPage': () => import('@/features/agreements/pages/AgreementsPage.vue'),
  'upload-records/UploadRecordsPage': () => import('@/features/upload-records/pages/UploadRecordsPage.vue'),
  'rbac/roles/RolesPage': () => import('@/features/rbac/roles/pages/RolesPage.vue'),
  'rbac/permissions/PermissionsPage': () => import('@/features/rbac/permissions/pages/PermissionsPage.vue'),
  'observability/pages/ObservabilitySystemPage': () => import('@/features/observability/pages/ObservabilitySystemPage.vue'),
  'observability/pages/ObservabilityApiPage': () => import('@/features/observability/pages/ObservabilityApiPage.vue'),
  'observability/pages/ObservabilitySecurityPage': () => import('@/features/observability/pages/ObservabilitySecurityPage.vue'),
  'notifications/pages/NotificationsPage': () => import('@/features/notifications/pages/NotificationsPage.vue'),
  'notification-templates/pages/NotificationTemplatesPage': () => import('@/features/notification-templates/pages/NotificationTemplatesPage.vue'),
  'audit-logs/pages/AuditLogsPage': () => import('@/features/audit-logs/pages/AuditLogsPage.vue'),
  'profile/pages/ProfilePage': () => import('@/features/profile/pages/ProfilePage.vue'),
}

export function resolveComponent(component?: string): LazyView | undefined {
  return component ? componentMap[component] : undefined
}
