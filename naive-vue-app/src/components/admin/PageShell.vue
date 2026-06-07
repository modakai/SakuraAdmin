<script setup lang="ts">
defineProps<{
  title: string
  description: string
}>()
</script>

<template>
  <section>
    <div class="page-shell__header">
      <div>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>
      <div class="page-shell__actions">
        <slot name="actions" />
      </div>
    </div>
    <slot />
  </section>
</template>

<style scoped>
.page-shell__header {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: -18px -18px 16px;
  padding: 16px 18px;
  border-bottom: 1px solid rgba(31, 41, 55, 0.08);
  background: rgba(246, 247, 251, 0.9);
  backdrop-filter: blur(14px);
}

h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

p {
  margin: 4px 0 0;
  color: #6b7280;
}

.page-shell__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

@media (max-width: 640px) {
  .page-shell__header {
    align-items: flex-start;
    flex-direction: column;
    margin: -12px -12px 14px;
    padding: 14px 12px;
  }
}
</style>
