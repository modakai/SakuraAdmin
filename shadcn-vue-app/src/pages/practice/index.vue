<script setup lang="ts">
import { practiceCards } from '@/constants/user-portal'
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-2">
      <h1 class="text-3xl font-semibold tracking-tight">
        刷题中心
      </h1>
      <p class="text-muted-foreground">
        这里预留分类训练、推荐训练和专题题单入口，后续可直接接题库接口。
      </p>
    </div>

    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      <UiCard v-for="item in practiceCards" :key="item.title">
        <UiCardHeader>
          <UiCardTitle>{{ item.title }}</UiCardTitle>
          <UiCardDescription>{{ item.description }}</UiCardDescription>
        </UiCardHeader>
        <UiCardContent class="space-y-3">
          <div class="flex items-center justify-between text-sm text-muted-foreground">
            <span>{{ item.level }}</span>
            <span>{{ item.count }} 题</span>
          </div>
          <UiButton class="w-full">
            开始练习
          </UiButton>
        </UiCardContent>
      </UiCard>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: user
  section: user
</route>
