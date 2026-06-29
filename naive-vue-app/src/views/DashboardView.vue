<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import MetricCard from '../components/admin/MetricCard.vue'
import EChartPanel from '../components/admin/EChartPanel.vue'
import { getDashboardStatistics } from '../services/api'
import type { DashboardStatistics } from '../services/types'

const message = useMessage()
const loading = ref(false)
const statistics = ref<DashboardStatistics | null>(null)

const cards = computed(() => [
  { title: '用户总数', value: statistics.value?.summary.userTotalCount ?? 0, hint: '当前未删除用户' },
  { title: '今日新增', value: statistics.value?.summary.todayNewUserCount ?? 0, hint: '今日注册用户' },
  { title: '通知数量', value: statistics.value?.summary.notificationCount ?? 0, hint: '已发布通知公告' },
  { title: '操作日志', value: statistics.value?.summary.operationLogCount ?? 0, hint: '后台审计记录' },
])

const trendOption = computed(() => ({
  color: ['#18a058'],
  tooltip: { trigger: 'axis' },
  grid: { top: 24, right: 22, bottom: 28, left: 42 },
  xAxis: {
    type: 'category',
    data: (statistics.value?.loginTrend ?? []).map(item => item.label),
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: '登录次数',
      type: 'line',
      smooth: true,
      symbolSize: 7,
      data: (statistics.value?.loginTrend ?? []).map(item => item.loginCount),
    },
  ],
}))

const moduleUsageOption = computed(() => ({
  color: ['#18a058', '#2080f0', '#f0a020', '#d03050', '#7c3aed'],
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '操作模块',
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '43%'],
      data: (statistics.value?.recentOperations ?? []).slice(0, 6).map(item => ({ name: item.module || '未知模块', value: 1 })),
    },
  ],
}))

async function refresh() {
  loading.value = true
  try {
    // 工作台聚合数据来自后端 dashboard 统计接口。
    statistics.value = await getDashboardStatistics()
  }
  catch (error: any) {
    statistics.value = null
    message.error(error?.message || '加载工作台失败')
  }
  finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <PageShell title="工作台" description="后台关键指标、登录趋势和最近操作概览。">
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
            <n-card class="admin-card" title="登录趋势">
              <EChartPanel :option="trendOption" />
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card class="admin-card" title="最近操作模块">
              <EChartPanel :option="moduleUsageOption" />
            </n-card>
          </n-grid-item>
          <n-grid-item>
            <n-card class="admin-card" title="最近动态">
              <n-timeline>
                <n-timeline-item
                  v-for="item in statistics?.recentOperations ?? []"
                  :key="item.id"
                  :type="item.result === 'success' ? 'success' : 'error'"
                  :title="item.action"
                  :content="`${item.operator} · ${item.module} · ${item.ipAddress}`"
                  :time="item.operationTime"
                />
              </n-timeline>
              <n-empty v-if="!statistics?.recentOperations?.length" description="暂无操作记录" />
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
