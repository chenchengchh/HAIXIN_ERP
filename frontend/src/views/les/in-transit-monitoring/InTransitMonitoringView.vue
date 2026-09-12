<template>
  <div class="in-transit-monitoring-view">
    <div class="page-header">
      <h2>在途监控</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/les">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/les">LES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/les/in-transit-monitoring">在途监控</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/les/in-transit-monitoring#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 功能标签页 -->
    <el-tabs v-model="activeTab" class="function-tabs">
      <!-- 实时位置追踪 -->
      <el-tab-pane label="实时位置追踪" name="location">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>基于 GIS 地图展示车辆实时 GPS 定位，支持多车动态聚合</span>
              </div>
            </template>
            <div class="location-content">
              <!-- 筛选条件 -->
              <div class="filter-bar">
                <el-select v-model="locationFilter.vehicleId" placeholder="选择车辆" style="width: 150px; margin-right: 16px;">
                  <el-option label="全部车辆" value="" />
                  <el-option v-for="vehicle in store.vehicles" :key="vehicle.id" :label="vehicle.licensePlate" :value="vehicle.id" />
                </el-select>
                <el-select v-model="locationFilter.status" placeholder="选择状态" style="width: 120px; margin-right: 16px;">
                  <el-option label="全部" value="" />
                  <el-option label="行驶中" value="driving" />
                  <el-option label="装货中" value="loading" />
                  <el-option label="卸货中" value="unloading" />
                  <el-option label="停留" value="stopped" />
                </el-select>
                <el-button type="primary" @click="handleRefreshLocation">刷新位置</el-button>
              </div>
              
              <!-- 地图容器 -->
              <div class="map-container">
                <!-- 模拟地图背景 -->
                <div class="map-background">
                  <div class="map-legend">
                    <span><span class="legend-dot" style="background: #67C23A;"></span> 正常</span>
                    <span><span class="legend-dot" style="background: #E6A23C;"></span> 警告</span>
                    <span><span class="legend-dot" style="background: #F56C6C;"></span> 异常</span>
                  </div>
                </div>
                <!-- 模拟车辆位置卡片 -->
                <div class="vehicle-locations">
                  <el-card v-for="vehicle in activeVehicles" :key="vehicle.vehicleId" class="vehicle-location-card">
                    <div class="vehicle-info">
                      <div class="vehicle-header">
                        <span class="vehicle-plate">{{ vehicle.licensePlate }}</span>
                        <el-tag :type="getStatusTag(vehicle.status)">{{ getStatusLabel(vehicle.status) }}</el-tag>
                      </div>
                      <div class="vehicle-details">
                        <p>司机: {{ vehicle.driverName }}</p>
                        <p>位置: {{ vehicle.currentLocation }}</p>
                        <p>速度: {{ vehicle.speed }} km/h</p>
                        <p>时间: {{ vehicle.lastUpdateTime }}</p>
                      </div>
                      <el-button size="small" type="primary" @click="viewVehicleDetail(vehicle)">查看详情</el-button>
                    </div>
                  </el-card>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 运输状态更新 -->
      <el-tab-pane label="运输状态更新" name="status">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>时间线展示装车、出发、在途、到达、卸货等各环节的时间戳与状态</span>
              </div>
            </template>
            <div class="status-content">
              <!-- 计划选择 -->
              <div class="plan-selector">
                <el-select v-model="selectedPlanId" placeholder="选择运输计划" style="width: 200px; margin-right: 16px;">
                  <el-option v-for="plan in inTransitPlans" :key="plan.id" :label="plan.planNo" :value="plan.id" />
                </el-select>
                <el-button type="primary" @click="handleViewStatus">查看状态</el-button>
              </div>
              
              <!-- 状态时间线 -->
              <div class="status-timeline">
                <el-timeline>
                  <el-timeline-item v-for="status in planStatusTimeline" :key="status.id" :timestamp="status.time" placement="top">
                    <el-card shadow="hover">
                      <div class="timeline-content">
                        <h4>{{ status.title }}</h4>
                        <p>{{ status.description }}</p>
                        <el-tag :type="status.type">{{ status.status }}</el-tag>
                      </div>
                    </el-card>
                  </el-timeline-item>
                </el-timeline>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 异常事件处理 -->
      <el-tab-pane label="异常事件处理" name="anomaly">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>对行驶偏差、长时间停留、交通事故、延误等异常进行预警与实时处理记录</span>
              </div>
            </template>
            <div class="anomaly-content">
              <!-- 异常事件列表 -->
              <el-table :data="store.anomalyEvents" style="width: 100%" height="500">
                <el-table-column label="事件编号" width="150">
                  <template #default="scope">
                    {{ (scope.row as any).eventNo || `EVT-${scope.row.id}` }}
                  </template>
                </el-table-column>
                <el-table-column prop="planId" label="计划ID" width="100" />
                <el-table-column prop="vehicleId" label="车辆ID" width="100" />
                <el-table-column prop="eventType" label="异常类型" width="120">
                  <template #default="scope">
                    <el-tag type="danger">{{ scope.row.eventType }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="事件描述" min-width="200">
                  <template #default="scope">
                    {{ (scope.row as any).eventDesc || scope.row.eventDescription }}
                  </template>
                </el-table-column>
                <el-table-column prop="eventTime" label="发生时间" width="180" />
                <el-table-column prop="handlingStatus" label="处理状态" width="120">
                  <template #default="scope">
                    <el-tag :type="scope.row.handlingStatus === 'handled' ? 'success' : 'warning'">
                      {{ scope.row.handlingStatus === 'handled' ? '已处理' : '待处理' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <el-button size="small" type="primary" @click="handleEventDetail(scope.row)">详情</el-button>
                    <el-button size="small" type="success" @click="handleEventResolve(scope.row)" v-if="scope.row.handlingStatus === 'pending'">处理</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
      
      <!-- 运输轨迹回放 -->
      <el-tab-pane label="运输轨迹回放" name="replay">
        <div class="tab-content">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>支持选择历史任务编号，查询在途监控轨迹点明细</span>
              </div>
            </template>
            <div class="replay-content">
              <!-- 回放控制 -->
              <div class="replay-control">
                <el-select v-model="replayFilter.planId" placeholder="选择历史任务" style="width: 200px; margin-right: 16px;">
                  <el-option v-for="plan in historicalPlans" :key="plan.id" :label="plan.planNo" :value="plan.id" />
                </el-select>
                <el-date-picker v-model="replayFilter.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 300px; margin-right: 16px;"></el-date-picker>
                <el-button type="primary" @click="handleLoadReplay">加载轨迹</el-button>
              </div>
              
              <div class="trace-data" v-if="traceData.length > 0">
                <h3>轨迹点数据</h3>
                <el-table :data="traceData" style="width: 100%" height="300">
                  <el-table-column prop="time" label="时间" width="180" />
                  <el-table-column prop="location" label="位置" min-width="150" />
                  <el-table-column prop="status" label="状态" width="120" />
                </el-table>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="vehicleDetailVisible" title="车辆详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车牌">{{ vehicleDetail?.licensePlate }}</el-descriptions-item>
        <el-descriptions-item label="车辆ID">{{ vehicleDetail?.vehicleId }}</el-descriptions-item>
        <el-descriptions-item label="司机">{{ vehicleDetail?.driverName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(vehicleDetail?.status || '') }}</el-descriptions-item>
        <el-descriptions-item label="位置" :span="2">{{ vehicleDetail?.currentLocation }}</el-descriptions-item>
        <el-descriptions-item label="速度(km/h)">{{ vehicleDetail?.speed }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ vehicleDetail?.lastUpdateTime }}</el-descriptions-item>
      </el-descriptions>

      <div style="margin-top: 16px;">
        <el-table :data="vehicleHistory" style="width: 100%" max-height="360">
          <el-table-column prop="recordTime" label="时间" width="180" />
          <el-table-column prop="latitude" label="纬度" width="120" />
          <el-table-column prop="longitude" label="经度" width="120" />
          <el-table-column prop="speed" label="速度(km/h)" width="120" />
          <el-table-column prop="direction" label="方向(°)" width="120" />
        </el-table>
      </div>
      <template #footer>
        <el-button type="primary" @click="vehicleDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="eventDetailVisible" title="异常事件详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="事件ID">{{ eventDetail?.id }}</el-descriptions-item>
        <el-descriptions-item label="计划ID">{{ eventDetail?.planId }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">{{ eventDetail?.eventType }}</el-descriptions-item>
        <el-descriptions-item label="事件时间">{{ eventDetail?.eventTime }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">{{ eventDetail?.handlingStatus }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ eventDetail?.handlingResult }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ eventDetail?.eventDescription }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ eventDetail?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="eventDetailVisible = false">关闭</el-button>
        <el-button v-if="eventDetail && eventDetail.handlingStatus !== 'handled'" type="primary" @click="handleEventResolve(eventDetail)">标记已处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import useLesStore from '../../../stores/les'
import * as types from '../../../types/les'
import { unwrapListResponse } from '../../../api'
import { monitoringApi } from '../../../api/les'
import { ElMessage } from 'element-plus'

// 激活的标签页
const activeTab = ref('location')

// 初始化store
const store = useLesStore()

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  location: '实时位置追踪',
  status: '运输状态更新',
  anomaly: '异常事件处理',
  replay: '运输轨迹回放'
}

// 活跃车辆（从store计算）
const activeVehicles = computed(() => {
  // 将vehicleLocations与vehicles和drivers关联
  const locations = Array.isArray(store.vehicleLocations) ? store.vehicleLocations : []
  const filtered = locations.filter(location => {
    if (locationFilter.value.vehicleId && String(location.vehicleId) !== String(locationFilter.value.vehicleId)) return false
    return true
  })
  return filtered.map(location => {
    const vehicle = store.vehicles.find(v => v.id === location.vehicleId)
    const driver = store.drivers.find(d => d.status === 'active') // 简化处理，实际应通过计划关联
    const latitude = Number((location as any).latitude)
    const longitude = Number((location as any).longitude)
    const speed = Number((location as any).speed)
    const timestamp = (location as any).timestamp ?? (location as any).recordTime ?? ''
    const status = Number.isFinite(speed) && speed > 0 ? 'driving' : 'stopped'
    if (locationFilter.value.status && status !== locationFilter.value.status) return null
    return {
      ...location,
      licensePlate: vehicle?.licensePlate || '未知',
      driverName: driver?.name || '未知',
      status,
      currentLocation:
        Number.isFinite(latitude) && Number.isFinite(longitude)
          ? `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`
          : '',
      lastUpdateTime: String(timestamp)
    }
  }).filter(Boolean) as any[]
})

// 在途计划（从store获取）
const inTransitPlans = computed(() => store.inTransitPlans)

// 历史计划（从store计算）
const historicalPlans = computed(() => store.completedPlans)

// 选中的计划ID
const selectedPlanId = ref<number | undefined>(undefined)

const planStatusTimeline = computed(() => {
  if (!selectedPlanId.value) return []
  const plan = store.transportPlans.find(p => p.id === selectedPlanId.value)
  if (!plan) return []
  const driver = store.drivers.find(d => d.id === plan.driverId)
  const vehicle = store.vehicles.find(v => v.id === plan.vehicleId)
  const voucher = store.signVouchers.find(v => v.planId === plan.id)
  const logs = store.monitorLogs.filter(l => l.planId === plan.id).sort((a, b) => new Date(a.recordTime).getTime() - new Date(b.recordTime).getTime())

  const timeline: any[] = []
  timeline.push({
    id: 1,
    title: '计划创建',
    description: `生成运输计划 ${plan.planNo}`,
    status: 'success',
    time: plan.createTime,
    type: 'success'
  })

  if (plan.driverId || plan.vehicleId) {
    timeline.push({
      id: 2,
      title: '资源指派',
      description: `司机：${driver?.name || '未指派'}，车辆：${vehicle?.licensePlate || '未指派'}`,
      status: 'success',
      time: plan.createTime,
      type: 'success'
    })
  }

  if (logs.length > 0) {
    const firstLog = logs[0]
    if (!firstLog) return timeline
    timeline.push({
      id: 3,
      title: '在途监控',
      description: `开始监控：${firstLog.currentStatus}`,
      status: 'processing',
      time: firstLog.recordTime,
      type: 'primary'
    })
  }

  if (plan.status === types.TransportPlanStatus.SIGNED && voucher?.arrivalTime) {
    timeline.push({
      id: 4,
      title: '完成签收',
      description: '运输计划已签收结案',
      status: 'success',
      time: voucher.arrivalTime,
      type: 'success'
    })
  }

  return timeline
})

// 筛选条件
const locationFilter = ref({
  vehicleId: '',
  status: ''
})

const replayFilter = ref({
  planId: '' as '' | number,
  dateRange: [] as Date[]
})

// 轨迹数据（从store计算）
const traceData = computed(() => {
  return store.monitorLogs
    .filter(log => log.planId === (replayFilter.value.planId || selectedPlanId.value))
    .map(log => ({
      time: log.recordTime,
      location: `${Number(log.latitude).toFixed(4)}, ${Number(log.longitude).toFixed(4)}`,
      status: log.currentStatus
    }))
    .sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime())
})

// 组件挂载时加载数据
onMounted(async () => {
  await Promise.all([
    store.fetchTransportPlans(),
    store.fetchVehicles(),
    store.fetchDrivers(),
    store.fetchMonitorLogs(),
    store.fetchAnomalyEvents(),
    store.fetchVehicleLocations()
  ])
})

// 获取状态标签类型
const getStatusTag = (status: string) => {
  switch (status) {
    case 'driving': return 'primary'
    case 'loading': return 'warning'
    case 'unloading': return 'warning'
    case 'stopped': return 'danger'
    default: return 'info'
  }
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  switch (status) {
    case 'driving': return '行驶中'
    case 'loading': return '装货中'
    case 'unloading': return '卸货中'
    case 'stopped': return '停留'
    default: return status
  }
}

// 刷新位置
const handleRefreshLocation = async () => {
  try {
    ElMessage({ message: '正在刷新位置数据...', type: 'info' })
    await Promise.all([
      store.fetchVehicleLocations(),
      store.fetchMonitorLogs(),
      store.fetchAnomalyEvents()
    ])
    ElMessage.success('位置数据已刷新')
    console.log('刷新位置数据')
  } catch (error) {
    ElMessage.error('刷新位置数据失败')
    console.error('Failed to refresh location data:', error)
  }
}

// 查看车辆详情
const viewVehicleDetail = async (vehicle: any) => {
  try {
    vehicleDetailVisible.value = true
    vehicleDetail.value = vehicle
    vehicleHistory.value = []
    const response = await monitoringApi.fetchVehicleHistory(Number(vehicle.vehicleId), { page: 1, size: 200 })
    vehicleHistory.value = unwrapListResponse<any>(response)
  } catch (error) {
    ElMessage.error(`加载车辆 ${vehicle.licensePlate} 详情失败`)
    console.error('Failed to view vehicle detail:', error)
  }
}

// 查看状态
const handleViewStatus = async () => {
  try {
    if (!selectedPlanId.value) {
      ElMessage.warning('请先选择运输计划')
      return
    }
    await store.fetchMonitorLogs(selectedPlanId.value)
    ElMessage.success('计划状态已加载')
    console.log('查看计划状态:', selectedPlanId.value)
  } catch (error) {
    ElMessage.error('加载计划状态失败')
    console.error('Failed to view plan status:', error)
  }
}

// 异常事件详情
const handleEventDetail = async (event: types.AnomalyEvent) => {
  try {
    eventDetail.value = event
    eventDetailVisible.value = true
  } catch (error) {
    ElMessage.error(`加载异常事件 ${event.id} 详情失败`)
    console.error('Failed to view event detail:', error)
  }
}

const vehicleDetailVisible = ref(false)
const vehicleDetail = ref<any | null>(null)
const vehicleHistory = ref<any[]>([])

const eventDetailVisible = ref(false)
const eventDetail = ref<types.AnomalyEvent | null>(null)

// 处理异常事件
const handleEventResolve = async (event: types.AnomalyEvent) => {
  try {
    ElMessage({ message: `正在处理异常事件 ${event.id}...`, type: 'info' })
    await store.handleAnomalyEvent(event.id, { handlingStatus: 'handled', handlingResult: '已处理' })
    await store.fetchAnomalyEvents()
    ElMessage.success(`异常事件 ${event.id} 已处理`)
    console.log('处理异常事件:', event)
  } catch (error) {
    ElMessage.error(`处理异常事件 ${event.id} 失败`)
    console.error('Failed to resolve event:', error)
  }
}

const handleLoadReplay = async () => {
  try {
    if (!replayFilter.value.planId) {
      ElMessage.warning('请先选择历史任务')
      return
    }
    await store.fetchMonitorLogs(Number(replayFilter.value.planId))
    ElMessage.success('轨迹数据已加载')
  } catch (error) {
    ElMessage.error('加载轨迹数据失败')
    console.error('Failed to load replay logs:', error)
  }
}
</script>

<style scoped>
.in-transit-monitoring-view {
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

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

/* 地图容器 */
.map-container {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 8px;
  position: relative;
}

/* 车辆位置卡片 */
.vehicle-locations {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 250px;
}

.vehicle-location-card {
  padding: 10px;
}

.vehicle-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.vehicle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.vehicle-plate {
  font-size: 16px;
}

.vehicle-details p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

/* 计划选择器 */
.plan-selector {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

/* 状态时间线 */
.status-timeline {
  padding: 10px 0;
}

.timeline-content h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: bold;
}

.timeline-content p {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #606266;
}

/* 回放控制 */
.replay-control {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 10px;
}

/* 回放地图 */
.replay-map {
  height: 400px;
  background-color: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

/* 回放进度 */
.replay-progress {
  margin-top: 20px;
}

.replay-info {
  display: flex;
  align-items: center;
  margin-top: 10px;
  font-size: 14px;
}

/* 轨迹数据 */
.trace-data {
  margin-top: 20px;
}

.trace-data h3 {
  margin-bottom: 16px;
  font-size: 1.2rem;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .in-transit-monitoring-view {
    padding: 16px;
  }
  
  h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 768px) {
  .in-transit-monitoring-view {
    padding: 12px;
  }
  
  h2 {
    font-size: 1.3rem;
    margin-bottom: 16px;
  }
  
  .filter-bar,
  .plan-selector,
  .replay-control {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .filter-bar .el-select,
  .plan-selector .el-select,
  .replay-control .el-select,
  .replay-control .el-date-picker {
    width: 100% !important;
  }
  
  .vehicle-locations {
    position: static;
    width: 100%;
    margin-top: 20px;
  }
  
  .replay-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
