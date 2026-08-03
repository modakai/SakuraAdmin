<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NTag, useMessage, type DataTableColumns, type TreeOption } from 'naive-ui'
import type { EntityId } from '@/shared/api/types'
import type { PermissionNode } from '@/features/auth/model'
import {
  assignPermissions,
  createRole,
  deleteRoleById,
  getPermissionTree,
  getRolePage,
  getRolePermissionIds,
  updateRole,
} from '../api'
import type { RoleItem } from '../model'

const message = useMessage()
const loading = ref(false)
const rows = ref<RoleItem[]>([])
const total = ref(0)

const query = reactive({
  page: 1,
  pageSize: 10,
  roleName: '',
})

async function loadRoles() {
  loading.value = true
  try {
    const page = await getRolePage({ ...query, roleName: query.roleName.trim() })
    rows.value = page.records
    total.value = page.totalRow
  }
  finally {
    loading.value = false
  }
}

// ---------- 新建 / 编辑 ----------
const formVisible = ref(false)
const form = reactive({ id: undefined as EntityId | undefined, roleCode: '', roleName: '', sortOrder: 0, remark: '' })
const formSaving = ref(false)

function openCreate() {
  Object.assign(form, { id: undefined, roleCode: '', roleName: '', sortOrder: 0, remark: '' })
  formVisible.value = true
}

function openEdit(row: RoleItem) {
  Object.assign(form, {
    id: row.id,
    roleCode: row.roleCode ?? '',
    roleName: row.roleName ?? '',
    sortOrder: row.sortOrder ?? 0,
    remark: row.remark ?? '',
  })
  formVisible.value = true
}

async function saveForm() {
  if (!form.roleName) {
    message.error('请输入角色名称')
    return
  }
  if (form.id === undefined && !form.roleCode) {
    message.error('请输入角色标识')
    return
  }
  formSaving.value = true
  try {
    if (form.id === undefined) {
      await createRole({ roleCode: form.roleCode, roleName: form.roleName, sortOrder: form.sortOrder, remark: form.remark })
      message.success('角色已创建')
    }
    else {
      await updateRole({ id: form.id, roleName: form.roleName, sortOrder: form.sortOrder, remark: form.remark })
      message.success('角色已更新')
    }
    formVisible.value = false
    await loadRoles()
  }
  finally {
    formSaving.value = false
  }
}

async function removeRole(row: RoleItem) {
  await deleteRoleById(row.id)
  message.success('角色已删除，关联已清理')
  await loadRoles()
}

// ---------- 分配权限 ----------
const drawerVisible = ref(false)
const treeLoading = ref(false)
const assigning = ref(false)
const treeOptions = ref<TreeOption[]>([])
const checkedKeys = ref<EntityId[]>([])
const currentRole = ref<RoleItem | null>(null)

function toTreeOption(node: PermissionNode): TreeOption {
  return {
    key: String(node.id),
    label: node.title,
    children: node.children?.map(toTreeOption),
  }
}

async function openAssign(row: RoleItem) {
  currentRole.value = row
  drawerVisible.value = true
  treeLoading.value = true
  try {
    const [tree, ids] = await Promise.all([getPermissionTree(), getRolePermissionIds(row.id)])
    treeOptions.value = tree.map(toTreeOption)
    checkedKeys.value = ids.map(String)
  }
  finally {
    treeLoading.value = false
  }
}

async function saveAssign() {
  if (!currentRole.value) {
    return
  }
  assigning.value = true
  try {
    await assignPermissions(currentRole.value.id, checkedKeys.value)
    message.success('权限已更新')
    drawerVisible.value = false
  }
  finally {
    assigning.value = false
  }
}

const columns: DataTableColumns<RoleItem> = [
  { title: '角色名称', key: 'roleName' },
  { title: '角色标识', key: 'roleCode' },
  {
    title: '类型',
    key: 'isSuperadmin',
    render: row => h(NTag, { type: row.isSuperadmin === 1 ? 'warning' : 'default', size: 'small' }, { default: () => row.isSuperadmin === 1 ? '超管' : '普通' }),
  },
  {
    title: '状态',
    key: 'status',
    render: row => h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small' }, { default: () => row.status === 1 ? '启用' : '禁用' }),
  },
  { title: '排序', key: 'sortOrder' },
  {
    title: '操作',
    key: 'actions',
    render: row => h('div', { style: 'display:flex;gap:8px' }, [
      h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
      h(NButton, { size: 'small', type: 'primary', ghost: true, onClick: () => openAssign(row) }, { default: () => '分配权限' }),
      row.isSuperadmin !== 1
        ? h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => removeRole(row) }, { default: () => '删除' })
        : undefined,
    ]),
  },
]

onMounted(loadRoles)
</script>

<template>
  <n-card title="角色管理" :bordered="false">
    <n-space class="toolbar" justify="space-between">
      <n-space align="center">
        <n-input v-model:value="query.roleName" placeholder="角色名称" clearable style="width: 220px" @keyup.enter="loadRoles" @clear="loadRoles" />
        <n-button type="primary" secondary @click="loadRoles">查询</n-button>
      </n-space>
      <n-button type="primary" v-permission="'system:role:add'" @click="openCreate">新建角色</n-button>
    </n-space>

    <n-data-table
      :columns="columns"
      :data="rows"
      :loading="loading"
      :row-key="(row: RoleItem) => String(row.id)"
      :pagination="{ page: query.page, pageSize: query.pageSize, itemCount: total, onChange: (page: number) => { query.page = page; loadRoles() } }"
    />
  </n-card>

  <n-modal v-model:show="formVisible" preset="card" :title="form.id === undefined ? '新建角色' : '编辑角色'" style="width: 480px">
    <n-form :model="form" label-placement="top">
      <n-form-item label="角色标识">
        <n-input v-model:value="form.roleCode" :disabled="form.id !== undefined" placeholder="如 ops" />
      </n-form-item>
      <n-form-item label="角色名称">
        <n-input v-model:value="form.roleName" placeholder="如 运维人员" />
      </n-form-item>
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

  <n-drawer v-model:show="drawerVisible" :width="420">
    <n-drawer-content :title="`分配权限 - ${currentRole?.roleName ?? ''}`" closable>
      <n-spin :show="treeLoading">
        <n-tree
          :data="treeOptions"
          checkable
          cascade
          default-expand-all
          v-model:checked-keys="checkedKeys"
        />
      </n-spin>
      <template #footer>
        <n-space justify="end">
          <n-button @click="drawerVisible = false">取消</n-button>
          <n-button type="primary" :loading="assigning" @click="saveAssign">保存权限</n-button>
        </n-space>
      </template>
    </n-drawer-content>
  </n-drawer>
</template>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
