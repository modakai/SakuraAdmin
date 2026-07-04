<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { CheckmarkCircleOutline, LockClosedOutline, PersonOutline, ShieldCheckmarkOutline } from '@vicons/ionicons5'
import { useSessionStore } from '../stores/session'

const router = useRouter()
const message = useMessage()
const session = useSessionStore()
const form = reactive({ account: 'sakura', password: '12345678' })
const loading = ref(false)
const rememberAccount = ref(true)

// 登录页只展示稳定的产品定位，避免用假的系统状态误导用户。
const highlights = [
  'Vue 3 + TypeScript + Naive UI',
  '权限、审计、通知等后台主链路',
  '面向可复用脚手架的管理端入口',
]

async function handleLogin() {
  if (!form.account || !form.password) {
    message.error('请输入账号和密码')
    return
  }
  loading.value = true
  try {
    // 登录成功后保存后端 token，后续后台接口会自动携带鉴权请求头。
    await session.login(form.account, form.password)
    message.success('已进入后台')
    router.push('/dashboard')
  }
  catch (error: any) {
    message.error(error?.message || '登录失败')
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login">
    <section class="login__shell" aria-label="Sakura Admin 登录">
      <aside class="login__intro">
        <div class="login__brand">
          <div class="login__mark">SA</div>
          <div>
            <n-tag type="success" round :bordered="false">Naive UI Admin</n-tag>
            <h1>Sakura Admin</h1>
          </div>
        </div>

        <p class="login__summary">
          一个克制、可落地的后台管理模板入口。登录页只承载身份校验，不堆叠无关看板信息。
        </p>

        <div class="login__divider" />

        <ul class="login__highlights" aria-label="模板能力">
          <li v-for="item in highlights" :key="item">
            <n-icon :component="CheckmarkCircleOutline" />
            <span>{{ item }}</span>
          </li>
        </ul>
      </aside>

      <section class="login__form-area">
        <div class="login__form-head">
          <n-icon :component="ShieldCheckmarkOutline" />
          <div>
            <h2>欢迎回来</h2>
            <p>登录后继续管理 Sakura Admin</p>
          </div>
        </div>

        <n-form :model="form" label-placement="top" class="login__form">
          <n-form-item label="账号">
            <n-input v-model:value="form.account" size="large" placeholder="请输入账号">
              <template #prefix>
                <n-icon :component="PersonOutline" />
              </template>
            </n-input>
          </n-form-item>
          <n-form-item label="密码">
            <n-input v-model:value="form.password" size="large" type="password" show-password-on="click" placeholder="请输入密码">
              <template #prefix>
                <n-icon :component="LockClosedOutline" />
              </template>
            </n-input>
          </n-form-item>

          <div class="login__options">
            <n-checkbox v-model:checked="rememberAccount">记住账号</n-checkbox>
            <span>默认演示账号：sakura</span>
          </div>

          <n-button type="primary" block size="large" :loading="loading" @click="handleLogin">
            进入后台
          </n-button>

        </n-form>
      </section>
    </section>
  </main>
</template>

<style scoped>
.login {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 40px;
  background:
    linear-gradient(135deg, rgba(24, 160, 88, 0.12), transparent 42%),
    #edf2ef;
  color: #16211c;
}

.login__shell {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(420px, 1fr);
  align-items: center;
  width: min(1180px, 100%);
  min-height: 660px;
  padding: 64px;
  gap: 68px;
  border: 1px solid rgba(24, 160, 88, 0.14);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 24px 70px rgba(32, 49, 42, 0.12);
}

.login__intro {
  display: flex;
  max-width: 520px;
  flex-direction: column;
}

.login__brand {
  display: flex;
  align-items: center;
  gap: 18px;
}

.login__mark {
  display: grid;
  width: 64px;
  height: 64px;
  place-items: center;
  border: 1px solid rgba(24, 160, 88, 0.22);
  border-radius: 18px;
  background: #ecf7ef;
  color: #087c43;
  font-weight: 800;
  letter-spacing: 0;
}

h1 {
  margin: 14px 0 0;
  font-size: 44px;
  line-height: 1.08;
  letter-spacing: 0;
}

.login__summary {
  margin: 32px 0 0;
  color: #52645c;
  font-size: 16px;
  line-height: 1.8;
}

.login__divider {
  width: 100%;
  height: 1px;
  margin: 42px 0 30px;
  background: rgba(22, 33, 28, 0.1);
}

.login__highlights {
  display: grid;
  margin: 0;
  padding: 0;
  gap: 18px;
  list-style: none;
}

.login__highlights li {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #2f4239;
  font-size: 15px;
}

.login__highlights :deep(.n-icon) {
  color: #18a058;
  font-size: 20px;
}

.login__form-area {
  width: min(100%, 520px);
  justify-self: end;
}

.login__form-head {
  display: flex;
  align-items: center;
  margin-bottom: 34px;
  gap: 16px;
}

.login__form-head > .n-icon {
  display: grid;
  width: 52px;
  height: 52px;
  place-items: center;
  border: 1px solid rgba(24, 160, 88, 0.18);
  border-radius: 16px;
  background: #eff8f1;
  color: #18a058;
  font-size: 28px;
}

h2 {
  margin: 0;
  font-size: 34px;
  line-height: 1.15;
  letter-spacing: 0;
}

.login__form-head p {
  margin: 8px 0 0;
  color: #68756f;
  font-size: 15px;
}

.login__form {
  padding: 0;
}

.login__form :deep(.n-form-item-label) {
  color: #26342e;
  font-weight: 650;
}

.login__form :deep(.n-input) {
  --n-border-radius: 8px;
}

.login__options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 2px 0 24px;
  gap: 16px;
  color: #7a857f;
  font-size: 13px;
}

.login :deep(.n-button) {
  --n-border-radius: 8px;
  min-height: 46px;
  font-weight: 700;
}

@media (max-width: 780px) {
  .login {
    padding: 20px;
  }

  .login__shell {
    min-height: auto;
    grid-template-columns: 1fr;
    padding: 32px 24px;
    gap: 38px;
  }

  .login__form-area {
    justify-self: stretch;
  }

  h1 {
    font-size: 36px;
  }

  h2 {
    font-size: 30px;
  }

  .login__options {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
}
</style>
