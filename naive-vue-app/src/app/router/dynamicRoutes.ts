import type { Router } from 'vue-router'
import type { PermissionNode } from '@/features/auth/model'
import { resolveComponent } from './componentMap'

// 后台布局路由名，动态子路由挂载到它下面。
export const ADMIN_LAYOUT_NAME = 'admin-layout'

/**
 * 动态注册路由：遍历菜单树，为每个 type=menu 且有 path+component 的节点注册子路由。
 * 组件不在映射表中时跳过注册，避免 404 页面。
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
