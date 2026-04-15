<script setup lang="ts">
import { ImageUpload } from '@/components/prop-ui/image-upload'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { practiceCards } from '@/constants/user-portal'

/**
 * 单图上传示例数据。
 */
const singleImageUrls = ref<string[]>([])

/**
 * 多图上传示例数据。
 */
const multipleImageUrls = ref<string[]>([])
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

    <div class="grid gap-4 xl:grid-cols-2">
      <Card>
        <CardHeader>
          <CardTitle>单图上传演示</CardTitle>
          <CardDescription>
            统一返回数组数据，单图模式下数组长度最多为 1。
          </CardDescription>
        </CardHeader>
        <CardContent class="space-y-4">
          <ImageUpload v-model="singleImageUrls" :max-count="1" />
          <pre class="overflow-x-auto rounded-lg bg-slate-950 p-4 text-xs text-slate-50">{{ JSON.stringify(singleImageUrls, null, 2) }}</pre>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>多图上传演示</CardTitle>
          <CardDescription>
            多图模式会在达到上限后阻止继续选择，并按上传成功顺序输出图片地址。
          </CardDescription>
        </CardHeader>
        <CardContent class="space-y-4">
          <ImageUpload v-model="multipleImageUrls" :max-count="3" tips="最多上传 3 张图片，支持编辑态回显与删除" />
          <pre class="overflow-x-auto rounded-lg bg-slate-950 p-4 text-xs text-slate-50">{{ JSON.stringify(multipleImageUrls, null, 2) }}</pre>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<route lang="yaml">
meta:
  layout: user
  section: user
</route>
