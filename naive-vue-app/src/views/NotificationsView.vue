<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NSpace, NTag, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { createNotification, getNotificationPage, runNotificationAction, updateNotification } from '../services/api'
import type { NotificationItem, NotificationStatus } from '../services/types'

const message = useMessage()
const loading = ref(false)
const rows = ref<NotificationItem[]>([])
const open = ref(false)
const form = reactive<Partial<NotificationItem>>({ id: 0, type: 'message', title: '', summary: '', content: '', receiverType: 'all', targetType: 'all', level: 'info', status: 'draft', pinned: 0, popup: 0 })

async function loadRows() {
  loading.value = true
  try {
    // 通知列表只展示后端真实记录，状态动作会立即调用发布/撤回/归档接口。
    const page = await getNotificationPage({ page: 1, pageSize: 50 })
    rows.value = page.records
  }
  catch (error: any) {
    rows.value = []
    message.error(error?.message || '加载通知失败')
  }
  finally {
    loading.value = false
  }
}

function openForm(row?: NotificationItem) {
  Object.assign(form, row ?? { id: 0, type: 'message', title: '', summary: '', content: '', receiverType: 'all', targetType: 'all', level: 'info', status: 'draft', pinned: 0, popup: 0 })
  open.value = true
}

async function save() {
  if (!form.title || !form.content) {
    message.error('标题和内容不能为空')
    return
  }
  try {
    if (form.id) {
      await updateNotification(form)
    }
    else {
      await createNotification(form)
    }
    message.success('通知草稿已保存')
    open.value = false
    await loadRows()
  }
  catch (error: any) {
    message.error(error?.message || '保存通知失败')
  }
}

async function setStatus(row: NotificationItem, status: NotificationStatus) {
  const action = status === 'published' ? 'publish' : status === 'revoked' || status === 'draft' ? 'revoke' : 'archive'
  try {
    await runNotificationAction(row.id, action)
    message.success('通知状态已更新')
    await loadRows()
  }
  catch (error: any) {
    message.error(error?.message || '更新通知状态失败')
  }
}

const columns = [
  { title: '标题', key: 'title' },
  { title: '类型', key: 'type' },
  { title: '接收端', key: 'receiverType' },
  { title: '级别', key: 'level', render: (row: NotificationItem) => h(NTag, { type: row.level === 'error' ? 'error' : row.level === 'warning' ? 'warning' : 'info' }, { default: () => row.level }) },
  { title: '状态', key: 'status' },
  { title: '置顶', key: 'pinned', render: (row: NotificationItem) => row.pinned ? '是' : '否' },
  { title: '更新时间', key: 'updateTime' },
  { title: '操作', key: 'actions', render: (row: NotificationItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openForm(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', onClick: () => setStatus(row, 'published') }, { default: () => '发布' }), h(NButton, { size: 'small', onClick: () => setStatus(row, 'revoked') }, { default: () => '撤回' }), h(NButton, { size: 'small', type: 'warning', ghost: true, onClick: () => setStatus(row, 'archived') }, { default: () => '归档' })] }) },
]

onMounted(loadRows)
</script>

<template>
  <PageShell title="通知公告" description="发布后台、用户端或全站范围内的通知消息与公告。">
    <template #actions>
      <n-button :loading="loading" @click="loadRows">刷新</n-button>
      <n-button type="primary" @click="openForm()">新建通知</n-button>
    </template>
    <n-card class="admin-card" title="通知列表"><n-data-table :columns="columns" :data="rows" :loading="loading" /></n-card>
    <n-modal v-model:show="open" preset="card" title="通知公告" style="width: 680px">
      <n-form :model="form" label-placement="left" label-width="96">
        <n-form-item label="类型"><n-select v-model:value="form.type" :options="[{ label: '通知消息', value: 'message' }, { label: '公告', value: 'announcement' }]" /></n-form-item>
        <n-form-item label="接收端"><n-select v-model:value="form.receiverType" :options="[{ label: '全部用户', value: 'all' }, { label: '后台用户', value: 'admin' }, { label: '用户端用户', value: 'app' }]" /></n-form-item>
        <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item label="摘要"><n-input v-model:value="form.summary" /></n-form-item>
        <n-form-item label="级别"><n-select v-model:value="form.level" :options="[{ label: '普通', value: 'info' }, { label: '警告', value: 'warning' }, { label: '严重', value: 'error' }]" /></n-form-item>
        <n-form-item label="置顶"><n-switch :value="form.pinned === 1" @update:value="(value: boolean) => form.pinned = value ? 1 : 0" /></n-form-item>
        <n-form-item label="弹窗"><n-switch :value="form.popup === 1" @update:value="(value: boolean) => form.popup = value ? 1 : 0" /></n-form-item>
        <n-form-item label="内容"><n-input v-model:value="form.content" type="textarea" :rows="5" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="open = false">取消</n-button><n-button type="primary" @click="save">保存草稿</n-button></n-space></template>
    </n-modal>
  </PageShell>
</template>
