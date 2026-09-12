<template>
  <div class="accounts-receivable-view">
    <!-- 数据概览卡片 -->
    <div class="overview-cards">
      <el-card shadow="hover" class="overview-card">
        <div class="overview-item">
          <div class="item-title">应收总额</div>
          <div class="item-value">{{ overviewData.totalAmount }}</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="overview-card">
        <div class="overview-item">
          <div class="item-title">已收金额</div>
          <div class="item-value">{{ overviewData.receivedAmount }}</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="overview-card">
        <div class="overview-item">
          <div class="item-title">未收金额</div>
          <div class="item-value">{{ overviewData.unreceivedAmount }}</div>
        </div>
      </el-card>
      <el-card shadow="hover" class="overview-card">
        <div class="overview-item">
          <div class="item-title">逾期金额</div>
          <div class="item-value">{{ overviewData.overdueAmount }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 报表分析区域 -->
    <el-card class="module-card">
      <template #header>
        <div class="card-header">
          <el-icon><DocumentChecked /></el-icon>
          <span>应收管理</span>
          <el-tabs v-model="activeTab" class="ml-auto" @tab-change="handleTabChange">
            <el-tab-pane label="应收款列表" name="list"></el-tab-pane>
            <el-tab-pane label="账龄分析" name="aging"></el-tab-pane>
            <el-tab-pane label="回款预测" name="forecast"></el-tab-pane>
            <el-tab-pane label="客户信用评估" name="credit"></el-tab-pane>
          </el-tabs>
        </div>
      </template>
      
      <!-- 应收款列表 -->
      <div v-if="activeTab === 'list'">
        <TableComponent
          :data="accountsReceivableList"
          :columns="accountsReceivableColumns"
          :total="accountsReceivableTotal"
          :loading="accountsReceivableLoading"
          :show-index="true"
          :show-selection="true"
          :show-action="true"
          :actions="accountsReceivableActions"
          :table-actions="tableActions"
          :filters="accountsReceivableFilters"
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
      
      <!-- 账龄分析 -->
      <div v-else-if="activeTab === 'aging'" class="report-section">
        <div class="report-header">
          <h3>应收账款账龄分析</h3>
          <el-button type="primary" @click="refreshAgingReport">刷新数据</el-button>
        </div>
        <div class="aging-report">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">账龄分布</div>
                </template>
                <div ref="agingChartRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">账龄明细</div>
                </template>
                <el-table
                  :data="agingData"
                  border
                  size="small"
                  style="width: 100%;"
                >
                  <el-table-column prop="ageGroup" label="账龄区间" width="120" />
                  <el-table-column prop="count" label="笔数" width="80" align="right" />
                  <el-table-column prop="amount" label="金额" width="120" align="right" />
                  <el-table-column prop="percentage" label="占比" width="100" align="right">
                    <template #default="{ row }">
                      {{ row.percentage }}%
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <!-- 回款预测 -->
      <div v-else-if="activeTab === 'forecast'" class="report-section">
        <div class="report-header">
          <h3>回款预测</h3>
          <el-button type="primary" @click="refreshForecastReport">刷新数据</el-button>
        </div>
        <div class="forecast-report">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">未来30天回款预测</div>
            </template>
            <div ref="forecastChartRef" class="chart-container"></div>
            <el-table :data="forecastData" border size="small" style="width: 100%; margin-top: 12px;">
              <el-table-column prop="label" label="周期" width="120" />
              <el-table-column prop="amount" label="预测金额" align="right">
                <template #default="{ row }">
                  {{ formatAmount(row.amount) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </div>
      
      <!-- 客户信用评估 -->
      <div v-else-if="activeTab === 'credit'" class="report-section">
        <div class="report-header">
          <h3>客户信用评估</h3>
          <el-button type="primary" @click="refreshCreditReport">刷新数据</el-button>
        </div>
        <div class="credit-report">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">客户信用评分</div>
            </template>
            <el-table
              :data="creditData"
              border
              size="small"
              style="width: 100%;"
            >
              <el-table-column prop="customer_name" label="客户名称" width="200" />
              <el-table-column prop="credit_score" label="信用评分" width="120" align="right" />
              <el-table-column prop="credit_level" label="信用等级" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.credit_level_type">{{ row.credit_level }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="overdue_count" label="逾期次数" width="120" align="right" />
              <el-table-column prop="overdue_amount" label="逾期金额" width="120" align="right" />
            </el-table>
          </el-card>
        </div>
      </div>
    </el-card>

    <!-- 收款单录入对话框 -->
    <DialogComponent
      v-model="receiptFormVisible"
      title="录入收款单"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      @confirm="handleReceiptSave"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="receipt-form">
        <el-form
          ref="receiptFormRef"
          :model="receiptForm"
          :rules="receiptFormRules"
          label-width="120px"
        >
          <el-form-item label="收款单编号" prop="receipt_no">
            <el-input v-model="receiptForm.receipt_no" placeholder="自动生成" disabled />
          </el-form-item>
          <el-form-item label="收款日期" prop="receipt_date" required>
            <el-date-picker
              v-model="receiptForm.receipt_date"
              type="date"
              placeholder="选择收款日期"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="客户名称" prop="customer_id" required>
            <el-select
              v-model="receiptForm.customer_id"
              placeholder="选择客户"
              style="width: 100%"
            >
              <el-option
                v-for="customer in customerList"
                :key="customer.id"
                :label="customer.customer_name"
                :value="customer.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="收款金额" prop="receipt_amount" required>
            <el-input-number
              v-model="receiptForm.receipt_amount"
              :min="0"
              :step="0.01"
              placeholder="输入收款金额"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="收款方式" prop="payment_method" required>
            <el-select
              v-model="receiptForm.payment_method"
              placeholder="选择收款方式"
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
              v-model="receiptForm.remark"
              type="textarea"
              rows="3"
              placeholder="输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>
    
    <!-- 应收款详情对话框 -->
    <DialogComponent
      v-model="detailVisible"
      title="应收款详情"
      width="800px"
      :show-footer="false"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="receivable-detail">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-item">
              <span class="label">发票编号：</span>
              <span class="value">{{ selectedReceivable?.invoice_no }}</span>
            </div>
            <div class="detail-item">
              <span class="label">客户名称：</span>
              <span class="value">{{ selectedReceivable?.customer_name }}</span>
            </div>
            <div class="detail-item">
              <span class="label">发票金额：</span>
              <span class="value">{{ selectedReceivable?.invoice_amount }}</span>
            </div>
            <div class="detail-item">
              <span class="label">已收金额：</span>
              <span class="value">{{ selectedReceivable?.received_amount }}</span>
            </div>
            <div class="detail-item">
              <span class="label">余额：</span>
              <span class="value">{{ selectedReceivable?.balance_amount }}</span>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-item">
              <span class="label">发票日期：</span>
              <span class="value">{{ selectedReceivable?.invoice_date }}</span>
            </div>
            <div class="detail-item">
              <span class="label">到期日期：</span>
              <span class="value">{{ selectedReceivable?.due_date }}</span>
            </div>
            <div class="detail-item">
              <span class="label">状态：</span>
              <span class="value">
                <el-tag :type="getStatusType(selectedReceivable?.status || '')" size="small">
                  {{ getStatusLabel(selectedReceivable?.status || '') }}
                </el-tag>
                <el-tag
                  v-if="isOverdue(selectedReceivable)"
                  type="danger"
                  size="small"
                  class="ml-10"
                >
                  逾期 {{ getOverdueDays(selectedReceivable) }} 天
                </el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ selectedReceivable?.create_time }}</span>
            </div>
            <div class="detail-item">
              <span class="label">备注：</span>
              <span class="value">{{ selectedReceivable?.remark || '无' }}</span>
            </div>
          </el-col>
        </el-row>
        
        <!-- 收款记录 -->
        <div class="receipt-records mt-20">
          <h3>收款记录</h3>
          <el-table
            :data="selectedReceivable?.receipts || []"
            border
            size="small"
            style="width: 100%;"
          >
            <el-table-column prop="receipt_no" label="收款单编号" width="180" />
            <el-table-column prop="receipt_date" label="收款日期" width="150" />
            <el-table-column prop="receipt_amount" label="收款金额" width="120" align="right" />
            <el-table-column prop="payment_method" label="收款方式" width="120" />
            <el-table-column prop="remark" label="备注" min-width="200" />
          </el-table>
          <div v-if="!(selectedReceivable?.receipts && selectedReceivable.receipts.length)" class="no-records">
            暂无收款记录
          </div>
        </div>
      </div>
    </DialogComponent>

    <!-- 核销对话框 -->
    <DialogComponent
      v-model="writeOffVisible"
      title="应收账款核销"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="核销"
      @confirm="handleWriteOff"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="write-off-form">
        <div class="write-off-info">
          <div class="info-item">
            <span class="label">客户名称：</span>
            <span class="value">{{ selectedReceivable?.customer_name }}</span>
          </div>
          <div class="info-item">
            <span class="label">本次核销金额：</span>
            <span class="value">{{ selectedReceivable?.balance_amount }}</span>
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
              <el-option label="坏账核销" value="bad_debt" />
            </el-select>
          </el-form-item>
          <el-form-item label="核销金额" prop="write_off_amount" required>
            <el-input-number
              v-model="writeOffForm.write_off_amount"
              :min="0"
              :max="selectedReceivable?.balance_amount"
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
    
    <!-- 应收账单创建对话框 -->
    <DialogComponent
      v-model="createBillVisible"
      title="创建应收账单"
      width="800px"
      :show-footer="true"
      :show-cancel-button="true"
      :show-confirm-button="true"
      cancel-text="取消"
      confirm-text="保存"
      @confirm="handleCreateBill"
      :show-close="true"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
    >
      <div class="create-bill-form">
        <el-form
          ref="createBillFormRef"
          :model="createBillForm"
          :rules="createBillFormRules"
          label-width="120px"
        >
          <el-form-item label="客户" prop="customer_id" required>
            <el-select
              v-model="createBillForm.customer_id"
              placeholder="选择客户"
              style="width: 100%"
            >
              <el-option
                v-for="customer in customerList"
                :key="customer.id"
                :label="customer.customer_name"
                :value="customer.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="计费模式" prop="billing_mode" required>
            <el-select
              v-model="createBillForm.billing_mode"
              placeholder="选择计费模式"
              style="width: 100%"
              @change="handleBillingModeChange"
            >
              <el-option label="固定金额" value="fixed" />
              <el-option label="按数量" value="quantity" />
              <el-option label="按时间" value="time" />
              <el-option label="混合模式" value="hybrid" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="发票编号" prop="invoice_no" required>
            <el-input
              v-model="createBillForm.invoice_no"
              placeholder="输入发票编号"
            />
          </el-form-item>
          
          <el-form-item label="发票金额" prop="invoice_amount" required>
            <el-input-number
              v-model="createBillForm.invoice_amount"
              :min="0.01"
              :step="0.01"
              placeholder="输入发票金额"
              style="width: 100%"
            />
          </el-form-item>
          
          <!-- 按数量计费字段 -->
          <el-form-item
            v-if="createBillForm.billing_mode === 'quantity'"
            label="数量" 
            prop="quantity"
            required
          >
            <el-input-number
              v-model="createBillForm.quantity"
              :min="1"
              :step="1"
              placeholder="输入数量"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item
            v-if="createBillForm.billing_mode === 'quantity'"
            label="单价" 
            prop="unit_price"
            required
          >
            <el-input-number
              v-model="createBillForm.unit_price"
              :min="0.01"
              :step="0.01"
              placeholder="输入单价"
              style="width: 100%"
            />
          </el-form-item>
          
          <!-- 按时间计费字段 -->
          <el-form-item
            v-if="createBillForm.billing_mode === 'time'"
            label="服务时长(小时)" 
            prop="service_hours"
            required
          >
            <el-input-number
              v-model="createBillForm.service_hours"
              :min="0.5"
              :step="0.5"
              placeholder="输入服务时长"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item
            v-if="createBillForm.billing_mode === 'time'"
            label="小时费率" 
            prop="hourly_rate"
            required
          >
            <el-input-number
              v-model="createBillForm.hourly_rate"
              :min="0.01"
              :step="0.01"
              placeholder="输入小时费率"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="发票日期" prop="invoice_date" required>
            <el-date-picker
              v-model="createBillForm.invoice_date"
              type="date"
              placeholder="选择发票日期"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="到期日期" prop="due_date" required>
            <el-date-picker
              v-model="createBillForm.due_date"
              type="date"
              placeholder="选择到期日期"
              style="width: 100%"
            />
          </el-form-item>
          
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="createBillForm.remark"
              type="textarea"
              rows="3"
              placeholder="输入备注信息"
            />
          </el-form-item>
        </el-form>
      </div>
    </DialogComponent>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { DocumentChecked, Plus, Delete, RefreshLeft, View, Check } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { TableComponent, DialogComponent } from '../../../components/base'
import { unwrapListResponse, unwrapPageResponse, unwrapResponseData } from '../../../api'
import { erpApi } from '../../../api/erp'
import { ErrorHandler } from '../../../utils/error-handler'
import type { AccountsReceivableStatus } from '../../../types/erp/finance'

// 响应式数据
// 应收款列表
const accountsReceivableList = ref<any[]>([])
const accountsReceivableTotal = ref(0)
const accountsReceivableLoading = ref(false)
const accountsReceivablePage = ref(1)
const accountsReceivableSize = ref(10)
const selectedRows = ref<any[]>([])
const selectedReceivable = ref<any>(null)

// 客户列表
const customerList = ref<any[]>([])

// 对话框状态
const receiptFormVisible = ref(false)
const writeOffVisible = ref(false)
const detailVisible = ref(false)
const createBillVisible = ref(false)

// 表单数据
const receiptFormRef = ref<any>(null)
const receiptFormRules = ref<any>({
  receipt_date: [{ required: true, message: '请选择收款日期', trigger: 'change' }],
  customer_id: [{ required: true, message: '请选择客户', trigger: 'change' }],
  receipt_amount: [{ required: true, message: '请输入收款金额', trigger: 'blur' }, { type: 'number', min: 0.01, message: '收款金额必须大于0', trigger: 'blur' }],
  payment_method: [{ required: true, message: '请选择收款方式', trigger: 'change' }]
})
const receiptForm = ref({
  id: 0,
  receipt_no: '',
  receipt_date: '',
  customer_id: 0,
  receipt_amount: 0,
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

// 账单创建表单
const createBillFormRef = ref<any>(null)
const createBillFormRules = ref<any>({
  customer_id: [{ required: true, message: '请选择客户', trigger: 'change' }],
  billing_mode: [{ required: true, message: '请选择计费模式', trigger: 'change' }],
  invoice_no: [{ required: true, message: '请输入发票编号', trigger: 'blur' }],
  invoice_amount: [{ required: true, message: '请输入发票金额', trigger: 'blur' }, { type: 'number', min: 0.01, message: '发票金额必须大于0', trigger: 'blur' }],
  invoice_date: [{ required: true, message: '请选择发票日期', trigger: 'change' }],
  due_date: [{ required: true, message: '请选择到期日期', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }, { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }],
  unit_price: [{ required: true, message: '请输入单价', trigger: 'blur' }, { type: 'number', min: 0.01, message: '单价必须大于0', trigger: 'blur' }],
  service_hours: [{ required: true, message: '请输入服务时长', trigger: 'blur' }, { type: 'number', min: 0.5, message: '服务时长必须大于0', trigger: 'blur' }],
  hourly_rate: [{ required: true, message: '请输入小时费率', trigger: 'blur' }, { type: 'number', min: 0.01, message: '小时费率必须大于0', trigger: 'blur' }]
})
const createBillForm = ref({
  id: 0,
  customer_id: 0,
  billing_mode: 'fixed',
  invoice_no: '',
  invoice_amount: 0,
  quantity: 1,
  unit_price: 0,
  service_hours: 1,
  hourly_rate: 0,
  invoice_date: '',
  due_date: '',
  remark: ''
})

// 报表相关数据
const activeTab = ref('list')
const overviewData = ref({
  totalAmount: '¥0.00',
  receivedAmount: '¥0.00',
  unreceivedAmount: '¥0.00',
  overdueAmount: '¥0.00'
})

// 账龄分析数据
const agingData = ref([
  { ageGroup: '0-30天', count: 0, amount: '¥0.00', percentage: 0 },
  { ageGroup: '31-60天', count: 0, amount: '¥0.00', percentage: 0 },
  { ageGroup: '61-90天', count: 0, amount: '¥0.00', percentage: 0 },
  { ageGroup: '91-180天', count: 0, amount: '¥0.00', percentage: 0 },
  { ageGroup: '180天以上', count: 0, amount: '¥0.00', percentage: 0 }
])

// 回款预测数据
const forecastData = ref<any[]>([])

// 客户信用评估数据
const creditData = ref<any[]>([])

// 图表引用
const agingChartRef = ref<any>(null)
const forecastChartRef = ref<any>(null)

// 图表实例
let agingChart: any = null
let forecastChart: any = null

// 应收款状态映射
const statusMap: Record<string, { label: string; type: string }> = {
  'pending': { label: '待收款', type: 'warning' },
  'partial': { label: '部分收款', type: 'info' },
  'paid': { label: '已收款', type: 'success' },
  'written_off': { label: '已核销', type: 'info' },
  'bad_debt': { label: '坏账', type: 'danger' }
}

// 应收款筛选条件
const accountsReceivableFilters = [
  { prop: 'invoice_no', label: '发票编号', type: 'input' as 'input', placeholder: '请输入发票编号' },
  { prop: 'customer_name', label: '客户名称', type: 'input' as 'input', placeholder: '请输入客户名称' },
  { prop: 'status', label: '状态', type: 'select' as 'select', options: [
    { label: '待收款', value: 'pending' },
    { label: '部分收款', value: 'partial' },
    { label: '已收款', value: 'paid' },
    { label: '已核销', value: 'written_off' },
    { label: '坏账', value: 'bad_debt' }
  ]},
  { prop: 'invoice_date', label: '发票日期', type: 'daterange' as 'daterange' },
  { prop: 'due_date', label: '到期日期', type: 'daterange' as 'daterange' }
] as any

// 应收款列配置
const accountsReceivableColumns = [
  { prop: 'invoice_no', label: '发票编号', width: 150 },
  { prop: 'customer_name', label: '客户名称', width: 200 },
  { prop: 'invoice_amount', label: '发票金额', width: 150, align: 'right' },
  { prop: 'received_amount', label: '已收金额', width: 150, align: 'right' },
  { prop: 'balance_amount', label: '余额', width: 150, align: 'right' },
  { prop: 'invoice_date', label: '发票日期', width: 150 },
  { prop: 'due_date', label: '到期日期', width: 150 },
  { prop: 'status', label: '状态', width: 120, slotName: 'status' },
  { prop: 'create_time', label: '创建时间', width: 180 }
]

// 应收款操作按钮
const accountsReceivableActions = [
  {
    text: '查看',
    type: 'primary',
    size: 'small',
    icon: View,
    handler: (row: any) => {
      selectedReceivable.value = row
      detailVisible.value = true
    }
  },
  {
    text: '收款',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      selectedReceivable.value = row
      // 打开收款单录入对话框
      receiptFormVisible.value = true
      // 自动填充客户信息
      receiptForm.value.customer_id = row.customer_id
    }
  },
  {
    text: '核销',
    type: 'success',
    size: 'small',
    icon: Check,
    handler: (row: any) => {
      selectedReceivable.value = row
      writeOffForm.value.write_off_amount = row.balance_amount
      writeOffVisible.value = true
    }
  }
]

// 表格操作按钮
const tableActions = [
  {
    key: 'add-bill',
    text: '创建账单',
    type: 'primary',
    icon: Plus,
    handler: () => {
      createBillVisible.value = true
    }
  },
  {
    key: 'add-receipt',
    text: '录入收款单',
    type: 'success',
    icon: Plus,
    handler: () => {
      receiptFormVisible.value = true
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
  await loadCustomers()
  await refreshOverview()
  // 初始化图表
  initCharts()
})

// 监听标签页切换
const handleTabChange = async (tab: string) => {
  activeTab.value = tab
  if (tab === 'aging') {
    await refreshAgingReport()
  } else if (tab === 'forecast') {
    await refreshForecastReport()
  } else if (tab === 'credit') {
    await refreshCreditReport()
  }
}



// 获取应收款列表
const getAccountsReceivableList = async (params: any) => {
  try {
    accountsReceivableLoading.value = true
    const result = await erpApi.finance.getAccountsReceivable({
      page: params.page || accountsReceivablePage.value,
      size: params.size || accountsReceivableSize.value,
      ...params
    })
    const page = unwrapPageResponse<any>(result)
    const data = page.list
    accountsReceivableList.value = data
    accountsReceivableTotal.value = page.total || data.length || 0
    accountsReceivablePage.value = params.page || accountsReceivablePage.value
    accountsReceivableSize.value = params.size || accountsReceivableSize.value

    refreshOverviewByList(data)
  } catch (error) {
    ErrorHandler.handleApiError(error)
    accountsReceivableList.value = []
    accountsReceivableTotal.value = 0
    refreshOverviewByList([])
  } finally {
    accountsReceivableLoading.value = false
  }
}

// 加载客户列表
const loadCustomers = async () => {
  try {
    const result = await erpApi.basicData.getCustomers({ page: 1, size: 100 })
    customerList.value = unwrapListResponse<any>(result)
  } catch (error) {
    ErrorHandler.handleApiError(error)
    customerList.value = []
  }
}

// 搜索应收款
const handleSearch = (params: any = {}) => {
  getAccountsReceivableList(params)
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  accountsReceivableSize.value = size
  getAccountsReceivableList({ page: accountsReceivablePage.value, size })
}

// 页码改变
const handleCurrentChange = (page: number) => {
  accountsReceivablePage.value = page
  getAccountsReceivableList({ page, size: accountsReceivableSize.value })
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

// 发送催款通知
const sendCollectionNotice = async (row: any) => {
  try {
    const res = await erpApi.finance.sendReceivableCollectionNotice(Number(row.id))
    const data = unwrapResponseData<any>(res) || {}
    const noticeId = data?.notice_id || data?.noticeId
    ElMessage.success(noticeId ? `催款通知发送成功：${noticeId}` : '催款通知发送成功')
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

const refreshOverviewByList = (list: any[]) => {
  const safeList = Array.isArray(list) ? list : []
  const totalAmount = safeList.reduce((sum, row) => sum + Number(row.amount || row.invoice_amount || 0), 0)
  const paidAmount = safeList.reduce((sum, row) => sum + Number(row.paid_amount || row.received_amount || 0), 0)
  const unpaidAmount = safeList.reduce((sum, row) => sum + Number(row.unpaid_amount || row.balance_amount || 0), 0)
  overviewData.value = {
    totalAmount: `¥${totalAmount.toFixed(2)}`,
    receivedAmount: `¥${paidAmount.toFixed(2)}`,
    unreceivedAmount: `¥${unpaidAmount.toFixed(2)}`,
    overdueAmount: '¥0.00'
  }
}

// 保存收款单
const handleReceiptSave = async () => {
  if (!receiptFormRef.value) return
  
  try {
    await receiptFormRef.value.validate()
    await erpApi.finance.createReceipt(receiptForm.value as any)
    // 关闭对话框
    receiptFormVisible.value = false
    // 刷新列表
    handleSearch()
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 刷新数据概览
const refreshOverview = async () => {
  try {
    refreshOverviewByList(accountsReceivableList.value as any)
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
  }
}

// 刷新账龄分析报表
const refreshAgingReport = async () => {
  try {
    const res = await erpApi.finance.getReceivableAgingReport()
    const dist = unwrapResponseData<any>(res)?.distribution || []
    agingData.value = (Array.isArray(dist) ? dist : []).map((it: any) => ({
      ageGroup: it.age_group || it.ageGroup,
      count: Number(it.count) || 0,
      amount: formatAmount(it.amount),
      percentage: Number(it.percentage) || 0
    }))
    renderAgingChart()
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    renderAgingChart()
  }
}

// 刷新回款预测报表
const refreshForecastReport = async () => {
  try {
    const res = await erpApi.finance.getReceivableForecastReport()
    const points = unwrapResponseData<any>(res)?.points || []
    forecastData.value = Array.isArray(points) ? points : []
    renderForecastChart()
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
    renderForecastChart()
  }
}

// 刷新客户信用评估报表
const refreshCreditReport = async () => {
  try {
    const res = await erpApi.finance.getReceivableCreditReport()
    const rows = unwrapListResponse<any>(res)
    creditData.value = (Array.isArray(rows) ? rows : []).map((it: any) => ({
      customer_name: it.customer_name || it.customerName,
      credit_score: Number(it.credit_score ?? it.creditScore) || 0,
      credit_level: it.credit_level || it.creditLevel,
      credit_level_type: it.credit_level_type || it.creditLevelType,
      overdue_count: Number(it.overdue_count ?? it.overdueCount) || 0,
      overdue_amount: formatAmount(it.overdue_amount ?? it.overdueAmount)
    }))
  } catch (error) {
    ErrorHandler.handleApiError(error, false)
  }
}

const formatAmount = (v: any) => {
  const n = typeof v === 'number' ? v : Number(v || 0)
  const safe = Number.isFinite(n) ? n : 0
  return `¥${safe.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

// 渲染账龄分析图表
const renderAgingChart = () => {
  if (!agingChartRef.value) return
  
  // 模拟图表数据
  const chartData = {
    legend: agingData.value.map(item => item.ageGroup),
    series: [{
      name: '金额',
      type: 'pie',
      radius: '70%',
      data: agingData.value.map(item => ({
        name: item.ageGroup,
        value: parseFloat(item.amount.replace(/[^\d.]/g, ''))
      })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  // 简化的图表渲染，实际项目中应使用ECharts等图表库
  const container = agingChartRef.value
  container.innerHTML = `<div style="text-align: center; padding: 40px; color: #909399;">账龄分析图表（示例）</div>`
}

// 渲染回款预测图表
const renderForecastChart = () => {
  if (!forecastChartRef.value) return
  
  // 简化的图表渲染，实际项目中应使用ECharts等图表库
  const container = forecastChartRef.value
  container.innerHTML = `<div style="text-align: center; padding: 40px; color: #909399;">回款预测图表（示例）</div>`
}

// 初始化图表
const initCharts = () => {
  // 延迟初始化，确保DOM已渲染
  setTimeout(() => {
    // 只有在当前标签页需要时才渲染对应图表
    if (activeTab.value === 'aging') {
      renderAgingChart()
    } else if (activeTab.value === 'forecast') {
      renderForecastChart()
    }
  }, 100)
}

// 计费模式变化处理
const handleBillingModeChange = () => {
  // 根据计费模式自动计算发票金额
  if (createBillForm.value.billing_mode === 'quantity') {
    createBillForm.value.invoice_amount = createBillForm.value.quantity * createBillForm.value.unit_price
  } else if (createBillForm.value.billing_mode === 'time') {
    createBillForm.value.invoice_amount = createBillForm.value.service_hours * createBillForm.value.hourly_rate
  }
}

// 创建应收账单
const handleCreateBill = async () => {
  if (!createBillFormRef.value) return
  
  try {
    await createBillFormRef.value.validate()
    // 调用API创建应收账单
    const receiptData = {
      bill_no: `BILL-${Date.now()}`,
      customer_id: createBillForm.value.customer_id.toString(),
      customer_name: '未知客户', // 实际项目中应该从客户选择器获取
      amount: createBillForm.value.invoice_amount,
      paid_amount: 0,
      unpaid_amount: createBillForm.value.invoice_amount,
      bill_date: createBillForm.value.invoice_date,
      due_date: createBillForm.value.due_date,
      status: 'unpaid' as AccountsReceivableStatus,
      remark: createBillForm.value.remark
    }
    await erpApi.finance.createReceipt(receiptData)
    
    // 关闭对话框
    createBillVisible.value = false
    // 刷新列表
    handleSearch()
    // 显示成功提示
    ElMessage.success('应收账单创建成功')
  } catch (error) {
    ErrorHandler.handleApiError(error)
  }
}

// 处理核销
const handleWriteOff = async () => {
  if (!writeOffFormRef.value || !selectedReceivable.value) return
  
  try {
    // 验证核销金额不能超过余额
    if (writeOffForm.value.write_off_amount > selectedReceivable.value.balance_amount) {
      throw new Error(`核销金额不能超过余额${selectedReceivable.value.balance_amount}`)
    }
    
    await writeOffFormRef.value.validate()
    await erpApi.finance.writeOffReceivable({
      receivableId: selectedReceivable.value.id,
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
.accounts-receivable-view {
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
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 20px;
      font-weight: bold;
      color: #1890ff;
      padding: 16px 20px;
      
      :deep(.el-tabs) {
        width: auto;
      }
    }
    
    // 报表区域
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
      
      // 账龄分析区域
      .aging-report {
        :deep(.el-card) {
          margin-bottom: 20px;
          
          .card-header {
            font-weight: bold;
            color: #303133;
          }
          
          .chart-container {
            height: 300px;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
      }
      
      // 回款预测区域
      .forecast-report {
        :deep(.el-card) {
          margin-bottom: 20px;
          
          .card-header {
            font-weight: bold;
            color: #303133;
          }
          
          .chart-container {
            height: 400px;
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
      }
      
      // 客户信用评估区域
      .credit-report {
        :deep(.el-card) {
          margin-bottom: 20px;
          
          .card-header {
            font-weight: bold;
            color: #303133;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .accounts-receivable-view {
    padding: 16px;
  }
}

@media (max-width: 768px) {
  .accounts-receivable-view {
    padding: 12px;
    
    .module-card {
      .card-header {
        font-size: 18px;
      }
    }
  }
}

// 详情对话框样式
.receivable-detail {
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
  
  .receipt-records {
    margin-top: 20px;
    
    h3 {
      font-size: 16px;
      font-weight: bold;
      color: #303133;
      margin-bottom: 16px;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebedf0;
    }
    
    .no-records {
      text-align: center;
      padding: 20px;
      color: #909399;
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

// 创建账单表单样式
.create-bill-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

// 核销表单样式
.write-off-form {
  .write-off-info {
    background-color: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 20px;
    
    .info-item {
      margin-bottom: 10px;
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
}
</style>
