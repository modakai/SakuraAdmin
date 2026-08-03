import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginPage from '@/features/auth/pages/LoginPage.vue'
import NotFoundPage from '@/shared/ui/NotFoundPage.vue'
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
    // 未匹配路径渲染 404 页而非重定向，避免目标路由未注册时无限循环。
    { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundPage },
  ],
})

// 菜单树驱动动态路由，登录态恢复后首次导航时注册一次。
let dynamicRoutesRegistered = false

/**
 * 计算已登录用户的可访问首页：优先工作台，其次菜单树第一个路径。
 */
function homePath(session: ReturnType<typeof useSessionStore>): string | undefined {
  const paths = collectMenuPaths(session.menuTree)
  if (paths.includes('/dashboard')) {
    return '/dashboard'
  }
  return paths[0]
}

router.beforeEach((to) => {
  const session = useSessionStore()
  // 未登录访问后台 → 登录页。
  if (!session.isAuthenticated) {
    if (to.path === '/login') {
      return true
    }
    return '/login'
  }
  // 已登录访问登录页 → 回到可访问首页。
  if (to.path === '/login') {
    return homePath(session) ?? '/dashboard'
  }
  // 已登录但动态路由尚未注册（如刷新页面）→ 按菜单树注册后重定向一次。
  // 注意：只保留 path/query/hash 重新导航。首次解析时目标路由未注册，to.name 会落到
  // catch-all（not-found）；若带 name 重定向，vue-router 按 name 解析将永远命中 404。
  if (!dynamicRoutesRegistered) {
    addDynamicRoutes(router, session.menuTree)
    dynamicRoutesRegistered = true
    return { path: to.path, query: to.query, hash: to.hash, replace: true }
  }
  // 权限拦截：目标路径不在用户菜单树中时回到可访问首页；
  // 没有任何可访问菜单时放行到 404 页，避免重定向死循环。
  const allowed = collectMenuPaths(session.menuTree)
  if (!allowed.includes(to.path)) {
    return homePath(session)
  }
  return true
})

export default router
