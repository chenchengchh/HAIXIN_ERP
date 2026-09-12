<template>
  <div class="positions-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Suitcase /></el-icon>
          <span>岗位管理</span>
        </div>
      </template>

      <TableComponent
        :data="positions"
        :columns="positionColumns"
        :total="positionTotal"
        :loading="positionLoading"
        :show-index="true"
        :show-action="true"
        :actions="positionActions"
        :table-actions="tableActions"
        :filters="positionFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="岗位编号" prop="position_code">
              <el-input v-model="formData.position_code" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位名称" prop="name">
              <el-input v-model="formData.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="等级" prop="level">
              <el-input v-model="formData.level" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门ID" prop="department_id">
              <el-input-number v-model="formData.department_id" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" />
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
import { computed, onMounted, ref } from 'vue'
import { Suitcase, Plus, Edit, Delete, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { unwrapPageResponse } from '../../../api'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'

const positions = ref<any[]>([])
const positionTotal = ref(0)
const positionLoading = ref(false)
const positionPage = ref(1)
const positionSize = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => (isEdit.value ? '编辑岗位' : '新增岗位'))
const submitLoading = ref(false)

const formRef = ref<FormInstance>()
const formData = ref<any>({
  id: undefined,
  name: '',
  position_code: '',
  level: '',
  department_id: 1,
  description: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  position_code: [{ required: true, message: '请输入岗位编号', trigger: 'blur' }]
}

const positionFilters = [
  { prop: 'keyword', label: '关键字', type: 'input' as 'input', placeholder: '名称/编号' }
] as any

const positionColumns = [
  { prop: 'positionCode', label: '岗位编号', width: 180 },
  { prop: 'name', label: '岗位名称', minWidth: 220 },
  { prop: 'level', label: '等级', width: 120 },
  { prop: 'departmentId', label: '部门ID', width: 120 }
]

const tableActions = [
  { key: 'add', text: '新增岗位', type: 'primary', icon: Plus, handler: handleAdd },
  { key: 'refresh', text: '刷新', icon: RefreshLeft, handler: () => handleSearch() }
]

const positionActions = [
  { text: '编辑', type: 'info', size: 'small', icon: Edit, handler: handleEdit },
  { text: '删除', type: 'danger', size: 'small', icon: Delete, handler: handleDelete }
]

onMounted(() => handleSearch())

const getPositionList = async (params: any = {}) => {
  try {
    positionLoading.value = true
    const res = await (erpApi.basicData as any).getPositions({
      page: params.page ?? positionPage.value,
      size: params.size ?? positionSize.value,
      keyword: params.keyword
    })
    const page = unwrapPageResponse<any>(res)
    positions.value = page.list
    positionTotal.value = page.total
    positionPage.value = page.page || (params.page ?? positionPage.value)
    positionSize.value = page.size || (params.size ?? positionSize.value)
  } catch (e) {
    ErrorHandler.handleApiError(e)
    positions.value = []
    positionTotal.value = 0
  } finally {
    positionLoading.value = false
  }
}

const handleSearch = (params: any = {}) => getPositionList(params)
const handleSizeChange = (size: number) => getPositionList({ page: positionPage.value, size })
const handleCurrentChange = (page: number) => getPositionList({ page, size: positionSize.value })

function handleAdd() {
  isEdit.value = false
  formData.value = { id: undefined, name: '', position_code: '', level: '', department_id: 1, description: '' }
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    name: row.name,
    position_code: row.positionCode,
    level: row.level,
    department_id: row.departmentId,
    description: row.description
  }
  dialogVisible.value = true
}

function handleDelete(row: any) {
  ElMessageBox.confirm(`确定要删除岗位"${row.name}"吗？`, '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await (erpApi.basicData as any).deletePosition(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (e) {
      ErrorHandler.handleApiError(e)
    }
  }).catch(() => {})
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      submitLoading.value = true
      const payload = {
        name: formData.value.name,
        position_code: formData.value.position_code,
        level: formData.value.level,
        department_id: formData.value.department_id,
        description: formData.value.description
      }
      if (isEdit.value) {
        await (erpApi.basicData as any).updatePosition(formData.value.id, payload)
        ElMessage.success('更新成功')
      } else {
        await (erpApi.basicData as any).createPosition(payload)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      handleSearch()
    } catch (e) {
      ErrorHandler.handleApiError(e)
    } finally {
      submitLoading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.positions-view {
  padding: 20px;
}
</style>

