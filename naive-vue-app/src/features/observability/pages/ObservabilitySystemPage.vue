<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import MetricCard from '@/shared/ui/MetricCard.vue'
import { getSystemStatus } from '../api'
import type { SystemStatus } from '../model'

const message = useMessage()
const loading = ref(false)
const status = ref<SystemStatus | null>(null)

const dependencies = computed(() => [
  status.value?.database,
  status.value?.redis,
].filter(Boolean))

function percent(metric?: { usagePercent?: number, value?: number }) {
  return Math.round(metric?.usagePercent ?? metric?.value ?? 0)
}

async function loadStatus() {
  loading.value = true
  try {
    // 系统状态直接读取后端聚合接口，不访问 actuator 原始端点。
    status.value = await getSystemStatus()
  }
  catch (error: any) {
    status.value = null
    message.error(error?.message || '加载系统状态失败')
  }
  finally {
    loading.value = false
  }
}

onMounted(loadStatus)
</script>

<template>
  <PageShell title="系统状态" description="查看 JVM、主机资源、数据库和 Redis 的当前运行状态。">
    <template #actions><n-button :loading="loading" @click="loadStatus">刷新</n-button></template>
    <div class="system-grid">
      <MetricCard title="综合状态" :value="status?.overallStatus ?? '-'" :hint="`采样时间：${status?.sampleTime ?? '-'}`" />
      <MetricCard title="系统 CPU" :value="`${percent(status?.os.systemCpu)}%`" />
      <MetricCard title="系统内存" :value="`${percent(status?.os.memory)}%`" />
      <MetricCard title="磁盘" :value="`${percent(status?.os.disk)}%`" />
    </div>
    <n-grid style="margin-top: 16px" :cols="2" :x-gap="16" :y-gap="16" responsive="screen">
      <n-grid-item>
        <n-card class="admin-card" title="JVM">
          <n-space v-if="status" vertical>
            <div><span>堆内存</span><n-progress type="line" :percentage="percent(status.jvm.heapMemory)" /></div>
            <div><span>非堆内存</span><n-progress type="line" :percentage="percent(status.jvm.nonHeapMemory)" /></div>
            <n-grid :cols="3" :x-gap="12">
              <n-grid-item><n-statistic label="线程" :value="status.jvm.threadCount" /></n-grid-item>
              <n-grid-item><n-statistic label="守护线程" :value="status.jvm.daemonThreadCount" /></n-grid-item>
              <n-grid-item><n-statistic label="GC 耗时" :value="`${status.jvm.gcTimeMillis} ms`" /></n-grid-item>
            </n-grid>
          </n-space>
          <n-empty v-else description="暂无 JVM 数据" />
        </n-card>
      </n-grid-item>
      <n-grid-item>
        <n-card class="admin-card" title="依赖状态">
          <n-list>
            <n-list-item v-for="item in dependencies" :key="item!.name">
              <n-thing :title="item!.name" :description="`${item!.latencyMillis ?? '-'} ms · ${item!.message ?? '无异常'}`">
                <template #header-extra><n-tag :type="item!.status === 'up' ? 'success' : 'warning'">{{ item!.status }}</n-tag></template>
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
