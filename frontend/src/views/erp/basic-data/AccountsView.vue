<template>
  <div class="accounts-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>会计科目管理</span>
        </div>
      </template>
      
      <TableComponent
        :data="accounts"
        :columns="accountColumns"
        :total="accountTotal"
        :loading="accountLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="accountActions"
        :table-actions="tableActions"
        :filters="accountFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <template #account_type="{ row }">
          <el-tag :type="getTypeColor(row.account_type)" size="small">
            {{ getTypeLabel(row.account_type) }}
          </el-tag>
        </template>

        <template #status="{ row }">
          <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
            {{ row.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="科目编码" prop="account_code">
              <el-input v-model="formData.account_code" placeholder="留空自动生成" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目名称" prop="account_name">
              <el-input v-model="formData.account_name" placeholder="请输入科目名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="科目类型" prop="account_type">
              <el-select v-model="formData.account_type" placeholder="请选择" style="width: 100%">
                <el-option label="资产类" value="asset" />
                <el-option label="负债类" value="liability" />
                <el-option label="权益类" value="equity" />
                <el-option label="成本类" value="cost" />
                <el-option label="损益类" value="profit" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="余额方向" prop="balance_direction">
              <el-radio-group v-model="formData.balance_direction">
                <el-radio value="debit">借方</el-radio>
                <el-radio value="credit">贷方</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上级科目" prop="parent_account">
              <el-input v-model="formData.parent_account" placeholder="请输入上级科目" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio value="active">启用</el-radio>
                <el-radio value="inactive">禁用</el-radio>
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
    <el-drawer v-model="detailVisible" title="科目详情" size="600px" direction="rtl">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="科目编码">{{ currentRow.account_code }}</el-descriptions-item>
          <el-descriptions-item label="科目名称">{{ currentRow.account_name }}</el-descriptions-item>
          <el-descriptions-item label="科目类型">
            <el-tag :type="getTypeColor(currentRow.account_type)" size="small">
              {{ getTypeLabel(currentRow.account_type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="余额方向">
            {{ currentRow.balance_direction === '借方' ? '借方' : '贷方' }}
          </el-descriptions-item>
          <el-descriptions-item label="上级科目">{{ currentRow.parent_account || '-' }}</el-descriptions-item>
          <el-descriptions-item label="科目级别">{{ currentRow.account_level || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRow.status === 'active' ? 'success' : 'danger'" size="small">
              {{ currentRow.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentRow.create_time || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Document, Plus, Edit, Delete, RefreshLeft, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { DataTransformer } from '../../../utils/data-transformer'
import type { Account, AccountFormData, AccountType, BalanceDirection, CommonStatus } from '../../../types/erp/basic-data'

const accounts = ref<Account[]>([])
const accountTotal = ref(0)
const accountLoading = ref(false)
const accountPage = ref(1)
const accountSize = ref(10)
const selectedRows = ref<Account[]>([])

const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑会计科目' : '新增会计科目')
const submitLoading = ref(false)
const currentRow = ref<Account | null>(null)

const formRef = ref<FormInstance>()
const formData = ref<AccountFormData>({
  account_code: '',
  account_name: '',
  account_type: '资产类' as AccountType,
  balance_direction: '借方' as BalanceDirection,
  parent_account: '',
  status: 'active' as CommonStatus,
  remark: ''
})

const formRules: FormRules = {
  account_name: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
  account_type: [{ required: true, message: '请选择科目类型', trigger: 'change' }],
  balance_direction: [{ required: true, message: '请选择余额方向', trigger: 'change' }]
}

const accountFilters = [
  { prop: 'account_code', label: '科目编码', type: 'input' as 'input', placeholder: '请输入科目编码' },
  { prop: 'account_name', label: '科目名称', type: 'input' as 'input', placeholder: '请输入科目名称' },
  { prop: 'account_type', label: '科目类型', type: 'select' as 'select', options: [
    { label: '资产类', value: 'asset' },
    { label: '负债类', value: 'liability' },
    { label: '权益类', value: 'equity' },
    { label: '成本类', value: 'cost' },
    { label: '损益类', value: 'profit' }
  ]},
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '启用', value: 'active' },
    { label: '禁用', value: 'inactive' }
  ]},
  { prop: 'create_time', label: '创建时间', type: 'daterange' as 'daterange' }
] as any

const accountColumns = [
  { prop: 'account_code', label: '科目编码', width: 150 },
  { prop: 'account_name', label: '科目名称', width: 200 },
  { prop: 'account_type', label: '科目类型', width: 120, slotName: 'account_type' },
  { prop: 'parent_account', label: '上级科目', width: 180 },
  { prop: 'account_level', label: '科目级别', width: 100, align: 'center' },
  { prop: 'balance_direction', label: '余额方向', width: 120 },
  { prop: 'status', label: '状态', width: 100, slotName: 'status' },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

const accountActions = [
  { text: '查看', type: 'primary', size: 'small', icon: View, handler: handleView },
  { text: '编辑', type: 'info', size: 'small', icon: Edit, handler: handleEdit },
  { text: '删除', type: 'danger', size: 'small', icon: Delete, handler: handleDelete }
]

const tableActions = [
  { key: 'add', text: '新增会计科目', type: 'primary', icon: Plus, handler: handleAdd },
  { key: 'refresh', text: '刷新', icon: RefreshLeft, handler: () => handleSearch() }
]

onMounted(() => handleSearch())

const getAccountList = async (params: any) => {
  try {
    accountLoading.value = true
    const result = await erpApi.basicData.getAccounts({
      page: params.page || accountPage.value,
      size: params.size || accountSize.value,
      ...params
    })
    const normalizedData = DataTransformer.normalizeResponse(result)
    let list = normalizedData?.data?.list || normalizedData?.data || []
    if (!Array.isArray(list)) list = []
    accounts.value = list
    accountTotal.value = Number(normalizedData?.data?.total) || list.length || 0
    accountPage.value = params.page || accountPage.value
    accountSize.value = params.size || accountSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // API调用失败时使用空数组避免进一步错误
    accounts.value = []
    accountTotal.value = 0
  } finally {
    accountLoading.value = false
  }
}

const handleSearch = (params: any = {}) => getAccountList(params)
const handleSizeChange = (size: number) => getAccountList({ page: accountPage.value, size })
const handleCurrentChange = (page: number) => getAccountList({ page, size: accountSize.value })
const handleSelectionChange = (rows: Account[]) => { selectedRows.value = rows }

function handleView(row: Account) {
  currentRow.value = row
  detailVisible.value = true
}

function handleAdd() {
  isEdit.value = false
  formData.value = {
    account_code: '', account_name: '', account_type: '资产类' as AccountType,
    balance_direction: '借方' as BalanceDirection, parent_account: '', status: 'active' as CommonStatus, remark: ''
  }
  dialogVisible.value = true
}

function handleEdit(row: Account) {
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

function handleDelete(row: Account) {
  ElMessageBox.confirm(`确定要删除科目"${row.account_name}"吗？此操作不可撤销。`, '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await erpApi.basicData.deleteAccount(row.id!)
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
      if (isEdit.value) {
        await erpApi.basicData.updateAccount(formData.value.id!, formData.value)
        ElMessage.success('更新成功')
      } else {
        await erpApi.basicData.createAccount(formData.value)
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
    'asset': 'success', 'liability': 'danger', 'equity': '',
    'cost': 'warning', 'profit': 'info'
  }
  return map[type] || 'info'
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'asset': '资产类', 'liability': '负债类', 'equity': '权益类',
    'cost': '成本类', 'profit': '损益类'
  }
  return map[type] || type
}
</script>

<style scoped lang="scss">
.accounts-view {
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
  .accounts-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .accounts-view {
    padding: 12px;
    .module-card .card-header {
      font-size: 18px;
    }
  }
}
</style>
