<template>
  <div class="capabilities-view">
    <!-- 自动化刻度盘 -->
    <div class="dial-section">
      <div class="section-title">
        <el-icon><Operation /></el-icon> 自动化刻度盘（Automation Dial）
      </div>
      <div class="section-desc">AI 可执行动作按风险分级，权限永不越级——治理原则对全员可见</div>
      <div class="dial-cards">
        <div class="dial-card auto">
          <div class="dial-level">AUTO</div>
          <div class="dial-desc">{{ dial.AUTO }}</div>
          <div class="dial-examples">晨会简报 · 异常告警 · 数据对账 · 根因分析</div>
        </div>
        <div class="dial-card confirm">
          <div class="dial-level">CONFIRM</div>
          <div class="dial-desc">{{ dial.CONFIRM }}</div>
          <div class="dial-examples">采购申请草稿 · 维护工单草稿 · 安全库存调整</div>
        </div>
        <div class="dial-card forbidden">
          <div class="dial-level">FORBIDDEN</div>
          <div class="dial-desc">{{ dial.FORBIDDEN }}</div>
          <div class="dial-examples">删除业务数据 · 直改库存金额 · 绕过审批流</div>
        </div>
      </div>
    </div>

    <!-- 技能矩阵 -->
    <div class="caps-section">
      <div class="section-title">
        <el-icon><Cpu /></el-icon> AI 技能矩阵（{{ phase }} 阶段）
      </div>
      <div class="section-desc">按智能化四条判据设计：主动性 · 闭环性 · 学习性 · 可解释性</div>

      <div v-loading="loading" class="caps-table-wrap">
        <el-table :data="capabilities" class="caps-table">
          <el-table-column prop="code" label="技能编码" width="200">
            <template #default="{ row }">
              <code class="cap-code">{{ row.code }}</code>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="技能名称" min-width="220" />
          <el-table-column prop="phase" label="期次" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="phaseTag(row.phase)" effect="dark">{{ row.phase }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 'ONLINE' ? 'success' : 'info'">
                {{ row.status === 'ONLINE' ? '在线' : '规划中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="endpoint" label="服务端点" min-width="300">
            <template #default="{ row }">
              <code class="cap-endpoint">{{ row.endpoint }}</code>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 期次说明 -->
      <div class="phase-notes">
        <div class="phase-note">
          <el-tag type="success" size="small" effect="dark">P0</el-tag>
          <span>信任地基：数据质量哨兵 + 维修 Copilot，零风险见效</span>
        </div>
        <div class="phase-note">
          <el-tag type="primary" size="small" effect="dark">P1</el-tag>
          <span>低风险增强：只附加不改动——晨会简报 / 故障预测 / 审批预审</span>
        </div>
        <div class="phase-note">
          <el-tag type="warning" size="small" effect="dark">P2</el-tag>
          <span>逐步开放写权限：根因会诊 / 风险预测 / 反馈学习环 / 库存优化</span>
        </div>
        <div class="phase-note">
          <el-tag type="info" size="small" effect="dark">P3</el-tag>
          <span>多模态扩展：语音车间（备件查询/异常记录）· 单据/仪表视觉理解（规则版已上线：发票建单/拍照抄表）</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Operation, Cpu } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { DataTransformer } from '@/utils/data-transformer'
import { getBrainStatus } from '@/api/ai'

// 页面状态
const loading = ref(false)
const dial = ref<Record<string, string>>({})
const capabilities = ref<any[]>([])
const phase = ref('P0')

/** 期次标签颜色 */
const phaseTag = (p: string) => {
  const map: Record<string, string> = { P0: 'success', P1: 'primary', P2: 'warning', P3: 'info' }
  return (map[p] || 'info') as any
}

/** 加载大脑状态 */
const loadStatus = async () => {
  loading.value = true
  try {
    const resp = await getBrainStatus()
    const data = DataTransformer.unwrapData<any>(resp)
    dial.value = data?.automationDial || {}
    capabilities.value = data?.capabilities || []
    phase.value = data?.phase || 'P0'
  } catch (e) {
    ElMessage.error('能力矩阵加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadStatus)
</script>

<style scoped lang="scss">
.capabilities-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dial-section,
.caps-section {
  background: var(--el-bg-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-title {
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

.section-desc {
  margin: 8px 0 18px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
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
      font-size: 18px;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .dial-desc {
      font-size: 13px;
      color: var(--el-text-color-secondary);
      line-height: 1.6;
      margin-bottom: 10px;
    }

    .dial-examples {
      font-size: 12px;
      color: var(--el-text-color-placeholder);
      border-top: 1px dashed var(--el-border-color-lighter);
      padding-top: 10px;
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

.caps-table {
  width: 100%;

  .cap-code {
    background: var(--el-fill-color-light);
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
  }

  .cap-endpoint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.phase-notes {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;

  .phase-note {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}
</style>
