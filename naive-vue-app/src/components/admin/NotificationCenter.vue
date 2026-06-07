<script setup lang="ts">
import { computed, ref } from 'vue'
import { NotificationsOutline } from '@vicons/ionicons5'
import { notifications } from '../../mock/admin'

const unreadIds = ref(new Set(notifications.filter(item => item.status === 'published').map(item => item.id)))
const unreadCount = computed(() => unreadIds.value.size)

function markRead(id: number) {
  // 使用 Set 复制触发 Vue 响应式更新。
  const next = new Set(unreadIds.value)
  next.delete(id)
  unreadIds.value = next
}

function markAllRead() {
  unreadIds.value = new Set()
}
</script>

<template>
  <n-popover trigger="click" placement="bottom-end" :width="360">
    <template #trigger>
      <n-badge :value="unreadCount">
        <n-button quaternary circle aria-label="打开通知中心">
          <template #icon><n-icon><NotificationsOutline /></n-icon></template>
        </n-button>
      </n-badge>
    </template>

    <div class="notice-head">
      <strong>通知中心</strong>
      <n-button text size="small" @click="markAllRead">全部已读</n-button>
    </div>
    <n-list hoverable>
      <n-list-item v-for="item in notifications" :key="item.id">
        <n-thing :title="item.title" :description="`${item.receiverType} · ${item.updateTime}`">
          <template #header-extra>
            <n-tag v-if="unreadIds.has(item.id)" size="small" type="success" @click="markRead(item.id)">未读</n-tag>
            <n-tag v-else size="small">已读</n-tag>
          </template>
        </n-thing>
      </n-list-item>
    </n-list>
  </n-popover>
</template>

<style scoped>
.notice-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
</style>
