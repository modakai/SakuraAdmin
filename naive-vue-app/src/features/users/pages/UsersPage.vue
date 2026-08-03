<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NSpace, NSwitch, NTag, useDialog, useMessage, type SelectOption } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import CommonImageUpload from '@/shared/upload/CommonImageUpload.vue'
import { listAllRoles } from '@/features/rbac/roles/api'
import { assignUserRoles, createUser, deleteUserById, getUserPage, getUserRoles, resetUserPassword, updateUser } from '../api'
import type { EntityId } from '@/shared/api/types'
import type { UserForm, UserItem } from '../model'

const message = useMessage()
const dialog = useDialog()
const loading = ref(false)
const rows = ref<UserItem[]>([])
const query = reactive({ userName: '', userRole: '', status: null as null | number })
const formOpen = ref(false)
const form = reactive<UserForm>({ id: '', userAccount: '', userName: '', userRole: 'user', status: 1 })

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage: (page: number) => {
    pagination.page = page
    loadUsers()
  },
  onUpdatePageSize: (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    loadUsers()
  },
})

async function loadUsers() {
  loading.value = true
  try {
    // 用户列表直接读取后端分页接口，避免本地状态掩盖真实数据问题。
    const page = await getUserPage({
      page: pagination.page,
      pageSize: pagination.pageSize,
      userName: query.userName,
      userRole: query.userRole,
      status: query.status,
    })
    rows.value = page.records
    pagination.itemCount = page.totalRow
  }
  catch (error: any) {
    rows.value = []
    pagination.itemCount = 0
    message.error(error?.message || '加载用户失败')
  }
  finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  loadUsers()
}

function handleReset() {
  query.userName = ''
  query.userRole = ''
  query.status = null
  handleSearch()
}

function openCreate() {
  // 新建时清空编辑态，账号只在新增时允许输入。
  Object.assign(form, { id: '', userAccount: '', userName: '', userRole: 'user', status: 1, userAvatar: '', userProfile: '' })
  formOpen.value = true
}

function openEdit(row: UserItem) {
  Object.assign(form, {
    id: row.id,
    userAccount: row.userAccount ?? '',
    userName: row.userName ?? '',
    userRole: row.userRole ?? 'user',
    status: row.status ?? 1,
    userAvatar: row.userAvatar ?? '',
    userProfile: row.userProfile ?? '',
  })
  formOpen.value = true
}

async function saveUser() {
  if (!form.userAccount || !form.userName) {
    message.error('账号和昵称不能为空')
    return
  }
  try {
    if (form.id) {
      await updateUser(form)
      message.success('用户已更新')
    }
    else {
      await createUser(form)
      message.success('用户已创建')
    }
    formOpen.value = false
    await loadUsers()
  }
  catch (error: any) {
    message.error(error?.message || '保存用户失败')
  }
}

function deleteUser(row: UserItem) {
  if (row.userAccount === 'sakura') {
    message.error('内置超级管理员不能删除')
    return
  }
  dialog.warning({
    title: '删除用户',
    content: `确认删除 ${row.userAccount}？`,
    positiveText: '删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUserById(row.id)
        message.success('用户已删除')
        await loadUsers()
      }
      catch (error: any) {
        message.error(error?.message || '删除用户失败')
      }
    },
  })
}

async function resetPassword(row: UserItem) {
  try {
    await resetUserPassword(row.id)
    message.success(`${row.userAccount} 的密码已重置为 12345678`)
  }
  catch (error: any) {
    message.error(error?.message || '重置密码失败')
  }
}

async function toggleStatus(row: UserItem, value: boolean) {
  if (row.userAccount === 'sakura' && !value) {
    message.error('内置超级管理员不能停用')
    return
  }
  try {
    await updateUser({
      id: row.id,
      userName: row.userName,
      userAvatar: row.userAvatar,
      userProfile: row.userProfile,
      userRole: row.userRole,
      status: value ? 1 : 0,
    })
    message.success('用户状态已更新')
    await loadUsers()
  }
  catch (error: any) {
    message.error(error?.message || '更新状态失败')
  }
}

// ---------- 分配角色 ----------
const assignOpen = ref(false)
const assignLoading = ref(false)
const assignSaving = ref(false)
const roleOptions = ref<SelectOption[]>([])
const assignRoleKeys = ref<EntityId[]>([])
const assignUser = ref<UserItem | null>(null)

async function openAssign(row: UserItem) {
  assignUser.value = row
  assignOpen.value = true
  assignLoading.value = true
  try {
    const [roles, owned] = await Promise.all([listAllRoles(), getUserRoles(row.id)])
    roleOptions.value = roles.map(role => ({ label: `${role.roleName}（${role.roleCode}）`, value: String(role.id) }))
    assignRoleKeys.value = owned.map(String)
  }
  finally {
    assignLoading.value = false
  }
}

async function saveAssign() {
  if (!assignUser.value) {
    return
  }
  assignSaving.value = true
  try {
    await assignUserRoles(assignUser.value.id, assignRoleKeys.value)
    message.success('角色已分配，权限生效')
    assignOpen.value = false
  }
  finally {
    assignSaving.value = false
  }
}

const columns = [
  { title: '账号', key: 'userAccount' },
  { title: '昵称', key: 'userName' },
  { title: '角色', key: 'userRole', render: (row: UserItem) => h(NTag, { type: row.userRole === 'admin' ? 'success' : 'default' }, { default: () => row.userRole === 'admin' ? '管理员' : '普通用户' }) },
  { title: '状态', key: 'status', render: (row: UserItem) => h(NSwitch, { value: row.status === 1, onUpdateValue: value => toggleStatus(row, value) }) },
  { title: '创建时间', key: 'createTime' },
  { title: '更新时间', key: 'updateTime' },
  {
    title: '操作',
    key: 'actions',
    render: (row: UserItem) => h(NSpace, { justify: 'end' }, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', onClick: () => openAssign(row) }, { default: () => '分配角色' }),
        h(NButton, { size: 'small', onClick: () => resetPassword(row) }, { default: () => '重置密码' }),
        h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => deleteUser(row) }, { default: () => '删除' }),
      ],
    }),
  },
]

onMounted(loadUsers)
</script>

<template>
  <PageShell title="用户管理" description="维护后台用户、角色和启停状态。">
    <template #actions>
      <n-button :loading="loading" @click="loadUsers">刷新</n-button>
      <n-button type="primary" @click="openCreate">新建用户</n-button>
    </template>

    <n-card class="admin-card" title="筛选条件">
      <div class="filter-grid">
        <n-input v-model:value="query.userName" placeholder="账号或昵称" clearable @keyup.enter="handleSearch" />
        <n-select v-model:value="query.userRole" placeholder="全部角色" clearable :options="[{ label: '管理员', value: 'admin' }, { label: '普通用户', value: 'user' }]" />
        <n-select v-model:value="query.status" placeholder="全部状态" clearable :options="[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]" />
        <n-space>
          <n-button type="primary" @click="handleSearch">查询</n-button>
          <n-button @click="handleReset">重置</n-button>
        </n-space>
      </div>
    </n-card>

    <n-card class="admin-card" title="用户列表" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" remote />
    </n-card>

    <n-modal v-model:show="formOpen" preset="card" title="用户信息" style="width: 560px">
      <n-form :model="form" label-placement="left" label-width="88">
        <n-form-item label="账号"><n-input v-model:value="form.userAccount" :disabled="!!form.id" /></n-form-item>
        <n-form-item label="昵称"><n-input v-model:value="form.userName" /></n-form-item>
        <n-form-item label="头像">
          <CommonImageUpload
            :model-value="form.userAvatar ? [form.userAvatar] : []"
            variant="avatar"
            :max="1"
            tips="支持 jpeg、jpg、svg、png、webp，单张不超过 1MB"
            @update:model-value="value => form.userAvatar = value[0] ?? ''"
          />
        </n-form-item>
        <n-form-item label="角色"><n-select v-model:value="form.userRole" :options="[{ label: '管理员', value: 'admin' }, { label: '普通用户', value: 'user' }]" /></n-form-item>
        <n-form-item label="状态"><n-switch :value="form.status === 1" @update:value="value => form.status = value ? 1 : 0" /></n-form-item>
        <n-form-item label="简介"><n-input v-model:value="form.userProfile" type="textarea" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="formOpen = false">取消</n-button>
          <n-button type="primary" @click="saveUser">保存</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-drawer v-model:show="assignOpen" :width="420">
      <n-drawer-content :title="`分配角色 - ${assignUser?.userName ?? ''}`" closable>
        <n-spin :show="assignLoading">
          <n-select v-model:value="assignRoleKeys" multiple :options="roleOptions" placeholder="选择角色" />
          <n-text depth="3" style="display: block; margin-top: 12px">角色变更后该用户权限即时生效。</n-text>
        </n-spin>
        <template #footer>
          <n-space justify="end">
            <n-button @click="assignOpen = false">取消</n-button>
            <n-button type="primary" :loading="assignSaving" @click="saveAssign">保存</n-button>
          </n-space>
        </template>
      </n-drawer-content>
    </n-drawer>
  </PageShell>
</template>
