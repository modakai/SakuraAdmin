import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import LoginPage from '@/features/auth/pages/LoginPage.vue'
import NotFoundPage from '@/shared/ui/NotFoundPage.vue'
import type { PermissionNode } from '@/features/auth/model'
import { useSessionStore } from '@/stores/session'
import { TOKEN_STORAGE_KEY } from '@/shared/api/request'
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
 * 注册后台动态路由（幂等）。
 *
 * <p>登录成功后、刷新恢复登录态时调用。登录场景下先注册再 push，可避免导航守卫
 * 中"注册 + 重定向"的边界问题（首次解析命中 catch-all 的 name 陷阱）。
 */
export function registerDynamicRoutes(menuTree: PermissionNode[]) {
  if (dynamicRoutesRegistered) {
    return
  }
  addDynamicRoutes(router, menuTree)
  dynamicRoutesRegistered = true
}

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
  // 登录态以 localStorage 为准实时判断，避免 pinia getter 缓存导致登录后仍判定未登录。
  const isAuthenticated = Boolean(localStorage.getItem(TOKEN_STORAGE_KEY))
  // 未登录访问后台 → 登录页。
  if (!isAuthenticated) {
    if (to.path === '/login') {
      return true
    }
    return '/login'
  }
  // 已登录访问登录页 → 回到可访问首页。
  if (to.path === '/login') {
    return homePath(session) ?? '/dashboard'
  }
  // 已登录但动态路由尚未注册（如刷新页面）→ 先注册，再按路径重新导航。
  // 用字符串 fullPath 重定向（与 homePath 分支一致），避免携带 name 命中 catch-all。
  if (!dynamicRoutesRegistered) {
    registerDynamicRoutes(session.menuTree)
    return to.fullPath
  }
  // 权限拦截：目标不在菜单树中则回可访问首页；菜单树为空时放行到 404 页（可返回登录），避免死循环。
  const allowed = collectMenuPaths(session.menuTree)
  if (allowed.length > 0 && !allowed.includes(to.path)) {
    return homePath(session) ?? '/dashboard'
  }
  return true
})

export default router
