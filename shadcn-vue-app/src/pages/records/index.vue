<script setup lang="ts">
import { recordRows, userMetrics } from '@/constants/user-portal'
</script>

<template>
  <div class="space-y-6">
    <div class="space-y-2">
      <h1 class="text-3xl font-semibold tracking-tight">
        学习记录
      </h1>
      <p class="text-muted-foreground">
        登录后查看最近训练成绩、完成进度与更新时间。
      </p>
    </div>

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
        <UiCardTitle>最近记录</UiCardTitle>
      </UiCardHeader>
      <UiCardContent>
        <div class="overflow-x-auto">
          <UiTable>
            <UiTableHeader>
              <UiTableRow>
                <UiTableHead>练习名称</UiTableHead>
                <UiTableHead>成绩</UiTableHead>
                <UiTableHead>完成进度</UiTableHead>
                <UiTableHead>更新时间</UiTableHead>
              </UiTableRow>
            </UiTableHeader>
            <UiTableBody>
              <UiTableRow v-for="row in recordRows" :key="row.title">
                <UiTableCell class="font-medium">
                  {{ row.title }}
                </UiTableCell>
                <UiTableCell>{{ row.score }}</UiTableCell>
                <UiTableCell>{{ row.progress }}</UiTableCell>
                <UiTableCell>{{ row.updatedAt }}</UiTableCell>
              </UiTableRow>
            </UiTableBody>
          </UiTable>
        </div>
      </UiCardContent>
    </UiCard>
  </div>
</template>

<route lang="yaml">
meta:
  layout: user
  auth: true
  section: user
</route>
