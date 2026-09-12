<template>
  <div class="optimization-suggestions">
    <!-- 组件标题 -->
    <div class="component-header">
      <h3>调度结果优化建议</h3>
      <div class="header-controls">
        <!-- 建议类型筛选 -->
        <el-select
          v-model="suggestionTypeFilter"
          placeholder="选择建议类型"
          size="small"
          style="margin-right: 10px;"
        >
          <el-option label="全部" value="" />
          <el-option label="资源优化" value="resource" />
          <el-option label="时间优化" value="time" />
          <el-option label="优先级优化" value="priority" />
          <el-option label="约束优化" value="constraint" />
        </el-select>
        
        <!-- 建议状态筛选 -->
        <el-select
          v-model="suggestionStatusFilter"
          placeholder="选择建议状态"
          size="small"
          style="margin-right: 10px;"
        >
          <el-option label="全部" value="" />
          <el-option label="未处理" value="pending" />
          <el-option label="已采纳" value="accepted" />
          <el-option label="已忽略" value="ignored" />
        </el-option>
        
        <!-- 重新分析按钮 -->
        <el-button
          type="primary"
          size="small"
          @click="reanalyzeSchedule"
          :loading="isAnalyzing"
        >
          重新分析
        </el-button>
      </div>
    </div>
    
    <!-- 建议统计信息 -->
    <div class="suggestion-stats">
      <el-statistic title="总建议数" :value="totalSuggestions" />
      <el-statistic title="未处理" :value="pendingSuggestions" />
      <el-statistic title="已采纳" :value="acceptedSuggestions" />
      <el-statistic title="已忽略" :value="ignoredSuggestions" />
      <el-statistic title="潜在收益" :value="potentialBenefit" suffix="%" />
    </div>
    
    <!-- 建议列表 -->
    <div class="suggestion-list">
      <el-collapse v-model="activeNames" accordion>
        <!-- 每个建议项 -->
        <el-collapse-item
          v-for="suggestion in filteredSuggestions"
          :key="suggestion.id"
          :title="getSuggestionTitle(suggestion)"
          :name="suggestion.id.toString()"
        >
          <div class="suggestion-content">
            <!-- 建议详情 -->
            <div class="suggestion-detail">
              <div class="detail-item">
                <span class="label">建议类型：</span>
                <el-tag :type="getSuggestionTypeTagType(suggestion.type)">
                  {{ getSuggestionTypeName(suggestion.type) }}
                </el-tag>
              </div>
              <div class="detail-item">
                <span class="label">优先级：</span>
                <el-tag :type="getPriorityTagType(suggestion.priority)">
                  {{ getPriorityName(suggestion.priority) }}
                </el-tag>
              </div>
              <div class="detail-item">
                <span class="label">影响范围：</span>
                <span>{{ suggestion.impactScope }}</span>
              </div>
              <div class="detail-item">
                <span class="label">潜在收益：</span>
                <span class="benefit-value">{{ suggestion.potentialBenefit }}%</span>
              </div>
              <div class="detail-item">
                <span class="label">状态：</span>
                <el-tag :type="getStatusTagType(suggestion.status)">
                  {{ getStatusName(suggestion.status) }}
                </el-tag>
              </div>
              <div class="detail-item full-width">
                <span class="label">建议描述：</span>
                <p>{{ suggestion.description }}</p>
              </div>
              <div class="detail-item full-width">
                <span class="label">优化方案：</span>
                <p>{{ suggestion.optimizationPlan }}</p>
              </div>
              <div v-if="suggestion.relatedTasks && suggestion.relatedTasks.length > 0" class="detail-item full-width">
                <span class="label">相关任务：</span>
                <div class="related-tasks">
                  <el-tag
                    v-for="taskId in suggestion.relatedTasks"
                    :key="taskId"
                    size="small"
                    type="info"
                    @click="emit('taskSelected', taskId)"
                    style="margin-right: 5px; cursor: pointer;"
                  >
                    任务{{ taskId }}
                  </el-tag>
                </div>
              </div>
              <div v-if="suggestion.relatedResources && suggestion.relatedResources.length > 0" class="detail-item full-width">
                <span class="label">相关资源：</span>
                <div class="related-resources">
                  <el-tag
                    v-for="resourceId in suggestion.relatedResources"
                    :key="resourceId"
                    size="small"
                    type="warning"
                    @click="emit('resourceSelected', resourceId)"
                    style="margin-right: 5px; cursor: pointer;"
                  >
                    资源{{ resourceId }}
                  </el-tag>
                </div>
              </div>
            </div>
            
            <!-- 建议操作 -->
            <div class="suggestion-actions" v-if="suggestion.status === 'pending'">
              <el-button
                type="success"
                size="small"
                @click="acceptSuggestion(suggestion)"
              >
                采纳建议
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="ignoreSuggestion(suggestion)"
                style="margin-left: 10px;"
              >
                忽略建议
              </el-button>
            </div>
            <div class="suggestion-actions" v-else>
              <el-button
                type="info"
                size="small"
                @click="resetSuggestionStatus(suggestion)"
              >
                重置状态
              </el-button>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
      
      <!-- 空状态 -->
      <div v-if="filteredSuggestions.length === 0" class="empty-state">
        <el-empty description="暂无优化建议" />
      </div>
    </div>
    
    <!-- 优化建议报告 -->
    <div class="suggestion-report">
      <el-divider content-position="left">优化建议报告</el-divider>
      <div class="report-content">
        <h4>分析结果概览</h4>
        <p>{{ analysisOverview }}</p>
        
        <h4>优化建议总结</h4>
        <div class="suggestion-summary">
          <div
            v-for="type in suggestionTypeStats"
            :key="type.type"
            class="summary-item"
          >
            <span class="summary-label">{{ getSuggestionTypeName(type.type) }}：</span>
            <span class="summary-value">{{ type.count }} 条建议</span>
          </div>
        </div>
        
        <h4>优化建议执行记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="record in executionRecords"
            :key="record.id"
            :timestamp="record.timestamp"
            :type="record.type === 'accept' ? 'success' : 'warning'"
          >
            {{ record.description }}
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// 优化建议类型定义
interface Suggestion {
  id: string | number
  type: 'resource' | 'time' | 'priority' | 'constraint'
  priority: 'high' | 'medium' | 'low'
  title: string
  description: string
  optimizationPlan: string
  impactScope: string
  potentialBenefit: number
  status: 'pending' | 'accepted' | 'ignored'
  relatedTasks?: (string | number)[]
  relatedResources?: (string | number)[]
  createdAt: string
  updatedAt?: string
}

// 执行记录类型定义
interface ExecutionRecord {
  id: string | number
  type: 'accept' | 'ignore' | 'reanalyze'
  description: string
  timestamp: string
}

// 定义props
const props = defineProps<{
  suggestions: Suggestion[]
  analysisOverview?: string
  executionRecords?: ExecutionRecord[]
}>()

// 定义事件
const emit = defineEmits<{
  (e: 'reanalyze'): void
  (e: 'acceptSuggestion', suggestion: Suggestion): void
  (e: 'ignoreSuggestion', suggestion: Suggestion): void
  (e: 'resetSuggestion', suggestion: Suggestion): void
  (e: 'taskSelected', taskId: string | number): void
  (e: 'resourceSelected', resourceId: string | number): void
}>()

// 状态管理
const isAnalyzing = ref(false)
const activeNames = ref<string[]>([])
const suggestionTypeFilter = ref<string>('')
const suggestionStatusFilter = ref<string>('')

// 计算属性
// 确保suggestions始终是数组
const safeSuggestions = computed(() => {
  return Array.isArray(props.suggestions) ? props.suggestions : []
})

// 筛选后的建议列表
const filteredSuggestions = computed(() => {
  let filtered = [...safeSuggestions.value]
  
  // 类型筛选
  if (suggestionTypeFilter.value) {
    filtered = filtered.filter(s => s.type === suggestionTypeFilter.value)
  }
  
  // 状态筛选
  if (suggestionStatusFilter.value) {
    filtered = filtered.filter(s => s.status === suggestionStatusFilter.value)
  }
  
  // 按优先级排序
  return filtered.sort((a, b) => {
    const priorityOrder = { high: 3, medium: 2, low: 1 }
    return priorityOrder[b.priority] - priorityOrder[a.priority]
  })
})

// 建议统计
const totalSuggestions = computed(() => safeSuggestions.value.length)
const pendingSuggestions = computed(() => 
  safeSuggestions.value.filter(s => s.status === 'pending').length
)
const acceptedSuggestions = computed(() => 
  safeSuggestions.value.filter(s => s.status === 'accepted').length
)
const ignoredSuggestions = computed(() => 
  safeSuggestions.value.filter(s => s.status === 'ignored').length
)

// 潜在收益总和
const potentialBenefit = computed(() => {
  const pendingSuggestions = safeSuggestions.value.filter(s => s.status === 'pending')
  if (pendingSuggestions.length === 0) return 0
  
  const totalBenefit = pendingSuggestions.reduce((sum, s) => sum + s.potentialBenefit, 0)
  return Math.round(totalBenefit / pendingSuggestions.length)
})

// 建议类型统计
const suggestionTypeStats = computed(() => {
  const stats: Array<{ type: string; count: number }> = []
  const types = ['resource', 'time', 'priority', 'constraint'] as const
  
  types.forEach(type => {
    const count = safeSuggestions.value.filter(s => s.type === type).length
    stats.push({ type, count })
  })
  
  return stats
})

// 获取建议类型名称
const getSuggestionTypeName = (type: string) => {
  const typeMap: Record<string, string> = {
    resource: '资源优化',
    time: '时间优化',
    priority: '优先级优化',
    constraint: '约束优化'
  }
  return typeMap[type] || '未知类型'
}

// 获取建议类型标签类型
const getSuggestionTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    resource: 'success',
    time: 'primary',
    priority: 'warning',
    constraint: 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取优先级名称
const getPriorityName = (priority: string) => {
  const priorityMap: Record<string, string> = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return priorityMap[priority] || '未知优先级'
}

// 获取优先级标签类型
const getPriorityTagType = (priority: string) => {
  const priorityMap: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'success'
  }
  return priorityMap[priority] || 'info'
}

// 获取状态名称
const getStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: '未处理',
    accepted: '已采纳',
    ignored: '已忽略'
  }
  return statusMap[status] || '未知状态'
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    pending: 'info',
    accepted: 'success',
    ignored: 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取建议标题
const getSuggestionTitle = (suggestion: Suggestion) => {
  return `${suggestion.title} (${getPriorityName(suggestion.priority)}优先级)`
}

// 重新分析调度结果
const reanalyzeSchedule = () => {
  isAnalyzing.value = true
  emit('reanalyze')
  // 模拟分析过程
  setTimeout(() => {
    isAnalyzing.value = false
  }, 1000)
}

// 采纳建议
const acceptSuggestion = (suggestion: Suggestion) => {
  emit('acceptSuggestion', suggestion)
}

// 忽略建议
const ignoreSuggestion = (suggestion: Suggestion) => {
  emit('ignoreSuggestion', suggestion)
}

// 重置建议状态
const resetSuggestionStatus = (suggestion: Suggestion) => {
  emit('resetSuggestion', suggestion)
}
</script>

<style scoped>
.optimization-suggestions {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
}

.component-header {
  padding: 15px;
  border-bottom: 1px solid #e4e7ed;
  background-color: #fafafa;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.component-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.suggestion-stats {
  display: flex;
  justify-content: space-around;
  padding: 15px;
  background-color: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.suggestion-stats :deep(.el-statistic) {
  text-align: center;
  flex: 1;
}

.suggestion-stats :deep(.el-statistic__title) {
  font-size: 12px;
  color: #909399;
}

.suggestion-stats :deep(.el-statistic__value) {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.suggestion-list {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
}

.empty-state {
  margin: 50px 0;
}

.suggestion-content {
  padding: 10px 0;
}

.suggestion-detail {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item.full-width {
  width: 100%;
  flex-direction: column;
  align-items: flex-start;
}

.detail-item .label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
}

.benefit-value {
  color: #67c23a;
  font-weight: 600;
}

.suggestion-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}

.related-tasks,
.related-resources {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 5px;
}

.related-tasks :deep(.el-tag),
.related-resources :deep(.el-tag) {
  cursor: pointer;
  user-select: none;
}

.related-tasks :deep(.el-tag):hover,
.related-resources :deep(.el-tag):hover {
  opacity: 0.8;
}

.suggestion-report {
  padding: 15px;
  border-top: 1px solid #e4e7ed;
  background-color: #fafafa;
}

.report-content h4 {
  margin: 15px 0 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.report-content p {
  margin: 0 0 15px 0;
  color: #606266;
  line-height: 1.6;
}

.suggestion-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  margin-bottom: 15px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-label {
  font-weight: 600;
  color: #606266;
}

.summary-value {
  color: #303133;
  font-weight: 600;
}

:deep(.el-collapse-item__header) {
  font-weight: 600;
  color: #303133;
}

@media (max-width: 768px) {
  .component-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-controls {
    width: 100%;
  }
  
  .suggestion-stats {
    flex-direction: column;
    gap: 10px;
  }
  
  .suggestion-stats :deep(.el-statistic) {
    text-align: left;
  }
  
  .suggestion-detail {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .detail-item {
    width: 100%;
  }
}
</style>