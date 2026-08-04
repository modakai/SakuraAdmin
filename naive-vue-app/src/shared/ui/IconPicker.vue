<script setup lang="ts">
import { computed, ref } from 'vue'
import { NButton, NEmpty, NIcon, NInput, NPopover, NScrollbar } from 'naive-ui'
import { CloseOutline, AppsOutline, ChevronDownOutline, SearchOutline } from '@vicons/ionicons5'
import { iconCatalog, resolveIcon } from './iconCatalog'

// 图标选择器：从 @vicons/ionicons5 的 Outline 系列中选择图标，替代手输图标名。
const props = defineProps<{ value?: string, disabled?: boolean }>()
const emit = defineEmits<{ (e: 'update:value', value: string): void }>()

const show = ref(false)
const query = ref('')

const currentIcon = computed(() => resolveIcon(props.value))

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) {
    return iconCatalog
  }
  return iconCatalog.filter(item => item.name.toLowerCase().includes(q))
})

function open() {
  if (!props.disabled) {
    query.value = ''
    show.value = true
  }
}

function pick(name: string) {
  emit('update:value', name)
  show.value = false
}

function clear() {
  emit('update:value', '')
}
</script>

<template>
  <n-popover v-model:show="show" trigger="click" placement="bottom-start" style="width: 400px" :disabled="disabled">
    <template #trigger>
      <div
        class="icon-picker__trigger"
        :class="{ 'icon-picker__trigger--disabled': disabled }"
        role="button"
        tabindex="0"
        @click="open"
        @keydown.enter="open"
      >
        <n-icon class="icon-picker__preview" :component="currentIcon ?? AppsOutline" />
        <span class="icon-picker__name" :class="{ 'icon-picker__name--empty': !value }">{{ value || '选择图标' }}</span>
        <span class="icon-picker__actions">
          <n-button
            v-if="value && !disabled"
            quaternary
            circle
            size="tiny"
            title="清除图标"
            @click.stop="clear"
          >
            <template #icon><n-icon><CloseOutline /></n-icon></template>
          </n-button>
          <n-icon class="icon-picker__arrow"><ChevronDownOutline /></n-icon>
        </span>
      </div>
    </template>

    <div class="icon-picker__panel">
      <n-input v-model:value="query" placeholder="搜索图标名称（英文）" clearable>
        <template #prefix><n-icon><SearchOutline /></n-icon></template>
      </n-input>
      <n-scrollbar style="max-height: 320px; margin-top: 10px">
        <div class="icon-picker__grid">
          <button
            v-for="item in filtered"
            :key="item.name"
            class="icon-picker__cell"
            :class="{ 'icon-picker__cell--active': item.name === value }"
            :title="item.name"
            @click="pick(item.name)"
          >
            <n-icon :component="item.component" size="22" />
            <span>{{ item.name }}</span>
          </button>
        </div>
        <n-empty v-if="filtered.length === 0" description="无匹配图标" style="margin-top: 12px" />
      </n-scrollbar>
    </div>
  </n-popover>
</template>

<style scoped>
.icon-picker__trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 34px;
  padding: 4px 10px;
  border: 1px solid var(--n-border-color, rgb(224, 224, 230));
  border-radius: 3px;
  background: var(--n-color, #fff);
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

html[data-theme="dark"] .icon-picker__trigger {
  border-color: rgba(255, 255, 255, 0.09);
  background: rgba(255, 255, 255, 0.09);
}

.icon-picker__trigger:hover {
  border-color: #18a058;
}

.icon-picker__trigger--disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.icon-picker__preview {
  color: #18a058;
  font-size: 18px;
}

.icon-picker__name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
  font-size: 13px;
}

html[data-theme="dark"] .icon-picker__name {
  color: rgba(255, 255, 255, 0.82);
}

.icon-picker__name--empty {
  color: #a0a0a0;
}

.icon-picker__actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.icon-picker__arrow {
  color: #a0a0a0;
  font-size: 14px;
}

.icon-picker__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(86px, 1fr));
  gap: 6px;
}

.icon-picker__cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 4px;
  border: 1px solid transparent;
  border-radius: 4px;
  background: transparent;
  color: #555;
  font-size: 11px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
  overflow: hidden;
}

.icon-picker__cell:hover {
  background: rgba(24, 160, 88, 0.08);
  border-color: rgba(24, 160, 88, 0.25);
}

.icon-picker__cell--active {
  background: rgba(24, 160, 88, 0.12);
  border-color: #18a058;
  color: #18a058;
}

.icon-picker__cell span {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
