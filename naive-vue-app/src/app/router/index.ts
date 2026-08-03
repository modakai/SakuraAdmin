import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginPage from '@/features/auth/pages/LoginPage.vue'
import { useSessionStore } from '@/stores/session'
import { ADMIN_LAYOUT_NAME, addDynamicRoutes, collectMenuPaths } from './dynamicRoutes'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginPage },
    {
      path: '/',
      name: ADMIN_LAYOUT_NAME,
      component: AdminLayout,
      redirect: '/dashboard',
      // 后台子路由由登录返回的菜单树动态注册。
      children: [],
    },
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

// 菜单树驱动动态路由，登录态恢复后首次导航时注册一次。
let dynamicRoutesRegistered = false

router.beforeEach((to) => {
  const session = useSessionStore()
  // 未登录访问后台 → 登录页。
  if (to.path !== '/login' && !session.isAuthenticated) {
    return '/login'
  }
  // 已登录访问登录页 → 工作台。
  if (to.path === '/login' && session.isAuthenticated) {
    return '/dashboard'
  }
  // 已登录但动态路由尚未注册（如刷新页面）→ 按菜单树注册后重定向一次。
  if (session.isAuthenticated && !dynamicRoutesRegistered) {
    addDynamicRoutes(router, session.menuTree)
    dynamicRoutesRegistered = true
    return { ...to, replace: true }
  }
  // 权限拦截：目标路径不在用户菜单树中时回到工作台。
  if (session.isAuthenticated && to.path !== '/dashboard' && to.path !== '/profile') {
    const allowed = collectMenuPaths(session.menuTree)
    if (!allowed.includes(to.path)) {
      return '/dashboard'
    }
  }
  return true
})

export default router
