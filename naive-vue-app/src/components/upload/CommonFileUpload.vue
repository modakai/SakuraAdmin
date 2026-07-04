<script setup lang="ts">
import { AttachOutline } from '@vicons/ionicons5'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { uploadFile, type UploadedFileResult } from '../../services/upload'
import type { UploadedFileValue } from './types'

const props = withDefaults(defineProps<{
  modelValue?: UploadedFileValue[]
  max?: number
  maxSize?: number
  accept?: string
  action?: string
  fieldName?: string
  biz?: 'attachment' | 'document' | 'import_file'
  disabled?: boolean
  drag?: boolean
  tips?: string
}>(), {
  modelValue: () => [],
  max: 5,
  maxSize: 20 * 1024 * 1024,
  accept: '.pdf,.doc,.docx,.xls,.xlsx,.jpeg,.jpg,.png,.webp,.svg,image/*,application/pdf',
  action: '/file/upload',
  fieldName: 'file',
  biz: 'attachment',
  disabled: false,
  drag: true,
  tips: '支持 pdf、doc、docx、xls、xlsx 和常见图片格式。',
})

const emit = defineEmits<{
  'update:modelValue': [value: UploadedFileValue[]]
  uploaded: [file: UploadedFileResult]
  error: [error: Error]
}>()

const message = useMessage()
const uploadedFiles = new Map<string, UploadedFileResult>()
const fileList = ref<UploadFileInfo[]>([])

function createFileInfo(file: UploadedFileValue): UploadFileInfo {
  // 外部值只保存业务需要的文件信息，组件展示时转换为 Naive UI 文件项。
  return {
    id: file.url,
    name: file.name || decodeURIComponent(file.url.split('/').at(-1) || '已上传文件'),
    status: 'finished',
    url: file.url,
  }
}

function syncFromModel(files: UploadedFileValue[]) {
  const next = files.filter(file => file.url).slice(0, props.max)
  const current = fileList.value.filter(file => file.status === 'finished').map(file => file.url).filter(Boolean)
  if (next.map(file => file.url).join('|') !== current.join('|')) {
    fileList.value = next.map(createFileInfo)
  }
}

function collectFiles(files: UploadFileInfo[]) {
  return files
    .filter(file => file.status === 'finished' && file.url)
    .map((file) => {
      const uploaded = uploadedFiles.get(file.id)
      return {
        url: file.url as string,
        name: uploaded?.name || file.name,
        size: uploaded?.size,
        type: uploaded?.type,
      }
    })
    .slice(0, props.max)
}

function handleChange(options: { fileList: UploadFileInfo[] }) {
  fileList.value = options.fileList
  emit('update:modelValue', collectFiles(options.fileList))
}

function handleBeforeUpload(options: { file: UploadFileInfo, fileList: UploadFileInfo[] }) {
  const rawFile = options.file.file
  if (!rawFile) {
    return false
  }
  if (rawFile.size > props.maxSize) {
    message.error(`文件大小不能超过 ${Math.round(props.maxSize / 1024 / 1024)}MB`)
    return false
  }
  if (options.fileList.length > props.max) {
    message.error(`最多上传 ${props.max} 个文件`)
    return false
  }
  return true
}

function handleCustomRequest(options: UploadCustomRequestOptions) {
  const rawFile = options.file.file
  if (!rawFile) {
    options.onError()
    return
  }

  uploadFile(rawFile, {
    action: props.action,
    fieldName: props.fieldName,
    // 通用文件上传必须声明业务类型，默认作为普通附件记录。
    data: { biz: props.biz },
    onProgress: options.onProgress,
  })
    .then((result) => {
      uploadedFiles.set(options.file.id, result)
      emit('uploaded', result)
      options.onFinish()
    })
    .catch((error: Error) => {
      message.error(error.message || '文件上传失败')
      emit('error', error)
      options.onError()
    })
}

function handleFinish(options: { file: UploadFileInfo }) {
  const uploaded = uploadedFiles.get(options.file.id)
  if (!uploaded) {
    return options.file
  }
  return {
    ...options.file,
    name: uploaded.name,
    url: uploaded.url,
    status: 'finished' as const,
  }
}

watch(() => props.modelValue, value => syncFromModel(value), { immediate: true, deep: true })
</script>

<template>
  <div class="common-file-upload">
    <n-upload
      v-model:file-list="fileList"
      :accept="accept"
      :max="max"
      :multiple="max > 1"
      :disabled="disabled"
      :custom-request="handleCustomRequest"
      :on-before-upload="handleBeforeUpload"
      :on-finish="handleFinish"
      show-download-button
      @change="handleChange"
    >
      <n-upload-dragger v-if="drag">
        <div class="common-file-upload__dragger">
          <n-icon :component="AttachOutline" />
          <strong>点击或拖拽文件上传</strong>
          <span>{{ tips }}</span>
        </div>
      </n-upload-dragger>
      <n-button v-else>选择文件</n-button>
    </n-upload>
  </div>
</template>

<style scoped>
.common-file-upload__dragger {
  display: grid;
  place-items: center;
  gap: 8px;
  padding: 18px 10px;
  color: #475569;
}

.common-file-upload__dragger :deep(.n-icon) {
  color: #18a058;
  font-size: 28px;
}

.common-file-upload__dragger span {
  color: #64748b;
  font-size: 12px;
}
</style>
