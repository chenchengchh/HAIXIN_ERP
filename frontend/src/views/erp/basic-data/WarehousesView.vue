<template>
  <div class="warehouses-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><House /></el-icon>
          <span>仓库管理</span>
        </div>
      </template>

      <TableComponent
        :data="warehouses"
        :columns="warehouseColumns"
        :total="warehouseTotal"
        :loading="warehouseLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="warehouseActions"
        :table-actions="tableActions"
        :filters="warehouseFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="仓库编码" prop="warehouseCode">
              <el-input v-model="formData.warehouseCode" placeholder="请输入仓库编码" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="仓库名称" prop="warehouseName">
              <el-input v-model="formData.warehouseName" placeholder="请输入仓库名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="manager">
              <el-input v-model="formData.manager" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Edit, Delete, RefreshLeft, House } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { unwrapPageResponse } from '../../../api'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import type { Warehouse, WarehouseQueryParams } from '../../../types/erp/basic-data'

const warehouses = ref<Warehouse[]>([])
const warehouseTotal = ref(0)
const warehouseLoading = ref(false)
const warehousePage = ref(1)
const warehouseSize = ref(10)
const selectedRows = ref<Warehouse[]>([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => (isEdit.value ? '编辑仓库' : '新增仓库'))
const submitLoading = ref(false)

const formRef = ref<FormInstance>()
const formData = ref<Warehouse>({
  warehouseCode: '',
  warehouseName: '',
  manager: '',
  status: 1,
  remark: ''
})

const formRules: FormRules = {
  warehouseCode: [{ required: true, message: '请输入仓库编码', trigger: 'blur' }],
  warehouseName: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }]
}

const warehouseFilters = [
  { prop: 'code', label: '仓库编码', type: 'input' as 'input', placeholder: '请输入仓库编码' },
  { prop: 'name', label: '仓库名称', type: 'input' as 'input', placeholder: '请输入仓库名称' }
] as any

const warehouseColumns = [
  { prop: 'warehouseCode', label: '仓库编码', width: 160 },
  { prop: 'warehouseName', label: '仓库名称', minWidth: 220 },
  { prop: 'manager', label: '负责人', width: 140 },
  { prop: 'status', label: '状态', width: 100, slotName: 'status' }
]

const warehouseActions = [
  { text: '编辑', type: 'info', size: 'small', icon: Edit, handler: handleEdit },
  { text: '删除', type: 'danger', size: 'small', icon: Delete, handler: handleDelete }
]

const tableActions = [
  { key: 'add', text: '新增仓库', type: 'primary', icon: Plus, handler: handleAdd },
  { key: 'refresh', text: '刷新', icon: RefreshLeft, handler: () => handleSearch() }
]

onMounted(() => handleSearch())

const getWarehouseList = async (params: WarehouseQueryParams & any) => {
  try {
    warehouseLoading.value = true
    const queryParams = {
      page: params.page ?? warehousePage.value,
      size: params.size ?? warehouseSize.value
    }
    const res = await erpApi.basicData.getWarehouses(queryParams)
    const page = unwrapPageResponse<Warehouse>(res)
    warehouses.value = page.list
    warehouseTotal.value = page.total
    warehousePage.value = page.page || queryParams.page
    warehouseSize.value = page.size || queryParams.size
  } catch (error) {
    ErrorHandler.handleApiError(error)
    warehouses.value = []
    warehouseTotal.value = 0
  } finally {
    warehouseLoading.value = false
  }
}

const handleSearch = (params: any = {}) => getWarehouseList(params)
const handleSizeChange = (size: number) => getWarehouseList({ page: warehousePage.value, size })
const handleCurrentChange = (page: number) => getWarehouseList({ page, size: warehouseSize.value })
const handleSelectionChange = (rows: Warehouse[]) => { selectedRows.value = rows }

function handleAdd() {
  isEdit.value = false
  formData.value = { warehouseCode: '', warehouseName: '', manager: '', status: 1, remark: '' }
  dialogVisible.value = true
}

function handleEdit(row: Warehouse) {
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

function handleDelete(row: Warehouse) {
  ElMessageBox.confirm(`确定要删除仓库"${row.warehouseName}"吗？此操作不可撤销。`, '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await (erpApi.basicData as any).deleteWarehouse(row.id!)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error) {
      ErrorHandler.handleApiError(error)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      submitLoading.value = true
      const payload: any = {
        warehouse_code: formData.value.warehouseCode,
        warehouse_name: formData.value.warehouseName,
        manager: formData.value.manager,
        status: formData.value.status,
        remark: formData.value.remark
      }
      if (isEdit.value) {
        await (erpApi.basicData as any).updateWarehouse(formData.value.id!, payload)
        ElMessage.success('更新成功')
      } else {
        await (erpApi.basicData as any).createWarehouse(payload)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      handleSearch()
    } catch (error) {
      ErrorHandler.handleApiError(error)
    } finally {
      submitLoading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.warehouses-view {
  padding: 20px;
}
</style>

