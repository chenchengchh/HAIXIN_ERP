<template>
  <div class="status-monitoring-view">
    <!-- 页面头部和面包屑导航 -->
    <div class="page-header">
      <h2>状态监控</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/agv">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/agv">AGV系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/agv/status-monitoring">状态监控</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/agv/status-monitoring/${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="function-tabs">
      <!-- AGV实时位置 -->
      <el-tab-pane label="AGV实时位置" name="position">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>数字孪生地图展示所有小车精确定位</span>
              </div>
            </template>
            <div class="agv-position-content">
              <!-- 3D数字孪生地图 -->
              <div class="digital-twin-map">
                <AgvDigitalTwin3D :agvs="agvStore.agvs" :tasks="agvStore.tasks" />

                <!-- 地图控制按钮（真实刷新） -->
                <div class="map-controls">
                  <el-button size="small" type="primary" @click="refreshMapData">
                    <el-icon><RefreshRight /></el-icon> 刷新
                  </el-button>
                </div>
              </div>
              
              <!-- AGV列表 -->
              <div class="agv-position-list">
                <h3>AGV位置列表</h3>
                <el-table :data="agvPositionList" style="width: 100%" height="350">
                  <el-table-column prop="code" label="AGV编号" width="100" />
                  <el-table-column prop="position" label="当前位置" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getAgvStatusType(scope.row.status)">
                        {{ scope.row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="speed" label="速度" width="80">
                    <template #default="scope">
                      {{ scope.row.speed }} m/s
                    </template>
                  </el-table-column>
                  <el-table-column prop="direction" label="方向" width="80">
                    <template #default="scope">
                      <el-tag type="info">{{ scope.row.direction }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="lastUpdate" label="最后更新" width="180" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="trackAGV(scope.row)">
                        追踪
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 电池状态监控 -->
      <el-tab-pane label="电池状态监控" name="battery">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>实时电压/电量监控，触发低电量自动充电指令</span>
              </div>
            </template>
            <div class="battery-monitoring-content">
              <!-- 电池状态概览 -->
              <div class="battery-overview">
                <h3>电池状态概览</h3>
                <div class="battery-status-cards">
                  <el-card class="status-card high-battery" shadow="hover">
                    <div class="status-card-content">
                      <el-icon class="status-icon"><CircleCheckFilled /></el-icon>
                      <div class="status-info">
                        <h4>高电量 (80%-100%)</h4>
                        <p class="count">{{ highBatteryCount }} 台</p>
                      </div>
                    </div>
                  </el-card>
                  
                  <el-card class="status-card medium-battery" shadow="hover">
                    <div class="status-card-content">
                      <el-icon class="status-icon"><Warning /></el-icon>
                      <div class="status-info">
                        <h4>中电量 (50%-79%)</h4>
                        <p class="count">{{ mediumBatteryCount }} 台</p>
                      </div>
                    </div>
                  </el-card>
                  
                  <el-card class="status-card low-battery" shadow="hover">
                    <div class="status-card-content">
                      <el-icon class="status-icon"><BatteryIcon /></el-icon>
                      <div class="status-info">
                        <h4>低电量 (30%-49%)</h4>
                        <p class="count">{{ lowBatteryCount }} 台</p>
                      </div>
                    </div>
                  </el-card>
                  
                  <el-card class="status-card critical-battery" shadow="hover">
                    <div class="status-card-content">
                      <el-icon class="status-icon"><CircleCloseFilled /></el-icon>
                      <div class="status-info">
                        <h4>临界电量 (<30%)</h4>
                        <p class="count">{{ criticalBatteryCount }} 台</p>
                      </div>
                    </div>
                  </el-card>
                  
                  <el-card class="status-card charging-battery" shadow="hover">
                    <div class="status-card-content">
                      <el-icon class="status-icon"><Lightning /></el-icon>
                      <div class="status-info">
                        <h4>充电中</h4>
                        <p class="count">{{ chargingBatteryCount }} 台</p>
                      </div>
                    </div>
                  </el-card>
                </div>
              </div>
              
              <!-- 电池详情列表 -->
              <div class="battery-details">
                <h3>电池详情列表</h3>
                <el-table :data="agvBatteryList" style="width: 100%" height="400">
                  <el-table-column prop="code" label="AGV编号" width="100" />
                  <el-table-column prop="batteryLevel" label="电量" width="120">
                    <template #default="scope">
                      <div class="battery-level-container">
                        <el-progress :percentage="scope.row.batteryLevel" :stroke-width="15" :color="getBatteryColor(scope.row.batteryLevel)" />
                        <span class="battery-value">{{ scope.row.batteryLevel }}%</span>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="voltage" label="电压" width="100">
                    <template #default="scope">
                      {{ scope.row.voltage }} V
                    </template>
                  </el-table-column>
                  <el-table-column prop="temperature" label="温度" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.temperature > 45 ? 'danger' : scope.row.temperature > 35 ? 'warning' : 'success'">
                        {{ scope.row.temperature }}°C
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="chargingStatus" label="充电状态" width="120">
                    <template #default="scope">
                      <el-tag :type="scope.row.chargingStatus === 'charging' ? 'success' : 'info'">
                        {{ scope.row.chargingStatus === 'charging' ? '充电中' : '未充电' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="estimatedRuntime" label="预计运行时间" width="120">
                    <template #default="scope">
                      {{ scope.row.estimatedRuntime }} 分钟
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="sendChargeCommand(scope.row)">
                        充电指令
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 运行状态监控 -->
      <el-tab-pane label="运行状态监控" name="operation">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>仪表盘实时展示速度、方向及各类故障告警</span>
              </div>
            </template>
            <div class="operation-monitoring-content">
              <!-- 运行状态仪表盘 -->
              <div class="operation-dashboard">
                <h3>运行状态仪表盘</h3>
                <div class="dashboard-stats">
                  <el-statistic class="stat-item" :value="totalAgvCount" title="总AGV数量" />
                  <el-statistic class="stat-item" :value="runningAgvCount" title="运行中" :value-style="{ color: '#409eff' }" />
                  <el-statistic class="stat-item" :value="idleAgvCount" title="空闲" :value-style="{ color: '#67c23a' }" />
                  <el-statistic class="stat-item" :value="chargingAgvCount" title="充电中" :value-style="{ color: '#e6a23c' }" />
                  <el-statistic class="stat-item" :value="faultAgvCount" title="故障" :value-style="{ color: '#f56c6c' }" />
                </div>
              </div>
              
              <!-- 运行状态列表 -->
              <div class="operation-status-list">
                <h3>运行状态详情</h3>
                <el-table :data="agvOperationList" style="width: 100%" height="400">
                  <el-table-column prop="code" label="AGV编号" width="100" />
                  <el-table-column prop="status" label="运行状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getAgvStatusType(scope.row.status)">
                        {{ scope.row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="speed" label="速度" width="100">
                    <template #default="scope">
                      {{ scope.row.speed }} m/s
                    </template>
                  </el-table-column>
                  <el-table-column prop="direction" label="方向" width="100">
                    <template #default="scope">
                      <el-tag type="info">{{ scope.row.direction }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="taskNo" label="当前任务" width="120" />
                  <el-table-column prop="temperature" label="设备温度" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.temperature > 50 ? 'danger' : scope.row.temperature > 40 ? 'warning' : 'success'">
                        {{ scope.row.temperature }}°C
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="faultInfo" label="故障信息" width="150">
                    <template #default="scope">
                      <el-tag v-if="scope.row.faultInfo" type="danger">
                        {{ scope.row.faultInfo }}
                      </el-tag>
                      <el-tag v-else type="success">
                        无故障
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="viewAgvDetails(scope.row)">
                        详情
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- 故障告警信息 -->
              <div class="fault-alerts">
                <h3>故障告警信息</h3>
                <el-timeline>
                  <el-timeline-item v-for="alert in faultAlerts" :key="alert.id" :timestamp="alert.time" placement="top">
                    <el-card :shadow="'hover'" class="alert-card">
                      <div class="alert-content">
                        <el-icon class="alert-icon"><WarningFilled /></el-icon>
                        <div class="alert-info">
                          <h4>{{ alert.agvCode }} - {{ alert.level }}告警</h4>
                          <p>{{ alert.message }}</p>
                        </div>
                        <el-button size="small" type="primary" @click="handleAlert(alert)">
                          处理
                        </el-button>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 交通管制 -->
      <el-tab-pane label="交通管制" name="traffic">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>监控交叉路口管理状态，显示排队与锁闭信息</span>
              </div>
            </template>
            <div class="traffic-control-content">
              <!-- 交通管制地图 -->
              <div class="traffic-control-map">
                <div class="map-placeholder">
                  <el-icon class="map-icon"><Grid /></el-icon>
                  <p>交通管制地图</p>
                  <p class="text-secondary">显示交叉路口管理状态</p>
                </div>
              </div>
              
              <!-- 交通管制列表 -->
              <div class="traffic-control-list">
                <h3>交通管制详情</h3>
                <el-table :data="trafficControlList" style="width: 100%" height="350">
                  <el-table-column prop="nodeId" label="节点ID" width="100" />
                  <el-table-column prop="nodeName" label="节点名称" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === 'locked' ? 'warning' : 'success'">
                        {{ scope.row.status === 'locked' ? '已锁闭' : '正常' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="lockAgv" label="占用AGV" width="120" />
                  <el-table-column prop="lockTime" label="锁闭时间" width="180" />
                  <el-table-column prop="queueLength" label="排队数量" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.queueLength > 0 ? 'warning' : 'success'">
                        {{ scope.row.queueLength }} 台
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="节点描述" />
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="releaseNode(scope.row)">
                        释放节点
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  RefreshRight,
  CircleCloseFilled, Warning, CircleCheckFilled, Lightning, WarningFilled, Grid, Warning as BatteryIcon
} from '@element-plus/icons-vue'
import { unwrapResponseData } from '@/api'
import { useAGVStore } from '@/stores/agv'
import agvApi from '@/api/agv'
import AgvDigitalTwin3D from '@/components/agv/AgvDigitalTwin3D.vue'

// 激活的标签页
const activeTab = ref('position')

// 标签页名称映射，用于面包屑导航
const tabLabelMap: Record<string, string> = {
  'position': 'AGV实时位置',
  'battery': '电池状态监控',
  'operation': '运行状态监控',
  'traffic': '交通管制'
}

const agvStore = useAGVStore()

onMounted(async () => {
  await Promise.all([
    agvStore.loadAGVs(),
    agvStore.loadTasks(),
    agvStore.loadBatteryStatuses(),
    agvStore.loadFaultAlerts(),
    agvStore.loadTrafficNodes(),
    agvStore.loadOperationStats()
  ])
})

const agvPositionList = computed(() => {
  return (agvStore.agvs || []).map(a => ({
    code: a.code,
    position: a.position,
    status: a.status,
    speed: a.speed,
    direction: a.direction || '',
    lastUpdate: a.lastUpdate || ''
  }))
})

const agvBatteryList = computed(() => {
  return (agvStore.batteryStatuses || []).map(b => ({
    code: b.agvCode,
    batteryLevel: b.batteryLevel,
    voltage: b.voltage,
    temperature: b.temperature,
    chargingStatus: b.chargingStatus,
    estimatedRuntime: b.estimatedRuntime
  }))
})

// 高电量AGV数量
const highBatteryCount = computed(() => {
  return agvBatteryList.value.filter(agv => agv.batteryLevel >= 80).length
})

// 中电量AGV数量
const mediumBatteryCount = computed(() => {
  return agvBatteryList.value.filter(agv => agv.batteryLevel >= 50 && agv.batteryLevel < 80).length
})

// 低电量AGV数量
const lowBatteryCount = computed(() => {
  return agvBatteryList.value.filter(agv => agv.batteryLevel >= 30 && agv.batteryLevel < 50).length
})

// 临界电量AGV数量
const criticalBatteryCount = computed(() => {
  return agvBatteryList.value.filter(agv => agv.batteryLevel < 30).length
})

// 充电中AGV数量
const chargingBatteryCount = computed(() => {
  return agvBatteryList.value.filter(agv => agv.chargingStatus === 'charging').length
})

const agvOperationList = computed(() => {
  const tasks = agvStore.tasks || []
  const alerts = agvStore.faultAlerts || []
  return (agvStore.agvs || []).map(a => {
    const task = tasks.find(t => t.agvCode === a.code && (t.status === 'running' || t.status === 'assigned' || t.status === 'paused'))
    const alert = alerts.find(al => al.agvCode === a.code && al.status === 'pending')
    return {
      code: a.code,
      status: a.status,
      speed: a.speed,
      direction: a.direction || '',
      taskNo: task?.taskNo || '',
      temperature: a.temperature || 0,
      faultInfo: alert?.message || ''
    }
  })
})

// 总AGV数量
const totalAgvCount = computed(() => {
  return agvOperationList.value.length
})

// 运行中AGV数量
const runningAgvCount = computed(() => {
  return agvOperationList.value.filter(agv => agv.status === 'running').length
})

// 空闲AGV数量
const idleAgvCount = computed(() => {
  return agvOperationList.value.filter(agv => agv.status === 'idle').length
})

// 充电中AGV数量
const chargingAgvCount = computed(() => {
  return agvOperationList.value.filter(agv => agv.status === 'charging').length
})

// 故障AGV数量
const faultAgvCount = computed(() => {
  return agvOperationList.value.filter(agv => agv.faultInfo).length
})

const faultAlerts = computed(() => {
  return (agvStore.faultAlerts || []).map(a => ({
    id: a.id,
    agvCode: a.agvCode,
    level: a.level,
    message: a.message,
    time: a.faultTime
  }))
})

const trafficControlList = computed(() => {
  return (agvStore.trafficNodes || []).map(n => ({
    nodeId: n.id,
    nodeName: n.nodeName,
    status: n.status,
    lockAgv: n.lockAgv,
    lockTime: n.lockTime,
    queueLength: n.queueLength,
    description: n.description
  }))
})

// 刷新3D数字孪生地图数据（重新拉取AGV与任务实时数据）
const refreshMapData = async () => {
  try {
    await Promise.all([agvStore.loadAGVs(), agvStore.loadTasks()])
    ElMessage.success('地图数据已刷新')
  } catch (error) {
    ElMessage.error('地图数据刷新失败')
    console.error('Failed to refresh map data:', error)
  }
}

// 追踪AGV
const trackAGV = async (agv: any) => {
  try {
    ElMessage({ message: `正在追踪AGV ${agv.code}...`, type: 'info' })
    const res: any = await agvApi.getAGVPosition(agv.code)
    const data = unwrapResponseData<any>(res) || {}
    ElMessage.success(`开始追踪AGV ${agv.code}，当前位置：${data.position || agv.position}`)
  } catch (error) {
    ElMessage.error(`追踪AGV ${agv.code} 失败`)
    console.error('Failed to track AGV:', error)
  }
}

// 获取AGV状态类型
const getAgvStatusType = (status: string) => {
  switch (status) {
    case 'running': return 'success'
    case 'idle': return 'info'
    case 'charging': return 'warning'
    case 'fault': return 'danger'
    default: return 'info'
  }
}

// 获取电池颜色
const getBatteryColor = (level: number) => {
  if (level >= 80) return '#67c23a'
  if (level >= 50) return '#e6a23c'
  if (level >= 30) return '#f56c6c'
  return '#909399'
}

// 发送充电指令
const sendChargeCommand = async (agv: any) => {
  try {
    ElMessage({ message: `正在发送充电指令给AGV ${agv.code}...`, type: 'info' })
    await agvStore.sendChargeCommand(agv.code)
    await agvStore.loadBatteryStatuses()
    await agvStore.loadAGVs()
    ElMessage.success(`已发送充电指令给AGV ${agv.code}`)
  } catch (error) {
    ElMessage.error(`发送充电指令给AGV ${agv.code} 失败`)
    console.error('Failed to send charge command:', error)
  }
}

// 查看AGV详情
const viewAgvDetails = async (agv: any) => {
  try {
    ElMessage({ message: `正在加载AGV ${agv.code} 详情...`, type: 'info' })
    await agvApi.getAGVDetail(agv.code)
    ElMessage.success(`AGV ${agv.code} 详情已加载`)
  } catch (error) {
    ElMessage.error(`加载AGV ${agv.code} 详情失败`)
    console.error('Failed to view AGV details:', error)
  }
}

// 处理告警
const handleAlert = async (alert: any) => {
  try {
    ElMessage({ message: '正在处理告警...', type: 'info' })
    await agvStore.handleFaultAlert(alert.id, '已处理', '系统')
    await agvStore.loadFaultAlerts()
    ElMessage.success('告警已处理')
  } catch (error) {
    ElMessage.error('处理告警失败')
    console.error('Failed to handle alert:', error)
  }
}

// 释放节点
const releaseNode = async (node: any) => {
  try {
    ElMessage({ message: `正在释放节点 ${node.nodeName}...`, type: 'info' })
    await agvStore.releaseTrafficNode(node.nodeId)
    await agvStore.loadTrafficNodes()
    ElMessage.success(`节点 ${node.nodeName} 已释放`)
  } catch (error) {
    ElMessage.error(`释放节点 ${node.nodeName} 失败`)
    console.error('Failed to release node:', error)
  }
}
</script>

<style scoped>
.status-monitoring-view {
  padding: var(--page-padding);
  height: 100%;
  overflow: auto;
  background-color: var(--bg-color-primary);
}

/* 页面头部样式 */
.page-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: var(--spacing-large);
}

.page-header h2 {
  margin: 0;
  color: var(--text-color-primary);
  font-size: var(--font-size-h2);
  font-weight: 600;
}

/* 功能标签页 */
.function-tabs {
  margin-bottom: 20px;
}

.tab-content {
  padding: 10px 0;
}

/* 卡片样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* AGV位置内容布局 */
.agv-position-content {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .agv-position-content {
    grid-template-columns: 1fr;
  }
}

/* 数字孪生地图 */
.digital-twin-map {
  position: relative;
  border-radius: 8px;
}

/* 地图控制按钮 */
.map-controls {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

/* 地图占位符 */
.map-placeholder {
  text-align: center;
  color: #606266;
}

.map-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

/* AGV位置列表 */
.agv-position-list h3,
.battery-overview h3,
.battery-details h3,
.operation-dashboard h3,
.operation-status-list h3,
.fault-alerts h3,
.traffic-control-list h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 电池监控内容布局 */
.battery-monitoring-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 电池状态概览 */
.battery-status-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

/* 状态卡片 */
.status-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.status-card:hover {
  transform: translateY(-2px);
}

.status-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon {
  font-size: 32px;
  color: #409eff;
}

.status-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: bold;
}

.status-info .count {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

/* 电池状态卡片颜色 */
.high-battery {
  border-left: 4px solid #67c23a;
}

.medium-battery {
  border-left: 4px solid #e6a23c;
}

.low-battery {
  border-left: 4px solid #f56c6c;
}

.critical-battery {
  border-left: 4px solid #909399;
}

.charging-battery {
  border-left: 4px solid #409eff;
}

/* 电池详情列表 */
.battery-level-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.battery-value {
  font-size: 14px;
  font-weight: bold;
}

/* 运行监控内容布局 */
.operation-monitoring-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 运行状态仪表盘 */
.operation-dashboard {
  margin-bottom: 20px;
}

/* 仪表盘统计 */
.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

/* 运行状态列表 */
.operation-status-list {
  margin-top: 20px;
}

/* 故障告警 */
.fault-alerts {
  margin-top: 20px;
}

/* 告警卡片 */
.alert-card {
  margin: 0;
}

.alert-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.alert-icon {
  color: #f56c6c;
  font-size: 20px;
  margin-top: 4px;
}

.alert-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: bold;
}

.alert-info p {
  margin: 0;
  color: #606266;
}

/* 交通管制内容布局 */
.traffic-control-content {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .traffic-control-content {
    grid-template-columns: 1fr;
  }
}

/* 交通管制地图 */
.traffic-control-map {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .status-monitoring-view {
    padding: 16px;
  }
  
  h2 {
    font-size: 1.5rem;
  }
  
  .battery-status-cards {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
  
  .dashboard-stats {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }
}

@media (max-width: 768px) {
  .status-monitoring-view {
    padding: 12px;
  }
  
  h2 {
    font-size: 1.3rem;
    margin-bottom: 16px;
  }
  
  .battery-status-cards {
    grid-template-columns: 1fr;
  }
  
  .dashboard-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
