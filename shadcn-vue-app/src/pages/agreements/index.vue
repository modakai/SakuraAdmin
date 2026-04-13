<script setup lang="ts">
import { LoaderCircleIcon, RefreshCwIcon, Trash2Icon } from '@lucide/vue'
import { toast } from 'vue-sonner'

import type { AgreementQuery } from '@/services/api/agreement.api'

import { BasicPage } from '@/components/global-layout'
import {
  useDeleteAgreementMutation,
  useGetAgreementPageQuery,
  useGetAgreementTypeOptionsQuery,
} from '@/services/api/agreement.api'

import AgreementFormDialog from './components/agreement-form-dialog.vue'

/**
 * 协议列表查询条件。
 */
const query = reactive<AgreementQuery>({
  current: 1,
  pageSize: 10,
  agreementType: '',
  title: '',
  status: '',
})

const { data, isFetching, refetch } = useGetAgreementPageQuery(query)
const { data: typeOptionsData } = useGetAgreementTypeOptionsQuery()
const { mutateAsync: deleteAgreement, isPending: isDeleting } = useDeleteAgreementMutation()

/**
 * 统一读取协议列表。
 */
const agreementList = computed(() => data.value?.data?.records ?? [])
const total = computed(() => data.value?.data?.totalRow ?? 0)
const typeOptions = computed(() => typeOptionsData.value?.data ?? [])
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / query.pageSize)))
const deletingId = ref<number | null>(null)

/**
 * 用字符串承接筛选状态，提交前转换为数字。
 */
const statusFilter = computed({
  get: () => query.status === '' ? 'all' : String(query.status),
  set: (value) => {
    query.status = value === 'all' ? '' : Number(value)
  },
})

/**
 * 用字符串承接全部类型选项。
 */
const agreementTypeFilter = computed({
  get: () => query.agreementType || 'all',
  set: value => query.agreementType = value === 'all' ? '' : value,
})

/**
 * 状态文案映射。
 */
function getStatusText(status: number) {
  return status === 1 ? '启用' : '禁用'
}

/**
 * 状态样式映射。
 */
function getStatusVariant(status: number) {
  return status === 1 ? 'default' : 'secondary'
}

/**
 * 格式化时间展示。
 */
function formatTime(value?: string) {
  if (!value) {
    return '-'
  }
  return new Date(value).toLocaleString()
}

/**
 * 提交查询前重置分页。
 */
function handleSearch() {
  query.current = 1
  refetch()
}

/**
 * 重置筛选条件。
 */
function handleReset() {
  query.current = 1
  query.pageSize = 10
  query.agreementType = ''
  query.title = ''
  query.status = ''
  refetch()
}

/**
 * 切换分页。
 */
function changePage(nextPage: number) {
  query.current = Math.min(Math.max(nextPage, 1), totalPages.value)
  refetch()
}

/**
 * 删除单条协议。
 */
async function handleDelete() {
  if (!deletingId.value) {
    return
  }
  try {
    await deleteAgreement(deletingId.value)
    toast.success('协议删除成功')
    deletingId.value = null
    refetch()
  }
  catch (error: any) {
    const message = error?.data?.message ?? error?.message ?? '删除失败'
    toast.error(message)
  }
}
</script>

<template>
  <BasicPage title="协议管理" description="维护用户协议、隐私协议等富文本内容。" sticky>
    <template #actions>
      <AgreementFormDialog @success="refetch()" />
      <UiButton variant="outline" :disabled="isFetching" @click="refetch()">
        <RefreshCwIcon class="mr-1 size-4" :class="{ 'animate-spin': isFetching }" />
        刷新
      </UiButton>
    </template>

    <div class="space-y-4">
      <UiCard>
        <UiCardHeader>
          <UiCardTitle>筛选条件</UiCardTitle>
        </UiCardHeader>
        <UiCardContent class="grid gap-4 md:grid-cols-4">
          <div class="space-y-2">
            <UiLabel>协议类型</UiLabel>
            <UiSelect v-model="agreementTypeFilter">
              <UiSelectTrigger class="w-full">
                <UiSelectValue placeholder="全部类型" />
              </UiSelectTrigger>
              <UiSelectContent>
                <UiSelectItem value="all">
                  全部类型
                </UiSelectItem>
                <UiSelectItem v-for="item in typeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </UiSelectItem>
              </UiSelectContent>
            </UiSelect>
          </div>

          <div class="space-y-2">
            <UiLabel>协议标题</UiLabel>
            <UiInput v-model="query.title" placeholder="请输入协议标题" />
          </div>

          <div class="space-y-2">
            <UiLabel>状态</UiLabel>
            <UiSelect v-model="statusFilter">
              <UiSelectTrigger class="w-full">
                <UiSelectValue placeholder="全部状态" />
              </UiSelectTrigger>
              <UiSelectContent>
                <UiSelectItem value="all">
                  全部状态
                </UiSelectItem>
                <UiSelectItem value="1">
                  启用
                </UiSelectItem>
                <UiSelectItem value="0">
                  禁用
                </UiSelectItem>
              </UiSelectContent>
            </UiSelect>
          </div>

          <div class="flex items-end gap-2">
            <UiButton class="flex-1" @click="handleSearch">
              查询
            </UiButton>
            <UiButton variant="outline" class="flex-1" @click="handleReset">
              重置
            </UiButton>
          </div>
        </UiCardContent>
      </UiCard>

      <UiCard>
        <UiCardHeader class="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
          <div>
            <UiCardTitle>协议列表</UiCardTitle>
            <UiCardDescription>
              当前共 {{ total }} 条协议记录。
            </UiCardDescription>
          </div>
        </UiCardHeader>
        <UiCardContent>
          <div v-if="isFetching" class="flex items-center justify-center py-12 text-sm text-muted-foreground">
            <LoaderCircleIcon class="mr-2 size-4 animate-spin" />
            正在加载协议数据...
          </div>

          <div v-else class="overflow-x-auto rounded-lg border">
            <table class="w-full text-sm">
              <thead class="bg-muted/50">
                <tr class="border-b text-left">
                  <th class="px-4 py-3 font-medium">
                    协议标题
                  </th>
                  <th class="px-4 py-3 font-medium">
                    协议类型
                  </th>
                  <th class="px-4 py-3 font-medium">
                    状态
                  </th>
                  <th class="px-4 py-3 font-medium">
                    排序值
                  </th>
                  <th class="px-4 py-3 font-medium">
                    更新时间
                  </th>
                  <th class="px-4 py-3 font-medium text-right">
                    操作
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in agreementList" :key="item.id" class="border-b last:border-b-0">
                  <td class="px-4 py-3">
                    <div class="font-medium">
                      {{ item.title }}
                    </div>
                    <div v-if="item.remark" class="mt-1 text-xs text-muted-foreground">
                      {{ item.remark }}
                    </div>
                  </td>
                  <td class="px-4 py-3 text-muted-foreground">
                    {{ item.agreementType }}
                  </td>
                  <td class="px-4 py-3">
                    <UiBadge :variant="getStatusVariant(item.status)">
                      {{ getStatusText(item.status) }}
                    </UiBadge>
                  </td>
                  <td class="px-4 py-3 text-muted-foreground">
                    {{ item.sortOrder }}
                  </td>
                  <td class="px-4 py-3 text-muted-foreground">
                    {{ formatTime(item.updateTime) }}
                  </td>
                  <td class="px-4 py-3">
                    <div class="flex justify-end gap-2">
                      <AgreementFormDialog :agreement-id="item.id" @success="refetch()" />
                      <UiButton variant="outline" size="sm" :disabled="isDeleting" @click="deletingId = item.id">
                        <Trash2Icon class="mr-1 size-4" />
                        删除
                      </UiButton>
                    </div>
                  </td>
                </tr>
                <tr v-if="agreementList.length === 0">
                  <td colspan="6" class="px-4 py-10 text-center text-muted-foreground">
                    暂无协议数据
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="mt-4 flex items-center justify-between text-sm text-muted-foreground">
            <span>第 {{ query.current }} / {{ totalPages }} 页</span>
            <div class="flex gap-2">
              <UiButton variant="outline" size="sm" :disabled="query.current <= 1" @click="changePage(query.current - 1)">
                上一页
              </UiButton>
              <UiButton variant="outline" size="sm" :disabled="query.current >= totalPages" @click="changePage(query.current + 1)">
                下一页
              </UiButton>
            </div>
          </div>
        </UiCardContent>
      </UiCard>
    </div>

    <UiAlertDialog :open="deletingId !== null" @update:open="value => !value ? deletingId = null : undefined">
      <UiAlertDialogContent>
        <UiAlertDialogHeader>
          <UiAlertDialogTitle>确认删除协议</UiAlertDialogTitle>
          <UiAlertDialogDescription>
            删除后当前协议内容将不可恢复，请确认是否继续。
          </UiAlertDialogDescription>
        </UiAlertDialogHeader>
        <UiAlertDialogFooter>
          <UiAlertDialogCancel @click="deletingId = null">
            取消
          </UiAlertDialogCancel>
          <UiAlertDialogAction :disabled="isDeleting" @click="handleDelete">
            确认删除
          </UiAlertDialogAction>
        </UiAlertDialogFooter>
      </UiAlertDialogContent>
    </UiAlertDialog>
  </BasicPage>
</template>

<route lang="yaml">
meta:
  auth: true
</route>
