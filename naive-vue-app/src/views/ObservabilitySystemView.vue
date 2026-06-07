<script setup lang="ts">
import PageShell from '../components/admin/PageShell.vue'
import MetricCard from '../components/admin/MetricCard.vue'

const dependencies = [
  { name: '数据库', status: '正常', latency: '18 ms', pool: '活跃 3 / 空闲 7 / 总数 10' },
  { name: 'Redis', status: '正常', latency: '6 ms', pool: '未暴露连接池指标' },
]
</script>

<template>
  <PageShell title="系统状态" description="查看 JVM、主机资源、数据库和 Redis 的当前运行状态。">
    <template #actions><n-button>刷新</n-button></template>
    <div class="system-grid">
      <MetricCard title="综合状态" value="正常" hint="采样时间：2026-06-07 19:45" />
      <MetricCard title="系统 CPU" value="28.4%" hint="负载稳定" />
      <MetricCard title="系统内存" value="62.7%" hint="7.5 GB / 12 GB" />
      <MetricCard title="磁盘" value="48.2%" hint="386 GB / 800 GB" />
    </div>
    <n-grid style="margin-top: 16px" :cols="2" :x-gap="16" :y-gap="16" responsive="screen">
      <n-grid-item>
        <n-card class="admin-card" title="JVM">
          <n-space vertical>
            <div><span>堆内存</span><n-progress type="line" :percentage="58" /></div>
            <div><span>非堆内存</span><n-progress type="line" :percentage="35" /></div>
            <n-grid :cols="3" :x-gap="12">
              <n-grid-item><n-statistic label="线程" value="86" /></n-grid-item>
              <n-grid-item><n-statistic label="守护线程" value="42" /></n-grid-item>
              <n-grid-item><n-statistic label="GC 耗时" value="19 ms" /></n-grid-item>
            </n-grid>
          </n-space>
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="admin-card" title="依赖状态">
          <n-list>
            <n-list-item v-for="item in dependencies" :key="item.name">
              <n-thing :title="item.name" :description="`${item.latency} · ${item.pool}`">
                <template #header-extra><n-tag type="success">{{ item.status }}</n-tag></template>
              </n-thing>
            </n-list-item>
          </n-list>
        </n-card>
      </n-grid-item>
    </n-grid>
  </PageShell>
</template>

<style scoped>
.system-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

@media (max-width: 980px) {
  .system-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .system-grid {
    grid-template-columns: 1fr;
  }
}
</style>
