<template>
  <div class="root-cause-view">
    <!-- 故障选择区 -->
    <div class="selector-panel">
      <div class="panel-title">
        <el-icon><Connection /></el-icon>
        <span>选择故障记录，沿本体链路追溯跨域根因</span>
      </div>
      <div class="selector-row">
        <el-select
          v-model="selectedFaultId"
          placeholder="选择 EAM 故障记录"
          filterable
          style="width: 420px"
          :loading="faultsLoading"
        >
          <el-option
            v-for="f in faultOptions"
            :key="f.id"
            :label="`#${f.id} ${f.equipmentName || ''} - ${f.faultDescription || f.description || ''}`"
            :value="f.id"
          />
        </el-select>
        <el-button type="primary" :disabled="!selectedFaultId" :loading="tracing" @click="handleTrace">
          开始追溯
        </el-button>
      </div>
    </div>

    <!-- 追溯结果 -->
    <div v-if="traceResult" class="trace-result">
      <!-- 结论 -->
      <div class="conclusion-card" :class="traceResult.chainLength > 1 ? 'multi' : 'single'">
        <div class="conclusion-title">
          <el-icon><Aim /></el-icon> 会诊结论
        </div>
        <div class="conclusion-text">{{ traceResult.conclusion }}</div>
        <div class="conclusion-meta">
          <el-tag size="small" effect="plain">追溯模式 {{ traceResult.evidence?.traceMode }}</el-tag>
          <el-tag size="small" effect="plain">链路长度 {{ traceResult.chainLength }}</el-tag>
          <el-tag v-for="s in traceResult.evidence?.sources || []" :key="s" size="small" type="info" effect="plain">{{ s }}</el-tag>
        </div>
      </div>

      <!-- 因果链路 -->
      <div class="chain-card">
        <div class="chain-title">
          <el-icon><Share /></el-icon> 因果链路（从故障现场向上游追溯）
        </div>
        <el-timeline class="chain-timeline">
          <el-timeline-item
            v-for="node in traceResult.chain"
            :key="node.seq"
            :type="node.seq === 1 ? 'danger' : 'warning'"
            :hollow="node.seq !== 1"
            size="large"
          >
            <div class="chain-node">
              <div class="node-head">
                <span class="node-stage">{{ node.stage }}</span>
                <el-tag size="small" effect="dark">{{ node.module }}</el-tag>
              </div>
              <div class="node-finding">{{ node.finding }}</div>
              <div class="node-evidence">
                <div class="evidence-source">
                  <el-icon><Coin /></el-icon> 证据源：{{ node.evidence?.source }}
                </div>
                <pre class="evidence-metrics">{{ JSON.stringify(node.evidence?.metrics, null, 2) }}</pre>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="!tracing" description="选择故障记录后点击「开始追溯」，AI 将沿 设备→工单→备件→采购 链路自动追溯" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Connection, Aim, Share, Coin } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'
import { getRootCauseByFault } from '@/api/ai'
import { getFaults } from '@/api/eam'

// 故障选择状态
const faultsLoading = ref(false)
const faultOptions = ref<any[]>([])
const selectedFaultId = ref<number | null>(null)

// 追溯状态
const tracing = ref(false)
const traceResult = ref<any>(null)

/** 加载 EAM 故障记录下拉选项 */
const loadFaults = async () => {
  faultsLoading.value = true
  try {
    const resp = await getFaults()
    faultOptions.value = DataTransformer.unwrapList<any>(resp)
  } catch (e) {
    ElMessage.error('故障记录加载失败')
  } finally {
    faultsLoading.value = false
  }
}

/** 执行跨域根因追溯 */
const handleTrace = async () => {
  if (!selectedFaultId.value) return
  tracing.value = true
  traceResult.value = null
  try {
    const resp = await getRootCauseByFault(selectedFaultId.value)
    const data = DataTransformer.unwrapData<any>(resp)
    if (!data || data.success === false) {
      ElMessage.warning(data?.conclusion || '未找到可追溯链路')
      return
    }
    traceResult.value = data
  } catch (e) {
    ElMessage.error('根因追溯失败')
  } finally {
    tracing.value = false
  }
}

onMounted(loadFaults)
</script>

<style scoped lang="scss">
.root-cause-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.selector-panel {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .panel-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 14px;

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .selector-row {
    display: flex;
    gap: 12px;
  }
}

.trace-result {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.conclusion-card {
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  background: var(--el-bg-color);
  border-left: 4px solid #67C23A;

  &.multi {
    border-left-color: #F56C6C;
  }

  .conclusion-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 10px;
  }

  .conclusion-text {
    font-size: 15px;
    line-height: 1.7;
    color: var(--el-text-color-regular);
    margin-bottom: 12px;
  }

  .conclusion-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

.chain-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

  .chain-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    margin-bottom: 20px;

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .chain-node {
    background: var(--el-fill-color-lighter);
    border-radius: 10px;
    padding: 14px 16px;

    .node-head {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 8px;

      .node-stage {
        font-weight: 700;
        color: var(--el-text-color-primary);
      }
    }

    .node-finding {
      font-size: 14px;
      color: var(--el-text-color-regular);
      line-height: 1.6;
      margin-bottom: 10px;
    }

    .node-evidence {
      .evidence-source {
        font-size: 12px;
        color: var(--el-text-color-secondary);
        display: flex;
        align-items: center;
        gap: 4px;
        margin-bottom: 6px;
      }

      .evidence-metrics {
        margin: 0;
        font-size: 12px;
        background: var(--el-bg-color);
        padding: 8px 10px;
        border-radius: 6px;
        overflow-x: auto;
        white-space: pre-wrap;
        word-break: break-all;
        max-height: 200px;
        overflow-y: auto;
      }
    }
  }
}
</style>
