<script setup lang="ts">
import { h, reactive, ref } from 'vue'
import { NButton, NSpace, NTag, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { nextId, notifications, type NotificationItem } from '../mock/admin'

const message = useMessage()
const rows = ref<NotificationItem[]>([...notifications])
const open = ref(false)
const form = reactive<NotificationItem>({ id: 0, type: 'message', title: '', receiverType: 'all', level: 'info', status: 'draft', pinned: 0, updateTime: '' })

function openForm(row?: NotificationItem) {
  Object.assign(form, row ?? { id: 0, type: 'message', title: '', receiverType: 'all', level: 'info', status: 'draft', pinned: 0, updateTime: new Date().toLocaleString() })
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
  message.success('通知草稿已保存')
  open.value = false
}

function setStatus(row: NotificationItem, status: NotificationItem['status']) {
  row.status = status
  row.updateTime = new Date().toLocaleString()
  message.success('通知状态已更新')
}

const columns = [
  { title: '标题', key: 'title' },
  { title: '类型', key: 'type' },
  { title: '接收端', key: 'receiverType' },
  { title: '级别', key: 'level', render: (row: NotificationItem) => h(NTag, { type: row.level === 'error' ? 'error' : row.level === 'warning' ? 'warning' : 'info' }, { default: () => row.level }) },
  { title: '状态', key: 'status' },
  { title: '置顶', key: 'pinned', render: (row: NotificationItem) => row.pinned ? '是' : '否' },
  { title: '更新时间', key: 'updateTime' },
  { title: '操作', key: 'actions', render: (row: NotificationItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openForm(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', onClick: () => setStatus(row, 'published') }, { default: () => '发布' }), h(NButton, { size: 'small', onClick: () => setStatus(row, 'draft') }, { default: () => '撤回' }), h(NButton, { size: 'small', type: 'warning', ghost: true, onClick: () => setStatus(row, 'archived') }, { default: () => '归档' })] }) },
]
</script>

<template>
  <PageShell title="通知公告" description="发布后台、用户端或全站范围内的通知消息与公告。">
    <template #actions><n-button type="primary" @click="openForm()">新建通知</n-button></template>
    <n-card class="admin-card" title="通知列表"><n-data-table :columns="columns" :data="rows" /></n-card>
    <n-modal v-model:show="open" preset="card" title="通知公告" style="width: 680px">
      <n-form :model="form" label-placement="left" label-width="96">
        <n-form-item label="类型"><n-select v-model:value="form.type" :options="[{ label: '通知消息', value: 'message' }, { label: '公告', value: 'announcement' }]" /></n-form-item>
        <n-form-item label="接收端"><n-select v-model:value="form.receiverType" :options="[{ label: '全部用户', value: 'all' }, { label: '后台用户', value: 'admin' }, { label: '用户端用户', value: 'app' }]" /></n-form-item>
        <n-form-item label="标题"><n-input v-model:value="form.title" /></n-form-item>
        <n-form-item label="级别"><n-select v-model:value="form.level" :options="[{ label: '普通', value: 'info' }, { label: '警告', value: 'warning' }, { label: '严重', value: 'error' }]" /></n-form-item>
        <n-form-item label="置顶"><n-switch :value="form.pinned === 1" @update:value="(value: boolean) => form.pinned = value ? 1 : 0" /></n-form-item>
        <n-form-item label="内容"><n-input type="textarea" :rows="5" placeholder="通知正文 mock 输入区" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="open = false">取消</n-button><n-button type="primary" @click="save">保存草稿</n-button></n-space></template>
    </n-modal>
  </PageShell>
</template>
