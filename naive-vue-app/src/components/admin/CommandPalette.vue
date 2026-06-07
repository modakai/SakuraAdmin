<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { SearchOutline } from '@vicons/ionicons5'

const router = useRouter()
const open = ref(false)
const keyword = ref('')

const commands = [
  { title: '工作台', path: '/dashboard', group: '主导航' },
  { title: '用户管理', path: '/users', group: '系统管理' },
  { title: '在线用户', path: '/online-users', group: '系统管理' },
  { title: '字典管理', path: '/dicts', group: '系统管理' },
  { title: '协议管理', path: '/agreements', group: '系统管理' },
  { title: '系统状态', path: '/observability/system-status', group: '运维监控' },
  { title: '接口监控', path: '/observability/api-monitor', group: '运维监控' },
  { title: '安全事件', path: '/observability/security-events', group: '运维监控' },
  { title: '通知公告', path: '/notifications', group: '系统设置' },
  { title: '消息模板', path: '/notification-templates', group: '系统设置' },
  { title: '审计日志', path: '/audit-logs', group: '系统设置' },
  { title: '个人中心', path: '/profile', group: '系统设置' },
]

const filteredCommands = computed(() => {
  const value = keyword.value.trim()
  if (!value) {
    return commands
  }
  return commands.filter(item => `${item.group}${item.title}${item.path}`.includes(value))
})

function go(path: string) {
  // 命令面板复刻源项目的快速跳转体验。
  open.value = false
  keyword.value = ''
  router.push(path)
}
</script>

<template>
  <n-button quaternary aria-label="打开命令搜索" @click="open = true">
    <template #icon><n-icon><SearchOutline /></n-icon></template>
    命令搜索
  </n-button>
  <n-modal v-model:show="open" preset="card" title="命令搜索" style="width: 620px">
    <n-input v-model:value="keyword" placeholder="输入页面名称、分组或路径" clearable />
    <n-list class="command-list" hoverable clickable>
      <n-list-item v-for="item in filteredCommands" :key="item.path" @click="go(item.path)">
        <n-thing :title="item.title" :description="`${item.group} · ${item.path}`" />
      </n-list-item>
      <n-list-item v-if="filteredCommands.length === 0">
        <n-empty description="没有匹配的页面" />
      </n-list-item>
    </n-list>
  </n-modal>
</template>

<style scoped>
.command-list {
  margin-top: 12px;
  max-height: 420px;
  overflow: auto;
}
</style>
