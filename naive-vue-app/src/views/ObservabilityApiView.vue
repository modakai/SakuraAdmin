<script setup lang="ts">
import { computed, h, reactive } from 'vue'
import { NTag } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import MetricCard from '../components/admin/MetricCard.vue'
import EChartPanel from '../components/admin/EChartPanel.vue'
import { errorTrend, slowApis, type ObservabilityEventItem } from '../mock/admin'

const query = reactive({ requestPath: '', ipAddress: '' })
const columns = [
  { title: '路径', key: 'requestPath' },
  { title: '方法', key: 'httpMethod' },
  { title: '状态码', key: 'statusCode', render: (row: ObservabilityEventItem) => h(NTag, { type: row.statusCode >= 500 ? 'error' : 'success' }, { default: () => row.statusCode }) },
  { title: '耗时', key: 'durationMillis', render: (row: ObservabilityEventItem) => `${row.durationMillis} ms` },
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
    data: errorTrend.map(item => item.bucket),
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: '4xx',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.08 },
      data: errorTrend.map(item => item.clientErrorCount),
    },
    {
      name: '5xx',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.08 },
      data: errorTrend.map(item => item.serverErrorCount),
    },
    {
      name: '异常',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.08 },
      data: errorTrend.map(item => item.exceptionCount),
    },
  ],
}))
</script>

<template>
  <PageShell title="接口监控" description="查看接口耗时、慢接口列表和错误趋势。">
    <template #actions><n-button>刷新</n-button></template>
    <div class="api-metrics">
      <MetricCard title="慢接口" :value="slowApis.length" />
      <MetricCard title="错误事件" value="11" />
      <MetricCard title="平均慢接口耗时" value="1055 ms" />
    </div>
    <n-card class="admin-card" title="筛选条件" style="margin-top: 16px">
      <div class="filter-grid">
        <n-input v-model:value="query.requestPath" placeholder="请求路径" />
        <n-input v-model:value="query.ipAddress" placeholder="IP 地址" />
        <n-input type="datetime-local" />
        <n-button type="primary">查询</n-button>
      </div>
    </n-card>
    <n-card class="admin-card" title="错误趋势" style="margin-top: 16px">
      <EChartPanel :option="errorTrendOption" />
    </n-card>
    <n-card class="admin-card" title="慢接口列表" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="slowApis" />
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
