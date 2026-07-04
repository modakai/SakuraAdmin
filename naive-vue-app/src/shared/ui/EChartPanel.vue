<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useElementSize } from '@vueuse/core'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsCoreOption, EChartsType } from 'echarts/core'

const props = defineProps<{
  option: EChartsCoreOption
}>()

const chartRef = ref<HTMLElement | null>(null)
let chart: EChartsType | null = null

echarts.use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const { width, height } = useElementSize(chartRef)

function renderChart() {
  if (!chartRef.value) {
    return
  }
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption(props.option, true)
}

onMounted(renderChart)

watch(() => props.option, renderChart, { deep: true })

watch([width, height], () => {
  // 容器尺寸变化时主动 resize，避免侧边栏折叠后图表错位。
  chart?.resize()
})

onBeforeUnmount(() => {
  chart?.dispose()
  chart = null
})
</script>

<template>
  <div ref="chartRef" class="echart-panel" />
</template>

<style scoped>
.echart-panel {
  width: 100%;
  height: 260px;
}
</style>
