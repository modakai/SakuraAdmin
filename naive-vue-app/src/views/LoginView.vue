<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useSessionStore } from '../stores/session'

const router = useRouter()
const message = useMessage()
const session = useSessionStore()
const form = reactive({ account: 'sakura', password: '12345678' })

function handleLogin() {
  // 复刻后台入口，不接真实认证接口。
  session.login()
  message.success('已进入 mock 后台')
  router.push('/dashboard')
}
</script>

<template>
  <main class="login">
    <section class="login__panel">
      <div>
        <n-tag type="success" round>Naive UI Admin</n-tag>
        <h1>Sakura 后台管理</h1>
        <p>使用 Vue 3、TypeScript、Vite 和 Naive UI 复刻 shadcn-vue-app 后台主链路。</p>
      </div>
      <n-card class="login__card">
        <n-form :model="form" label-placement="top">
          <n-form-item label="账号">
            <n-input v-model:value="form.account" />
          </n-form-item>
          <n-form-item label="密码">
            <n-input v-model:value="form.password" type="password" show-password-on="click" />
          </n-form-item>
          <n-button type="primary" block size="large" @click="handleLogin">进入后台</n-button>
        </n-form>
      </n-card>
    </section>
  </main>
</template>

<style scoped>
.login {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  background:
    linear-gradient(120deg, rgba(24, 160, 88, 0.13), transparent 42%),
    #eef2f7;
}

.login__panel {
  display: grid;
  align-items: center;
  width: min(920px, 100%);
  grid-template-columns: 1fr 360px;
  gap: 36px;
}

h1 {
  margin: 16px 0 10px;
  font-size: 42px;
}

p {
  max-width: 520px;
  color: #5f6b7a;
  font-size: 16px;
}

.login__card {
  border-radius: 8px;
}

@media (max-width: 780px) {
  .login__panel {
    grid-template-columns: 1fr;
  }
}
</style>
