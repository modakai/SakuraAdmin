<script setup lang="ts">
import { LoaderCircleIcon, PlusIcon, SquarePenIcon } from '@lucide/vue'
import { useI18n } from 'vue-i18n'
import { toast } from 'vue-sonner'

import type { DictEntityId, DictTypeForm } from '@/services/types/dict.type'

import {
  useCreateDictTypeMutation,
  useGetDictTypeDetailQuery,
  useUpdateDictTypeMutation,
} from '@/services/api/dict.api'

const props = defineProps<{
  dictTypeId?: DictEntityId
}>()

const emit = defineEmits<{
  success: []
}>()

const { t } = useI18n()
const open = ref(false)
const isEdit = computed(() => !!props.dictTypeId)
const form = reactive<DictTypeForm>({
  id: undefined,
  dictCode: '',
  dictName: '',
  status: 1,
  remark: '',
})

const { data: detailData, isFetching: isFetchingDetail, refetch: refetchDetail } = useGetDictTypeDetailQuery(props.dictTypeId)
const { mutateAsync: createDictType, isPending: isCreating } = useCreateDictTypeMutation()
const { mutateAsync: updateDictType, isPending: isUpdating } = useUpdateDictTypeMutation()

const statusValue = computed({
  get: () => String(form.status),
  set: value => form.status = Number(value),
})
const isSubmitting = computed(() => isCreating.value || isUpdating.value)

watch(open, async (value) => {
  if (!value) {
    return
  }
  if (!isEdit.value) {
    resetForm()
    return
  }
  const detailResult = await refetchDetail()
  if (detailResult.data?.data) {
    // 显式回填详情，避免缓存命中时仅靠 watch 导致表单显示默认值。
    form.id = detailResult.data.data.id
    form.dictCode = detailResult.data.data.dictCode
    form.dictName = detailResult.data.data.dictName
    form.status = detailResult.data.data.status
    form.remark = detailResult.data.data.remark ?? ''
  }
})

watch(detailData, (value) => {
  if (!open.value || !value?.data) {
    return
  }
  form.id = value.data.id
  form.dictCode = value.data.dictCode
  form.dictName = value.data.dictName
  form.status = value.data.status
  form.remark = value.data.remark ?? ''
}, { immediate: true })

/**
 * 重置到默认表单值，避免新增沿用上一条编辑数据。
 */
function resetForm() {
  form.id = undefined
  form.dictCode = ''
  form.dictName = ''
  form.status = 1
  form.remark = ''
}

/**
 * 提交字典类型新增或更新请求。
 */
async function handleSubmit() {
  if (!form.dictCode.trim()) {
    toast.error(t('pages.dicts.typeForm.codeRequired'))
    return
  }
  if (!form.dictName.trim()) {
    toast.error(t('pages.dicts.typeForm.nameRequired'))
    return
  }

  try {
    const payload: DictTypeForm = {
      id: form.id,
      dictCode: form.dictCode.trim(),
      dictName: form.dictName.trim(),
      status: form.status,
      remark: form.remark?.trim() || undefined,
    }

    if (isEdit.value && payload.id) {
      await updateDictType(payload)
      toast.success(t('pages.dicts.typeUpdateSuccess'))
    }
    else {
      await createDictType(payload)
      toast.success(t('pages.dicts.typeCreateSuccess'))
    }
    open.value = false
    emit('success')
    resetForm()
  }
  catch (error: any) {
    const message = error?.data?.message ?? error?.message ?? t('pages.dicts.typeSaveFailed')
    toast.error(message)
  }
}
</script>

<template>
  <UiDialog v-model:open="open">
    <UiDialogTrigger as-child>
      <UiButton :variant="isEdit ? 'outline' : 'default'" size="sm">
        <component :is="isEdit ? SquarePenIcon : PlusIcon" class="mr-1 size-4" />
        {{ isEdit ? t('actions.edit') : t('pages.dicts.createType') }}
      </UiButton>
    </UiDialogTrigger>

    <UiDialogContent class="max-w-xl">
      <UiDialogHeader>
        <UiDialogTitle>
          {{ isEdit ? t('pages.dicts.editTypeTitle') : t('pages.dicts.createTypeTitle') }}
        </UiDialogTitle>
        <UiDialogDescription>
          {{ t('pages.dicts.typeForm.description') }}
        </UiDialogDescription>
      </UiDialogHeader>

      <div v-if="isEdit && isFetchingDetail" class="flex items-center justify-center py-10 text-sm text-muted-foreground">
        <LoaderCircleIcon class="mr-2 size-4 animate-spin" />
        {{ t('pages.dicts.loadingTypeDetail') }}
      </div>

      <div v-else class="grid gap-4 py-2">
        <div class="space-y-2">
          <UiLabel>{{ t('pages.dicts.columns.dictCode') }}</UiLabel>
          <UiInput v-model="form.dictCode" :placeholder="t('pages.dicts.typeForm.codePlaceholder')" />
        </div>

        <div class="space-y-2">
          <UiLabel>{{ t('pages.dicts.columns.dictName') }}</UiLabel>
          <UiInput v-model="form.dictName" :placeholder="t('pages.dicts.typeForm.namePlaceholder')" />
        </div>

        <div class="space-y-2">
          <UiLabel>{{ t('pages.dicts.columns.status') }}</UiLabel>
          <UiSelect v-model="statusValue">
            <UiSelectTrigger class="w-full">
              <UiSelectValue :placeholder="t('pages.dicts.typeForm.statusPlaceholder')" />
            </UiSelectTrigger>
            <UiSelectContent>
              <UiSelectItem value="1">
                {{ t('common.status.enabled') }}
              </UiSelectItem>
              <UiSelectItem value="0">
                {{ t('common.status.disabled') }}
              </UiSelectItem>
            </UiSelectContent>
          </UiSelect>
        </div>

        <div class="space-y-2">
          <UiLabel>{{ t('pages.dicts.columns.remark') }}</UiLabel>
          <UiTextarea v-model="form.remark" :placeholder="t('pages.dicts.typeForm.remarkPlaceholder')" />
        </div>
      </div>

      <UiDialogFooter>
        <UiButton variant="outline" @click="open = false">
          {{ t('actions.cancel') }}
        </UiButton>
        <UiButton :disabled="isSubmitting" @click="handleSubmit">
          <LoaderCircleIcon v-if="isSubmitting" class="mr-2 size-4 animate-spin" />
          {{ t('actions.saveChanges') }}
        </UiButton>
      </UiDialogFooter>
    </UiDialogContent>
  </UiDialog>
</template>
