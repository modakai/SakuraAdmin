<script setup lang="ts">
import { computed, reactive, watchEffect } from 'vue'
import { useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import CommonImageUpload from '@/shared/upload/CommonImageUpload.vue'
import { updateMyPassword, updateMyUser } from '../api'
import { useAppearanceStore } from '@/stores/appearance'
import { useSessionStore } from '@/stores/session'

const message = useMessage()
const session = useSessionStore()
const appearance = useAppearanceStore()

const profile = reactive({
  name: session.user.name,
  account: session.user.account,
  role: session.user.role,
  bio: session.user.profile,
  avatar: session.user.avatar,
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const avatarUploadModel = computed({
  get: () => profile.avatar ? [profile.avatar] : [],
  set: (urls: string[]) => {
    // 头像上传组件对外使用数组模型，个人资料接口只保存单个头像 URL。
    profile.avatar = urls[0] ?? ''
  },
})
const avatarInitial = computed(() => {
  // 头像图片不可用时使用姓名首字母兜底，避免空名称导致页面渲染异常。
  return profile.name.trim().slice(0, 1) || 'S'
})

watchEffect(() => {
  // 登录态刷新后同步到表单，避免顶部用户信息和个人中心信息不一致。
  profile.name = session.user.name
  profile.account = session.user.account
  profile.role = session.user.role
  profile.bio = session.user.profile
  profile.avatar = session.user.avatar
})

async function saveProfile() {
  try {
    await updateMyUser({
      userName: profile.name,
      userAvatar: profile.avatar,
      userProfile: profile.bio,
    })
    await session.refreshCurrentUser()
    message.success('个人资料已保存')
  }
  catch (error: any) {
    message.error(error?.message || '保存个人资料失败')
  }
}

async function updatePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    message.error('请填写当前密码和新密码')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次新密码不一致')
    return
  }
  try {
    await updateMyPassword(passwordForm.oldPassword, passwordForm.newPassword, passwordForm.confirmPassword)
    Object.assign(passwordForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
    message.success('密码已更新')
  }
  catch (error: any) {
    message.error(error?.message || '更新密码失败')
  }
}
</script>

<template>
  <PageShell title="个人中心" description="后台管理员的资料、安全设置和界面偏好。">
    <n-grid :cols="3" :x-gap="16" :y-gap="16" responsive="screen">
      <n-grid-item span="1">
        <n-card class="admin-card profile-card">
          <n-avatar v-if="profile.avatar" :size="72" :src="profile.avatar" color="#18a058" />
          <n-avatar v-else :size="72" color="#18a058">{{ avatarInitial }}</n-avatar>
          <h2>{{ profile.name }}</h2>
          <p>{{ profile.role }}</p>
          <n-descriptions :column="1" bordered size="small">
            <n-descriptions-item label="账号">{{ profile.account }}</n-descriptions-item>
            <n-descriptions-item label="角色">{{ profile.role }}</n-descriptions-item>
            <n-descriptions-item label="简介">{{ profile.bio || '-' }}</n-descriptions-item>
          </n-descriptions>
        </n-card>
      </n-grid-item>

      <n-grid-item span="2">
        <n-tabs type="line" animated>
          <n-tab-pane name="profile" tab="资料设置">
            <n-card class="admin-card">
              <n-form :model="profile" label-placement="left" label-width="86">
                <n-form-item label="姓名"><n-input v-model:value="profile.name" /></n-form-item>
                <n-form-item label="账号"><n-input v-model:value="profile.account" disabled /></n-form-item>
                <n-form-item label="头像">
                  <CommonImageUpload
                    v-model="avatarUploadModel"
                    variant="avatar"
                    :max="1"
                    tips="支持 jpeg、jpg、svg、png、webp，单张不超过 1MB"
                  />
                </n-form-item>
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
