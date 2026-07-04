<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NSpace, NSwitch, useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import { createNotificationTemplate, getNotificationTemplatePage, toggleNotificationTemplate, updateNotificationTemplate } from '../api'
import type { NotificationTemplateItem } from '../model'

const message = useMessage()
const loading = ref(false)
const rows = ref<NotificationTemplateItem[]>([])
const open = ref(false)
const form = reactive<Partial<NotificationTemplateItem>>({ id: 0, templateCode: '', eventType: '', receiverType: 'app', titleTemplate: '', contentTemplate: '', variableSchema: '', enabled: 1, remark: '' })

async function loadRows() {
  loading.value = true
  try {
    // 模板列表来自后端模板分页接口，启停走独立状态接口。
    const page = await getNotificationTemplatePage({ page: 1, pageSize: 50 })
    rows.value = page.records
  }
  catch (error: any) {
    rows.value = []
    message.error(error?.message || '加载模板失败')
  }
  finally {
    loading.value = false
  }
}

function openForm(row?: NotificationTemplateItem) {
  Object.assign(form, row ?? { id: 0, templateCode: '', eventType: '', receiverType: 'app', titleTemplate: '', contentTemplate: '', variableSchema: '', enabled: 1, remark: '' })
  open.value = true
}

async function save() {
  if (!form.templateCode || !form.eventType || !form.titleTemplate || !form.contentTemplate) {
    message.error('模板编码、事件类型、标题模板和内容模板不能为空')
    return
  }
  try {
    if (form.id) {
      await updateNotificationTemplate(form)
    }
    else {
      await createNotificationTemplate(form)
    }
    message.success('消息模板已保存')
    open.value = false
    await loadRows()
  }
  catch (error: any) {
    message.error(error?.message || '保存模板失败')
  }
}

async function toggle(row: NotificationTemplateItem, value: boolean) {
  try {
    await toggleNotificationTemplate(row.id, value)
    message.success('模板状态已更新')
    await loadRows()
  }
  catch (error: any) {
    message.error(error?.message || '更新模板状态失败')
  }
}

const columns = [
  { title: '模板编码', key: 'templateCode' },
  { title: '事件类型', key: 'eventType' },
  { title: '接收端', key: 'receiverType' },
  { title: '标题模板', key: 'titleTemplate' },
  { title: '启用', key: 'enabled', render: (row: NotificationTemplateItem) => h(NSwitch, { value: row.enabled === 1, onUpdateValue: value => toggle(row, value) }) },
  { title: '操作', key: 'actions', render: (row: NotificationTemplateItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openForm(row) }, { default: () => '编辑' })] }) },
]

onMounted(loadRows)
</script>

<template>
  <PageShell title="消息模板" description="维护系统事件自动通知模板，支持变量占位符。">
    <template #actions>
      <n-button :loading="loading" @click="loadRows">刷新</n-button>
      <n-button type="primary" @click="openForm()">新建模板</n-button>
    </template>
    <n-card class="admin-card" title="模板列表"><n-data-table :columns="columns" :data="rows" :loading="loading" /></n-card>
    <n-modal v-model:show="open" preset="card" title="消息模板" style="width: 680px">
      <n-form :model="form" label-placement="left" label-width="108">
        <n-form-item label="模板编码"><n-input v-model:value="form.templateCode" :disabled="!!form.id" /></n-form-item>
        <n-form-item label="事件类型"><n-input v-model:value="form.eventType" /></n-form-item>
        <n-form-item label="接收端"><n-select v-model:value="form.receiverType" :options="[{ label: '后台用户', value: 'admin' }, { label: '用户端用户', value: 'app' }, { label: '全部用户', value: 'all' }]" /></n-form-item>
        <n-form-item label="标题模板"><n-input v-model:value="form.titleTemplate" /></n-form-item>
        <n-form-item label="内容模板"><n-input v-model:value="form.contentTemplate" type="textarea" :rows="4" /></n-form-item>
        <n-form-item label="变量定义 JSON"><n-input v-model:value="form.variableSchema" type="textarea" :rows="4" /></n-form-item>
        <n-form-item label="启用"><n-switch :value="form.enabled === 1" @update:value="value => form.enabled = value ? 1 : 0" /></n-form-item>
        <n-form-item label="备注"><n-input v-model:value="form.remark" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="open = false">取消</n-button><n-button type="primary" @click="save">保存模板</n-button></n-space></template>
    </n-modal>
  </PageShell>
</template>
