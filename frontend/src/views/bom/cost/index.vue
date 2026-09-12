<template>
  <bom-layout title="BOM成本分析" :breadcrumb-items="[{ label: 'BOM分析工具' }, { label: '多层成本分析' }]">
    <!-- BOM选择区域 -->
    <div class="bom-select-area card-glossy">
      <el-form :model="bomForm" label-width="80px" inline>
        <el-form-item label="BOM选择">
          <el-select v-model="bomForm.selectedBom" placeholder="选择要分析的BOM" style="width: 240px" @change="handleBomChange">
            <el-option v-for="bom in bomList" :key="bom.id" :label="bom.name" :value="String(bom.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="版本">
          <el-select v-model="bomForm.selectedVersion" placeholder="选择版本" style="width: 120px">
            <el-option v-for="version in versionList" :key="version" :label="version" :value="version" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refreshCostData" :loading="loading">
            <el-icon><RefreshRight /></el-icon>
            刷新数据
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button @click="exportCostReport">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- BOM成本分析内容 -->
    <div class="cost-analysis-view">
      <!-- 成本汇总 -->
      <div class="cost-summary">
        <h3>成本汇总</h3>
        <div class="summary-stats">
          <el-card class="stat-card card-glossy">
            <div class="stat-content">
              <div class="stat-label">直接材料成本</div>
              <div class="stat-value">{{ formatCurrency(summaryData.materialCost) }}</div>
              <div class="stat-change" :class="summaryData.materialCostChange >= 0 ? 'increase' : 'decrease'">
                <el-icon>{{ summaryData.materialCostChange >= 0 ? 'ArrowUp' : 'ArrowDown' }}</el-icon>
                {{ Math.abs(summaryData.materialCostChange) }}%
              </div>
            </div>
          </el-card>
          <el-card class="stat-card card-glossy">
            <div class="stat-content">
              <div class="stat-label">直接人工成本</div>
              <div class="stat-value">{{ formatCurrency(summaryData.laborCost) }}</div>
              <div class="stat-change" :class="summaryData.laborCostChange >= 0 ? 'increase' : 'decrease'">
                <el-icon>{{ summaryData.laborCostChange >= 0 ? 'ArrowUp' : 'ArrowDown' }}</el-icon>
                {{ Math.abs(summaryData.laborCostChange) }}%
              </div>
            </div>
          </el-card>
          <el-card class="stat-card card-glossy">
            <div class="stat-content">
              <div class="stat-label">制造费用</div>
              <div class="stat-value">{{ formatCurrency(summaryData.overheadCost) }}</div>
              <div class="stat-change" :class="summaryData.overheadCostChange >= 0 ? 'increase' : 'decrease'">
                <el-icon>{{ summaryData.overheadCostChange >= 0 ? 'ArrowUp' : 'ArrowDown' }}</el-icon>
                {{ Math.abs(summaryData.overheadCostChange) }}%
              </div>
            </div>
          </el-card>
          <el-card class="stat-card card-glossy">
            <div class="stat-content">
              <div class="stat-label">总成本</div>
              <div class="stat-value total-cost">{{ formatCurrency(summaryData.totalCost) }}</div>
              <div class="stat-change" :class="summaryData.totalCostChange >= 0 ? 'increase' : 'decrease'">
                <el-icon><ArrowUp v-if="summaryData.totalCostChange >= 0" /><ArrowDown v-else /></el-icon>
                {{ Math.abs(summaryData.totalCostChange) }}%
              </div>
            </div>
          </el-card>
        </div>
      </div>
      
      <!-- 成本对比 -->
      <div class="cost-comparison card-glossy" v-if="showComparison">
        <h3>成本对比</h3>
        <div class="comparison-chart">
          <div ref="barChartRef" class="chart-container"></div>
        </div>
      </div>
      
      <!-- 成本明细 -->
      <div class="cost-detail card-glossy">
        <h3>成本明细</h3>
        <el-table :data="costDetails" style="width: 100%" stripe>
          <el-table-column prop="level" label="层级" width="80" />
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" min-width="200" />
          <el-table-column prop="spec" label="规格型号" min-width="150" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="usageQty" label="用量" width="80" />
          <el-table-column prop="unitPrice" label="单价" width="100" :formatter="formatCurrency" />
          <el-table-column prop="totalCost" label="总成本" width="120" :formatter="formatCurrency" />
          <el-table-column prop="costType" label="成本类型" width="120" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="showMaterialDetail(row)">
                <el-icon><View /></el-icon>
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 成本构成分析 -->
      <div class="cost-charts">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="chart-box card-glossy">
              <h3>成本构成分析</h3>
              <div ref="pieChartRef" class="chart-container"></div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="chart-box card-glossy">
              <h3>成本趋势分析</h3>
              <div ref="lineChartRef" class="chart-container"></div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </bom-layout>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Money, RefreshRight, Download, ArrowUp, ArrowDown, View } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { unwrapPageResponse, unwrapResponseData } from '@/api'
import { bomApi } from '@/api/bom'
import BomLayout from '../../../components/BomLayout.vue'

// 路由实例
const router = useRouter()

const pieChartRef = ref<HTMLElement | null>(null)
const lineChartRef = ref<HTMLElement | null>(null)
const barChartRef = ref<HTMLElement | null>(null)

// 加载状态
const loading = ref(false)

// BOM选择表单
const bomForm = reactive({
  selectedBom: '',
  selectedVersion: ''
})

// BOM列表
const bomList = ref<{ id: number; name: string }[]>([])

// 版本列表
const versionList = ref<string[]>([])
const versionToHeaderId = ref<Record<string, number>>({})

// 是否显示对比
const showComparison = ref(false)

// 基础统计
const summaryData = reactive({
  materialCost: 0,
  laborCost: 0,
  overheadCost: 0,
  totalCost: 0,
  materialCostChange: 0,
  laborCostChange: 0,
  overheadCostChange: 0,
  totalCostChange: 0
})

// 成本明细数据
const costDetails = ref<any[]>([])

// 同BOM各版本成本趋势数据（真实计算）
const trendData = ref<{ version: string; id: number; total: number }[]>([])

// 上一版本材料总成本（用于涨跌幅与对比图）
const previousCost = ref<number | null>(null)

// 格式化货币
const formatCurrency = (...args: any[]) => {
  const cellValue = args.length >= 3 ? args[2] : args[0]
  const value = typeof cellValue === 'number' ? cellValue : Number(cellValue ?? 0)
  return `¥ ${Number.isFinite(value) ? value.toFixed(2) : '0.00'}`
}

// 处理BOM变更
const handleBomChange = async () => {
  const headerId = Number(bomForm.selectedBom)
  if (!headerId) {
    versionList.value = []
    versionToHeaderId.value = {}
    bomForm.selectedVersion = ''
    return
  }

  try {
    const headerRes: any = await bomApi.getBomVersionDetail(headerId)
    const header = unwrapResponseData<any>(headerRes)
    const bomCode = header?.bomCode
    if (!bomCode) {
      versionList.value = []
      versionToHeaderId.value = {}
      bomForm.selectedVersion = ''
      return
    }

    const versionsRes: any = await bomApi.getBomVersions({ page: 1, size: 200, bomCode })
    const list = unwrapPageResponse<any>(versionsRes).list
    const map: Record<string, number> = {}
    const versions = (Array.isArray(list) ? list : [])
      .map((v: any) => {
        if (v?.version && v?.id) {
          map[String(v.version)] = Number(v.id)
        }
        return String(v?.version || '')
      })
      .filter((v: string) => !!v)

    versionToHeaderId.value = map
    versionList.value = versions
    bomForm.selectedVersion = String(header.version || versions[0] || '')
  } catch {
    versionList.value = []
    versionToHeaderId.value = {}
    bomForm.selectedVersion = ''
  }
}

const loadBomList = async () => {
  try {
    const res: any = await bomApi.getBomVersions({ page: 1, size: 200 })
    const list = unwrapPageResponse<any>(res).list
    bomList.value = (Array.isArray(list) ? list : []).map((h: any) => ({
      id: Number(h.id),
      name: `${h.materialName || h.materialCode || ''} - ${h.bomCode || ''} (${h.version || ''})`
    }))

    const first = bomList.value.length > 0 ? bomList.value[0] : undefined
    if (!bomForm.selectedBom && first) {
      bomForm.selectedBom = String(first.id)
      await handleBomChange()
    }
  } catch {
    bomList.value = []
  }
}

// 计算指定版本BOM的成本明细与材料总成本（纯计算，不更新界面状态）
const computeCostByHeaderId = async (headerId: number) => {
  const headerRes: any = await bomApi.getBomVersionDetail(headerId)
  const header = unwrapResponseData<any>(headerRes)
  if (!header?.materialId) {
    throw new Error('BOM版本数据不完整')
  }

  const treeRes: any = await bomApi.getBomTree(Number(header.materialId), header.version || undefined)
  const tree = unwrapResponseData<any>(treeRes)
  const nodes = Array.isArray(tree?.nodes) ? tree.nodes : []

  // 物料信息缓存：单价 + 规格，避免同一物料重复请求
  const materialCache = new Map<number, { price: number; spec: string }>()
  const getMaterialInfo = async (materialId: number) => {
    const cached = materialCache.get(materialId)
    if (cached) return cached
    const res: any = await bomApi.getMaterialDetail(materialId)
    const data = unwrapResponseData<any>(res)
    const rawPrice = data?.unitPrice
    const price = rawPrice === null || rawPrice === undefined ? 0 : Number(rawPrice)
    const info = {
      price: Number.isFinite(price) ? price : 0,
      spec: data?.materialSpec || ''
    }
    materialCache.set(materialId, info)
    return info
  }

  const rows: any[] = []
  const traverse = async (node: any, multiplier: number) => {
    const line = node?.line || {}
    const qty = Number(line.quantity ?? 0)
    const effQty = multiplier * qty
    const materialId = Number(line.childMaterialId ?? 0)
    const info = materialId > 0 ? await getMaterialInfo(materialId) : { price: 0, spec: '' }
    const totalCost = effQty * info.price
    rows.push({
      level: Number(line.level ?? 1),
      materialId,
      materialCode: line.childMaterialCode || '',
      materialName: line.childMaterialName || '',
      spec: info.spec,
      unit: line.unit || '件',
      usageQty: effQty,
      unitPrice: info.price,
      totalCost,
      costType: '直接材料'
    })
    const children = Array.isArray(node?.children) ? node.children : []
    for (const c of children) {
      await traverse(c, effQty)
    }
  }

  for (const n of nodes) {
    await traverse(n, 1)
  }

  const materialCost = rows.reduce((sum, r) => sum + Number(r.totalCost ?? 0), 0)
  return { rows, materialCost, header }
}

// 加载并展示指定版本的成本数据
const loadCostByHeaderId = async (headerId: number) => {
  const { rows, materialCost } = await computeCostByHeaderId(headerId)
  costDetails.value = rows
  summaryData.materialCost = materialCost
  summaryData.laborCost = 0
  summaryData.overheadCost = 0
  summaryData.totalCost = materialCost
}

// 计算涨跌幅百分比（上一版本为0时返回0，避免除零）
const calcChangePct = (current: number, previous: number) => {
  if (!previous) return 0
  return Number((((current - previous) / previous) * 100).toFixed(2))
}

// 加载同BOM编码各版本成本趋势与上一版本对比（真实数据）
const loadVersionTrend = async (currentHeaderId: number) => {
  const entries = Object.entries(versionToHeaderId.value)
    .map(([version, id]) => ({ version, id: Number(id) }))
  if (entries.length === 0) {
    trendData.value = []
    previousCost.value = null
    return
  }
  // 逐版本计算材料总成本
  const costs: { version: string; id: number; total: number }[] = []
  for (const e of entries) {
    try {
      const { materialCost } = await computeCostByHeaderId(e.id)
      costs.push({ version: e.version, id: e.id, total: materialCost })
    } catch {
      costs.push({ version: e.version, id: e.id, total: 0 })
    }
  }
  trendData.value = costs
  // 上一版本：非当前版本中的第一个
  const prev = costs.find(c => c.id !== Number(currentHeaderId))
  previousCost.value = prev ? prev.total : null
  // 涨跌幅
  summaryData.materialCostChange = previousCost.value !== null
    ? calcChangePct(summaryData.materialCost, previousCost.value)
    : 0
  summaryData.laborCostChange = 0
  summaryData.overheadCostChange = 0
  summaryData.totalCostChange = summaryData.materialCostChange
  // 有两个及以上版本时显示对比图
  showComparison.value = previousCost.value !== null
}

// 刷新成本数据
const refreshCostData = async () => {
  if (!bomForm.selectedBom) {
    ElMessage.warning('请先选择BOM')
    return
  }
  
  try {
    loading.value = true
    const selectedVersion = String(bomForm.selectedVersion || '').trim()
    const headerIdByVersion = selectedVersion ? versionToHeaderId.value[selectedVersion] : 0
    const headerId = headerIdByVersion || Number(bomForm.selectedBom)
    await loadCostByHeaderId(headerId)
    await loadVersionTrend(headerId)
    ElMessage.success('成本数据刷新成功')
    // 刷新图表
    await nextTick()
    initPieChart()
    initLineChart()
    if (showComparison.value) {
      initBarChart()
    }
  } catch (error) {
    ElMessage.error('刷新成本数据失败')
  } finally {
    loading.value = false
  }
}

// 导出成本报告（CSV格式，含汇总与明细）
const exportCostReport = () => {
  if (costDetails.value.length === 0) {
    ElMessage.warning('暂无成本数据可导出')
    return
  }
  // 组装CSV内容：汇总信息 + 明细表
  const summaryLines = [
    '成本汇总',
    `直接材料,${summaryData.materialCost.toFixed(2)}`,
    `直接人工,${summaryData.laborCost.toFixed(2)}`,
    `制造费用,${summaryData.overheadCost.toFixed(2)}`,
    `总成本,${summaryData.totalCost.toFixed(2)}`,
    ''
  ]
  const header = '层级,物料编码,物料名称,单位,用量,单价,总成本,成本类型'
  const detailLines = costDetails.value.map((row: any) =>
    [row.level, row.materialCode, row.materialName, row.unit, row.usageQty, row.unitPrice, row.totalCost?.toFixed(2), row.costType]
      .map((cell: any) => `"${String(cell ?? '').replace(/"/g, '""')}"`)
      .join(',')
  )
  // 加BOM头避免Excel打开中文乱码
  const csvContent = '﻿' + [...summaryLines, header, ...detailLines].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `bom_cost_report_${Date.now()}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  ElMessage.success('成本报告导出成功')
}

// 查看物料详情：跳转物料档案详情页（路径参数形式）
const showMaterialDetail = (row: any) => {
  if (!row?.materialId) {
    ElMessage.warning('该行缺少物料ID，无法查看详情')
    return
  }
  router.push(`/home/bom/material/detail/${row.materialId}`)
}

// 初始化饼图
const initPieChart = () => {
  if (!pieChartRef.value) return
  const myChart = echarts.init(pieChartRef.value)
  const option = {
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: ¥{c} ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    series: [
      {
        name: '成本构成',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false, position: 'center' },
        emphasis: { label: { show: true, fontSize: 20, fontWeight: 'bold' } },
        labelLine: { show: false },
        data: [
          { value: summaryData.materialCost, name: '直接材料' },
          { value: summaryData.laborCost, name: '直接人工' },
          { value: summaryData.overheadCost, name: '制造费用' }
        ]
      }
    ]
  }
  myChart.setOption(option)
}

// 初始化折线图（使用同BOM各版本的真实成本趋势）
const initLineChart = () => {
  if (!lineChartRef.value) return
  const myChart = echarts.init(lineChartRef.value)
  const labels = trendData.value.length > 0 ? trendData.value.map(t => t.version) : ['当前版本']
  const values = trendData.value.length > 0 ? trendData.value.map(t => Number(t.total.toFixed(2))) : [summaryData.totalCost]
  const option = {
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value' },
    tooltip: { trigger: 'axis' },
    series: [
      {
        name: '总成本',
        data: values,
        type: 'line',
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0)' }
          ])
        }
      }
    ]
  }
  myChart.setOption(option)
}

// 初始化柱状图（当前版本 vs 上一版本，真实数据）
const initBarChart = () => {
  if (!barChartRef.value) return
  const myChart = echarts.init(barChartRef.value)
  const prev = previousCost.value ?? 0
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['当前版本', '上一版本'] },
    xAxis: { type: 'category', data: ['直接材料', '直接人工', '制造费用', '总成本'] },
    yAxis: { type: 'value' },
    series: [
      {
        name: '当前版本',
        type: 'bar',
        data: [summaryData.materialCost, summaryData.laborCost, summaryData.overheadCost, summaryData.totalCost],
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '上一版本',
        type: 'bar',
        data: [prev, 0, 0, prev],
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  myChart.setOption(option)
}

onMounted(async () => {
  await loadBomList()
  // 自动加载默认BOM的成本数据，避免首屏空白
  if (bomForm.selectedBom) {
    try {
      loading.value = true
      const headerId = versionToHeaderId.value[String(bomForm.selectedVersion)] || Number(bomForm.selectedBom)
      await loadCostByHeaderId(headerId)
      await loadVersionTrend(headerId)
    } catch {
      // 首屏加载失败时保持空态，由用户手动刷新重试
    } finally {
      loading.value = false
    }
  }
  await nextTick()
  initPieChart()
  initLineChart()
  if (showComparison.value) {
    initBarChart()
  }
})
</script>

<style scoped>
.cost-analysis-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.bom-select-area {
  margin-bottom: 24px;
  padding: 20px;
}

.action-area {
  margin: 16px 0;
  display: flex;
  justify-content: flex-end;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 1.2rem;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 1.4rem;
  color: #409eff;
}

.cost-content {
  padding: 0;
}

.cost-summary h3,
.cost-detail h3,
.cost-trend h3,
.cost-comparison h3 {
  margin: 0 0 20px 0;
  font-size: 1.1rem;
  color: #303133;
  border-left: 3px solid #409eff;
  padding-left: 10px;
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 30px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
}

.stat-content {
  text-align: center;
  padding: 20px;
}

.stat-label {
  font-size: 0.9rem;
  color: #606266;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-change {
  font-size: 0.8rem;
  font-weight: 500;
}

.stat-change.increase {
  color: #f56c6c;
}

.stat-change.decrease {
  color: #67c23a;
}

.total-cost {
  color: #f56c6c;
}

.cost-detail {
  margin-bottom: 30px;
  padding: 20px;
}

.cost-comparison {
  margin-bottom: 30px;
  padding: 20px;
}

.comparison-chart {
  margin-top: 16px;
}

.cost-trend {
  margin-bottom: 30px;
}

.cost-charts {
  margin-top: 30px;
}

.chart-box {
  padding: 20px;
  margin-bottom: 20px;
}

.chart-box h3 {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.loading-container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  margin: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cost-analysis-view {
    padding: 12px;
  }
  
  .bom-select-area {
    padding: 12px;
  }
  
  .summary-stats {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .stat-content {
    padding: 15px;
  }
  
  .chart-container {
    height: 250px;
    padding: 10px;
  }
  
  .cost-detail,
  .cost-comparison,
  .chart-box {
    padding: 12px;
  }
}
</style>
