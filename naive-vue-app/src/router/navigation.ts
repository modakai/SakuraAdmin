import type { Component } from 'vue'
import type { RouteRecordRaw } from 'vue-router'
import {
  AlertCircleOutline,
  AnalyticsOutline,
  BookOutline,
  CloudUploadOutline,
  DocumentTextOutline,
  HomeOutline,
  NotificationsOutline,
  PeopleOutline,
  PersonCircleOutline,
  PulseOutline,
  ServerOutline,
  SettingsOutline,
} from '@vicons/ionicons5'

type LazyView = () => Promise<{ default: Component }>

export interface NavigationPage {
  kind: 'page'
  title: string
  path: `/${string}`
  name: string
  icon: Component
  component: LazyView
  visibleInMenu?: boolean
}

export interface NavigationGroup {
  kind: 'group'
  title: string
  key: string
  icon: Component
  children: NavigationPage[]
}

export type NavigationNode = NavigationGroup | NavigationPage

// 导航注册表是后台页面与侧边栏菜单的唯一事实来源。
export const navigationRegistry: NavigationNode[] = [
  {
    kind: 'page',
    title: '工作台',
    path: '/dashboard',
    name: 'dashboard',
    icon: HomeOutline,
    component: () => import('@/views/DashboardView.vue'),
  },
  {
    kind: 'group',
    title: '系统管理',
    key: 'system',
    icon: SettingsOutline,
    children: [
      {
        kind: 'page',
        title: '用户管理',
        path: '/users',
        name: 'users',
        icon: PeopleOutline,
        component: () => import('@/views/UsersView.vue'),
      },
      {
        kind: 'page',
        title: '在线用户',
        path: '/online-users',
        name: 'online-users',
        icon: PulseOutline,
        component: () => import('@/views/OnlineUsersView.vue'),
      },
      {
        kind: 'page',
        title: '字典管理',
        path: '/dicts',
        name: 'dicts',
        icon: BookOutline,
        component: () => import('@/views/DictsView.vue'),
      },
      {
        kind: 'page',
        title: '协议管理',
        path: '/agreements',
        name: 'agreements',
        icon: DocumentTextOutline,
        component: () => import('@/views/AgreementsView.vue'),
      },
      {
        kind: 'page',
        title: '上传记录',
        path: '/upload-records',
        name: 'upload-records',
        icon: CloudUploadOutline,
        component: () => import('@/views/UploadRecordsView.vue'),
      },
    ],
  },
  {
    kind: 'group',
    title: '运维监控',
    key: 'observability',
    icon: AnalyticsOutline,
    children: [
      {
        kind: 'page',
        title: '系统状态',
        path: '/observability/system-status',
        name: 'system-status',
        icon: ServerOutline,
        component: () => import('@/views/ObservabilitySystemView.vue'),
      },
      {
        kind: 'page',
        title: '接口监控',
        path: '/observability/api-monitor',
        name: 'api-monitor',
        icon: AnalyticsOutline,
        component: () => import('@/views/ObservabilityApiView.vue'),
      },
      {
        kind: 'page',
        title: '安全事件',
        path: '/observability/security-events',
        name: 'security-events',
        icon: AlertCircleOutline,
        component: () => import('@/views/ObservabilitySecurityView.vue'),
      },
    ],
  },
  {
    kind: 'group',
    title: '系统设置',
    key: 'settings',
    icon: SettingsOutline,
    children: [
      {
        kind: 'page',
        title: '通知公告',
        path: '/notifications',
        name: 'notifications',
        icon: NotificationsOutline,
        component: () => import('@/views/NotificationsView.vue'),
      },
      {
        kind: 'page',
        title: '消息模板',
        path: '/notification-templates',
        name: 'notification-templates',
        icon: DocumentTextOutline,
        component: () => import('@/views/NotificationTemplatesView.vue'),
      },
      {
        kind: 'page',
        title: '审计日志',
        path: '/audit-logs',
        name: 'audit-logs',
        icon: AlertCircleOutline,
        component: () => import('@/views/AuditLogsView.vue'),
      },
    ],
  },
  {
    kind: 'page',
    title: '个人中心',
    path: '/profile',
    name: 'profile',
    icon: PersonCircleOutline,
    component: () => import('@/views/ProfileCenterView.vue'),
    visibleInMenu: false,
  },
]

export function flattenNavigationPages(nodes: NavigationNode[] = navigationRegistry): NavigationPage[] {
  return nodes.flatMap((node) => node.kind === 'group' ? node.children : [node])
}

export function createAdminChildRoutes(): RouteRecordRaw[] {
  return flattenNavigationPages().map((page) => ({
    path: page.path.slice(1),
    name: page.name,
    component: page.component,
    meta: { title: page.title },
  }))
}
