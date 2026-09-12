<template>
  <div class="production-orders-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Operation /></el-icon>
          <span>生产订单管理</span>
        </div>
      </template>
      
      <!-- 生产订单列表 -->
      <TableComponent
        :data="productionOrders"
        :columns="productionOrderColumns"
        :total="productionOrderTotal"
        :loading="productionOrderLoading"
        :show-index="true"
        :show-selection="true"
        :show-action="true"
        action-slot-name="actions"
        :table-actions="tableActions"
        :filters="productionOrderFilters"
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
        <!-- MES 集成状态列（F1 前端配套） -->
        <template #mesStatus="{ row }">
          <el-tag
            :type="getMesStatusType(row.mes_integration_status)"
            size="small"
          >
            {{ getMesStatusLabel(row.mes_integration_status) }}
          </el-tag>
        </template>
        <!-- 操作列：按订单状态条件渲染，审批操作统一在OA审批中心进行 -->
        <template #actions="{ row }">
          <el-button type="primary" size="small" link @click="handleView(row)">
            <el-icon><View /></el-icon>查看
          </el-button>
          <!-- APS来源订单：仅保留MES下发与状态流转 -->
          <template v-if="row.source === 'aps'">
            <el-button type="success" size="small" link @click="handleRelease(row)">
              <el-icon><UploadFilled /></el-icon>下发MES
            </el-button>
          </template>
          <!-- 本地订单：按状态流转 -->
          <template v-else>
            <!-- 草稿：可编辑、提交审批、删除 -->
            <template v-if="row.status === 'draft'">
              <el-button type="primary" size="small" link @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button type="primary" size="small" link @click="handleSubmitApproval(row)">
                <el-icon><Promotion /></el-icon>提交审批
              </el-button>
              <el-button type="danger" size="small" link @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
            <!-- 已审核：可开工 -->
            <el-button
              v-else-if="row.status === 'approved'"
              type="success"
              size="small"
              link
              @click="handleStart(row)"
            >
              <el-icon><VideoPlay /></el-icon>开工
            </el-button>
            <!-- 生产中：可暂停、完成 -->
            <template v-else-if="row.status === 'in_production'">
              <el-button type="warning" size="small" link @click="handlePause(row)">
                <el-icon><VideoPause /></el-icon>暂停
              </el-button>
              <el-button type="success" size="small" link @click="handleComplete(row)">
                <el-icon><CircleCheck /></el-icon>完成
              </el-button>
            </template>
            <!-- 已暂停（后端映射为in_production分支）：恢复 -->
            <el-button
              v-else-if="row.status === 'paused'"
              type="primary"
              size="small"
              link
              @click="handleResume(row)"
            >
              <el-icon><VideoPlay /></el-icon>恢复
            </el-button>
            <!-- 已完成/已取消：可取消（兜底操作） -->
            <el-button
              v-else-if="row.status === 'completed' || row.status === 'cancelled'"
              type="info"
              size="small"
              link
              disabled
            >
              <el-icon><CircleCheck /></el-icon>已归档
            </el-button>
            <!-- 其他状态：取消 -->
            <el-button
              v-else
              type="danger"
              size="small"
              link
              @click="handleCancel(row)"
            >
              <el-icon><CircleClose /></el-icon>取消
            </el-button>
          </template>
        </template>
      </TableComponent>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="订单编号" prop="order_no">
              <el-input v-model="formData.order_no" placeholder="留空自动生成" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品编码" prop="product_code">
              <el-input v-model="formData.product_code" placeholder="请输入产品编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称" prop="product_name">
              <el-input v-model="formData.product_name" placeholder="请输入产品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划数量" prop="planned_qty">
              <el-input-number v-model="formData.planned_qty" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车间" prop="workshop">
              <el-input v-model="formData.workshop" placeholder="请输入车间" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产线" prop="production_line">
              <el-input v-model="formData.production_line" placeholder="请输入产线" />
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
import { computed, ref, onMounted } from 'vue'
import { Operation, Plus, Edit, Delete, RefreshLeft, View, UploadFilled, VideoPlay, VideoPause, CircleCheck, CircleClose, Promotion } from '@element-plus/icons-vue'
import { TableComponent } from '../../../components/base'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../../../stores/auth'
import type { FormInstance, FormRules } from 'element-plus'
import type { ProductionOrder, ProductionOrderStatus } from '../../../types/erp/production'

const authStore = useAuthStore()

// 响应式数据
const productionOrders = ref<ProductionOrder[]>([])
const productionOrderTotal = ref(0)
const productionOrderLoading = ref(false)
const productionOrderPage = ref(1)
const productionOrderSize = ref(10)
const selectedRows = ref<ProductionOrder[]>([])
const currentSource = ref<'local' | 'aps'>('local')

const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = computed(() => (isEdit.value ? '编辑生产订单' : '新增生产订单'))
const submitLoading = ref(false)

const formRef = ref<FormInstance>()
const formData = ref<any>({
  id: undefined,
  order_no: '',
  product_code: '',
  product_name: '',
  planned_qty: 0,
  workshop: '',
  production_line: '',
  status: 'draft' as ProductionOrderStatus,
  remark: ''
})

const formRules: FormRules = {
  product_code: [{ required: true, message: '请输入产品编码', trigger: 'blur' }],
  product_name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }]
}

// 生产订单状态映射
const statusMap: Record<string, { label: string; type: string }> = {
  'draft': { label: '草稿', type: 'warning' },
  'approved': { label: '已审核', type: 'success' },
  'in_production': { label: '生产中', type: 'info' },
  'paused': { label: '已暂停', type: 'warning' },
  'completed': { label: '已完成', type: 'success' },
  'cancelled': { label: '已取消', type: 'danger' },
  'CREATED': { label: 'CREATED', type: 'info' },
  'RUNNING': { label: 'RUNNING', type: 'warning' },
  'RELEASED': { label: 'RELEASED', type: 'success' },
  'STARTED': { label: 'STARTED', type: 'success' },
  'PAUSED': { label: 'PAUSED', type: 'warning' },
  'COMPLETED': { label: 'COMPLETED', type: 'success' },
  'CANCELLED': { label: 'CANCELLED', type: 'danger' }
}

// 生产订单筛选条件
const productionOrderFilters = [
  { prop: 'order_no', label: '订单编号', type: 'input' as 'input', placeholder: '请输入订单编号' },
  { prop: 'product_name', label: '产品名称', type: 'input' as 'input', placeholder: '请输入产品名称' },
  { prop: 'source', label: '数据来源', type: 'select' as 'select', options: [
    { label: '本地', value: 'local' },
    { label: 'APS', value: 'aps' }
  ]},
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '草稿', value: 'draft' },
    { label: '已审核', value: 'approved' },
    { label: '生产中', value: 'in_production' },
    { label: '已完成', value: 'completed' },
    { label: '已取消', value: 'cancelled' },
    { label: 'RELEASED', value: 'released' },
    { label: 'STARTED', value: 'started' },
    { label: 'PAUSED', value: 'paused' },
    { label: 'COMPLETED', value: 'completed' },
    { label: 'CANCELLED', value: 'cancelled' }
  ]},
  { prop: 'create_time', label: '创建时间', type: 'daterange' as 'daterange' }
] as any

// 生产订单列配置
const productionOrderColumns = [
  { prop: 'source', label: '来源', width: 90 },
  { prop: 'order_no', label: '订单编号', width: 150 },
  { prop: 'product_name', label: '产品名称', width: 200 },
  { prop: 'planned_qty', label: '计划数量', width: 100, align: 'right' },
  { prop: 'actual_qty', label: '实际数量', width: 100, align: 'right' },
  { prop: 'start_date', label: '计划开始日期', width: 150 },
  { prop: 'end_date', label: '计划结束日期', width: 150 },
  { prop: 'status', label: '状态', width: 100, slotName: 'status' },
  { prop: 'mes_integration_status', label: 'MES集成', width: 110, slotName: 'mesStatus' },
  { prop: 'creator', label: '创建人', width: 100 },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

// 生产订单操作按钮已改为模板内条件渲染（见 #actions 插槽），此处仅保留表格级操作

// 表格操作按钮
const tableActions = [
  {
    key: 'add',
    text: '新增生产订单',
    type: 'primary',
    icon: Plus,
    handler: () => {
      if (currentSource.value !== 'local') {
        ElMessage.warning('APS来源订单不支持在ERP侧新增')
        return
      }
      handleAdd()
    }
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

// 获取生产订单列表
const getProductionOrderList = async (params: any) => {
  try {
    productionOrderLoading.value = true
    const result = await erpApi.production.getProductionOrders({
      page: params.page || productionOrderPage.value,
      size: params.size || productionOrderSize.value,
      source: params.source || currentSource.value,
      ...params
    })
    
    const normalizedData = (result as any).data
    const data = normalizedData?.data?.list || []
    productionOrders.value = Array.isArray(data) ? data : []
    productionOrderTotal.value = Number(normalizedData?.data?.total) || 0
    productionOrderPage.value = params.page || productionOrderPage.value
    productionOrderSize.value = params.size || productionOrderSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // 显示虚拟数据或空数据
    productionOrders.value = []
    productionOrderTotal.value = 0
  } finally {
    productionOrderLoading.value = false
  }
}

// 搜索生产订单
const handleSearch = (params: any = {}) => {
  const nextSource = (params.source || currentSource.value || 'local') as any
  currentSource.value = nextSource === 'aps' ? 'aps' : 'local'
  getProductionOrderList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  productionOrderSize.value = size
  getProductionOrderList({ page: productionOrderPage.value, size, source: currentSource.value })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  productionOrderPage.value = page
  getProductionOrderList({ page, size: productionOrderSize.value, source: currentSource.value })
}

/**
 * 提交生产订单审批（走OA统一审批流）。
 * <p>仅草稿(draft)状态允许提交，审批操作在OA审批中心进行，
 * 审批完成后OA回调自动更新订单状态为approved/draft。</p>
 * @param row 生产订单行数据
 */
const handleSubmitApproval = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确认提交生产订单【${row.order_no}】到OA审批中心？审批通过后订单将变为"已审核"状态，方可开工。`,
      '提交审批',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    const userInfo = authStore.userInfo as any
    const res: any = await erpApi.production.submitOrderApproval(
      Number(row.id),
      Number(userInfo?.id) || undefined,
      userInfo?.name || userInfo?.username || undefined
    )
    const submitted = res?.data?.approvalSubmitted !== false
    ElMessage.success(submitted ? '已提交OA审批，请前往 OA > 审批中心 查看进度' : '审批提交失败（OA服务不可用），请稍后重试')
    handleSearch({ source: currentSource.value })
  } catch (error) {
    if (String(error).includes('cancel')) return
    ErrorHandler.handleApiError(error)
  }
}

const handleRelease = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认下发该APS订单到MES生成工单？', '下发确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await erpApi.production.releaseProductionOrderToMes(Number(row.id))
    ElMessage.success('下发成功')
    handleSearch({ source: currentSource.value })
  } catch (e) {
    if (String(e).includes('cancel')) return
    ErrorHandler.handleApiError(e)
  }
}

const handleStart = async (row: any) => {
  try {
    await erpApi.production.startProductionOrder(Number(row.id), row.source === 'aps' ? 'aps' : undefined)
    ElMessage.success('开工成功')
    handleSearch({ source: currentSource.value })
  } catch (e) {
    ErrorHandler.handleApiError(e)
  }
}

const handlePause = async (row: any) => {
  try {
    await erpApi.production.pauseProductionOrder(Number(row.id), row.source === 'aps' ? 'aps' : undefined)
    ElMessage.success('暂停成功')
    handleSearch({ source: currentSource.value })
  } catch (e) {
    ErrorHandler.handleApiError(e)
  }
}

const handleResume = async (row: any) => {
  try {
    await erpApi.production.resumeProductionOrder(Number(row.id), row.source === 'aps' ? 'aps' : undefined)
    ElMessage.success('恢复成功')
    handleSearch({ source: currentSource.value })
  } catch (e) {
    ErrorHandler.handleApiError(e)
  }
}

const handleComplete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认完成该订单？', '完成确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await erpApi.production.completeProductionOrder(Number(row.id), row.source === 'aps' ? 'aps' : undefined)
    ElMessage.success('完成成功')
    handleSearch({ source: currentSource.value })
  } catch (e) {
    if (String(e).includes('cancel')) return
    ErrorHandler.handleApiError(e)
  }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '取消确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await erpApi.production.cancelProductionOrder(Number(row.id), row.source === 'aps' ? 'aps' : undefined)
    ElMessage.success('取消成功')
    handleSearch({ source: currentSource.value })
  } catch (e) {
    if (String(e).includes('cancel')) return
    ErrorHandler.handleApiError(e)
  }
}

// 选择行改变
const handleSelectionChange = (rows: ProductionOrder[]) => {
  selectedRows.value = rows
}

// 状态标签类型
const getStatusType = (status: string) => {
  return statusMap[status]?.type || 'info'
}

// 状态标签文本
const getStatusLabel = (status: string) => {
  return statusMap[status]?.label || status
}

/**
 * MES 集成状态映射（F1 前端配套）。
 * 状态流转：NULL/NOT_SENT（未推送）→ PENDING（已入队）→ CONFIRMED（MES已接收）
 */
const mesStatusMap: Record<string, { type: 'info' | 'warning' | 'success'; label: string }> = {
  NOT_SENT: { type: 'info', label: '未推送' },
  PENDING: { type: 'warning', label: '推送中' },
  CONFIRMED: { type: 'success', label: '已接收' }
}

/** 获取 MES 集成状态标签类型 */
const getMesStatusType = (status: string) => {
  if (!status) return 'info'
  return mesStatusMap[status]?.type || 'info'
}

/** 获取 MES 集成状态标签文本 */
const getMesStatusLabel = (status: string) => {
  if (!status) return '未推送'
  return mesStatusMap[status]?.label || status
}

const handleAdd = () => {
  isEdit.value = false
  formData.value = { id: undefined, order_no: '', product_code: '', product_name: '', planned_qty: 0, workshop: '', production_line: '', status: 'draft', remark: '' }
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleView = (row: any) => {
  ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, '生产订单详情', { dangerouslyUseHTMLString: true })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除生产订单"${row.order_no}"吗？`, '删除确认',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await erpApi.production.deleteProductionOrder(Number(row.id))
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
        order_no: formData.value.order_no,
        product_code: formData.value.product_code,
        product_name: formData.value.product_name,
        planned_qty: formData.value.planned_qty,
        workshop: formData.value.workshop,
        production_line: formData.value.production_line,
        remark: formData.value.remark
      }
      // 仅新增时设置初始状态为草稿；编辑不传status，状态由审批流/生产流转驱动，避免绕过审批
      if (!isEdit.value) {
        payload.status = 'draft'
      }
      if (isEdit.value && formData.value.id) {
        await erpApi.production.updateProductionOrder(Number(formData.value.id), payload)
        ElMessage.success('更新成功')
      } else {
        await erpApi.production.createProductionOrder(payload)
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
.production-orders-view {
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

// 响应式设计
@media (max-width: 1024px) {
  .production-orders-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .production-orders-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}
</style>
