<template>
  <div class="briefing-view">
    <!-- 头部操作区 -->
    <div class="briefing-header">
      <div class="header-info">
        <div class="date-badge">
          <el-icon><Calendar /></el-icon>
          {{ today }}
        </div>
        <div class="header-desc">六域 Agent 并行巡检 · 无异常不发声 · 每日 8:30 自动推送</div>
      </div>
      <el-button type="primary" :icon="Refresh" :loading="generating" @click="handleGenerate">立即巡检</el-button>
    </div>

    <!-- 六域巡检结果 -->
    <div v-loading="loading" class="domain-grid">
      <div v-for="domain in domains" :key="domain.code" class="domain-card" :class="domainStatus(domain.code)">
        <div class="domain-head">
          <div class="domain-icon">
            <el-icon><component :is="domain.icon" /></el-icon>
          </div>
          <div class="domain-meta">
            <div class="domain-name">{{ domain.name }}</div>
            <div class="domain-scope">{{ domain.scope }}</div>
          </div>
          <el-tag size="small" :type="domainTagType(domain.code)" effect="dark">
            {{ domainTagLabel(domain.code) }}
          </el-tag>
        </div>

        <!-- 该域异常条目 -->
        <div v-if="itemsOf(domain.code).length > 0" class="domain-items">
          <div v-for="item in itemsOf(domain.code)" :key="item.id" class="brief-item">
            <div class="brief-title">{{ item.title }}</div>
            <div v-if="actionAdvice(item)" class="brief-advice">
              <el-icon><Position /></el-icon> {{ actionAdvice(item) }}
            </div>
            <div v-if="evidenceMetrics(item)" class="brief-metrics">
              <span v-for="(v, k) in evidenceMetrics(item)" :key="k" class="metric-chip">{{ k }}: {{ v }}</span>
            </div>
          </div>
        </div>
        <div v-else class="domain-ok">
          <el-icon><CircleCheck /></el-icon>
          <span>本域巡检无异常</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  Calendar, Refresh, Position, CircleCheck,
  Odometer, Tools, ShoppingCart, Medal, Van, Lightning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'
import { getSuggestions, generateBriefing, type AiSuggestion } from '@/api/ai'

// 页面状态
const loading = ref(false)
const generating = ref(false)
const briefingItems = ref<AiSuggestion[]>([])

/** 今日日期显示 */
const today = new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })

/** 六域定义（与后端巡检域一一对应） */
const domains = [
  { code: 'PLANNER', name: '计划员', scope: 'MES 工单达成 · 排产冲突', icon: Odometer },
  { code: 'EQUIPMENT', name: '设备工程师', scope: 'EAM 设备健康 · 预测告警', icon: Tools },
  { code: 'PROCUREMENT', name: '采购员', scope: 'SRM 在途风险 · 到货异常', icon: ShoppingCart },
  { code: 'QUALITY', name: '质量工程师', scope: 'QMS 不合格趋势 · 待处理8D', icon: Medal },
  { code: 'LOGISTICS', name: '仓储物流', scope: 'WMS 库存预警 · LES 在途异常', icon: Van },
  { code: 'ENERGY', name: '能源专员', scope: 'EMS 能耗对标 · 采集链路', icon: Lightning }
]

/** 解析条目 analysis JSON */
const parsed = (item: AiSuggestion) => {
  if (!item.analysis) return null
  try { return JSON.parse(item.analysis) } catch { return null }
}

/** 提取所属域 */
const domainOf = (item: AiSuggestion) => parsed(item)?.domain || ''

/** 提取建议动作 */
const actionAdvice = (item: AiSuggestion) => parsed(item)?.actionAdvice || ''

/** 提取关键指标 */
const evidenceMetrics = (item: AiSuggestion) => parsed(item)?.evidence?.metrics || null

/** 按域过滤条目 */
const itemsOf = (domainCode: string) => briefingItems.value.filter(i => domainOf(i) === domainCode)

/** 域状态（有异常/无异常） */
const domainStatus = (domainCode: string) => {
  const items = itemsOf(domainCode)
  if (items.length === 0) return 'ok'
  return items.some(i => i.severity === 'CRITICAL') ? 'critical' : 'warning'
}

/** 域状态标签 */
const domainTagType = (domainCode: string) => {
  const s = domainStatus(domainCode)
  return s === 'critical' ? 'danger' : s === 'warning' ? 'warning' : 'success'
}

const domainTagLabel = (domainCode: string) => {
  const n = itemsOf(domainCode).length
  return n > 0 ? `${n} 条异常` : '正常'
}

/** 加载今日简报（MORNING_BRIEFING 类型建议） */
const loadBriefing = async () => {
  loading.value = true
  try {
    const resp = await getSuggestions(undefined, 'MORNING_BRIEFING')
    briefingItems.value = DataTransformer.unwrapList<AiSuggestion>(resp)
  } catch (e) {
    ElMessage.error('简报加载失败')
  } finally {
    loading.value = false
  }
}

/** 手动触发六域巡检 */
const handleGenerate = async () => {
  generating.value = true
  try {
    const resp = await generateBriefing()
    const items = DataTransformer.unwrapList(resp)
    ElMessage.success(items.length > 0 ? `巡检完成，发现 ${items.length} 条异常` : '巡检完成，六域均无异常')
    loadBriefing()
  } catch (e) {
    ElMessage.error('巡检执行失败')
  } finally {
    generating.value = false
  }
}

onMounted(loadBriefing)
</script>

<style scoped lang="scss">
.briefing-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.briefing-header {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .header-info {
    .date-badge {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);

      .el-icon {
        color: var(--el-color-primary);
      }
    }

    .header-desc {
      margin-top: 6px;
      font-size: 13px;
      color: var(--el-text-color-secondary);
    }
  }
}

.domain-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 20px;
  min-height: 300px;
}

.domain-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border-top: 3px solid #67C23A;

  &.warning { border-top-color: #E6A23C; }
  &.critical { border-top-color: #F56C6C; }

  .domain-head {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;

    .domain-icon {
      width: 44px;
      height: 44px;
      border-radius: 12px;
      background: var(--el-color-primary-light-9);
      color: var(--el-color-primary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 22px;
    }

    .domain-meta {
      flex: 1;

      .domain-name {
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .domain-scope {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        margin-top: 2px;
      }
    }
  }

  .domain-items {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .brief-item {
      background: var(--el-fill-color-lighter);
      border-radius: 8px;
      padding: 12px 14px;

      .brief-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        line-height: 1.5;
      }

      .brief-advice {
        margin-top: 8px;
        font-size: 13px;
        color: var(--el-color-primary);
        display: flex;
        align-items: flex-start;
        gap: 6px;
        line-height: 1.5;
      }

      .brief-metrics {
        margin-top: 8px;
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .metric-chip {
          font-size: 12px;
          background: var(--el-bg-color);
          border: 1px solid var(--el-border-color-lighter);
          padding: 2px 8px;
          border-radius: 4px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }

  .domain-ok {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 24px 0;
    color: #67C23A;
    font-size: 14px;
  }
}
</style>
