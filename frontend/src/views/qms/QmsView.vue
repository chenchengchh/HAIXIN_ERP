<template>
  <div class="qms-view">
    <!-- 子路由视图 -->
    <div v-if="isChildRoute" class="child-router-view">
      <div class="page-header">
        <el-page-header @back="goBack">
          <template #content>
            <span class="text-large font-600 mr-3">{{ currentTabLabel }}</span>
          </template>
        </el-page-header>
      </div>
      <router-view v-slot="{ Component }">
        <transition name="route-transition" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
    
    <!-- 主视图 -->
    <div v-else class="main-dashboard">
      <div class="view-header">
        <div class="header-content">
          <h2>QMS 质量管理</h2>
          <p class="subtitle">全流程质量管控，持续提升产品品质</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus">
            新建检验
          </el-button>
        </div>
      </div>
      
      <!-- 质量概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon check">
            <el-icon><Checked /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日检验批次</div>
            <div class="stat-value">128</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 12%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon rate">
            <el-icon><Medal /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">一次合格率</div>
            <div class="stat-value">98.5<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 0.5%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon defect">
            <el-icon><CircleClose /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">不合格品待处理</div>
            <div class="stat-value warning">5</div>
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon> 2
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon warning">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">质量异常预警</div>
            <div class="stat-value danger">2</div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 质量检验 -->
        <div class="module-card" @click="navigateToModule('quality-inspection')">
          <div class="card-icon inspection">
            <el-icon><List /></el-icon>
          </div>
          <div class="card-info">
            <h3>质量检验计划</h3>
            <p>检验标准与任务执行</p>
            <div class="features-list">
              <span><el-icon><SetUp /></el-icon> 检验标准</span>
              <span><el-icon><Calendar /></el-icon> 检验计划</span>
              <span><el-icon><EditPen /></el-icon> 结果录入</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 不合格品管理 -->
        <div class="module-card" @click="navigateToModule('non-conforming')">
          <div class="card-icon ncr">
            <el-icon><Failed /></el-icon>
          </div>
          <div class="card-info">
            <h3>不合格品管理</h3>
            <p>不合格品评审与处置流程</p>
            <div class="features-list">
              <span><el-icon><Tickets /></el-icon> 不合格登记</span>
              <span><el-icon><User /></el-icon> MRB评审</span>
              <span><el-icon><Delete /></el-icon> 报废/返工</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 质量异常 -->
        <div class="module-card" @click="navigateToModule('quality-anomaly')">
          <div class="card-icon exception">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="card-info">
            <h3>质量异常管理</h3>
            <p>突发质量事件的快速响应</p>
            <div class="features-list">
              <span><el-icon><WarningFilled /></el-icon> 异常报告</span>
              <span><el-icon><Connection /></el-icon> 围堵措施</span>
              <span><el-icon><Finished /></el-icon> 结案归档</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 质量分析 -->
        <div class="module-card" @click="navigateToModule('quality-analysis')">
          <div class="card-icon analysis">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="card-info">
            <h3>质量分析</h3>
            <p>多维质量数据统计与洞察</p>
            <div class="features-list">
              <span><el-icon><PieChart /></el-icon> 不良分布</span>
              <span><el-icon><TrendCharts /></el-icon> SPC控制图</span>
              <span><el-icon><DataLine /></el-icon> 质量成本</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 质量管控流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Operation /></el-icon>
          <span>质量管控闭环</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon plan">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">检验策划</div>
              <div class="step-desc">标准与计划制定</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon execute">
              <el-icon><List /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">检验执行</div>
              <div class="step-desc">IQC/IPQC/FQC</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon control">
              <el-icon><Failed /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">不合格管控</div>
              <div class="step-desc">标识/隔离/处置</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon improve">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">持续改进</div>
              <div class="step-desc">数据分析与CAPA</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { 
  ArrowRight, 
  Plus,
  Checked,
  CaretTop,
  CaretBottom,
  Medal,
  CircleClose,
  Warning,
  List,
  SetUp,
  Calendar,
  EditPen,
  Failed,
  Tickets,
  User,
  Delete,
  Bell,
  WarningFilled,
  Connection,
  Finished,
  DataAnalysis,
  PieChart,
  TrendCharts,
  DataLine,
  Right,
  Operation
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

// 标签页名称映射，用于面包屑导航
const tabLabelMap: Record<string, string> = {
  'quality-inspection': '质量检验',
  'non-conforming': '不合格品管理',
  'quality-anomaly': '质量异常',
  'quality-analysis': '质量分析'
}

// 当前标签页名称
const currentTabLabel = computed(() => {
  const moduleName = route.path.split('/').pop() || ''
  return tabLabelMap[moduleName] || '功能详情'
})

const navigateToModule = (module: string) => {
  router.push(`/home/qms/${module}`)
}

// 是否有未保存的更改（可以根据实际业务场景动态设置）
const hasUnsavedChanges = ref(false)

const goBack = async () => {
  // 如果有未保存的更改，显示确认提示
  if (hasUnsavedChanges.value) {
    try {
      await ElMessageBox.confirm('您有未保存的更改，确定要返回吗？', '确认返回', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch (error) {
      // 用户取消返回
      return
    }
  }
  
  // 直接跳转到QMS主界面
  router.push('/home/qms')
}
</script>

<style scoped lang="scss">
.qms-view {
    padding: 24px;
    min-height: 100%;
    background-color: var(--bg-color-page);
  
  .main-dashboard {
    max-width: 1600px;
    margin: 0 auto;
  }
}

.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  
  .header-content {
    h2 {
      font-size: 28px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }
    
    .subtitle {
      font-size: 16px;
      color: var(--text-secondary);
      margin: 0;
    }
  }
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
  
  .stat-item {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      margin-right: 16px;
      
      &.check { background: #e1f3d8; color: #67c23a; }
      &.rate { background: #e6f7ff; color: #1890ff; }
      &.defect { background: #fef0f0; color: #f56c6c; }
      &.warning { background: #fdf6ec; color: #e6a23c; }
    }
    
    .stat-info {
      flex: 1;
      
      .stat-label {
        font-size: 14px;
        color: var(--text-secondary);
        margin-bottom: 4px;
      }
      
      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: var(--text-primary);
        line-height: 1.2;
        
        .unit {
          font-size: 14px;
          font-weight: normal;
          margin-left: 4px;
        }
        
        &.warning { color: #e6a23c; }
        &.danger { color: #f56c6c; }
      }
      
      .stat-trend {
        font-size: 12px;
        display: flex;
        align-items: center;
        margin-top: 4px;
        
        &.up { color: #67c23a; }
        &.down { color: #f56c6c; }
        
        .el-icon {
          margin-right: 2px;
        }
      }
    }
  }
}

.module-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.module-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  position: relative;
  overflow: hidden;
  height: 220px;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
    border-color: var(--primary-light);
    
    .card-icon {
      transform: scale(1.1) rotate(5deg);
      background: var(--primary-color);
      color: #fff;
    }
    
    .arrow-icon {
      transform: translateX(4px);
      color: var(--primary-color);
    }
  }
  
  .card-icon {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    background: var(--bg-color-page);
    color: var(--primary-color);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-bottom: 20px;
    transition: all 0.3s ease;
    
    &.inspection { color: #409EFF; background: #ecf5ff; }
    &.ncr { color: #F56C6C; background: #fef0f0; }
    &.exception { color: #E6A23C; background: #fdf6ec; }
    &.analysis { color: #67C23A; background: #f0f9eb; }
  }
  
  .card-info {
    flex: 1;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      margin: 0 0 8px 0;
      color: var(--text-primary);
    }
    
    p {
      font-size: 14px;
      color: var(--text-secondary);
      margin: 0 0 16px 0;
      line-height: 1.5;
    }
    
    .features-list {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      
      span {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: var(--text-regular);
        background: var(--bg-color-page);
        padding: 4px 8px;
        border-radius: 4px;
        
        .el-icon {
          font-size: 14px;
        }
      }
    }
  }
  
  .card-action {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid var(--border-color-light);
    
    .action-text {
      font-size: 14px;
      color: var(--text-secondary);
    }
    
    .arrow-icon {
      font-size: 16px;
      color: var(--text-placeholder);
      transition: all 0.3s ease;
    }
  }
}

.process-flow {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 24px;
    
    .el-icon {
      color: var(--primary-color);
    }
  }
  
  .flow-steps {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    
    .flow-step {
      display: flex;
      align-items: center;
      flex: 1;
      
      &:last-child {
        flex: 0 0 auto;
        .step-arrow {
          display: none;
        }
      }
      
      .step-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        margin-right: 16px;
        flex-shrink: 0;
        
        &.plan { background: #ecf5ff; color: #409EFF; }
        &.iqc { background: #f0f9eb; color: #67C23A; }
        &.ipqc { background: #fdf6ec; color: #E6A23C; }
        &.fqc { background: #fef0f0; color: #F56C6C; }
        &.improve { background: #ecfdf5; color: #10b981; }
      }
      
      .step-content {
        .step-title {
          font-size: 16px;
          font-weight: 600;
          color: var(--text-primary);
          margin-bottom: 4px;
        }
        
        .step-desc {
          font-size: 12px;
          color: var(--text-secondary);
        }
      }
      
      .step-arrow {
        flex: 1;
        display: flex;
        justify-content: center;
        color: var(--text-placeholder);
        font-size: 20px;
        margin: 0 16px;
      }
    }
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: 1fr 1fr;
  }
  
  .flow-steps {
    flex-direction: column;
    gap: 24px;
    padding: 0 !important;
    
    .flow-step {
      width: 100%;
      
      .step-arrow {
        transform: rotate(90deg);
        margin: 12px 0;
      }
    }
  }
}
</style>