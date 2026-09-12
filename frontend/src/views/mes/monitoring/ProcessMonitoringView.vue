<template>
  <div class="mes-monitoring-container">
    <!-- 面包屑导航 -->
    <div class="page-header">
      <div class="header-top">
        <h2>过程监控管理</h2>
        <div class="status-bar">
          <el-tooltip :content="store.wsConnected ? '实时连接已建立' : '实时连接未建立'" placement="top">
            <el-tag 
              :type="store.wsConnected ? 'success' : 'danger'" 
              size="small" 
              style="margin-right: 10px;">
              {{ store.wsConnected ? '已连接' : '未连接' }}
            </el-tag>
          </el-tooltip>
          <el-button 
            type="primary" 
            size="small" 
            @click="updateData()" 
            :loading="Object.values(loading).some(load => load)">
            刷新数据
          </el-button>
        </div>
      </div>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/mes">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes">MES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes/monitoring">过程监控</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/mes/monitoring#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" class="function-tabs">
        <el-tab-pane label="生产进度实时跟踪" name="production-progress">
          <el-card shadow="hover" class="content-card">
            <div class="card-content">
              <el-table v-loading="loading.productionProgress" :data="productionProgress" style="width: 100%">
                <el-table-column prop="workOrderNo" label="工单号" min-width="120" />
                <el-table-column prop="materialName" label="产品名称" min-width="150" />
                <el-table-column prop="totalQty" label="总数量" width="80" />
                <el-table-column prop="completedQty" label="已完成数量" width="100" />
                <el-table-column prop="progress" label="进度" width="150">
                  <template #default="scope">
                    <el-progress :percentage="scope.row.progress" :color="scope.row.progress === 100 ? '#67c23a' : '#409eff'" />
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'in_progress' ? 'warning' : 'success'">
                      {{ statusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="updateTime" label="更新时间" width="150" />
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="设备状态监控" name="equipment-status">
          <el-card shadow="hover" class="content-card">
            <div class="card-content">
              <div class="equipment-grid">
                <el-card 
                  v-for="equipment in equipmentStatus" 
                  :key="equipment.id" 
                  class="equipment-card"
                  :class="`status-${equipment.status}`">
                  <div class="equipment-header">
                    <h3>{{ equipment.equipmentName }}</h3>
                    <el-tag
                      :type="equipment.status === 'running' ? 'success' : 
                             equipment.status === 'idle' ? 'info' : 
                             equipment.status === 'down' ? 'danger' : 'warning'">
                      {{ equipmentStatusMap[equipment.status] }}
                    </el-tag>
                  </div>
                  <div class="equipment-info">
                    <div class="info-item">
                      <span class="label">设备类型:</span>
                      <span class="value">{{ equipment.equipmentType }}</span>
                    </div>
                    <div class="info-item">
                      <span class="label">运行时间:</span>
                      <span class="value">{{ equipment.uptime }}h</span>
                    </div>
                    <div class="info-item">
                      <span class="label">停机时间:</span>
                      <span class="value">{{ equipment.downtime }}h</span>
                    </div>
                    <div class="info-item">
                      <span class="label">设备效率:</span>
                      <span class="value">{{ equipment.efficiency }}%</span>
                    </div>
                    <div class="info-item" v-if="equipment.temperature">
                      <span class="label">温度:</span>
                      <span class="value">{{ equipment.temperature }}°C</span>
                    </div>
                    <div class="info-item" v-if="equipment.pressure">
                      <span class="label">压力:</span>
                      <span class="value">{{ equipment.pressure }}MPa</span>
                    </div>
                    <div class="info-item" v-if="equipment.speed">
                      <span class="label">速度:</span>
                      <span class="value">{{ equipment.speed }}rpm</span>
                    </div>
                  </div>
                  <div class="equipment-footer">
                    <el-button type="info" size="small" @click="viewEquipmentDetail(equipment.equipmentId)">
                      查看详情
                    </el-button>
                  </div>
                </el-card>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="工艺参数监控" name="process-params">
          <el-card shadow="hover" class="content-card">
            <div class="card-content">
              <el-tabs v-model="innerActiveTab" type="card">
                <el-tab-pane label="所有参数" name="all">
                  <el-table v-loading="loading.processParams" :data="processParams" style="width: 100%">
                    <el-table-column prop="workstationName" label="工站名称" min-width="120" />
                    <el-table-column prop="parameterName" label="参数名称" min-width="120" />
                    <el-table-column prop="parameterValue" label="当前值" width="100" />
                    <el-table-column prop="unit" label="单位" width="80" />
                    <el-table-column prop="upperLimit" label="上限" width="80" />
                    <el-table-column prop="lowerLimit" label="下限" width="80" />
                    <el-table-column prop="status" label="状态" width="100">
                      <template #default="scope">
                        <el-tag
                          :type="scope.row.status === 'normal' ? 'success' : 
                                 scope.row.status === 'warning' ? 'warning' : 'danger'">
                          {{ paramStatusMap[scope.row.status] }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="timestamp" label="采集时间" width="150" />
                    <el-table-column label="操作" width="150" fixed="right">
                      <template #default="scope">
                        <el-button type="info" size="small" @click="quickViewTrend(scope.row)">
                          查看趋势
                        </el-button>
                        <el-button v-if="scope.row.status !== 'normal'" type="warning" size="small" @click="viewParamAdvice(scope.row)">
                          处理建议
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                <el-tab-pane label="异常参数" name="abnormal">
                  <el-table v-loading="loading.processParams" :data="abnormalProcessParams" style="width: 100%">
                    <el-table-column prop="workstationName" label="工站名称" min-width="120" />
                    <el-table-column prop="parameterName" label="参数名称" min-width="120" />
                    <el-table-column prop="parameterValue" label="当前值" width="100" />
                    <el-table-column prop="unit" label="单位" width="80" />
                    <el-table-column prop="upperLimit" label="上限" width="80" />
                    <el-table-column prop="lowerLimit" label="下限" width="80" />
                    <el-table-column prop="status" label="状态" width="100">
                      <template #default="scope">
                        <el-tag
                          :type="scope.row.status === 'warning' ? 'warning' : 'danger'">
                          {{ paramStatusMap[scope.row.status] }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="timestamp" label="采集时间" width="150" />
                    <el-table-column label="操作" width="150" fixed="right">
                      <template #default="scope">
                        <el-button type="info" size="small" @click="quickViewTrend(scope.row)">
                          查看趋势
                        </el-button>
                        <el-button type="warning" size="small" @click="viewParamAdvice(scope.row)">
                          处理建议
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="工艺参数趋势图" name="parameter-trend">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <el-select v-model="selectedParameter" placeholder="选择参数" style="width: 200px; margin-right: 10px;">
                <el-option
                  v-for="param in parameterOptions"
                  :key="param.value"
                  :label="param.label"
                  :value="param.value"
                />
              </el-select>
              <el-select v-model="selectedWorkstation" placeholder="选择工站" style="width: 200px; margin-right: 10px;">
                <el-option
                  v-for="workstation in workstationOptions"
                  :key="workstation.value"
                  :label="workstation.label"
                  :value="workstation.value"
                />
              </el-select>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 300px; margin-right: 10px;"
                @change="updateChart"
              />
              <el-select v-model="timeGranularity" placeholder="时间粒度" style="width: 120px; margin-right: 10px;" @change="updateChart">
                <el-option label="小时" value="hour" />
                <el-option label="天" value="day" />
                <el-option label="周" value="week" />
              </el-select>
              <el-button type="primary" size="small" @click="updateChart">
                查询
              </el-button>
            </div>
            <div class="card-content">
              <div class="chart-container">
                <div id="parameterTrendChart" style="height: 400px; width: 100%;"></div>
              </div>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
    <!-- 设备详情对话框 -->
    <el-dialog
      v-model="showEquipmentDetailDialog"
      title="设备详情"
      width="800px"
      destroy-on-close>
      <div v-if="selectedEquipment" class="equipment-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备名称" :span="1">{{ selectedEquipment.equipmentName }}</el-descriptions-item>
          <el-descriptions-item label="设备ID" :span="1">{{ selectedEquipment.equipmentId }}</el-descriptions-item>
          <el-descriptions-item label="设备类型" :span="1">{{ selectedEquipment.equipmentType }}</el-descriptions-item>
          <el-descriptions-item label="状态" :span="1">
            <el-tag
              :type="selectedEquipment.status === 'running' ? 'success' : 
                     selectedEquipment.status === 'idle' ? 'info' : 
                     selectedEquipment.status === 'down' ? 'danger' : 'warning'">
              {{ equipmentStatusMap[selectedEquipment.status] }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="运行时间(小时)" :span="1">{{ selectedEquipment.uptime }}</el-descriptions-item>
          <el-descriptions-item label="停机时间(小时)" :span="1">{{ selectedEquipment.downtime }}</el-descriptions-item>
          <el-descriptions-item label="设备效率" :span="1">{{ selectedEquipment.efficiency }}%</el-descriptions-item>
          <el-descriptions-item label="温度(°C)" :span="1">{{ selectedEquipment.temperature || '-' }}</el-descriptions-item>
          <el-descriptions-item label="压力(MPa)" :span="1">{{ selectedEquipment.pressure || '-' }}</el-descriptions-item>
          <el-descriptions-item label="速度(rpm)" :span="1">{{ selectedEquipment.speed || '-' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间" :span="1">{{ selectedEquipment.updateTime }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 故障历史记录 -->
        <el-card shadow="hover" class="mt-20">
          <template #header>
            <div class="card-header-title">
              <span>故障历史记录</span>
            </div>
          </template>
          <el-table :data="equipmentFaultHistory" style="width: 100%">
            <el-table-column prop="faultId" label="故障ID" width="120" />
            <el-table-column prop="faultType" label="故障类型" width="120" />
            <el-table-column prop="faultDescription" label="故障描述" min-width="200" />
            <el-table-column prop="occurTime" label="发生时间" width="150" />
            <el-table-column prop="repairTime" label="修复时间" width="150" />
            <el-table-column prop="repairPerson" label="维修人员" width="120" />
          </el-table>
        </el-card>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEquipmentDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 快速查看趋势对话框 -->
    <el-dialog
      v-model="showQuickTrendDialog"
      :title="quickTrendParam ? `${quickTrendParam.workstationName} - ${quickTrendParam.parameterName}趋势图` : '趋势图'"
      width="800px"
      destroy-on-close>
      <div class="quick-trend-dialog">
        <div v-if="quickTrendParam" class="param-info">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="当前值">
              <span class="param-value">{{ quickTrendParam.parameterValue }}{{ quickTrendParam.unit }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="正常范围">
              <span>{{ quickTrendParam.lowerLimit }} - {{ quickTrendParam.upperLimit }}{{ quickTrendParam.unit }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="采集时间">
              <span>{{ quickTrendParam.timestamp }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="chart-container">
          <div id="quickTrendChart" style="height: 300px; width: 100%;"></div>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showQuickTrendDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="showAdviceDialog"
      :title="adviceParam ? `处理建议 - ${adviceParam.workstationName} / ${adviceParam.parameterName}` : '处理建议'"
      width="700px"
      destroy-on-close>
      <el-skeleton v-if="adviceLoading" :rows="6" animated />
      <div v-else class="advice-content">
        <el-descriptions v-if="adviceParam" :column="2" border>
          <el-descriptions-item label="工站">{{ adviceParam.workstationName }}</el-descriptions-item>
          <el-descriptions-item label="参数">{{ adviceParam.parameterName }}</el-descriptions-item>
          <el-descriptions-item label="当前值">{{ adviceParam.parameterValue }}{{ adviceParam.unit }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="adviceParam.status === 'alarm' ? 'danger' : adviceParam.status === 'warning' ? 'warning' : 'success'">
              {{ paramStatusMap[adviceParam.status] || adviceParam.status }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-alert
          v-if="adviceData?.advice"
          :title="adviceData.advice"
          type="warning"
          show-icon
          style="margin-top: 12px;" />

        <el-card v-if="Array.isArray(adviceData?.actions) && adviceData.actions.length" shadow="never" style="margin-top: 12px;">
          <template #header>
            <div>推荐处置步骤</div>
          </template>
          <el-steps direction="vertical" :active="0" finish-status="success">
            <el-step v-for="(a, idx) in adviceData.actions" :key="idx" :title="String(a)" />
          </el-steps>
        </el-card>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAdviceDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { useMesMonitoringStore } from '../../../stores/mes/monitoring'
import { storeToRefs } from 'pinia'
import type { EquipmentStatus, ProcessParam } from '../../../api/mes/monitoring'
import { getEquipmentFaultHistory, getProcessParamAdvice } from '../../../api/mes/monitoring'
import * as echarts from 'echarts'
import { ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 状态管理
// 使用storeToRefs保持state响应性
const store = useMesMonitoringStore()
const { 
  productionProgress, 
  equipmentStatus, 
  processParams,
  loading
} = storeToRefs(store)

// 本地计算属性：异常工艺参数
const abnormalProcessParams = computed(() => {
  return processParams.value.filter(param => param.status === 'warning' || param.status === 'alarm')
})

// 获取生产进度数据
const fetchProductionProgress = async () => {
  await store.fetchProductionProgress()
}

// 获取设备状态数据
const fetchEquipmentStatus = async () => {
  await store.fetchEquipmentStatus()
}

// 获取工艺参数数据
const fetchProcessParams = async () => {
  await store.fetchProcessParams()
}

// 状态映射
const statusMap: Record<string, string> = {
  in_progress: '处理中',
  completed: '已完成'
}

const equipmentStatusMap: Record<string, string> = {
  running: '运行中',
  idle: '待机',
  down: '故障',
  maintenance: '维护'
}

const paramStatusMap: Record<string, string> = {
  normal: '正常',
  warning: '警告',
  alarm: '报警'
}

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'production-progress': '生产进度实时跟踪',
  'equipment-status': '设备状态监控',
  'process-params': '工艺参数监控',
  'parameter-trend': '工艺参数趋势图'
}

// 外层标签页
const activeTab = ref('production-progress')
// 内层标签页
const innerActiveTab = ref('all')

// 参数选择
const selectedParameter = ref('temperature')
const selectedWorkstation = ref('all')
const dateRange = ref<[string, string]>([
  new Date(new Date().setDate(new Date().getDate() - 7)).toISOString().split('T')[0] || '',
  new Date().toISOString().split('T')[0] || ''
])
const timeGranularity = ref('hour')

// 图表引用
let parameterTrendChart: echarts.ECharts | null = null

// 设备详情对话框
const showEquipmentDetailDialog = ref(false)
const selectedEquipment = ref<EquipmentStatus | null>(null)

const equipmentFaultHistory = ref<any[]>([])

// 快速查看趋势对话框
const showQuickTrendDialog = ref(false)
const quickTrendParam = ref<ProcessParam | null>(null)

// 快速趋势图引用
let quickTrendChart: echarts.ECharts | null = null

// 模拟数据：参数选项
const parameterOptions = ref([
  { value: 'temperature', label: '温度' },
  { value: 'pressure', label: '压力' },
  { value: 'speed', label: '速度' },
  { value: 'vibration', label: '振动' },
  { value: 'current', label: '电流' }
])

// 模拟数据：工站选项
const workstationOptions = ref([
  { value: 'all', label: '所有工站' },
  { value: 'workstation1', label: '工站1' },
  { value: 'workstation2', label: '工站2' },
  { value: 'workstation3', label: '工站3' }
])

// 统一更新数据
const updateData = () => {
  fetchProductionProgress()
  fetchEquipmentStatus()
  fetchProcessParams()
  
  // 更新图表数据
  updateChart()
  
  // 检查异常设备和参数，触发报警
  checkForAlarms()
}

// 页面加载时获取数据
onMounted(() => {
  // 初始化WebSocket连接
  store.initWebSocket()
  
  // 初始化获取数据
  updateData()
  
  // 每10秒刷新一次数据
  updateInterval.value = setInterval(() => {
    updateData()
  }, 10000) as unknown as number
})

// 定时器引用
const updateInterval = ref<number>(0)

// 组件卸载时清理资源
onUnmounted(() => {
  // 清除定时器
  if (updateInterval.value) {
    clearInterval(updateInterval.value)
  }
  
  // 清除事件监听器
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('resize', handleQuickTrendResize)
  
  // 销毁图表实例
  disposeChart()
  disposeQuickTrendChart()
})

// 销毁图表实例
const disposeChart = () => {
  if (parameterTrendChart) {
    parameterTrendChart.dispose()
    parameterTrendChart = null
  }
}

// 窗口大小变化处理函数
const handleResize = () => {
  parameterTrendChart?.resize()
}

// 查看设备详情
const viewEquipmentDetail = async (equipmentId: string) => {
  try {
    // 获取设备详情
    await store.fetchEquipmentDetail(equipmentId)
    // 查找设备信息
    const equipment = equipmentStatus.value.find(item => item.equipmentId === equipmentId)
    if (equipment) {
      selectedEquipment.value = equipment
      const faults = await getEquipmentFaultHistory(equipmentId)
      equipmentFaultHistory.value = faults.data || []
      showEquipmentDetailDialog.value = true
    }
  } catch (error) {
    console.error('获取设备详情失败:', error)
    ElMessage.error('获取设备详情失败')
  }
}

// 报警列表
const alarms = ref<Array<{
  id: string;
  deviceId: string;
  deviceName: string;
  parameterName: string;
  parameterValue: number;
  unit: string;
  threshold: number;
  status: 'warning' | 'alarm';
  time: string;
  confirmed: boolean;
  confirmedBy?: string;
  confirmedTime?: string;
  handled: boolean;
  handledBy?: string;
  handledTime?: string;
  handlingNote?: string;
}>>([])

// 检查异常，触发报警
const checkForAlarms = () => {
  // 检查工艺参数异常
  const abnormalParams = processParams.value.filter(param => param.status === 'warning' || param.status === 'alarm')
  
  // 添加新的报警记录
  abnormalParams.forEach(param => {
    const existingAlarm = alarms.value.find(alarm => 
      alarm.deviceId === param.workstationId && 
      alarm.parameterName === param.parameterName &&
      !alarm.handled // 只检查未处理的报警
    )
    if (!existingAlarm) {
      // 创建新报警
      const newAlarm = {
        id: `${param.workstationId}-${param.parameterName}-${Date.now()}`,
        deviceId: param.workstationId,
        deviceName: param.workstationName,
        parameterName: param.parameterName,
        parameterValue: param.parameterValue,
        unit: param.unit,
        threshold: param.status === 'warning' ? param.upperLimit : param.lowerLimit,
        status: param.status as 'warning' | 'alarm',
        time: new Date().toLocaleString(),
        confirmed: false,
        handled: false
      }
      
      // 添加到报警列表
      alarms.value.push(newAlarm)
      
      // 显示报警消息
      if (param.status === 'alarm') {
        ElMessage.error({
          message: `【${param.workstationName}】${param.parameterName}异常报警: ${param.parameterValue}${param.unit}`,
          duration: 5000,
          showClose: true
        })
      } else {
        ElMessage.warning({
          message: `【${param.workstationName}】${param.parameterName}警告: ${param.parameterValue}${param.unit}`,
          duration: 3000,
          showClose: true
        })
      }
      
      // 限制报警记录数量
      if (alarms.value.length > 50) {
        alarms.value.shift()
      }
    }
  })
  
  // 清理已恢复正常的报警
  const recoveredAlarms: typeof alarms.value = []
  
  // 找出已恢复正常的报警
  alarms.value = alarms.value.filter(alarm => {
    const stillAbnormal = processParams.value.some(param => 
      param.workstationId === alarm.deviceId && 
      param.parameterName === alarm.parameterName && 
      (param.status === 'warning' || param.status === 'alarm')
    )
    
    if (!stillAbnormal && !alarm.handled) {
      recoveredAlarms.push(alarm)
      return false
    }
    return true
  })
  
  // 显示恢复正常的消息
  recoveredAlarms.forEach(alarm => {
    ElMessage.success({
      message: `【${alarm.deviceName}】${alarm.parameterName}已恢复正常`,
      duration: 3000
    })
  })
}

// 确认报警
const confirmAlarm = (alarmId: string) => {
  const alarm = alarms.value.find(a => a.id === alarmId)
  if (alarm) {
    alarm.confirmed = true
    alarm.confirmedBy = '当前用户' // 实际项目中应从登录信息获取
    alarm.confirmedTime = new Date().toLocaleString()
    ElMessage.success('报警已确认')
  }
}

// 处理报警
const handleAlarm = (alarmId: string, note?: string) => {
  const alarm = alarms.value.find(a => a.id === alarmId)
  if (alarm) {
    alarm.handled = true
    alarm.handledBy = '当前用户' // 实际项目中应从登录信息获取
    alarm.handledTime = new Date().toLocaleString()
    alarm.handlingNote = note || ''
    ElMessage.success('报警已处理')
  }
}

// 查看参数建议
const showAdviceDialog = ref(false)
const adviceLoading = ref(false)
const adviceParam = ref<ProcessParam | null>(null)
const adviceData = ref<any>(null)

const viewParamAdvice = async (param: ProcessParam) => {
  adviceParam.value = param
  adviceData.value = null
  showAdviceDialog.value = true
  adviceLoading.value = true
  try {
    const res = await getProcessParamAdvice(param.id)
    adviceData.value = res.data
  } catch (e) {
    ElMessage.error('获取处理建议失败，请重试')
  } finally {
    adviceLoading.value = false
  }
}

// 快速查看趋势
const quickViewTrend = (param: ProcessParam) => {
  quickTrendParam.value = param
  showQuickTrendDialog.value = true
  
  // 延迟初始化图表，确保DOM已渲染
  setTimeout(() => {
    initQuickTrendChart()
    updateQuickTrendChart()
  }, 100)
}

// 初始化快速趋势图
const initQuickTrendChart = () => {
  if (quickTrendChart) {
    return
  }
  
  const chartDom = document.getElementById('quickTrendChart')
  if (chartDom) {
    try {
      quickTrendChart = echarts.init(chartDom)
      
      const option = {
        title: {
          text: '参数趋势图',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: (params: any) => {
            let result = params[0].axisValue + '<br/>'
            params.forEach((param: any) => {
              result += `${param.seriesName}: ${param.value}${quickTrendParam.value?.unit || ''}<br/>`
            })
            return result
          }
        },
        legend: {
          data: ['当前值', '上限', '下限'],
          bottom: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: []
        },
        yAxis: {
          type: 'value',
          name: '',
          axisLabel: {
            formatter: '{value}'
          }
        },
        series: [
          {
            name: '当前值',
            type: 'line',
            data: [],
            smooth: true,
            itemStyle: {
              color: '#409EFF'
            },
            lineStyle: {
              width: 2
            }
          },
          {
            name: '上限',
            type: 'line',
            data: [],
            lineStyle: {
              type: 'dashed',
              color: '#F56C6C'
            },
            symbol: 'none'
          },
          {
            name: '下限',
            type: 'line',
            data: [],
            lineStyle: {
              type: 'dashed',
              color: '#E6A23C'
            },
            symbol: 'none'
          }
        ]
      }
      
      quickTrendChart.setOption(option)
      
      // 监听窗口大小变化
      window.addEventListener('resize', handleQuickTrendResize)
    } catch (error) {
      console.error('快速趋势图初始化失败:', error)
      quickTrendChart = null
    }
  }
}

// 更新快速趋势图
const updateQuickTrendChart = () => {
  if (!quickTrendChart || !quickTrendParam.value) {
    return
  }
  
  const param = quickTrendParam.value
  
  // 生成最近12小时的时间数据
  const hours = Array.from({ length: 12 }, (_, i) => {
    const date = new Date()
    date.setHours(date.getHours() - 11 + i)
    return `${date.getHours()}:00`
  })
  
  // 生成模拟的参数值
  const baseValue = param.parameterValue
  const currentValues = hours.map(() => {
    return parseFloat((baseValue + (Math.random() - 0.5) * 10).toFixed(2))
  })
  
  // 生成上下限数据
  const upperLimitValues = hours.map(() => param.upperLimit)
  const lowerLimitValues = hours.map(() => param.lowerLimit)
  
  quickTrendChart.setOption({
    xAxis: {
      data: hours
    },
    yAxis: {
      name: `${param.parameterName} (${param.unit})`
    },
    series: [
      {
        name: '当前值',
        data: currentValues
      },
      {
        name: '上限',
        data: upperLimitValues
      },
      {
        name: '下限',
        data: lowerLimitValues
      }
    ]
  })
}

// 窗口大小变化时调整快速趋势图
const handleQuickTrendResize = () => {
  quickTrendChart?.resize()
}

// 销毁快速趋势图
const disposeQuickTrendChart = () => {
  if (quickTrendChart) {
    quickTrendChart.dispose()
    quickTrendChart = null
  }
  window.removeEventListener('resize', handleQuickTrendResize)
}

// 清除报警
const clearAlarm = (alarmId: string) => {
  const index = alarms.value.findIndex(alarm => alarm.id === alarmId)
  if (index !== -1) {
    alarms.value.splice(index, 1)
  }
}

// 确认所有报警
const confirmAllAlarms = () => {
  alarms.value.forEach(alarm => {
    if (!alarm.confirmed) {
      alarm.confirmed = true
      alarm.confirmedBy = '当前用户'
      alarm.confirmedTime = new Date().toLocaleString()
    }
  })
  ElMessage.success('所有报警已确认')
}

// 处理所有报警
const handleAllAlarms = () => {
  alarms.value.forEach(alarm => {
    if (!alarm.handled) {
      alarm.handled = true
      alarm.handledBy = '当前用户'
      alarm.handledTime = new Date().toLocaleString()
      alarm.handlingNote = '批量处理'
    }
  })
  ElMessage.success('所有报警已处理')
}

// 初始化图表
const initChart = () => {
  // 只有在图表可见且未初始化时才初始化
  if (activeTab.value !== 'parameter-trend' || parameterTrendChart) {
    return
  }
  
  // 获取DOM元素
  const chartDom = document.getElementById('parameterTrendChart')
  if (chartDom) {
    // 确保图表容器样式正确
    chartDom.style.width = '100%'
    chartDom.style.height = '300px'
    chartDom.style.display = 'block'
    
    // 确保DOM元素有实际的宽高
    if (chartDom.clientWidth === 0 || chartDom.clientHeight === 0) {
      // 尝试强制重排
      chartDom.offsetHeight
      
      // 如果仍然没有宽高，使用默认值
      if (chartDom.clientWidth === 0) {
        chartDom.style.width = '800px'
      }
      if (chartDom.clientHeight === 0) {
        chartDom.style.height = '300px'
      }
    }
    
    try {
      // 初始化图表
      parameterTrendChart = echarts.init(chartDom)
      
      // 图表配置
      const option = {
        title: {
          text: '工艺参数趋势图',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: (params: any) => {
            let result = params[0].axisValue + '<br/>'
            params.forEach((param: any) => {
              const unit = getUnit(selectedParameter.value)
              result += `${param.seriesName}: ${param.value}${unit}<br/>`
            })
            return result
          }
        },
        legend: {
          data: ['当前值', '上限', '下限'],
          bottom: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: []
        },
        yAxis: {
          type: 'value',
          name: '',
          axisLabel: {
            formatter: '{value}'
          }
        },
        series: [
          {
            name: '当前值',
            type: 'line',
            data: [],
            smooth: true,
            itemStyle: {
              color: '#409EFF'
            },
            lineStyle: {
              width: 2
            }
          },
          {
            name: '上限',
            type: 'line',
            data: [],
            lineStyle: {
              type: 'dashed',
              color: '#F56C6C'
            },
            symbol: 'none'
          },
          {
            name: '下限',
            type: 'line',
            data: [],
            lineStyle: {
              type: 'dashed',
              color: '#E6A23C'
            },
            symbol: 'none'
          }
        ]
      }
      
      // 设置图表配置
      parameterTrendChart.setOption(option)
      
      // 监听窗口大小变化，调整图表大小
      window.addEventListener('resize', handleResize)
      
      // 初始化图表数据
      updateChart()
      
      // 强制触发一次调整大小，确保图表正确显示
      setTimeout(() => {
        parameterTrendChart?.resize()
      }, 100)
    } catch (error) {
      console.error('图表初始化失败:', error)
      parameterTrendChart = null
    }
  }
}

// 更新图表数据
const updateChart = () => {
  // 只有在图表初始化且可见时才更新数据
  if (!parameterTrendChart || activeTab.value !== 'parameter-trend') {
    return
  }
  
  // 获取实际工艺参数数据
  const parameter = selectedParameter.value
  const workstation = selectedWorkstation.value
  
  // 过滤数据：根据选择的工站和参数
  const filteredData = processParams.value.filter(param => {
    const matchesWorkstation = workstation === 'all' || param.workstationId === workstation
    const matchesParameter = param.parameterName.toLowerCase() === parameter.toLowerCase()
    return matchesWorkstation && matchesParameter
  })
  
  // 生成时间轴数据
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  
  // 生成当前值数据（如果没有实际数据则使用模拟数据）
  const currentValues = hours.map(() => {
    // 如果有实际数据，使用实际数据，否则使用模拟数据
    if (filteredData.length > 0) {
      // 简单模拟：使用随机实际数据
      const randomIndex = Math.floor(Math.random() * filteredData.length)
      const randomData = filteredData[randomIndex]
      return randomData?.parameterValue || 0
    } else {
      // 模拟数据
      switch (parameter) {
        case 'temperature':
          return Math.round(30 + Math.random() * 20)
        case 'pressure':
          return Math.round(10 + Math.random() * 10)
        case 'speed':
          return Math.round(1000 + Math.random() * 500)
        case 'vibration':
          return Math.round(0 + Math.random() * 5)
        case 'current':
          return Math.round(5 + Math.random() * 5)
        default:
          return 0
      }
    }
  })
  
  // 设置上下限（根据参数类型，优先使用实际数据中的上下限）
  let upperLimit = 0
  let lowerLimit = 0
  
  // 如果有实际数据，使用实际数据中的上下限
  if (filteredData.length > 0) {
    const firstData = filteredData[0]
    upperLimit = firstData?.upperLimit || getDefaultUpperLimit(parameter)
    lowerLimit = firstData?.lowerLimit || getDefaultLowerLimit(parameter)
  } else {
    // 否则使用默认值
    upperLimit = getDefaultUpperLimit(parameter)
    lowerLimit = getDefaultLowerLimit(parameter)
  }
  
  // 生成上下限数据
  const upperLimitValues = hours.map(() => upperLimit)
  const lowerLimitValues = hours.map(() => lowerLimit)
  
  // 获取参数标签和单位
  const paramLabel = parameterOptions.value.find(opt => opt.value === parameter)?.label || parameter
  const unit = getUnit(parameter)
  
  // 更新图表数据
  parameterTrendChart.setOption({
    xAxis: {
      data: hours
    },
    yAxis: {
      name: `${paramLabel} (${unit})`
    },
    series: [
      {
        name: '当前值',
        data: currentValues
      },
      {
        name: '上限',
        data: upperLimitValues
      },
      {
        name: '下限',
        data: lowerLimitValues
      }
    ]
  })
  
  // 更新后调整图表大小，确保显示正常
  setTimeout(() => {
    parameterTrendChart?.resize()
  }, 50)
}

// 修复图表容器样式
const fixChartContainerStyle = () => {
  const chartContainer = document.querySelector('.chart-container') as HTMLElement
  if (chartContainer) {
    chartContainer.style.width = '100%'
    chartContainer.style.height = '300px'
    chartContainer.style.display = 'block'
  }
  
  const cardContent = document.querySelector('.card-content') as HTMLElement
  if (cardContent) {
    cardContent.style.padding = '20px'
  }
}

// 监听标签页切换
watch(activeTab, (newTab, oldTab) => {
  if (newTab === 'parameter-trend') {
    // 切换到趋势图标签页，修复容器样式并初始化图表
    fixChartContainerStyle()
    // 延迟初始化，确保DOM样式已更新
    setTimeout(() => {
      initChart()
    }, 50)
  } else if (oldTab === 'parameter-trend') {
    // 从趋势图标签页切换走，销毁图表
    disposeChart()
  }
})

// 获取参数默认上限
const getDefaultUpperLimit = (parameter: string): number => {
  switch (parameter) {
    case 'temperature':
      return 45
    case 'pressure':
      return 18
    case 'speed':
      return 1400
    case 'vibration':
      return 4
    case 'current':
      return 9
    default:
      return 100
  }
}

// 获取参数默认下限
const getDefaultLowerLimit = (parameter: string): number => {
  switch (parameter) {
    case 'temperature':
      return 25
    case 'pressure':
      return 8
    case 'speed':
      return 800
    case 'vibration':
      return 0
    case 'current':
      return 4
    default:
      return 0
  }
}

// 获取参数单位
const getUnit = (parameter: string): string => {
  switch (parameter) {
    case 'temperature':
      return '°C'
    case 'pressure':
      return 'MPa'
    case 'speed':
      return 'rpm'
    case 'vibration':
      return 'mm/s'
    case 'current':
      return 'A'
    default:
      return ''
  }
}

// 监听参数和工站选择变化，更新图表
watch([selectedParameter, selectedWorkstation], () => {
  updateChart()
})

// 监听工艺参数数据变化，更新图表
watch(processParams, () => {
  if (activeTab.value === 'parameter-trend') {
    updateChart()
  }
}, { deep: true })
</script>

<style scoped>
.mes-monitoring-container {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: var(--bg-color-page);
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 16px 0;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--border-color-light);
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  margin-bottom: 8px;
}

.status-bar {
  display: flex;
  align-items: center;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin: 0;
}

/* 模块导航样式 */
.module-nav {
  background-color: var(--bg-color);
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  min-height: calc(100% - 120px);
}

/* 标签页样式 */
.function-tabs {
  padding: 16px;
}

/* 覆盖默认的卡片样式，使其更符合设计规范 */
.function-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.function-tabs :deep(.el-tabs__content) {
  padding: 16px 0 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-content {
  padding: 0;
}

/* 卡片样式 */
.content-card {
  margin: 20px;
}

/* 设备卡片网格 */
.equipment-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px;
}

.equipment-card {
  transition: all 0.3s ease;
}

.equipment-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.equipment-card.status-running {
  border-left: 4px solid #67c23a;
}

.equipment-card.status-idle {
  border-left: 4px solid #409eff;
}

.equipment-card.status-down {
  border-left: 4px solid #f56c6c;
}

.equipment-card.status-maintenance {
  border-left: 4px solid #e6a23c;
}

.equipment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.equipment-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
}

.equipment-info {
  margin-bottom: 15px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #606266;
}

.info-item .value {
  font-weight: 500;
  color: #303133;
}

.equipment-footer {
  display: flex;
  justify-content: flex-end;
}

.chart-container {
  height: 300px;
  width: 100%;
}

/* 快速趋势对话框样式 */
.quick-trend-dialog {
  padding: 10px 0;
}

.param-info {
  margin-bottom: 20px;
}

.param-value {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mes-monitoring-container {
    padding: 12px;
  }
  
  h1 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
  
  .content-card {
    margin: 10px;
  }
  
  .equipment-grid {
    grid-template-columns: 1fr;
    padding: 10px;
  }
  
  /* 调整对话框大小 */
  .quick-trend-dialog {
    padding: 5px 0;
  }
  
  .chart-container {
    height: 200px !important;
  }
}
</style>
