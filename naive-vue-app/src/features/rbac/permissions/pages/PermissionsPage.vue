<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { NButton, NTag, useMessage, type DataTableColumns, type FormInst, type FormRules, type SelectOption, type TreeSelectOption } from 'naive-ui'
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
const formRef = ref<FormInst | null>(null)
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
  visible: 1,
  remark: '',
})

function resetForm() {
  Object.assign(form, {
    id: undefined, parentId: 0, type: 'menu', title: '', permissionCode: '',
    path: '', component: '', icon: '', sortOrder: 0, status: 1, visible: 1, remark: '',
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
    status: row.status ?? 1,
    visible: row.visible ?? 1,
    remark: row.remark ?? '',
  })
  formVisible.value = true
}

// ---------- 父级选择 ----------
// 在权限树中按 id 定位节点。
function findNode(nodes: PermissionNode[], id: number): PermissionNode | undefined {
  for (const node of nodes) {
    if (Number(node.id) === id) {
      return node
    }
    const found = findNode(node.children ?? [], id)
    if (found) {
      return found
    }
  }
  return undefined
}

// 收集一棵节点的全部 id（含自身与子孙），用于编辑时排除自身子树、防止形成环。
function collectIds(nodes: PermissionNode[]): number[] {
  return nodes.flatMap(node => [Number(node.id), ...collectIds(node.children ?? [])])
}

// 把菜单节点构建为 tree-select 选项，剔除被排除的 id。
function buildMenuOptions(nodes: PermissionNode[], exclude: Set<number>): TreeSelectOption[] {
  return nodes
    .filter(node => node.type === 'menu' && !exclude.has(Number(node.id)))
    .map(node => ({
      label: node.title,
      key: Number(node.id),
      children: buildMenuOptions(node.children ?? [], exclude),
    }))
}

// 父级下拉：菜单 → 顶层 + 全部菜单节点；按钮 → 仅菜单（必须绑定菜单，无顶层）。
// 编辑时排除自身及子孙，从根源上选不出环；接口固定顶层、不显示该控件。
const parentOptions = computed<TreeSelectOption[]>(() => {
  let exclude = new Set<number>()
  if (editing.value && form.id != null) {
    const node = findNode(rows.value, Number(form.id))
    exclude = new Set(node ? collectIds([node]) : [])
  }
  const menuOptions = buildMenuOptions(rows.value, exclude)
  if (form.type === 'button') {
    return menuOptions
  }
  return [{ label: '顶层（根目录）', key: 0 }, ...menuOptions]
})

// 必填校验按类型动态变化：按钮必须绑定菜单；按钮/接口/叶子菜单必须填权限码；叶子菜单必须填组件标识。
const formRules = computed<FormRules>(() => {
  const rules: FormRules = {
    title: { required: true, message: '请输入标题', trigger: ['blur', 'input'] },
  }
  const isLeafMenu = form.type === 'menu' && Boolean(form.path)
  if (form.type === 'button') {
    rules.parentId = {
      validator: (_rule, value) => {
        if (!value || value === 0) {
          return new Error('请选择绑定的菜单')
        }
        return true
      },
      trigger: ['change', 'blur'],
    }
  }
  if (form.type === 'button' || form.type === 'api' || isLeafMenu) {
    rules.permissionCode = { required: true, message: '请输入权限码', trigger: ['blur', 'input'] }
  }
  if (isLeafMenu) {
    rules.component = { required: true, message: '请填写组件标识', trigger: ['blur', 'input'] }
  }
  return rules
})

async function saveForm() {
  // 接口固定顶层：切到接口类型时父级置 0，避免残留旧父级被后端拒绝。
  if (form.type === 'api') {
    form.parentId = 0
  }
  try {
    await formRef.value?.validate()
  }
  catch {
    message.error('请完善必填项')
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
    title: '显示',
    key: 'visible',
    width: 90,
    render: row => {
      // visible 仅对菜单有意义，按钮/接口不展示该标记。
      if (row.type !== 'menu') {
        return h(NTag, { type: 'default', size: 'small' }, { default: () => '—' })
      }
      const hidden = row.visible === 0
      return h(NTag, { type: hidden ? 'default' : 'success', size: 'small' }, { default: () => (hidden ? '已隐藏' : '显示') })
    },
  },
  {
    title: '操作',
    key: 'actions',
    width: 210,
    render: row => {
      // 仅菜单可作为父级（按钮绑定菜单、接口固定顶层），故只有菜单行提供「新增子节点」。
      const actions = row.type === 'menu'
        ? [h(NButton, { size: 'small', onClick: () => openCreate(row) }, { default: () => '新增子节点' })]
        : []
      actions.push(
        h(NButton, { size: 'small', onClick: () => openEdit(row) }, { default: () => '编辑' }),
        h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => removePermission(row) }, { default: () => '删除' }),
      )
      return h('div', { style: 'display:flex;gap:8px' }, actions)
    },
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

  <n-modal v-model:show="formVisible" preset="card" :title="editing ? '编辑权限点' : '新增权限点'" style="width: 620px">
    <n-form ref="formRef" :model="form" :rules="formRules" label-placement="top">
      <n-grid :cols="2" :x-gap="16">
        <n-form-item-gi label="类型">
          <n-select v-model:value="form.type" :options="typeOptions" />
        </n-form-item-gi>
        <n-form-item-gi path="title" label="标题">
          <n-input v-model:value="form.title" placeholder="如 用户管理" />
        </n-form-item-gi>
        <n-form-item-gi v-if="form.type === 'menu' || form.type === 'button'" path="parentId" :label="form.type === 'button' ? '绑定菜单' : '父级菜单'">
          <n-tree-select
            v-model:value="form.parentId"
            :options="parentOptions"
            :placeholder="form.type === 'button' ? '选择绑定的菜单（必须选择）' : '选择父级菜单（默认顶层）'"
            :default-expand-all="true"
          />
        </n-form-item-gi>
        <n-form-item-gi :span="form.type === 'api' ? 2 : 1" path="permissionCode" label="权限码">
          <n-input v-model:value="form.permissionCode" placeholder="如 system:user:list（目录节点可留空）" />
        </n-form-item-gi>
        <template v-if="form.type === 'menu'">
          <n-form-item-gi label="路由路径">
            <n-input v-model:value="form.path" placeholder="如 /users" />
          </n-form-item-gi>
          <n-form-item-gi path="component" label="组件标识">
            <n-input v-model:value="form.component" placeholder="如 users/UsersPage（需在组件映射表登记）" />
          </n-form-item-gi>
          <n-form-item-gi :span="2" label="图标">
            <n-input v-model:value="form.icon" placeholder="如 PeopleOutline" />
          </n-form-item-gi>
        </template>
        <n-form-item-gi v-if="form.type === 'menu'" label="显示/隐藏">
          <n-switch v-model:value="form.visible" :checked-value="1" :unchecked-value="0">
            <template #checked>显示</template>
            <template #unchecked>隐藏</template>
          </n-switch>
        </n-form-item-gi>
        <n-form-item-gi label="排序">
          <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
        </n-form-item-gi>
      </n-grid>
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
