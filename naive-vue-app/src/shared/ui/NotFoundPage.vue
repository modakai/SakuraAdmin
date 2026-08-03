<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/session'

const router = useRouter()
const session = useSessionStore()

async function backToLogin() {
  await session.logout()
  router.push('/login')
}
</script>

<template>
  <div class="not-found">
    <n-result status="404" title="页面不存在" description="你访问的页面不存在，或你没有访问权限">
      <template #footer>
        <n-space justify="center">
          <n-button @click="backToLogin">返回登录</n-button>
          <n-button type="primary" @click="router.push('/dashboard')">返回工作台</n-button>
        </n-space>
      </template>
    </n-result>
  </div>
</template>

<style scoped>
.not-found {
  display: grid;
  min-height: 100vh;
  place-items: center;
}
</style>
