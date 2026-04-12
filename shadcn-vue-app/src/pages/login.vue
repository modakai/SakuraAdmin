<script setup lang="ts">
import { AlertCircleIcon } from '@lucide/vue'

import type { LoginPayload } from '@/constants/mock-auth'

import { useAuth } from '@/composables/use-auth'

// 用户端登录页走统一认证逻辑，但默认回用户端首页。
const { loading, login } = useAuth()
const formState = reactive<LoginPayload>({
  email: 'student@example.com',
  password: '123456',
  entry: 'user',
})
const errorMessage = ref('')

async function handleLogin() {
  errorMessage.value = ''
  try {
    await login(formState)
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后重试。'
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-muted/30 p-4">
    <UiCard class="w-full max-w-sm">
      <UiCardHeader>
        <UiCardTitle class="text-2xl">
          用户端登录
        </UiCardTitle>
        <UiCardDescription>
          登录后默认进入用户端首页。如账号具备后台角色，可从用户端进入后台。
        </UiCardDescription>
      </UiCardHeader>
      <UiCardContent class="grid gap-4">
        <UiAlert v-if="errorMessage" variant="destructive">
          <AlertCircleIcon class="size-4" />
          <UiAlertTitle>登录失败</UiAlertTitle>
          <UiAlertDescription>{{ errorMessage }}</UiAlertDescription>
        </UiAlert>

        <div class="grid gap-2">
          <UiLabel for="user-email">
            邮箱
          </UiLabel>
          <UiInput id="user-email" v-model="formState.email" type="email" placeholder="student@example.com" />
        </div>

        <div class="grid gap-2">
          <UiLabel for="user-password">
            密码
          </UiLabel>
          <UiInput id="user-password" v-model="formState.password" type="password" placeholder="123456" />
        </div>

        <UiButton class="w-full" @click="handleLogin">
          <UiSpinner v-if="loading" class="mr-2" />
          进入用户端
        </UiButton>

        <UiCardDescription>
          演示账号：`student@example.com / 123456`，管理员账号：`admin@example.com / 123456`
        </UiCardDescription>
      </UiCardContent>
    </UiCard>
  </div>
</template>

<route lang="yaml">
meta:
  layout: false
  guestOnly: true
  authEntry: user
</route>
