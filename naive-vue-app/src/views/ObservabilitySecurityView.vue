<script setup lang="ts">
import { h, reactive } from 'vue'
import { NTag } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import MetricCard from '../components/admin/MetricCard.vue'
import { securityEvents, type SecurityEventItem } from '../mock/admin'

const query = reactive({ eventType: '', severity: '' })
const columns = [
  { title: '事件类型', key: 'eventType' },
  { title: '级别', key: 'severity', render: (row: SecurityEventItem) => h(NTag, { type: row.severity === 'high' ? 'error' : row.severity === 'medium' ? 'warning' : 'info' }, { default: () => row.severity }) },
  { title: '账号', key: 'accountIdentifier' },
  { title: 'IP', key: 'ipAddress' },
  { title: '描述', key: 'description' },
  { title: '时间', key: 'eventTime' },
]
</script>

<template>
  <PageShell title="安全事件" description="追踪登录失败、越权访问和敏感操作等安全信号。">
    <template #actions><n-button>刷新</n-button></template>
    <div class="security-metrics">
      <MetricCard title="高危事件" value="1" hint="需要处理" />
      <MetricCard title="中危事件" value="1" />
      <MetricCard title="异常 IP" value="2" />
    </div>
    <n-card class="admin-card" title="筛选条件" style="margin-top: 16px">
      <div class="filter-grid">
        <n-input v-model:value="query.eventType" placeholder="事件类型" />
        <n-select v-model:value="query.severity" clearable placeholder="事件级别" :options="[{ label: '低', value: 'low' }, { label: '中', value: 'medium' }, { label: '高', value: 'high' }]" />
        <n-input type="datetime-local" />
        <n-button type="primary">查询</n-button>
      </div>
    </n-card>
    <n-card class="admin-card" title="事件列表" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="securityEvents" />
    </n-card>
  </PageShell>
</template>

<style scoped>
.security-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 780px) {
  .security-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
