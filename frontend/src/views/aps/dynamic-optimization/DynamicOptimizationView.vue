<template>
  <div class="dynamic-optimization-view">
    <div class="page-header">
      <h2>动态优化</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/aps' }">APS系统</el-breadcrumb-item>
        <el-breadcrumb-item>动态优化</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards" style="margin-bottom: 20px;">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="订单变更数" :value="statistics.orderChanges">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em;"><Edit /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="设备故障数" :value="statistics.equipmentFailures">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em; color: #f56c6c;"><Warning /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="物料短缺数" :value="statistics.materialShortages">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em; color: #e6a23c;"><Box /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="瓶颈工序数" :value="statistics.bottleneckProcesses">
              <template #prefix>
                <el-icon style="vertical-align: -0.125em; color: #409eff;"><Setting /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 动态优化模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane v-for="tab in tabs" :key="tab.name" :label="tab.label" :name="tab.name">
          <div class="tab-content">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>{{ tab.label }}</span>
                  <el-button type="primary" size="small" @click="handleRefresh">刷新数据</el-button>
                </div>
              </template>
              <!-- 搜索栏 -->
              <div class="search-bar" style="margin-bottom: 16px;">
                <el-input
                  v-model="searchForm.keyword"
                  placeholder="请输入描述或建议内容搜索"
                  style="width: 300px; margin-right: 10px;"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
                <el-select
                  v-model="searchForm.status"
                  placeholder="选择状态"
                  style="width: 150px; margin-right: 10px;"
                  clearable
                >
                  <el-option label="待处理" value="NEW" />
                  <el-option label="已采纳" value="ACCEPTED" />
                  <el-option label="已忽略" value="IGNORED" />
                </el-select>
              </div>
              <el-table :data="filteredSuggestions(tab.type)" stripe style="width: 100%" v-loading="loading" empty-text="暂无数据">
                <el-table-column prop="id" label="ID" width="80" fixed="left" />
                <el-table-column prop="description" label="异常描述" min-width="260" show-overflow-tooltip />
                <el-table-column prop="suggestion" label="优化建议" min-width="300" show-overflow-tooltip />
                <el-table-column prop="priority" label="优先级" width="100">
                  <template #default="scope">
                    <el-tag :type="getPriorityTagType(scope.row.priority)" size="small">
                      {{ getPriorityText(scope.row.priority) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getStatusTagType(scope.row.status)" size="small">
                      {{ getStatusText(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdTime" label="创建时间" width="170">
                  <template #default="scope">
                    {{ scope.row.createdTime ? new Date(scope.row.createdTime).toLocaleString() : '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="170" fixed="right">
                  <template #default="scope">
                    <template v-if="scope.row.status === 'NEW'">
                      <el-button type="primary" size="small" @click="handleAccept(scope.row)">
                        {{ tab.type === 'bottleneck' ? '优化' : '采纳' }}
                      </el-button>
                      <el-button type="warning" size="small" @click="handleIgnore(scope.row)">忽略</el-button>
                    </template>
                    <el-button v-else type="info" size="small" disabled>
                      {{ scope.row.status === 'ACCEPTED' ? '已采纳' : '已忽略' }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
// 导入Element Plus组件
import { ElMessage } from 'element-plus'
import { Edit, Warning, Box, Setting, Search } from '@element-plus/icons-vue'
import { unwrapListResponse } from '../../../api'
import { DataTransformer } from '../../../utils/data-transformer'
// 导入API服务
import { ScheduleOptimizationAPI } from '../../../api/aps'

// 活跃标签
const activeTab = ref<string>('order-change')

/**
 * 标签页与建议类型映射（type对应后端aps_optimization_suggestion.type）
 */
const tabs = [
  { name: 'order-change', label: '订单变更调整', type: 'orderChange' },
  { name: 'equipment-failure', label: '设备故障响应', type: 'equipmentFailure' },
  { name: 'material-shortage', label: '物料短缺处理', type: 'materialShortage' },
  { name: 'bottleneck-optimization', label: '瓶颈工序优化', type: 'bottleneck' }
]

// 全部优化建议数据（真实API）
const suggestions = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: ''
})

// 统计数据：按类型计数
const statistics = computed(() => {
  return {
    orderChanges: countByType('orderChange'),
    equipmentFailures: countByType('equipmentFailure'),
    materialShortages: countByType('materialShortage'),
    bottleneckProcesses: countByType('bottleneck')
  }
})

/**
 * 按类型统计建议数量
 */
const countByType = (type: string) => {
  return suggestions.value.filter((item: any) => item.type === type).length
}

/**
 * 按类型过滤建议，并应用搜索条件（关键词匹配描述/建议，状态精确匹配）
 */
const filteredSuggestions = (type: string) => {
  let result = suggestions.value.filter((item: any) => item.type === type)

  if (searchForm.keyword) {
    const keyword = searchForm.keyword.toLowerCase()
    result = result.filter((item: any) =>
      item.description?.toLowerCase().includes(keyword) ||
      item.suggestion?.toLowerCase().includes(keyword)
    )
  }

  if (searchForm.status) {
    result = result.filter((item: any) => item.status === searchForm.status)
  }

  return result
}

const getResponseMessage = (response: any, fallback: string) => {
  const normalized = DataTransformer.normalizeResponse(response)
  return normalized?.msg || normalized?.message || fallback
}

/**
 * 状态中文映射
 */
const getStatusText = (status: string) => {
  const map: { [key: string]: string } = {
    NEW: '待处理',
    ACCEPTED: '已采纳',
    IGNORED: '已忽略'
  }
  return map[status] || status
}

const getStatusTagType = (status: string) => {
  const map: { [key: string]: string } = {
    NEW: 'warning',
    ACCEPTED: 'success',
    IGNORED: 'info'
  }
  return map[status] || 'info'
}

/**
 * 优先级中文映射
 */
const getPriorityText = (priority: string) => {
  const map: { [key: string]: string } = {
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低'
  }
  return map[priority] || priority
}

const getPriorityTagType = (priority: string) => {
  const map: { [key: string]: string } = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info'
  }
  return map[priority] || 'info'
}

/**
 * 从后端加载全部优化建议（scheduleResultId为空时返回全部）
 */
const fetchData = async () => {
  loading.value = true
  try {
    const response = await ScheduleOptimizationAPI.getOptimizationSuggestions({})
    suggestions.value = unwrapListResponse<any>(response)
  } catch (error: any) {
    console.error('获取数据失败:', error.message || error)
    ElMessage.error(error.message || '获取数据失败')
    suggestions.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 采纳建议：调用后端accept接口并同步本地状态
 */
const handleAccept = async (row: any) => {
  try {
    const response = await ScheduleOptimizationAPI.acceptSuggestion(row.id)
    const responseData = DataTransformer.normalizeResponse(response)

    if (DataTransformer.isSuccessCode(responseData?.code)) {
      const item = suggestions.value.find((s: any) => s.id === row.id)
      if (item) {
        item.status = 'ACCEPTED'
      }
      ElMessage.success('建议已采纳')
    } else {
      ElMessage.error(getResponseMessage(responseData, '操作失败'))
    }
  } catch (error: any) {
    console.error('采纳建议失败:', error.message || error)
    ElMessage.error(error.message || '采纳建议失败')
  }
}

/**
 * 忽略建议：调用后端ignore接口并同步本地状态
 */
const handleIgnore = async (row: any) => {
  try {
    const response = await ScheduleOptimizationAPI.ignoreSuggestion(row.id)
    const responseData = DataTransformer.normalizeResponse(response)

    if (DataTransformer.isSuccessCode(responseData?.code)) {
      const item = suggestions.value.find((s: any) => s.id === row.id)
      if (item) {
        item.status = 'IGNORED'
      }
      ElMessage.success('建议已忽略')
    } else {
      ElMessage.error(getResponseMessage(responseData, '操作失败'))
    }
  } catch (error: any) {
    console.error('忽略建议失败:', error.message || error)
    ElMessage.error(error.message || '忽略建议失败')
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchData()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.dynamic-optimization-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #303133;
}

.module-nav {
  min-height: calc(100vh - 150px);
}

.tab-content {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 1.1rem;
}

/* 响应式设计 */
.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-card :deep(.el-statistic__number) {
  font-size: 24px;
  font-weight: bold;
}

.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 768px) {
  .dynamic-optimization-view {
    padding: 12px;
  }

  .page-header h2 {
    font-size: 1.2rem;
  }

  .module-nav {
    min-height: auto;
  }

  .tab-content {
    padding: 12px;
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
