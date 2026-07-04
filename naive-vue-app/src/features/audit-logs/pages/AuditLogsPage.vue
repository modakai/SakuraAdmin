<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NTag, useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import { exportAuditLogs, getAuditLogDetail, getAuditLogPage } from '../api'
import type { AuditLogItem } from '../model'

const message = useMessage()
const loading = ref(false)
const exporting = ref(false)
const detail = ref<AuditLogItem | null>(null)
const rows = ref<AuditLogItem[]>([])
const query = reactive({ accountIdentifier: '', result: '' as '' | 'success' | 'failure', auditStartTime: '', auditEndTime: '' })
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
    // 审计日志直接走后端分页，导出也复用当前筛选条件。
    const page = await getAuditLogPage({ page: pagination.page, pageSize: pagination.pageSize, ...query })
    rows.value = page.records
    pagination.itemCount = page.totalRow
  }
  catch (error: any) {
    rows.value = []
    pagination.itemCount = 0
    message.error(error?.message || '加载审计日志失败')
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadRows()
}

async function openDetail(row: AuditLogItem) {
  try {
    detail.value = await getAuditLogDetail(row.id)
  }
  catch (error: any) {
    message.error(error?.message || '加载详情失败')
  }
}

async function handleExport() {
  exporting.value = true
  try {
    const blob = await exportAuditLogs({ page: pagination.page, pageSize: pagination.pageSize, ...query, exportLimit: 5000 })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `audit-logs-${Date.now()}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
  }
  catch (error: any) {
    message.error(error?.message || '导出失败')
  }
  finally {
    exporting.value = false
  }
}

const columns = [
  { title: '类型', key: 'logType', render: (row: AuditLogItem) => row.logType === 'login' ? '登录日志' : '管理员操作' },
  { title: '账号', key: 'accountIdentifier' },
  { title: 'IP', key: 'ipAddress' },
  { title: '请求', key: 'requestPath' },
  { title: '描述', key: 'operationDescription' },
  { title: '结果', key: 'result', render: (row: AuditLogItem) => h(NTag, { type: row.result === 'success' ? 'success' : 'error' }, { default: () => row.result === 'success' ? '成功' : '失败' }) },
  { title: '耗时', key: 'costMillis', render: (row: AuditLogItem) => `${row.costMillis ?? row.durationMillis ?? 0} ms` },
  { title: '时间', key: 'auditTime' },
  { title: '操作', key: 'actions', render: (row: AuditLogItem) => h(NButton, { size: 'small', onClick: () => openDetail(row) }, { default: () => '详情' }) },
]

onMounted(loadRows)
</script>

<template>
  <PageShell title="审计日志" description="查询登录行为、管理员操作、异常结果和请求轨迹。">
    <template #actions>
      <n-button :loading="exporting" @click="handleExport">导出</n-button>
      <n-button :loading="loading" @click="loadRows">刷新</n-button>
    </template>
    <n-card class="admin-card" title="筛选条件">
      <div class="filter-grid">
        <n-input v-model:value="query.accountIdentifier" placeholder="账号" clearable />
        <n-select v-model:value="query.result" placeholder="执行结果" clearable :options="[{ label: '成功', value: 'success' }, { label: '失败', value: 'failure' }]" />
        <n-input v-model:value="query.auditStartTime" type="datetime-local" />
        <n-input v-model:value="query.auditEndTime" type="datetime-local" />
        <n-button type="primary" @click="handleSearch">查询</n-button>
      </div>
    </n-card>
    <n-card class="admin-card" title="审计记录" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" remote />
    </n-card>
    <n-drawer :show="!!detail" width="420" @update:show="(value: boolean) => { if (!value) detail = null }">
      <n-drawer-content title="审计详情">
        <n-descriptions v-if="detail" :column="1" bordered>
          <n-descriptions-item label="账号">{{ detail.accountIdentifier }}</n-descriptions-item>
          <n-descriptions-item label="请求">{{ detail.httpMethod }} {{ detail.requestPath }}</n-descriptions-item>
          <n-descriptions-item label="IP">{{ detail.ipAddress }}</n-descriptions-item>
          <n-descriptions-item label="结果">{{ detail.result }}</n-descriptions-item>
          <n-descriptions-item label="描述">{{ detail.operationDescription }}</n-descriptions-item>
          <n-descriptions-item label="异常">{{ detail.exceptionSummary || '-' }}</n-descriptions-item>
        </n-descriptions>
      </n-drawer-content>
    </n-drawer>
  </PageShell>
</template>
