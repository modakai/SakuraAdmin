<script lang="ts" setup>
import { AlertCircleIcon, ArrowRightIcon, LockKeyholeIcon, UserRoundIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'

import type { AuthEntry } from '@/utils/auth-routing'

import { useAuth } from '@/composables/use-auth'

import { getAuthEntryConfig } from './auth-entry-config'

const props = withDefaults(defineProps<Props>(), {
  entry: 'admin',
})

interface Props {
  entry?: AuthEntry
}

const { login, loading } = useAuth()
const { t } = useI18n()

// 前后台共用一套表单状态，差异从入口配置中读取。
const config = computed(() => getAuthEntryConfig(props.entry))
const formState = reactive({
  userAccount: config.value.defaultAccount,
  userPassword: '12345678',
})
const errorMessage = ref('')

// 当入口切换时，同步示例账号，避免前后台残留上一次输入模板。
watch(() => props.entry, () => {
  formState.userAccount = config.value.defaultAccount
  formState.userPassword = '12345678'
  errorMessage.value = ''
}, { immediate: true })

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
    errorMessage.value = error instanceof Error ? error.message : t('pages.login.failedRetry')
  }
}
</script>

<template>
  <UiCard class="w-full border-0 bg-transparent shadow-none">
    <UiCardHeader class="space-y-5 px-2">
      <div class="inline-flex w-fit items-center rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-xs font-medium tracking-[0.2em] text-sky-700 uppercase dark:border-sky-500/25 dark:bg-sky-500/10 dark:text-sky-100">
        {{ t(config.badgeKey) }}
      </div>

      <div class="space-y-2">
        <UiCardTitle class="text-3xl font-semibold tracking-tight text-slate-950 dark:text-slate-50">
          {{ t(config.titleKey) }}
        </UiCardTitle>
        <UiCardDescription class="max-w-md text-sm leading-6 text-slate-600 dark:text-slate-300">
          {{ t(config.descriptionKey) }}
        </UiCardDescription>
      </div>
    </UiCardHeader>

    <UiCardContent class="grid gap-5 px-2 pb-2">
      <UiAlert v-if="errorMessage" variant="destructive">
        <AlertCircleIcon class="size-4" />
        <UiAlertTitle>{{ t('pages.login.failedTitle') }}</UiAlertTitle>
        <UiAlertDescription>{{ errorMessage }}</UiAlertDescription>
      </UiAlert>

      <div class="grid gap-2">
        <UiLabel for="auth-account">
          {{ t('common.account') }}
        </UiLabel>
        <div class="relative">
          <UserRoundIcon class="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-slate-400" />
          <UiInput
            id="auth-account"
            v-model="formState.userAccount"
            type="text"
            class="h-12 rounded-xl border-slate-200 bg-slate-50/80 pl-11 shadow-none focus-visible:bg-white dark:border-slate-800 dark:bg-slate-900/70"
            :placeholder="config.defaultAccount"
          />
        </div>
      </div>

      <div class="grid gap-2">
        <UiLabel for="auth-password">
          {{ t('common.password') }}
        </UiLabel>
        <div class="relative">
          <LockKeyholeIcon class="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-slate-400" />
          <UiInput
            id="auth-password"
            v-model="formState.userPassword"
            type="password"
            class="h-12 rounded-xl border-slate-200 bg-slate-50/80 pl-11 shadow-none focus-visible:bg-white dark:border-slate-800 dark:bg-slate-900/70"
            :placeholder="t('pages.login.passwordPlaceholder')"
          />
        </div>
      </div>

      <UiButton class="group mt-2 h-12 w-full rounded-xl bg-slate-950 text-base hover:bg-slate-800 dark:bg-sky-500 dark:text-slate-950 dark:hover:bg-sky-400" @click="handleLogin">
        <UiSpinner v-if="loading" class="mr-2" />
        <template v-else>
          {{ t(config.submitKey) }}
          <ArrowRightIcon class="ml-2 size-4 transition-transform group-hover:translate-x-0.5" />
        </template>
      </UiButton>
    </UiCardContent>
  </UiCard>
</template>
