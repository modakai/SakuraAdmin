<script setup lang="ts">
import { h, ref } from 'vue'
import { NButton, useDialog, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { onlineUsers, type OnlineUserItem } from '../mock/admin'

const message = useMessage()
const dialog = useDialog()
const rows = ref<OnlineUserItem[]>([...onlineUsers])

function forceLogout(row: OnlineUserItem) {
  // 在线用户页面只模拟强退动作，不调用真实会话服务。
  dialog.warning({
    title: '强制下线',
    content: `确认强制 ${row.userAccount} 下线？`,
    positiveText: '强退',
    negativeText: '取消',
    onPositiveClick: () => {
      rows.value = rows.value.filter(item => item.id !== row.id)
      message.success('用户已下线')
    },
  })
}

const columns = [
  { title: '账号', key: 'userAccount' },
  { title: '昵称', key: 'userName' },
  { title: '角色', key: 'userRole' },
  { title: '登录 IP', key: 'loginIp' },
  { title: '登录地点', key: 'loginLocation' },
  { title: '登录时间', key: 'loginTime' },
  { title: '操作', key: 'actions', render: (row: OnlineUserItem) => h(NButton, { size: 'small', type: 'warning', ghost: true, onClick: () => forceLogout(row) }, { default: () => '强制下线' }) },
]
</script>

<template>
  <PageShell title="在线用户" description="查看当前后台在线会话，并支持 mock 强制下线。">
    <template #actions>
      <n-button @click="message.success('在线用户已刷新')">刷新</n-button>
    </template>
    <n-card class="admin-card" title="在线会话">
      <n-data-table :columns="columns" :data="rows" :pagination="{ pageSize: 10 }" />
    </n-card>
  </PageShell>
</template>
