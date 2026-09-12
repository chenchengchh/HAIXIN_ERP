<template>
  <div class="departments-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Grid /></el-icon>
          <span>部门管理</span>
        </div>
      </template>

      <TableComponent
        :data="departments"
        :columns="departmentColumns"
        :total="departmentTotal"
        :loading="departmentLoading"
        :show-index="true"
        :show-action="true"
        :actions="departmentActions"
        :table-actions="tableActions"
        :filters="departmentFilters"
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
            <el-form-item label="部门编号" prop="department_code">
              <el-input v-model="formData.department_code" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门名称" prop="name">
              <el-input v-model="formData.name" />
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
import { Grid, Plus, Edit, Delete, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { unwrapPageResponse } from '../../../api'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'

type DepartmentRow = {
  id?: number
  name: string
  departmentCode: string
  description?: string
  status?: string
  createdTime?: string
}

const departments = ref<DepartmentRow[]>([])
const departmentTotal = ref(0)
const departmentLoading = ref(false)
const departmentPage = ref(1)
const departmentSize = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => (isEdit.value ? '编辑部门' : '新增部门'))
const submitLoading = ref(false)

const formRef = ref<FormInstance>()
const formData = ref<any>({
  id: undefined,
  name: '',
  department_code: '',
  description: ''
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  department_code: [{ required: true, message: '请输入部门编号', trigger: 'blur' }]
}

const departmentFilters = [
  { prop: 'keyword', label: '关键字', type: 'input' as 'input', placeholder: '名称/编号' }
] as any

const departmentColumns = [
  { prop: 'departmentCode', label: '部门编号', width: 180 },
  { prop: 'name', label: '部门名称', minWidth: 220 },
  { prop: 'description', label: '描述', minWidth: 260 }
]

const tableActions = [
  { key: 'add', text: '新增部门', type: 'primary', icon: Plus, handler: handleAdd },
  { key: 'refresh', text: '刷新', icon: RefreshLeft, handler: () => handleSearch() }
]

const departmentActions = [
  { text: '编辑', type: 'info', size: 'small', icon: Edit, handler: handleEdit },
  { text: '删除', type: 'danger', size: 'small', icon: Delete, handler: handleDelete }
]

onMounted(() => handleSearch())

const getDepartmentList = async (params: any = {}) => {
  try {
    departmentLoading.value = true
    const res = await (erpApi.basicData as any).getDepartments({
      page: params.page ?? departmentPage.value,
      size: params.size ?? departmentSize.value,
      keyword: params.keyword
    })
    const page = unwrapPageResponse<DepartmentRow>(res)
    departments.value = page.list
    departmentTotal.value = page.total
    departmentPage.value = page.page || (params.page ?? departmentPage.value)
    departmentSize.value = page.size || (params.size ?? departmentSize.value)
  } catch (e) {
    ErrorHandler.handleApiError(e)
    departments.value = []
    departmentTotal.value = 0
  } finally {
    departmentLoading.value = false
  }
}

const handleSearch = (params: any = {}) => getDepartmentList(params)
const handleSizeChange = (size: number) => getDepartmentList({ page: departmentPage.value, size })
const handleCurrentChange = (page: number) => getDepartmentList({ page, size: departmentSize.value })

function handleAdd() {
  isEdit.value = false
  formData.value = { id: undefined, name: '', department_code: '', description: '' }
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    name: row.name,
    department_code: row.departmentCode,
    description: row.description
  }
  dialogVisible.value = true
}

function handleDelete(row: any) {
  ElMessageBox.confirm(`确定要删除部门"${row.name}"吗？`, '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await (erpApi.basicData as any).deleteDepartment(row.id)
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
        department_code: formData.value.department_code,
        description: formData.value.description
      }
      if (isEdit.value) {
        await (erpApi.basicData as any).updateDepartment(formData.value.id, payload)
        ElMessage.success('更新成功')
      } else {
        await (erpApi.basicData as any).createDepartment(payload)
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
.departments-view {
  padding: 20px;
}
</style>

