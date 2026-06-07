<script setup lang="ts">
import { computed, h, reactive, ref } from 'vue'
import { NButton, NSpace, NSwitch, NTag, useDialog, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { nextId, users, type UserItem } from '../mock/admin'

const message = useMessage()
const dialog = useDialog()
const rows = ref<UserItem[]>([...users])
const query = reactive({ userName: '', userRole: '', status: null as null | number })
const formOpen = ref(false)
const form = reactive<UserItem>({ id: 0, userAccount: '', userName: '', userRole: 'user', status: 1, lastLoginIp: '-', createTime: '' })

const filteredRows = computed(() => rows.value.filter((item) => {
  const matchName = !query.userName || item.userName.includes(query.userName) || item.userAccount.includes(query.userName)
  const matchRole = !query.userRole || item.userRole === query.userRole
  const matchStatus = query.status === null || item.status === query.status
  return matchName && matchRole && matchStatus
}))

function openCreate() {
  // 新建时重置表单，避免复用上一次编辑状态。
  Object.assign(form, { id: 0, userAccount: '', userName: '', userRole: 'user', status: 1, lastLoginIp: '-', createTime: new Date().toLocaleString() })
  formOpen.value = true
}

function openEdit(row: UserItem) {
  Object.assign(form, row)
  formOpen.value = true
}

function saveUser() {
  if (!form.userAccount || !form.userName) {
    message.error('账号和昵称不能为空')
    return
  }
  if (form.id) {
    const index = rows.value.findIndex(item => item.id === form.id)
    rows.value[index] = { ...form }
    message.success('用户已更新')
  }
  else {
    rows.value.unshift({ ...form, id: nextId(rows.value) })
    message.success('用户已创建')
  }
  formOpen.value = false
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
    onPositiveClick: () => {
      rows.value = rows.value.filter(item => item.id !== row.id)
      message.success('用户已删除')
    },
  })
}

function resetPassword(row: UserItem) {
  message.success(`${row.userAccount} 的密码已重置为 12345678`)
}

function toggleStatus(row: UserItem, value: boolean) {
  if (row.userAccount === 'sakura' && !value) {
    message.error('内置超级管理员不能停用')
    return
  }
  row.status = value ? 1 : 0
  message.success('用户状态已更新')
}

const columns = [
  { title: '账号', key: 'userAccount' },
  { title: '昵称', key: 'userName' },
  { title: '角色', key: 'userRole', render: (row: UserItem) => h(NTag, { type: row.userRole === 'admin' ? 'success' : 'default' }, { default: () => row.userRole === 'admin' ? '管理员' : '普通用户' }) },
  { title: '状态', key: 'status', render: (row: UserItem) => h(NSwitch, { value: row.status === 1, onUpdateValue: value => toggleStatus(row, value) }) },
  { title: '最后登录 IP', key: 'lastLoginIp' },
  { title: '创建时间', key: 'createTime' },
  {
    title: '操作',
    key: 'actions',
    render: (row: UserItem) => h(NSpace, { justify: 'end' }, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', onClick: () => resetPassword(row) }, { default: () => '重置密码' }),
        h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => deleteUser(row) }, { default: () => '删除' }),
      ],
    }),
  },
]
</script>

<template>
  <PageShell title="用户管理" description="维护后台用户、角色和启停状态。">
    <template #actions>
      <n-button type="primary" @click="openCreate">新建用户</n-button>
    </template>

    <n-card class="admin-card" title="筛选条件">
      <div class="filter-grid">
        <n-input v-model:value="query.userName" placeholder="账号或昵称" clearable />
        <n-select v-model:value="query.userRole" placeholder="全部角色" clearable :options="[{ label: '管理员', value: 'admin' }, { label: '普通用户', value: 'user' }]" />
        <n-select v-model:value="query.status" placeholder="全部状态" clearable :options="[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]" />
        <n-button @click="query.userName = ''; query.userRole = ''; query.status = null">重置</n-button>
      </div>
    </n-card>

    <n-card class="admin-card" title="用户列表" style="margin-top: 16px">
      <n-data-table :columns="columns" :data="filteredRows" :pagination="{ pageSize: 10 }" />
    </n-card>

    <n-modal v-model:show="formOpen" preset="card" title="用户信息" style="width: 560px">
      <n-form :model="form" label-placement="left" label-width="88">
        <n-form-item label="账号"><n-input v-model:value="form.userAccount" :disabled="!!form.id" /></n-form-item>
        <n-form-item label="昵称"><n-input v-model:value="form.userName" /></n-form-item>
        <n-form-item label="角色"><n-select v-model:value="form.userRole" :options="[{ label: '管理员', value: 'admin' }, { label: '普通用户', value: 'user' }]" /></n-form-item>
        <n-form-item label="状态"><n-switch :value="form.status === 1" @update:value="value => form.status = value ? 1 : 0" /></n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="formOpen = false">取消</n-button>
          <n-button type="primary" @click="saveUser">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </PageShell>
</template>
