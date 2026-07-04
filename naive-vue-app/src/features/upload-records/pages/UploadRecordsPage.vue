<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui'
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NTag, useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import { getUploadRecordPage } from '../api'
import type { UploadRecordBiz, UploadRecordItem, UploadRecordQuery, UploadRecordType } from '../model'

const message = useMessage()
const loading = ref(false)
const rows = ref<UploadRecordItem[]>([])

const query = reactive<UploadRecordQuery>({
  page: 1,
  pageSize: 10,
  userId: '',
  uploadType: '',
  biz: '',
  startTime: '',
  endTime: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50],
  showSizePicker: true,
  itemCount: 0,
  onUpdatePage: (page: number) => {
    pagination.page = page
    loadRows()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.page = 1
    pagination.pageSize = pageSize
    loadRows()
  },
})

const uploadTypeOptions = [
  { label: '图片', value: 'image' },
  { label: '通用文件', value: 'file' },
]

const bizOptions = [
  { label: '用户头像', value: 'user_avatar' },
  { label: '照片墙', value: 'photo_wall' },
  { label: '普通图片', value: 'image' },
  { label: '通用附件', value: 'attachment' },
  { label: '文档附件', value: 'document' },
  { label: '导入文件', value: 'import_file' },
]

function getUploadTypeLabel(value?: UploadRecordType | string) {
  return value === 'image' ? '图片' : '通用文件'
}

function getBizLabel(value?: UploadRecordBiz | string) {
  const labels: Record<string, string> = {
    user_avatar: '用户头像',
    photo_wall: '照片墙',
    image: '普通图片',
    attachment: '通用附件',
    document: '文档附件',
    import_file: '导入文件',
  }
  return value ? labels[value] ?? value : '-'
}

function formatFileSize(value?: number) {
  if (!value) {
    return '0 B'
  }
  const units = ['B', 'KB', 'MB', 'GB']
  let size = value
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex += 1
  }
  return `${size.toFixed(unitIndex === 0 ? 0 : 2)} ${units[unitIndex]}`
}

function formatTime(value?: string) {
  return value ? new Date(value).toLocaleString() : '-'
}

async function copyText(value: string | undefined, label: string) {
  if (!value) {
    message.warning(`${label}为空`)
    return
  }
  try {
    await navigator.clipboard.writeText(value)
    message.success(`${label}已复制`)
  }
  catch {
    message.error(`${label}复制失败`)
  }
}

function openUrl(url?: string) {
  if (!url) {
    message.warning('文件地址为空')
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

async function loadRows() {
  loading.value = true
  try {
    // 上传记录是成功上传审计表，前端只分页查询，不在这里承载删除或业务绑定动作。
    const page = await getUploadRecordPage({ ...query, page: pagination.page, pageSize: pagination.pageSize })
    rows.value = page.records
    pagination.itemCount = page.totalRow
  }
  catch (error: any) {
    rows.value = []
    pagination.itemCount = 0
    message.error(error?.message || '加载上传记录失败')
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadRows()
}

function handleReset() {
  Object.assign(query, {
    userId: '',
    uploadType: '',
    biz: '',
    startTime: '',
    endTime: '',
  })
  pagination.page = 1
  loadRows()
}

const columns: DataTableColumns<UploadRecordItem> = [
  {
    title: '文件',
    key: 'originalName',
    minWidth: 240,
    render: row => h('div', { class: 'file-cell' }, [
      h('div', { class: 'file-cell__name', title: row.originalName || '-' }, row.originalName || '-'),
      h('div', { class: 'file-cell__object', title: row.objectName || '-' }, row.objectName || '-'),
    ]),
  },
  {
    title: '类型',
    key: 'uploadType',
    width: 110,
    render: row => h(NTag, { type: row.uploadType === 'image' ? 'success' : 'info' }, { default: () => getUploadTypeLabel(row.uploadType) }),
  },
  { title: '业务', key: 'biz', width: 120, render: row => getBizLabel(row.biz) },
  { title: '用户 ID', key: 'userId', width: 110, render: row => row.userId || '-' },
  { title: '大小', key: 'fileSize', width: 110, render: row => formatFileSize(row.fileSize) },
  { title: 'Content-Type', key: 'contentType', minWidth: 160, ellipsis: { tooltip: true } },
  { title: '上传时间', key: 'createTime', width: 190, render: row => formatTime(row.createTime) },
  {
    title: '操作',
    key: 'actions',
    width: 230,
    render: row => h('div', { class: 'table-actions' }, [
      h(NButton, { size: 'small', onClick: () => openUrl(row.url) }, { default: () => '打开' }),
      h(NButton, { size: 'small', onClick: () => copyText(row.url, '文件地址') }, { default: () => '复制地址' }),
      h(NButton, { size: 'small', onClick: () => copyText(row.objectName, '对象名') }, { default: () => '复制对象名' }),
    ]),
  },
]

onMounted(loadRows)
</script>

<template>
  <PageShell title="上传记录" description="查看用户成功上传的图片和通用文件记录。">
    <template #actions>
      <n-button :loading="loading" @click="loadRows">刷新</n-button>
    </template>

    <n-card class="admin-card" title="筛选条件">
      <div class="filter-grid upload-record-filter">
        <n-input v-model:value="query.userId" placeholder="用户 ID" clearable />
        <n-select v-model:value="query.uploadType" placeholder="上传类型" clearable :options="uploadTypeOptions" />
        <n-select v-model:value="query.biz" placeholder="业务类型" clearable :options="bizOptions" />
        <n-input v-model:value="query.startTime" type="datetime-local" />
        <n-input v-model:value="query.endTime" type="datetime-local" />
        <div class="upload-record-filter__actions">
          <n-button type="primary" @click="handleSearch">查询</n-button>
          <n-button @click="handleReset">重置</n-button>
        </div>
      </div>
    </n-card>

    <n-card class="admin-card" title="成功上传记录" style="margin-top: 16px">
      <n-data-table
        :columns="columns"
        :data="rows"
        :loading="loading"
        :pagination="pagination"
        :scroll-x="1280"
        remote
      />
    </n-card>
  </PageShell>
</template>

<style scoped>
.upload-record-filter {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.upload-record-filter__actions {
  display: flex;
  gap: 8px;
}

.file-cell {
  min-width: 0;
}

.file-cell__name {
  overflow: hidden;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-cell__object {
  overflow: hidden;
  margin-top: 4px;
  color: #6b7280;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 1180px) {
  .upload-record-filter {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .upload-record-filter {
    grid-template-columns: 1fr;
  }
}
</style>
