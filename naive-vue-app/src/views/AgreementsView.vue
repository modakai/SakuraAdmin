<script setup lang="ts">
import { h, reactive, ref } from 'vue'
import { NButton, NSpace, NTag, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { agreements, nextId, type AgreementItem } from '../mock/admin'

const message = useMessage()
const rows = ref<AgreementItem[]>([...agreements])
const open = ref(false)
const form = reactive<AgreementItem>({ id: 0, agreementType: '', title: '', status: 1, sortOrder: 10, updateTime: '' })

function openForm(row?: AgreementItem) {
  // 协议管理只保留后台维护入口，不复刻公开协议用户页。
  Object.assign(form, row ?? { id: 0, agreementType: '', title: '', status: 1, sortOrder: 10, updateTime: new Date().toLocaleString() })
  open.value = true
}

function save() {
  if (form.id) {
    const index = rows.value.findIndex(item => item.id === form.id)
    rows.value[index] = { ...form, updateTime: new Date().toLocaleString() }
  }
  else {
    rows.value.unshift({ ...form, id: nextId(rows.value), updateTime: new Date().toLocaleString() })
  }
  message.success('协议已保存')
  open.value = false
}

const columns = [
  { title: '协议类型', key: 'agreementType' },
  { title: '标题', key: 'title' },
  { title: '状态', key: 'status', render: (row: AgreementItem) => h(NTag, { type: row.status ? 'success' : 'default' }, { default: () => row.status ? '启用' : '停用' }) },
  { title: '排序', key: 'sortOrder' },
  { title: '更新时间', key: 'updateTime' },
  { title: '操作', key: 'actions', render: (row: AgreementItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openForm(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => { rows.value = rows.value.filter(item => item.id !== row.id); message.success('协议已删除') } }, { default: () => '删除' })] }) },
]
</script>

<template>
  <PageShell title="协议管理" description="维护服务协议、隐私政策等协议内容的后台记录。">
    <template #actions><n-button type="primary" @click="openForm()">新建协议</n-button></template>
    <n-card class="admin-card" title="协议列表"><n-data-table :columns="columns" :data="rows" /></n-card>
    <n-modal v-model:show="open" preset="card" title="协议信息" style="width: 640px">
      <n-form :model="form" label-placement="left" label-width="88">
        <n-form-item label="协议类型"><n-input v-model:value="form.agreementType" /></n-form-item>
        <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item label="排序"><n-input-number v-model:value="form.sortOrder" /></n-form-item>
        <n-form-item label="状态"><n-switch :value="form.status === 1" @update:value="(value: boolean) => form.status = value ? 1 : 0" /></n-form-item>
        <n-form-item label="内容"><n-input type="textarea" :rows="6" placeholder="这里使用 mock 富文本内容区域，后续可替换为编辑器。" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="open = false">取消</n-button><n-button type="primary" @click="save">保存</n-button></n-space></template>
    </n-modal>
  </PageShell>
</template>
