<script setup lang="ts">
import { AlertCircleIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'

import type { LoginPayload } from '@/services/types/auth.type'

import { useAuth } from '@/composables/use-auth'

// 用户端登录页走统一认证逻辑，但默认回用户端首页。
const { loading, login } = useAuth()
const formState = reactive<LoginPayload>({
  userAccount: 'student@example.com',
  userPassword: '12345678',
  entry: 'user',
})
const errorMessage = ref('')
const { t } = useI18n()

async function handleLogin() {
  errorMessage.value = ''
  try {
    await login(formState)
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : t('pages.login.failedRetry')
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-muted/30 p-4">
    <UiCard class="w-full max-w-sm">
      <UiCardHeader>
        <UiCardTitle class="text-2xl">
          {{ t('pages.login.title') }}
        </UiCardTitle>
        <UiCardDescription>
          {{ t('pages.login.description') }}
        </UiCardDescription>
      </UiCardHeader>
      <UiCardContent class="grid gap-4">
        <UiAlert v-if="errorMessage" variant="destructive">
          <AlertCircleIcon class="size-4" />
          <UiAlertTitle>{{ t('pages.login.failedTitle') }}</UiAlertTitle>
          <UiAlertDescription>{{ errorMessage }}</UiAlertDescription>
        </UiAlert>

        <div class="grid gap-2">
          <UiLabel for="user-email">
            {{ t('common.email') }}
          </UiLabel>
          <UiInput id="user-email" v-model="formState.userAccount" type="email" :placeholder="t('pages.login.emailPlaceholder')" />
        </div>

        <div class="grid gap-2">
          <UiLabel for="user-password">
            {{ t('common.password') }}
          </UiLabel>
          <UiInput id="user-password" v-model="formState.userPassword" type="password" :placeholder="t('pages.login.passwordPlaceholder')" />
        </div>

        <UiButton class="w-full" @click="handleLogin">
          <UiSpinner v-if="loading" class="mr-2" />
          {{ t('pages.login.enterUser') }}
        </UiButton>

        <UiCardDescription>
          {{ t('pages.login.demoTip') }}
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
