<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { NButton, NSpace, NTag, useMessage } from 'naive-ui'
import PageShell from '@/shared/ui/PageShell.vue'
import {
  createDictItem,
  createDictType,
  deleteDictItemById,
  deleteDictTypeById,
  getDictItemPage,
  getDictTypePage,
  updateDictItem,
  updateDictType,
} from '../api'
import type { EntityId } from '@/shared/api/types'
import type { DictItemItem, DictTypeItem } from '../model'

const message = useMessage()
const typeLoading = ref(false)
const itemLoading = ref(false)
const types = ref<DictTypeItem[]>([])
const items = ref<DictItemItem[]>([])
const selectedTypeId = ref<EntityId>('')
const typeFormOpen = ref(false)
const itemFormOpen = ref(false)
const typeForm = reactive<Partial<DictTypeItem>>({ id: '', dictCode: '', dictName: '', status: 1, remark: '' })
const itemForm = reactive<Partial<DictItemItem>>({ id: '', dictTypeId: '', dictLabel: '', dictValue: '', sortOrder: 10, status: 1, tagType: '', remark: '' })
const typeQuery = reactive({ keyword: '', status: null as null | number })
const itemQuery = reactive({ keyword: '', status: null as null | number })
const typePagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onUpdatePage: (page: number) => {
    typePagination.page = page
    loadTypes()
  },
})
const itemPagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  onUpdatePage: (page: number) => {
    itemPagination.page = page
    loadItems()
  },
})

const selectedType = computed(() => types.value.find(item => item.id === selectedTypeId.value) ?? null)
const enabledTypeCount = computed(() => types.value.filter(item => item.status === 1).length)
const enabledItemCount = computed(() => items.value.filter(item => item.status === 1).length)

async function loadTypes() {
  typeLoading.value = true
  try {
    // 后端编码和名称条件按 AND 组合，快速搜索只映射名称，避免误过滤。
    const keyword = typeQuery.keyword.trim()
    const page = await getDictTypePage({
      page: typePagination.page,
      pageSize: typePagination.pageSize,
      dictName: keyword,
      status: typeQuery.status,
    })
    types.value = page.records
    typePagination.itemCount = page.totalRow
    if (!selectedTypeId.value && page.records.length > 0) {
      selectedTypeId.value = page.records[0].id
    }
  }
  catch (error: any) {
    types.value = []
    typePagination.itemCount = 0
    message.error(error?.message || '加载字典类型失败')
  }
  finally {
    typeLoading.value = false
  }
}

async function loadItems() {
  if (!selectedTypeId.value) {
    items.value = []
    return
  }
  itemLoading.value = true
  try {
    const page = await getDictItemPage({
      page: itemPagination.page,
      pageSize: itemPagination.pageSize,
      dictTypeId: selectedTypeId.value,
      dictLabel: itemQuery.keyword.trim(),
      status: itemQuery.status,
    })
    items.value = page.records
    itemPagination.itemCount = page.totalRow
  }
  catch (error: any) {
    items.value = []
    itemPagination.itemCount = 0
    message.error(error?.message || '加载字典项失败')
  }
  finally {
    itemLoading.value = false
  }
}

function openTypeForm(row?: DictTypeItem) {
  // 编辑和新增共用同一表单，保存时按 id 判断调用新增或更新接口。
  Object.assign(typeForm, row ?? { id: '', dictCode: '', dictName: '', status: 1, remark: '' })
  typeFormOpen.value = true
}

async function saveType() {
  if (!typeForm.dictCode || !typeForm.dictName) {
    message.error('字典编码和名称不能为空')
    return
  }
  try {
    if (typeForm.id) {
      await updateDictType(typeForm)
    }
    else {
      await createDictType(typeForm)
    }
    message.success('字典类型已保存')
    typeFormOpen.value = false
    await loadTypes()
  }
  catch (error: any) {
    message.error(error?.message || '保存字典类型失败')
  }
}

function openItemForm(row?: DictItemItem) {
  if (!selectedTypeId.value) {
    message.error('请先选择字典类型')
    return
  }
  Object.assign(itemForm, row ?? { id: '', dictTypeId: selectedTypeId.value, dictLabel: '', dictValue: '', sortOrder: 10, status: 1, tagType: '', remark: '' })
  itemFormOpen.value = true
}

async function saveItem() {
  if (!itemForm.dictLabel || !itemForm.dictValue) {
    message.error('字典项标签和值不能为空')
    return
  }
  try {
    if (itemForm.id) {
      await updateDictItem(itemForm)
    }
    else {
      await createDictItem(itemForm)
    }
    message.success('字典项已保存')
    itemFormOpen.value = false
    await loadItems()
  }
  catch (error: any) {
    message.error(error?.message || '保存字典项失败')
  }
}

async function deleteType(row: DictTypeItem) {
  try {
    await deleteDictTypeById(row.id)
    message.success('字典类型已删除')
    selectedTypeId.value = ''
    await loadTypes()
    await loadItems()
  }
  catch (error: any) {
    message.error(error?.message || '删除字典类型失败')
  }
}

async function deleteItem(row: DictItemItem) {
  try {
    await deleteDictItemById(row.id)
    message.success('字典项已删除')
    await loadItems()
  }
  catch (error: any) {
    message.error(error?.message || '删除字典项失败')
  }
}

function resetTypeQuery() {
  typeQuery.keyword = ''
  typeQuery.status = null
  typePagination.page = 1
  loadTypes()
}

function resetItemQuery() {
  itemQuery.keyword = ''
  itemQuery.status = null
  itemPagination.page = 1
  loadItems()
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
  { title: '排序', key: 'sortOrder' },
  { title: '状态', key: 'status', render: (row: DictItemItem) => h(NTag, { type: row.status ? 'success' : 'default' }, { default: () => row.status ? '启用' : '停用' }) },
  { title: '操作', key: 'actions', render: (row: DictItemItem) => h(NSpace, { justify: 'end' }, { default: () => [h(NButton, { size: 'small', onClick: () => openItemForm(row) }, { default: () => '编辑' }), h(NButton, { size: 'small', type: 'error', ghost: true, onClick: () => deleteItem(row) }, { default: () => '删除' })] }) },
]

watch(selectedTypeId, () => {
  itemPagination.page = 1
  loadItems()
})

onMounted(loadTypes)
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
          <n-input v-model:value="typeQuery.keyword" placeholder="搜索编码或名称" clearable @keyup.enter="loadTypes" />
          <n-select v-model:value="typeQuery.status" clearable placeholder="状态" :options="[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]" />
          <n-button type="primary" @click="loadTypes">查询</n-button>
          <n-button @click="resetTypeQuery">重置</n-button>
        </div>
        <n-data-table
          :columns="typeColumns"
          :data="types"
          :loading="typeLoading"
          :row-key="(row: DictTypeItem) => row.id"
          :row-props="(row: DictTypeItem) => ({
            class: row.id === selectedTypeId ? 'dict-row dict-row--active' : 'dict-row',
            onClick: () => selectedTypeId = row.id,
          })"
          :pagination="typePagination"
          remote
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
          <n-input v-model:value="itemQuery.keyword" placeholder="搜索标签或值" clearable @keyup.enter="loadItems" />
          <n-select v-model:value="itemQuery.status" clearable placeholder="状态" :options="[{ label: '启用', value: 1 }, { label: '停用', value: 0 }]" />
          <n-button type="primary" @click="loadItems">查询</n-button>
          <n-button @click="resetItemQuery">重置</n-button>
        </div>
        <n-data-table :columns="itemColumns" :data="items" :loading="itemLoading" :pagination="itemPagination" remote />
      </n-card>
    </div>

    <n-modal v-model:show="typeFormOpen" preset="card" title="字典类型" style="width: 520px">
      <n-form :model="typeForm" label-placement="left" label-width="88">
        <n-form-item label="编码"><n-input v-model:value="typeForm.dictCode" :disabled="!!typeForm.id" /></n-form-item>
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
        <n-form-item label="排序"><n-input-number v-model:value="itemForm.sortOrder" /></n-form-item>
        <n-form-item label="启用"><n-switch :value="itemForm.status === 1" @update:value="(value: boolean) => itemForm.status = value ? 1 : 0" /></n-form-item>
        <n-form-item label="备注"><n-input v-model:value="itemForm.remark" type="textarea" /></n-form-item>
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
  grid-template-columns: minmax(180px, 1fr) 120px auto auto;
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
