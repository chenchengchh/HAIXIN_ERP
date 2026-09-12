<template>
  <div class="mes-wip-container">
    <!-- 面包屑导航 -->
    <div class="page-header">
      <h2>在制品管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home/mes">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes">MES系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/mes/wip">在制品管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item :to="`/home/mes/wip#${activeTab}`">
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" class="function-tabs">
        <el-tab-pane label="在制品位置跟踪" name="wip-location">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <el-input
                v-model="wipSearchSn"
                placeholder="请输入产品序列号"
                style="width: 200px; margin-right: 10px"
                clearable
                @keyup.enter="searchWipDetail">
                <template #append>
                  <el-button @click="searchWipDetail">搜索</el-button>
                </template>
              </el-input>
            </div>
            <div class="card-content">
              <el-table v-loading="loading.wipLocations" :data="wipLocations" style="width: 100%">
                <el-table-column prop="snCode" label="产品序列号" min-width="150" />
                <el-table-column prop="workOrderNo" label="工单号" min-width="120" />
                <el-table-column prop="currentStepName" label="当前工序" min-width="120" />
                <el-table-column prop="currentStationName" label="当前工站" min-width="120" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'queuing' ? 'info' : 
                             scope.row.status === 'processing' ? 'warning' : 
                             scope.row.status === 'completed' ? 'success' : 'danger'">
                      {{ statusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="updateTime" label="更新时间" width="150" />
                <el-table-column label="操作" width="150" fixed="right">
                  <template #default="scope">
                    <el-button type="info" size="small" @click="viewWipDetail(scope.row)">
                      查看详情
                    </el-button>
                    <el-button type="primary" size="small" @click="updateWipStatus(scope.row)">
                      更新状态
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="批次管理" name="batch-management">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <el-button type="primary" @click="showBatchDialog = true">
                <el-icon><Plus /></el-icon> 新增批次
              </el-button>
            </div>
            <div class="card-content">
              <el-table v-loading="loading.batches" :data="batches" style="width: 100%">
                <el-table-column prop="batchNo" label="批次号" min-width="150" />
                <el-table-column prop="workOrderNo" label="工单号" min-width="120" />
                <el-table-column prop="materialName" label="产品名称" min-width="150" />
                <el-table-column prop="qty" label="数量" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'in_process' ? 'warning' : 
                             scope.row.status === 'completed' ? 'success' : 'danger'">
                      {{ batchStatusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="150" />
                <el-table-column prop="updateTime" label="更新时间" width="150" />
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="scope">
                    <el-button type="info" size="small" @click="viewBatchDetail(scope.row)">
                      批次详情
                    </el-button>
                    <el-button type="primary" size="small" @click="trackBatch(scope.row)">
                      批次追溯
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
        
        <el-tab-pane label="流转记录" name="flow-records">
          <el-card shadow="hover" class="content-card">
            <div class="card-header">
              <div class="flow-record-filters">
                <el-input
                  v-model="flowSearchSn"
                  placeholder="请输入产品序列号"
                  style="width: 200px; margin-right: 10px"
                  clearable
                  @keyup.enter="filterFlowRecords">
                  <template #append>
                    <el-button @click="filterFlowRecords">搜索</el-button>
                  </template>
                </el-input>
                <el-select
                  v-model="flowStatusFilter"
                  placeholder="选择流转状态"
                  style="width: 150px; margin-right: 10px"
                  clearable
                  @change="filterFlowRecords">
                  <el-option label="全部" value="" />
                  <el-option label="成功" value="success" />
                  <el-option label="失败" value="failed" />
                </el-select>
              </div>
            </div>
            <div class="card-content">
                <el-table v-loading="loading.flowRecords" :data="filteredFlowRecords" style="width: 100%">
                <el-table-column prop="snCode" label="产品序列号" min-width="150" />
                <el-table-column prop="batchNo" label="批次号" min-width="120" />
                <el-table-column prop="fromStepName" label="来源工序" min-width="120" />
                <el-table-column prop="toStepName" label="目标工序" min-width="120" />
                <el-table-column prop="fromStationName" label="来源工站" min-width="120" />
                <el-table-column prop="toStationName" label="目标工站" min-width="120" />
                <el-table-column prop="operatorName" label="操作员" min-width="100" />
                <el-table-column prop="timestamp" label="流转时间" width="150" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag
                      :type="scope.row.status === 'success' ? 'success' : 'danger'">
                      {{ flowStatusMap[scope.row.status] }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 在制品详情对话框 -->
    <el-dialog
      v-model="showWipDetailDialog"
      title="在制品详情"
      width="800px"
      destroy-on-close>
      <div v-if="selectedWip" class="wip-detail">
        <h3>基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="产品序列号">{{ selectedWip.snCode }}</el-descriptions-item>
          <el-descriptions-item label="工单号">{{ selectedWip.workOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="当前工序">{{ selectedWip.currentStepName }}</el-descriptions-item>
          <el-descriptions-item label="当前工站">{{ selectedWip.currentStationName }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusMap[selectedWip.status] }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ selectedWip.updateTime }}</el-descriptions-item>
        </el-descriptions>

        <h3 style="margin-top: 20px">位置历史</h3>
        <el-table :data="selectedWip.locationHistory" style="width: 100%">
          <el-table-column prop="stationName" label="工站名称" min-width="120" />
          <el-table-column prop="stepName" label="工序名称" min-width="120" />
          <el-table-column prop="startTime" label="开始时间" width="150" />
          <el-table-column prop="endTime" label="结束时间" width="150" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 批次创建对话框 -->
    <el-dialog
      v-model="showBatchDialog"
      title="新增批次"
      width="600px"
      destroy-on-close>
      <el-form :model="newBatch" label-position="top" :rules="batchRules" ref="batchFormRef">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="批次号" prop="batchNo">
              <el-input v-model="newBatch.batchNo" placeholder="请输入批次号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工单号" prop="workOrderNo">
              <el-input v-model="newBatch.workOrderNo" placeholder="请输入工单号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产品名称" prop="materialName">
              <el-input v-model="newBatch.materialName" placeholder="请输入产品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量" prop="qty">
              <el-input-number v-model="newBatch.qty" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchDialog = false">取消</el-button>
          <el-button type="primary" @click="submitBatch">确认</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 批次详情对话框 -->
    <el-dialog
      v-model="showBatchDetailDialog"
      title="批次详情"
      width="800px"
      destroy-on-close>
      <div v-if="selectedBatch" class="batch-detail">
        <h3>基本信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次号">{{ selectedBatch.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="工单号">{{ selectedBatch.workOrderNo }}</el-descriptions-item>
          <el-descriptions-item label="产品名称">{{ selectedBatch.materialName }}</el-descriptions-item>
          <el-descriptions-item label="数量">{{ selectedBatch.qty }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ batchStatusMap[selectedBatch.status] }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ selectedBatch.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ selectedBatch.updateTime }}</el-descriptions-item>
        </el-descriptions>


      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchDetailDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 批次追溯对话框 -->
    <el-dialog
      v-model="showBatchTraceDialog"
      title="批次追溯"
      width="900px"
      destroy-on-close>
      <div v-if="selectedBatch" class="batch-trace">
        <h3>批次追溯信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次号">{{ selectedBatch.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="工单号">{{ selectedBatch.workOrderNo }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBatchTraceDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 在制品状态更新对话框 -->
    <el-dialog
      v-model="showUpdateStatusDialog"
      title="更新在制品状态"
      width="500px"
      destroy-on-close>
      <div v-if="selectedWipForUpdate" class="wip-status-update">
        <el-form :model="statusUpdateForm" label-position="top" ref="statusUpdateFormRef">
          <el-form-item label="产品序列号">
            <el-input v-model="selectedWipForUpdate.snCode" readonly />
          </el-form-item>
          <el-form-item label="当前状态">
            <el-input v-model="statusMap[selectedWipForUpdate.status]" readonly />
          </el-form-item>
          <el-form-item label="新状态" required>
            <el-select v-model="newWipStatus" placeholder="请选择新状态" style="width: 100%">
              <el-option label="排队中" value="queuing" />
              <el-option label="处理中" value="processing" />
              <el-option label="已完成" value="completed" />
              <el-option label="已报废" value="scrapped" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUpdateStatusDialog = false">取消</el-button>
          <el-button type="primary" @click="submitWipStatusUpdate">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useMesWipStore } from '../../../stores/mes/wip'
import { storeToRefs } from 'pinia'
import type { WipLocation, Batch, FlowRecord } from '../../../api/mes/wip'
import { ArrowRight, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 状态管理
// 使用storeToRefs保持state/getters响应性，actions直接解构
const store = useMesWipStore()
const { 
  wipLocations, 
  batches, 
  flowRecords, 
  processingWip, 
  completedBatches, 
  successfulFlowRecords,
  loading
} = storeToRefs(store)
const {
  fetchWipLocations,
  fetchBatches,
  fetchFlowRecords,
  fetchWipDetail,
  updateWipLocation,
  createBatch
} = store

// 状态映射
const statusMap: Record<string, string> = {
  queuing: '排队中',
  processing: '处理中',
  completed: '已完成',
  scrapped: '已报废'
}

const batchStatusMap: Record<string, string> = {
  in_process: '处理中',
  completed: '已完成',
  scrapped: '已报废'
}

const flowStatusMap: Record<string, string> = {
  success: '成功',
  failed: '失败'
}

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'wip-location': '在制品位置跟踪',
  'batch-management': '批次管理',
  'flow-records': '流转记录'
}

// 活跃标签页
const activeTab = ref('wip-location')

// 搜索
const wipSearchSn = ref('')

// 流转记录筛选
const flowSearchSn = ref('')
const flowStatusFilter = ref('')
// 本地筛选结果，初始为空，通过watch监听store数据变化后自动填充
const filteredFlowRecords = ref<FlowRecord[]>([])

// 对话框
const showWipDetailDialog = ref(false)
const selectedWip = ref<WipLocation | null>(null)

const showBatchDialog = ref(false)
const batchFormRef = ref()
const newBatch = reactive<Partial<Batch>>({
  batchNo: '',
  workOrderNo: '',
  materialName: '',
  qty: 1
})

// 批次详情和追溯对话框
const showBatchDetailDialog = ref(false)
const showBatchTraceDialog = ref(false)
const selectedBatch = ref<Batch | null>(null)

const batchRules = {
  batchNo: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  workOrderNo: [{ required: true, message: '请输入工单号', trigger: 'blur' }],
  materialName: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  qty: [{ required: true, type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }]
}

// 页面加载时获取数据
onMounted(() => {
  fetchWipLocations()
  fetchBatches()
  fetchFlowRecords()
})

// 搜索在制品详情
const searchWipDetail = async () => {
  if (wipSearchSn.value) {
    try {
      await fetchWipDetail(wipSearchSn.value)
      // 找到对应的在制品
      const wip = wipLocations.value.find(w => w.snCode === wipSearchSn.value)
      if (wip) {
        selectedWip.value = wip
        showWipDetailDialog.value = true
      }
    } catch (err) {
      console.error('搜索在制品详情失败:', err)
    }
  }
}

// 查看在制品详情
const viewWipDetail = (wip: WipLocation) => {
  selectedWip.value = wip
  showWipDetailDialog.value = true
}

// 查看批次详情
const viewBatchDetail = (batch: Batch) => {
  selectedBatch.value = batch
  showBatchDetailDialog.value = true
}

// 批次追溯
const trackBatch = (batch: Batch) => {
  selectedBatch.value = batch
  showBatchTraceDialog.value = true
}

// 更新在制品状态
const updateWipStatus = (wip: WipLocation) => {
  // 弹出状态更新表单
  showUpdateStatusDialog.value = true
  selectedWipForUpdate.value = wip
  newWipStatus.value = wip.status
}

// 在制品状态更新表单
const statusUpdateFormRef = ref()
const statusUpdateForm = reactive({})

// 在制品状态更新对话框
const showUpdateStatusDialog = ref(false)
const selectedWipForUpdate = ref<WipLocation | null>(null)
const newWipStatus = ref<'queuing' | 'processing' | 'completed' | 'scrapped'>('processing')

// 提交状态更新
const submitWipStatusUpdate = async () => {
  if (selectedWipForUpdate.value) {
    try {
      await updateWipLocation({
        snCode: selectedWipForUpdate.value.snCode,
        stationId: selectedWipForUpdate.value.currentStationId || '',
        stepId: selectedWipForUpdate.value.currentStepId || '',
        status: newWipStatus.value
      })
      showUpdateStatusDialog.value = false
      // 刷新数据
      await fetchWipLocations()
      ElMessage.success('在制品状态更新成功')
    } catch (err) {
      console.error('更新在制品状态失败:', err)
      ElMessage.error('更新在制品状态失败，请重试')
    }
  }
}

// 批次状态统计
const batchStatusStats = computed(() => {
  return {
    inProcess: batches.value.filter(batch => batch.status === 'in_process').length,
    completed: batches.value.filter(batch => batch.status === 'completed').length,
    scrapped: batches.value.filter(batch => batch.status === 'scrapped').length
  }
})

// 在制品状态统计
const wipStatusStats = computed(() => {
  return {
    queuing: wipLocations.value.filter(wip => wip.status === 'queuing').length,
    processing: wipLocations.value.filter(wip => wip.status === 'processing').length,
    completed: wipLocations.value.filter(wip => wip.status === 'completed').length,
    scrapped: wipLocations.value.filter(wip => wip.status === 'scrapped').length
  }
})

// 筛选流转记录
const filterFlowRecords = () => {
  let filtered = flowRecords.value
  
  // 根据产品序列号筛选
  if (flowSearchSn.value) {
    filtered = filtered.filter((record: FlowRecord) => record.snCode.includes(flowSearchSn.value))
  }
  
  // 根据状态筛选
  if (flowStatusFilter.value) {
    filtered = filtered.filter((record: FlowRecord) => record.status === flowStatusFilter.value)
  }
  
  filteredFlowRecords.value = filtered
}

// 监听store中流转记录变化，数据加载完成后自动应用当前筛选条件
watch(flowRecords, () => {
  filterFlowRecords()
})

// 提交批次
const submitBatch = async () => {
  if (batchFormRef.value) {
    await batchFormRef.value.validate(async (valid: boolean) => {
      if (valid) {
        try {
          await createBatch(newBatch)
          showBatchDialog.value = false
          // 重置表单
          Object.assign(newBatch, {
            batchNo: '',
            workOrderNo: '',
            materialName: '',
            qty: 1
          })
          if (batchFormRef.value) {
            batchFormRef.value.resetFields()
          }
          // 刷新数据
          fetchBatches()
          // 显示成功消息
          ElMessage.success('批次创建成功')
        } catch (err) {
          console.error('创建批次失败:', err)
          ElMessage.error('创建批次失败，请重试')
        }
      }
    })
  }
}
</script>

<style scoped>
.mes-wip-container {
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

.wip-detail h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-weight: bold;
}

/* 流转记录筛选样式 */
.flow-record-filters {
  display: flex;
  align-items: center;
}

/* 批次追溯卡片样式 */
.trace-card-success {
  border-left: 4px solid #67C23A;
}

.trace-card-failed {
  border-left: 4px solid #F56C6C;
}

.trace-content {
  padding: 10px 0;
}

.trace-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
}

.trace-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 10px;
}

.trace-operator,
.trace-sn {
  font-size: 14px;
  color: #606266;
}

.trace-description {
  font-size: 14px;
  margin-bottom: 10px;
  color: #303133;
}

.trace-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
}

.trace-time {
  font-style: italic;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mes-wip-container {
    padding: 12px;
  }
  
  h1 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .content-card {
    margin: 10px;
  }
  
  .flow-record-filters {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    width: 100%;
  }
  
  .flow-record-filters .el-input,
  .flow-record-filters .el-select {
    width: 100% !important;
    margin-right: 0 !important;
  }
  
  .trace-info {
    flex-direction: column;
    gap: 5px;
  }
}
</style>
