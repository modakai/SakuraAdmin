<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { NIcon, useMessage } from 'naive-ui'
import {
  AlertCircleOutline,
  AnalyticsOutline,
  BookOutline,
  DocumentTextOutline,
  HomeOutline,
  LogOutOutline,
  MoonOutline,
  NotificationsOutline,
  PeopleOutline,
  PersonCircleOutline,
  PulseOutline,
  ServerOutline,
  SettingsOutline,
  SunnyOutline,
} from '@vicons/ionicons5'
import CommandPalette from '../components/admin/CommandPalette.vue'
import NotificationCenter from '../components/admin/NotificationCenter.vue'
import { useAppearanceStore } from '../stores/appearance'
import { useSessionStore } from '../stores/session'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const session = useSessionStore()
const appearance = useAppearanceStore()
const collapsed = ref(false)

function renderIcon(icon: unknown) {
  return () => h(NIcon, null, { default: () => h(icon as any) })
}

function menuLink(label: string, to: string) {
  return () => h(RouterLink, { to }, { default: () => label })
}

const menuOptions = [
  { label: menuLink('工作台', '/dashboard'), key: '/dashboard', icon: renderIcon(HomeOutline) },
  {
    label: '系统管理',
    key: 'system',
    icon: renderIcon(SettingsOutline),
    children: [
      { label: menuLink('用户管理', '/users'), key: '/users', icon: renderIcon(PeopleOutline) },
      { label: menuLink('在线用户', '/online-users'), key: '/online-users', icon: renderIcon(PulseOutline) },
      { label: menuLink('字典管理', '/dicts'), key: '/dicts', icon: renderIcon(BookOutline) },
      { label: menuLink('协议管理', '/agreements'), key: '/agreements', icon: renderIcon(DocumentTextOutline) },
    ],
  },
  {
    label: '运维监控',
    key: 'observability',
    icon: renderIcon(AnalyticsOutline),
    children: [
      { label: menuLink('系统状态', '/observability/system-status'), key: '/observability/system-status', icon: renderIcon(ServerOutline) },
      { label: menuLink('接口监控', '/observability/api-monitor'), key: '/observability/api-monitor', icon: renderIcon(AnalyticsOutline) },
      { label: menuLink('安全事件', '/observability/security-events'), key: '/observability/security-events', icon: renderIcon(AlertCircleOutline) },
    ],
  },
  {
    label: '系统设置',
    key: 'settings',
    icon: renderIcon(SettingsOutline),
    children: [
      { label: menuLink('通知公告', '/notifications'), key: '/notifications', icon: renderIcon(NotificationsOutline) },
      { label: menuLink('消息模板', '/notification-templates'), key: '/notification-templates', icon: renderIcon(DocumentTextOutline) },
      { label: menuLink('审计日志', '/audit-logs'), key: '/audit-logs', icon: renderIcon(AlertCircleOutline) },
      { label: menuLink('个人中心', '/profile'), key: '/profile', icon: renderIcon(PersonCircleOutline) },
    ],
  },
]

const selectedKey = computed(() => route.path)

function logout() {
  // 本地 mock 登出只清理浏览器保存的登录态。
  session.logout()
  message.success('已退出后台')
  router.push('/login')
}
</script>

<template>
  <n-layout class="admin-page" has-sider>
    <n-layout-sider
      bordered
      collapse-mode="width"
      :collapsed-width="64"
      :width="252"
      :collapsed="collapsed"
      show-trigger
      @collapse="collapsed = true"
      @expand="collapsed = false"
    >
      <div class="brand" :class="{ 'brand--collapsed': collapsed }">
        <div class="brand__mark">S</div>
        <div v-if="!collapsed">
          <strong>Sakura Admin</strong>
          <span>Naive UI 复刻版</span>
        </div>
      </div>
      <n-menu
        :collapsed="collapsed"
        :collapsed-width="64"
        :collapsed-icon-size="20"
        :options="menuOptions"
        :value="selectedKey"
        default-expand-all
      />
    </n-layout-sider>

    <n-layout>
      <n-layout-header bordered class="topbar">
        <CommandPalette />
        <div class="topbar__right">
          <n-select
            class="density-select"
            :value="appearance.density"
            :options="[
              { label: '舒适', value: 'comfortable' },
              { label: '紧凑', value: 'compact' },
            ]"
            @update:value="(value: string | number) => appearance.setDensity(value as 'comfortable' | 'compact')"
          />
          <n-button quaternary circle aria-label="切换主题" @click="appearance.toggleColorMode">
            <template #icon>
              <n-icon>
                <MoonOutline v-if="appearance.colorMode === 'light'" />
                <SunnyOutline v-else />
              </n-icon>
            </template>
          </n-button>
          <NotificationCenter />
          <n-tag type="success" round>{{ session.user.role }}</n-tag>
          <RouterLink class="topbar__user" to="/profile">{{ session.user.name }}</RouterLink>
          <n-button quaternary circle aria-label="退出登录" @click="logout">
            <template #icon><n-icon><LogOutOutline /></n-icon></template>
          </n-button>
        </div>
      </n-layout-header>

      <n-layout-content class="admin-main">
        <router-view />
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<style scoped>
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 16px;
}

.brand--collapsed {
  justify-content: center;
  padding: 0;
}

.brand__mark {
  display: grid;
  flex: 0 0 34px;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 8px;
  background: #18a058;
  color: #fff;
  font-weight: 800;
}

.brand strong,
.brand span {
  display: block;
}

.brand span {
  margin-top: 2px;
  color: #8b949e;
  font-size: 12px;
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 18px;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(14px);
}

html[data-theme="dark"] .topbar {
  background: rgba(16, 16, 20, 0.86);
}

.topbar__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.density-select {
  width: 92px;
}

.topbar__user {
  color: #374151;
  font-weight: 600;
}
</style>
