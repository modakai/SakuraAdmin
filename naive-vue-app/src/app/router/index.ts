import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginPage from '@/features/auth/pages/LoginPage.vue'
import { useSessionStore } from '@/stores/session'
import { createAdminChildRoutes } from './navigation'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginPage },
    {
      path: '/',
      component: AdminLayout,
      redirect: '/dashboard',
      children: createAdminChildRoutes(),
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
