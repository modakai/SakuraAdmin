<script setup lang="ts">
import { computed, watchEffect } from 'vue'
import { darkTheme } from 'naive-ui'
import { useAppearanceStore } from './stores/appearance'

const appearance = useAppearanceStore()
const theme = computed(() => appearance.colorMode === 'dark' ? darkTheme : null)

watchEffect(() => {
  // 将主题状态同步到根节点，方便全局 CSS 调整背景和顶部栏透明色。
  document.documentElement.dataset.theme = appearance.colorMode
  document.documentElement.dataset.density = appearance.density
})
</script>

<template>
  <n-config-provider :theme="theme">
    <n-message-provider>
      <n-dialog-provider>
        <router-view />
      </n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>
