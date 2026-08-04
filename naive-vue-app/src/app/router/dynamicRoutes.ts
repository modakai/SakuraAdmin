import type { Router } from 'vue-router'
import type { PermissionNode } from '@/features/auth/model'
import { resolveComponent } from './componentMap'

// 后台布局路由名，动态子路由挂载到它下面。
export const ADMIN_LAYOUT_NAME = 'admin-layout'

// 已注册的动态子路由 name 清单。vue-router 5 的 RouteRecordNormalized 已不含 parent 字段，
// 重建路由时据此移除，避免依赖路由记录的内部结构。
const registeredDynamicRouteNames: (string | symbol)[] = []

/**
 * 动态注册路由：遍历菜单树，为每个 type=menu 且有 path+component 的节点注册子路由。
 * 组件不在映射表中时跳过注册，避免 404 页面。注册过的 name 记入清单，供重建时移除。
 */
export function addDynamicRoutes(router: Router, tree: PermissionNode[] = []): void {
  const walk = (nodes: PermissionNode[]) => {
    nodes.forEach((node) => {
      if (node.type === 'menu' && node.path && node.component) {
        const loader = resolveComponent(node.component)
        if (loader) {
          router.addRoute(ADMIN_LAYOUT_NAME, {
            path: node.path.replace(/^\//, ''),
            name: node.path,
            component: loader,
            meta: { title: node.title, permissionCode: node.permissionCode },
          })
          registeredDynamicRouteNames.push(node.path)
        }
      }
      if (node.children?.length) {
        walk(node.children)
      }
    })
  }
  walk(tree)
}

/**
 * 重建动态路由：先移除已注册的全部动态子路由，再按新菜单树重新注册。
 *
 * <p>权限刷新（ADR-0004）需要路由表随菜单增删即时更新 —— 原有一次幂等注册无法做到：
 * 新增菜单不会注册、删除的菜单路由仍残留。静态路由（/login、404 等）不受影响。
 */
export function rebuildDynamicRoutes(router: Router, tree: PermissionNode[] = []): void {
  while (registeredDynamicRouteNames.length > 0) {
    const name = registeredDynamicRouteNames.pop()
    if (name) {
      router.removeRoute(name)
    }
  }
  addDynamicRoutes(router, tree)
}

/**
 * 收集菜单树中所有可访问路径，供路由守卫做权限拦截。
 */
export function collectMenuPaths(tree: PermissionNode[] = []): string[] {
  const paths: string[] = []
  const walk = (nodes: PermissionNode[]) => {
    nodes.forEach((node) => {
      if (node.type === 'menu' && node.path) {
        paths.push(node.path)
      }
      if (node.children?.length) {
        walk(node.children)
      }
    })
  }
  walk(tree)
  return paths
}
