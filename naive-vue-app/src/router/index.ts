import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import UsersView from '../views/UsersView.vue'
import OnlineUsersView from '../views/OnlineUsersView.vue'
import DictsView from '../views/DictsView.vue'
import AgreementsView from '../views/AgreementsView.vue'
import ObservabilitySystemView from '../views/ObservabilitySystemView.vue'
import ObservabilityApiView from '../views/ObservabilityApiView.vue'
import ObservabilitySecurityView from '../views/ObservabilitySecurityView.vue'
import NotificationsView from '../views/NotificationsView.vue'
import NotificationTemplatesView from '../views/NotificationTemplatesView.vue'
import AuditLogsView from '../views/AuditLogsView.vue'
import UploadRecordsView from '../views/UploadRecordsView.vue'
import ProfileCenterView from '../views/ProfileCenterView.vue'
import { useSessionStore } from '../stores/session'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView },
    {
      path: '/',
      component: AdminLayout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { title: '工作台' } },
        { path: 'users', name: 'users', component: UsersView, meta: { title: '用户管理' } },
        { path: 'online-users', name: 'online-users', component: OnlineUsersView, meta: { title: '在线用户' } },
        { path: 'dicts', name: 'dicts', component: DictsView, meta: { title: '字典管理' } },
        { path: 'agreements', name: 'agreements', component: AgreementsView, meta: { title: '协议管理' } },
        { path: 'observability/system-status', name: 'system-status', component: ObservabilitySystemView, meta: { title: '系统状态' } },
        { path: 'observability/api-monitor', name: 'api-monitor', component: ObservabilityApiView, meta: { title: '接口监控' } },
        { path: 'observability/security-events', name: 'security-events', component: ObservabilitySecurityView, meta: { title: '安全事件' } },
        { path: 'notifications', name: 'notifications', component: NotificationsView, meta: { title: '通知公告' } },
        { path: 'notification-templates', name: 'notification-templates', component: NotificationTemplatesView, meta: { title: '消息模板' } },
        { path: 'audit-logs', name: 'audit-logs', component: AuditLogsView, meta: { title: '审计日志' } },
        { path: 'upload-records', name: 'upload-records', component: UploadRecordsView, meta: { title: '上传记录' } },
        { path: 'profile', name: 'profile', component: ProfileCenterView, meta: { title: '个人中心' } },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const session = useSessionStore()
  // 新项目暂不接后端，使用本地登录态保护后台页面。
  if (to.path !== '/login' && !session.isAuthenticated) {
    return '/login'
  }
  if (to.path === '/login' && session.isAuthenticated) {
    return '/dashboard'
  }
})

export default router
