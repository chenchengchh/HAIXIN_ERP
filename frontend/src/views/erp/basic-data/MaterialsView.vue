<template>
  <div class="materials-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Box /></el-icon>
          <span>物料管理</span>
        </div>
      </template>
      
      <!-- 物料列表 -->
      <TableComponent
        :data="materials"
        :columns="materialColumns"
        :total="materialTotal"
        :loading="materialLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        :actions="materialActions"
        :table-actions="tableActions"
        :filters="materialFilters"
        :show-filter="true"
        @search="handleSearch"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @selection-change="handleSelectionChange"
      >
        <!-- 状态列自定义 -->
        <template #status="{ row }">
          <el-tag
            :type="getStatusType(row.status)"
            size="small"
          >
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
        
        <!-- 审核状态列自定义 -->
        <template #approvalStatus="{ row }">
          <el-tag
            :type="getApprovalStatusType(row.approval_status)"
            size="small"
          >
            {{ getApprovalStatusLabel(row.approval_status) }}
          </el-tag>
        </template>
      </TableComponent>
    </el-card>
    
    <!-- 批量导入对话框 -->
    <DialogComponent
      v-model="importDialogVisible"
      title="批量导入物料"
      width="600px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="导入"
      @confirm="handleImportConfirm"
      @cancel="handleImportCancel"
    >
      <div class="import-dialog">
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          :description="'1. 请下载导入模板，按照模板格式填写数据；\n2. 支持Excel和CSV格式文件；\n3. 导入前请确保数据格式正确'"
          show-icon
          class="import-alert"
        />
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :on-change="handleFileChange"
          :file-list="fileList"
          accept=".xlsx,.csv"
          drag
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            拖拽文件到此处或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip text-center">
              支持上传 .xlsx .csv 格式文件，单个文件不超过 2MB
            </div>
          </template>
        </el-upload>
        <div class="template-download">
          <el-button type="primary" link @click="handleDownloadTemplate">
            下载导入模板
          </el-button>
        </div>
      </div>
    </DialogComponent>
    
    <!-- 批量导出对话框 -->
    <DialogComponent
      v-model="exportDialogVisible"
      title="批量导出物料"
      width="500px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="导出"
      @confirm="handleExportConfirm"
    >
      <div class="export-dialog">
        <el-form :model="exportForm" label-width="100px">
          <el-form-item label="导出格式" prop="format">
            <el-radio-group v-model="exportForm.format">
              <el-radio value="xlsx">Excel (.xlsx)</el-radio>
              <el-radio value="csv">CSV (.csv)</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="导出范围" prop="range">
            <el-radio-group v-model="exportForm.range">
              <el-radio value="all">全部数据</el-radio>
              <el-radio value="selected">选中数据</el-radio>
              <el-radio value="filtered">筛选后数据</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-tabs v-model="activeTabMaterial">
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="物料编码" prop="material_code">
                  <el-input v-model="formData.material_code" placeholder="留空自动生成" :disabled="isEdit" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="物料名称" prop="material_name">
                  <el-input v-model="formData.material_name" placeholder="请输入物料名称" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="物料类型" prop="material_type">
                  <el-select v-model="formData.material_type" placeholder="请选择类型" style="width: 100%">
                    <el-option label="原材料" value="raw_material" />
                    <el-option label="半成品" value="semi_finished" />
                    <el-option label="成品" value="finished" />
                    <el-option label="辅助材料" value="auxiliary" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规格型号" prop="specification">
                  <el-input v-model="formData.specification" placeholder="请输入规格型号" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="计量单位" prop="unit">
                  <el-select v-model="formData.unit" placeholder="请选择单位" style="width: 100%">
                    <el-option label="件" value="piece" />
                    <el-option label="公斤" value="kg" />
                    <el-option label="米" value="m" />
                    <el-option label="平方米" value="m2" />
                    <el-option label="立方米" value="m3" />
                  </el-select>
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
          </el-tab-pane>

          <el-tab-pane label="库存信息" name="inventory">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="安全库存" prop="safety_stock">
                  <el-input-number v-model="formData.safety_stock" :min="0" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最小库存" prop="min_stock">
                  <el-input-number v-model="formData.min_stock" :min="0" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="最大库存" prop="max_stock">
                  <el-input-number v-model="formData.max_stock" :min="0" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="采购提前期" prop="lead_time">
                  <el-input-number v-model="formData.lead_time" :min="0" style="width: 100%">
                    <template #append>天</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
          </el-tab-pane>

          <el-tab-pane label="采购信息" name="purchase">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="默认供应商" prop="default_supplier">
                  <el-input v-model="formData.default_supplier" placeholder="请输入供应商" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="单价" prop="unit_price">
                  <el-input-number v-model="formData.unit_price" :min="0" :precision="2" style="width: 100%">
                    <template #prepend>¥</template>
                  </el-input-number>
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
    <el-drawer v-model="detailVisible" title="物料详情" size="600px" direction="rtl">
      <div v-if="currentRow" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="物料编码">{{ currentRow.material_code }}</el-descriptions-item>
          <el-descriptions-item label="物料名称">{{ currentRow.material_name }}</el-descriptions-item>
          <el-descriptions-item label="物料类型">{{ currentRow.material_type }}</el-descriptions-item>
          <el-descriptions-item label="规格型号">{{ currentRow.specification || '-' }}</el-descriptions-item>
          <el-descriptions-item label="计量单位">{{ currentRow.unit }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentRow.status)" size="small">
              {{ getStatusLabel(currentRow.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="安全库存">{{ currentRow.safety_stock || 0 }}</el-descriptions-item>
          <el-descriptions-item label="最小库存">{{ currentRow.min_stock || 0 }}</el-descriptions-item>
          <el-descriptions-item label="最大库存">{{ currentRow.max_stock || 0 }}</el-descriptions-item>
          <el-descriptions-item label="采购提前期">{{ currentRow.lead_time || 0 }} 天</el-descriptions-item>
          <el-descriptions-item label="默认供应商">{{ currentRow.default_supplier || '-' }}</el-descriptions-item>
          <el-descriptions-item label="单价">¥{{ currentRow.unit_price || 0 }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getApprovalStatusType(currentRow.approval_status)" size="small">
              {{ getApprovalStatusLabel(currentRow.approval_status) }}
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
import { Plus, Edit, Delete, RefreshLeft, View, Check, Upload, Download, UploadFilled, Box } from '@element-plus/icons-vue'
import { unwrapPageResponse } from '../../../api'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { DataTransformer } from '../../../utils/data-transformer'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import type { Material, MaterialFormData, MaterialType, CommonStatus } from '../../../types/erp/basic-data'

// 查看详情
function handleView(row: Material) {
  currentRow.value = row
  detailVisible.value = true
}

// 新增物料
function handleAdd() {
  isEdit.value = false
  activeTabMaterial.value = 'basic'
  formData.value = {
    material_code: '',
    material_name: '',
    material_type: 'raw_material' as MaterialType,
    specification: '',
    unit: 'piece',
    status: 'active' as CommonStatus,
    safety_stock: 0,
    min_stock: 0,
    max_stock: 0,
    lead_time: 0,
    default_supplier: '',
    unit_price: 0,
    remark: ''
  }
  dialogVisible.value = true
}

// 编辑物料
function handleEdit(row: Material) {
  isEdit.value = true
  activeTabMaterial.value = 'basic'
  formData.value = { ...row }
  dialogVisible.value = true
}


// 响应式数据
const materials = ref<Material[]>([])
const materialTotal = ref<number>(0)
const materialLoading = ref<boolean>(false)
const materialPage = ref<number>(1)
const materialSize = ref<number>(10)
const selectedRows = ref<Material[]>([])

// 导入导出相关数据
const importDialogVisible = ref(false)
const exportDialogVisible = ref(false)
const fileList = ref<UploadFile[]>([])
const currentFile = ref<UploadFile | null>(null)

const exportForm = ref({
  format: 'xlsx',
  range: 'all'
})

// 对话框控制
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑物料' : '新增物料')
// 子组件内的activeTab，避免与父组件冲突
const activeTabMaterial = ref('basic')
const submitLoading = ref(false)
const currentRow = ref<Material | null>(null)

// 表单引用和数据
const formRef = ref<FormInstance>()
const formData = ref<MaterialFormData>({
  material_code: '',
  material_name: '',
  material_type: 'raw_material' as MaterialType,
  specification: '',
  unit: 'piece',
  status: 'active' as CommonStatus,
  safety_stock: 0,
  min_stock: 0,
  max_stock: 0,
  lead_time: 0,
  default_supplier: '',
  unit_price: 0,
  remark: ''
})

// 表单验证规则
const formRules: FormRules = {
  material_name: [
    { required: true, message: '请输入物料名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  material_type: [{ required: true, message: '请选择物料类型', trigger: 'change' }],
  unit: [{ required: true, message: '请选择计量单位', trigger: 'change' }]
}

// 物料筛选条件
const materialFilters = [
  { prop: 'code', label: '物料编码', type: 'input' as 'input', placeholder: '请输入物料编码' },
  { prop: 'name', label: '物料名称', type: 'input' as 'input', placeholder: '请输入物料名称' },
  { prop: 'material_type', label: '物料类型', type: 'select' as 'select', options: [
    { label: '原材料', value: 'raw_material' },
    { label: '半成品', value: 'semi_finished' },
    { label: '成品', value: 'finished' },
    { label: '辅助材料', value: 'auxiliary' }
  ]},
  { prop: 'unit', label: '计量单位', type: 'select' as 'select', options: [
    { label: '件', value: 'piece' },
    { label: '公斤', value: 'kg' },
    { label: '米', value: 'm' },
    { label: '平方米', value: 'm2' },
    { label: '立方米', value: 'm3' }
  ]},
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '启用', value: 'active' },
    { label: '禁用', value: 'inactive' }
  ]},
  { prop: 'approval_status', label: '审核状态', type: 'select' as 'select', options: [
    { label: '待审核', value: 'pending' },
    { label: '已审核', value: 'approved' },
    { label: '已拒绝', value: 'rejected' }
  ]},
  { prop: 'create_time', label: '创建时间', type: 'daterange' as 'daterange' }
] as any

// 物料列配置
const materialColumns = [
  { prop: 'material_code', label: '物料编码', width: 150 },
  { prop: 'material_name', label: '物料名称', width: 200 },
  { prop: 'material_type', label: '物料类型', width: 120 },
  { prop: 'specification', label: '规格型号', width: 150 },
  { prop: 'unit', label: '计量单位', width: 100 },
  { prop: 'safety_stock', label: '安全库存', width: 120, align: 'right' },
  { prop: 'status', label: '状态', width: 100, slotName: 'status' },
  { prop: 'approval_status', label: '审核状态', width: 120, slotName: 'approvalStatus' },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

// 物料审核
async function handleMaterialApproval(_row: Material) {
  try {
    await erpApi.basicData.approveMaterial(Number(_row.id))
    ElMessage.success('物料审核成功')
    handleSearch() // 刷新物料列表
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 批量审核
async function handleBatchApproval() {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要审核的物料')
    return
  }
  
  try {
    const ids = selectedRows.value.map(r => Number(r.id)).filter(v => !Number.isNaN(v))
    await erpApi.basicData.batchApproveMaterials(ids)
    ElMessage.success('批量审核成功')
    handleSearch() // 刷新物料列表
    selectedRows.value = [] // 清空选中项
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 物料操作按钮
const materialActions = [
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
    text: '审核',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: handleMaterialApproval,
    visible: (row: Material) => row.approval_status === 'pending'
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
    text: '新增物料',
    type: 'primary',
    icon: Plus,
    handler: handleAdd
  },
  {
    key: 'import',
    text: '批量导入',
    type: 'success',
    icon: Upload,
    handler: () => {
      handleOpenImportDialog()
    }
  },
  {
    key: 'export',
    text: '批量导出',
    type: 'warning',
    icon: Download,
    handler: () => {
      handleOpenExportDialog()
    }
  },
  {
    key: 'batch-approval',
    text: '批量审核',
    type: 'success',
    icon: Check,
    handler: () => {
      handleBatchApproval()
    },
    visible: () => selectedRows.value.length > 0
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => {
      handleSearch()
    }
  }
]

// 初始加载
onMounted(() => {
  handleSearch()
})

// 获取物料列表
async function getMaterialList(params: any) {
  try {
    materialLoading.value = true
    const queryParams = {
      page: params.page ?? materialPage.value,
      size: params.size ?? materialSize.value,
      code: params.code,
      name: params.name
    }
    const res = await erpApi.basicData.getMaterials(queryParams as any)
    const page = unwrapPageResponse<any>(res)
    const list = page.list

    materials.value = (list as any[]).map((it: any) => ({
      id: it.id,
      material_code: it.materialCode ?? it.material_code ?? '',
      material_name: it.materialName ?? it.material_name ?? '',
      material_type: it.materialType ?? it.material_type,
      specification: it.specification,
      unit: it.unit,
      safety_stock: it.safetyStock ?? it.safety_stock,
      min_stock: it.minStock ?? it.min_stock,
      max_stock: it.maxStock ?? it.max_stock,
      lead_time: it.leadTime ?? it.lead_time,
      default_supplier: it.defaultSupplier ?? it.default_supplier,
      unit_price: it.unitPrice ?? it.unit_price,
      status: (it.status === 1 || it.status === 'active') ? 'active' : 'inactive',
      approval_status: it.approvalStatus ?? it.approval_status ?? 'pending',
      remark: it.remark,
      create_time: it.createdTime ?? it.created_time,
      update_time: it.updatedTime ?? it.updated_time
    })) as any

    materialTotal.value = page.total
    materialPage.value = page.page || queryParams.page
    materialSize.value = page.size || queryParams.size
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // API调用失败时使用空数组避免进一步错误
    materials.value = []
    materialTotal.value = 0
  } finally {
    materialLoading.value = false
  }
}


// 搜索物料
function handleSearch(params: any = {}) {
  getMaterialList(params)
}

// 分页大小改变
function handleSizeChange(size: number) {
  materialSize.value = size
  getMaterialList({ page: materialPage.value, size })
}

// 页码改变
function handleCurrentChange(page: number) {
  materialPage.value = page
  getMaterialList({ page, size: materialSize.value })
}

// 选择行改变
function handleSelectionChange(rows: Material[]) {
  selectedRows.value = rows
}

// 物料状态类型
function getStatusType(status?: string) {
  if (!status) return ''
  const statusMap: Record<string, string> = {
    'active': 'success',
    'inactive': 'danger'
  }
  return statusMap[status] || ''
}

// 物料状态标签文本
function getStatusLabel(status?: string) {
  if (!status) return '-'
  const statusMap: Record<string, string> = {
    'active': '启用',
    'inactive': '禁用'
  }
  return statusMap[status] || status
}

// 审核状态类型
function getApprovalStatusType(status?: string) {
  if (!status) return 'warning'
  const statusMap: Record<string, string> = {
    'pending': 'warning',
    'approved': 'success',
    'rejected': 'danger'
  }
  return statusMap[status] || ''
}

// 审核状态标签文本
function getApprovalStatusLabel(status?: string) {
  if (!status) return '待审核'
  const statusMap: Record<string, string> = {
    'pending': '待审核',
    'approved': '已审核',
    'rejected': '已拒绝'
  }
  return statusMap[status] || status
}

// 物料审核已移动到上方以解决引用顺序问题

// 批量审核已移动到上方以解决引用顺序问题

// 打开导入对话框
function handleOpenImportDialog() {
  fileList.value = []
  currentFile.value = null
  importDialogVisible.value = true
}

// 打开导出对话框
function handleOpenExportDialog() {
  exportDialogVisible.value = true
}


// 文件变化处理
const handleFileChange = (file: UploadFile) => {
  fileList.value = [file]
  currentFile.value = file
}

// 确认导入
const handleImportConfirm = async () => {
  if (!currentFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }
  
  try {
    // 这里可以调用API进行文件导入
    ElMessage.success('物料导入成功')
    importDialogVisible.value = false
    handleSearch() // 刷新物料列表
    fileList.value = []
    currentFile.value = null
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 取消导入
const handleImportCancel = () => {
  fileList.value = []
  currentFile.value = null
}

// 下载导入模板
const handleDownloadTemplate = () => {
  // 这里可以调用API下载导入模板
  ElMessage.success('导入模板下载功能将在这里实现')
}

// 确认导出
const handleExportConfirm = async () => {
  try {
    // 这里可以调用API进行文件导出
    ElMessage.success('物料导出成功')
    exportDialogVisible.value = false
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 删除物料
function handleDelete(row: Material) {
  ElMessageBox.confirm(
    `确定要删除物料"${row.material_name}"吗？此操作不可撤销。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await erpApi.basicData.deleteMaterial(row.id!)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error) {
      ErrorHandler.handleApiError(error)
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      submitLoading.value = true
      const payload: any = {
        material_name: formData.value.material_name,
        material_type: formData.value.material_type,
        specification: formData.value.specification,
        unit: formData.value.unit,
        safety_stock: formData.value.safety_stock,
        min_stock: formData.value.min_stock,
        max_stock: formData.value.max_stock,
        lead_time: formData.value.lead_time,
        default_supplier: formData.value.default_supplier,
        unit_price: formData.value.unit_price,
        approval_status: formData.value.approval_status,
        status: formData.value.status === 'active' ? 1 : 0,
        remark: formData.value.remark
      }
      if (!isEdit.value && formData.value.material_code) {
        payload.material_code = formData.value.material_code
      }
      if (isEdit.value) {
        await erpApi.basicData.updateMaterial(formData.value.id!, payload)
        ElMessage.success('更新成功')
      } else {
        await erpApi.basicData.createMaterial(payload)
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
.materials-view {
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
  
  .import-dialog {
    .import-alert {
      margin-bottom: 20px;
    }
    
    .template-download {
      margin-top: 20px;
      text-align: center;
    }
  }
  
  .export-dialog {
    padding: 10px 0;
  }

  .detail-content {
    padding: 20px;
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .materials-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .materials-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}
</style>
