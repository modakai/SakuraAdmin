<script setup lang="ts">
import { computed, h, reactive, ref } from 'vue'
import { NButton, NSpace, NTag, useMessage } from 'naive-ui'
import PageShell from '../components/admin/PageShell.vue'
import { dictItems, dictTypes, nextId, type DictItemItem, type DictTypeItem } from '../mock/admin'

const message = useMessage()
const types = ref<DictTypeItem[]>([...dictTypes])
const items = ref<DictItemItem[]>([...dictItems])
const selectedTypeId = ref(types.value[0]?.id ?? 0)
const typeFormOpen = ref(false)
const itemFormOpen = ref(false)
const typeForm = reactive<DictTypeItem>({ id: 0, dictCode: '', dictName: '', status: 1, remark: '' })
const itemForm = reactive<DictItemItem>({ id: 0, dictTypeId: selectedTypeId.value, dictLabel: '', dictValue: '', status: 1 })
const typeQuery = reactive({ keyword: '', status: null as null | number })
const itemQuery = reactive({ keyword: '', status: null as null | number })

const filteredTypes = computed(() => types.value.filter((item) => {
  const keyword = typeQuery.keyword.trim()
  const matchKeyword = !keyword || item.dictCode.includes(keyword) || item.dictName.includes(keyword) || item.remark.includes(keyword)
  const matchStatus = typeQuery.status === null || item.status === typeQuery.status
  return matchKeyword && matchStatus
}))
const selectedType = computed(() => types.value.find(item => item.id === selectedTypeId.value) ?? null)
const selectedItems = computed(() => items.value.filter((item) => {
  const keyword = itemQuery.keyword.trim()
  const matchType = item.dictTypeId === selectedTypeId.value
  const matchKeyword = !keyword || item.dictLabel.includes(keyword) || item.dictValue.includes(keyword)
  const matchStatus = itemQuery.status === null || item.status === itemQuery.status
  return matchType && matchKeyword && matchStatus
}))
const enabledTypeCount = computed(() => types.value.filter(item => item.status === 1).length)
const enabledItemCount = computed(() => items.value.filter(item => item.status === 1).length)

function openTypeForm(row?: DictTypeItem) {
  // 字典类型和字典项分开编辑，贴近源项目左右分栏结构。
  Object.assign(typeForm, row ?? { id: 0, dictCode: '', dictName: '', status: 1, remark: '' })
  typeFormOpen.value = true
}

function saveType() {
  if (typeForm.id) {
    const index = types.value.findIndex(item => item.id === typeForm.id)
    types.value[index] = { ...typeForm }
  }
  else {
    types.value.push({ ...typeForm, id: nextId(types.value) })
  }
  message.success('字典类型已保存')
  typeFormOpen.value = false
}

function openItemForm(row?: DictItemItem) {
  if (!selectedTypeId.value) {
    message.error('请先选择字典类型')
    return
  }
  Object.assign(itemForm, row ?? { id: 0, dictTypeId: selectedTypeId.value, dictLabel: '', dictValue: '', status: 1 })
  itemFormOpen.value = true
}

function saveItem() {
  if (itemForm.id) {
    const index = items.value.findIndex(item => item.id === itemForm.id)
    items.value[index] = { ...itemForm }
  }
  else {
    items.value.push({ ...itemForm, id: nextId(items.value) })
  }
  message.success('字典项已保存')
  itemFormOpen.value = false
}

function deleteType(row: DictTypeItem) {
  types.value = types.value.filter(item => item.id !== row.id)
  items.value = items.value.filter(item => item.dictTypeId !== row.id)
  selectedTypeId.value = types.value[0]?.id ?? 0
  message.success('字典类型已删除')
}

function deleteItem(row: DictItemItem) {
  items.value = items.value.filter(item => item.id !== row.id)
  message.success('字典项已删除')
}

function resetTypeQuery() {
  typeQuery.keyword = ''
  typeQuery.status = null
}

function resetItemQuery() {
  itemQuery.keyword = ''
  itemQuery.status = null
}

const typeColumns = [
  { title: '编码', key: 'dictCode' },
  { title: '名称', key: 'dictName' },
  { title: '状态', key: 'status', render: (row: DictTypeItem) => h(NTag, { type: row.status ? 'success' : 'default' }, { default: () => row.status ? '启用' : '停用' }) },
  { title: '备注', key: 'remark' },
  { title: '操作', key: 'actions', render: (row: DictTypeItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openTypeForm(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => deleteType(row) }, { default: () => '删除' })] }) },
]
const itemColumns = [
  { title: '标签', key: 'dictLabel' },
  { title: '值', key: 'dictValue' },
  { title: '状态', key: 'status', render: (row: DictItemItem) => h(NTag, { type: row.status ? 'success' : 'default' }, { default: () => row.status ? '启用' : '停用' }) },
  { title: '操作', key: 'actions', render: (row: DictItemItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openItemForm(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => deleteItem(row) }, { default: () => '删除' })] }) },
]
</script>

<template>
  <PageShell title="字典管理" description="维护字典类型和字典项，复刻源项目双列表管理方式。">
    <template #actions>
      <n-button type="primary" @click="openTypeForm()">新建字典类型</n-button>
      <n-button :disabled="!selectedTypeId" @click="openItemForm()">新建字典项</n-button>
    </template>

    <div class="dict-summary">
      <n-card class="admin-card" size="small">
        <n-statistic label="字典类型" :value="types.length" />
        <template #footer>启用 {{ enabledTypeCount }} 个</template>
      </n-card>
      <n-card class="admin-card" size="small">
        <n-statistic label="字典项" :value="items.length" />
        <template #footer>启用 {{ enabledItemCount }} 个</template>
      </n-card>
      <n-card class="admin-card" size="small">
        <n-statistic label="当前类型" :value="selectedType?.dictName ?? '-'" />
        <template #footer>{{ selectedType?.dictCode ?? '请选择类型' }}</template>
      </n-card>
    </div>

    <div class="dict-workspace">
      <n-card class="admin-card dict-card" title="字典类型">
        <div class="dict-toolbar">
          <n-input v-model:value="typeQuery.keyword" placeholder="搜索编码、名称或备注" clearable />
          <n-select
            v-model:value="typeQuery.status"
            clearable
            placeholder="状态"
            :options="[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]"
          />
          <n-button @click="resetTypeQuery">重置</n-button>
        </div>
        <n-data-table
          :columns="typeColumns"
          :data="filteredTypes"
          :row-key="(row: DictTypeItem) => row.id"
          :row-props="(row: DictTypeItem) => ({
            class: row.id === selectedTypeId ? 'dict-row dict-row--active' : 'dict-row',
            onClick: () => selectedTypeId = row.id,
          })"
          :pagination="{ pageSize: 6 }"
        />
      </n-card>

      <n-card class="admin-card dict-card" title="字典项">
        <template #header-extra>
          <n-tag v-if="selectedType" type="success">{{ selectedType.dictName }}</n-tag>
        </template>
        <div class="selected-type">
          <div>
            <strong>{{ selectedType?.dictName ?? '未选择字典类型' }}</strong>
            <p>{{ selectedType?.remark ?? '请选择左侧字典类型后维护字典项。' }}</p>
          </div>
          <n-space>
            <n-tag>{{ selectedType?.dictCode ?? '-' }}</n-tag>
            <n-tag :type="selectedType?.status ? 'success' : 'default'">{{ selectedType?.status ? '启用' : '停用' }}</n-tag>
          </n-space>
        </div>
        <div class="dict-toolbar">
          <n-input v-model:value="itemQuery.keyword" placeholder="搜索标签或值" clearable />
          <n-select
            v-model:value="itemQuery.status"
            clearable
            placeholder="状态"
            :options="[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]"
          />
          <n-button @click="resetItemQuery">重置</n-button>
        </div>
        <n-data-table :columns="itemColumns" :data="selectedItems" :pagination="{ pageSize: 6 }" />
      </n-card>
    </div>

    <n-modal v-model:show="typeFormOpen" preset="card" title="字典类型" style="width: 520px">
      <n-form :model="typeForm" label-placement="left" label-width="88">
        <n-form-item label="编码"><n-input v-model:value="typeForm.dictCode" /></n-form-item>
        <n-form-item label="名称"><n-input v-model:value="typeForm.dictName" /></n-form-item>
        <n-form-item label="启用"><n-switch :value="typeForm.status === 1" @update:value="(value: boolean) => typeForm.status = value ? 1 : 0" /></n-form-item>
        <n-form-item label="备注"><n-input v-model:value="typeForm.remark" type="textarea" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="typeFormOpen = false">取消</n-button><n-button type="primary" @click="saveType">保存</n-button></n-space></template>
    </n-modal>

    <n-modal v-model:show="itemFormOpen" preset="card" title="字典项" style="width: 520px">
      <n-form :model="itemForm" label-placement="left" label-width="88">
        <n-form-item label="标签"><n-input v-model:value="itemForm.dictLabel" /></n-form-item>
        <n-form-item label="值"><n-input v-model:value="itemForm.dictValue" /></n-form-item>
        <n-form-item label="启用"><n-switch :value="itemForm.status === 1" @update:value="(value: boolean) => itemForm.status = value ? 1 : 0" /></n-form-item>
      </n-form>
      <template #footer><n-space justify="end"><n-button @click="itemFormOpen = false">取消</n-button><n-button type="primary" @click="saveItem">保存</n-button></n-space></template>
    </n-modal>
  </PageShell>
</template>

<style scoped>
.dict-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.dict-workspace {
  display: grid;
  grid-template-columns: minmax(420px, 0.9fr) minmax(520px, 1.1fr);
  gap: 16px;
  align-items: start;
}

.dict-card {
  min-height: 520px;
}

.dict-toolbar {
  display: grid;
  grid-template-columns: minmax(180px, 1fr) 120px auto;
  gap: 10px;
  margin-bottom: 14px;
}

.selected-type {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding: 14px;
  border: 1px solid rgba(24, 160, 88, 0.18);
  border-radius: 8px;
  background: rgba(24, 160, 88, 0.06);
}

.selected-type p {
  margin: 4px 0 0;
  color: #6b7280;
}

:deep(.dict-row) {
  cursor: pointer;
}

:deep(.dict-row--active td) {
  background: rgba(24, 160, 88, 0.1) !important;
}

@media (max-width: 1180px) {
  .dict-workspace {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .dict-summary {
    grid-template-columns: 1fr;
  }

  .dict-toolbar {
    grid-template-columns: 1fr;
  }

  .selected-type {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
