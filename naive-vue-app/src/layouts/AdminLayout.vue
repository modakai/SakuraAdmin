<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import type { MenuOption } from 'naive-ui'
import { NIcon, useMessage } from 'naive-ui'
import {
  LogOutOutline,
  MoonOutline,
  SunnyOutline,
} from '@vicons/ionicons5'
import NotificationCenter from '@/features/notifications/ui/NotificationCenter.vue'
import { navigationRegistry, type NavigationNode, type NavigationPage } from '@/app/router/navigation'
import CommandPalette from '@/shared/ui/CommandPalette.vue'
import { useAppearanceStore } from '@/stores/appearance'
import { useSessionStore } from '@/stores/session'

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

function createPageMenuOption(page: NavigationPage): MenuOption | null {
  if (page.visibleInMenu === false) {
    return null
  }

  return {
    label: menuLink(page.title, page.path),
    key: page.path,
    icon: renderIcon(page.icon),
  }
}

function createMenuOption(node: NavigationNode): MenuOption | null {
  if (node.kind === 'page') {
    return createPageMenuOption(node)
  }

  const children = node.children
    .map(createPageMenuOption)
    .filter((option): option is MenuOption => option !== null)

  // 菜单分组没有可见子页面时不渲染，避免出现空折叠菜单。
  if (children.length === 0) {
    return null
  }

  return {
    label: node.title,
    key: node.key,
    icon: renderIcon(node.icon),
    children,
  }
}

const menuOptions = computed(() => navigationRegistry
  .map(createMenuOption)
  .filter((option): option is MenuOption => option !== null))

const selectedKey = computed(() => route.path)

async function logout() {
  await session.logout()
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
