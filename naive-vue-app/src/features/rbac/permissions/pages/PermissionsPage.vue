<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NTag, useMessage, type DataTableColumns, type SelectOption } from 'naive-ui'
import type { EntityId } from '@/shared/api/types'
import type { PermissionNode } from '@/features/auth/model'
import { createPermission, deletePermissionById, getPermissionTree, updatePermission } from '../api'

const message = useMessage()
const loading = ref(false)
const rows = ref<PermissionNode[]>([])

async function loadTree() {
  loading.value = true
  try {
    rows.value = await getPermissionTree()
  }
  finally {
    loading.value = false
  }
}

const typeOptions: SelectOption[] = [
  { label: '菜单', value: 'menu' },
  { label: '按钮', value: 'button' },
  { label: '接口', value: 'api' },
]

const typeTagMap: Record<string, { type: 'info' | 'success' | 'warning', label: string }> = {
  menu: { type: 'info', label: '菜单' },
  button: { type: 'success', label: '按钮' },
  api: { type: 'warning', label: '接口' },
}

// ---------- 新建 / 编辑 ----------
const formVisible = ref(false)
const formSaving = ref(false)
const editing = ref(false)
const form = reactive({
  id: undefined as EntityId | undefined,
  parentId: 0,
  type: 'menu' as 'menu' | 'button' | 'api',
  title: '',
  permissionCode: '',
  path: '',
  component: '',
  icon: '',
  sortOrder: 0,
  status: 1,
  remark: '',
})

function resetForm() {
  Object.assign(form, {
    id: undefined, parentId: 0, type: 'menu', title: '', permissionCode: '',
    path: '', component: '', icon: '', sortOrder: 0, status: 1, remark: '',
  })
}

function openCreate(parent: PermissionNode | null) {
  editing.value = false
  resetForm()
  form.parentId = parent ? Number(parent.id) : 0
  formVisible.value = true
}

function openEdit(row: PermissionNode) {
  editing.value = true
  Object.assign(form, {
    id: row.id,
    parentId: Number(row.parentId),
    type: row.type,
    title: row.title,
    permissionCode: row.permissionCode ?? '',
    path: row.path ?? '',
    component: row.component ?? '',
    icon: row.icon ?? '',
    sortOrder: row.sortOrder ?? 0,
    status: 1,
    remark: '',
  })
  formVisible.value = true
}

async function saveForm() {
  if (!form.title) {
    message.error('请输入标题')
    return
  }
  formSaving.value = true
  try {
    if (editing.value) {
      await updatePermission({ ...form })
      message.success('权限点已更新')
    }
    else {
      await createPermission({ ...form })
      message.success('权限点已创建')
    }
    formVisible.value = false
    await loadTree()
  }
  finally {
    formSaving.value = false
  }
}

async function removePermission(row: PermissionNode) {
  await deletePermissionById(row.id)
  message.success('权限点已删除')
  await loadTree()
}

const columns: DataTableColumns<PermissionNode> = [
  {
    title: '标题',
    key: 'title',
    width: 220,
  },
  {
    title: '类型',
    key: 'type',
    width: 90,
    render: row => {
      const conf = typeTagMap[row.type] ?? { type: 'default' as const, label: row.type }
      return h(NTag, { type: conf.type, size: 'small' }, { default: () => conf.label })
    },
  },
  { title: '权限码', key: 'permissionCode', render: row => row.permissionCode || '-' },
  { title: '路径', key: 'path', render: row => row.path || '-' },
  { title: '排序', key: 'sortOrder', width: 70 },
  {
    title: '操作',
    key: 'actions',
    width: 210,
    render: row => h('div', { style: 'display:flex;gap:8px' }, [
      h(NButton, { size: 'small', onClick: () => openCreate(row) }, { default: () => '新增子节点' }),
      h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
      h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => removePermission(row) }, { default: () => '删除' }),
    ]),
  },
]

onMounted(loadTree)
</script>

<template>
  <n-card title="权限管理" :bordered="false">
    <n-space class="toolbar" justify="space-between">
      <n-text depth="3">维护菜单、按钮、接口三类权限点树；菜单由登录后的权限点树驱动显示。</n-text>
      <n-button type="primary" v-permission="'system:permission:add'" @click="openCreate(null)">新增权限点</n-button>
    </n-space>

    <n-data-table
      :columns="columns"
      :data="rows"
      :loading="loading"
      :row-key="(row: PermissionNode) => String(row.id)"
      default-expand-all
    />
  </n-card>

  <n-modal v-model:show="formVisible" preset="card" :title="editing ? '编辑权限点' : '新增权限点'" style="width: 520px">
    <n-form :model="form" label-placement="top">
      <n-form-item label="类型">
        <n-select v-model:value="form.type" :options="typeOptions" />
      </n-form-item>
      <n-form-item label="标题">
        <n-input v-model:value="form.title" placeholder="如 用户管理" />
      </n-form-item>
      <n-form-item label="权限码">
        <n-input v-model:value="form.permissionCode" placeholder="如 system:user:list（目录节点可留空）" />
      </n-form-item>
      <template v-if="form.type === 'menu'">
        <n-form-item label="路由路径">
          <n-input v-model:value="form.path" placeholder="如 /users" />
        </n-form-item>
        <n-form-item label="组件标识">
          <n-input v-model:value="form.component" placeholder="如 users/UsersPage（需在组件映射表登记）" />
        </n-form-item>
        <n-form-item label="图标">
          <n-input v-model:value="form.icon" placeholder="如 PeopleOutline" />
        </n-form-item>
      </template>
      <n-form-item label="排序">
        <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
      </n-form-item>
      <n-form-item label="备注">
        <n-input v-model:value="form.remark" type="textarea" :rows="2" />
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="formVisible = false">取消</n-button>
        <n-button type="primary" :loading="formSaving" @click="saveForm">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
