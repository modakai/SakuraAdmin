import type { Directive } from 'vue'
import { useSessionStore } from '@/stores/session'

/**
 * 按钮级权限指令：无对应权限码时移除元素。
 *
 * @example
 * <n-button v-permission="'system:user:add'">新增用户</n-button>
 */
export const permissionDirective: Directive<HTMLElement, string> = {
  mounted(el, binding) {
    const code = binding.value
    if (!code) {
      return
    }
    const session = useSessionStore()
    if (!session.hasPermission(code)) {
      el.remove()
    }
  },
}
