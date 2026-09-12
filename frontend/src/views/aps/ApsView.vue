<template>
  <div class="aps-view">
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
    
    <!-- 主视图 -->
    <div v-else class="main-dashboard">
      <div class="view-header">
        <div class="header-content">
          <h2>APS 高级计划与排程</h2>
          <p class="subtitle">智能排程与资源优化，实现精益生产</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="VideoPlay" @click="goPlanGeneration">
            立即排程
          </el-button>
        </div>
      </div>

      <!-- 排程概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon rate">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">排程达成率</div>
            <div class="stat-value">{{ dashboardStats.scheduleRate }}<span class="unit">%</span></div>
            <div class="stat-sub">已排程 {{ dashboardStats.scheduledCount }} / {{ dashboardStats.planCount }} 个计划</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon resource">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">资源利用率</div>
            <div class="stat-value">{{ dashboardStats.resourceUtilization }}<span class="unit">%</span></div>
            <div class="stat-sub">设备与生产线占用情况</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon order">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待排计划</div>
            <div class="stat-value warning">{{ dashboardStats.pendingPlans }}</div>
            <div class="stat-sub">草稿状态待排程</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon delay">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">异常预警</div>
            <div class="stat-value danger">{{ dashboardStats.activeAlerts }}</div>
            <div class="stat-sub">未处理偏差预警</div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 基础数据配置 -->
        <div class="module-card" @click="navigateToModule('basic-data')">
          <div class="card-icon basic">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="card-info">
            <h3>基础数据</h3>
            <p>构建高精度生产模型</p>
            <div class="features-list">
              <span><el-icon><DataBoard /></el-icon> 产能模型</span>
              <span><el-icon><Share /></el-icon> 工艺路线</span>
              <span><el-icon><Calendar /></el-icon> 资源日历</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 计划生成 -->
        <div class="module-card" @click="navigateToModule('plan-generation')">
          <div class="card-icon plan">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="card-info">
            <h3>计划生成</h3>
            <p>快速生成最优生产计划</p>
            <div class="features-list">
              <span><el-icon><List /></el-icon> 主计划MPS</span>
              <span><el-icon><Grid /></el-icon> 详细排程</span>
              <span><el-icon><Cpu /></el-icon> 自动运算</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 动态优化 -->
        <div class="module-card" @click="navigateToModule('dynamic-optimization')">
          <div class="card-icon optimize">
            <el-icon><Refresh /></el-icon>
          </div>
          <div class="card-info">
            <h3>动态优化</h3>
            <p>敏捷应对生产异常</p>
            <div class="features-list">
              <span><el-icon><Lightning /></el-icon> 插单急单</span>
              <span><el-icon><WarningFilled /></el-icon> 故障重排</span>
              <span><el-icon><Lock /></el-icon> 锁定排程</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 计划监控 -->
        <div class="module-card" @click="navigateToModule('plan-monitoring')">
          <div class="card-icon monitor">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="card-info">
            <h3>计划监控</h3>
            <p>可视化监控生产全过程</p>
            <div class="features-list">
              <span><el-icon><Aim /></el-icon> 进度追踪</span>
              <span><el-icon><Bell /></el-icon> 延期预警</span>
              <span><el-icon><TrendCharts /></el-icon> 产能负荷</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 智能排程闭环 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          <span>智能排程闭环</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon demand">
              <el-icon><List /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">需求统筹</div>
              <div class="step-desc">订单/预测/库存</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon capacity">
              <el-icon><DataBoard /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">产能平衡</div>
              <div class="step-desc">资源/物料约束</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon schedule">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">智能排程</div>
              <div class="step-desc">算法运算/优化</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon execute">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">发布执行</div>
              <div class="step-desc">工单下发/反馈</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { PlanMonitoringAPI } from '@/api/aps'
import {
  Setting,
  Calendar,
  Refresh,
  Monitor,
  ArrowRight,
  Connection,
  Right,
  List,
  DataBoard,
  VideoPlay,
  CaretTop,
  Cpu,
  Warning,
  Share,
  Grid,
  Lightning,
  WarningFilled,
  Lock,
  Aim,
  Bell,
  TrendCharts,
  DataLine
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

/**
 * 仪表盘统计数据（来自后端全局聚合接口 /monitoring/dashboard/stats）
 */
const dashboardStats = reactive({
  scheduleRate: 0,
  resourceUtilization: 0,
  pendingPlans: 0,
  activeAlerts: 0,
  planCount: 0,
  scheduledCount: 0
})

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

const navigateToModule = (modulePath: string) => {
  router.push(`/home/aps/${modulePath}`)
}

/** 立即排程：跳转到计划生成模块 */
const goPlanGeneration = () => {
  router.push('/home/aps/plan-generation')
}

/**
 * 加载仪表盘统计数据
 */
const loadDashboardStats = async () => {
  try {
    const res = await PlanMonitoringAPI.getDashboardStats()
    const data = (res as any)?.data ?? res
    if (data) {
      dashboardStats.scheduleRate = data.scheduleRate ?? 0
      dashboardStats.resourceUtilization = data.resourceUtilization ?? 0
      dashboardStats.pendingPlans = data.pendingPlans ?? 0
      dashboardStats.activeAlerts = data.activeAlerts ?? 0
      dashboardStats.planCount = data.planCount ?? 0
      dashboardStats.scheduledCount = data.scheduledCount ?? 0
    }
  } catch (error) {
    console.error('加载仪表盘统计数据失败:', error)
  }
}

onMounted(() => {
  loadDashboardStats()
})

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.aps-view {
  padding: 24px;
  min-height: 100%;
  background-color: var(--bg-color-page);
  
  .child-router-view {
    background: #fff;
    border-radius: 8px;
    padding: 24px;
    min-height: calc(100vh - 120px);
    box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  }
  
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
      
      &.rate { background: #e1f3d8; color: #67c23a; }
      &.resource { background: #e6f7ff; color: #1890ff; }
      &.order { background: #fdf6ec; color: #e6a23c; }
      &.delay { background: #fef0f0; color: #f56c6c; }
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
      
      .stat-sub {
        font-size: 12px;
        color: var(--text-secondary);
        margin-top: 4px;
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
    
    &.basic { color: #409EFF; background: #ecf5ff; }
    &.plan { color: #67C23A; background: #f0f9eb; }
    &.optimize { color: #E6A23C; background: #fdf6ec; }
    &.monitor { color: #F56C6C; background: #fef0f0; }
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
        
        &.demand { background: #ecf5ff; color: #409EFF; }
        &.capacity { background: #f0f9eb; color: #67C23A; }
        &.schedule { background: #fdf6ec; color: #E6A23C; }
        &.execute { background: #fef0f0; color: #F56C6C; }
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
