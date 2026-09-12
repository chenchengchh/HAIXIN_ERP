<template>
  <div class="workshop-view">
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><Operation /></el-icon>
          <span>车间管理</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" type="card" class="workshop-tabs">
        <!-- 工序工单标签页 -->
        <el-tab-pane label="工序工单" name="workshop-orders">
          <div class="tab-content">
            <!-- 工序工单列表 -->
            <TableComponent
              :data="workshopOrders"
              :columns="workshopOrderColumns"
              :total="workshopOrderTotal"
              :loading="workshopOrderLoading"
              :show-index="true"
              :show-selection="true"
              :show-action="true"
              :actions="workshopOrderActions"
              :table-actions="workshopOrderTableActions"
              :filters="workshopOrderFilters"
              :show-filter="true"
              @search="handleWorkshopOrderSearch"
              @size-change="handleWorkshopOrderSizeChange"
              @current-change="handleWorkshopOrderCurrentChange"
              @selection-change="handleWorkshopOrderSelectionChange"
            >
              <!-- 工单状态列自定义 -->
              <template #orderStatus="{ row }">
                <el-tag
                  :type="getWorkshopOrderStatusType(row.status)"
                  size="small"
                >
                  {{ getWorkshopOrderStatusLabel(row.status) }}
                </el-tag>
              </template>
            </TableComponent>
          </div>
        </el-tab-pane>
        
        <!-- 生产报工标签页 -->
        <el-tab-pane label="生产报工" name="production-reports">
          <div class="tab-content">
            <!-- 生产报工列表 -->
            <TableComponent
              :data="productionReports"
              :columns="productionReportColumns"
              :total="productionReportTotal"
              :loading="productionReportLoading"
              :show-index="true"
              :show-selection="true"
              :show-action="true"
              :actions="productionReportActions"
              :table-actions="productionReportTableActions"
              :filters="productionReportFilters"
              :show-filter="true"
              @search="handleProductionReportSearch"
              @size-change="handleProductionReportSizeChange"
              @current-change="handleProductionReportCurrentChange"
              @selection-change="handleProductionReportSelectionChange"
            >
              <!-- 报工状态列自定义 -->
              <template #reportStatus="{ row }">
                <el-tag
                  :type="getProductionReportStatusType(row.status)"
                  size="small"
                >
                  {{ getProductionReportStatusLabel(row.status) }}
                </el-tag>
              </template>
            </TableComponent>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 生产报工对话框 -->
    <DialogComponent
      v-model="reportFormVisible"
      title="生产报工"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      @confirm="handleReportSave"
    >
      <div class="report-form">
        <el-form
          ref="reportFormRef"
          :model="reportForm"
          label-width="120px"
        >
          <el-form-item label="报工单号" prop="report_no">
            <el-input v-model="reportForm.report_no" placeholder="自动生成" disabled />
          </el-form-item>
          <el-form-item label="工序工单" prop="workshop_order_id" required>
            <el-select
              v-model="reportForm.workshop_order_id"
              placeholder="选择工序工单"
              style="width: 100%"
            >
              <el-option
                v-for="order in workshopOrderList"
                :key="order.id"
                :label="order.order_no"
                :value="order.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="报工日期" prop="report_date" required>
            <el-date-picker
              v-model="reportForm.report_date"
              type="date"
              placeholder="选择报工日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="计划数量" prop="planned_qty" required>
                <el-input-number
                  v-model="reportForm.planned_qty"
                  :min="1"
                  :step="1"
                  placeholder="输入计划数量"
                  style="width: 100%"
                  disabled
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="实际数量" prop="actual_qty" required>
                <el-input-number
                  v-model="reportForm.actual_qty"
                  :min="1"
                  :step="1"
                  placeholder="输入实际数量"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="合格数量" prop="qualified_qty" required>
                <el-input-number
                  v-model="reportForm.qualified_qty"
                  :min="0"
                  :step="1"
                  placeholder="输入合格数量"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="不合格数量" prop="unqualified_qty" required>
                <el-input-number
                  v-model="reportForm.unqualified_qty"
                  :min="0"
                  :step="1"
                  placeholder="输入不合格数量"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="报工人员" prop="reporter">
            <el-input v-model="reportForm.reporter" placeholder="输入报工人员" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
          <el-input
            v-model="reportForm.remark"
            type="textarea"
            :rows="3"
            placeholder="输入备注信息"
          />
        </el-form-item>
        </el-form>
      </div>
    </DialogComponent>

    <DialogComponent
      v-model="batchUploadVisible"
      title="批量上传报工"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="上传"
      :confirm-loading="batchUploading"
      @confirm="handleBatchUploadSubmit"
    >
      <el-alert
        title="请上传Excel文件（第一行表头），支持列名：工序工单/工单号、报工日期、计划数量、实际数量、合格数量、不合格数量、操作人、备注"
        type="info"
        :closable="false"
        class="mb-12"
      />
      <el-upload
        :auto-upload="false"
        :show-file-list="false"
        :before-upload="handleBatchUploadBeforeUpload"
        accept=".xlsx,.xls,.csv"
      >
        <el-button type="primary" :icon="Upload">选择文件</el-button>
        <span v-if="batchUploadFileName" class="ml-10">{{ batchUploadFileName }}</span>
      </el-upload>

      <el-divider />

      <div v-if="batchUploadRows.length === 0" class="mt-10">
        <el-empty description="尚未解析到数据" />
      </div>
      <div v-else>
        <el-table :data="batchUploadRows" border stripe height="320px">
          <el-table-column prop="workshop_order" label="工序工单" width="180" />
          <el-table-column prop="report_date" label="报工日期" width="120" />
          <el-table-column prop="planned_qty" label="计划数量" width="120" align="right" />
          <el-table-column prop="actual_qty" label="实际数量" width="120" align="right" />
          <el-table-column prop="qualified_qty" label="合格数量" width="120" align="right" />
          <el-table-column prop="unqualified_qty" label="不合格数量" width="120" align="right" />
          <el-table-column prop="reporter" label="操作人" width="120" />
          <el-table-column prop="remark" label="备注" min-width="200" />
        </el-table>
      </div>
    </DialogComponent>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Operation, Edit, RefreshLeft, View, Check, Upload } from '@element-plus/icons-vue'
import { TableComponent, DialogComponent } from '../../../components/base'
import { unwrapListResponse, unwrapPageResponse } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import type { WorkshopOrder, ProductionReport, WorkshopOrderStatus, ProductionReportStatus } from '../../../types/erp/production'

// 响应式数据
const activeTab = ref('workshop-orders')

// 工序工单相关数据
const workshopOrders = ref<WorkshopOrder[]>([])
const workshopOrderTotal = ref(0)
const workshopOrderLoading = ref(false)
const workshopOrderPage = ref(1)
const workshopOrderSize = ref(10)
const workshopOrderSelectedRows = ref<WorkshopOrder[]>([])
const workshopOrderList = ref<WorkshopOrder[]>([])

// 生产报工相关数据
const productionReports = ref<ProductionReport[]>([])
const productionReportTotal = ref(0)
const productionReportLoading = ref(false)
const productionReportPage = ref(1)
const productionReportSize = ref(10)
const productionReportSelectedRows = ref<ProductionReport[]>([])

// 生产报工表单数据
const reportFormVisible = ref(false)
const reportFormRef = ref<any>(null)
const reportEditId = ref<number | null>(null)
const reportForm = ref({
  report_no: '',
  workshop_order_id: '',
  report_date: new Date(),
  planned_qty: 0,
  actual_qty: 0,
  qualified_qty: 0,
  unqualified_qty: 0,
  reporter: '',
  remark: ''
})

const batchUploadVisible = ref(false)
const batchUploading = ref(false)
const batchUploadFileName = ref('')
const batchUploadRows = ref<any[]>([])

// 工序工单状态映射
const workshopOrderStatusMap: Record<string, { label: string; type: string }> = {
  'pending': { label: '待处理', type: 'warning' },
  'in_progress': { label: '执行中', type: 'info' },
  'completed': { label: '已完成', type: 'success' },
  'cancelled': { label: '已取消', type: 'danger' }
}

// 生产报工状态映射
const productionReportStatusMap: Record<string, { label: string; type: string }> = {
  'draft': { label: '草稿', type: 'warning' },
  'submitted': { label: '已提交', type: 'info' },
  'approved': { label: '已审核', type: 'success' },
  'rejected': { label: '已拒绝', type: 'danger' }
}

// 工序工单筛选条件
const workshopOrderFilters = [
  { prop: 'order_no', label: '工单编号', type: 'input' as 'input', placeholder: '请输入工单编号' },
  { prop: 'production_order_no', label: '生产订单号', type: 'input' as 'input', placeholder: '请输入生产订单号' },
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '待处理', value: 'pending' },
    { label: '执行中', value: 'in_progress' },
    { label: '已完成', value: 'completed' },
    { label: '已取消', value: 'cancelled' }
  ]},
  { prop: 'create_time', label: '创建时间', type: 'daterange' as 'daterange' }
] as any

// 工序工单列配置
const workshopOrderColumns = [
  { prop: 'order_no', label: '工单编号', width: 150 },
  { prop: 'production_order_no', label: '生产订单号', width: 150 },
  { prop: 'product_name', label: '产品名称', width: 200 },
  { prop: 'process_name', label: '工序名称', width: 150 },
  { prop: 'planned_qty', label: '计划数量', width: 100, align: 'right' },
  { prop: 'completed_qty', label: '完成数量', width: 100, align: 'right' },
  { prop: 'status', label: '状态', width: 120, slotName: 'orderStatus' },
  { prop: 'start_date', label: '计划开始日期', width: 150 },
  { prop: 'end_date', label: '计划结束日期', width: 150 },
  { prop: 'remark', label: '备注', minWidth: 200 }
]

// 工序工单操作按钮
const workshopOrderActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, '工序工单详情', { dangerouslyUseHTMLString: true })
    }
  },
  {
    text: '报工',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      handleCreateReport(row)
    }
  }
]

// 工序工单表格操作按钮
const workshopOrderTableActions = [
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => {
      handleWorkshopOrderSearch()
    }
  }
]

// 生产报工筛选条件
const productionReportFilters = [
  { prop: 'report_no', label: '报工单号', type: 'input' as 'input', placeholder: '请输入报工单号' },
  { prop: 'workshop_order_no', label: '工单编号', type: 'input' as 'input', placeholder: '请输入工单编号' },
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '草稿', value: 'draft' },
    { label: '已提交', value: 'submitted' },
    { label: '已审核', value: 'approved' },
    { label: '已拒绝', value: 'rejected' }
  ]},
  { prop: 'report_date', label: '报工日期', type: 'daterange' as 'daterange' }
] as any

// 生产报工列配置
const productionReportColumns = [
  { prop: 'report_no', label: '报工单号', width: 150 },
  { prop: 'workshop_order_no', label: '工单编号', width: 150 },
  { prop: 'production_order_no', label: '生产订单号', width: 150 },
  { prop: 'product_name', label: '产品名称', width: 200 },
  { prop: 'process_name', label: '工序名称', width: 150 },
  { prop: 'report_date', label: '报工日期', width: 150 },
  { prop: 'actual_qty', label: '实际数量', width: 100, align: 'right' },
  { prop: 'qualified_qty', label: '合格数量', width: 100, align: 'right' },
  { prop: 'unqualified_qty', label: '不合格数量', width: 100, align: 'right' },
  { prop: 'reporter', label: '报工人员', width: 100 },
  { prop: 'status', label: '状态', width: 120, slotName: 'reportStatus' },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

// 生产报工操作按钮
const productionReportActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      ElMessageBox.alert(`<pre>${JSON.stringify(row, null, 2)}</pre>`, '生产报工详情', { dangerouslyUseHTMLString: true })
    }
  },
  {
    text: '编辑',
    type: 'info',
    size: 'small',
    icon: Edit,
    handler: (row: any) => {
      reportEditId.value = Number(row.id)
      reportForm.value = {
        report_no: row.report_no,
        workshop_order_id: row.workshop_order_no,
        report_date: row.report_date ? new Date(row.report_date) : new Date(),
        planned_qty: row.planned_qty ?? 0,
        actual_qty: row.actual_qty ?? 0,
        qualified_qty: row.qualified_qty ?? 0,
        unqualified_qty: row.unqualified_qty ?? 0,
        reporter: row.reporter ?? '',
        remark: row.remark ?? ''
      }
      reportFormVisible.value = true
    }
  },
  {
    text: '审核',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      ElMessageBox.confirm(`确认审核报工单"${row.report_no}"吗？`, '审核确认',
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      ).then(async () => {
        try {
          await erpApi.production.updateProductionReport(Number(row.id), { status: 'approved' } as any)
          ElMessage.success('审核成功')
          handleProductionReportSearch()
        } catch (e) {
          ErrorHandler.handleApiError(e)
        }
      }).catch(() => {})
    }
  }
]

// 生产报工表格操作按钮
const productionReportTableActions = [
  {
    key: 'batch-upload',
    text: '批量上传',
    type: 'primary',
    icon: Upload,
    handler: () => {
      batchUploadVisible.value = true
      batchUploadRows.value = []
      batchUploadFileName.value = ''
    }
  },
  {
    key: 'refresh',
    text: '刷新',
    icon: RefreshLeft,
    handler: () => {
      handleProductionReportSearch()
    }
  }
]

// 初始加载
onMounted(() => {
  handleWorkshopOrderSearch()
  handleProductionReportSearch()
  getWorkshopOrdersForSelect()
})

const handleBatchUploadBeforeUpload = async (file: File) => {
  try {
    batchUploadFileName.value = file.name
    const buffer = await file.arrayBuffer()
    const workbook = XLSX.read(buffer, { type: 'array' })
    const sheetName = workbook.SheetNames[0]
    if (!sheetName) {
      ElMessage.warning('未找到工作表')
      batchUploadRows.value = []
      return false
    }
    const sheet = workbook.Sheets[sheetName]
    if (!sheet) {
      ElMessage.warning('未找到工作表内容')
      batchUploadRows.value = []
      return false
    }
    const rows = XLSX.utils.sheet_to_json<any[]>(sheet, { header: 1, blankrows: false })
    const parsed = parseBatchUploadRows(rows)
    batchUploadRows.value = parsed
    if (parsed.length === 0) {
      ElMessage.warning('未解析到有效数据')
    } else {
      ElMessage.success(`已解析 ${parsed.length} 条报工记录`)
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
    batchUploadRows.value = []
  }
  return false
}

const parseBatchUploadRows = (rows: any[]) => {
  if (!Array.isArray(rows) || rows.length < 2) return []
  const header = (rows[0] || []).map((h: any) => String(h || '').trim())
  const idx = (name: string) => header.findIndex((h: string) => h === name)
  const idxs = {
    workshopOrder: idx('工序工单') >= 0 ? idx('工序工单') : idx('工单号'),
    reportDate: idx('报工日期'),
    plannedQty: idx('计划数量'),
    actualQty: idx('实际数量'),
    qualifiedQty: idx('合格数量'),
    unqualifiedQty: idx('不合格数量'),
    reporter: idx('操作人'),
    remark: idx('备注')
  }

  const out: any[] = []
  for (let i = 1; i < rows.length; i++) {
    const r = rows[i] || []
    const workshopOrder = idxs.workshopOrder >= 0 ? String(r[idxs.workshopOrder] || '').trim() : ''
    if (!workshopOrder) continue

    const reportDate = idxs.reportDate >= 0 ? String(r[idxs.reportDate] || '').trim() : ''
    out.push({
      workshop_order: workshopOrder,
      report_date: reportDate || new Date().toISOString().slice(0, 10),
      planned_qty: idxs.plannedQty >= 0 ? Number(r[idxs.plannedQty] || 0) : 0,
      actual_qty: idxs.actualQty >= 0 ? Number(r[idxs.actualQty] || 0) : 0,
      qualified_qty: idxs.qualifiedQty >= 0 ? Number(r[idxs.qualifiedQty] || 0) : 0,
      unqualified_qty: idxs.unqualifiedQty >= 0 ? Number(r[idxs.unqualifiedQty] || 0) : 0,
      reporter: idxs.reporter >= 0 ? String(r[idxs.reporter] || '').trim() : '',
      remark: idxs.remark >= 0 ? String(r[idxs.remark] || '').trim() : ''
    })
  }
  return out
}

const resolveWorkshopOrderId = (workshopOrder: string) => {
  if (!workshopOrder) return ''
  const matchedByNo = workshopOrderList.value.find(o => String(o.order_no) === workshopOrder)
  if (matchedByNo?.id) return String(matchedByNo.id)
  const matchedById = workshopOrderList.value.find(o => String(o.id) === workshopOrder)
  if (matchedById?.id) return String(matchedById.id)
  return ''
}

const handleBatchUploadSubmit = async () => {
  if (batchUploadRows.value.length === 0) {
    ElMessage.warning('请先选择并解析文件')
    return
  }
  try {
    batchUploading.value = true
    const errors: string[] = []
    let successCount = 0
    for (let i = 0; i < batchUploadRows.value.length; i++) {
      const row = batchUploadRows.value[i]
      const workshopOrderId = resolveWorkshopOrderId(row.workshop_order)
      if (!workshopOrderId) {
        errors.push(`第${i + 1}行：工序工单不存在或未同步：${row.workshop_order}`)
        continue
      }
      const payload: any = {
        report_no: '',
        workshop_order_id: workshopOrderId,
        report_date: row.report_date,
        planned_qty: row.planned_qty || 0,
        actual_qty: row.actual_qty || 0,
        qualified_qty: row.qualified_qty || 0,
        unqualified_qty: row.unqualified_qty || 0,
        reporter: row.reporter || '',
        remark: row.remark || ''
      }
      try {
        await erpApi.production.createProductionReport(payload)
        successCount++
      } catch (e) {
        errors.push(`第${i + 1}行：上传失败：${row.workshop_order}`)
      }
    }

    if (errors.length > 0) {
      ElMessage.warning(`上传完成：成功 ${successCount} 条，失败 ${errors.length} 条`)
    } else {
      ElMessage.success(`上传完成：成功 ${successCount} 条`)
    }

    batchUploadVisible.value = false
    handleProductionReportSearch()
  } finally {
    batchUploading.value = false
  }
}

// 获取工序工单列表
const getWorkshopOrderList = async (params: any) => {
  try {
    workshopOrderLoading.value = true
    const result = await erpApi.production.getWorkshopOrders({
      page: params.page || workshopOrderPage.value,
      size: params.size || workshopOrderSize.value,
      ...params
    })
    const page = unwrapPageResponse<WorkshopOrder>(result)
    workshopOrders.value = page.list
    workshopOrderTotal.value = page.total || page.list.length || 0
    workshopOrderPage.value = params.page || workshopOrderPage.value
    workshopOrderSize.value = params.size || workshopOrderSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // 显示虚拟数据或空数据
    workshopOrders.value = []
    workshopOrderTotal.value = 0
  } finally {
    workshopOrderLoading.value = false
  }
}

// 搜索工序工单
const handleWorkshopOrderSearch = (params: any = {}) => {
  getWorkshopOrderList(params)
}

// 工序工单分页大小改变
const handleWorkshopOrderSizeChange = (size: number) => {
  workshopOrderSize.value = size
  getWorkshopOrderList({ page: workshopOrderPage.value, size })
}

// 工序工单页码改变
const handleWorkshopOrderCurrentChange = (page: number) => {
  workshopOrderPage.value = page
  getWorkshopOrderList({ page, size: workshopOrderSize.value })
}

// 工序工单选择行改变
const handleWorkshopOrderSelectionChange = (rows: WorkshopOrder[]) => {
  workshopOrderSelectedRows.value = rows
}

// 获取生产报工列表
const getProductionReportList = async (params: any) => {
  try {
    productionReportLoading.value = true
    const result = await erpApi.production.getProductionReports({
      page: params.page || productionReportPage.value,
      size: params.size || productionReportSize.value,
      ...params
    })
    const page = unwrapPageResponse<ProductionReport>(result)
    productionReports.value = page.list
    productionReportTotal.value = page.total || page.list.length || 0
    productionReportPage.value = params.page || productionReportPage.value
    productionReportSize.value = params.size || productionReportSize.value
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // 显示虚拟数据或空数据
    productionReports.value = []
    productionReportTotal.value = 0
  } finally {
    productionReportLoading.value = false
  }
}

// 搜索生产报工
const handleProductionReportSearch = (params: any = {}) => {
  getProductionReportList(params)
}

// 生产报工分页大小改变
const handleProductionReportSizeChange = (size: number) => {
  productionReportSize.value = size
  getProductionReportList({ page: productionReportPage.value, size })
}

// 生产报工页码改变
const handleProductionReportCurrentChange = (page: number) => {
  productionReportPage.value = page
  getProductionReportList({ page, size: productionReportSize.value })
}

// 生产报工选择行改变
const handleProductionReportSelectionChange = (rows: ProductionReport[]) => {
  productionReportSelectedRows.value = rows
}

// 获取工序工单下拉列表
const getWorkshopOrdersForSelect = async () => {
  try {
    const result = await erpApi.production.getWorkshopOrders({ page: 1, size: 100 })
    workshopOrderList.value = unwrapListResponse<WorkshopOrder>(result)
  } catch (error) {
    ErrorHandler.handleApiError(error)
    // 显示虚拟数据或空数据
    workshopOrderList.value = []
  }
}

// 打开报工对话框
const handleCreateReport = (row: any) => {
  // 初始化表单数据
  reportEditId.value = null
  reportForm.value = {
    report_no: '',
    workshop_order_id: row.id,
    report_date: new Date(),
    planned_qty: row.planned_qty,
    actual_qty: row.planned_qty - (row.completed_qty || 0),
    qualified_qty: row.planned_qty - (row.completed_qty || 0),
    unqualified_qty: 0,
    reporter: '',
    remark: ''
  }
  reportFormVisible.value = true
}

// 保存生产报工
const handleReportSave = async () => {
  if (!reportFormRef.value) return
  
  try {
    await reportFormRef.value.validate()
    const payload: any = { ...reportForm.value }
    const result = reportEditId.value
      ? await erpApi.production.updateProductionReport(reportEditId.value, payload)
      : await erpApi.production.createProductionReport(payload)
    if (result) {
      reportFormVisible.value = false
      reportEditId.value = null
      ElMessage.success('生产报工保存成功')
      handleProductionReportSearch() // 刷新生产报工列表
      handleWorkshopOrderSearch() // 刷新工序工单列表
    }
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 工序工单状态标签类型
const getWorkshopOrderStatusType = (status: string) => {
  return workshopOrderStatusMap[status]?.type || 'info'
}

// 工序工单状态标签文本
const getWorkshopOrderStatusLabel = (status: string) => {
  return workshopOrderStatusMap[status]?.label || status
}

// 生产报工状态标签类型
const getProductionReportStatusType = (status: string) => {
  return productionReportStatusMap[status]?.type || 'info'
}

// 生产报工状态标签文本
const getProductionReportStatusLabel = (status: string) => {
  return productionReportStatusMap[status]?.label || status
}
</script>

<style scoped lang="scss">
.workshop-view {
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
  
  .workshop-tabs {
    margin-top: 20px;
  }
  
  .tab-content {
    padding: 20px 0;
  }
  
  .report-form {
    padding: 10px 0;
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .workshop-view {
    padding: 16px;
    
    .tab-content {
      padding: 16px 0;
    }
  }
}

@media (max-width: 768px) {
  .workshop-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}
</style>
