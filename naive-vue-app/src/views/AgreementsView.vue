<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NSpace, NTag, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { createAgreement, deleteAgreementById, getAgreementPage, updateAgreement } from '../services/api'
import type { AgreementItem } from '../services/types'

const message = useMessage()
const loading = ref(false)
const rows = ref<AgreementItem[]>([])
const open = ref(false)
const form = reactive<Partial<AgreementItem>>({ id: 0, agreementType: '', title: '', content: '', status: 1, sortOrder: 10, remark: '' })

async function loadRows() {
  loading.value = true
  try {
    // 协议管理只接后台维护接口，公开协议页不在本次 naive 管理端范围内。
    const page = await getAgreementPage({ page: 1, pageSize: 50 })
    rows.value = page.records
  }
  catch (error: any) {
    rows.value = []
    message.error(error?.message || '加载协议失败')
  }
  finally {
    loading.value = false
  }
}

function openForm(row?: AgreementItem) {
  Object.assign(form, row ?? { id: 0, agreementType: '', title: '', content: '', status: 1, sortOrder: 10, remark: '' })
  open.value = true
}

async function save() {
  if (!form.agreementType || !form.title || !form.content) {
    message.error('协议类型、标题和内容不能为空')
    return
  }
  try {
    if (form.id) {
      await updateAgreement(form)
    }
    else {
      await createAgreement(form)
    }
    message.success('协议已保存')
    open.value = false
    await loadRows()
  }
  catch (error: any) {
    message.error(error?.message || '保存协议失败')
  }
}

async function remove(row: AgreementItem) {
  try {
    await deleteAgreementById(row.id)
    message.success('协议已删除')
    await loadRows()
  }
  catch (error: any) {
    message.error(error?.message || '删除协议失败')
  }
}

const columns = [
  { title: '协议类型', key: 'agreementType' },
  { title: '标题', key: 'title' },
  { title: '状态', key: 'status', render: (row: AgreementItem) => h(NTag, { type: row.status ? 'success' : 'default' }, { default: () => row.status ? '启用' : '停用' }) },
  { title: '排序', key: 'sortOrder' },
  { title: '更新时间', key: 'updateTime' },
  { title: '操作', key: 'actions', render: (row: AgreementItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openForm(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => remove(row) }, { default: () => '删除' })] }) },
]

onMounted(loadRows)
</script>

<template>
  <PageShell title="协议管理" description="维护服务协议、隐私政策等协议内容的后台记录。">
    <template #actions>
      <n-button :loading="loading" @click="loadRows">刷新</n-button>
      <n-button type="primary" @click="openForm()">新建协议</n-button>
    </template>
    <n-card class="admin-card" title="协议列表"><n-data-table :columns="columns" :data="rows" :loading="loading" /></n-card>
    <n-modal v-model:show="open" preset="card" title="协议信息" style="width: 640px">
      <n-form :model="form" label-placement="left" label-width="88">
        <n-form-item label="协议类型"><n-input v-model:value="form.agreementType" /></n-form-item>
        <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item label="排序"><n-input-number v-model:value="form.sortOrder" /></n-form-item>
        <n-form-item label="状态"><n-switch :value="form.status === 1" @update:value="(value: boolean) => form.status = value ? 1 : 0" /></n-form-item>
        <n-form-item label="内容"><n-input v-model:value="form.content" type="textarea" :rows="8" /></n-form-item>
        <n-form-item label="备注"><n-input v-model:value="form.remark" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="open = false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
    </n-modal>
  </PageShell>
</template>
