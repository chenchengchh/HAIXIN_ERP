<template>
  <div class="path-planning-view">
    <!-- 页面头部和面包屑导航 -->
    <div class="page-header">
      <h2>路径规划</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/agv">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/agv">AGV系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/agv/path-planning">路径规划</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/agv/path-planning/${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="function-tabs">
      <!-- 动态路径规划 -->
      <el-tab-pane label="动态路径规划" name="dynamic">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>AGV实时避开障碍物</span>
              </div>
            </template>
            <div class="dynamic-path-content">
              <!-- 3D数字孪生地图（展示真实AGV位置/路径/障碍物） -->
              <AgvDigitalTwin3D :agvs="agvList" :tasks="tasks" />
              
              <!-- 路径信息面板 -->
              <div class="path-info-panel">
                <h3>路径详情</h3>
                <el-descriptions :column="1" border>
                  <el-descriptions-item label="当前AGV">{{ currentAgvPath.agvCode || '暂无运行中AGV' }}</el-descriptions-item>
                  <el-descriptions-item label="起点">{{ currentAgvPath.startPoint || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="终点">{{ currentAgvPath.endPoint || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="路径长度">{{ currentAgvPath.distance ? currentAgvPath.distance + 'm' : '-' }}</el-descriptions-item>
                  <el-descriptions-item label="预计时间">{{ currentAgvPath.estimatedTime ? currentAgvPath.estimatedTime + '分钟' : '-' }}</el-descriptions-item>
                  <el-descriptions-item label="当前位置">{{ currentAgvPath.position || '-' }}</el-descriptions-item>
                </el-descriptions>
                
                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <el-button type="primary" @click="replanPath">重新规划路径</el-button>
                  <el-button @click="pauseAGV">暂停AGV</el-button>
                  <el-button @click="viewHistoryPaths">查看历史路径</el-button>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 路径优化 -->
      <el-tab-pane label="路径优化" name="optimization">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>最短路径算法生成的推荐路径</span>
              </div>
            </template>
            <div class="optimization-content">
              <!-- 优化参数设置 -->
              <div class="optimization-params">
                <h3>优化参数</h3>
                <el-form :model="optimizationParams" label-width="120px">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="算法类型">
                        <el-select v-model="optimizationParams.algorithm" placeholder="选择算法">
                          <el-option label="Dijkstra" value="dijkstra" />
                          <el-option label="A*" value="a-star" />
                          <el-option label="遗传算法" value="genetic" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="优化目标">
                        <el-select v-model="optimizationParams.target" placeholder="选择优化目标">
                          <el-option label="最短距离" value="distance" />
                          <el-option label="最短时间" value="time" />
                          <el-option label="最低能耗" value="energy" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="起点">
                        <el-input v-model="optimizationParams.start" placeholder="输入起点" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="终点">
                        <el-input v-model="optimizationParams.end" placeholder="输入终点" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <!-- 操作按钮 -->
                  <div class="action-buttons">
                    <el-button type="primary" @click="calculateOptimizedPath">计算最优路径</el-button>
                    <el-button>保存参数</el-button>
                  </div>
                </el-form>
              </div>
              
              <!-- 优化结果展示 -->
              <div class="optimization-result">
                <h3>优化结果</h3>
                <el-table :data="optimizedPaths" style="width: 100%">
                  <el-table-column prop="id" label="路径ID" width="80" />
                  <el-table-column prop="algorithm" label="算法" width="120" />
                  <el-table-column prop="distance" label="距离(m)" width="100" />
                  <el-table-column prop="time" label="时间(min)" width="100" />
                  <el-table-column prop="energy" label="能耗" width="100" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="scope.row.status === 'optimal' ? 'success' : 'info'">
                        {{ scope.row.status === 'optimal' ? '最优' : '候选' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="scope">
                      <el-button size="small" type="primary" @click="applyPath(scope.row)">
                        应用
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 多AGV路径协调 -->
      <el-tab-pane label="多AGV路径协调" name="coordination">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>可视化展示各车路径，标记并避免路径冲突点</span>
              </div>
            </template>
            <div class="coordination-content">
              <!-- 多AGV路径3D数字孪生地图 -->
              <AgvDigitalTwin3D :agvs="agvList" :tasks="tasks" />
              
              <!-- AGV列表 -->
              <div class="agv-list-panel">
                <h3>AGV列表</h3>
                <el-table :data="agvList" style="width: 100%" height="300">
                  <el-table-column prop="code" label="AGV编号" width="100" />
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="scope">
                      <el-tag :type="getAgvStatusType(scope.row.status)">
                        {{ scope.row.status }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="path" label="路径" />
                  <el-table-column prop="conflicts" label="冲突点" width="120">
                    <template #default="scope">
                      <el-tag v-if="scope.row.conflicts > 0" type="danger">
                        {{ scope.row.conflicts }}个
                      </el-tag>
                      <el-tag v-else type="success">
                        无
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 路径模拟与验证 -->
      <el-tab-pane label="路径模拟与验证" name="simulation">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>沙盒模式，验证新路径的可行性</span>
              </div>
            </template>
            <div class="simulation-content">
              <!-- 模拟控制面板 -->
              <div class="simulation-panel">
                <h3>模拟控制</h3>
                <el-form :model="simulationParams" label-width="120px">
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="AGV数量">
                        <el-input-number v-model="simulationParams.agvCount" :min="1" :max="20" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="模拟速度">
                        <el-slider v-model="simulationParams.speed" :min="0.5" :max="3" :step="0.5" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="模拟时长">
                        <el-input v-model="simulationParams.duration" placeholder="分钟" suffix="分钟" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="障碍物密度">
                        <el-select v-model="simulationParams.obstacleDensity" placeholder="选择密度">
                          <el-option label="低密度" value="low" />
                          <el-option label="中密度" value="medium" />
                          <el-option label="高密度" value="high" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <!-- 模拟按钮组 -->
                  <div class="action-buttons">
                    <el-button type="primary" @click="startSimulation">开始模拟</el-button>
                    <el-button @click="pauseSimulation">暂停</el-button>
                    <el-button @click="stopSimulation">停止</el-button>
                    <el-button @click="resetSimulation">重置</el-button>
                  </div>
                </el-form>
                
                <!-- 模拟结果 -->
                <div class="simulation-results">
                  <h3>模拟结果</h3>
                  <el-descriptions :column="2" border>
                    <el-descriptions-item label="模拟时间">{{ simulationResults.simulationTime }}</el-descriptions-item>
                    <el-descriptions-item label="完成任务数">{{ simulationResults.completedTasks }}个</el-descriptions-item>
                    <el-descriptions-item label="冲突次数">{{ simulationResults.conflictCount }}次</el-descriptions-item>
                    <el-descriptions-item label="平均路径长度">{{ simulationResults.avgPathLength }}m</el-descriptions-item>
                    <el-descriptions-item label="平均完成时间">{{ simulationResults.avgCompletionTime }}分钟</el-descriptions-item>
                    <el-descriptions-item label="能量消耗">{{ simulationResults.energyConsumption }}Wh</el-descriptions-item>
                  </el-descriptions>
                </div>
              </div>
              
              <!-- 模拟可视化区域 -->
              <div class="simulation-visualization">
                <el-table :data="agvList" style="width: 100%" height="360">
                  <el-table-column prop="code" label="AGV编号" width="120" />
                  <el-table-column prop="status" label="状态" width="120">
                    <template #default="scope">
                      <el-tag :type="getAgvStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="path" label="当前位置/路径" min-width="220" />
                  <el-table-column prop="battery" label="电量(%)" width="120" />
                  <el-table-column prop="speed" label="速度" width="100" />
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
import { unwrapListResponse, unwrapResponseData } from '../../../api'
import { agvApi } from '../../../api/agv'
import { ElMessage } from 'element-plus'
import AgvDigitalTwin3D from '@/components/agv/AgvDigitalTwin3D.vue'

// 定义类型
interface OptimizedPath {
  id: number
  algorithm: string
  distance: number
  time: number
  energy: number
  status: 'optimal' | 'candidate'
}

interface AgvItem {
  code: string
  status: 'running' | 'idle' | 'charging'
  path: string
  conflicts: number
  battery: number
  speed: number
  position: string
  batteryLevel: number
  direction: string
}

// 激活的标签页
const activeTab = ref('dynamic')

// 标签页名称映射，用于面包屑导航
const tabLabelMap: Record<string, string> = {
  'dynamic': '动态路径规划',
  'optimization': '路径优化',
  'coordination': '多AGV路径协调',
  'simulation': '路径模拟与验证'
}

// 优化参数
const optimizationParams = ref({
  algorithm: 'a-star',
  target: 'distance',
  start: 'A区-01',
  end: 'B区-05'
})

// 优化路径列表
const optimizedPaths = ref<OptimizedPath[]>([])

// AGV列表
const agvList = ref<AgvItem[]>([])

// 任务列表（3D数字孪生场景路径线数据）
const tasks = ref<any[]>([])

// 模拟参数
const simulationParams = ref({
  agvCount: 5,
  speed: 1,
  duration: 10,
  obstacleDensity: 'medium'
})

// 模拟结果
const simulationResults = ref({
  simulationTime: '00:00:00',
  completedTasks: 0,
  conflictCount: 0,
  avgPathLength: 0,
  avgCompletionTime: 0,
  energyConsumption: 0
})

const unwrapApiData = (response: any) => unwrapResponseData<any>(response)

const unwrapApiList = (response: any) => unwrapListResponse<any>(response)

// 当前AGV路径详情（取第一个运行中的AGV及其任务；设备状态仅 running/idle/charging，assigned 属于任务状态）
const currentAgvPath = computed(() => {
  const runningAgv = agvList.value.find(a => a.status === 'running')
  if (!runningAgv) return { agvCode: '', startPoint: '', endPoint: '', distance: 0, estimatedTime: 0, position: '' }
  const activeTask = tasks.value.find((t: any) => t?.agvCode === runningAgv.code && (t?.status === 'running' || t?.status === 'assigned'))
  return {
    agvCode: runningAgv.code,
    startPoint: activeTask?.startPoint || '',
    endPoint: activeTask?.endPoint || '',
    distance: 0,
    estimatedTime: 0,
    position: runningAgv.position || ''
  }
})

const refreshAgvAndTasks = async () => {
  const agvRes = await agvApi.getAGVs()
  const taskRes = await agvApi.getTasks()
  const agvs: any[] = unwrapApiList(agvRes) || []
  const taskData: any[] = unwrapApiList(taskRes) || []
  tasks.value = taskData
  agvList.value = agvs.map((a: any) => {
    const activeTask = taskData.find((t: any) => t?.agvCode === a?.code && (t?.status === 'running' || t?.status === 'assigned' || t?.status === 'paused'))
    return {
      code: a?.code || '',
      status: (a?.status || 'idle') as any,
      path: activeTask ? `${activeTask.startPoint || ''} → ${activeTask.endPoint || ''}` : (a?.position || ''),
      conflicts: 0,
      battery: Number(a?.batteryLevel ?? 0),
      speed: Number(a?.speed ?? 0),
      position: a?.position || '',
      batteryLevel: Number(a?.batteryLevel ?? 0),
      direction: a?.direction || ''
    }
  })
}

onMounted(async () => {
  await refreshAgvAndTasks()
})

// 计算最优路径
const calculateOptimizedPath = async () => {
  try {
    ElMessage({ message: '正在计算最优路径...', type: 'info' })
    const simRes = await agvApi.simulatePath({
      startPoint: optimizationParams.value.start,
      endPoint: optimizationParams.value.end,
      algorithm: optimizationParams.value.algorithm
    } as any)
    const sim = unwrapApiData(simRes) || {}
    optimizedPaths.value = [
      {
        id: 1,
        algorithm: String(sim.algorithm || optimizationParams.value.algorithm),
        distance: Number(sim.distance ?? 0),
        time: Number(sim.time ?? 0),
        energy: Number(sim.energy ?? 0),
        status: 'optimal'
      }
    ]
    ElMessage({ message: '最优路径计算完成', type: 'success' })
  } catch (error) {
    ElMessage({ message: '最优路径计算失败', type: 'error' })
    console.error('Failed to calculate optimized path:', error)
  }
}

// 应用路径
const applyPath = async (path: any) => {
  try {
    ElMessage({ message: '正在应用路径...', type: 'info' })
    await agvApi.createPathPlan({
      taskId: '',
      agvCode: '',
      startPoint: optimizationParams.value.start,
      endPoint: optimizationParams.value.end,
      distance: path.distance,
      estimatedTime: path.time,
      algorithm: path.algorithm
    } as any)
    ElMessage({ message: `已应用 ${path.algorithm} 路径`, type: 'success' })
  } catch (error) {
    ElMessage({ message: '路径应用失败', type: 'error' })
    console.error('Failed to apply path:', error)
  }
}

// 获取AGV状态类型
const getAgvStatusType = (status: string) => {
  switch (status) {
    case 'running': return 'success'
    case 'idle': return 'info'
    case 'charging': return 'warning'
    default: return 'info'
  }
}

// 开始模拟
const startSimulation = async () => {
  try {
    ElMessage({ message: '正在开始路径模拟...', type: 'info' })
    const simRes = await agvApi.simulatePath({
      startPoint: optimizationParams.value.start,
      endPoint: optimizationParams.value.end,
      algorithm: optimizationParams.value.algorithm
    } as any)
    const sim = unwrapApiData(simRes) || {}
    simulationResults.value = {
      simulationTime: String(sim.simulationId || ''),
      completedTasks: Number(sim.completedTasks ?? 0),
      conflictCount: Number(sim.conflictCount ?? 0),
      avgPathLength: Number(sim.distance ?? 0),
      avgCompletionTime: Number(sim.time ?? 0),
      energyConsumption: Number(sim.energy ?? 0)
    }
    ElMessage({ message: '路径模拟已开始', type: 'success' })
  } catch (error) {
    ElMessage({ message: '模拟启动失败', type: 'error' })
    console.error('Failed to start simulation:', error)
  }
}

// 暂停模拟
const pauseSimulation = () => {
  try {
    ElMessage({ message: '路径模拟已暂停', type: 'warning' })
    console.log('Simulation paused')
  } catch (error) {
    ElMessage({ message: '暂停模拟失败', type: 'error' })
    console.error('Failed to pause simulation:', error)
  }
}

// 停止模拟
const stopSimulation = () => {
  try {
    ElMessage({ message: '路径模拟已停止', type: 'info' })
    // 重置模拟结果
    simulationResults.value = {
      simulationTime: '00:00:00',
      completedTasks: 0,
      conflictCount: 0,
      avgPathLength: 0,
      avgCompletionTime: 0,
      energyConsumption: 0
    }
    console.log('Simulation stopped')
  } catch (error) {
    ElMessage({ message: '停止模拟失败', type: 'error' })
    console.error('Failed to stop simulation:', error)
  }
}

// 重置模拟
const resetSimulation = () => {
  try {
    simulationParams.value = {
      agvCount: 5,
      speed: 1,
      duration: 10,
      obstacleDensity: 'medium'
    }
    simulationResults.value = {
      simulationTime: '00:00:00',
      completedTasks: 0,
      conflictCount: 0,
      avgPathLength: 0,
      avgCompletionTime: 0,
      energyConsumption: 0
    }
    ElMessage({ message: '模拟已重置', type: 'success' })
    console.log('Simulation reset')
  } catch (error) {
    ElMessage({ message: '重置模拟失败', type: 'error' })
    console.error('Failed to reset simulation:', error)
  }
}

// 重新规划路径
const replanPath = async () => {
  try {
    ElMessage({ message: '正在重新规划路径...', type: 'info' })
    const plansRes = await agvApi.getPathPlans()
    const plans: any[] = unwrapApiList(plansRes) || []
    if (!plans.length) {
      ElMessage({ message: '暂无可优化路径规划', type: 'warning' })
      return
    }
    await agvApi.optimizePath(plans[0].id)
    ElMessage({ message: '路径重新规划完成', type: 'success' })
  } catch (error) {
    ElMessage({ message: '路径重新规划失败', type: 'error' })
    console.error('Failed to replan path:', error)
  }
}

// 暂停AGV
const pauseAGV = async () => {
  try {
    ElMessage({ message: '正在暂停AGV...', type: 'info' })
    const code = agvList.value[0]?.code || ''
    if (!code) {
      ElMessage({ message: '暂无AGV可操作', type: 'warning' })
      return
    }
    await agvApi.sendPauseCommand(code)
    await refreshAgvAndTasks()
    ElMessage({ message: 'AGV已暂停', type: 'success' })
  } catch (error) {
    ElMessage({ message: 'AGV暂停失败', type: 'error' })
    console.error('Failed to pause AGV:', error)
  }
}

// 查看历史路径
const viewHistoryPaths = async () => {
  try {
    ElMessage({ message: '正在加载历史路径...', type: 'info' })
    const plansRes = await agvApi.getPathPlans()
    const plans: any[] = unwrapApiList(plansRes) || []
    ElMessage({ message: `历史路径加载完成，共加载${plans.length}条`, type: 'success' })
  } catch (error) {
    ElMessage({ message: '历史路径加载失败', type: 'error' })
    console.error('Failed to load history paths:', error)
  }
}
</script>

<style scoped>
.path-planning-view {
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

/* 路径信息面板 */
.path-info-panel {
  margin-top: 20px;
}

.path-info-panel h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 优化内容布局 */
.optimization-content,
.coordination-content,
.simulation-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .optimization-content,
  .coordination-content,
  .simulation-content {
    grid-template-columns: 1fr;
  }
}

/* 优化参数区域 */
.optimization-params h3,
.optimization-result h3,
.agv-list-panel h3,
.simulation-panel h3,
.simulation-results h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 操作按钮组 */
.action-buttons {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

/* 动态路径内容 */
.dynamic-path-content {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
}

@media (max-width: 1200px) {
  .dynamic-path-content {
    grid-template-columns: 1fr;
  }
}

/* 模拟可视化区域 */
.simulation-visualization {
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
  .path-planning-view {
    padding: 16px;
  }
  
  h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 768px) {
  .path-planning-view {
    padding: 12px;
  }
  
  h2 {
    font-size: 1.3rem;
    margin-bottom: 16px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .simulation-visualization {
    height: 300px;
  }
}
</style>
