<script setup lang="ts">
import { MenuIcon, ShieldCheckIcon } from '@lucide/vue'

import { useAuth } from '@/composables/use-auth'
import { userNavItems } from '@/constants/user-portal'

const route = useRoute()
const { hasAdminAccess, isLogin, logout, session } = useAuth()

// 当前路由用于导航高亮。
const activePath = computed(() => route.path)
</script>

<template>
  <div class="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(14,165,233,0.08),_transparent_28%),linear-gradient(180deg,_rgba(255,255,255,1),_rgba(248,250,252,1))] dark:bg-[radial-gradient(circle_at_top,_rgba(56,189,248,0.12),_transparent_25%),linear-gradient(180deg,_rgba(2,8,23,1),_rgba(15,23,42,1))]">
    <header class="sticky top-0 z-40 border-b bg-background/85 backdrop-blur">
      <div class="container mx-auto flex h-16 items-center gap-4 px-4">
        <RouterLink to="/" class="font-semibold tracking-tight">
          Question Forge
        </RouterLink>

        <nav class="hidden items-center gap-2 md:flex">
          <RouterLink
            v-for="item in userNavItems"
            :key="item.to"
            :to="item.to"
            class="rounded-md px-3 py-2 text-sm transition-colors"
            :class="activePath === item.to ? 'bg-secondary text-foreground' : 'text-muted-foreground hover:bg-accent hover:text-foreground'"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="ml-auto hidden items-center gap-2 md:flex">
          <UiButton
            v-if="hasAdminAccess"
            variant="outline"
            size="sm"
            @click="$router.push('/dashboard')"
          >
            <ShieldCheckIcon class="mr-1 size-4" />
            进入后台
          </UiButton>

          <template v-if="isLogin">
            <UiButton variant="ghost" size="sm" @click="$router.push('/profile')">
              {{ session.user?.name }}
            </UiButton>
            <UiButton variant="outline" size="sm" @click="logout">
              退出
            </UiButton>
          </template>

          <template v-else>
            <UiButton variant="ghost" size="sm" @click="$router.push('/auth/sign-in')">
              登录
            </UiButton>
            <UiButton variant="outline" size="sm" @click="$router.push('/auth/sign-in')">
              统一登录
            </UiButton>
          </template>
        </div>

        <UiDropdownMenu>
          <UiDropdownMenuTrigger as-child class="md:hidden">
            <UiButton variant="outline" size="icon">
              <MenuIcon class="size-4" />
            </UiButton>
          </UiDropdownMenuTrigger>
          <UiDropdownMenuContent align="end" class="w-56">
            <UiDropdownMenuItem
              v-for="item in userNavItems"
              :key="item.to"
              @click="$router.push(item.to)"
            >
              {{ item.label }}
            </UiDropdownMenuItem>
            <UiDropdownMenuSeparator />
            <UiDropdownMenuItem v-if="hasAdminAccess" @click="$router.push('/dashboard')">
              进入后台
            </UiDropdownMenuItem>
            <UiDropdownMenuItem v-if="isLogin" @click="logout">
              退出登录
            </UiDropdownMenuItem>
            <UiDropdownMenuItem v-else @click="$router.push('/auth/sign-in')">
              用户登录
            </UiDropdownMenuItem>
            <UiDropdownMenuItem v-if="!isLogin" @click="$router.push('/auth/sign-in')">
              统一登录
            </UiDropdownMenuItem>
          </UiDropdownMenuContent>
        </UiDropdownMenu>
      </div>
    </header>

    <main class="container mx-auto px-4 py-8">
      <router-view />
    </main>

    <footer class="border-t bg-background/70">
      <div class="container mx-auto flex flex-col gap-2 px-4 py-6 text-sm text-muted-foreground md:flex-row md:items-center md:justify-between">
        <span>Question Forge 用户端模板</span>
        <span>与后台共用认证接口，按角色决定后台访问权限。</span>
      </div>
    </footer>
  </div>
</template>
