<template>
  <div class="purchase-reconciliation">
    <div class="header">
      <div class="title">采购对账差异</div>
      <div class="actions">
        <el-switch v-model="onlyMismatch" active-text="仅差异" @change="reload" />
        <el-button type="primary" @click="reload" :loading="loading">刷新</el-button>
        <el-button @click="exportExcel" :loading="exporting">导出Excel</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" label-width="80px">
        <el-form-item label="采购单号">
          <el-input v-model="filters.orderNo" placeholder="模糊匹配" clearable @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="供应商编码">
          <el-input v-model="filters.supplierCode" placeholder="模糊匹配" clearable @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="供应商名称">
          <el-input v-model="filters.supplierName" placeholder="模糊匹配" clearable @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="物料编码">
          <el-input v-model="filters.materialCode" placeholder="精确匹配" clearable @keyup.enter="reload" />
        </el-form-item>
        <el-form-item label="创建日期">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="onFilterChange"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="12" class="summary-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric">
            <div class="label">扫描订单</div>
            <div class="value">{{ summary.totalOrders ?? '-' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric">
            <div class="label">差异订单</div>
            <div class="value warn">{{ summary.mismatchOrders ?? '-' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric">
            <div class="label">质检HOLD</div>
            <div class="value danger">{{ summary.qcHoldOrders ?? '-' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric">
            <div class="label">部分/全到货</div>
            <div class="value">{{ (summary.partialReceivedOrders ?? 0) + (summary.receivedOrders ?? 0) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <el-table :data="rows" v-loading="loading" style="width: 100%">
        <el-table-column prop="orderNo" label="采购单号" min-width="160" />
        <el-table-column prop="supplierName" label="供应商" min-width="160" />
        <el-table-column prop="orderStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.orderStatus)">
              {{ statusLabel(row.orderStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="headAmount" label="头金额" width="120">
          <template #default="{ row }">{{ fmtMoney(row.headAmount) }}</template>
        </el-table-column>
        <el-table-column prop="itemAmountSum" label="行汇总" width="120">
          <template #default="{ row }">{{ fmtMoney(row.itemAmountSum) }}</template>
        </el-table-column>
        <el-table-column prop="amountDiff" label="差额" width="120">
          <template #default="{ row }">
            <span :class="{ danger: row.amountMismatch }">{{ fmtMoney(row.amountDiff) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderedQtySum" label="应收" width="120">
          <template #default="{ row }">{{ fmtNumber(row.orderedQtySum) }}</template>
        </el-table-column>
        <el-table-column prop="receivedQtySum" label="已收" width="120">
          <template #default="{ row }">{{ fmtNumber(row.receivedQtySum) }}</template>
        </el-table-column>
        <el-table-column prop="receiveRate" label="到货率" width="120">
          <template #default="{ row }">{{ fmtRate(row.receiveRate) }}</template>
        </el-table-column>
        <el-table-column prop="hasMismatch" label="差异" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.hasMismatch" type="danger">是</el-tag>
            <el-tag v-else type="success">否</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @current-change="reload"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>

    <el-row :gutter="12" class="agg-row">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>
            <div class="card-title">差异原因汇总</div>
          </template>
          <div class="reason-grid">
            <div class="reason-item">
              <div class="label">差异订单</div>
              <div class="value warn">{{ reasonSummary.mismatchOrders ?? '-' }}</div>
            </div>
            <div class="reason-item">
              <div class="label">质检HOLD</div>
              <div class="value danger">{{ reasonSummary.qcHoldOrders ?? '-' }}</div>
            </div>
            <div class="reason-item">
              <div class="label">金额差异</div>
              <div class="value warn">{{ reasonSummary.amountMismatchOrders ?? '-' }}</div>
            </div>
            <div class="reason-item">
              <div class="label">超收异常</div>
              <div class="value warn">{{ reasonSummary.overReceivedOrders ?? '-' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never">
          <template #header>
            <div class="card-title">供应商聚合</div>
          </template>
          <el-table :data="supplierAggRows" v-loading="loading" style="width: 100%">
            <el-table-column prop="supplierName" label="供应商" min-width="160" />
            <el-table-column prop="supplierCode" label="编码" width="120" />
            <el-table-column prop="totalOrders" label="订单数" width="100" />
            <el-table-column prop="mismatchOrders" label="差异数" width="100">
              <template #default="{ row }">
                <span class="warn">{{ row.mismatchOrders }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="qcHoldOrders" label="HOLD" width="90">
              <template #default="{ row }">
                <span class="danger">{{ row.qcHoldOrders }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="amountMismatchOrders" label="金额差" width="90" />
            <el-table-column prop="overReceivedOrders" label="超收" width="90" />
          </el-table>
          <div class="pagination">
            <el-pagination
              v-model:current-page="supplierAggPage"
              v-model:page-size="supplierAggSize"
              :page-sizes="[5, 10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              :total="supplierAggTotal"
              @current-change="reloadSupplierAgg"
              @size-change="onSupplierAggSizeChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-title">物料聚合</div>
      </template>
      <el-table :data="materialAggRows" v-loading="loading" style="width: 100%">
        <el-table-column prop="materialCode" label="物料编码" min-width="140" />
        <el-table-column prop="materialName" label="物料名称" min-width="180" />
        <el-table-column prop="orderCount" label="涉及订单" width="100" />
        <el-table-column prop="orderedQtySum" label="应收汇总" width="120">
          <template #default="{ row }">{{ fmtNumber(row.orderedQtySum) }}</template>
        </el-table-column>
        <el-table-column prop="receivedQtySum" label="已收汇总" width="120">
          <template #default="{ row }">{{ fmtNumber(row.receivedQtySum) }}</template>
        </el-table-column>
        <el-table-column prop="overReceivedLines" label="超收行数" width="110">
          <template #default="{ row }">
            <span class="warn">{{ row.overReceivedLines }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="materialAggPage"
          v-model:page-size="materialAggSize"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="materialAggTotal"
          @current-change="reloadMaterialAgg"
          @size-change="onMaterialAggSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { reportApi } from '@/api/scm'

type Summary = {
  scannedLimit?: number
  totalOrders?: number
  qcHoldOrders?: number
  partialReceivedOrders?: number
  receivedOrders?: number
  mismatchOrders?: number
}

type ReasonSummary = {
  totalOrders?: number
  qcHoldOrders?: number
  amountMismatchOrders?: number
  overReceivedOrders?: number
  mismatchOrders?: number
}

const loading = ref(false)
const exporting = ref(false)
const onlyMismatch = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const rows = ref<any[]>([])
const summary = reactive<Summary>({})
const reasonSummary = reactive<ReasonSummary>({})

const supplierAggPage = ref(1)
const supplierAggSize = ref(10)
const supplierAggTotal = ref(0)
const supplierAggRows = ref<any[]>([])

const materialAggPage = ref(1)
const materialAggSize = ref(10)
const materialAggTotal = ref(0)
const materialAggRows = ref<any[]>([])

const filters = reactive({
  orderNo: '',
  supplierCode: '',
  supplierName: '',
  materialCode: '',
  dateRange: [] as string[]
})

const buildParams = () => {
  const startDate = Array.isArray(filters.dateRange) && filters.dateRange.length > 0 ? filters.dateRange[0] : undefined
  const endDate = Array.isArray(filters.dateRange) && filters.dateRange.length > 1 ? filters.dateRange[1] : undefined
  return {
    orderNo: filters.orderNo || undefined,
    supplierCode: filters.supplierCode || undefined,
    supplierName: filters.supplierName || undefined,
    materialCode: filters.materialCode || undefined,
    startDate,
    endDate
  }
}

const resetFilters = () => {
  filters.orderNo = ''
  filters.supplierCode = ''
  filters.supplierName = ''
  filters.materialCode = ''
  filters.dateRange = []
  page.value = 1
  supplierAggPage.value = 1
  materialAggPage.value = 1
  reload()
}

const onFilterChange = () => {
  page.value = 1
  supplierAggPage.value = 1
  materialAggPage.value = 1
  reload()
}

const loadSummary = async () => {
  const { data } = await reportApi.getPurchaseReconciliationSummary({ limit: 1000, ...buildParams() })
  Object.assign(summary, data || {})
}

const loadPage = async () => {
  const { data } = await reportApi.getPurchaseReconciliationPage({
    page: page.value,
    size: size.value,
    onlyMismatch: onlyMismatch.value,
    ...buildParams()
  })
  total.value = Number(data?.total ?? 0)
  rows.value = Array.isArray(data?.list) ? data.list : []
}

const loadReasonSummary = async () => {
  const { data } = await reportApi.getPurchaseReconciliationReasonSummary({ ...buildParams() })
  Object.assign(reasonSummary, data || {})
}

const loadSupplierAgg = async () => {
  const { data } = await reportApi.getPurchaseReconciliationSupplierAggPage({
    page: supplierAggPage.value,
    size: supplierAggSize.value,
    ...buildParams()
  })
  supplierAggTotal.value = Number(data?.total ?? 0)
  supplierAggRows.value = Array.isArray(data?.list) ? data.list : []
}

const loadMaterialAgg = async () => {
  const { data } = await reportApi.getPurchaseReconciliationMaterialAggPage({
    page: materialAggPage.value,
    size: materialAggSize.value,
    ...buildParams()
  })
  materialAggTotal.value = Number(data?.total ?? 0)
  materialAggRows.value = Array.isArray(data?.list) ? data.list : []
}

const exportExcel = async () => {
  exporting.value = true
  try {
    const res = await reportApi.exportPurchaseReconciliation({
      onlyMismatch: onlyMismatch.value,
      limit: 20000,
      ...buildParams()
    })
    const blob = new Blob([res.data], { type: res.headers?.['content-type'] || 'application/octet-stream' })
    const cd = String(res.headers?.['content-disposition'] || '')
    const filename = parseFilename(cd) || 'purchase-reconciliation.xlsx'
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    a.click()
    URL.revokeObjectURL(url)
  } catch (e: any) {
    ElMessage.error(e?.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

const parseFilename = (contentDisposition: string) => {
  const m1 = contentDisposition.match(/filename\*\=UTF-8''([^;]+)/i)
  if (m1 && m1[1]) {
    try {
      return decodeURIComponent(m1[1])
    } catch {
      return m1[1]
    }
  }
  const m2 = contentDisposition.match(/filename=\"?([^\";]+)\"?/i)
  return m2 && m2[1] ? m2[1] : ''
}

const reload = async () => {
  loading.value = true
  try {
    await Promise.all([loadSummary(), loadPage(), loadReasonSummary(), loadSupplierAgg(), loadMaterialAgg()])
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const onSizeChange = () => {
  page.value = 1
  reload()
}

const reloadSupplierAgg = () => {
  loadSupplierAgg().catch((e: any) => ElMessage.error(e?.message || '加载失败'))
}

const onSupplierAggSizeChange = () => {
  supplierAggPage.value = 1
  reloadSupplierAgg()
}

const reloadMaterialAgg = () => {
  loadMaterialAgg().catch((e: any) => ElMessage.error(e?.message || '加载失败'))
}

const onMaterialAggSizeChange = () => {
  materialAggPage.value = 1
  reloadMaterialAgg()
}

const fmtMoney = (v: any) => {
  const n = Number(v ?? 0)
  return n.toFixed(2)
}

const fmtNumber = (v: any) => {
  const n = Number(v ?? 0)
  return Number.isFinite(n) ? n.toString() : '0'
}

const fmtRate = (v: any) => {
  const n = Number(v ?? 0)
  if (!Number.isFinite(n)) return '0%'
  return `${(n * 100).toFixed(2)}%`
}

const statusLabel = (s: any) => {
  const v = Number(s)
  if (v === 10) return '已创建'
  if (v === 20) return '已提交'
  if (v === 30) return '已审批'
  if (v === 40) return '已确认'
  if (v === 50) return '部分到货'
  if (v === 58) return '质检HOLD'
  if (v === 60) return '已到货'
  if (v === 70) return '已关闭'
  if (v === 90) return '已取消'
  return String(s ?? '-')
}

const statusTagType = (s: any) => {
  const v = Number(s)
  if (v === 58) return 'danger'
  if (v === 50) return 'warning'
  if (v === 60) return 'success'
  if (v === 40) return 'info'
  return ''
}

onMounted(() => {
  reload()
})
</script>

<style scoped>
.purchase-reconciliation {
  padding: 12px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.summary-row {
  margin-bottom: 12px;
}
.filter-card {
  margin-bottom: 12px;
}
.metric {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.metric .label {
  color: #909399;
  font-size: 12px;
}
.metric .value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}
.metric .value.warn {
  color: #e6a23c;
}
.metric .value.danger {
  color: #f56c6c;
}
.danger {
  color: #f56c6c;
}
.table-card {
  margin-top: 12px;
}
.agg-row {
  margin-top: 12px;
}
.card-title {
  font-weight: 600;
}
.warn {
  color: #e6a23c;
}
.reason-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.reason-item .label {
  color: #909399;
  font-size: 12px;
}
.reason-item .value {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 12px;
}
</style>
