<template>
  <div class="decision-center">
    <el-tabs v-model="activeTab" class="main-tabs">
      <el-tab-pane label="建议中心" name="suggestions">
    <!-- 扫描触发区 -->
    <div class="scan-panel">
      <div class="scan-title">
        <el-icon><Aim /></el-icon>
        <span>决策引擎手动扫描</span>
      </div>
      <div class="scan-buttons">
        <el-button size="small" :loading="scanning === 'fault'" @click="handleScan('fault')">故障预测</el-button>
        <el-button size="small" :loading="scanning === 'delivery'" @click="handleScan('delivery')">交期风险</el-button>
        <el-button size="small" :loading="scanning === 'energy'" @click="handleScan('energy')">能耗负荷</el-button>
        <el-button size="small" :loading="scanning === 'preaudit'" @click="handleScan('preaudit')">审批预审</el-button>
        <el-button size="small" :loading="scanning === 'safety'" @click="handleScan('safety')">安全库存</el-button>
        <el-button size="small" :loading="scanning === 'shortage'" @click="handleScan('shortage')">缺货闭环</el-button>
        <el-button size="small" :loading="scanning === 'sentinel'" @click="handleScan('sentinel')">数据哨兵</el-button>
        <el-button size="small" type="primary" :loading="scanning === 'briefing'" @click="handleScan('briefing')">晨会巡检</el-button>
      </div>
    </div>

    <!-- 采纳率统计 -->
    <div class="stats-panel" v-if="adoptionStats.length > 0">
      <div v-for="item in adoptionStats" :key="item.type" class="adoption-card">
        <div class="adoption-type">{{ typeLabel(item.type) }}</div>
        <el-progress
          :percentage="Math.round(item.acceptRate)"
          :color="rateColor(item.acceptRate)"
          :stroke-width="8"
        />
        <div class="adoption-detail">
          采纳 {{ item.accepted }} / 拒绝 {{ item.rejected }} / 待办 {{ item.pending }}
        </div>
      </div>
    </div>

    <!-- 过滤栏 -->
    <div class="filter-bar">
      <el-radio-group v-model="filterStatus" @change="loadList">
        <el-radio-button value="PENDING">待处理</el-radio-button>
        <el-radio-button value="ACCEPTED">已采纳</el-radio-button>
        <el-radio-button value="REJECTED">已拒绝</el-radio-button>
        <el-radio-button value="EXECUTED">已执行</el-radio-button>
      </el-radio-group>
      <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 160px" @change="loadList">
        <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
      </el-select>
      <el-button :icon="Refresh" circle @click="loadList" />
    </div>

    <!-- 建议列表 -->
    <div v-loading="loading" class="suggestion-list">
      <el-empty v-if="suggestions.length === 0" description="当前无建议，AI 决策引擎持续巡检中" />
      <div v-for="item in suggestions" :key="item.id" class="suggestion-card" :class="item.severity.toLowerCase()">
        <div class="card-main">
          <div class="card-head">
            <el-tag size="small" effect="plain">{{ typeLabel(item.suggestionType) }}</el-tag>
            <el-tag size="small" :type="severityTag(item.severity)">{{ severityLabel(item.severity) }}</el-tag>
            <el-tag size="small" :type="item.automationLevel === 'CONFIRM' ? 'warning' : 'success'" effect="dark">
              {{ item.automationLevel }}
            </el-tag>
            <span class="module-badge">{{ item.module }}</span>
            <span class="time">{{ formatTime(item.createdTime) }}</span>
          </div>
          <div class="card-title">{{ item.title }}</div>

          <!-- 证据链展开 -->
          <el-collapse class="evidence-collapse">
            <el-collapse-item title="AI 分析过程与证据链" name="analysis">
              <div v-if="parsedAnalysis(item)" class="analysis-block">
                <div v-if="parsedAnalysis(item)?.algorithm" class="analysis-row">
                  <span class="label">算法：</span>{{ parsedAnalysis(item).algorithm }}
                </div>
                <div v-if="parsedAnalysis(item)?.evidence?.sources" class="analysis-row">
                  <span class="label">数据源：</span>
                  <div class="source-list">
                    <template v-for="(s, si) in parsedAnalysis(item).evidence.sources" :key="si">
                      <!-- P4-5 结构化证据源：可点击下钻 -->
                      <div v-if="isStructuredSource(s)" class="source-item" :class="{ clickable: s.drillUrl }" @click="drillEvidence(s)">
                        <el-tag size="small" type="primary" effect="plain" class="source-tag">{{ s.db }}.{{ s.table }}</el-tag>
                        <span v-if="s.filter" class="source-filter">{{ s.filter }}</span>
                        <el-tag v-if="s.rowCount != null" size="small" type="info" effect="plain">{{ s.rowCount }}行</el-tag>
                        <el-icon v-if="s.drillUrl" class="drill-icon" title="点击下钻到原始数据"><TopRight /></el-icon>
                      </div>
                      <!-- 旧格式：表名字符串 -->
                      <el-tag v-else size="small" class="source-tag">{{ s }}</el-tag>
                    </template>
                  </div>
                </div>
                <div v-if="parsedAnalysis(item)?.evidence?.steps?.length" class="analysis-row">
                  <span class="label">推理步骤：</span>
                  <ol class="steps-list">
                    <li v-for="(step, si) in parsedAnalysis(item).evidence.steps" :key="si">{{ step }}</li>
                  </ol>
                </div>
                <div v-if="parsedAnalysis(item)?.evidence?.queryTime" class="analysis-row">
                  <span class="label">取证时间：</span>{{ parsedAnalysis(item).evidence.queryTime }}
                </div>
                <div v-if="parsedAnalysis(item)?.evidence?.metrics" class="analysis-row">
                  <span class="label">关键指标：</span>
                  <pre class="metrics-pre">{{ JSON.stringify(parsedAnalysis(item).evidence.metrics, null, 2) }}</pre>
                </div>
                <!-- P4-6 多专家会诊结论 -->
                <div v-if="parsedAnalysis(item)?.crossDomain?.length" class="analysis-row">
                  <span class="label">跨域会诊：</span>
                  <div class="cross-domain-list">
                    <div v-for="(f, fi) in parsedAnalysis(item).crossDomain" :key="fi" class="cross-domain-item">
                      <el-tag size="small" :type="crossStatusTag(f.status)" effect="plain">{{ domainLabel(f.domain) }}</el-tag>
                      <span class="cross-summary">{{ f.summary }}</span>
                    </div>
                  </div>
                </div>
                <div v-if="parsedAnalysis(item)?.crossDomainRisk" class="analysis-row combo-risk">
                  <el-icon><WarningFilled /></el-icon>
                  <span>{{ parsedAnalysis(item).crossDomainRisk }}</span>
                </div>
                <div v-if="!parsedAnalysis(item)?.algorithm && !parsedAnalysis(item)?.evidence" class="analysis-row raw">
                  <pre class="metrics-pre">{{ item.analysis }}</pre>
                </div>
              </div>
              <el-text v-else type="info">无分析详情</el-text>
            </el-collapse-item>
          </el-collapse>

          <!-- CONFIRM 级动作草稿 -->
          <div v-if="item.automationLevel === 'CONFIRM' && parsedAction(item)" class="action-draft">
            <div class="draft-title">
              <el-icon><EditPen /></el-icon> 动作草稿（采纳后经现有端点执行）
            </div>
            <div class="draft-body">
              <el-tag size="small" type="warning">{{ parsedAction(item).method }}</el-tag>
              <code class="draft-endpoint">{{ parsedAction(item).endpoint }}</code>
              <div class="draft-desc">{{ parsedAction(item).description }}</div>
              <pre v-if="parsedAction(item).payload" class="metrics-pre">{{ JSON.stringify(parsedAction(item).payload, null, 2) }}</pre>
            </div>
          </div>

          <!-- 人工反馈展示 -->
          <div v-if="item.feedback" class="feedback-line">
            <el-icon><ChatDotRound /></el-icon> 处置说明：{{ item.feedback }}
          </div>
        </div>

        <!-- 操作按钮 -->
        <div v-if="item.status === 'PENDING'" class="card-actions">
          <el-button
            v-if="item.automationLevel === 'CONFIRM' && parsedAction(item)"
            type="primary"
            size="small"
            :loading="executing === String(item.id)"
            @click="handleExecute(item)"
          >
            <el-icon><VideoPlay /></el-icon> 执行
          </el-button>
          <!-- P4-6 次要动作（多专家联动补充动作） -->
          <el-button
            v-for="(sa, si) in secondaryActionsOf(item)"
            :key="'sa-' + item.id + '-' + si"
            type="warning"
            size="small"
            plain
            :loading="executing === item.id + '-' + si"
            :title="sa.description"
            @click="handleExecute(item, si)"
          >
            <el-icon><VideoPlay /></el-icon> {{ domainLabel(sa.domain) }}联动
          </el-button>
          <el-button type="success" size="small" @click="handleFeedback(item, 'ACCEPTED')">
            <el-icon><Check /></el-icon> 采纳
          </el-button>
          <el-button type="warning" size="small" plain @click="openModifyDialog(item)">
            <el-icon><EditPen /></el-icon> 修改采纳
          </el-button>
          <el-button type="danger" size="small" plain @click="openRejectDialog(item)">
            <el-icon><Close /></el-icon> 拒绝
          </el-button>
        </div>
        <div v-else class="card-status">
          <el-tag :type="statusTag(item.status)" effect="dark">{{ statusLabel(item.status) }}</el-tag>
          <span v-if="item.resolvedTime" class="time">{{ formatTime(item.resolvedTime) }}</span>
        </div>
      </div>
    </div>
      </el-tab-pane>

      <!-- 技能健康看板（P4-8 决策可观测性） -->
      <el-tab-pane label="技能健康" name="health">
        <div v-loading="healthLoading" class="health-panel">
          <div class="health-header">
            <span class="health-tip">各技能近 24h / 7d 执行统计（连续 3 次失败自动标红），数据来自 ai_decision_log 决策日志</span>
            <el-button :icon="Refresh" circle @click="loadHealth" />
          </div>
          <el-empty v-if="skillHealth.length === 0" description="暂无执行记录，技能将于下个调度周期开始留痕" />
          <el-table v-else :data="skillHealth" style="width: 100%">
            <el-table-column label="技能" min-width="170">
              <template #default="{ row }">
                <div class="skill-name">
                  <el-tag :type="row.health === 'RED' ? 'danger' : 'success'" size="small" effect="dark">
                    {{ row.health === 'RED' ? '异常' : '健康' }}
                  </el-tag>
                  <span>{{ skillName(row.skillCode) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="24h执行" width="90" align="center" prop="runs24h" />
            <el-table-column label="24h成功率" width="100" align="center">
              <template #default="{ row }">{{ row.successRate24h }}%</template>
            </el-table-column>
            <el-table-column label="24h平均耗时" width="110" align="center">
              <template #default="{ row }">{{ row.avgDurationMs24h }}ms</template>
            </el-table-column>
            <el-table-column label="24h产出" width="90" align="center" prop="produced24h" />
            <el-table-column label="7d执行" width="90" align="center" prop="runs7d" />
            <el-table-column label="7d成功率" width="100" align="center">
              <template #default="{ row }">{{ row.successRate7d }}%</template>
            </el-table-column>
            <el-table-column label="7d产出" width="90" align="center" prop="produced7d" />
            <el-table-column label="连续失败" width="90" align="center">
              <template #default="{ row }">
                <el-text v-if="row.consecutiveErrors > 0" type="danger">{{ row.consecutiveErrors }}</el-text>
                <el-text v-else type="success">0</el-text>
              </template>
            </el-table-column>
            <el-table-column label="最近执行" width="160" align="center">
              <template #default="{ row }">{{ row.lastRunTime || '-' }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 拒绝/修改对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogAction === 'REJECTED' ? '拒绝建议' : '修改后采纳'" width="480px">
      <el-input
        v-model="dialogFeedback"
        type="textarea"
        :rows="4"
        :placeholder="dialogAction === 'REJECTED' ? '请说明拒绝原因（将沉淀至学习环）' : '请说明修改内容（将沉淀至学习环）'"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :type="dialogAction === 'REJECTED' ? 'danger' : 'warning'" @click="submitDialog">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Aim, Refresh, Check, Close, EditPen, ChatDotRound, VideoPlay, TopRight, WarningFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'
import {
  getSuggestions, feedbackSuggestion, getAdoptionStats,
  runFaultPrediction, runDeliveryRisk, runEnergyLoad, runPreauditScan,
  runSafetyStock, runStockShortage, runDqSentinel, generateBriefing,
  executeSuggestion, getSkillHealth,
  type AiSuggestion, type SkillHealthRow
} from '@/api/ai'

// 列表与过滤状态
const loading = ref(false)
const suggestions = ref<AiSuggestion[]>([])
const filterStatus = ref('PENDING')
const filterType = ref('')
const adoptionStats = ref<any[]>([])
const scanning = ref('')
const activeTab = ref('suggestions')
const executing = ref<string | null>(null)

// 技能健康看板状态
const healthLoading = ref(false)
const skillHealth = ref<SkillHealthRow[]>([])

// 对话框状态
const dialogVisible = ref(false)
const dialogAction = ref('REJECTED')
const dialogFeedback = ref('')
const dialogTarget = ref<AiSuggestion | null>(null)

/** 建议类型选项 */
const typeOptions = [
  { value: 'MORNING_BRIEFING', label: '晨会简报' },
  { value: 'FAULT_PREDICTION', label: '故障预测' },
  { value: 'DELIVERY_RISK', label: '交期风险' },
  { value: 'ENERGY_LOAD', label: '能耗负荷' },
  { value: 'PREAUDIT', label: '审批预审' },
  { value: 'STOCK_SHORTAGE', label: '备件缺货' },
  { value: 'SAFETY_STOCK', label: '安全库存' },
  { value: 'DQ_SENTINEL', label: '数据哨兵' },
  { value: 'EIGHT_D', label: '8D报告' },
  { value: 'ROOT_CAUSE', label: '根因分析' },
  { value: 'EVENT_TRIGGER', label: '事件触发' }
]

/** 类型中文标签 */
const typeLabel = (type: string) => typeOptions.find(t => t.value === type)?.label || type

/** 技能编码中文名（健康看板用） */
const skillName = (code: string) => typeOptions.find(t => t.value === code)?.label || code

/** 严重级别标签 */
const severityLabel = (s: string) => ({ INFO: '提示', WARNING: '警告', CRITICAL: '严重' } as Record<string, string>)[s] || s
const severityTag = (s: string) => (({ INFO: 'info', WARNING: 'warning', CRITICAL: 'danger' } as Record<string, string>)[s] || 'info') as any

/** 状态标签 */
const statusLabel = (s: string) => ({ PENDING: '待处理', ACCEPTED: '已采纳', REJECTED: '已拒绝', MODIFIED: '修改采纳', EXECUTED: '已执行' } as Record<string, string>)[s] || s
const statusTag = (s: string) => (({ ACCEPTED: 'success', REJECTED: 'danger', MODIFIED: 'warning', EXECUTED: 'primary' } as Record<string, string>)[s] || 'info') as any

/** 采纳率颜色 */
const rateColor = (rate: number) => rate >= 70 ? '#67C23A' : rate >= 40 ? '#E6A23C' : '#F56C6C'

/** 格式化时间 */
const formatTime = (time: string | null) => time ? time.replace('T', ' ').substring(0, 19) : '-'

/** 解析 analysis JSON 字段 */
const parsedAnalysis = (item: AiSuggestion) => {
  if (!item.analysis) return null
  try { return JSON.parse(item.analysis) } catch { return { raw: item.analysis } }
}

/** 解析 actionDraft JSON 字段 */
const parsedAction = (item: AiSuggestion) => {
  if (!item.actionDraft) return null
  try { return JSON.parse(item.actionDraft) } catch { return null }
}

/** P4-6：解析次要动作列表（多专家联动补充动作，位于 analysis.secondaryActions） */
const secondaryActionsOf = (item: AiSuggestion): any[] => {
  const a = parsedAnalysis(item)
  return Array.isArray(a?.secondaryActions) ? a.secondaryActions : []
}

/** P4-6：领域中文标签 */
const domainLabel = (d: string) => ({ EQUIPMENT: '设备', INVENTORY: '库存', PROCUREMENT: '采购', PRODUCTION: '生产' } as Record<string, string>)[d] || d

/** P4-6：会诊结论状态标签类型 */
const crossStatusTag = (s: string) => (({ OK: 'success', WARNING: 'warning', CRITICAL: 'danger', UNKNOWN: 'info' } as Record<string, string>)[s] || 'info') as any

// P4-5 结构化证据链：下钻路由
const router = useRouter()

/** 判断证据源是否为结构化对象（新格式），字符串为旧格式 */
const isStructuredSource = (s: any) => s && typeof s === 'object' && s.table

/** 点击证据源下钻到原始数据页面（drillUrl 为前端路由，可带查询参数） */
const drillEvidence = (s: any) => {
  if (!s?.drillUrl) return
  router.push(s.drillUrl)
}

/** 加载建议列表 */
const loadList = async () => {
  loading.value = true
  try {
    const resp = filterType.value
      ? await getSuggestions(undefined, filterType.value)
      : await getSuggestions(filterStatus.value)
    let list = DataTransformer.unwrapList<AiSuggestion>(resp)
    // 按类型过滤时前端再过滤状态（后端类型查询不带状态）
    if (filterType.value && filterStatus.value) {
      list = list.filter(i => i.status === filterStatus.value)
    }
    suggestions.value = list
  } catch (e) {
    ElMessage.error('建议列表加载失败')
  } finally {
    loading.value = false
  }
}

/** 加载采纳率统计 */
const loadStats = async () => {
  try {
    const resp = await getAdoptionStats()
    const data = DataTransformer.unwrapData<any>(resp)
    adoptionStats.value = data?.byType || []
  } catch (e) {
    // 统计失败不阻塞主流程
  }
}

/** 触发决策引擎扫描 */
const handleScan = async (kind: string) => {
  scanning.value = kind
  try {
    const fnMap: Record<string, () => Promise<any>> = {
      fault: runFaultPrediction,
      delivery: runDeliveryRisk,
      energy: runEnergyLoad,
      preaudit: runPreauditScan,
      safety: runSafetyStock,
      shortage: runStockShortage,
      sentinel: runDqSentinel,
      briefing: generateBriefing
    }
    const scanFn = fnMap[kind]
    if (!scanFn) return
    const resp = await scanFn()
    const data = DataTransformer.unwrapData<any>(resp)
    const count = Array.isArray(data) ? data.length : (typeof data === 'number' ? data : (data ? 1 : 0))
    ElMessage.success(`扫描完成，新增 ${count} 条建议（幂等去重）`)
    loadList()
    loadStats()
  } catch (e) {
    ElMessage.error('扫描执行失败')
  } finally {
    scanning.value = ''
  }
}

/** 快捷采纳 */
const handleFeedback = async (item: AiSuggestion, action: string) => {
  try {
    await feedbackSuggestion(item.id, action)
    ElMessage.success('已采纳该建议')
    loadList()
    loadStats()
  } catch (e) {
    ElMessage.error('处置失败，请重试')
  }
}

/**
 * 一键执行建议动作（P4-1）
 * 弹确认框（confirmText+rollbackHint）→ 调执行端点 → 成功展示业务单号，失败原因回写卡片允许重试。
 * actionIndex（P4-6）：非空时执行次要动作（多专家联动补充动作），成功后建议保持待处理，主动作仍可执行
 */
const handleExecute = async (item: AiSuggestion, actionIndex?: number) => {
  const action = actionIndex == null ? parsedAction(item) : secondaryActionsOf(item)[actionIndex]
  const confirmText = action?.confirmText || action?.description || '确认执行该建议动作？'
  const rollbackHint = action?.rollbackHint
  try {
    await ElMessageBox.confirm(
      rollbackHint ? `${confirmText}\n${rollbackHint}` : confirmText,
      '执行确认',
      { confirmButtonText: '确认执行', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return // 用户取消
  }
  executing.value = actionIndex == null ? String(item.id) : `${item.id}-${actionIndex}`
  try {
    const resp = await executeSuggestion(item.id, actionIndex)
    const data = DataTransformer.unwrapData<any>(resp)
    ElMessage.success(data?.message || '执行成功')
  } catch (e: any) {
    // 失败原因已由后端回写 feedback，刷新列表后卡片可见，允许重试
    ElMessage.error('执行失败，原因已回写建议卡片')
  } finally {
    executing.value = null
    loadList()
    loadStats()
  }
}

/** 加载技能健康看板（P4-8） */
const loadHealth = async () => {
  healthLoading.value = true
  try {
    const resp = await getSkillHealth()
    const data = DataTransformer.unwrapData<any>(resp)
    skillHealth.value = data?.skills || []
  } catch (e) {
    // 健康数据加载失败不阻塞建议主流程
  } finally {
    healthLoading.value = false
  }
}

/** 打开拒绝对话框 */
const openRejectDialog = (item: AiSuggestion) => {
  dialogTarget.value = item
  dialogAction.value = 'REJECTED'
  dialogFeedback.value = ''
  dialogVisible.value = true
}

/** 打开修改采纳对话框 */
const openModifyDialog = (item: AiSuggestion) => {
  dialogTarget.value = item
  dialogAction.value = 'MODIFIED'
  dialogFeedback.value = ''
  dialogVisible.value = true
}

/** 提交对话框处置 */
const submitDialog = async () => {
  if (!dialogTarget.value) return
  if (!dialogFeedback.value.trim()) {
    ElMessage.warning('请填写处置说明')
    return
  }
  try {
    await feedbackSuggestion(dialogTarget.value.id, dialogAction.value, dialogFeedback.value.trim())
    ElMessage.success(dialogAction.value === 'REJECTED' ? '已拒绝该建议' : '已修改采纳')
    dialogVisible.value = false
    loadList()
    loadStats()
  } catch (e) {
    ElMessage.error('处置失败，请重试')
  }
}

onMounted(() => {
  loadList()
  loadStats()
  loadHealth()
})
</script>

<style scoped lang="scss">
.decision-center {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.scan-panel {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .scan-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    white-space: nowrap;

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .scan-buttons {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.stats-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;

  .adoption-card {
    background: var(--el-bg-color);
    border-radius: 12px;
    padding: 16px 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    .adoption-type {
      font-weight: 600;
      margin-bottom: 10px;
      color: var(--el-text-color-primary);
    }

    .adoption-detail {
      margin-top: 8px;
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-height: 200px;
}

.suggestion-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  gap: 16px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border-left: 4px solid var(--el-color-info);

  &.warning { border-left-color: #E6A23C; }
  &.critical { border-left-color: #F56C6C; }
  &.info { border-left-color: #409EFF; }

  .card-main {
    flex: 1;
    min-width: 0;
  }

  .card-head {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 10px;

    .module-badge {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      background: var(--el-fill-color-light);
      padding: 2px 8px;
      border-radius: 4px;
    }

    .time {
      margin-left: auto;
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }
  }

  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    line-height: 1.5;
    margin-bottom: 10px;
  }

  .evidence-collapse {
    border: none;

    :deep(.el-collapse-item__header) {
      font-size: 13px;
      color: var(--el-color-primary);
      border: none;
      height: 32px;
    }

    :deep(.el-collapse-item__wrap) {
      border: none;
    }

    .analysis-block {
      background: var(--el-fill-color-lighter);
      border-radius: 8px;
      padding: 12px 14px;

      .analysis-row {
        font-size: 13px;
        margin-bottom: 8px;
        color: var(--el-text-color-regular);

        .label {
          font-weight: 600;
          color: var(--el-text-color-primary);
        }

        .source-tag {
          margin-right: 6px;
        }

        // P4-5 结构化证据源样式
        .source-list {
          display: inline-flex;
          flex-wrap: wrap;
          gap: 6px;
          vertical-align: middle;

          .source-item {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 2px 6px;
            border-radius: 6px;
            background: var(--el-bg-color);
            border: 1px solid var(--el-border-color-lighter);
            font-size: 12px;

            &.clickable {
              cursor: pointer;
              transition: all 0.2s;

              &:hover {
                border-color: var(--el-color-primary);
                box-shadow: 0 0 4px rgba(64, 158, 255, 0.3);

                .drill-icon {
                  color: var(--el-color-primary);
                }
              }
            }

            .source-filter {
              color: var(--el-text-color-secondary);
              font-family: monospace;
            }

            .drill-icon {
              color: var(--el-text-color-placeholder);
              font-size: 14px;
            }
          }
        }

        .steps-list {
          margin: 4px 0 0;
          padding-left: 20px;
          font-size: 12px;
          color: var(--el-text-color-regular);

          li {
            margin-bottom: 2px;
          }
        }

        .metrics-pre {
          margin: 6px 0 0;
          font-size: 12px;
          background: var(--el-bg-color);
          padding: 8px 10px;
          border-radius: 6px;
          overflow-x: auto;
          white-space: pre-wrap;
          word-break: break-all;
        }

        /* P4-6 跨域会诊结论 */
        .cross-domain-list {
          display: flex;
          flex-direction: column;
          gap: 6px;
          margin-top: 4px;

          .cross-domain-item {
            display: flex;
            align-items: flex-start;
            gap: 8px;
            font-size: 12px;
            color: var(--el-text-color-regular);

            .cross-summary {
              line-height: 20px;
            }
          }
        }

        /* P4-6 组合风险高亮 */
        &.combo-risk {
          display: flex;
          align-items: center;
          gap: 6px;
          margin-top: 8px;
          padding: 8px 10px;
          border-radius: 6px;
          background: rgba(245, 108, 108, 0.08);
          border: 1px solid rgba(245, 108, 108, 0.35);
          color: var(--el-color-danger);
          font-size: 12px;
          font-weight: 600;
        }
      }
    }
  }

  .action-draft {
    margin-top: 10px;
    background: rgba(230, 162, 60, 0.06);
    border: 1px dashed rgba(230, 162, 60, 0.4);
    border-radius: 8px;
    padding: 12px 14px;

    .draft-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      font-weight: 600;
      color: #E6A23C;
      margin-bottom: 8px;
    }

    .draft-body {
      font-size: 13px;

      .draft-endpoint {
        margin-left: 8px;
        background: var(--el-bg-color);
        padding: 2px 8px;
        border-radius: 4px;
        font-size: 12px;
      }

      .draft-desc {
        margin-top: 6px;
        color: var(--el-text-color-secondary);
      }

      .metrics-pre {
        margin-top: 8px;
        font-size: 12px;
        background: var(--el-bg-color);
        padding: 8px 10px;
        border-radius: 6px;
        overflow-x: auto;
      }
    }
  }

  .feedback-line {
    margin-top: 10px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .card-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    justify-content: center;

    .el-button {
      margin-left: 0;
    }
  }

  .card-status {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    justify-content: center;
    gap: 6px;

    .time {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
    }
  }
}

.main-tabs {
  :deep(.el-tabs__item) {
    font-size: 15px;
  }
}

.health-panel {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  min-height: 200px;

  .health-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 14px;

    .health-tip {
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }

  .skill-name {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}
</style>
