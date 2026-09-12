<template>
  <div class="les-view">
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
          <h2>LES 物流执行系统</h2>
          <p class="subtitle">Logistics Execution System - 全链路物流执行监控，确保精准高效交付</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus" @click="handleCreateWaybill">
            新建运单
          </el-button>
        </div>
      </div>
      
      <!-- 物流概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon waybill">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日运单</div>
            <div class="stat-value">{{ todayWaybillCount }}<span class="unit">单</span></div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon vehicle">
            <el-icon><Van /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">在途车辆</div>
            <div class="stat-value">{{ inTransitVehicleCount }}<span class="unit">辆</span></div>
            <div class="stat-sub">空闲 {{ idleVehicleCount }} 辆</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon abnormal">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">异常签收</div>
            <div class="stat-value warning">{{ exceptionSignCount }}<span class="unit">单</span></div>
            <div class="stat-sub">需人工介入</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon delivery">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">准时交付率</div>
            <div class="stat-value">{{ onTimeRate }}<span class="unit">%</span></div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 运输管理 -->
        <div class="module-card" @click="navigateToModule('transport-management')">
          <div class="card-icon transport">
            <el-icon><Van /></el-icon>
          </div>
          <div class="card-info">
            <h3>运输管理</h3>
            <p>运单生成、车辆调度与路线规划</p>
            <div class="features-list">
              <span><el-icon><List /></el-icon> 运单管理</span>
              <span><el-icon><Coordinate /></el-icon> 智能调度</span>
              <span><el-icon><User /></el-icon> 承运商</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 在途监控 -->
        <div class="module-card" @click="navigateToModule('in-transit-monitoring')">
          <div class="card-icon monitor">
            <el-icon><MapLocation /></el-icon>
          </div>
          <div class="card-info">
            <h3>在途监控</h3>
            <p>车辆实时定位与轨迹回放监控</p>
            <div class="features-list">
              <span><el-icon><Location /></el-icon> 实时定位</span>
              <span><el-icon><VideoPlay /></el-icon> 轨迹回放</span>
              <span><el-icon><Bell /></el-icon> 异常报警</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 签收管理 -->
        <div class="module-card" @click="navigateToModule('sign-management')">
          <div class="card-icon sign">
            <el-icon><Checked /></el-icon>
          </div>
          <div class="card-info">
            <h3>签收管理</h3>
            <p>电子回单与运费结算管理</p>
            <div class="features-list">
              <span><el-icon><DocumentChecked /></el-icon> 电子回单</span>
              <span><el-icon><Star /></el-icon> 满意度</span>
              <span><el-icon><Money /></el-icon> 运费结算</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 物流分析 -->
        <div class="module-card" @click="navigateToModule('logistics-analysis')">
          <div class="card-icon analysis">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="card-info">
            <h3>物流分析</h3>
            <p>多维度物流数据统计与分析</p>
            <div class="features-list">
              <span><el-icon><DataLine /></el-icon> 成本分析</span>
              <span><el-icon><Stopwatch /></el-icon> 时效分析</span>
              <span><el-icon><PieChart /></el-icon> 运力分布</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 物流执行闭环 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          <span>物流执行闭环</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon plan">
              <el-icon><List /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">计划调度</div>
              <div class="step-desc">订单整合/车辆指派</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon execute">
              <el-icon><Van /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">运输执行</div>
              <div class="step-desc">提货/干线/配送</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon track">
              <el-icon><MapLocation /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">全程监控</div>
              <div class="step-desc">定位/轨迹/异常</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon settle">
              <el-icon><Money /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">签收结算</div>
              <div class="step-desc">回单/评价/对账</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Van, 
  MapLocation, 
  Checked, 
  TrendCharts, 
  ArrowRight, 
  Connection,
  Right,
  List,
  Money,
  Plus,
  CaretTop,
  Warning,
  Timer,
  Coordinate,
  User,
  Location,
  VideoPlay,
  Bell,
  DocumentChecked,
  Star,
  DataLine,
  Stopwatch,
  PieChart
} from '@element-plus/icons-vue'
import useLesStore from '../../stores/les'
import * as types from '../../types/les'

const route = useRoute()
const router = useRouter()
const store = useLesStore()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'transport-management': '运输管理',
  'in-transit-monitoring': '在途监控',
  'sign-management': '签收管理',
  'logistics-analysis': '物流分析'
}

// 当前标签页名称
const currentTabLabel = computed(() => {
  const moduleName = route.path.split('/').pop() || ''
  return tabLabelMap[moduleName] || '功能详情'
})

const navigateToModule = (modulePath: string) => {
  router.push(`/home/les/${modulePath}`)
}

const handleCreateWaybill = () => {
  router.push({ path: '/home/les/transport-management', query: { create: '1' } })
}

const todayWaybillCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return store.transportPlans.filter(p => (p.createTime || '').slice(0, 10) === today).length
})

const inTransitVehicleCount = computed(() => {
  const ids = new Set(
    store.transportPlans
      .filter(p => p.status === types.TransportPlanStatus.IN_TRANSIT && p.vehicleId)
      .map(p => p.vehicleId)
  )
  return ids.size
})

const idleVehicleCount = computed(() => {
  return store.vehicles.filter(v => v.status === 'available').length
})

const exceptionSignCount = computed(() => {
  return store.signVouchers.filter(v => v.onTimeStatus === types.OnTimeStatus.DELAYED).length
})

const onTimeRate = computed(() => {
  return Number((store.transportStats?.averageOnTimeRate || 0).toFixed(1))
})

const goBack = () => {
  router.back()
}

onMounted(async () => {
  await Promise.all([
    store.fetchTransportPlans(),
    store.fetchVehicles(),
    store.fetchSignVouchers(),
    store.fetchTransportStats()
  ])
})
</script>

<style scoped lang="scss">
.les-view {
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
    justify-content: space-between;
    align-items: center;
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
      
      &.waybill { background: #ecf5ff; color: #409eff; }
      &.vehicle { background: #fdf6ec; color: #e6a23c; }
      &.abnormal { background: #fef0f0; color: #f56c6c; }
      &.delivery { background: #f0f9eb; color: #67c23a; }
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
        
        &.warning { color: #f56c6c; }
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
    
    &.transport { color: #409EFF; background: #ecf5ff; }
    &.monitor { color: #67c23a; background: #f0f9eb; }
    &.sign { color: #e6a23c; background: #fdf6ec; }
    &.analysis { color: #8b5cf6; background: #f5f3ff; }
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
        &.execute { background: #f0f9eb; color: #67c23a; }
        &.track { background: #fdf6ec; color: #e6a23c; }
        &.settle { background: #f5f3ff; color: #8b5cf6; }
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
