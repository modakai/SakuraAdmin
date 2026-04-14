<script setup lang="ts">
import { LoaderIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'

import { BasicPage } from '@/components/global-layout'

import { getColumns } from './components/columns'
import DataTable from './components/data-table.vue'
import UserCreate from './components/user-create.vue'
import UserInvite from './components/user-invite.vue'
import { users } from './data/users'

const loading = ref(false)
const { t } = useI18n()
const columns = computed(() => getColumns())

function mockLoading() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 2000)
}
</script>

<template>
  <BasicPage
    :title="t('pages.users.title')"
    :description="t('pages.users.description')"
    sticky
  >
    <template #actions>
      <UserInvite />
      <UserCreate />
      <UiButton variant="outline" @click="mockLoading">
        <LoaderIcon />{{ t('pages.users.mockLoading') }}
      </UiButton>
    </template>
    <div class="overflow-x-auto">
      <DataTable :loading :data="users" :columns="columns" />
    </div>
  </BasicPage>
</template>
