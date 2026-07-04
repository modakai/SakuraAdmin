<script setup lang="ts">
defineProps<{
  title: string
  value: string | number
  hint?: string
}>()
</script>

<template>
  <n-card class="admin-card metric-card" size="small">
    <div class="metric-card__title">{{ title }}</div>
    <div class="metric-card__value">{{ value }}</div>
    <div v-if="hint" class="metric-card__hint">{{ hint }}</div>
  </n-card>
</template>

<style scoped>
.metric-card__title {
  color: #6b7280;
  font-size: 13px;
}

.metric-card__value {
  margin-top: 8px;
  color: #111827;
  font-size: 28px;
  font-weight: 750;
}

.metric-card__hint {
  margin-top: 6px;
  color: #7c3aed;
  font-size: 12px;
}
</style>
