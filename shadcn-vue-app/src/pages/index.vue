<script setup lang="ts">
import { ArrowRightIcon, CircleUserRoundIcon, ShieldCheckIcon, SparklesIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'

import { useAuth } from '@/composables/use-auth'

const { hasAdminAccess, isLogin } = useAuth()
const { t } = useI18n()

const userMetrics = computed(() => [
  { title: t('pages.home.metrics.today'), value: '48', hint: t('pages.home.metrics.todayHint') },
  { title: t('pages.home.metrics.accuracy'), value: '89%', hint: t('pages.home.metrics.accuracyHint') },
  { title: t('pages.home.metrics.streak'), value: '16', hint: t('pages.home.metrics.streakHint') },
])

const userFeatures = computed(() => [
  { title: t('pages.home.features.practiceTitle'), description: t('pages.home.features.practiceDesc'), icon: SparklesIcon },
  { title: t('pages.home.features.trackTitle'), description: t('pages.home.features.trackDesc'), icon: CircleUserRoundIcon },
  { title: t('pages.home.features.adminTitle'), description: t('pages.home.features.adminDesc'), icon: ShieldCheckIcon },
])

const userHighlights = computed(() => [
  { title: t('pages.home.highlights.algTitle'), content: t('pages.home.highlights.algContent'), time: t('pages.home.highlights.algTime') },
  { title: t('pages.home.highlights.reportTitle'), content: t('pages.home.highlights.reportContent'), time: t('pages.home.highlights.reportTime') },
  { title: t('pages.home.highlights.auditTitle'), content: t('pages.home.highlights.auditContent'), time: t('pages.home.highlights.auditTime') },
])
</script>

<template>
  <div class="space-y-10">
    <section class="grid gap-6 lg:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
      <UiCard class="border-none bg-gradient-to-br from-sky-500 to-cyan-500 text-white shadow-xl">
        <UiCardHeader class="space-y-5">
          <UiBadge variant="secondary" class="w-fit bg-white/15 text-white hover:bg-white/15">
            {{ t('pages.home.badge') }}
          </UiBadge>
          <div class="space-y-3">
            <UiCardTitle class="text-4xl leading-tight md:text-5xl">
              {{ t('pages.home.title') }}
            </UiCardTitle>
            <UiCardDescription class="max-w-2xl text-sky-50">
              {{ t('pages.home.description') }}
            </UiCardDescription>
          </div>
        </UiCardHeader>
        <UiCardContent class="flex flex-wrap gap-3">
          <UiButton variant="secondary" @click="$router.push('/practice')">
            {{ t('pages.home.startPractice') }}
            <ArrowRightIcon class="size-4" />
          </UiButton>
          <UiButton
            v-if="hasAdminAccess"
            variant="outline"
            class="border-white/30 bg-white/10 text-white hover:bg-white/15"
            @click="$router.push('/dashboard')"
          >
            <ShieldCheckIcon class="mr-1 size-4" />
            {{ t('pages.home.enterAdmin') }}
          </UiButton>
          <UiButton
            v-else-if="!isLogin"
            variant="outline"
            class="border-white/30 bg-white/10 text-white hover:bg-white/15"
            @click="$router.push('/login')"
          >
            {{ t('pages.login.enterUser') }}
          </UiButton>
        </UiCardContent>
      </UiCard>

      <div class="grid gap-4">
        <UiCard v-for="metric in userMetrics" :key="metric.title">
          <UiCardHeader class="pb-3">
            <UiCardDescription>{{ metric.title }}</UiCardDescription>
            <UiCardTitle class="text-3xl">
              {{ metric.value }}
            </UiCardTitle>
          </UiCardHeader>
          <UiCardContent class="text-sm text-muted-foreground">
            {{ metric.hint }}
          </UiCardContent>
        </UiCard>
      </div>
    </section>

    <section class="grid gap-4 md:grid-cols-3">
      <UiCard v-for="item in userFeatures" :key="item.title">
        <UiCardHeader>
          <component :is="item.icon" class="size-5 text-primary" />
          <UiCardTitle class="pt-2">
            {{ item.title }}
          </UiCardTitle>
          <UiCardDescription>{{ item.description }}</UiCardDescription>
        </UiCardHeader>
      </UiCard>
    </section>

    <section class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_360px]">
      <UiCard>
        <UiCardHeader>
          <UiCardTitle>{{ t('pages.home.guideTitle') }}</UiCardTitle>
          <UiCardDescription>{{ t('pages.home.guideDesc') }}</UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="space-y-3 text-sm text-muted-foreground">
          <p>{{ t('pages.home.userLoginPath') }}</p>
          <p>{{ t('pages.home.adminLoginPath') }}</p>
          <p>{{ t('pages.home.accessNote') }}</p>
        </UiCardContent>
      </UiCard>

      <UiCard>
        <UiCardHeader>
          <UiCardTitle>{{ t('pages.home.recentTitle') }}</UiCardTitle>
        </UiCardHeader>
        <UiCardContent class="space-y-4">
          <div v-for="item in userHighlights" :key="item.title" class="rounded-lg border p-4">
            <div class="flex items-center justify-between gap-4">
              <p class="font-medium">
                {{ item.title }}
              </p>
              <span class="text-xs text-muted-foreground">{{ item.time }}</span>
            </div>
            <p class="mt-2 text-sm text-muted-foreground">
              {{ item.content }}
            </p>
          </div>
        </UiCardContent>
      </UiCard>
    </section>
  </div>
</template>

<route lang="yaml">
meta:
  layout: user
  section: user
</route>
