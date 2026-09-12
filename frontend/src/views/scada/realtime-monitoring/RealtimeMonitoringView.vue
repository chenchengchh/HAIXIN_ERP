<template>
  <div class="realtime-monitoring-view">
    <div class="page-header">
      <h2>实时监控</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/scada">SCADA系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/scada/realtime-monitoring">实时监控</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/scada/realtime-monitoring#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs v-model="activeTab" class="function-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="工艺流程图" name="flowchart" />
      <el-tab-pane label="设备状态监控" name="device-status" />
      <el-tab-pane label="趋势图分析" name="trend-analysis" />
      <el-tab-pane label="实时报警显示" name="real-alarm" />
    </el-tabs>
    
    <!-- 工艺流程图 -->
    <div v-if="activeTab === 'flowchart'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>工艺流程图</span>
            <el-select v-model="selectedFlowchart" placeholder="选择流程图">
              <el-option label="生产流水线1" value="1" />
              <el-option label="生产流水线2" value="2" />
            </el-select>
          </div>
        </template>
        <div class="flowchart-container">
          <div class="simulated-flowchart">
            <h3>生产流水线1 - 工艺流程图</h3>
            <!-- 模拟流程图节点 -->
            <div class="flowchart-nodes">
              <!-- 节点1 -->
              <div class="flowchart-node active">
                <div class="node-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">原材料投入</div>
                  <div class="node-status">运行中</div>
                </div>
              </div>
              
              <!-- 节点连接线 -->
              <div class="node-connector"><el-icon><ArrowRight /></el-icon></div>
              
              <!-- 节点2 -->
              <div class="flowchart-node active">
                <div class="node-icon">
                  <el-icon><Setting /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">加工工序1</div>
                  <div class="node-status">运行中</div>
                </div>
              </div>
              
              <!-- 节点连接线 -->
              <div class="node-connector"><el-icon><ArrowRight /></el-icon></div>
              
              <!-- 节点3 -->
              <div class="flowchart-node active">
                <div class="node-icon">
                  <el-icon><Setting /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">加工工序2</div>
                  <div class="node-status">运行中</div>
                </div>
              </div>
              
              <!-- 节点连接线 -->
              <div class="node-connector"><el-icon><ArrowRight /></el-icon></div>
              
              <!-- 节点4 -->
              <div class="flowchart-node active">
                <div class="node-icon">
                  <el-icon><Bell /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">质检</div>
                  <div class="node-status">运行中</div>
                </div>
              </div>
              
              <!-- 节点连接线 -->
              <div class="node-connector"><el-icon><ArrowRight /></el-icon></div>
              
              <!-- 节点5 -->
              <div class="flowchart-node active">
                <div class="node-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="node-info">
                  <div class="node-name">包装</div>
                  <div class="node-status">运行中</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 设备状态监控 -->
    <div v-if="activeTab === 'device-status'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>设备状态监控</span>
          </div>
        </template>
        <div class="device-status-content">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="device-status">
                <div class="device-item">
                  <el-statistic :value="onlineDevices" title="在线设备">
                    <template #prefix>
                      <el-icon color="#67C23A"><CircleCheck /></el-icon>
                    </template>
                  </el-statistic>
                </div>
                <div class="device-item">
                  <el-statistic :value="offlineDevices" title="离线设备">
                    <template #prefix>
                      <el-icon color="#F56C6C"><CircleClose /></el-icon>
                    </template>
                  </el-statistic>
                </div>
                <div class="device-item">
                  <el-statistic :value="alarmDevices" title="报警设备">
                    <template #prefix>
                      <el-icon color="#E6A23C"><Warning /></el-icon>
                    </template>
                  </el-statistic>
                </div>
              </div>
            </el-col>
            
            <!-- 核心运行参数 -->
            <el-col :span="16">
              <div class="core-params">
                <el-row :gutter="20">
                  <el-col :span="6">
                    <div class="param-item">
                      <span class="param-label">液位</span>
                      <el-progress type="dashboard" :percentage="currentLevel" :format="levelFormat" :color="getProgressColor(currentLevel, 0, 100)"></el-progress>
                      <span class="param-value">{{ currentLevel }} %</span>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="param-item">
                      <span class="param-label">压力</span>
                      <el-progress type="dashboard" :percentage="currentPressure" :format="pressureFormat" :color="getProgressColor(currentPressure, 0, 200)"></el-progress>
                      <span class="param-value">{{ currentPressure }} bar</span>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="param-item">
                      <span class="param-label">温度</span>
                      <el-progress type="dashboard" :percentage="currentTemperature" :format="temperatureFormat" :color="getProgressColor(currentTemperature, 0, 150)"></el-progress>
                      <span class="param-value">{{ currentTemperature }} °C</span>
                    </div>
                  </el-col>
                  <el-col :span="6">
                    <div class="param-item">
                      <span class="param-label">流量</span>
                      <el-progress type="dashboard" :percentage="currentFlow" :format="flowFormat" :color="getProgressColor(currentFlow, 0, 500)"></el-progress>
                      <span class="param-value">{{ currentFlow }} m³/h</span>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>
    
    <!-- 趋势图分析 -->
    <div v-if="activeTab === 'trend-analysis'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>趋势图分析</span>
            <div class="header-actions">
              <el-select v-model="selectedTrendPoint" placeholder="选择点位" style="margin-right: 10px;">
                <el-option label="温度" value="temperature" />
                <el-option label="压力" value="pressure" />
                <el-option label="流量" value="flow" />
              </el-select>
              <el-button type="primary" size="small" @click="handleRefreshTrend">
                <el-icon><RefreshRight /></el-icon>
                刷新
              </el-button>
              <el-button type="success" size="small" @click="handleExportTrend">
                <el-icon><Download /></el-icon>
                导出
              </el-button>
            </div>
          </div>
        </template>
        <div class="trend-chart">
          <div class="simulated-trend-chart">
            <h3>{{ getTrendPointLabel(selectedTrendPoint) }} - 趋势图</h3>
            <div class="chart-container">
              <div class="chart-content" style="width: 100%; height: 300px; background-color: transparent; padding: 0; display: block; overflow: hidden;">
                <!-- 使用动态SVG与明确的样式 -->
                <svg 
                  width="100%" 
                  height="100%" 
                  style="
                    background-color: #ffffff;
                    border-radius: 4px;
                    display: block;
                    margin: 0;
                    padding: 0;
                  "
                >
                  <!-- 绘制坐标轴 -->
                  <line x1="60" y1="260" x2="820" y2="260" stroke="#606266" stroke-width="2" />
                  <line x1="60" y1="40" x2="60" y2="260" stroke="#606266" stroke-width="2" />
                  
                  <!-- 绘制动态趋势填充 -->
                  <polygon 
                    :points="trendData.map((value, index) => `${index * 63.33 + 60},${260 - value * 2.2}`).join(' ') + ' ' + `${(trendData.length - 1) * 63.33 + 60},260` + ' ' + '60,260'" 
                    fill="rgba(64, 158, 255, 0.3)" 
                    stroke="none"
                  />
                  
                  <!-- 绘制动态趋势线 -->
                  <polyline 
                    :points="trendData.map((value, index) => `${index * 63.33 + 60},${260 - value * 2.2}`).join(' ')" 
                    stroke="#409EFF" 
                    stroke-width="3" 
                    fill="none" 
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  
                  <!-- 绘制动态数据点 -->
                  <circle 
                    v-for="(point, index) in trendData" 
                    :key="index"
                    :cx="index * 63.33 + 60" 
                    :cy="260 - point * 2.2" 
                    r="4" 
                    fill="#409EFF" 
                    stroke="#ffffff" 
                    stroke-width="2"
                    style="cursor: pointer;"
                  />
                  
                  <!-- 绘制X轴刻度标签 -->
                  <text 
                    v-for="i in 12" 
                    :key="`x-label-${i}`" 
                    :x="(i-1) * 63.33 + 60" 
                    y="280" 
                    text-anchor="middle" 
                    font-size="12" 
                    fill="#606266"
                  >
                    {{ i }}h
                  </text>
                  
                  <!-- 绘制Y轴刻度标签 -->
                  <text 
                    v-for="i in 5" 
                    :key="`y-label-${i}`" 
                    x="40" 
                    :y="260 - i * 44" 
                    text-anchor="end" 
                    font-size="12" 
                    fill="#606266"
                  >
                    {{ i * 20 }}%
                  </text>
                </svg>
              </div>
              <div class="chart-info">
                <div class="info-item">
                  <span class="label">当前值:</span>
                  <span class="value">{{ currentTrendValue }} {{ getTrendUnit(selectedTrendPoint) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">最大值:</span>
                  <span class="value">{{ maxTrendValue }} {{ getTrendUnit(selectedTrendPoint) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">最小值:</span>
                  <span class="value">{{ minTrendValue }} {{ getTrendUnit(selectedTrendPoint) }}</span>
                </div>
                <div class="info-item">
                  <span class="label">平均值:</span>
                  <span class="value">{{ avgTrendValue }} {{ getTrendUnit(selectedTrendPoint) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 实时报警显示 -->
    <div v-if="activeTab === 'real-alarm'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>实时报警显示</span>
            <div class="header-actions">
              <el-button type="primary" size="small" @click="handleAcknowledgeAll">
                <el-icon><CircleCheck /></el-icon>
                全部确认
              </el-button>
              <el-button type="success" size="small" @click="handleClearAll">
                <el-icon><Delete /></el-icon>
                全部清除
              </el-button>
            </div>
          </div>
        </template>
        <div class="alarm-content">
          <div class="simulated-alarm-list">
            <el-table 
              :data="alarmList" 
              stripe 
              border 
              style="width: 100%" 
              size="small"
            >
              <el-table-column prop="id" label="报警ID" width="100" />
              <el-table-column prop="time" label="报警时间" width="180" />
              <el-table-column prop="device" label="设备名称" width="150" />
              <el-table-column prop="point" label="报警点位" width="120" />
              <el-table-column prop="value" label="当前值" width="100" />
              <el-table-column prop="threshold" label="阈值" width="100" />
              <el-table-column prop="level" label="报警级别" width="100">
                <template #default="scope">
                  <el-tag 
                    :type="getAlarmLevelType(scope.row.level)" 
                    effect="dark"
                  >
                    {{ scope.row.level }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag 
                    :type="scope.row.status === '未确认' ? 'warning' : 'success'" 
                  >
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="报警描述" />
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button 
                    type="primary" 
                    size="small" 
                    @click="handleAcknowledge(scope.row.id)"
                    :disabled="scope.row.status !== '未确认'"
                  >
                    <el-icon><CircleCheck /></el-icon>
                    确认
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="handleClear(scope.row.id)"
                    style="margin-left: 5px;"
                  >
                    <el-icon><Delete /></el-icon>
                    清除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  CircleCheck, CircleClose, Warning, RefreshRight, Download, Delete, 
  Setting, Bell, Document, ArrowRight 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { scadaAlarmApi, scadaRealtimeApi, scadaOverviewApi, scadaCollectPointApi } from '@/api/scada'
import { useWebSocket } from '@/utils/websocket'
import { buildWsUrl } from '@/utils/runtime-url'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('flowchart')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'flowchart': '工艺流程图',
  'device-status': '设备状态监控',
  'trend-analysis': '趋势图分析',
  'real-alarm': '实时报警显示'
}

// 实时监控模块
const selectedFlowchart = ref('1')
const onlineDevices = ref(65)
const offlineDevices = ref(5)
const alarmDevices = ref(3)

const currentLevel = ref(0)
const currentPressure = ref(0)
const currentTemperature = ref(0)
const currentFlow = ref(0)

const selectedTrendPoint = ref('temperature')

// 趋势图相关数据
const trendData = ref<number[]>([65, 72, 68, 80, 75, 82, 90, 85, 78, 88, 95, 92])
const currentTrendValue = ref<number>(92)
const maxTrendValue = ref<number>(95)
const minTrendValue = ref<number>(65)
const avgTrendValue = ref<number>(80)

// 报警列表数据
const alarmList = ref<any[]>([])

const wsClient = ref<ReturnType<typeof useWebSocket> | null>(null)

const initWebSocket = () => {
  const wsUrl = buildWsUrl('/scada/realtime')
  wsClient.value = useWebSocket(wsUrl, {
    onMessage: (message) => {
      try {
        const data = JSON.parse(message.data)
        if (data?.type === 'trend' && Array.isArray(data.values)) {
          const values = data.values.map((x: any) => Number(x)).filter((x: any) => Number.isFinite(x))
          if (values.length) {
            trendData.value = values
            currentTrendValue.value = values[values.length - 1] || 0
            maxTrendValue.value = Math.max(...values)
            minTrendValue.value = Math.min(...values)
            avgTrendValue.value = Math.round(values.reduce((a: number, b: number) => a + b, 0) / values.length)
          }
          return
        }

        if (data?.type === 'alarms' && Array.isArray(data.list)) {
          alarmList.value = data.list.map((item: any) => {
            return {
              id: String(item.id),
              time: item.time || item.triggerTime || '',
              device: item.device || item.deviceName || '',
              point: item.point || item.tagCode || '',
              value: item.value ?? item.currentValue,
              threshold: item.threshold ?? '',
              level: item.level || item.severity || '一般',
              status: item.status || '未确认',
              description: item.description || item.alarmType || item.alarmName || ''
            }
          })
        }
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }
  })
}

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'flowchart': 'flowchart',
    'device-status': 'device-status',
    'trend-analysis': 'trend-analysis',
    'real-alarm': 'real-alarm'
  }
  const tabName = route.params.tab || 'flowchart'
  return tabMap[tabName as string] || 'flowchart'
}

// 组件挂载时，从路由获取标签页状态
onMounted(() => {
  activeTab.value = getActiveTabFromRoute()
  Promise.allSettled([loadTrend(), loadAlarms(), loadDeviceStats(), loadDeviceParams()]).catch(() => undefined)
  initWebSocket()
})

onUnmounted(() => {
  wsClient.value?.close()
  wsClient.value = null
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/scada/realtime-monitoring/${tabName}`
  })
}

// 计算属性
const getProgressColor = (value: number, min: number, max: number) => {
  const percentage = ((value - min) / (max - min)) * 100
  if (percentage > 80) return '#F56C6C'
  if (percentage > 50) return '#E6A23C'
  return '#67C23A'
}

const levelFormat = (percentage: number) => {
  return `${percentage} %`
}

const pressureFormat = (percentage: number) => {
  return `${percentage} bar`
}

const temperatureFormat = (percentage: number) => {
  return `${percentage} °C`
}

const flowFormat = (percentage: number) => {
  return `${percentage} m³/h`
}

// 趋势图相关方法
const getTrendPointLabel = (point: string) => {
  const labelMap: Record<string, string> = {
    temperature: '温度',
    pressure: '压力',
    flow: '流量'
  }
  return labelMap[point] || '温度'
}

const getTrendUnit = (point: string) => {
  const unitMap: Record<string, string> = {
    temperature: '°C',
    pressure: 'bar',
    flow: 'm³/h'
  }
  return unitMap[point] || '°C'
}

// 生成趋势图数据点 - 适配新的SVG尺寸
const trendDataPoints = computed(() => {
  return trendData.value.map((value, index) => {
    return `${index * 64.17 + 50},${200 - value * 1.8}`
  }).join(' ')
})

// 生成趋势图面积数据点 - 适配新的SVG尺寸
const trendAreaPoints = computed(() => {
  const points = trendData.value.map((value, index) => {
    return `${index * 64.17 + 50},${200 - value * 1.8}`
  })
  return points.concat([`${(trendData.value.length - 1) * 64.17 + 50},200`, '50,200'])
})

// 调试用：输出趋势图数据
const debugTrendData = () => {
  console.log('趋势数据:', trendData.value)
  console.log('趋势线数据点:', trendDataPoints.value)
  console.log('趋势面积数据点:', trendAreaPoints.value)
}

// 刷新趋势图
const handleRefreshTrend = async () => {
  try {
    await loadTrend()
    
    ElMessage({
      message: '趋势图数据刷新成功',
      type: 'success'
    })
  } catch (error) {
    ElMessage({
      message: '趋势图数据刷新失败',
      type: 'error'
    })
  }
}

// 导出趋势图
const handleExportTrend = async () => {
  try {
    const response = await scadaRealtimeApi.exportTrend({ point: selectedTrendPoint.value })
    const blob = new Blob([response.data], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `SCADA_趋势_${selectedTrendPoint.value}_${new Date().toISOString().split('T')[0]}.csv`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage({
      message: '趋势图数据导出成功',
      type: 'success'
    })
  } catch (error) {
    ElMessage({
      message: '趋势图数据导出失败',
      type: 'error'
    })
  }
}

// 报警相关方法
const getAlarmLevelType = (level: string) => {
  const typeMap: Record<string, string> = {
    '紧急': 'danger',
    '警告': 'warning',
    '一般': 'info'
  }
  return typeMap[level] || 'info'
}

// 确认报警
const handleAcknowledge = async (alarmId: string) => {
  try {
    await scadaAlarmApi.acknowledge(alarmId)
    await loadAlarms()
    ElMessage({ message: '报警确认成功', type: 'success' })
  } catch (error) {
    ElMessage({
      message: '报警确认失败',
      type: 'error'
    })
  }
}

// 清除报警
const handleClear = async (alarmId: string) => {
  try {
    await scadaAlarmApi.clear(alarmId)
    await loadAlarms()
    ElMessage({
      message: '报警清除成功',
      type: 'success'
    })
  } catch (error) {
    ElMessage({
      message: '报警清除失败',
      type: 'error'
    })
  }
}

// 全部确认报警
const handleAcknowledgeAll = async () => {
  try {
    await scadaAlarmApi.acknowledgeAll()
    await loadAlarms()
    ElMessage({
      message: '全部报警确认成功',
      type: 'success'
    })
  } catch (error) {
    ElMessage({
      message: '全部报警确认失败',
      type: 'error'
    })
  }
}

// 全部清除报警
const handleClearAll = async () => {
  try {
    await scadaAlarmApi.clearAll()
    await loadAlarms()
    ElMessage({
      message: '全部报警清除成功',
      type: 'success'
    })
  } catch (error) {
    ElMessage({
      message: '全部报警清除失败',
      type: 'error'
    })
  }
}

const extractListData = (response: any) => {
  return unwrapListResponse<any>(response)
}

const loadTrend = async () => {
  const response = await scadaRealtimeApi.getTrend({ point: selectedTrendPoint.value, size: 12 })
  const data = unwrapResponseData<any>(response)
  const values: number[] = Array.isArray(data)
    ? data.map((x: any) => (typeof x === 'number' ? x : Number(x?.value))).filter((x: any) => Number.isFinite(x))
    : (Array.isArray(data?.values) ? data.values : [])

  if (values.length) {
    trendData.value = values
    currentTrendValue.value = values[values.length - 1] || 0
    maxTrendValue.value = Math.max(...values)
    minTrendValue.value = Math.min(...values)
    avgTrendValue.value = Math.round(values.reduce((a, b) => a + b, 0) / values.length)
  }
}

// 加载设备状态统计（在线/离线/报警设备数）
const loadDeviceStats = async () => {
  try {
    const response = await scadaOverviewApi.stats()
    const data = unwrapResponseData<any>(response) || {}
    const total = Number(data.totalDevices) || 0
    const online = Number(data.onlineDevices) || 0
    onlineDevices.value = online
    offlineDevices.value = Math.max(total - online, 0)
    alarmDevices.value = Number(data.activeAlarms) || 0
  } catch (error) {
    console.error('加载设备状态统计失败:', error)
  }
}

// 加载核心运行参数（采集点最新值：液位/压力/温度/流量）
const loadDeviceParams = async () => {
  try {
    const response = await scadaCollectPointApi.list()
    const list = unwrapListResponse<any>(response)
    const valueMap = new Map<string, number>()
    list.forEach((item: any) => {
      const tagCode = item.tagCode || item.tag_code
      const latest = Number(item.value ?? item.latestValue ?? item.latest_value)
      if (tagCode && Number.isFinite(latest)) {
        valueMap.set(tagCode, latest)
      }
    })
    currentLevel.value = Math.round((valueMap.get('LEVEL_001') ?? 0) * 10) / 10
    currentPressure.value = Math.round((valueMap.get('PRESS_001') ?? 0) * 10) / 10
    currentTemperature.value = Math.round((valueMap.get('TEMP_001') ?? 0) * 10) / 10
    currentFlow.value = Math.round((valueMap.get('FLOW_001') ?? 0) * 10) / 10
  } catch (error) {
    console.error('加载核心运行参数失败:', error)
  }
}

const loadAlarms = async () => {
  const response = await scadaAlarmApi.listActive()
  const list = extractListData(response)
  alarmList.value = list.map((item: any) => {
    return {
      id: String(item.id),
      time: item.time || item.triggerTime || '',
      device: item.device || item.deviceName || '',
      point: item.point || item.tagCode || '',
      value: item.value ?? item.currentValue,
      threshold: item.threshold ?? '',
      level: item.level || item.severity || '一般',
      status: item.status || '未确认',
      description: item.description || item.alarmType || item.alarmName || ''
    }
  })
}

</script>

<style scoped>
.realtime-monitoring-view {
  padding: 20px;
  height: 100%;
  overflow: auto;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color-light);
}

.page-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.8rem;
}

/* 覆盖默认的h2样式，确保只影响页面标题 */
h2 {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.sub-card {
  margin-bottom: 20px;
}

.sub-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1rem;
}

.sub-card-content {
  padding: 10px 0;
}

.flowchart-container {
  height: 300px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 模拟流程图样式 */
.simulated-flowchart {
  width: 100%;
  height: 100%;
  padding: 20px;
  text-align: center;
}

.flowchart-nodes {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  flex-wrap: wrap;
}

.flowchart-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background-color: #fff;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  width: 120px;
  transition: all 0.3s ease;
}

.flowchart-node.active {
  border-color: #409EFF;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
}

.node-icon {
  font-size: 30px;
  color: #409EFF;
  margin-bottom: 10px;
}

.node-name {
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 5px;
}

.node-status {
  font-size: 12px;
  color: #67C23A;
}

.node-connector {
  font-size: 20px;
  color: #909399;
  margin: 0 10px;
}

.device-status {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.device-status-content {
  padding: 10px 0;
}

.device-item {
  text-align: center;
}

.core-params {
  padding: 10px 0;
}

.param-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.param-label {
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.param-value {
  margin-top: 10px;
  font-size: 16px;
  font-weight: bold;
  color: #409EFF;
}

.trend-chart {
  height: 400px;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
}

/* 模拟趋势图样式 */
.simulated-trend-chart {
  width: 100%;
  height: 100%;
}

.simulated-trend-chart h3 {
  text-align: center;
  margin-bottom: 20px;
  color: #303133;
}

.chart-container {
  height: calc(100% - 40px);
  display: flex;
  flex-direction: column;
}

.chart-axis {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.x-axis {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-left: 50px;
}

.y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 300px;
  margin-right: 10px;
  text-align: right;
}

.axis-label {
  font-size: 12px;
  color: #909399;
  width: 60px;
}

.chart-content {
  flex: 1;
  position: relative;
  height: 300px;
  overflow: hidden;
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.chart-info {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #e0e0e0;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.info-item .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.info-item .value {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.alarm-content {
  height: 400px;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  overflow: auto;
}

/* 模拟报警列表样式 */
.simulated-alarm-list {
  width: 100%;
  height: 100%;
}

.header-actions {
  display: flex;
  align-items: center;
}

.tab-content {
  padding: 10px 0;
}
</style>
