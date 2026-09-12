<template>
  <div class="agv-view">
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
          <h2>AGV 自动导引车系统</h2>
          <p class="subtitle">智能物流搬运，实现仓储自动化</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Monitor">
            监控大屏
          </el-button>
        </div>
      </div>
      
      <!-- 车辆状态统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon active">
            <el-icon><Van /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">运行中车辆</div>
            <div class="stat-value">{{ operationStats.activeAgvCount || 0 }}<span class="total">/{{ operationStats.totalAgvCount || 0 }}</span></div>
            <div class="stat-sub">在线率 {{ operationStats.uptimeRate ? operationStats.uptimeRate.toFixed(0) : 0 }}%</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon task">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">运行中任务</div>
            <div class="stat-value">{{ operationStats.runningTaskCount || 0 }}</div>
            <div class="stat-sub">已完成 {{ operationStats.completedTaskCount || 0 }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon battery">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">充电中</div>
            <div class="stat-value warning">{{ chargingCount }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon error">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">故障车辆</div>
            <div class="stat-value danger">{{ operationStats.faultAgvCount || 0 }}</div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 路径规划 -->
        <div class="module-card" @click="navigateToModule('path-planning')">
          <div class="card-icon path">
            <el-icon><Guide /></el-icon>
          </div>
          <div class="card-info">
            <h3>路径规划</h3>
            <p>动态路径优化与交通管制</p>
            <div class="features-list">
              <span><el-icon><Share /></el-icon> 路线绘制</span>
              <span><el-icon><Position /></el-icon> 避障算法</span>
              <span><el-icon><Sort /></el-icon> 交管策略</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 任务管理 -->
        <div class="module-card" @click="navigateToModule('task-management')">
          <div class="card-icon task">
            <el-icon><List /></el-icon>
          </div>
          <div class="card-info">
            <h3>任务管理</h3>
            <p>搬运任务调度与执行监控</p>
            <div class="features-list">
              <span><el-icon><Plus /></el-icon> 任务下发</span>
              <span><el-icon><Finished /></el-icon> 优先级调度</span>
              <span><el-icon><Timer /></el-icon> 任务追踪</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 状态监控 -->
        <div class="module-card" @click="navigateToModule('status-monitoring')">
          <div class="card-icon monitor">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="card-info">
            <h3>状态监控</h3>
            <p>车辆实时位置与状态监视</p>
            <div class="features-list">
              <span><el-icon><Location /></el-icon> 实时位置</span>
              <span><el-icon><Warning /></el-icon> 电量监控</span>
              <span><el-icon><VideoPlay /></el-icon> 轨迹回放</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 调度管理 -->
        <div class="module-card" @click="navigateToModule('dispatch-management')">
          <div class="card-icon dispatch">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="card-info">
            <h3>调度管理</h3>
            <p>多车协同与智能调度策略</p>
            <div class="features-list">
              <span><el-icon><Connection /></el-icon> 多车协同</span>
              <span><el-icon><Setting /></el-icon> 策略配置</span>
              <span><el-icon><DataAnalysis /></el-icon> 效率分析</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 智能搬运流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          <span>智能搬运闭环</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon order">
              <el-icon><List /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">任务下发</div>
              <div class="step-desc">WMS/MES/人工指令</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon dispatch">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">智能调度</div>
              <div class="step-desc">最优车辆指派</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon path">
              <el-icon><Guide /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">路径规划</div>
              <div class="step-desc">动态避障导航</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon execute">
              <el-icon><Van /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">搬运执行</div>
              <div class="step-desc">精准取放货</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAGVStore } from '@/stores/agv'
import { 
  ArrowRight, 
  Monitor,
  Van,
  List,
  Lightning,
  Warning,
  Guide,
  Share,
  Position,
  Sort,
  Plus,
  Finished,
  Timer,
  Location,
  VideoPlay,
  Cpu,
  Connection,
  Setting,
  DataAnalysis,
  Right
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const agvStore = useAGVStore()
const { operationStats, agvs } = storeToRefs(agvStore)

// 充电中AGV数量
const chargingCount = computed(() => agvs.value.filter(a => a.status === 'charging').length)

onMounted(async () => {
  await Promise.all([agvStore.loadOperationStats(), agvStore.loadAGVs()])
})

const isChildRoute = computed(() => {
  return route.name !== 'agv'
})

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'path-planning': '路径规划',
  'task-management': '任务管理',
  'status-monitoring': '状态监控',
  'dispatch-management': '调度管理'
}

// 当前标签页名称
const currentTabLabel = computed(() => {
  const moduleName = route.path.split('/').pop() || ''
  return tabLabelMap[moduleName] || '功能详情'
})

const navigateToModule = (module: string) => {
  router.push(`/home/agv/${module}`)
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.agv-view {
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
  
  .page-header {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 20px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-color-light);
    
    h2 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
    }
    
    .el-breadcrumb {
      font-size: 14px;
      color: var(--text-secondary);
      
      .el-breadcrumb__inner.is-link {
        color: var(--text-regular);
        
        &:hover {
          color: var(--primary-color);
        }
      }
      
      .el-breadcrumb__item:last-child .el-breadcrumb__inner {
        color: var(--text-secondary);
        font-weight: normal;
      }
    }
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
      
      &.active { background: #ecf5ff; color: #409EFF; }
      &.task { background: #f0f9eb; color: #67C23A; }
      &.battery { background: #fdf6ec; color: #E6A23C; }
      &.error { background: #fef0f0; color: #f56c6c; }
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
        
        .total {
          font-size: 14px;
          color: var(--text-secondary);
          font-weight: normal;
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
    
    &.path { color: #409EFF; background: #ecf5ff; }
    &.task { color: #67C23A; background: #f0f9eb; }
    &.monitor { color: #E6A23C; background: #fdf6ec; }
    &.dispatch { color: #F56C6C; background: #fef0f0; }
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
        
        &.order { background: #ecf5ff; color: #409EFF; }
        &.dispatch { background: #f0f9eb; color: #67C23A; }
        &.path { background: #fdf6ec; color: #E6A23C; }
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