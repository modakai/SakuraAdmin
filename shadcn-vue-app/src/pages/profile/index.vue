<script setup lang="ts">
import { ShieldCheckIcon } from '@lucide/vue'

import { useAuth } from '@/composables/use-auth'
import { userHighlights, userMetrics } from '@/constants/user-portal'

const { hasAdminAccess, session } = useAuth()
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
      <div class="space-y-2">
        <h1 class="text-3xl font-semibold tracking-tight">
          个人中心
        </h1>
        <p class="text-muted-foreground">
          展示当前用户信息、学习概览和后台入口。
        </p>
      </div>

      <UiButton
        v-if="hasAdminAccess"
        variant="outline"
        @click="$router.push('/dashboard')"
      >
        <ShieldCheckIcon class="mr-1 size-4" />
        进入后台管理
      </UiButton>
    </div>

    <div class="grid gap-6 lg:grid-cols-[320px_minmax(0,1fr)]">
      <UiCard>
        <UiCardHeader>
          <UiCardTitle>{{ session.user?.name }}</UiCardTitle>
          <UiCardDescription>{{ session.user?.email }}</UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="space-y-2">
          <UiBadge v-for="role in session.user?.roles" :key="role" variant="secondary" class="mr-2">
            {{ role }}
          </UiBadge>
        </UiCardContent>
      </UiCard>

      <div class="space-y-4">
        <div class="grid gap-4 md:grid-cols-3">
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

        <UiCard>
          <UiCardHeader>
            <UiCardTitle>最近动态</UiCardTitle>
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
      </div>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: user
  auth: true
  section: user
</route>
