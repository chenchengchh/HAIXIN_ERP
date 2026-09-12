<template>
  <div class="ai-view">
    <!-- 子路由视图 -->
    <div v-if="isChildRoute" class="child-router-view">
      <div class="page-header">
        <el-page-header @back="goBack">
          <template #content>
            <span class="text-large font-600 mr-3">功能详情</span>
          </template>
        </el-page-header>
      </div>
      <router-view v-slot="{ Component }">
        <transition name="route-transition" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>

    <!-- 主视图：AI 决策中心 -->
    <div v-else class="dashboard-container">
      <div class="dashboard-header">
        <div class="header-title">
          <h2>AI 决策中心</h2>
          <p class="subtitle">主动发现 · 闭环执行 · 持续学习 · 证据可溯</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Sunny" :loading="briefingLoading" @click="handleGenerateBriefing">生成晨会简报</el-button>
          <el-button :icon="Refresh" @click="refreshData">刷新数据</el-button>
        </div>
      </div>

      <!-- 统计概览 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon pending">
            <el-icon><BellFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待处理建议</div>
            <div class="stat-value">{{ stats.pending }}</div>
            <div class="stat-sub">等待人工确认</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon total">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">累计建议</div>
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-sub">全部决策引擎产出</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon rate">
            <el-icon><Aim /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">建议采纳率</div>
            <div class="stat-value">{{ stats.acceptRate }}<span class="unit">%</span></div>
            <div class="stat-sub">S13 反馈学习环</div>
          </div>
        </div>

        <div class="stat-item">
          <div class="stat-icon capability">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">在线技能</div>
            <div class="stat-value">{{ stats.onlineCaps }}<span class="total">/{{ stats.totalCaps }}</span></div>
            <div class="stat-sub">当前阶段 {{ brainPhase }}</div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 决策建议中心 -->
        <div class="module-card" @click="navigateToModule('decision-center')">
          <div class="card-icon decision">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="card-info">
            <h3>决策建议中心</h3>
            <p>全部 AI 建议的统一处置入口</p>
            <div class="features-list">
              <span><el-icon><BellFilled /></el-icon> 待办建议</span>
              <span><el-icon><Check /></el-icon> 采纳/拒绝</span>
              <span><el-icon><DataAnalysis /></el-icon> 采纳率分析</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 晨会简报 -->
        <div class="module-card" @click="navigateToModule('morning-briefing')">
          <div class="card-icon briefing">
            <el-icon><Sunny /></el-icon>
          </div>
          <div class="card-info">
            <h3>AI 晨会简报</h3>
            <p>六域巡检每日经营异常推送</p>
            <div class="features-list">
              <span><el-icon><Odometer /></el-icon> 生产/设备</span>
              <span><el-icon><Van /></el-icon> 采购/物流</span>
              <span><el-icon><Warning /></el-icon> 质量/能源</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 根因会诊 -->
        <div class="module-card" @click="navigateToModule('root-cause')">
          <div class="card-icon rca">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="card-info">
            <h3>跨域根因会诊</h3>
            <p>沿本体链路追溯系统性失效</p>
            <div class="features-list">
              <span><el-icon><Share /></el-icon> 因果链路</span>
              <span><el-icon><Search /></el-icon> 证据追溯</span>
              <span><el-icon><Document /></el-icon> 改进建议</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 能力矩阵 -->
        <div class="module-card" @click="navigateToModule('capabilities')">
          <div class="card-icon capability-card">
            <el-icon><MagicStick /></el-icon>
          </div>
          <div class="card-info">
            <h3>能力矩阵</h3>
            <p>自动化刻度盘与技能在线状态</p>
            <div class="features-list">
              <span><el-icon><Operation /></el-icon> 自动化分级</span>
              <span><el-icon><Cpu /></el-icon> 技能清单</span>
              <span><el-icon><Timer /></el-icon> 期次规划</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 最新待办建议预览 -->
      <div class="dashboard-section mt-4">
        <div class="section-header">
          <h3><el-icon><BellFilled /></el-icon> 最新待办建议</h3>
          <el-button text type="primary" @click="navigateToModule('decision-center')">查看全部</el-button>
        </div>
        <el-table v-loading="loading" :data="latestSuggestions" class="suggestion-table">
          <el-table-column prop="title" label="建议内容" min-width="380" show-overflow-tooltip />
          <el-table-column prop="suggestionType" label="类型" width="150">
            <template #default="{ row }">
              <el-tag size="small">{{ typeLabel(row.suggestionType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="module" label="模块" width="90" />
          <el-table-column prop="severity" label="级别" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="severityTag(row.severity)">{{ severityLabel(row.severity) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="automationLevel" label="自动化" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="row.automationLevel === 'CONFIRM' ? 'warning' : 'success'">{{ row.automationLevel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdTime" label="生成时间" width="170">
            <template #default="{ row }">{{ formatTime(row.createdTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="success" @click.stop="handleFeedback(row, 'ACCEPTED')">采纳</el-button>
              <el-button size="small" type="danger" plain @click.stop="handleFeedback(row, 'REJECTED')">拒绝</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无待处理建议，AI 将持续巡检" />
          </template>
        </el-table>
      </div>

      <!-- 自动化刻度盘 -->
      <div class="dashboard-section mt-4">
        <div class="section-header">
          <h3><el-icon><Operation /></el-icon> 自动化刻度盘</h3>
        </div>
        <div class="dial-cards">
          <div v-for="(desc, level) in automationDial" :key="level" class="dial-card" :class="String(level).toLowerCase()">
            <div class="dial-level">{{ level }}</div>
            <div class="dial-desc">{{ desc }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowRight, Cpu, DataLine, Aim, Monitor, MagicStick, Operation, Timer,
  Connection, Share, Search, Warning, Document, DataAnalysis, Sunny, Van,
  Refresh, Check, BellFilled, Odometer
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'
import {
  getSuggestions, feedbackSuggestion, getAdoptionStats, getBrainStatus, generateBriefing,
  type AiSuggestion
} from '@/api/ai'

// 路由实例
const router = useRouter()
const route = useRoute()

// 判断是否是子路由
const isChildRoute = computed(() => {
  return route.path !== '/home/ai' && route.path.startsWith('/home/ai/')
})

// 页面状态
const loading = ref(false)
const briefingLoading = ref(false)
const latestSuggestions = ref<AiSuggestion[]>([])
const automationDial = ref<Record<string, string>>({})
const brainPhase = ref('P0')
const stats = ref({ pending: 0, total: 0, acceptRate: 0, onlineCaps: 0, totalCaps: 0 })

/** 建议类型中文标签 */
const typeLabel = (type: string) => {
  const map: Record<string, string> = {
    MORNING_BRIEFING: '晨会简报',
    FAULT_PREDICTION: '故障预测',
    DELIVERY_RISK: '交期风险',
    ENERGY_LOAD: '能耗负荷',
    PREAUDIT: '审批预审',
    ROOT_CAUSE: '根因分析',
    STOCK_SHORTAGE: '备件缺货',
    SAFETY_STOCK: '安全库存',
    DQ_SENTINEL: '数据哨兵',
    EIGHT_D: '8D报告'
  }
  return map[type] || type
}

/** 严重级别中文标签 */
const severityLabel = (severity: string) => {
  const map: Record<string, string> = { INFO: '提示', WARNING: '警告', CRITICAL: '严重' }
  return map[severity] || severity
}

/** 严重级别标签颜色 */
const severityTag = (severity: string) => {
  const map: Record<string, string> = { INFO: 'info', WARNING: 'warning', CRITICAL: 'danger' }
  return (map[severity] || 'info') as any
}

/** 格式化时间显示 */
const formatTime = (time: string | null) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

/** 加载仪表盘数据（建议/统计/大脑状态） */
const loadData = async () => {
  loading.value = true
  try {
    const [sugResp, statsResp, statusResp] = await Promise.all([
      getSuggestions('PENDING'),
      getAdoptionStats(),
      getBrainStatus()
    ])

    const list = DataTransformer.unwrapList<AiSuggestion>(sugResp)
    latestSuggestions.value = list.slice(0, 5)

    const adoption = DataTransformer.unwrapData<any>(statsResp)
    const byType = adoption?.byType || []
    const accepted = byType.reduce((sum: number, t: any) => sum + (t.accepted || 0), 0)
    const rejected = byType.reduce((sum: number, t: any) => sum + (t.rejected || 0), 0)
    const resolved = accepted + rejected
    stats.value.total = adoption?.totalCount ?? 0
    stats.value.pending = list.length
    stats.value.acceptRate = resolved > 0 ? Math.round((accepted / resolved) * 1000) / 10 : 0

    const status = DataTransformer.unwrapData<any>(statusResp)
    const caps = status?.capabilities || []
    stats.value.totalCaps = caps.length
    stats.value.onlineCaps = caps.filter((c: any) => c.status === 'ONLINE').length
    brainPhase.value = status?.phase || 'P0'
    automationDial.value = status?.automationDial || {}
  } catch (e) {
    ElMessage.error('AI 决策中心数据加载失败')
  } finally {
    loading.value = false
  }
}

/** 刷新数据 */
const refreshData = () => {
  loadData()
}

/** 手动生成晨会简报 */
const handleGenerateBriefing = async () => {
  briefingLoading.value = true
  try {
    const resp = await generateBriefing()
    const items = DataTransformer.unwrapList(resp)
    ElMessage.success(`六域巡检完成，发现 ${items.length} 条异常`)
    loadData()
  } catch (e) {
    ElMessage.error('晨会简报生成失败')
  } finally {
    briefingLoading.value = false
  }
}

/** 快捷处置建议（采纳/拒绝） */
const handleFeedback = async (row: AiSuggestion, action: string) => {
  try {
    await feedbackSuggestion(row.id, action)
    ElMessage.success(action === 'ACCEPTED' ? '已采纳该建议' : '已拒绝该建议')
    loadData()
  } catch (e) {
    ElMessage.error('处置失败，请重试')
  }
}

// 返回上一级
const goBack = () => {
  router.push('/home/ai')
}

// 导航到模块详情页
const navigateToModule = (module: string) => {
  router.push(`/home/ai/${module}`)
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.ai-view {
  padding: 24px;
  height: 100%;
  overflow-y: auto;
  background-color: var(--el-bg-color-page);
}

.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  .header-title {
    h2 {
      margin: 0;
      font-size: 24px;
      color: var(--el-text-color-primary);
      font-weight: 600;
    }

    .subtitle {
      margin: 8px 0 0;
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }
  }
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;

  .stat-item {
    background: var(--el-bg-color);
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      margin-right: 16px;

      &.pending {
        background-color: rgba(230, 162, 60, 0.1);
        color: #E6A23C;
      }

      &.total {
        background-color: rgba(64, 158, 255, 0.1);
        color: #409EFF;
      }

      &.rate {
        background-color: rgba(103, 194, 58, 0.1);
        color: #67C23A;
      }

      &.capability {
        background-color: rgba(245, 108, 108, 0.1);
        color: #F56C6C;
      }
    }

    .stat-info {
      flex: 1;

      .stat-label {
        font-size: 14px;
        color: var(--el-text-color-secondary);
        margin-bottom: 4px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: var(--el-text-color-primary);
        line-height: 1.2;

        .unit {
          font-size: 14px;
          margin-left: 4px;
          font-weight: normal;
        }

        .total {
          font-size: 16px;
          color: var(--el-text-color-placeholder);
          font-weight: normal;
        }
      }

      .stat-sub {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-top: 4px;
      }
    }
  }
}

.module-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.module-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  border: 1px solid var(--el-border-color-lighter);
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px 0 rgba(0, 0, 0, 0.1);
    border-color: var(--el-color-primary-light-5);

    .card-icon {
      transform: scale(1.1) rotate(5deg);
    }

    .arrow-icon {
      transform: translateX(4px);
      color: var(--el-color-primary);
    }
  }

  .card-icon {
    width: 64px;
    height: 64px;
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32px;
    margin-bottom: 20px;
    transition: all 0.3s ease;

    &.decision {
      background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(64, 158, 255, 0.2));
      color: #409EFF;
    }

    &.briefing {
      background: linear-gradient(135deg, rgba(230, 162, 60, 0.1), rgba(230, 162, 60, 0.2));
      color: #E6A23C;
    }

    &.rca {
      background: linear-gradient(135deg, rgba(103, 194, 58, 0.1), rgba(103, 194, 58, 0.2));
      color: #67C23A;
    }

    &.capability-card {
      background: linear-gradient(135deg, rgba(245, 108, 108, 0.1), rgba(245, 108, 108, 0.2));
      color: #F56C6C;
    }
  }

  .card-info {
    flex: 1;

    h3 {
      margin: 0 0 8px;
      font-size: 18px;
      color: var(--el-text-color-primary);
    }

    p {
      margin: 0 0 20px;
      color: var(--el-text-color-secondary);
      font-size: 14px;
    }

    .features-list {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      span {
        display: flex;
        align-items: center;
        font-size: 13px;
        color: var(--el-text-color-regular);
        background-color: var(--el-fill-color-light);
        padding: 4px 10px;
        border-radius: 6px;

        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }

  .card-action {
    margin-top: 20px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    color: var(--el-text-color-secondary);
    font-size: 14px;

    .arrow-icon {
      margin-left: 4px;
      transition: all 0.3s ease;
    }
  }
}

.dashboard-section {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      margin: 0;
      font-size: 16px;
      display: flex;
      align-items: center;
      color: var(--el-text-color-primary);

      .el-icon {
        margin-right: 8px;
        color: var(--el-color-primary);
      }
    }
  }
}

.suggestion-table {
  width: 100%;
}

.dial-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;

  .dial-card {
    border-radius: 10px;
    padding: 18px 20px;
    border: 1px solid var(--el-border-color-lighter);

    .dial-level {
      font-size: 16px;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .dial-desc {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      line-height: 1.6;
    }

    &.auto {
      border-left: 4px solid #67C23A;
      background: rgba(103, 194, 58, 0.05);
      .dial-level { color: #67C23A; }
    }

    &.confirm {
      border-left: 4px solid #E6A23C;
      background: rgba(230, 162, 60, 0.05);
      .dial-level { color: #E6A23C; }
    }

    &.forbidden {
      border-left: 4px solid #F56C6C;
      background: rgba(245, 108, 108, 0.05);
      .dial-level { color: #F56C6C; }
    }
  }
}

.mt-4 {
  margin-top: 0;
}
</style>
