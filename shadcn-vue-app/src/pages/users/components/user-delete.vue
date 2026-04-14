<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import { toast } from 'vue-sonner'

import { ModalClose, ModalDescription, ModalFooter, ModalHeader, ModalTitle } from '@/components/prop-ui/modal'

import type { User } from '../data/schema'

const { user } = defineProps<{
  user: User
}>()

const emits = defineEmits<{
  (e: 'remove'): void
}>()
const { t } = useI18n()

function handleRemove() {
  toast(t('pages.users.deleteToast'), {
    description: h('pre', { class: 'mt-2 w-[340px] rounded-md bg-slate-950 p-4' }, h('code', { class: 'text-white' }, JSON.stringify(user, null, 2))),
  })

  emits('remove')
}
</script>

<template>
  <div>
    <ModalHeader>
      <ModalTitle>
        {{ t('pages.users.deleteTitle', { username: user.username }) }}
      </ModalTitle>

      <ModalDescription>
        {{ t('pages.users.deleteDescription', { id: user.id }) }}
      </ModalDescription>
    </ModalHeader>

    <ModalFooter>
      <ModalClose as-child>
        <UiButton variant="outline">
          {{ t('actions.cancel') }}
        </UiButton>
      </ModalClose>

      <ModalClose as-child>
        <UiButton variant="destructive" @click="handleRemove">
          {{ t('actions.delete') }}
        </UiButton>
      </ModalClose>
    </ModalFooter>
  </div>
</template>
