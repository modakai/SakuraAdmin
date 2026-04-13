<script lang="ts" setup>
import type { AuthEntry } from '@/utils/auth-routing'

import { useAuth } from '@/composables/use-auth'

import GitHubButton from './github-button.vue'
import GoogleButton from './google-button.vue'
import PrivacyPolicyButton from './privacy-policy-button.vue'
import TermsOfServiceButton from './terms-of-service-button.vue'
import ToForgotPasswordLink from './to-forgot-password-link.vue'

const props = withDefaults(defineProps<Props>(), {
  entry: 'admin',
})

const { login, loading } = useAuth()

interface Props {
  entry?: AuthEntry
}

// 不同入口共用同一表单组件，只切默认账号和跳转方向。
const formState = reactive({
  userAccount: props.entry === 'admin' ? 'admin@example.com' : 'student@example.com',
  userPassword: '12345678',
})
const errorMessage = ref('')

async function handleLogin() {
  errorMessage.value = ''

  try {
    await login({
      userAccount: formState.userAccount,
      userPassword: formState.userPassword,
      entry: props.entry,
    })
  }
  catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败，请稍后重试。'
  }
}
</script>

<template>
  <UiCard class="w-full max-w-sm">
    <UiCardHeader>
      <UiCardTitle class="text-2xl">
        {{ props.entry === 'admin' ? 'Admin Login' : 'User Login' }}
      </UiCardTitle>
      <UiCardDescription>
        {{ props.entry === 'admin'
          ? 'Use the admin login entry to access dashboard pages. Only users with admin role can enter the backend.'
          : 'Use the user login entry to access the public-facing learning pages.' }}
      </UiCardDescription>
    </UiCardHeader>
    <UiCardContent class="grid gap-4">
      <UiAlert v-if="errorMessage" variant="destructive">
        <UiAlertTitle>登录失败</UiAlertTitle>
        <UiAlertDescription>{{ errorMessage }}</UiAlertDescription>
      </UiAlert>

      <div class="grid gap-2">
        <UiLabel for="email">
          {{ $t('email') }}
        </UiLabel>
        <UiInput id="email" v-model="formState.userAccount" type="email" placeholder="m@example.com" required />
      </div>
      <div class="grid gap-2">
        <div class="flex items-center justify-between">
          <UiLabel for="password">
            {{ $t('password') }}
          </UiLabel>
          <ToForgotPasswordLink />
        </div>
        <UiInput id="password" v-model="formState.userPassword" type="password" required placeholder="*********" />
      </div>

      <UiButton class="w-full" @click="handleLogin">
        <UiSpinner v-if="loading" class="mr-2" />
        {{ $t('login') }}
      </UiButton>

      <UiSeparator label="Or continue with" />

      <div class="flex flex-col items-center justify-between gap-4">
        <GitHubButton />
        <GoogleButton />
      </div>

      <UiCardDescription>
        演示账号需使用后端已存在的数据，密码长度至少 8 位。
      </UiCardDescription>

      <UiCardDescription>
        By clicking login, you agree to our
        <TermsOfServiceButton />
        and
        <PrivacyPolicyButton />
      </UiCardDescription>
    </UiCardContent>
  </UiCard>
</template>
