<script setup lang="ts">
import { BookMarkedIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'

import { BasicPage } from '@/components/global-layout'

/**
 * 字典管理占位页。
 * 先提供统一入口，后续可在这里继续扩展字典列表、编辑和状态管理能力。
 */
const { t } = useI18n()
const placeholderFeatures = computed(() => [
  t('pages.dicts.featureType'),
  t('pages.dicts.featureItem'),
  t('pages.dicts.featureStatus'),
])
</script>

<template>
  <BasicPage :title="t('pages.dicts.title')" :description="t('pages.dicts.description')" sticky>
    <UiCard>
      <UiCardHeader>
        <UiCardTitle class="flex items-center gap-2">
          <BookMarkedIcon class="size-5" />
          {{ t('pages.dicts.buildingTitle') }}
        </UiCardTitle>
        <UiCardDescription>
          {{ t('pages.dicts.buildingDesc') }}
        </UiCardDescription>
      </UiCardHeader>
      <UiCardContent class="space-y-3">
        <div class="rounded-lg border border-dashed bg-muted/30 p-4 text-sm text-muted-foreground">
          {{ t('pages.dicts.todo') }}
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
