import type { Directive, WatchStopHandle } from 'vue'
import { watch } from 'vue'
import { useSessionStore } from '@/stores/session'

/**
 * 按钮级权限指令：无对应权限码时移除元素。
 *
 * <p>权限刷新（ADR-0004）会整体替换 session.permissions，因此指令在 mounted 后
 * 监听权限码集合变化并重新评估，已渲染的按钮随权限变更自动显示/隐藏。
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
    const evaluate = () => {
      if (!session.hasPermission(code)) {
        el.remove()
      }
    }
    evaluate()
    const stop: WatchStopHandle = watch(() => session.permissions, evaluate)
    ;(el as HTMLElement & { __permissionStop?: WatchStopHandle }).__permissionStop = stop
  },
  unmounted(el) {
    ;(el as HTMLElement & { __permissionStop?: WatchStopHandle }).__permissionStop?.()
  },
}
