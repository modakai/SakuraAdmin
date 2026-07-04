<script setup lang="ts">
import { AddOutline } from '@vicons/ionicons5'
import type { UploadCustomRequestOptions, UploadFileInfo } from 'naive-ui'
import { computed, ref, watch } from 'vue'
import { NIcon, useMessage } from 'naive-ui'
import { uploadFile, type UploadedFileResult } from '../../services/upload'

const props = withDefaults(defineProps<{
  modelValue?: string[]
  variant?: 'wall' | 'avatar' | 'image'
  max?: number
  maxSize?: number
  accept?: string
  action?: string
  fieldName?: string
  biz?: 'user_avatar' | 'photo_wall' | 'image'
  disabled?: boolean
  tips?: string
}>(), {
  modelValue: () => [],
  variant: 'wall',
  max: 9,
  maxSize: 5 * 1024 * 1024,
  accept: '.jpeg,.jpg,.svg,.png,.webp,image/jpeg,image/png,image/svg+xml,image/webp',
  action: '/file/image/upload',
  fieldName: 'file',
  biz: 'image',
  disabled: false,
  tips: '',
})

const emit = defineEmits<{
  'update:modelValue': [value: string[]]
  uploaded: [file: UploadedFileResult]
  error: [error: Error]
}>()

const message = useMessage()
const uploadedFiles = new Map<string, UploadedFileResult>()
const fileList = ref<UploadFileInfo[]>([])
const effectiveMax = computed(() => props.variant === 'avatar' ? 1 : props.max)
const isAvatar = computed(() => props.variant === 'avatar')

function createFileInfo(url: string): UploadFileInfo {
  // 外部 URL 回填为已完成文件，保证表单编辑场景能直接展示历史图片。
  const name = decodeURIComponent(url.split('/').at(-1) || '已上传图片')
  return {
    id: url,
    name,
    status: 'finished',
    url,
  }
}

function syncFromModel(urls: string[]) {
  const next = urls.filter(Boolean).slice(0, effectiveMax.value)
  const current = fileList.value.filter(file => file.status === 'finished').map(file => file.url).filter(Boolean)
  if (next.join('|') !== current.join('|')) {
    fileList.value = next.map(createFileInfo)
  }
}

function collectUrls(files: UploadFileInfo[]) {
  return files
    .filter(file => file.status === 'finished' && file.url)
    .map(file => file.url as string)
    .slice(0, effectiveMax.value)
}

function handleChange(options: { fileList: UploadFileInfo[] }) {
  fileList.value = options.fileList
  emit('update:modelValue', collectUrls(options.fileList))
}

function handleBeforeUpload(options: { file: UploadFileInfo, fileList: UploadFileInfo[] }) {
  const rawFile = options.file.file
  if (!rawFile) {
    return false
  }
  if (!rawFile.type.startsWith('image/')) {
    message.error('只能上传图片文件')
    return false
  }
  if (rawFile.size > props.maxSize) {
    message.error(`图片大小不能超过 ${Math.round(props.maxSize / 1024 / 1024)}MB`)
    return false
  }
  if (options.fileList.length > effectiveMax.value) {
    message.error(`最多上传 ${effectiveMax.value} 张图片`)
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
    // 后端按业务类型校验图片上传用途，头像模式默认归类为用户头像。
    data: { biz: props.variant === 'avatar' ? 'user_avatar' : props.biz },
    onProgress: options.onProgress,
  })
    .then((result) => {
      uploadedFiles.set(options.file.id, result)
      emit('uploaded', result)
      options.onFinish()
    })
    .catch((error: Error) => {
      message.error(error.message || '图片上传失败')
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
  <div :class="['common-image-upload', { 'common-image-upload--avatar': isAvatar }]">
    <n-upload
      v-model:file-list="fileList"
      list-type="image-card"
      :accept="accept"
      :max="effectiveMax"
      :multiple="effectiveMax > 1"
      :disabled="disabled"
      :custom-request="handleCustomRequest"
      :on-before-upload="handleBeforeUpload"
      :on-finish="handleFinish"
      @change="handleChange"
    >
      <div class="common-image-upload__trigger">
        <n-icon :component="AddOutline" />
        <span>{{ isAvatar ? '上传头像' : '上传图片' }}</span>
      </div>
    </n-upload>
    <p v-if="tips" class="common-image-upload__tips">{{ tips }}</p>
  </div>
</template>

<style scoped>
.common-image-upload__trigger {
  display: grid;
  place-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
}

.common-image-upload__trigger :deep(.n-icon) {
  font-size: 22px;
}

.common-image-upload__tips {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

.common-image-upload--avatar :deep(.n-upload-file-list .n-upload-file),
.common-image-upload--avatar :deep(.n-upload-trigger.n-upload-trigger--image-card) {
  width: 96px;
  height: 96px;
  border-radius: 50%;
}
</style>
