<template>
  <div class="suppliers-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><OfficeBuilding /></el-icon>
          <span>供应商管理</span>
        </div>
      </template>
      
      <TableComponent
        :data="suppliers"
        :columns="supplierColumns"
        :total="supplierTotal"
        :loading="supplierLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="supplierActions"
        :table-actions="tableActions"
        :filters="supplierFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <template #supplier_type="{ row }">
          <el-tag :type="getTypeColor(row.supplier_type)" size="small">
            {{ getTypeLabel(row.supplier_type) }}
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-tabs v-model="activeTabSupplier">
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="供应商编码" prop="supplier_code">
                  <el-input v-model="formData.supplier_code" placeholder="留空自动生成" :disabled="isEdit" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="供应商名称" prop="supplier_name">
                  <el-input v-model="formData.supplier_name" placeholder="请输入供应商名称" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="供应商类型" prop="supplier_type">
                  <el-select v-model="formData.supplier_type" placeholder="请选择类型" style="width: 100%">
                    <el-option label="原材料" value="raw_material" />
                    <el-option label="半成品" value="semi_finished" />
                    <el-option label="成品" value="finished" />
                    <el-option label="服务" value="service" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="供应商评级" prop="rating">
                  <el-select v-model="formData.rating" placeholder="请选择评级" style="width: 100%">
                    <el-option label="A级（优秀）" value="A" />
                    <el-option label="B级（良好）" value="B" />
                    <el-option label="C级（合格）" value="C" />
                    <el-option label="D级（待改进）" value="D" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-radio-group v-model="formData.status">
                    <el-radio value="active">启用</el-radio>
                    <el-radio value="inactive">禁用</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="付款方式" prop="payment_method">
                  <el-input v-model="formData.payment_method" placeholder="如：预付/月结" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

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
                  <el-input v-model="formData.fax" placeholder="请输入传真" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="地址" prop="address">
              <el-input v-model="formData.address" type="textarea" :rows="3" placeholder="请输入详细地址" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="其他信息" name="other">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="银行账号" prop="bank_account">
                  <el-input v-model="formData.bank_account" placeholder="请输入银行账号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="开户银行" prop="bank_name">
                  <el-input v-model="formData.bank_name" placeholder="请输入开户银行" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="备注" prop="remark">
              <el-input v-model="formData.remark" type="textarea" :rows="4" placeholder="请输入备注信息" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="供应商详情" size="600px" direction="rtl">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="供应商编码">{{ currentRow.supplier_code }}</el-descriptions-item>
          <el-descriptions-item label="供应商名称">{{ currentRow.supplier_name }}</el-descriptions-item>
          <el-descriptions-item label="供应商类型">
            <el-tag :type="getTypeColor(currentRow.supplier_type)" size="small">
              {{ getTypeLabel(currentRow.supplier_type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="供应商评级">
            <el-tag :type="getRatingColor(currentRow.rating)" size="small">
              {{ currentRow.rating }}级
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentRow.status === 'active' ? 'success' : 'danger'" size="small">
              {{ currentRow.status === 'active' ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="付款方式">{{ currentRow.payment_method || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentRow.contact_person || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentRow.contact_phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="邮箱" :span="2">{{ currentRow.email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ currentRow.address || '-' }}</el-descriptions-item>
          <el-descriptions-item label="银行账号">{{ currentRow.bank_account || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开户银行">{{ currentRow.bank_name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentRow.create_time || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentRow.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Plus, Edit, Delete, RefreshLeft, View, OfficeBuilding } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { unwrapPageResponse } from '../../../api'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import type { Supplier, SupplierFormData, SupplierType, SupplierRating, CommonStatus } from '../../../types/erp/basic-data'

const suppliers = ref<Supplier[]>([])
const supplierTotal = ref(0)
const supplierLoading = ref(false)
const supplierPage = ref(1)
const supplierSize = ref(10)
const selectedRows = ref<Supplier[]>([])

const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑供应商' : '新增供应商')
const activeTabSupplier = ref('basic')
const submitLoading = ref(false)
const currentRow = ref<Supplier | null>(null)

const formRef = ref<FormInstance>()
const formData = ref<SupplierFormData>({
  supplier_code: '',
  supplier_name: '',
  supplier_type: '原材料' as SupplierType,
  rating: 'B' as SupplierRating,
  status: 'active' as CommonStatus,
  payment_method: '',
  contact_person: '',
  contact_phone: '',
  email: '',
  fax: '',
  address: '',
  bank_account: '',
  bank_name: '',
  remark: ''
})

const formRules: FormRules = {
  supplier_name: [
    { required: true, message: '请输入供应商名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  supplier_type: [{ required: true, message: '请选择供应商类型', trigger: 'change' }],
  contact_phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

const supplierFilters = [
  { prop: 'code', label: '供应商编码', type: 'input' as 'input', placeholder: '请输入供应商编码' },
  { prop: 'name', label: '供应商名称', type: 'input' as 'input', placeholder: '请输入供应商名称' },
  { prop: 'supplier_type', label: '供应商类型', type: 'select' as 'select', options: [
    { label: '原材料', value: 'raw_material' },
    { label: '半成品', value: 'semi_finished' },
    { label: '成品', value: 'finished' },
    { label: '服务', value: 'service' }
  ]},
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '启用', value: 'active' },
    { label: '禁用', value: 'inactive' }
  ]},
  { prop: 'create_time', label: '创建时间', type: 'daterange' as 'daterange' }
] as any

const supplierColumns = [
  { prop: 'supplier_code', label: '供应商编码', width: 150 },
  { prop: 'supplier_name', label: '供应商名称', width: 200 },
  { prop: 'supplier_type', label: '供应商类型', width: 120, slotName: 'supplier_type' },
  { prop: 'contact_person', label: '联系人', width: 120 },
  { prop: 'contact_phone', label: '联系电话', width: 150 },
  { prop: 'address', label: '地址', minWidth: 200 },
  { prop: 'status', label: '状态', width: 100, slotName: 'status' },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

const supplierActions = [
  { text: '查看', type: 'primary', size: 'small', icon: View, handler: handleView },
  { text: '编辑', type: 'info', size: 'small', icon: Edit, handler: handleEdit },
  { text: '删除', type: 'danger', size: 'small', icon: Delete, handler: handleDelete }
]

const tableActions = [
  { key: 'add', text: '新增供应商', type: 'primary', icon: Plus, handler: handleAdd },
  { key: 'refresh', text: '刷新', icon: RefreshLeft, handler: () => handleSearch() }
]

onMounted(() => {
  handleSearch()
})

const getSupplierList = async (params: any) => {
  try {
    supplierLoading.value = true
    const queryParams = {
      page: params.page ?? supplierPage.value,
      size: params.size ?? supplierSize.value,
      code: params.code,
      name: params.name
    }
    const res = await erpApi.basicData.getSuppliers(queryParams as any)
    const page = unwrapPageResponse<any>(res)
    const list = page.list

    suppliers.value = (list as any[]).map((it: any) => ({
      id: it.id,
      supplier_code: it.supplierCode ?? it.supplier_code ?? '',
      supplier_name: it.supplierName ?? it.supplier_name ?? '',
      supplier_type: it.supplierType ?? it.supplier_type,
      rating: it.rating,
      contact_person: it.contactPerson ?? it.contact_person,
      contact_phone: it.contactPhone ?? it.contact_phone,
      email: it.email,
      fax: it.fax,
      address: it.address,
      payment_method: it.paymentMethod ?? it.payment_method,
      bank_account: it.bankAccount ?? it.bank_account,
      bank_name: it.bankName ?? it.bank_name,
      status: (it.status === 1 || it.status === 'active') ? 'active' : 'inactive',
      remark: it.remark,
      create_time: it.createdTime ?? it.created_time,
      update_time: it.updatedTime ?? it.updated_time
    })) as any

    supplierTotal.value = page.total
    supplierPage.value = page.page || queryParams.page
    supplierSize.value = page.size || queryParams.size
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // API调用失败时使用空数组避免进一步错误
    suppliers.value = []
    supplierTotal.value = 0
  } finally {
    supplierLoading.value = false
  }
}

const handleSearch = (params: any = {}) => getSupplierList(params)
const handleSizeChange = (size: number) => getSupplierList({ page: supplierPage.value, size })
const handleCurrentChange = (page: number) => getSupplierList({ page, size: supplierSize.value })
const handleSelectionChange = (rows: Supplier[]) => { selectedRows.value = rows }

function handleView(row: Supplier) {
  currentRow.value = row
  detailVisible.value = true
}

function handleAdd() {
  isEdit.value = false
  activeTabSupplier.value = 'basic'
  formData.value = {
    supplier_code: '', supplier_name: '', supplier_type: 'raw_material' as SupplierType,
    rating: 'B' as SupplierRating, status: 'active' as CommonStatus, payment_method: '',
    contact_person: '', contact_phone: '', email: '', fax: '', address: '',
    bank_account: '', bank_name: '', remark: ''
  }
  dialogVisible.value = true
}

function handleEdit(row: Supplier) {
  isEdit.value = true
  activeTabSupplier.value = 'basic'
  formData.value = { ...row }
  dialogVisible.value = true
}

function handleDelete(row: Supplier) {
  ElMessageBox.confirm(`确定要删除供应商"${row.supplier_name}"吗？此操作不可撤销。`, '删除确认', 
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await erpApi.basicData.deleteSupplier(row.id!)
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
        supplier_name: formData.value.supplier_name,
        supplier_type: formData.value.supplier_type,
        rating: formData.value.rating,
        contact_person: formData.value.contact_person,
        contact_phone: formData.value.contact_phone,
        email: formData.value.email,
        fax: formData.value.fax,
        address: formData.value.address,
        payment_method: formData.value.payment_method,
        bank_account: formData.value.bank_account,
        bank_name: formData.value.bank_name,
        status: formData.value.status === 'active' ? 1 : 0,
        remark: formData.value.remark
      }
      if (!isEdit.value && formData.value.supplier_code) {
        payload.supplier_code = formData.value.supplier_code
      }
      if (isEdit.value) {
        await erpApi.basicData.updateSupplier(formData.value.id!, payload)
        ElMessage.success('更新成功')
      } else {
        await erpApi.basicData.createSupplier(payload)
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
    'raw_material': '', 'semi_finished': 'success',
    'finished': 'warning', 'service': 'info'
  }
  return map[type] || 'info'
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'raw_material': '原材料', 'semi_finished': '半成品',
    'finished': '成品', 'service': '服务'
  }
  return map[type] || type
}

const getRatingColor = (rating?: string) => {
  if (!rating) return 'info'
  const map: Record<string, string> = {
    'A': 'success', 'B': 'primary', 'C': 'warning', 'D': 'danger'
  }
  return map[rating] || 'info'
}
</script>

<style scoped lang="scss">
.suppliers-view {
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
  .suppliers-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .suppliers-view {
    padding: 12px;
    .module-card .card-header {
      font-size: 18px;
    }
  }
}
</style>
