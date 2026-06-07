<script setup lang="ts">
import { computed, ref } from 'vue'
import PageShell from '../components/admin/PageShell.vue'
import MetricCard from '../components/admin/MetricCard.vue'
import EChartPanel from '../components/admin/EChartPanel.vue'

const loading = ref(false)
const cards = [
  { title: '用户总数', value: 1280, hint: '较昨日 +24' },
  { title: '今日活跃', value: 326, hint: '活跃率 25.5%' },
  { title: '通知触达', value: '93.8%', hint: '近 24 小时' },
  { title: '慢接口', value: 12, hint: '需要关注' },
]

const workspaceTrend = [
  { day: '周一', visits: 420, activeUsers: 168 },
  { day: '周二', visits: 680, activeUsers: 231 },
  { day: '周三', visits: 520, activeUsers: 206 },
  { day: '周四', visits: 760, activeUsers: 284 },
  { day: '周五', visits: 610, activeUsers: 246 },
  { day: '周六', visits: 910, activeUsers: 326 },
  { day: '周日', visits: 740, activeUsers: 298 },
]

const moduleUsage = [
  { name: '用户管理', value: 32 },
  { name: '通知公告', value: 24 },
  { name: '审计日志', value: 18 },
  { name: '运维监控', value: 16 },
  { name: '字典协议', value: 10 },
]

const trendOption = computed(() => ({
  color: ['#18a058', '#2080f0'],
  tooltip: { trigger: 'axis' },
  legend: { top: 0, data: ['访问量', '活跃用户'] },
  grid: { top: 42, right: 22, bottom: 28, left: 42 },
  xAxis: {
    type: 'category',
    data: workspaceTrend.map(item => item.day),
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: '访问量',
      type: 'bar',
      barWidth: 18,
      itemStyle: { borderRadius: [5, 5, 0, 0] },
      data: workspaceTrend.map(item => item.visits),
    },
    {
      name: '活跃用户',
      type: 'line',
      smooth: true,
      symbolSize: 7,
      data: workspaceTrend.map(item => item.activeUsers),
    },
  ],
}))

const moduleUsageOption = computed(() => ({
  color: ['#18a058', '#2080f0', '#f0a020', '#d03050', '#7c3aed'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '模块使用',
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '43%'],
      avoidLabelOverlap: true,
      data: moduleUsage,
    },
  ],
}))

function refresh() {
  // mock 刷新只模拟后台工作台的刷新反馈。
  loading.value = true
  window.setTimeout(() => (loading.value = false), 600)
}
</script>

<template>
  <PageShell title="workspace" description="workspace description">
    <template #actions>
      <n-button type="primary" :loading="loading" @click="refresh">刷新数据</n-button>
    </template>

    <n-tabs type="segment" animated>
      <n-tab-pane name="overview" tab="Overview">
        <div class="dashboard-grid">
          <MetricCard v-for="card in cards" :key="card.title" v-bind="card" />
        </div>
        <n-grid class="dashboard-section" :cols="2" :x-gap="16" :y-gap="16" responsive="screen">
          <n-grid-item>
            <n-card class="admin-card" title="访问趋势">
              <EChartPanel :option="trendOption" />
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card class="admin-card" title="模块使用占比">
              <EChartPanel :option="moduleUsageOption" />
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card class="admin-card" title="最近动态">
              <n-timeline>
                <n-timeline-item type="success" title="管理员登录" content="sakura 从本机进入后台" time="19:40" />
                <n-timeline-item title="用户状态更新" content="operator 启用了一个普通用户" time="18:12" />
                <n-timeline-item type="warning" title="慢接口告警" content="/api/audit/log/export 耗时 1280ms" time="18:40" />
              </n-timeline>
            </n-card>
          </n-grid-item>
        </n-grid>
      </n-tab-pane>
      <n-tab-pane name="analytics" tab="Analytics" disabled />
      <n-tab-pane name="reports" tab="Reports" disabled />
      <n-tab-pane name="notifications" tab="Notifications" disabled />
    </n-tabs>
  </PageShell>
</template>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.dashboard-section {
  margin-top: 16px;
}

@media (max-width: 980px) {
  .dashboard-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
