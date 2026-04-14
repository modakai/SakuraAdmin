<script setup lang="ts">
import { useI18n } from 'vue-i18n'

import Error from '@/components/custom-error.vue'

const { t } = useI18n()
</script>

<template>
  <div class="flex items-center justify-center h-screen">
    <Error
      :code="401"
      :subtitle="t('errors.unauthorized.subtitle')"
      :error="t('errors.unauthorized.description')"
    />
  </div>
</template>

<route lang="yaml">
meta:
  layout: blank
</route>
