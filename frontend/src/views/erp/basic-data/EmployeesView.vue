<template>
  <div class="employees-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><User /></el-icon>
          <span>人员管理</span>
        </div>
      </template>

      <TableComponent
        :data="employees"
        :columns="employeeColumns"
        :total="employeeTotal"
        :loading="employeeLoading"
        :show-index="true"
        :show-action="true"
        :actions="employeeActions"
        :table-actions="tableActions"
        :filters="employeeFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="employee_code">
              <el-input v-model="formData.employee_code" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机" prop="phone">
              <el-input v-model="formData.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门ID" prop="department_id">
              <el-input-number v-model="formData.department_id" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位ID" prop="position_id">
              <el-input-number v-model="formData.position_id" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" />
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
import { User, Plus, Edit, Delete, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { unwrapPageResponse } from '../../../api'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'

const employees = ref<any[]>([])
const employeeTotal = ref(0)
const employeeLoading = ref(false)
const employeePage = ref(1)
const employeeSize = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => (isEdit.value ? '编辑人员' : '新增人员'))
const submitLoading = ref(false)

const formRef = ref<FormInstance>()
const formData = ref<any>({
  id: undefined,
  employee_code: '',
  name: '',
  phone: '',
  email: '',
  department_id: 1,
  position_id: 1,
  remark: ''
})

const formRules: FormRules = {
  employee_code: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const employeeFilters = [
  { prop: 'keyword', label: '关键字', type: 'input' as 'input', placeholder: '姓名/工号' }
] as any

const employeeColumns = [
  { prop: 'employeeCode', label: '工号', width: 160 },
  { prop: 'name', label: '姓名', width: 140 },
  { prop: 'phone', label: '手机', width: 160 },
  { prop: 'email', label: '邮箱', minWidth: 200 },
  { prop: 'departmentId', label: '部门ID', width: 120 },
  { prop: 'positionId', label: '岗位ID', width: 120 }
]

const tableActions = [
  { key: 'add', text: '新增人员', type: 'primary', icon: Plus, handler: handleAdd },
  { key: 'refresh', text: '刷新', icon: RefreshLeft, handler: () => handleSearch() }
]

const employeeActions = [
  { text: '编辑', type: 'info', size: 'small', icon: Edit, handler: handleEdit },
  { text: '删除', type: 'danger', size: 'small', icon: Delete, handler: handleDelete }
]

onMounted(() => handleSearch())

const getEmployeeList = async (params: any = {}) => {
  try {
    employeeLoading.value = true
    const res = await erpApi.basicData.getEmployees({
      page: params.page ?? employeePage.value,
      size: params.size ?? employeeSize.value,
      keyword: params.keyword
    })
    const page = unwrapPageResponse<any>(res)
    employees.value = page.list
    employeeTotal.value = page.total
    employeePage.value = page.page || (params.page ?? employeePage.value)
    employeeSize.value = page.size || (params.size ?? employeeSize.value)
  } catch (e) {
    ErrorHandler.handleApiError(e)
    employees.value = []
    employeeTotal.value = 0
  } finally {
    employeeLoading.value = false
  }
}

const handleSearch = (params: any = {}) => getEmployeeList(params)
const handleSizeChange = (size: number) => getEmployeeList({ page: employeePage.value, size })
const handleCurrentChange = (page: number) => getEmployeeList({ page, size: employeeSize.value })

function handleAdd() {
  isEdit.value = false
  formData.value = { id: undefined, employee_code: '', name: '', phone: '', email: '', department_id: 1, position_id: 1, remark: '' }
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    employee_code: row.employeeCode,
    name: row.name,
    phone: row.phone,
    email: row.email,
    department_id: row.departmentId,
    position_id: row.positionId,
    remark: row.remark
  }
  dialogVisible.value = true
}

function handleDelete(row: any) {
  ElMessageBox.confirm(`确定要删除人员"${row.name}"吗？`, '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await (erpApi.basicData as any).deleteEmployee(row.id)
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
      const payload: any = {
        employee_code: formData.value.employee_code,
        name: formData.value.name,
        phone: formData.value.phone,
        email: formData.value.email,
        department_id: formData.value.department_id,
        position_id: formData.value.position_id,
        remark: formData.value.remark
      }
      if (isEdit.value) {
        await (erpApi.basicData as any).updateEmployee(formData.value.id, payload)
        ElMessage.success('更新成功')
      } else {
        await erpApi.basicData.createEmployee(payload)
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
.employees-view {
  padding: 20px;
}
</style>

