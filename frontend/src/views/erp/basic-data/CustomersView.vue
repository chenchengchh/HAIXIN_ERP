<template>
  <div class="customers-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><UserFilled /></el-icon>
          <span>客户管理</span>
        </div>
      </template>
      
      <!-- 客户列表 -->
      <TableComponent
        :data="customers"
        :columns="customerColumns"
        :total="customerTotal"
        :loading="customerLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="customerActions"
        :table-actions="tableActions"
        :filters="customerFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <!-- 客户等级列 -->
        <template #customer_level="{ row }">
          <el-tag :type="getLevelType(row.customer_level)" size="small">
            {{ getLevelLabel(row.customer_level) }}
          </el-tag>
        </template>

        <!-- 状态列自定义 -->
        <template #status="{ row }">
          <el-tag
            :type="row.status === 'active' ? 'success' : 'danger'"
            size="small"
          >
            {{ row.status === 'active' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-tabs v-model="activeTabCustomer">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="客户编码" prop="customer_code">
                  <el-input
                    v-model="formData.customer_code"
                    placeholder="留空自动生成"
                    :disabled="isEdit"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户名称" prop="customer_name">
                  <el-input v-model="formData.customer_name" placeholder="请输入客户名称" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="客户等级" prop="customer_level">
                  <el-select v-model="formData.customer_level" placeholder="请选择客户等级" style="width: 100%">
                    <el-option label="VIP客户" value="vip" />
                    <el-option label="普通客户" value="normal" />
                    <el-option label="潜在客户" value="potential" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户状态" prop="status">
                  <el-radio-group v-model="formData.status">
                    <el-radio value="active">启用</el-radio>
                    <el-radio value="inactive">禁用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="信用额度" prop="credit_limit">
                  <el-input-number
                    v-model="formData.credit_limit"
                    :min="0"
                    :precision="2"
                    style="width: 100%"
                    placeholder="请输入信用额度"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="付款条件" prop="payment_terms">
                  <el-input v-model="formData.payment_terms" placeholder="如：月结30天" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <!-- 联系信息 -->
          <el-tab-pane label="联系信息" name="contact">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系人" prop="contact_person">
                  <el-input v-model="formData.contact_person" placeholder="请输入联系人" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="contact_phone">
                  <el-input v-model="formData.contact_phone" placeholder="请输入联系电话" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="formData.email" placeholder="请输入邮箱" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="传真" prop="fax">
                  <el-input v-model="formData.fax" placeholder="请输入传真号码" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="地址" prop="address">
              <el-input
                v-model="formData.address"
                type="textarea"
                :rows="3"
                placeholder="请输入详细地址"
              />
            </el-form-item>
          </el-tab-pane>

          <!-- 其他信息 -->
          <el-tab-pane label="其他信息" name="other">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="所属行业" prop="industry">
                  <el-input v-model="formData.industry" placeholder="如：制造业" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="公司规模" prop="company_size">
                  <el-select v-model="formData.company_size" placeholder="请选择公司规模" style="width: 100%">
                    <el-option label="小型（1-50人）" value="small" />
                    <el-option label="中型（51-500人）" value="medium" />
                    <el-option label="大型（500人以上）" value="large" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="4"
                placeholder="请输入备注信息"
              />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      title="客户详情"
      size="600px"
      direction="rtl"
    >
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="客户编码">
            {{ currentRow.customer_code }}
          </el-descriptions-item>
          <el-descriptions-item label="客户名称">
            {{ currentRow.customer_name }}
          </el-descriptions-item>
          <el-descriptions-item label="客户等级">
            <el-tag :type="getLevelType(currentRow.customer_level)" size="small">
              {{ getLevelLabel(currentRow.customer_level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户状态">
            <el-tag :type="currentRow.status === 'active' ? 'success' : 'danger'" size="small">
              {{ currentRow.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="信用额度">
            ¥{{ currentRow.credit_limit || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="付款条件">
            {{ currentRow.payment_terms || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系人">
            {{ currentRow.contact_person || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentRow.contact_phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱" :span="2">
            {{ currentRow.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">
            {{ currentRow.address || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="所属行业">
            {{ currentRow.industry || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="公司规模">
            {{ getCompanySizeLabel(currentRow.company_size) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ currentRow.create_time || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentRow.remark || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Edit, Delete, RefreshLeft, View, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { DataTransformer } from '../../../utils/data-transformer'
import type { Customer, CustomerFormData, CustomerLevel, CommonStatus } from '../../../types/erp/basic-data'

// 响应式数据
const customers = ref<Customer[]>([])
const customerTotal = ref(0)
const customerLoading = ref(false)
const customerPage = ref(1)
const customerSize = ref(10)
const selectedRows = ref<Customer[]>([])

// 对话框控制
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑客户' : '新增客户')
// 子组件内的activeTab，避免与父组件冲突
const activeTabCustomer = ref('basic')
const submitLoading = ref(false)
const currentRow = ref<Customer | null>(null)

// 表单引用和数据
const formRef = ref<FormInstance>()
const formData = ref<CustomerFormData>({
  customer_code: '',
  customer_name: '',
  customer_level: 'normal' as CustomerLevel,
  status: 'active' as CommonStatus,
  credit_limit: 0,
  payment_terms: '',
  contact_person: '',
  contact_phone: '',
  email: '',
  fax: '',
  address: '',
  industry: '',
  company_size: '',
  remark: ''
})

// 表单验证规则
const formRules: FormRules = {
  customer_name: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  customer_level: [
    { required: true, message: '请选择客户等级', trigger: 'change' }
  ],
  contact_phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 客户筛选条件
const customerFilters = [
  { prop: 'customer_code', label: '客户编码', type: 'input' as 'input', placeholder: '请输入客户编码' },
  { prop: 'customer_name', label: '客户名称', type: 'input' as 'input', placeholder: '请输入客户名称' },
  { prop: 'customer_level', label: '客户等级', type: 'select' as 'select', options: [
    { label: 'VIP', value: 'vip' },
    { label: '普通', value: 'normal' },
    { label: '潜在', value: 'potential' }
  ]},
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '启用', value: 'active' },
    { label: '禁用', value: 'inactive' }
  ]},
  { prop: 'create_time', label: '创建时间', type: 'daterange' as 'daterange' }
] as any

// 客户列配置
const customerColumns = [
  { prop: 'customer_code', label: '客户编码', width: 150 },
  { prop: 'customer_name', label: '客户名称', width: 200 },
  { prop: 'customer_level', label: '客户等级', width: 100, slotName: 'customer_level' },
  { prop: 'contact_person', label: '联系人', width: 120 },
  { prop: 'contact_phone', label: '联系电话', width: 150 },
  { prop: 'address', label: '地址', minWidth: 200 },
  { prop: 'status', label: '状态', width: 100, slotName: 'status' },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

// 客户操作按钮
const customerActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: handleView
  },
  {
    text: '编辑',
    type: 'info',
    size: 'small',
    icon: Edit,
    handler: handleEdit
  },
  {
    text: '删除',
    type: 'danger',
    size: 'small',
    icon: Delete,
    handler: handleDelete
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'add',
    text: '新增客户',
    type: 'primary',
    icon: Plus,
    handler: handleAdd
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => handleSearch()
  }
]

// 初始加载
onMounted(() => {
  handleSearch()
})

// 获取客户列表
const getCustomerList = async (params: any) => {
  try {
    customerLoading.value = true
    const result = await erpApi.basicData.getCustomers({
      page: params.page || customerPage.value,
      size: params.size || customerSize.value,
      ...params
    })

    const normalizedData = DataTransformer.normalizeResponse(result)
    let list = normalizedData?.data?.list || normalizedData?.data || []
    if (!Array.isArray(list)) list = []

    customers.value = list
    customerTotal.value = Number(normalizedData?.data?.total) || list.length || 0
    customerPage.value = params.page || customerPage.value
    customerSize.value = params.size || customerSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    customers.value = []
    customerTotal.value = 0
  } finally {
    customerLoading.value = false
  }
}

// 搜索客户
const handleSearch = (params: any = {}) => {
  getCustomerList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  customerSize.value = size
  getCustomerList({ page: customerPage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  customerPage.value = page
  getCustomerList({ page, size: customerSize.value })
}

// 选择行改变
const handleSelectionChange = (rows: Customer[]) => {
  selectedRows.value = rows
}

// 查看详情
function handleView(row: Customer) {
  currentRow.value = row
  detailVisible.value = true
}

// 新增客户
function handleAdd() {
  isEdit.value = false
  activeTabCustomer.value = 'basic'
  formData.value = {
    customer_code: '',
    customer_name: '',
    customer_level: 'normal' as CustomerLevel,
    status: 'active' as CommonStatus,
    credit_limit: 0,
    payment_terms: '',
    contact_person: '',
    contact_phone: '',
    email: '',
    fax: '',
    address: '',
    industry: '',
    company_size: '',
    remark: ''
  }
  dialogVisible.value = true
}

// 编辑客户
function handleEdit(row: Customer) {
  isEdit.value = true
  activeTabCustomer.value = 'basic'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除客户
function handleDelete(row: Customer) {
  ElMessageBox.confirm(
    `确定要删除客户"${row.customer_name}"吗？此操作不可撤销。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await erpApi.basicData.deleteCustomer(row.id!)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error) {
      ErrorHandler.handleApiError(error)
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      submitLoading.value = true
      if (isEdit.value) {
        await erpApi.basicData.updateCustomer(formData.value.id!, formData.value)
        ElMessage.success('更新成功')
      } else {
        await erpApi.basicData.createCustomer(formData.value)
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

// 获取客户等级类型
const getLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    'vip': 'danger',
    'normal': '',
    'potential': 'warning'
  }
  return typeMap[level] || 'info'
}

// 获取客户等级标签
const getLevelLabel = (level: string) => {
  const labelMap: Record<string, string> = {
    'vip': 'VIP',
    'normal': '普通',
    'potential': '潜在'
  }
  return labelMap[level] || level
}

// 获取公司规模标签
const getCompanySizeLabel = (size?: string) => {
  if (!size) return '-'
  const labelMap: Record<string, string> = {
    'small': '小型（1-50人）',
    'medium': '中型（51-500人）',
    'large': '大型（500人以上）'
  }
  return labelMap[size] || '-'
}
</script>

<style scoped lang="scss">
.customers-view {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

// 响应式设计
@media (max-width: 1024px) {
  .customers-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .customers-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}
</style>
