<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { NTag, useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import MetricCard from '@/shared/ui/MetricCard.vue'
import EChartPanel from '@/shared/ui/EChartPanel.vue'
import { getApiSummary, getErrorTrend, getSlowApiPage } from '../api'
import type { ApiSummary, ErrorTrendBucket, ObservabilityEventItem } from '../model'

const message = useMessage()
const loading = ref(false)
const summary = ref<ApiSummary | null>(null)
const slowApis = ref<ObservabilityEventItem[]>([])
const errorTrend = ref<ErrorTrendBucket[]>([])
const query = reactive({
  requestPath: '',
  ipAddress: '',
  timeRange: null as [number, number] | null,
})

function buildQuery() {
  return {
    page: 1,
    pageSize: 10,
    requestPath: query.requestPath,
    ipAddress: query.ipAddress,
    startTime: query.timeRange?.[0] ? new Date(query.timeRange[0]).toISOString() : '',
    endTime: query.timeRange?.[1] ? new Date(query.timeRange[1]).toISOString() : '',
  }
}

async function loadRows() {
  loading.value = true
  try {
    // 接口监控页拆成摘要、趋势、慢接口三类后端接口。
    const params = buildQuery()
    const [nextSummary, trend, slowPage] = await Promise.all([
      getApiSummary(params),
      getErrorTrend(params),
      getSlowApiPage(params),
    ])
    summary.value = nextSummary
    errorTrend.value = trend
    slowApis.value = slowPage.records
  }
  catch (error: any) {
    summary.value = null
    errorTrend.value = []
    slowApis.value = []
    message.error(error?.message || '加载接口监控失败')
  }
  finally {
    loading.value = false
  }
}

const columns = [
  { title: '路径', key: 'requestPath' },
  { title: '方法', key: 'httpMethod' },
  { title: '状态码', key: 'statusCode', render: (row: ObservabilityEventItem) => h(NTag, { type: (row.statusCode ?? 0) >= 500 ? 'error' : 'success' }, { default: () => row.statusCode ?? '-' }) },
  { title: '耗时', key: 'durationMillis', render: (row: ObservabilityEventItem) => `${row.durationMillis ?? 0} ms` },
  { title: 'IP', key: 'ipAddress' },
  { title: '账号', key: 'accountIdentifier' },
  { title: '时间', key: 'eventTime' },
]

const errorTrendOption = computed(() => ({
  color: ['#18a058', '#f0a020', '#d03050'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, data: ['4xx', '5xx', '异常'] },
  grid: { top: 42, right: 18, bottom: 28, left: 36 },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: errorTrend.value.map(item => item.bucket),
  },
  yAxis: { type: 'value' },
  series: [
    { name: '4xx', type: 'line', smooth: true, areaStyle: { opacity: 0.08 }, data: errorTrend.value.map(item => item.clientErrorCount) },
    { name: '5xx', type: 'line', smooth: true, areaStyle: { opacity: 0.08 }, data: errorTrend.value.map(item => item.serverErrorCount) },
    { name: '异常', type: 'line', smooth: true, areaStyle: { opacity: 0.08 }, data: errorTrend.value.map(item => item.exceptionCount) },
  ],
}))

onMounted(loadRows)
</script>

<template>
  <PageShell title="接口监控" description="查看接口耗时、慢接口列表和错误趋势。">
    <template #actions><n-button :loading="loading" @click="loadRows">刷新</n-button></template>
    <div class="api-metrics">
      <MetricCard title="慢接口" :value="summary?.slowApiCount ?? 0" />
      <MetricCard title="错误事件" :value="summary?.errorCount ?? 0" />
      <MetricCard title="平均慢接口耗时" :value="`${summary?.averageSlowDurationMillis ?? 0} ms`" />
    </div>
    <n-card class="admin-card" title="筛选条件" style="margin-top: 16px">
      <div class="filter-grid">
        <n-input v-model:value="query.requestPath" placeholder="请求路径" />
        <n-input v-model:value="query.ipAddress" placeholder="IP 地址" />
        <n-date-picker
          v-model:value="query.timeRange"
          type="datetimerange"
          clearable
          start-placeholder="开始时间"
          end-placeholder="结束时间"
        />
        <n-button type="primary" @click="loadRows">查询</n-button>
      </div>
    </n-card>
    <n-card class="admin-card" title="错误趋势" style="margin-top: 16px">
      <EChartPanel :option="errorTrendOption" />
    </n-card>
    <n-card class="admin-card" title="慢接口列表" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="slowApis" :loading="loading" />
    </n-card>
  </PageShell>
</template>

<style scoped>
.api-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 780px) {
  .api-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
