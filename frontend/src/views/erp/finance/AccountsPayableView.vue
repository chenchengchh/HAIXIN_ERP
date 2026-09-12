<template>
  <div class="accounts-payable-view">
    <!-- 数据概览卡片 -->
    <div class="overview-cards">
      <el-card shadow="hover" class="overview-card">
        <div class="overview-item">
          <div class="item-title">应付总额</div>
          <div class="item-value">{{ overviewData.totalAmount }}</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="overview-card">
        <div class="overview-item">
          <div class="item-title">已付金额</div>
          <div class="item-value">{{ overviewData.paidAmount }}</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="overview-card">
        <div class="overview-item">
          <div class="item-title">未付金额</div>
          <div class="item-value">{{ overviewData.unpaidAmount }}</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="overview-card">
        <div class="overview-item">
          <div class="item-title">逾期金额</div>
          <div class="item-value">{{ overviewData.overdueAmount }}</div>
        </div>
      </el-card>
    </div>
    
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><DocumentChecked /></el-icon>
          <span>应付管理</span>
          <el-tabs v-model="activeTab" class="ml-auto" @tab-change="handleTabChange">
            <el-tab-pane label="应付款列表" name="list"></el-tab-pane>
            <el-tab-pane label="付款计划" name="plan"></el-tab-pane>
            <el-tab-pane label="账龄分析" name="aging"></el-tab-pane>
            <el-tab-pane label="供应商对账" name="reconciliation"></el-tab-pane>
          </el-tabs>
        </div>
      </template>
      
      <!-- 应付款列表 -->
      <div v-if="activeTab === 'list'">
        <TableComponent
          :data="accountsPayableList"
          :columns="accountsPayableColumns"
          :total="accountsPayableTotal"
          :loading="accountsPayableLoading"
          :show-index="true"
          :show-selection="true"
          :show-action="true"
          :actions="accountsPayableActions"
          :table-actions="tableActions"
          :filters="accountsPayableFilters"
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
            <el-tag
              v-if="isOverdue(row)"
              type="danger"
              size="small"
              class="ml-10"
            >
              逾期 {{ getOverdueDays(row) }} 天
            </el-tag>
          </template>
        </TableComponent>
      </div>
      
      <!-- 付款计划 -->
      <div v-else-if="activeTab === 'plan'" class="report-section">
        <div class="report-header">
          <h3>付款计划</h3>
          <el-button type="primary" @click="refreshPaymentPlan">刷新数据</el-button>
        </div>
        <div class="plan-report">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">未来30天付款计划</div>
            </template>
            <el-table :data="paymentPlanData" border size="small" style="width: 100%;">
              <el-table-column prop="label" label="周期" width="120" />
              <el-table-column prop="amount" label="计划金额" align="right">
                <template #default="{ row }">
                  {{ formatAmount(row.amount) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
      
      <!-- 账龄分析 -->
      <div v-else-if="activeTab === 'aging'" class="report-section">
        <div class="report-header">
          <h3>应付款账龄分析</h3>
          <el-button type="primary" @click="refreshAgingAnalysis">刷新数据</el-button>
        </div>
        <div class="aging-report">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">账龄分析</div>
            </template>
            <el-table :data="payableAgingData" border size="small" style="width: 100%;">
              <el-table-column prop="ageGroup" label="账龄区间" width="120" />
              <el-table-column prop="count" label="笔数" width="80" align="right" />
              <el-table-column prop="amount" label="金额" width="140" align="right" />
              <el-table-column prop="percentage" label="占比" width="100" align="right">
                <template #default="{ row }">{{ row.percentage }}%</template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
      
      <!-- 供应商对账 -->
      <div v-else-if="activeTab === 'reconciliation'" class="report-section">
        <div class="report-header">
          <h3>供应商对账</h3>
          <el-button type="primary" @click="refreshReconciliation">刷新数据</el-button>
        </div>
        <div class="reconciliation-report">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">供应商对账</div>
            </template>
            <el-table :data="reconciliationData" border size="small" style="width: 100%;">
              <el-table-column prop="supplier_name" label="供应商" width="240" />
              <el-table-column prop="count" label="单据数" width="120" align="right" />
              <el-table-column prop="amount" label="金额" align="right">
                <template #default="{ row }">
                  {{ formatAmount(row.amount) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 付款单录入对话框 -->
    <DialogComponent
      v-model="paymentFormVisible"
      title="录入付款单"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      :show-close="true"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      @confirm="handlePaymentSave"
    >
      <div class="payment-form">
        <el-form
          ref="paymentFormRef"
          :model="paymentForm"
          :rules="paymentFormRules"
          label-width="120px"
        >
          <el-form-item label="付款单编号" prop="payment_no">
            <el-input v-model="paymentForm.payment_no" placeholder="自动生成" disabled />
          </el-form-item>
          <el-form-item label="付款日期" prop="payment_date" required>
            <el-date-picker
              v-model="paymentForm.payment_date"
              type="date"
              placeholder="选择付款日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="供应商名称" prop="supplier_id" required>
            <el-select
              v-model="paymentForm.supplier_id"
              placeholder="选择供应商"
              style="width: 100%"
            >
              <el-option
                v-for="supplier in supplierList"
                :key="supplier.id"
                :label="supplier.supplier_name"
                :value="supplier.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="付款金额" prop="payment_amount" required>
            <el-input-number
              v-model="paymentForm.payment_amount"
              :min="0"
              :step="0.01"
              placeholder="输入付款金额"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="付款方式" prop="payment_method" required>
            <el-select
              v-model="paymentForm.payment_method"
              placeholder="选择付款方式"
              style="width: 100%"
            >
              <el-option label="现金" value="cash" />
              <el-option label="银行转账" value="bank_transfer" />
              <el-option label="支票" value="check" />
              <el-option label="汇票" value="draft" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="paymentForm.remark"
              type="textarea"
              rows="3"
              placeholder="输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>
    
    <!-- S11 发票视觉识别对话框（拍照/粘贴文本 → 规则结构化 → 预填建单表单） -->
    <DocVisionDialog
      v-model="visionVisible"
      doc-type="invoice"
      title="发票识别建单"
      @confirm="handleVisionConfirm"
    />

    <!-- 新增应付记录对话框（发票识别预填，CONFIRM 级人工确认后保存） -->
    <DialogComponent
      v-model="invoiceFormVisible"
      title="新增应付记录"
      width="640px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      :close-on-click-modal="false"
      @confirm="handleInvoiceSave"
    >
      <el-form :model="invoiceForm" label-width="110px">
        <el-form-item label="发票号码" required>
          <el-input v-model="invoiceForm.finance_no" placeholder="发票号码（作为单据唯一编号）" />
        </el-form-item>
        <el-form-item label="供应商名称" required>
          <el-input v-model="invoiceForm.supplier_name" placeholder="销售方/供应商名称" />
        </el-form-item>
        <el-form-item label="发票金额" required>
          <el-input-number v-model="invoiceForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开票日期" required>
          <el-date-picker v-model="invoiceForm.invoice_date" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="invoiceForm.remark" type="textarea" :rows="2" placeholder="备注信息（可选）" />
        </el-form-item>
      </el-form>
    </DialogComponent>

    <!-- 应付款详情对话框 -->
    <DialogComponent
      v-model="detailVisible"
      title="应付款详情"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="false"
      cancel-text="关闭"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="payable-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <span class="label">发票编号：</span>
              <span class="value">{{ selectedPayable?.invoice_no }}</span>
            </div>
            <div class="detail-item">
              <span class="label">供应商名称：</span>
              <span class="value">{{ selectedPayable?.supplier_name }}</span>
            </div>
            <div class="detail-item">
              <span class="label">发票金额：</span>
              <span class="value">{{ selectedPayable?.invoice_amount }}</span>
            </div>
            <div class="detail-item">
              <span class="label">已付金额：</span>
              <span class="value">{{ selectedPayable?.paid_amount }}</span>
            </div>
            <div class="detail-item">
              <span class="label">余额：</span>
              <span class="value">{{ selectedPayable?.balance_amount }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <span class="label">发票日期：</span>
              <span class="value">{{ selectedPayable?.invoice_date }}</span>
            </div>
            <div class="detail-item">
              <span class="label">到期日期：</span>
              <span class="value">{{ selectedPayable?.due_date }}</span>
            </div>
            <div class="detail-item">
              <span class="label">状态：</span>
              <span class="value">
                <el-tag :type="getStatusType(selectedPayable?.status || '')" size="small">
                  {{ getStatusLabel(selectedPayable?.status || '') }}
                </el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ selectedPayable?.create_time }}</span>
            </div>
            <div class="detail-item">
              <span class="label">备注：</span>
              <span class="value">{{ selectedPayable?.remark || '无' }}</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </DialogComponent>

    <!-- 核销对话框 -->
    <DialogComponent
      v-model="writeOffVisible"
      title="应付账款核销"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="核销"
      @confirm="handleWriteOff"
    >
      <div class="write-off-form">
        <div class="write-off-info">
          <div class="info-item">
            <span class="label">供应商名称：</span>
            <span class="value">{{ selectedPayable?.supplier_name }}</span>
          </div>
          <div class="info-item">
            <span class="label">本次核销金额：</span>
            <span class="value">{{ selectedPayable?.balance_amount }}</span>
          </div>
        </div>
        
        <el-form
          ref="writeOffFormRef"
          :model="writeOffForm"
          label-width="120px"
          class="mt-20"
        >
          <el-form-item label="核销方式" prop="write_off_type" required>
            <el-select
              v-model="writeOffForm.write_off_type"
              placeholder="选择核销方式"
              style="width: 100%"
            >
              <el-option label="全额核销" value="full" />
              <el-option label="部分核销" value="partial" />
            </el-select>
          </el-form-item>
          <el-form-item label="核销金额" prop="write_off_amount" required>
            <el-input-number
              v-model="writeOffForm.write_off_amount"
              :min="0"
              :max="selectedPayable?.balance_amount"
              :step="0.01"
              placeholder="输入核销金额"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="核销日期" prop="write_off_date" required>
            <el-date-picker
              v-model="writeOffForm.write_off_date"
              type="date"
              placeholder="选择核销日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="writeOffForm.remark"
              type="textarea"
              rows="3"
              placeholder="输入核销备注"
            />
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentChecked, Plus, Delete, RefreshLeft, View, Check, Camera } from '@element-plus/icons-vue'
import { TableComponent, DialogComponent } from '../../../components/base'
import api, { unwrapListResponse, unwrapPageResponse, unwrapResponseData } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import DocVisionDialog from '../../../components/ai/DocVisionDialog.vue'

// 响应式数据
// 应付款列表
const accountsPayableList = ref<any[]>([])
const accountsPayableTotal = ref(0)
const accountsPayableLoading = ref(false)
const accountsPayablePage = ref(1)
const accountsPayableSize = ref(10)
const selectedRows = ref<any[]>([])
const selectedPayable = ref<any>(null)

// 供应商列表
const supplierList = ref<any[]>([])

// 对话框状态
const paymentFormVisible = ref(false)
const writeOffVisible = ref(false)
const detailVisible = ref(false)

// S11 发票视觉识别与建单状态
const visionVisible = ref(false)
const invoiceFormVisible = ref(false)
const invoiceSaving = ref(false)
const invoiceForm = ref({
  finance_no: '',
  supplier_name: '',
  amount: undefined as number | undefined,
  invoice_date: '',
  remark: ''
})

// 数据概览
const overviewData = ref({
  totalAmount: '¥0.00',
  paidAmount: '¥0.00',
  unpaidAmount: '¥0.00',
  overdueAmount: '¥0.00'
})

// 当前活动标签页
const activeTab = ref('list')
const paymentPlanData = ref<any[]>([])
const payableAgingData = ref<any[]>([])
const reconciliationData = ref<any[]>([])

// 表单数据
const paymentFormRef = ref<any>(null)
const paymentFormRules = ref<any>({
  payment_date: [{ required: true, message: '请选择付款日期', trigger: 'change' }],
  supplier_id: [{ required: true, message: '请选择供应商', trigger: 'change' }],
  payment_amount: [{ required: true, message: '请输入付款金额', trigger: 'blur' }, { type: 'number', min: 0.01, message: '付款金额必须大于0', trigger: 'blur' }],
  payment_method: [{ required: true, message: '请选择付款方式', trigger: 'change' }]
})
const paymentForm = ref({
  id: 0,
  payment_no: '',
  payment_date: '',
  supplier_id: 0,
  payment_amount: 0,
  payment_method: 'cash',
  remark: ''
})

const writeOffFormRef = ref<any>(null)
const writeOffFormRules = ref<any>({
  write_off_type: [{ required: true, message: '请选择核销方式', trigger: 'change' }],
  write_off_amount: [{ required: true, message: '请输入核销金额', trigger: 'blur' }, { type: 'number', min: 0.01, message: '核销金额必须大于0', trigger: 'blur' }],
  write_off_date: [{ required: true, message: '请选择核销日期', trigger: 'change' }]
})
const writeOffForm = ref({
  write_off_type: 'full',
  write_off_amount: 0,
  write_off_date: '',
  remark: ''
})

// 应付款状态映射
const statusMap: Record<string, { label: string; type: string }> = {
  'pending': { label: '待付款', type: 'warning' },
  'partial': { label: '部分付款', type: 'info' },
  'paid': { label: '已付款', type: 'success' },
  'written_off': { label: '已核销', type: 'info' },
  'overdue': { label: '已逾期', type: 'danger' }
}

// 应付款筛选条件
const accountsPayableFilters = [
  { prop: 'invoice_no', label: '发票编号', type: 'input' as 'input', placeholder: '请输入发票编号' },
  { prop: 'supplier_name', label: '供应商名称', type: 'input' as 'input', placeholder: '请输入供应商名称' },
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '待付款', value: 'pending' },
    { label: '部分付款', value: 'partial' },
    { label: '已付款', value: 'paid' },
    { label: '已核销', value: 'written_off' },
    { label: '已逾期', value: 'overdue' }
  ]},
  { prop: 'invoice_date', label: '发票日期', type: 'daterange' as 'daterange' },
  { prop: 'due_date', label: '到期日期', type: 'daterange' as 'daterange' }
] as any

// 应付款列配置
const accountsPayableColumns = [
  { prop: 'invoice_no', label: '发票编号', width: 150 },
  { prop: 'supplier_name', label: '供应商名称', width: 200 },
  { prop: 'invoice_amount', label: '发票金额', width: 150, align: 'right' },
  { prop: 'paid_amount', label: '已付金额', width: 150, align: 'right' },
  { prop: 'balance_amount', label: '余额', width: 150, align: 'right' },
  { prop: 'invoice_date', label: '发票日期', width: 150 },
  { prop: 'due_date', label: '到期日期', width: 150 },
  { prop: 'status', label: '状态', width: 120, slotName: 'status' },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

// 应付款操作按钮
const accountsPayableActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      selectedPayable.value = row
      detailVisible.value = true
    }
  },
  {
    text: '付款',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      selectedPayable.value = row
      // 打开付款单录入对话框
      paymentFormVisible.value = true
      // 自动填充供应商信息
      paymentForm.value.supplier_id = row.supplier_id
      paymentForm.value.payment_amount = row.balance_amount
    }
  },
  {
    text: '核销',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      selectedPayable.value = row
      writeOffForm.value.write_off_amount = row.balance_amount
      writeOffVisible.value = true
    }
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'invoice-vision',
    text: '发票识别建单',
    type: 'success',
    icon: Camera,
    handler: () => {
      visionVisible.value = true
    }
  },
  {
    key: 'add-payment',
    text: '录入付款单',
    type: 'primary',
    icon: Plus,
    handler: () => {
      paymentFormVisible.value = true
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
onMounted(async () => {
  await handleSearch()
  await loadSuppliers()
  await refreshOverview()
})

// 刷新数据概览
const refreshOverview = async () => {
  try {
    refreshOverviewByList(accountsPayableList.value as any)
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
  }
}

// 监听标签页切换
const handleTabChange = async (tab: string) => {
  activeTab.value = tab
  if (tab === 'plan') {
    await refreshPaymentPlan()
  } else if (tab === 'aging') {
    await refreshAgingAnalysis()
  } else if (tab === 'reconciliation') {
    await refreshReconciliation()
  }
}

// 刷新付款计划
const refreshPaymentPlan = async () => {
  try {
    const res = await erpApi.finance.getPayablePlanReport()
    const points = unwrapResponseData<any>(res)?.points || []
    paymentPlanData.value = Array.isArray(points) ? points : []
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
  }
}

// 刷新账龄分析
const refreshAgingAnalysis = async () => {
  try {
    const res = await erpApi.finance.getPayableAgingReport()
    const dist = unwrapResponseData<any>(res)?.distribution || []
    payableAgingData.value = (Array.isArray(dist) ? dist : []).map((it: any) => ({
      ageGroup: it.age_group || it.ageGroup,
      count: Number(it.count) || 0,
      amount: formatAmount(it.amount),
      percentage: Number(it.percentage) || 0
    }))
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
  }
}

// 刷新供应商对账
const refreshReconciliation = async () => {
  try {
    const res = await erpApi.finance.getPayableReconciliationReport()
    const rows = unwrapListResponse<any>(res)
    reconciliationData.value = Array.isArray(rows) ? rows : []
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
  }
}

const formatAmount = (v: any) => {
  const n = typeof v === 'number' ? v : Number(v || 0)
  const safe = Number.isFinite(n) ? n : 0
  return `¥${safe.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

// 加载供应商列表
const loadSuppliers = async () => {
  try {
    const result = await erpApi.basicData.getSuppliers({ page: 1, size: 100 })
    supplierList.value = unwrapListResponse<any>(result)
  } catch (error) {
    ErrorHandler.handleApiError(error)
    supplierList.value = []
  }
}

/**
 * 后端财务行 → 前端应付列表行 字段归一化。
 * 后端 FinanceEntity 返回 finance_no/amount/transaction_date/description/status(数字)/created_time，
 * 列定义为 invoice_no/supplier_name/invoice_amount/invoice_date/status(字符串)/create_time，在此对齐。
 */
const normalizePayableRow = (row: any) => {
  // 供应商名称：识别建单写入 description 的"供应商：XXX"前缀，兼容纯文本备注
  const desc = String(row.description || '')
  const supplierMatch = desc.match(/^供应商：([^；;]+)/)
  // 状态：后端数字 0=待付款 1=已付款，映射到 statusMap 的字符串键
  const statusKey = row.status === 0 || row.status === '0' ? 'pending'
    : row.status === 1 || row.status === '1' ? 'paid'
    : String(row.status || 'pending')
  const amount = Number(row.amount ?? row.invoice_amount ?? 0)
  const paidAmount = Number(row.paid_amount ?? 0)
  return {
    ...row,
    invoice_no: row.finance_no ?? row.invoice_no ?? '',
    supplier_name: row.supplier_name ?? (supplierMatch?.[1]?.trim() || desc),
    invoice_amount: amount,
    paid_amount: paidAmount,
    balance_amount: Number(row.balance_amount ?? (amount - paidAmount)),
    unpaid_amount: Number(row.unpaid_amount ?? (amount - paidAmount)),
    invoice_date: row.transaction_date ? String(row.transaction_date).slice(0, 10) : (row.invoice_date ?? ''),
    due_date: row.due_date ?? '',
    status: statusKey,
    create_time: row.created_time ?? row.create_time ?? ''
  }
}

// 获取应付款列表
const getAccountsPayableList = async (params: any) => {
  try {
    accountsPayableLoading.value = true
    const result = await erpApi.finance.getAccountsPayable({
      page: params.page || accountsPayablePage.value,
      size: params.size || accountsPayableSize.value,
      ...params
    })
    const page = unwrapPageResponse<any>(result)
    const data = page.list.map(normalizePayableRow)
    accountsPayableList.value = data
    accountsPayableTotal.value = page.total || data.length || 0
    accountsPayablePage.value = params.page || accountsPayablePage.value
    accountsPayableSize.value = params.size || accountsPayableSize.value

    refreshOverviewByList(data)
  } catch (error) {
    ErrorHandler.handleApiError(error)
    accountsPayableList.value = []
    accountsPayableTotal.value = 0
    refreshOverviewByList([])
  } finally {
    accountsPayableLoading.value = false
  }
}

const refreshOverviewByList = (list: any[]) => {
  const safeList = Array.isArray(list) ? list : []
  const totalAmount = safeList.reduce((sum, row) => sum + Number(row.amount || 0), 0)
  const paidAmount = safeList.reduce((sum, row) => sum + Number(row.paid_amount || 0), 0)
  const unpaidAmount = safeList.reduce((sum, row) => sum + Number(row.unpaid_amount || 0), 0)
  overviewData.value = {
    totalAmount: `¥${totalAmount.toFixed(2)}`,
    paidAmount: `¥${paidAmount.toFixed(2)}`,
    unpaidAmount: `¥${unpaidAmount.toFixed(2)}`,
    overdueAmount: '¥0.00'
  }
}

// 搜索应付款
const handleSearch = (params: any = {}) => {
  getAccountsPayableList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  accountsPayableSize.value = size
  getAccountsPayableList({ page: accountsPayablePage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  accountsPayablePage.value = page
  getAccountsPayableList({ page, size: accountsPayableSize.value })
}

// 选择行改变
const handleSelectionChange = (rows: any[]) => {
  selectedRows.value = rows
}

// 状态标签类型
const getStatusType = (status: string) => {
  // 确保返回有效的type值，避免ElTag组件type属性验证失败
  return statusMap[status]?.type || 'info'
}

// 状态标签文本
const getStatusLabel = (status: string) => {
  return statusMap[status]?.label || status
}

// 判断是否逾期
const isOverdue = (row: any) => {
  if (!row || !row.due_date || row.status === 'paid' || row.status === 'written_off') {
    return false
  }
  
  const dueDate = new Date(row.due_date)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  return dueDate < today
}

// 计算逾期天数
const getOverdueDays = (row: any) => {
  if (!row || !isOverdue(row)) {
    return 0
  }
  
  const dueDate = new Date(row.due_date)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  dueDate.setHours(0, 0, 0, 0)
  
  const timeDiff = today.getTime() - dueDate.getTime()
  return Math.ceil(timeDiff / (1000 * 60 * 60 * 24))
}

// 获取预警级别
const getWarningLevel = (row: any) => {
  if (!row || !row.due_date || row.status === 'paid' || row.status === 'written_off') {
    return ''
  }
  
  const dueDate = new Date(row.due_date)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  dueDate.setHours(0, 0, 0, 0)
  
  const daysDiff = Math.ceil((dueDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
  
  if (daysDiff < 0) {
    return 'overdue'
  } else if (daysDiff <= 7) {
    return 'warning'
  } else if (daysDiff <= 15) {
    return 'info'
  } else {
    return ''
  }
}

// 保存付款单
const handlePaymentSave = async () => {
  if (!paymentFormRef.value) return
  
  try {
    await paymentFormRef.value.validate()
    await erpApi.finance.createPayment(paymentForm.value as any)
    // 关闭对话框
    paymentFormVisible.value = false
    // 刷新列表
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

/**
 * S11 发票识别确认：预填新增应付表单并弹出人工确认框（CONFIRM 级）
 * @param fields 识别提取字段 {invoiceNo, invoiceDate, totalAmount, supplierName}
 */
const handleVisionConfirm = (fields: Record<string, any>) => {
  invoiceForm.value = {
    finance_no: String(fields.invoiceNo || ''),
    supplier_name: String(fields.supplierName || ''),
    amount: fields.totalAmount != null ? Number(fields.totalAmount) : undefined,
    invoice_date: String(fields.invoiceDate || ''),
    remark: ''
  }
  invoiceFormVisible.value = true
}

/** 保存发票识别建单：走现有 ERP 财务创建端点（transaction_type=2 应付），成功后清缓存刷新列表 */
const handleInvoiceSave = async () => {
  const form = invoiceForm.value
  if (!form.finance_no || !form.supplier_name || !form.amount || !form.invoice_date) {
    ElMessage.warning('请完整填写发票号码、供应商、金额与开票日期')
    return
  }
  invoiceSaving.value = true
  try {
    await erpApi.finance.createPayment({
      finance_no: form.finance_no,
      transaction_type: 2,
      amount: form.amount,
      currency: 'CNY',
      transaction_date: `${form.invoice_date} 00:00:00`,
      description: `供应商：${form.supplier_name}${form.remark ? '；' + form.remark : ''}`,
      status: 0
    } as any)
    ElMessage.success('应付记录已保存')
    invoiceFormVisible.value = false
    // 写操作后手动清除列表缓存（子路径写操作无法自动失效父列表缓存）
    api.clearCache('/api/v1/erp/finance/list')
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  } finally {
    invoiceSaving.value = false
  }
}

// 处理核销
const handleWriteOff = async () => {
  if (!writeOffFormRef.value || !selectedPayable.value) return
  
  try {
    await writeOffFormRef.value.validate()
    await erpApi.finance.writeOffPayable({
      payableId: selectedPayable.value.id,
      writeOffType: writeOffForm.value.write_off_type,
      writeOffAmount: writeOffForm.value.write_off_amount,
      writeOffDate: writeOffForm.value.write_off_date,
      remark: writeOffForm.value.remark
    })
    // 关闭对话框
    writeOffVisible.value = false
    // 刷新列表
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}
</script>

<style scoped lang="scss">
.accounts-payable-view {
  padding: 20px;
  
  // 数据概览卡片
  .overview-cards {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 20px;
    
    .overview-card {
      border-radius: 8px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      transition: transform 0.3s ease;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
      }
      
      .overview-item {
        text-align: center;
        padding: 20px 0;
        
        .item-title {
          font-size: 14px;
          color: #606266;
          margin-bottom: 10px;
        }
        
        .item-value {
          font-size: 28px;
          font-weight: bold;
          color: #1890ff;
        }
      }
    }
  }
  
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
  .accounts-payable-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .accounts-payable-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}

// 详情对话框样式
.payable-detail {
  .detail-item {
    margin-bottom: 16px;
    display: flex;
    align-items: center;
    
    .label {
      font-weight: bold;
      width: 120px;
      color: #606266;
    }
    
    .value {
      color: #303133;
    }
  }
}

// 报表区域样式
.report-section {
  padding: 20px;
  
  .report-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h3 {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
      margin: 0;
    }
  }
  
  .plan-report,
  .aging-report,
  .reconciliation-report {
    :deep(.el-card) {
      margin-bottom: 20px;
      
      .card-header {
        font-weight: bold;
        color: #303133;
      }
      
      .plan-content,
      .aging-content,
      .reconciliation-content {
        padding: 20px;
        text-align: center;
        color: #606266;
      }
    }
  }
}

// 间距样式
.mt-10 {
  margin-top: 10px;
}

.ml-10 {
  margin-left: 10px;
}

.mt-20 {
  margin-top: 20px;
}
</style>
