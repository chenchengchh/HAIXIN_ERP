<template>
  <div class="what-if-analysis">
    <div class="analysis-header">
      <h3>What-If 场景分析</h3>
      <el-button type="primary" size="small" @click="createNewScenario">
        <el-icon><Plus /></el-icon>
        新建场景
      </el-button>
    </div>
    
    <div class="analysis-content">
      <!-- 场景列表 -->
      <div class="scenario-list">
        <el-card v-for="scenario in scenarios" :key="scenario.id" class="scenario-card">
          <template #header>
            <div class="scenario-header">
              <div class="scenario-info">
                <h4>{{ scenario.name }}</h4>
                <el-tag :type="getScenarioStatusType(scenario.status)" size="small">
                  {{ getScenarioStatusText(scenario.status) }}
                </el-tag>
              </div>
              <div class="scenario-actions">
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="runScenario(scenario)"
                  :loading="scenario.status === 'running'"
                  :disabled="scenario.status === 'running'">
                  <el-icon><VideoPlay /></el-icon>
                  运行
                </el-button>
                <el-button 
                  type="info" 
                  size="small" 
                  @click="viewResults(scenario)"
                  :disabled="scenario.status !== 'completed'">
                  <el-icon><View /></el-icon>
                  结果
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  @click="deleteScenario(scenario)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
          
          <div class="scenario-details">
            <el-descriptions :column="2" size="small">
              <el-descriptions-item label="创建时间">{{ formatDate(scenario.createdAt) }}</el-descriptions-item>
              <el-descriptions-item label="运行时间">{{ scenario.runTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="参数变化">{{ scenario.parameterCount }} 项</el-descriptions-item>
              <el-descriptions-item label="预期影响">{{ scenario.impact }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>
      
      <!-- 参数配置面板 -->
      <div class="parameter-panel" v-if="showParameterPanel">
        <el-card>
          <template #header>
            <div class="panel-header">
              <span>场景参数配置</span>
              <el-button link @click="closeParameterPanel">
            <el-icon><Close /></el-icon>
          </el-button>
            </div>
          </template>
          
          <el-form :model="currentScenario" label-position="top" size="small">
            <el-form-item label="场景名称">
              <el-input v-model="currentScenario.name" placeholder="请输入场景名称" />
            </el-form-item>
            
            <el-form-item label="场景类型">
              <el-radio-group v-model="currentScenario.type">
              <el-radio value="order">订单变更</el-radio>
              <el-radio value="resource">资源变更</el-radio>
              <el-radio value="process">工艺变更</el-radio>
            </el-radio-group>
            </el-form-item>
            
            <!-- 订单变更参数 -->
            <div v-if="currentScenario.type === 'order'">
              <el-form-item label="订单数量变化">
                <el-input-number 
                  v-model="currentScenario.parameters.orderChange" 
                  :min="-100" 
                  :max="100" 
                  :step="10"
                  :precision="0" />
                <span style="margin-left: 10px;">%</span>
              </el-form-item>
              <el-form-item label="交期调整">
                <el-input-number 
                  v-model="currentScenario.parameters.deliveryAdjustment" 
                  :min="-30" 
                  :max="30" 
                  :step="1" />
                <span style="margin-left: 10px;">天</span>
              </el-form-item>
            </div>
            
            <!-- 资源变更参数 -->
            <div v-if="currentScenario.type === 'resource'">
              <el-form-item label="资源数量变化">
                <el-input-number 
                  v-model="currentScenario.parameters.resourceChange" 
                  :min="-50" 
                  :max="50" 
                  :step="5" />
                <span style="margin-left: 10px;">%</span>
              </el-form-item>
              <el-form-item label="设备故障率">
                <el-input-number 
                  v-model="currentScenario.parameters.failureRate" 
                  :min="0" 
                  :max="30" 
                  :step="1" />
                <span style="margin-left: 10px;">%</span>
              </el-form-item>
            </div>
            
            <!-- 工艺变更参数 -->
            <div v-if="currentScenario.type === 'process'">
              <el-form-item label="工艺时间调整">
                <el-input-number 
                  v-model="currentScenario.parameters.processTimeChange" 
                  :min="-50" 
                  :max="50" 
                  :step="5" />
                <span style="margin-left: 10px;">%</span>
              </el-form-item>
              <el-form-item label="工序顺序调整">
                <el-switch v-model="currentScenario.parameters.processOrderChange" />
              </el-form-item>
            </div>
            
            <el-form-item>
              <el-button type="primary" @click="saveScenario">保存场景</el-button>
              <el-button @click="closeParameterPanel">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      
      <!-- 对比分析结果 -->
      <div class="comparison-results" v-if="showComparison">
        <el-card>
          <template #header>
            <div class="panel-header">
              <span>场景对比分析</span>
              <el-button link @click="closeComparison">
            <el-icon><Close /></el-icon>
          </el-button>
            </div>
          </template>
          
          <div class="comparison-chart">
            <div ref="comparisonChart" class="chart-container"></div>
          </div>
          
          <el-table :data="comparisonData" style="width: 100%; margin-top: 20px;">
            <el-table-column prop="metric" label="指标" width="150" />
            <el-table-column prop="baseline" label="基准值" width="120" />
            <el-table-column prop="scenario" label="场景值" width="120" />
            <el-table-column prop="change" label="变化" width="120">
              <template #default="{ row }">
                <span :class="getChangeClass(row.change)">
                  {{ formatChange(row.change) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="impact" label="影响分析" />
          </el-table>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Plus, VideoPlay, View, Delete, Close } from '@element-plus/icons-vue'

// 定义场景类型
interface Scenario {
  id: string
  name: string
  type: 'order' | 'resource' | 'process'
  status: 'draft' | 'ready' | 'running' | 'completed' | 'failed'
  parameters: {
    orderChange?: number
    deliveryAdjustment?: number
    resourceChange?: number
    failureRate?: number
    processTimeChange?: number
    processOrderChange?: boolean
  }
  createdAt: Date
  runTime?: string
  parameterCount: number
  impact: string
  results?: any
}

interface ComparisonMetric {
  metric: string
  baseline: number
  scenario: number
  change: number
  impact: string
}

// 响应式数据
const scenarios = ref<Scenario[]>([])
const showParameterPanel = ref(false)
const showComparison = ref(false)
const currentScenario = reactive<Scenario>({
  id: '',
  name: '',
  type: 'order',
  status: 'draft',
  parameters: {},
  createdAt: new Date(),
  parameterCount: 0,
  impact: '中等'
})

const comparisonChart = ref<HTMLElement>()
const comparisonData = ref<ComparisonMetric[]>([])
let chartInstance: echarts.ECharts | null = null

// 生成模拟场景数据
const generateMockScenarios = (): Scenario[] => {
  return [
    {
      id: '1',
      name: '订单增加20%场景',
      type: 'order',
      status: 'completed',
      parameters: { orderChange: 20, deliveryAdjustment: 0 },
      createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000),
      runTime: '2.5分钟',
      parameterCount: 2,
      impact: '高',
      results: { totalDuration: 120, resourceUtilization: 85, delayedOrders: 3 }
    },
    {
      id: '2',
      name: '设备故障率5%场景',
      type: 'resource',
      status: 'completed',
      parameters: { resourceChange: -5, failureRate: 5 },
      createdAt: new Date(Date.now() - 48 * 60 * 60 * 1000),
      runTime: '1.8分钟',
      parameterCount: 2,
      impact: '中等',
      results: { totalDuration: 115, resourceUtilization: 78, delayedOrders: 2 }
    },
    {
      id: '3',
      name: '工艺时间缩短10%场景',
      type: 'process',
      status: 'ready',
      parameters: { processTimeChange: -10, processOrderChange: false },
      createdAt: new Date(Date.now() - 72 * 60 * 60 * 1000),
      parameterCount: 2,
      impact: '低',
      results: null
    }
  ]
}

// 获取场景状态类型
const getScenarioStatusType = (status: string): string => {
  const typeMap: Record<string, string> = {
    draft: 'info',
    ready: 'primary',
    running: 'warning',
    completed: 'success',
    failed: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取场景状态文本
const getScenarioStatusText = (status: string): string => {
  const textMap: Record<string, string> = {
    draft: '草稿',
    ready: '就绪',
    running: '运行中',
    completed: '已完成',
    failed: '失败'
  }
  return textMap[status] || status
}

// 创建新场景
const createNewScenario = () => {
  currentScenario.id = Date.now().toString()
  currentScenario.name = ''
  currentScenario.type = 'order'
  currentScenario.status = 'draft'
  currentScenario.parameters = {}
  currentScenario.createdAt = new Date()
  currentScenario.parameterCount = 0
  currentScenario.impact = '中等'
  showParameterPanel.value = true
}

// 保存场景
const saveScenario = () => {
  // 计算参数数量
  currentScenario.parameterCount = Object.keys(currentScenario.parameters).length
  
  // 添加到场景列表
  const existingIndex = scenarios.value.findIndex(s => s.id === currentScenario.id)
  if (existingIndex >= 0) {
    scenarios.value[existingIndex] = { ...currentScenario }
  } else {
    scenarios.value.push({ ...currentScenario })
  }
  
  // 关闭面板
  closeParameterPanel()
}

// 关闭参数面板
const closeParameterPanel = () => {
  showParameterPanel.value = false
}

// 运行场景
const runScenario = async (scenario: Scenario) => {
  scenario.status = 'running'
  
  try {
    // 模拟运行时间
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 模拟结果
    scenario.runTime = '2.1分钟'
    scenario.status = 'completed'
    scenario.results = {
      totalDuration: Math.round(100 + Math.random() * 40),
      resourceUtilization: Math.round(70 + Math.random() * 30),
      delayedOrders: Math.round(Math.random() * 5)
    }
    
    // 显示成功消息
    // ElMessage.success('场景运行完成')
  } catch (error) {
    scenario.status = 'failed'
    // ElMessage.error('场景运行失败')
  }
}

// 查看结果
const viewResults = (scenario: Scenario) => {
  // 生成对比数据
  generateComparisonData(scenario)
  showComparison.value = true
  
  // 初始化图表
  nextTick(() => {
    initComparisonChart()
  })
}

// 生成对比数据
const generateComparisonData = (scenario: Scenario) => {
  const baseline = {
    totalDuration: 100,
    resourceUtilization: 75,
    delayedOrders: 1,
    cost: 100000
  }
  
  const scenarioResults = scenario.results || {
    totalDuration: 120,
    resourceUtilization: 85,
    delayedOrders: 3,
    cost: 115000
  }
  
  comparisonData.value = [
    {
      metric: '总工期',
      baseline: baseline.totalDuration,
      scenario: scenarioResults.totalDuration,
      change: ((scenarioResults.totalDuration - baseline.totalDuration) / baseline.totalDuration) * 100,
      impact: scenarioResults.totalDuration > baseline.totalDuration ? '工期延长，需要优化' : '工期优化'
    },
    {
      metric: '资源利用率',
      baseline: baseline.resourceUtilization,
      scenario: scenarioResults.resourceUtilization,
      change: scenarioResults.resourceUtilization - baseline.resourceUtilization,
      impact: scenarioResults.resourceUtilization > baseline.resourceUtilization ? '利用率提升' : '利用率下降'
    },
    {
      metric: '延期订单',
      baseline: baseline.delayedOrders,
      scenario: scenarioResults.delayedOrders,
      change: scenarioResults.delayedOrders - baseline.delayedOrders,
      impact: scenarioResults.delayedOrders > baseline.delayedOrders ? '延期风险增加' : '延期风险降低'
    },
    {
      metric: '总成本',
      baseline: baseline.cost,
      scenario: scenarioResults.cost || baseline.cost,
      change: (((scenarioResults.cost || baseline.cost) - baseline.cost) / baseline.cost) * 100,
      impact: (scenarioResults.cost || baseline.cost) > baseline.cost ? '成本增加' : '成本降低'
    }
  ]
}

// 初始化对比图表
const initComparisonChart = () => {
  if (!comparisonChart.value) return
  
  chartInstance = echarts.init(comparisonChart.value)
  
  const option: echarts.EChartsOption = {
    title: {
      text: '场景对比分析',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['基准值', '场景值'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: comparisonData.value.map(item => item.metric)
    },
    yAxis: {
      type: 'value',
      name: '数值'
    },
    series: [
      {
        name: '基准值',
        type: 'bar',
        data: comparisonData.value.map(item => item.baseline),
        itemStyle: {
          color: '#909399'
        }
      },
      {
        name: '场景值',
        type: 'bar',
        data: comparisonData.value.map(item => item.scenario),
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

// 关闭对比面板
const closeComparison = () => {
  showComparison.value = false
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
}

// 删除场景
const deleteScenario = (scenario: Scenario) => {
  const index = scenarios.value.findIndex(s => s.id === scenario.id)
  if (index >= 0) {
    scenarios.value.splice(index, 1)
  }
}

// 格式化日期
const formatDate = (date: Date): string => {
  return date.toLocaleString('zh-CN')
}

// 获取变化样式
const getChangeClass = (change: number): string => {
  if (change > 0) return 'text-danger'
  if (change < 0) return 'text-success'
  return 'text-info'
}

// 格式化变化
const formatChange = (change: number): string => {
  if (change === 0) return '无变化'
  if (Math.abs(change) < 1) return change.toFixed(2)
  return change > 0 ? `+${change.toFixed(1)}` : change.toFixed(1)
}

// 组件挂载时初始化
onMounted(() => {
  scenarios.value = generateMockScenarios()
})
</script>

<style scoped>
.what-if-analysis {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  border-radius: 4px;
  box-sizing: border-box;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.analysis-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.analysis-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.scenario-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-bottom: 20px;
}

.scenario-card {
  transition: all 0.3s ease;
}

.scenario-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.scenario-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.scenario-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.scenario-info h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
}

.scenario-actions {
  display: flex;
  gap: 8px;
}

.scenario-details {
  padding: 10px 0;
}

.parameter-panel,
.comparison-results {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80%;
  max-width: 800px;
  max-height: 80vh;
  z-index: 1000;
  background: white;
  border-radius: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  margin: 20px 0;
}

.text-success {
  color: #67C23A;
  font-weight: bold;
}

.text-danger {
  color: #F56C6C;
  font-weight: bold;
}

.text-info {
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .analysis-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .analysis-content {
    padding: 15px;
  }
  
  .scenario-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .scenario-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .parameter-panel,
  .comparison-results {
    width: 95%;
    max-width: none;
  }
}

@media (max-width: 480px) {
  .analysis-header {
    padding: 10px 15px;
  }
  
  .analysis-content {
    padding: 10px;
  }
  
  .scenario-actions {
    flex-wrap: wrap;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>