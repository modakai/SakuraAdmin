<script setup lang="ts">
import { h, reactive, ref } from 'vue'
import { NButton, NSpace, NSwitch, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { nextId, templates, type TemplateItem } from '../mock/admin'

const message = useMessage()
const rows = ref<TemplateItem[]>([...templates])
const open = ref(false)
const form = reactive<TemplateItem>({ id: 0, templateCode: '', eventType: '', receiverType: 'app', titleTemplate: '', enabled: 1 })

function openForm(row?: TemplateItem) {
  Object.assign(form, row ?? { id: 0, templateCode: 'user_disabled', eventType: 'user_disabled', receiverType: 'app', titleTemplate: '账户封禁通知', enabled: 1 })
  open.value = true
}

function save() {
  if (form.id) {
    const index = rows.value.findIndex(item => item.id === form.id)
    rows.value[index] = { ...form }
  }
  else {
    rows.value.unshift({ ...form, id: nextId(rows.value) })
  }
  message.success('消息模板已保存')
  open.value = false
}

function toggle(row: TemplateItem, value: boolean) {
  row.enabled = value ? 1 : 0
  message.success('模板状态已更新')
}

const columns = [
  { title: '模板编码', key: 'templateCode' },
  { title: '事件类型', key: 'eventType' },
  { title: '接收端', key: 'receiverType' },
  { title: '标题模板', key: 'titleTemplate' },
  { title: '启用', key: 'enabled', render: (row: TemplateItem) => h(NSwitch, { value: row.enabled === 1, onUpdateValue: value => toggle(row, value) }) },
  { title: '操作', key: 'actions', render: (row: TemplateItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openForm(row) }, { default: () => '编辑' })] }) },
]
</script>

<template>
  <PageShell title="消息模板" description="维护系统事件自动通知模板，支持变量占位符。">
    <template #actions><n-button type="primary" @click="openForm()">新建模板</n-button></template>
    <n-card class="admin-card" title="模板列表"><n-data-table :columns="columns" :data="rows" /></n-card>
    <n-modal v-model:show="open" preset="card" title="消息模板" style="width: 680px">
      <n-form :model="form" label-placement="left" label-width="108">
        <n-form-item label="模板编码"><n-input v-model:value="form.templateCode" /></n-form-item>
        <n-form-item label="事件类型"><n-input v-model:value="form.eventType" /></n-form-item>
        <n-form-item label="接收端"><n-select v-model:value="form.receiverType" :options="[{ label: '后台用户', value: 'admin' }, { label: '用户端用户', value: 'app' }, { label: '全部用户', value: 'all' }]" /></n-form-item>
        <n-form-item label="标题模板"><n-input v-model:value="form.titleTemplate" /></n-form-item>
        <n-form-item label="变量定义 JSON"><n-input type="textarea" :rows="4" value="[{&quot;name&quot;:&quot;reason&quot;,&quot;required&quot;:true}]" /></n-form-item>
        <n-form-item label="启用"><n-switch :value="form.enabled === 1" @update:value="value => form.enabled = value ? 1 : 0" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="open = false">取消</n-button><n-button type="primary" @click="save">保存模板</n-button></n-space></template>
    </n-modal>
  </PageShell>
</template>
