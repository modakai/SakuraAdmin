<script setup lang="ts">
import { ArrowRightIcon, ShieldCheckIcon } from '@lucide/vue'

import { useAuth } from '@/composables/use-auth'
import { userFeatures, userHighlights, userMetrics } from '@/constants/user-portal'

const { hasAdminAccess, isLogin } = useAuth()
</script>

<template>
  <div class="space-y-10">
    <section class="grid gap-6 lg:grid-cols-[minmax(0,1.2fr)_minmax(320px,0.8fr)]">
      <UiCard class="border-none bg-gradient-to-br from-sky-500 to-cyan-500 text-white shadow-xl">
        <UiCardHeader class="space-y-5">
          <UiBadge variant="secondary" class="w-fit bg-white/15 text-white hover:bg-white/15">
            用户端首页
          </UiBadge>
          <div class="space-y-3">
            <UiCardTitle class="text-4xl leading-tight md:text-5xl">
              一套模板，同时覆盖用户端与后台管理。
            </UiCardTitle>
            <UiCardDescription class="max-w-2xl text-sky-50">
              这里承载用户端首页、刷题、学习记录和个人中心；后台则保留现成的管理页面与工作台。
            </UiCardDescription>
          </div>
        </UiCardHeader>
        <UiCardContent class="flex flex-wrap gap-3">
          <UiButton variant="secondary" @click="$router.push('/practice')">
            开始刷题
            <ArrowRightIcon class="size-4" />
          </UiButton>
          <UiButton
            v-if="hasAdminAccess"
            variant="outline"
            class="border-white/30 bg-white/10 text-white hover:bg-white/15"
            @click="$router.push('/dashboard')"
          >
            <ShieldCheckIcon class="mr-1 size-4" />
            进入后台
          </UiButton>
          <UiButton
            v-else-if="!isLogin"
            variant="outline"
            class="border-white/30 bg-white/10 text-white hover:bg-white/15"
            @click="$router.push('/login')"
          >
            用户登录
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
          <UiCardTitle>用户端说明</UiCardTitle>
          <UiCardDescription>首版模板已补齐用户端首页、登录页、刷题中心、学习记录和个人中心。</UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="space-y-3 text-sm text-muted-foreground">
          <p>用户端登录入口：`/login`</p>
          <p>后台登录入口：`/auth/sign-in`</p>
          <p>统一认证返回后，是否允许进入后台由角色决定。</p>
        </UiCardContent>
      </UiCard>

      <UiCard>
        <UiCardHeader>
          <UiCardTitle>近期动态</UiCardTitle>
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
