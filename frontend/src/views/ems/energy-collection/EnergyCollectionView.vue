<template>
  <div class="energy-collection-view">
    <div class="page-header">
      <h2>能源采集</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/ems">EMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/ems/energy-collection">能源采集</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/ems/energy-collection#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    <el-tabs v-model="activeTab" class="function-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="实时数据采集" name="real-time" />
      <el-tab-pane label="多能源支持" name="multi-energy" />
      <el-tab-pane label="采集设备管理" name="meter-management" />
      <el-tab-pane label="数据校准" name="data-calibration" />
    </el-tabs>
    
    <!-- 实时数据采集 -->
    <div v-if="activeTab === 'real-time'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>实时数据采集</span>
          </div>
        </template>
        <div class="real-time-content">
          <el-table v-loading="emsStore.loading.realTimeData" :data="emsStore.realTimeData" style="width: 100%" height="400">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="energyType" label="能源类型" width="120" />
            <el-table-column prop="area" label="区域" width="120" />
            <el-table-column prop="actualValue" label="实时值" width="100" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="collectionTime" label="采集时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'normal' ? 'success' : 'danger'">
                  {{ scope.row.status === 'normal' ? '正常' : '异常' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 多能源支持 -->
    <div v-if="activeTab === 'multi-energy'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>多能源支持</span>
          </div>
        </template>
        <div class="multi-energy-content">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="energy-item">
                <h4>电力能源</h4>
                <p>支持380V/220V电力采集，精度0.5级</p>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="energy-item">
                <h4>水资源</h4>
                <p>支持脉冲式、超声波式水表采集</p>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="energy-item">
                <h4>燃气能源</h4>
                <p>支持天然气、液化气等气体采集</p>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="energy-item">
                <h4>热能能源</h4>
                <p>支持蒸汽、热水等热能采集</p>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>
    
    <!-- 采集设备管理 -->
    <div v-if="activeTab === 'meter-management'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>采集设备管理</span>
            <el-button type="primary" size="small" @click="handleAddDevice">添加设备</el-button>
          </div>
        </template>
        <div class="meter-management-content">
          <el-table v-loading="emsStore.loading.meterDevices" :data="emsStore.meterDevices" style="width: 100%" height="400">
            <el-table-column prop="id" label="设备ID" width="100" />
            <el-table-column prop="name" label="设备名称" width="150" />
            <el-table-column prop="type" label="设备类型" width="120" />
            <el-table-column prop="ipAddress" label="IP地址" width="150" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'online' ? 'success' : 'danger'">
                  {{ scope.row.status === 'online' ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastUpdate" label="最后更新" width="180" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" type="primary" @click="handleEditDevice(scope.row)">编辑</el-button>
                <el-button size="small" @click="handleDeleteDevice(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    
    <!-- 设备编辑对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑设备"
      width="500px"
      destroy-on-close
    >
      <el-form :model="editDeviceForm" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="editDeviceForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model="editDeviceForm.type" placeholder="请输入设备类型" />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="editDeviceForm.ipAddress" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="editDeviceForm.status" placeholder="选择设备状态">
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveDevice">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 设备添加对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加设备"
      width="500px"
      destroy-on-close
    >
      <el-form :model="addDeviceForm" label-width="100px">
        <el-form-item label="设备名称">
          <el-input v-model="addDeviceForm.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型">
          <el-input v-model="addDeviceForm.type" placeholder="请输入设备类型" />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="addDeviceForm.ipAddress" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="设备状态">
          <el-select v-model="addDeviceForm.status" placeholder="选择设备状态">
            <el-option label="在线" value="online" />
            <el-option label="离线" value="offline" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveAddDevice">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 数据校准 -->
    <div v-if="activeTab === 'data-calibration'" class="tab-content">
      <el-card shadow="hover" class="sub-card">
        <template #header>
          <div class="sub-card-header">
            <span>数据校准</span>
          </div>
        </template>
        <div class="data-calibration-content">
          <el-form :model="calibrationForm" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="设备选择">
                  <el-select v-model="calibrationForm.meterId" placeholder="选择设备">
                    <el-option 
                      v-for="device in emsStore.meterDevices" 
                      :key="device.id" 
                      :label="device.name" 
                      :value="device.id.toString()" 
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="原始值">
                  <el-input-number v-model="calibrationForm.rawValue" :min="0" :precision="2" placeholder="输入仪表原始读数" style="width: 100%;" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="校准系数">
                  <el-input-number v-model="calibrationForm.coefficient" :min="0.1" :max="2" :step="0.1" placeholder="输入校准系数" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item label="校准原因">
                  <el-input v-model="calibrationForm.reason" type="textarea" placeholder="输入校准原因" />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item>
                  <el-button type="primary" @click="handleCalibrate">执行校准</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
          
          <!-- 校准历史记录 -->
          <div class="calibration-history">
            <h4>校准历史记录</h4>
            <el-table :data="emsStore.calibrationHistory" style="width: 100%" height="200">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="meterName" label="设备名称" width="120" />
              <el-table-column prop="rawValue" label="原始值" width="100" />
              <el-table-column prop="calibratedValue" label="校准值" width="100" />
              <el-table-column prop="coefficient" label="校准系数" width="100" />
              <el-table-column prop="calibrationTime" label="校准时间" width="180" />
            </el-table>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useEmsStore } from '@/stores/ems'

// 路由实例
const router = useRouter()
const route = useRoute()

// 激活的标签页
const activeTab = ref('real-time')

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'real-time': '实时数据采集',
  'multi-energy': '多能源支持',
  'meter-management': '采集设备管理',
  'data-calibration': '数据校准'
}

// 状态管理
const emsStore = useEmsStore()

// 数据校准表单
const calibrationForm = ref({
  meterId: '',
  rawValue: undefined as number | undefined,
  coefficient: 1,
  reason: ''
})

// 设备编辑对话框
const editDialogVisible = ref(false)
const editDeviceForm = ref({
  id: 0,
  name: '',
  type: '',
  ipAddress: '',
  status: 'online'
})

// 添加设备对话框
const addDialogVisible = ref(false)
const addDeviceForm = reactive({
  name: '',
  type: '',
  ipAddress: '',
  status: 'online' as const
})

// 从路由获取标签页状态
const getActiveTabFromRoute = () => {
  const tabMap: Record<string, string> = {
    'real-time': 'real-time',
    'multi-energy': 'multi-energy',
    'meter-management': 'meter-management',
    'data-calibration': 'data-calibration'
  }
  const tabName = route.params.tab || 'real-time'
  return tabMap[tabName as string] || 'real-time'
}

// 组件挂载时，从路由获取标签页状态并加载数据
onMounted(async () => {
  activeTab.value = getActiveTabFromRoute()
  
  // 加载能源采集相关数据
  await Promise.all([
    emsStore.fetchRealTimeData(),
    emsStore.fetchMeterDevices(),
    emsStore.fetchCalibrationHistory()
  ])
})

// 标签页切换事件
const handleTabChange = (tabName: string) => {
  console.log('切换标签页:', tabName)
  // 更新路由，保持标签页状态
  router.push({
    path: `/home/ems/energy-collection/${tabName}`
  })
}

// 方法
/**
 * 编辑设备
 * @param device 设备信息
 */
const handleEditDevice = (device: any) => {
  console.log('编辑设备:', device)
  // 填充编辑表单
  editDeviceForm.value = {
    id: device.id,
    name: device.name,
    type: device.type,
    ipAddress: device.ipAddress,
    status: device.status
  }
  // 打开编辑对话框
  editDialogVisible.value = true
}

/**
 * 保存设备编辑
 */
const handleSaveDevice = async () => {
  console.log('保存设备:', editDeviceForm.value)
  try {
    const result = await emsStore.updateMeterDevice(editDeviceForm.value.id, {
      name: editDeviceForm.value.name,
      type: editDeviceForm.value.type,
      ipAddress: editDeviceForm.value.ipAddress,
      status: editDeviceForm.value.status
    })
    if (!result) {
      ElMessage.error('设备编辑失败')
      return
    }
    ElMessage.success('设备编辑成功')
    editDialogVisible.value = false
  } catch (error) {
    ElMessage.error('设备编辑失败')
  }
}

/**
 * 删除设备
 * @param device 设备信息
 */
const handleDeleteDevice = async (device: any) => {
  try {
    // 确认删除
    await ElMessageBox.confirm('确定要删除该设备吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用删除设备API
    await emsStore.deleteMeterDevice(device.id)
    ElMessage.success('设备删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除设备失败:', error)
      ElMessage.error('设备删除失败')
    }
  }
}

/**
 * 打开添加设备对话框
 */
const handleAddDevice = () => {
  addDeviceForm.name = ''
  addDeviceForm.type = ''
  addDeviceForm.ipAddress = ''
  addDeviceForm.status = 'online'
  addDialogVisible.value = true
}

/**
 * 保存添加设备
 */
const handleSaveAddDevice = async () => {
  console.log('添加设备:', addDeviceForm)
  try {
    const result = await emsStore.addMeterDevice({
      name: addDeviceForm.name,
      type: addDeviceForm.type,
      ipAddress: addDeviceForm.ipAddress,
      status: addDeviceForm.status
    })
    if (!result) {
      ElMessage.error('设备添加失败')
      return
    }
    ElMessage.success('设备添加成功')
    addDialogVisible.value = false
  } catch (error) {
    ElMessage.error('设备添加失败')
  }
}

/**
 * 执行数据校准
 */
const handleCalibrate = async () => {
  console.log('执行校准:', calibrationForm.value)
  if (!calibrationForm.value.meterId) {
    ElMessage.warning('请选择要校准的设备')
    return
  }
  if (calibrationForm.value.rawValue === undefined || calibrationForm.value.rawValue === null) {
    ElMessage.warning('请输入仪表原始读数')
    return
  }

  // 根据选择的设备ID查找设备名称（后端必填参数）
  const selectedDevice = emsStore.meterDevices.find(
    (device: any) => device.id.toString() === calibrationForm.value.meterId
  )
  if (!selectedDevice) {
    ElMessage.warning('未找到所选设备，请重新选择')
    return
  }

  try {
    const success = await emsStore.executeCalibration({
      meterId: parseInt(calibrationForm.value.meterId),
      meterName: selectedDevice.name,
      rawValue: calibrationForm.value.rawValue,
      coefficient: calibrationForm.value.coefficient,
      reason: calibrationForm.value.reason
    })
    if (!success) {
      ElMessage.error('数据校准失败')
      return
    }
    ElMessage.success('数据校准成功')
    handleReset()
  } catch (error) {
    console.error('执行数据校准失败:', error)
    ElMessage.error('数据校准失败')
  }
}

/**
 * 重置校准表单
 */
const handleReset = () => {
  calibrationForm.value = {
    meterId: '',
    rawValue: undefined,
    coefficient: 1,
    reason: ''
  }
}
</script>

<style scoped>
.energy-collection-view {
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

.tab-content {
  padding: 10px 0;
}

.real-time-content {
  padding: 10px 0;
}

.multi-energy-content {
  padding: 10px 0;
}

.energy-item {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  height: 100%;
}

.energy-item h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.energy-item p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.meter-management-content {
  padding: 10px 0;
}

.data-calibration-content {
  padding: 10px 0;
}

.calibration-history {
  margin-top: 20px;
}

.calibration-history h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .energy-collection-view {
    padding: 12px;
  }
  
  .sub-card {
    margin-bottom: 12px;
  }
}
</style>
