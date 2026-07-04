<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, useDialog, useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import { forceLogoutOnlineUser, getOnlineUserPage } from '../api'
import type { OnlineUserItem } from '../model'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const rows = ref<OnlineUserItem[]>([])
const total = ref(0)
const query = reactive({ userAccount: '', userName: '', userRole: '', loginIp: '' })
const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onUpdatePage: (page: number) => {
    pagination.page = page
    loadRows()
  },
})

async function loadRows() {
  loading.value = true
  try {
    // 在线用户来自 Redis 会话仓库，页面不再使用静态 mock 会话。
    const page = await getOnlineUserPage({ page: pagination.page, pageSize: pagination.pageSize, ...query })
    rows.value = page.records
    total.value = page.totalRow
    pagination.itemCount = page.totalRow
  }
  catch (error: any) {
    rows.value = []
    total.value = 0
    pagination.itemCount = 0
    message.error(error?.message || '加载在线用户失败')
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadRows()
}

function forceLogout(row: OnlineUserItem) {
  dialog.warning({
    title: '强制下线',
    content: `确认强制 ${row.userAccount || row.sessionId} 下线？`,
    positiveText: '强退',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await forceLogoutOnlineUser(row.sessionId)
        message.success('用户已下线')
        await loadRows()
      }
      catch (error: any) {
        message.error(error?.message || '强制下线失败')
      }
    },
  })
}

const columns = [
  { title: '账号', key: 'userAccount' },
  { title: '昵称', key: 'userName' },
  { title: '角色', key: 'userRole' },
  { title: '登录 IP', key: 'loginIp' },
  { title: '客户端', key: 'clientInfo' },
  { title: '登录时间', key: 'loginTime' },
  { title: '最后访问', key: 'lastAccessTime' },
  { title: '操作', key: 'actions', render: (row: OnlineUserItem) => h(NButton, { size: 'small', type: 'warning', ghost: true, onClick: () => forceLogout(row) }, { default: () => '强制下线' }) },
]

onMounted(loadRows)
</script>

<template>
  <PageShell title="在线用户" description="查看当前后台在线会话，并支持强制下线。">
    <template #actions>
      <n-button :loading="loading" @click="loadRows">刷新</n-button>
    </template>
    <n-card class="admin-card" title="筛选条件">
      <div class="filter-grid">
        <n-input v-model:value="query.userAccount" placeholder="账号" clearable @keyup.enter="handleSearch" />
        <n-input v-model:value="query.userName" placeholder="昵称" clearable @keyup.enter="handleSearch" />
        <n-input v-model:value="query.userRole" placeholder="角色" clearable @keyup.enter="handleSearch" />
        <n-input v-model:value="query.loginIp" placeholder="登录 IP" clearable @keyup.enter="handleSearch" />
        <n-button type="primary" @click="handleSearch">查询</n-button>
      </div>
    </n-card>
    <n-card class="admin-card" title="在线会话" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" remote />
    </n-card>
  </PageShell>
</template>
