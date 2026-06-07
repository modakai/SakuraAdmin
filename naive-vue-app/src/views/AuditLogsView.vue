<script setup lang="ts">
import { computed, h, reactive, ref } from 'vue'
import { NButton, NTag, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { auditLogs, type AuditLogItem } from '../mock/admin'

const message = useMessage()
const detail = ref<AuditLogItem | null>(null)
const query = reactive({ account: '', result: '' })
const rows = computed(() => auditLogs.filter(row => (!query.account || row.accountIdentifier.includes(query.account)) && (!query.result || row.result === query.result)))

const columns = [
  { title: '类型', key: 'logType', render: (row: AuditLogItem) => row.logType === 'login' ? '登录日志' : '管理员操作' },
  { title: '账号', key: 'accountIdentifier' },
  { title: 'IP', key: 'ipAddress' },
  { title: '请求', key: 'requestPath' },
  { title: '描述', key: 'operationDescription' },
  { title: '结果', key: 'result', render: (row: AuditLogItem) => h(NTag, { type: row.result === 'success' ? 'success' : 'error' }, { default: () => row.result === 'success' ? '成功' : '失败' }) },
  { title: '耗时', key: 'durationMillis', render: (row: AuditLogItem) => `${row.durationMillis} ms` },
  { title: '时间', key: 'auditTime' },
  { title: '操作', key: 'actions', render: (row: AuditLogItem) => h(NButton, { size: 'small', onClick: () => detail.value = row }, { default: () => '详情' }) },
]
</script>

<template>
  <PageShell title="审计日志" description="查询登录行为、管理员操作、异常结果和请求轨迹。">
    <template #actions>
      <n-button @click="message.success('已模拟导出当前筛选结果')">导出</n-button>
      <n-button @click="message.success('审计日志已刷新')">刷新</n-button>
    </template>
    <n-card class="admin-card" title="筛选条件">
      <div class="filter-grid">
        <n-input v-model:value="query.account" placeholder="账号" clearable />
        <n-select v-model:value="query.result" placeholder="执行结果" clearable :options="[{ label: '成功', value: 'success' }, { label: '失败', value: 'failure' }]" />
        <n-input type="datetime-local" />
        <n-input type="datetime-local" />
      </div>
    </n-card>
    <n-card class="admin-card" title="审计记录" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="rows" />
    </n-card>
    <n-drawer :show="!!detail" width="420" @update:show="(value: boolean) => { if (!value) detail = null }">
      <n-drawer-content title="审计详情">
        <n-descriptions v-if="detail" :column="1" bordered>
          <n-descriptions-item label="账号">{{ detail.accountIdentifier }}</n-descriptions-item>
          <n-descriptions-item label="请求">{{ detail.httpMethod }} {{ detail.requestPath }}</n-descriptions-item>
          <n-descriptions-item label="IP">{{ detail.ipAddress }}</n-descriptions-item>
          <n-descriptions-item label="结果">{{ detail.result }}</n-descriptions-item>
          <n-descriptions-item label="描述">{{ detail.operationDescription }}</n-descriptions-item>
        </n-descriptions>
      </n-drawer-content>
    </n-drawer>
  </PageShell>
</template>
