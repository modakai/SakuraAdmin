<script setup lang="ts">
import { reactive } from 'vue'
import { useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { useAppearanceStore } from '../stores/appearance'
import { useSessionStore } from '../stores/session'

const message = useMessage()
const session = useSessionStore()
const appearance = useAppearanceStore()

const profile = reactive({
  name: session.user.name,
  email: session.user.email,
  phone: '138****2026',
  department: '平台运营部',
  bio: '负责后台运营、系统配置和安全审计。',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const loginLogs = [
  { time: '2026-06-07 19:40:12', ip: '127.0.0.1', location: '本机', status: '成功' },
  { time: '2026-06-06 18:20:10', ip: '10.0.0.18', location: '内网', status: '成功' },
  { time: '2026-06-05 09:31:44', ip: '203.0.113.8', location: '未知', status: '失败' },
]

function saveProfile() {
  // 本地 mock 保存，同时更新顶部展示名称。
  session.user.name = profile.name
  session.user.email = profile.email
  message.success('个人资料已保存')
}

function updatePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.error('请填写当前密码和新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次新密码不一致')
    return
  }
  Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  message.success('密码已更新，mock 环境不会真实修改后端密码')
}
</script>

<template>
  <PageShell title="个人中心" description="后台管理员的资料、安全设置、界面偏好和最近登录记录。">
    <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen">
      <n-grid-item span="1">
        <n-card class="admin-card profile-card">
          <n-avatar :size="72" color="#18a058">{{ profile.name.slice(0, 1) }}</n-avatar>
          <h2>{{ profile.name }}</h2>
          <p>{{ session.user.role }}</p>
          <n-descriptions :column="1" bordered size="small">
            <n-descriptions-item label="邮箱">{{ profile.email }}</n-descriptions-item>
            <n-descriptions-item label="部门">{{ profile.department }}</n-descriptions-item>
            <n-descriptions-item label="手机号">{{ profile.phone }}</n-descriptions-item>
          </n-descriptions>
        </n-card>
      </n-grid-item>

      <n-grid-item span="2">
        <n-tabs type="line" animated>
          <n-tab-pane name="profile" tab="资料设置">
            <n-card class="admin-card">
              <n-form :model="profile" label-placement="left" label-width="86">
                <n-form-item label="姓名"><n-input v-model:value="profile.name" /></n-form-item>
                <n-form-item label="邮箱"><n-input v-model:value="profile.email" /></n-form-item>
                <n-form-item label="手机号"><n-input v-model:value="profile.phone" /></n-form-item>
                <n-form-item label="部门"><n-input v-model:value="profile.department" /></n-form-item>
                <n-form-item label="简介"><n-input v-model:value="profile.bio" type="textarea" :rows="4" /></n-form-item>
              </n-form>
              <n-space justify="end"><n-button type="primary" @click="saveProfile">保存资料</n-button></n-space>
            </n-card>
          </n-tab-pane>

          <n-tab-pane name="security" tab="账号安全">
            <n-card class="admin-card">
              <n-form :model="passwordForm" label-placement="left" label-width="96">
                <n-form-item label="当前密码"><n-input v-model:value="passwordForm.oldPassword" type="password" show-password-on="click" /></n-form-item>
                <n-form-item label="新密码"><n-input v-model:value="passwordForm.newPassword" type="password" show-password-on="click" /></n-form-item>
                <n-form-item label="确认密码"><n-input v-model:value="passwordForm.confirmPassword" type="password" show-password-on="click" /></n-form-item>
              </n-form>
              <n-space justify="end"><n-button type="primary" @click="updatePassword">更新密码</n-button></n-space>
            </n-card>
          </n-tab-pane>

          <n-tab-pane name="appearance" tab="界面偏好">
            <n-card class="admin-card">
              <n-form label-placement="left" label-width="96">
                <n-form-item label="主题模式">
                  <n-radio-group :value="appearance.colorMode" @update:value="appearance.toggleColorMode">
                    <n-radio-button value="light">浅色</n-radio-button>
                    <n-radio-button value="dark">深色</n-radio-button>
                  </n-radio-group>
                </n-form-item>
                <n-form-item label="页面密度">
                  <n-radio-group :value="appearance.density" @update:value="(value: string | number) => appearance.setDensity(value as 'comfortable' | 'compact')">
                    <n-radio-button value="comfortable">舒适</n-radio-button>
                    <n-radio-button value="compact">紧凑</n-radio-button>
                  </n-radio-group>
                </n-form-item>
              </n-form>
            </n-card>
          </n-tab-pane>

          <n-tab-pane name="logs" tab="最近登录">
            <n-card class="admin-card">
              <n-data-table
                :columns="[
                  { title: '时间', key: 'time' },
                  { title: 'IP', key: 'ip' },
                  { title: '地点', key: 'location' },
                  { title: '状态', key: 'status' },
                ]"
                :data="loginLogs"
              />
            </n-card>
          </n-tab-pane>
        </n-tabs>
      </n-grid-item>
    </n-grid>
  </PageShell>
</template>

<style scoped>
.profile-card {
  text-align: center;
}

.profile-card h2 {
  margin: 12px 0 4px;
}

.profile-card p {
  margin: 0 0 16px;
  color: #6b7280;
}
</style>
