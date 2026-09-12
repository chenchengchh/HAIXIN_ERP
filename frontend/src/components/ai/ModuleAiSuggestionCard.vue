<template>
  <!--
    通用 AI 建议卡片（各业务模块内嵌入口）
    按模块+建议类型加载 PENDING 建议，空态自动隐藏，不占版面；
    点击建议项跳转 AI 决策中心查看证据链与动作草稿。
  -->
  <el-card v-if="visibleSuggestions.length > 0" class="module-ai-card" shadow="hover" v-loading="loading">
    <template #header>
      <div class="card-header">
        <span class="header-title">
          <el-icon class="header-icon"><Cpu /></el-icon>
          {{ title }}
        </span>
        <el-button type="primary" text size="small" @click="goToDecisionCenter">
          查看全部
          <el-icon class="ml-4"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </template>
    <div class="suggestion-list">
      <div
        v-for="item in visibleSuggestions"
        :key="item.id"
        class="suggestion-item"
        :class="item.severity.toLowerCase()"
        @click="goToDecisionCenter"
      >
        <el-tag size="small" :type="severityTag(item.severity)" effect="dark" class="severity-tag">
          {{ severityLabel(item.severity) }}
        </el-tag>
        <span class="item-title" :title="item.title">{{ item.title }}</span>
        <el-tag size="small" :type="item.automationLevel === 'CONFIRM' ? 'warning' : 'success'" effect="plain" class="level-tag">
          {{ item.automationLevel }}
        </el-tag>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
/**
 * 通用 AI 建议卡片组件
 * <p>各业务模块页面内嵌使用，按模块+建议类型自动加载 PENDING 建议。
 * 空态（无 PENDING 建议）时整体隐藏，加载失败静默降级不阻塞业务页面。</p>
 *
 * @prop module    模块标识（如 WMS/MES/EMS/SRM），对应 AiSuggestion.module
 * @prop suggestionType 建议类型（如 SAFETY_STOCK/FAULT_PREDICTION/DELIVERY_RISK/ENERGY_LOAD）
 * @prop title     卡片标题（如"AI 安全库存建议"）
 * @prop maxDisplay 最多展示条数，默认 5
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Cpu, ArrowRight } from '@element-plus/icons-vue'
import { getSuggestions, type AiSuggestion } from '@/api/ai'
import { DataTransformer } from '@/utils/data-transformer'

const props = withDefaults(defineProps<{
  module?: string
  suggestionType?: string
  title?: string
  maxDisplay?: number
}>(), {
  module: '',
  suggestionType: '',
  title: 'AI 智能建议',
  maxDisplay: 5
})

const router = useRouter()
const loading = ref(false)
const suggestions = ref<AiSuggestion[]>([])

/** 最多展示前 N 条建议 */
const visibleSuggestions = computed(() => suggestions.value.slice(0, props.maxDisplay))

/**
 * 加载当前模块+类型的 PENDING 建议
 * 失败时静默降级为空列表，仅输出告警日志
 */
const loadSuggestions = async () => {
  loading.value = true
  try {
    const resp = await getSuggestions('PENDING', props.suggestionType)
    const list = DataTransformer.unwrapList<AiSuggestion>(resp)
    // 按模块过滤（后端类型查询不带模块过滤，前端再过滤）
    suggestions.value = props.module
      ? list.filter(item => item.status === 'PENDING' && item.module === props.module)
      : list.filter(item => item.status === 'PENDING')
  } catch (e) {
    console.warn('AI 建议加载失败，已降级为空状态', e)
    suggestions.value = []
  } finally {
    loading.value = false
  }
}

/** 跳转 AI 决策中心查看详情 */
const goToDecisionCenter = () => {
  router.push('/home/ai/decision-center')
}

/** 严重级别映射为 Element Plus 标签类型 */
const severityTag = (severity: string): 'danger' | 'warning' | 'info' => {
  const map: Record<string, 'danger' | 'warning' | 'info'> = {
    CRITICAL: 'danger',
    WARNING: 'warning',
    INFO: 'info'
  }
  return map[severity] || 'info'
}

/** 严重级别映射为中文标签 */
const severityLabel = (severity: string): string => {
  const map: Record<string, string> = {
    CRITICAL: '严重',
    WARNING: '警告',
    INFO: '提示'
  }
  return map[severity] || severity
}

onMounted(loadSuggestions)

// 暴露刷新方法供父组件调用
defineExpose({ refresh: loadSuggestions })
</script>

<style scoped lang="scss">
.module-ai-card {
  margin-bottom: 16px;
  border-left: 3px solid var(--el-color-primary);

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .header-title {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      font-weight: 600;

      .header-icon {
        color: var(--el-color-primary);
      }
    }
  }

  .suggestion-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .suggestion-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: background 0.2s;

    &:hover {
      background: var(--el-fill-color-light);
    }

    &.critical {
      border-left: 3px solid var(--el-color-danger);
    }
    &.warning {
      border-left: 3px solid var(--el-color-warning);
    }
    &.info {
      border-left: 3px solid var(--el-color-info);
    }

    .severity-tag {
      flex-shrink: 0;
    }

    .item-title {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 13px;
      color: var(--el-text-color-primary);
    }

    .level-tag {
      flex-shrink: 0;
    }
  }
}

.ml-4 {
  margin-left: 4px;
}
</style>
