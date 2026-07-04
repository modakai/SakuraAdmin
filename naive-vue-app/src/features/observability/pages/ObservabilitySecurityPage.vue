<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { NTag, useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import MetricCard from '@/shared/ui/MetricCard.vue'
import { getSecurityEventPage } from '../api'
import type { ObservabilityEventItem } from '../model'

const message = useMessage()
const loading = ref(false)
const rows = ref<ObservabilityEventItem[]>([])
const query = reactive({ eventType: '', eventLevel: '' })

const highCount = computed(() => rows.value.filter(item => item.eventLevel === 'high').length)
const mediumCount = computed(() => rows.value.filter(item => item.eventLevel === 'medium').length)
const abnormalIpCount = computed(() => new Set(rows.value.map(item => item.ipAddress).filter(Boolean)).size)

async function loadRows() {
  loading.value = true
  try {
    // 安全事件来自后端运维事件表，筛选项只提交非空条件。
    const page = await getSecurityEventPage({
      page: 1,
      pageSize: 20,
      eventType: query.eventType as any,
      eventLevel: query.eventLevel,
    })
    rows.value = page.records
  }
  catch (error: any) {
    rows.value = []
    message.error(error?.message || '加载安全事件失败')
  }
  finally {
    loading.value = false
  }
}

const columns = [
  { title: '事件类型', key: 'eventType' },
  { title: '级别', key: 'eventLevel', render: (row: ObservabilityEventItem) => h(NTag, { type: row.eventLevel === 'high' ? 'error' : row.eventLevel === 'medium' ? 'warning' : 'info' }, { default: () => row.eventLevel || '-' }) },
  { title: '账号', key: 'accountIdentifier' },
  { title: 'IP', key: 'ipAddress' },
  { title: '描述', key: 'detail' },
  { title: '时间', key: 'eventTime' },
]

onMounted(loadRows)
</script>

<template>
  <PageShell title="安全事件" description="追踪登录失败、越权访问和敏感操作等安全信号。">
    <template #actions><n-button :loading="loading" @click="loadRows">刷新</n-button></template>
    <div class="security-metrics">
      <MetricCard title="高危事件" :value="highCount" hint="需要处理" />
      <MetricCard title="中危事件" :value="mediumCount" />
      <MetricCard title="异常 IP" :value="abnormalIpCount" />
    </div>
    <n-card class="admin-card" title="筛选条件" style="margin-top: 16px">
      <div class="filter-grid">
        <n-input v-model:value="query.eventType" placeholder="事件类型" />
        <n-select v-model:value="query.eventLevel" clearable placeholder="事件级别" :options="[{ label: '低', value: 'low' }, { label: '中', value: 'medium' }, { label: '高', value: 'high' }]" />
        <n-button type="primary" @click="loadRows">查询</n-button>
      </div>
    </n-card>
    <n-card class="admin-card" title="事件列表" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="rows" :loading="loading" />
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
