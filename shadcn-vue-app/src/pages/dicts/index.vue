<script setup lang="ts">
import { BookMarkedIcon } from '@lucide/vue'

import { BasicPage } from '@/components/global-layout'

/**
 * 字典管理占位页。
 * 先提供统一入口，后续可在这里继续扩展字典列表、编辑和状态管理能力。
 */
const placeholderFeatures = [
  '字典类型维护',
  '字典数据维护',
  '启用状态控制',
]
</script>

<template>
  <BasicPage title="字典管理" description="维护系统字典类型与字典数据的基础信息。" sticky>
    <UiCard>
      <UiCardHeader>
        <UiCardTitle class="flex items-center gap-2">
          <BookMarkedIcon class="size-5" />
          功能建设中
        </UiCardTitle>
        <UiCardDescription>
          当前页面为占位页，已挂载到后台“系统管理”菜单下，方便后续继续扩展真实业务功能。
        </UiCardDescription>
      </UiCardHeader>
      <UiCardContent class="space-y-3">
        <div class="rounded-lg border border-dashed bg-muted/30 p-4 text-sm text-muted-foreground">
          后续可以在这里接入字典分页查询、表单编辑、状态切换和缓存刷新等能力。
        </div>

        <div class="flex flex-wrap gap-2">
          <UiBadge v-for="feature in placeholderFeatures" :key="feature" variant="secondary">
            {{ feature }}
          </UiBadge>
        </div>
      </UiCardContent>
    </UiCard>
  </BasicPage>
</template>

<route lang="yaml">
meta:
  auth: true
</route>
