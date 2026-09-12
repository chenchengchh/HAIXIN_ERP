<template>
  <div class="dispatch-management-view">
    <!-- 页面头部和面包屑导航 -->
    <div class="page-header">
      <h2>调度管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/agv">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/agv">AGV系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/agv/dispatch-management">调度管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/agv/dispatch-management/${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="function-tabs">
      <!-- 多AGV协同 -->
      <el-tab-pane label="多AGV协同" name="collaboration">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>管理多台小车群集的自动化协同策略</span>
              </div>
            </template>
            <div class="collaboration-content">
              <!-- 协同策略配置 -->
              <div class="collaboration-strategy">
                <h3>协同策略配置</h3>
                <el-form :model="collaborationStrategy" label-width="120px">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="协同模式">
                        <el-select v-model="collaborationStrategy.mode" placeholder="选择协同模式">
                          <el-option label="集中式调度" value="centralized" />
                          <el-option label="分布式调度" value="distributed" />
                          <el-option label="混合式调度" value="hybrid" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="任务分配算法">
                        <el-select v-model="collaborationStrategy.assignmentAlgorithm" placeholder="选择分配算法">
                          <el-option label="最短距离优先" value="shortest-distance" />
                          <el-option label="最小负载优先" value="least-load" />
                          <el-option label="综合优化" value="comprehensive" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="最大同时作业数">
                        <el-input-number v-model="collaborationStrategy.maxConcurrentTasks" :min="1" :max="50" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="协同响应时间">
                        <el-input-number v-model="collaborationStrategy.responseTime" :min="100" :max="1000" :step="100" suffix="ms" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="24">
                      <el-form-item label="策略描述">
                        <el-input v-model="collaborationStrategy.description" type="textarea" :rows="2" placeholder="输入策略描述" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <!-- 操作按钮 -->
                  <div class="action-buttons">
                    <el-button type="primary" @click="saveStrategy">保存策略</el-button>
                    <el-button @click="resetStrategy">重置</el-button>
                    <el-button type="info" @click="loadDefaultStrategy">加载默认策略</el-button>
                  </div>
                </el-form>
              </div>
              
              <!-- 协同状态监控 -->
              <div class="collaboration-monitoring">
                <h3>协同状态监控</h3>
                <div class="collaboration-stats">
                  <el-card class="stat-card" shadow="hover">
                    <el-statistic :value="totalAgvCount" title="总AGV数量" />
                  </el-card>
                  <el-card class="stat-card" shadow="hover">
                    <el-statistic :value="activeAgvCount" title="活跃AGV数量" :value-style="{ color: '#409eff' }" />
                  </el-card>
                  <el-card class="stat-card" shadow="hover">
                    <el-statistic :value="runningTaskCount" title="运行中任务数" :value-style="{ color: '#67c23a' }" />
                  </el-card>
                  <el-card class="stat-card" shadow="hover">
                    <el-statistic :value="collisionAvoidanceCount" title="避免冲突次数" :value-style="{ color: '#e6a23c' }" />
                  </el-card>
                </div>
                
                <!-- 协同日志 -->
                <div class="collaboration-logs">
                  <h4>协同日志</h4>
                  <el-scrollbar height="200px">
                    <div v-for="log in collaborationLogs" :key="log.id" class="log-item">
                      <span class="log-time">{{ log.time }}</span>
                      <span class="log-content">{{ log.content }}</span>
                    </div>
                  </el-scrollbar>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 冲突避免 -->
      <el-tab-pane label="冲突避免" name="collision">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>配置距离门槛，防止AGV物理碰撞</span>
              </div>
            </template>
            <div class="collision-avoidance-content">
              <!-- 冲突避免配置 -->
              <div class="collision-avoidance-config">
                <h3>冲突避免配置</h3>
                <el-form :model="collisionAvoidanceConfig" label-width="120px">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="安全距离阈值">
                        <el-input-number v-model="collisionAvoidanceConfig.safetyDistance" :min="0.5" :max="5" :step="0.5" suffix="m" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="预警距离阈值">
                        <el-input-number v-model="collisionAvoidanceConfig.warningDistance" :min="1" :max="10" :step="0.5" suffix="m" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="紧急停止距离">
                        <el-input-number v-model="collisionAvoidanceConfig.emergencyStopDistance" :min="0.2" :max="2" :step="0.1" suffix="m" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="冲突处理策略">
                        <el-select v-model="collisionAvoidanceConfig.handlingStrategy" placeholder="选择处理策略">
                          <el-option label="等待优先" value="wait-first" />
                          <el-option label="避让优先" value="avoid-first" />
                          <el-option label="重新规划" value="replan" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="优先级规则">
                        <el-select v-model="collisionAvoidanceConfig.priorityRule" placeholder="选择优先级规则">
                          <el-option label="任务优先级" value="task-priority" />
                          <el-option label="AGV编号" value="agv-id" />
                          <el-option label="距离优先级" value="distance-priority" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="冲突检测频率">
                        <el-input-number v-model="collisionAvoidanceConfig.detectionFrequency" :min="10" :max="1000" :step="10" suffix="ms" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <!-- 操作按钮 -->
                  <div class="action-buttons">
                    <el-button type="primary" @click="saveCollisionConfig">保存配置</el-button>
                    <el-button @click="resetCollisionConfig">重置</el-button>
                  </div>
                </el-form>
              </div>
              
              <!-- 冲突避免效果 -->
              <div class="collision-avoidance-effect">
                <h3>冲突避免效果</h3>
                <div class="effect-chart">
                  <div class="chart-placeholder">
                    <el-icon class="chart-icon"><DataLine /></el-icon>
                    <p>冲突避免效果图表</p>
                    <p class="text-secondary">展示冲突避免策略的效果统计</p>
                  </div>
                </div>
                
                <!-- 最近冲突事件 -->
                <div class="recent-collisions">
                  <h4>最近冲突事件</h4>
                  <el-table :data="recentCollisions" style="width: 100%" height="200">
                    <el-table-column prop="id" label="事件ID" width="80" />
                    <el-table-column prop="agv1" label="AGV 1" width="100" />
                    <el-table-column prop="agv2" label="AGV 2" width="100" />
                    <el-table-column prop="distance" label="距离(m)" width="100" />
                    <el-table-column prop="time" label="时间" width="180" />
                    <el-table-column prop="status" label="状态" width="100">
                      <template #default="scope">
                        <el-tag :type="scope.row.status === 'avoided' ? 'success' : 'warning'">
                          {{ scope.row.status === 'avoided' ? '已避免' : '已处理' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 交通规则 -->
      <el-tab-pane label="交通规则" name="traffic-rules">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>设置各区域的行驶规则（单向/禁行/限速）</span>
              </div>
            </template>
            <div class="traffic-rules-content">
              <!-- 交通规则列表 -->
              <div class="traffic-rules-list">
                <h3>交通规则列表</h3>
                <el-table :data="trafficRules" style="width: 100%" height="400" @selection-change="handleTrafficRuleSelectionChange">
                  <el-table-column type="selection" width="55" />
                  <el-table-column prop="id" label="规则ID" width="80" />
                  <el-table-column prop="area" label="区域名称" />
                  <el-table-column prop="ruleType" label="规则类型" width="120">
                    <template #default="scope">
                      <el-tag :type="getRuleTypeTag(scope.row.ruleType)">
                        {{ getRuleTypeLabel(scope.row.ruleType) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="direction" label="行驶方向" width="100">
                    <template #default="scope">
                      <el-tag type="info">{{ scope.row.direction }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="speedLimit" label="限速(km/h)" width="120">
                    <template #default="scope">
                      {{ scope.row.speedLimit }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="规则描述" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-switch v-model="scope.row.status" @change="toggleRuleStatus(scope.row)" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="editRule(scope.row)">
                        编辑
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                
                <!-- 规则操作按钮 -->
                <div class="rule-action-buttons">
                  <el-button type="primary" @click="addRule">添加规则</el-button>
                  <el-button type="danger" @click="deleteSelectedRules">删除选中规则</el-button>
                  <el-button @click="exportRules">导出规则</el-button>
                </div>
              </div>
              
              <!-- 区域地图 -->
              <div class="area-map">
                <h3>区域地图</h3>
                <div class="map-placeholder">
                  <el-icon class="map-icon"><Grid /></el-icon>
                  <p>AGV行驶区域地图</p>
                  <p class="text-secondary">可视化展示各区域的交通规则</p>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 应急处理 -->
      <el-tab-pane label="应急处理" name="emergency">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>手动/自动触发故障小车隔离与后随小车重规划流程</span>
              </div>
            </template>
            <div class="emergency-content">
              <!-- 应急处理面板 -->
              <div class="emergency-panel">
                <h3>应急处理</h3>
                <el-card class="emergency-status-card" shadow="hover">
                  <div class="emergency-status">
                    <h4>系统应急状态</h4>
                    <el-tag type="success" size="large">正常</el-tag>
                  </div>
                </el-card>
                
                <!-- 应急操作按钮 -->
                <div class="emergency-actions">
                  <h4>应急操作</h4>
                  <el-button type="danger" size="large" @click="stopAllAgvs">
                    <el-icon><CloseBold /></el-icon> 紧急停止所有AGV
                  </el-button>
                  <el-button type="warning" size="large" @click="pauseAllAgvs">
                    <el-icon><VideoPause /></el-icon> 暂停所有AGV
                  </el-button>
                  <el-button type="success" size="large" @click="resumeAllAgvs">
                    <el-icon><VideoPlay /></el-icon> 恢复所有AGV
                  </el-button>
                </div>
                
                <!-- 故障AGV列表 -->
                <div class="fault-agv-list">
                  <h4>故障AGV列表</h4>
                  <el-table :data="faultAgvs" style="width: 100%" height="250">
                    <el-table-column prop="code" label="AGV编号" width="100" />
                    <el-table-column prop="faultType" label="故障类型" width="120">
                      <template #default="scope">
                        <el-tag type="danger">{{ scope.row.faultType }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="position" label="当前位置" />
                    <el-table-column prop="faultTime" label="故障时间" width="180" />
                    <el-table-column prop="status" label="处理状态" width="120">
                      <template #default="scope">
                        <el-tag :type="scope.row.status === 'pending' ? 'warning' : 'success'">
                          {{ scope.row.status === 'pending' ? '待处理' : '已处理' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="120">
                      <template #default="scope">
                        <el-button size="small" type="primary" @click="handleFaultAgv(scope.row)">
                          处理
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
              </div>
              
              <!-- 应急日志 -->
              <div class="emergency-logs">
                <h3>应急日志</h3>
                <el-scrollbar height="500px">
                  <el-timeline>
                    <el-timeline-item v-for="log in emergencyLogs" :key="log.id" :timestamp="log.time" placement="top">
                      <el-card :shadow="'hover'" class="log-card">
                        <div class="log-content">
                          <h4>{{ log.title }}</h4>
                          <p>{{ log.description }}</p>
                          <div class="log-meta">
                            <span class="log-operator">操作人: {{ log.operator }}</span>
                            <el-tag :type="log.level === '紧急' ? 'danger' : log.level === '警告' ? 'warning' : 'info'">
                              {{ log.level }}
                            </el-tag>
                          </div>
                        </div>
                      </el-card>
                    </el-timeline-item>
                  </el-timeline>
                </el-scrollbar>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 交通规则编辑/新增对话框 -->
    <el-dialog v-model="ruleDialogVisible" :title="isEditRule ? '编辑交通规则' : '添加交通规则'" width="600px">
      <el-form :model="ruleForm" label-width="100px">
        <el-form-item label="区域名称" required>
          <el-input v-model="ruleForm.area" placeholder="输入区域名称" />
        </el-form-item>
        <el-form-item label="规则类型" required>
          <el-select v-model="ruleForm.ruleType" placeholder="选择规则类型" style="width: 100%">
            <el-option label="单向行驶" value="directional" />
            <el-option label="限速" value="speed" />
            <el-option label="禁行区域" value="restricted" />
          </el-select>
        </el-form-item>
        <el-form-item label="行驶方向">
          <el-select v-model="ruleForm.direction" placeholder="选择行驶方向" style="width: 100%" clearable>
            <el-option label="东" value="东" />
            <el-option label="南" value="南" />
            <el-option label="西" value="西" />
            <el-option label="北" value="北" />
            <el-option label="双向" value="双向" />
          </el-select>
        </el-form-item>
        <el-form-item label="限速(km/h)">
          <el-input-number v-model="ruleForm.speedLimit" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="规则描述">
          <el-input v-model="ruleForm.description" type="textarea" :rows="2" placeholder="输入规则描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="ruleForm.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DataLine, Grid, CloseBold, VideoPause, VideoPlay,
  Check, EditPen, Delete, Plus
} from '@element-plus/icons-vue'
import { unwrapListResponse, unwrapResponseData } from '@/api'
import { useAGVStore } from '@/stores/agv'
import agvApi from '@/api/agv'

// 激活的标签页
const activeTab = ref('collaboration')

// 标签页名称映射，用于面包屑导航
const tabLabelMap: Record<string, string> = {
  'collaboration': '多AGV协同',
  'collision': '冲突避免',
  'traffic-rules': '交通规则',
  'emergency': '应急处理'
}

// 协同策略
const collaborationStrategy = ref({
  mode: 'centralized',
  assignmentAlgorithm: 'comprehensive',
  maxConcurrentTasks: 20,
  responseTime: 500,
  description: '集中式调度，综合优化任务分配，最大同时作业数20，响应时间500ms'
})

const agvStore = useAGVStore()
const selectedTrafficRules = ref<any[]>([])

// 交通规则对话框状态
const ruleDialogVisible = ref(false)
const isEditRule = ref(false)
const ruleForm = ref<any>({
  id: null,
  area: '',
  ruleType: 'speed',
  direction: '',
  speedLimit: 5,
  description: '',
  status: true
})

onMounted(async () => {
  await Promise.all([
    agvStore.loadOperationStats(),
    agvStore.loadCollaborationLogs(),
    agvStore.loadCollisionAvoidanceConfig(),
    agvStore.loadTrafficRules(),
    agvStore.loadFaultAlerts(),
    agvStore.loadCollaborationStrategies()
  ])

  const active = agvStore.collaborationStrategies.find(s => s.status) || agvStore.collaborationStrategies[0]
  if (active) {
    collaborationStrategy.value = {
      mode: active.mode,
      assignmentAlgorithm: active.assignmentAlgorithm,
      maxConcurrentTasks: active.maxConcurrentTasks,
      responseTime: active.responseTime,
      description: active.description
    }
  }
})

const totalAgvCount = computed(() => Number(agvStore.operationStats?.totalAgvCount ?? agvStore.operationStats?.totalAgvCount ?? 0))
const activeAgvCount = computed(() => Number(agvStore.operationStats?.activeAgvCount ?? 0))
const runningTaskCount = computed(() => Number(agvStore.operationStats?.runningTaskCount ?? 0))
const collisionAvoidanceCount = computed(() => Number(agvStore.operationStats?.collisionAvoidanceCount ?? 0))

const collaborationLogs = computed(() => {
  return (agvStore.collaborationLogs || []).map((l: any) => ({
    id: l.id,
    time: l.time,
    content: l.content
  }))
})

const collisionAvoidanceConfig = computed({
  get: () => agvStore.collisionAvoidanceConfig as any,
  set: (v: any) => { (agvStore.collisionAvoidanceConfig as any) = v }
})

const recentCollisions = computed(() => [])
const trafficRules = computed(() => agvStore.trafficRules as any)
const faultAgvs = computed(() => {
  return (agvStore.faultAlerts || []).map((a: any) => ({
    id: a.id,
    code: a.agvCode,
    agvCode: a.agvCode,
    faultType: a.faultType,
    position: a.position,
    faultTime: a.faultTime,
    status: a.status
  }))
})
const emergencyLogs = computed(() => collaborationLogs.value.map((l: any) => ({
  id: l.id,
  title: '系统事件',
  description: l.content,
  time: l.time,
  operator: 'system',
  level: '信息'
})))

const handleTrafficRuleSelectionChange = (rows: any[]) => {
  selectedTrafficRules.value = rows || []
}

// 保存协同策略：优先更新当前激活策略，无激活策略时才新建
const saveStrategy = async () => {
  try {
    ElMessage({ message: '正在保存协同策略...', type: 'info' })
    const configPayload = {
      mode: collaborationStrategy.value.mode,
      assignmentAlgorithm: collaborationStrategy.value.assignmentAlgorithm,
      maxConcurrentTasks: collaborationStrategy.value.maxConcurrentTasks,
      responseTime: collaborationStrategy.value.responseTime
    }
    const active = agvStore.collaborationStrategies.find((s: any) => s.status)
    if (active?.id) {
      // 更新现有激活策略
      await agvApi.updateCollaborationStrategy(active.id, {
        description: collaborationStrategy.value.description,
        config: JSON.stringify(configPayload)
      } as any)
    } else {
      // 无激活策略时新建并激活
      const createdRes: any = await agvApi.createCollaborationStrategy({
        name: `策略-${Date.now()}`,
        description: collaborationStrategy.value.description,
        config: JSON.stringify(configPayload)
      } as any)
      const created = unwrapResponseData<any>(createdRes)
      if (created?.id) {
        await agvApi.activateCollaborationStrategy(created.id)
      }
    }
    await agvStore.loadCollaborationStrategies()
    await agvStore.loadCollaborationLogs()
    ElMessage.success('协同策略保存成功')
  } catch (error) {
    ElMessage.error('保存协同策略失败')
    console.error('Failed to save strategy:', error)
  }
}

// 重置协同策略
const resetStrategy = () => {
  try {
    collaborationStrategy.value = {
      mode: 'centralized',
      assignmentAlgorithm: 'comprehensive',
      maxConcurrentTasks: 20,
      responseTime: 500,
      description: '集中式调度，综合优化任务分配，最大同时作业数20，响应时间500ms'
    }
    ElMessage.info('协同策略已重置')
  } catch (error) {
    ElMessage.error('重置协同策略失败')
    console.error('Failed to reset strategy:', error)
  }
}

// 加载默认协同策略
const loadDefaultStrategy = () => {
  try {
    collaborationStrategy.value = {
      mode: 'centralized',
      assignmentAlgorithm: 'comprehensive',
      maxConcurrentTasks: 20,
      responseTime: 500,
      description: '默认协同策略'
    }
    ElMessage.success('已加载默认协同策略')
  } catch (error) {
    ElMessage.error('加载默认协同策略失败')
    console.error('Failed to load default strategy:', error)
  }
}

// 保存冲突避免配置
const saveCollisionConfig = async () => {
  try {
    ElMessage({ message: '正在保存冲突避免配置...', type: 'info' })
    await agvStore.updateCollisionAvoidanceConfig({
      safetyDistance: collisionAvoidanceConfig.value.safetyDistance,
      warningDistance: collisionAvoidanceConfig.value.warningDistance,
      emergencyStopDistance: collisionAvoidanceConfig.value.emergencyStopDistance,
      handlingStrategy: collisionAvoidanceConfig.value.handlingStrategy,
      priorityRule: collisionAvoidanceConfig.value.priorityRule,
      detectionFrequency: collisionAvoidanceConfig.value.detectionFrequency
    } as any)
    ElMessage.success('冲突避免配置保存成功')
  } catch (error) {
    ElMessage.error('保存冲突避免配置失败')
    console.error('Failed to save collision config:', error)
  }
}

// 重置冲突避免配置
const resetCollisionConfig = () => {
  try {
    collisionAvoidanceConfig.value = {
      safetyDistance: 1.5,
      warningDistance: 3.0,
      emergencyStopDistance: 0.5,
      handlingStrategy: 'wait-first',
      priorityRule: 'task-priority',
      detectionFrequency: 100
    }
    ElMessage.info('冲突避免配置已重置')
  } catch (error) {
    ElMessage.error('重置冲突避免配置失败')
    console.error('Failed to reset collision config:', error)
  }
}

// 获取规则类型标签
const getRuleTypeTag = (ruleType: string) => {
  switch (ruleType) {
    case 'directional': return 'primary'
    case 'speed': return 'warning'
    case 'restricted': return 'danger'
    default: return 'info'
  }
}

// 获取规则类型标签
const getRuleTypeLabel = (ruleType: string) => {
  switch (ruleType) {
    case 'directional': return '方向规则'
    case 'speed': return '速度规则'
    case 'restricted': return '限制规则'
    default: return ruleType
  }
}

// 切换规则状态（v-model已更新为新值：true=启用）
const toggleRuleStatus = async (rule: any) => {
  try {
    ElMessage({ message: `正在${rule.status ? '启用' : '禁用'}规则...`, type: 'info' })
    await agvApi.updateTrafficRule(rule.id, { status: rule.status } as any)
    await agvStore.loadTrafficRules()
    ElMessage.success(`规则已${rule.status ? '启用' : '禁用'}`)
  } catch (error) {
    ElMessage.error('切换规则状态失败')
    console.error('Failed to toggle rule status:', error)
  }
}

// 编辑规则：打开对话框并填充当前规则数据
const editRule = (rule: any) => {
  isEditRule.value = true
  ruleForm.value = {
    id: rule.id,
    area: rule.area || '',
    ruleType: rule.ruleType || 'speed',
    direction: rule.direction || '',
    speedLimit: rule.speedLimit ?? 5,
    description: rule.description || '',
    status: !!rule.status
  }
  ruleDialogVisible.value = true
}

// 添加规则：打开对话框并填充默认值
const addRule = () => {
  isEditRule.value = false
  ruleForm.value = {
    id: null,
    area: '',
    ruleType: 'speed',
    direction: '',
    speedLimit: 5,
    description: '',
    status: true
  }
  ruleDialogVisible.value = true
}

// 保存规则（新增或更新）
const saveRule = async () => {
  if (!ruleForm.value.area || !ruleForm.value.area.trim()) {
    ElMessage.warning('请填写区域名称')
    return
  }
  try {
    if (isEditRule.value && ruleForm.value.id != null) {
      await agvApi.updateTrafficRule(ruleForm.value.id, {
        area: ruleForm.value.area,
        ruleType: ruleForm.value.ruleType,
        direction: ruleForm.value.direction,
        speedLimit: ruleForm.value.speedLimit,
        description: ruleForm.value.description,
        status: ruleForm.value.status
      } as any)
      ElMessage.success('规则更新成功')
    } else {
      await agvApi.createTrafficRule({
        area: ruleForm.value.area,
        ruleType: ruleForm.value.ruleType,
        direction: ruleForm.value.direction,
        speedLimit: ruleForm.value.speedLimit,
        description: ruleForm.value.description,
        status: ruleForm.value.status
      } as any)
      ElMessage.success('新规则添加成功')
    }
    ruleDialogVisible.value = false
    await agvStore.loadTrafficRules()
  } catch (error) {
    ElMessage.error('保存规则失败')
    console.error('Failed to save rule:', error)
  }
}

// 删除选中规则
const deleteSelectedRules = async () => {
  try {
    ElMessage({ message: '正在删除选中规则...', type: 'info' })
    if (!selectedTrafficRules.value.length) {
      ElMessage.warning('请先选择规则')
      return
    }
    for (const r of selectedTrafficRules.value) {
      await agvApi.deleteTrafficRule(r.id)
    }
    await agvStore.loadTrafficRules()
    ElMessage.success('选中规则已删除')
  } catch (error) {
    ElMessage.error('删除规则失败')
    console.error('Failed to delete rules:', error)
  }
}

// 导出规则
const exportRules = async () => {
  try {
    ElMessage({ message: '正在导出规则...', type: 'info' })
    const res: any = await agvApi.getTrafficRules()
    const data = unwrapListResponse<any>(res)
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `agv-traffic-rules-${Date.now()}.json`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('规则导出成功')
  } catch (error) {
    ElMessage.error('导出规则失败')
    console.error('Failed to export rules:', error)
  }
}

// 紧急停止所有AGV
const stopAllAgvs = async () => {
  try {
    ElMessage({ message: '正在紧急停止所有AGV...', type: 'warning' })
    await agvStore.emergencyStopAllAGVs()
    await agvStore.loadOperationStats()
    ElMessage.success('所有AGV已紧急停止')
  } catch (error) {
    ElMessage.error('紧急停止AGV失败')
    console.error('Failed to stop all AGVs:', error)
  }
}

// 暂停所有AGV
const pauseAllAgvs = async () => {
  try {
    ElMessage({ message: '正在暂停所有AGV...', type: 'info' })
    await agvStore.pauseAllAGVs()
    await agvStore.loadOperationStats()
    ElMessage.success('所有AGV已暂停')
  } catch (error) {
    ElMessage.error('暂停AGV失败')
    console.error('Failed to pause all AGVs:', error)
  }
}

// 恢复所有AGV
const resumeAllAgvs = async () => {
  try {
    ElMessage({ message: '正在恢复所有AGV...', type: 'info' })
    await agvStore.resumeAllAGVs()
    await agvStore.loadOperationStats()
    ElMessage.success('所有AGV已恢复运行')
  } catch (error) {
    ElMessage.error('恢复AGV失败')
    console.error('Failed to resume all AGVs:', error)
  }
}

// 处理故障AGV
const handleFaultAgv = async (agv: any) => {
  try {
    ElMessage({ message: `正在处理故障AGV ${agv.agvCode || agv.code}...`, type: 'info' })
    await agvStore.handleFaultAlert(agv.id, '已处理', '系统')
    await agvStore.loadFaultAlerts()
    ElMessage.success(`故障AGV ${agv.agvCode || agv.code} 已处理`)
  } catch (error) {
    ElMessage.error(`处理故障AGV ${agv.agvCode || agv.code} 失败`)
    console.error('Failed to handle fault AGV:', error)
  }
}
</script>

<style scoped>
.dispatch-management-view {
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
  margin-bottom: var(--spacing-large);
}

.tab-content {
  padding: var(--spacing-small) 0;
}

/* 卡片样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

/* 协同内容布局 */
.collaboration-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-large);
}

@media (max-width: 1200px) {
  .collaboration-content {
    grid-template-columns: 1fr;
  }
}

/* 协同策略配置 */
.collaboration-strategy h3,
.collaboration-monitoring h3,
.collision-avoidance-config h3,
.collision-avoidance-effect h3,
.traffic-rules-list h3,
.area-map h3,
.emergency-panel h3 {
  margin-bottom: var(--spacing-medium);
  font-size: var(--font-size-h3);
}

/* 操作按钮组 */
.action-buttons {
  margin-top: var(--spacing-large);
  display: flex;
  gap: var(--spacing-small);
}

/* 协同监控统计 */
.collaboration-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: var(--spacing-medium);
  margin-bottom: var(--spacing-large);
}

.stat-card {
  text-align: center;
  padding: var(--spacing-large);
}

/* 协同日志 */
.collaboration-logs h4,
.recent-collisions h4,
.emergency-actions h4,
.fault-agv-list h4 {
  margin-bottom: 12px;
  font-size: 1rem;
}

.log-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.log-time {
  color: #909399;
  font-size: 14px;
  width: 180px;
}

.log-content {
  flex: 1;
  font-size: 14px;
}

/* 冲突避免内容布局 */
.collision-avoidance-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .collision-avoidance-content {
    grid-template-columns: 1fr;
  }
}

/* 效果图表 */
.effect-chart {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.chart-placeholder {
  text-align: center;
  color: #606266;
}

.chart-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

/* 交通规则内容布局 */
.traffic-rules-content {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .traffic-rules-content {
    grid-template-columns: 1fr;
  }
}

/* 规则操作按钮 */
.rule-action-buttons {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}

/* 区域地图 */
.area-map {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  height: 500px;
  display: flex;
  flex-direction: column;
}

.map-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #606266;
}

.map-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

/* 应急内容布局 */
.emergency-content {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .emergency-content {
    grid-template-columns: 1fr;
  }
}

/* 应急状态卡片 */
.emergency-status-card {
  margin-bottom: 20px;
}

.emergency-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.emergency-status h4 {
  margin: 0;
  font-size: 16px;
}

/* 应急操作按钮 */
.emergency-actions {
  margin-bottom: 20px;
}

.emergency-actions h4 {
  margin-bottom: 16px;
}

.emergency-actions .el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}

/* 日志卡片 */
.log-card {
  margin-bottom: 16px;
}

.log-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.log-content p {
  margin: 0 0 12px 0;
  color: #606266;
}

.log-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .dispatch-management-view {
    padding: 16px;
  }
  
  h2 {
    font-size: 1.5rem;
  }
  
  .collaboration-stats {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }
}

@media (max-width: 768px) {
  .dispatch-management-view {
    padding: 12px;
  }
  
  h2 {
    font-size: 1.3rem;
    margin-bottom: 16px;
  }
  
  .action-buttons,
  .rule-action-buttons {
    flex-direction: column;
  }
  
  .effect-chart {
    height: 200px;
  }
}
</style>
