<template>
  <div class="organization-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><OfficeBuilding /></el-icon>
          <span>组织架构管理</span>
        </div>
      </template>
      
      <TableComponent
        :data="organizations"
        :columns="organizationColumns"
        :total="organizationTotal"
        :loading="organizationLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="organizationActions"
        :table-actions="tableActions"
        :filters="organizationFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <template #org_type="{ row }">
          <el-tag :type="getTypeColor(row.organizationType)" size="small">
            {{ getTypeLabel(row.organizationType) }}
          </el-tag>
        </template>

        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织编码" prop="organizationCode">
              <el-input v-model="formData.organizationCode" placeholder="留空自动生成" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组织名称" prop="organizationName">
              <el-input v-model="formData.organizationName" placeholder="请输入组织名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="组织类型" prop="organizationType">
              <el-select v-model="formData.organizationType" placeholder="请选择" style="width: 100%">
                <el-option label="集团" value="group" />
                <el-option label="公司" value="company" />
                <el-option label="部门" value="department" />
                <el-option label="小组" value="team" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="层级" prop="level">
              <el-input-number v-model="formData.level" :min="1" :max="10" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12" />
          <el-col :span="12" />
        </el-row>

        <el-row :gutter="20">
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="组织详情" size="600px" direction="rtl">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="组织编码">{{ currentRow.organizationCode }}</el-descriptions-item>
          <el-descriptions-item label="组织名称">{{ currentRow.organizationName }}</el-descriptions-item>
          <el-descriptions-item label="组织类型">
            <el-tag :type="getTypeColor(currentRow.organizationType)" size="small">
              {{ getTypeLabel(currentRow.organizationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="层级">{{ currentRow.level ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRow.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentRow.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.createdTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { OfficeBuilding, Plus, Edit, Delete, RefreshLeft, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { unwrapPageResponse } from '../../../api'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import type { Organization, OrganizationFormData, OrganizationType } from '../../../types/erp/basic-data'

const organizations = ref<Organization[]>([])
const organizationTotal = ref(0)
const organizationLoading = ref(false)
const organizationPage = ref(1)
const organizationSize = ref(10)
const selectedRows = ref<Organization[]>([])

const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑组织' : '新增组织')
const submitLoading = ref(false)
const currentRow = ref<Organization | null>(null)

const formRef = ref<FormInstance>()
const formData = ref<OrganizationFormData>({
  organizationCode: '',
  organizationName: '',
  organizationType: 'department' as OrganizationType,
  level: 1,
  status: 1,
  remark: ''
})

const formRules: FormRules = {
  organizationName: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  organizationType: [{ required: true, message: '请选择组织类型', trigger: 'change' }]
}

const organizationFilters = [
  { prop: 'code', label: '组织编码', type: 'input' as 'input', placeholder: '请输入组织编码' },
  { prop: 'name', label: '组织名称', type: 'input' as 'input', placeholder: '请输入组织名称' }
] as any

const organizationColumns = [
  { prop: 'organizationCode', label: '组织编码', width: 150 },
  { prop: 'organizationName', label: '组织名称', width: 220 },
  { prop: 'organizationType', label: '组织类型', width: 120, slotName: 'org_type' },
  { prop: 'level', label: '层级', width: 80 },
  { prop: 'status', label: '状态', width: 100, slotName: 'status' },
  { prop: 'createdTime', label: '创建时间', width: 180 }
]

const organizationActions = [
  { text: '查看', type: 'primary', size: 'small', icon: View, handler: handleView },
  { text: '编辑', type: 'info', size: 'small', icon: Edit, handler: handleEdit },
  { text: '删除', type: 'danger', size: 'small', icon: Delete, handler: handleDelete }
]

const tableActions = [
  { key: 'add', text: '新增组织', type: 'primary', icon: Plus, handler: handleAdd },
  { key: 'refresh', text: '刷新', icon: RefreshLeft, handler: () => handleSearch() }
]

onMounted(() => handleSearch())

const getOrganizationList = async (params: any) => {
  try {
    organizationLoading.value = true
    const queryParams = {
      page: params.page ?? organizationPage.value,
      size: params.size ?? organizationSize.value,
      code: params.code,
      name: params.name
    }
    const res = await erpApi.basicData.getOrganization(queryParams)
    const page = unwrapPageResponse<Organization>(res)
    organizations.value = page.list
    organizationTotal.value = page.total
    organizationPage.value = page.page || queryParams.page
    organizationSize.value = page.size || queryParams.size
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // API调用失败时使用空数组避免进一步错误
    organizations.value = []
    organizationTotal.value = 0
  } finally {
    organizationLoading.value = false
  }
}

const handleSearch = (params: any = {}) => getOrganizationList(params)
const handleSizeChange = (size: number) => getOrganizationList({ page: organizationPage.value, size })
const handleCurrentChange = (page: number) => getOrganizationList({ page, size: organizationSize.value })
const handleSelectionChange = (rows: Organization[]) => { selectedRows.value = rows }

function handleView(row: Organization) {
  currentRow.value = row
  detailVisible.value = true
}

function handleAdd() {
  isEdit.value = false
  formData.value = {
    organizationCode: '',
    organizationName: '',
    organizationType: 'department' as OrganizationType,
    level: 1,
    status: 1,
    remark: ''
  }
  dialogVisible.value = true
}

function handleEdit(row: Organization) {
  isEdit.value = true
  formData.value = {
    id: row.id,
    organizationCode: row.organizationCode,
    organizationName: row.organizationName,
    organizationType: row.organizationType,
    level: row.level ?? 1,
    status: row.status ?? 1,
    remark: row.remark
  } as any
  dialogVisible.value = true
}

function handleDelete(row: Organization) {
  ElMessageBox.confirm(`确定要删除组织"${row.organizationName}"吗？此操作不可撤销。`, '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await erpApi.basicData.deleteOrganization(row.id!)
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
        organization_name: formData.value.organizationName,
        organization_type: formData.value.organizationType,
        level: formData.value.level ?? 1,
        status: formData.value.status,
        remark: formData.value.remark
      }
      if (!isEdit.value && formData.value.organizationCode) {
        payload.organization_code = formData.value.organizationCode
      }
      if (isEdit.value) {
        await erpApi.basicData.updateOrganization(formData.value.id!, payload)
        ElMessage.success('更新成功')
      } else {
        await erpApi.basicData.createOrganization(payload)
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

const getTypeColor = (type: string) => {
  const map: Record<string, string> = {
    'group': 'danger', 'company': '', 'department': 'success', 'team': 'warning'
  }
  return map[type] || 'info'
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'group': '集团', 'company': '公司', 'department': '部门', 'team': '小组'
  }
  return map[type] || type
}
</script>

<style scoped lang="scss">
.organization-view {
  padding: 20px;
  
  .module-card {
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 20px;
      font-weight: bold;
      color: #1890ff;
    }
  }
}

.detail-content {
  padding: 20px;
}

@media (max-width: 1024px) {
  .organization-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .organization-view {
    padding: 12px;
    .module-card .card-header {
      font-size: 18px;
    }
  }
}
</style>
