<template>
  <div class="scm-view">
    <!-- 子路由视图 -->
    <div v-if="isChildRoute" class="child-router-view">
      <div class="page-header">
        <el-page-header @back="goBack">
          <template #content>
            <span class="text-large font-600 mr-3">{{ childTitle }}</span>
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
          <h2>SCM 供应链管理</h2>
          <p class="subtitle">端到端供应链协同，实现供需平衡与高效交付</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="DataAnalysis" @click="navigateToModule('demand-forecast')">
            需求预测
          </el-button>
        </div>
      </div>
      
      <!-- 供应链概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon plan">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">计划达成率</div>
            <div class="stat-value">92.5<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 1.2%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon inventory">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">库存周转天数</div>
            <div class="stat-value">28.5<span class="unit">天</span></div>
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon> 2.1天
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon delivery">
            <el-icon><Van /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">OTD准时交付</div>
            <div class="stat-value">95.8<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 0.5%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon cost">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">供应链成本</div>
            <div class="stat-value">12.3<span class="unit">%</span></div>
            <div class="stat-sub">占营收比例</div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 需求计划 -->
        <div class="module-card" @click="navigateToModule('demand-forecast')">
          <div class="card-icon forecast">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="card-info">
            <h3>需求计划</h3>
            <p>精准预测与需求管理</p>
            <div class="features-list">
              <span><el-icon><TrendCharts /></el-icon> 销量预测</span>
              <span><el-icon><List /></el-icon> 需求计划</span>
              <span><el-icon><Share /></el-icon> S&OP协同</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 供应计划 -->
        <div class="module-card" @click="navigateToModule('planning')">
          <div class="card-icon plan">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="card-info">
            <h3>供应计划</h3>
            <p>资源优化与排程计算</p>
            <div class="features-list">
              <span><el-icon><Connection /></el-icon> MRP运算</span>
              <span><el-icon><List /></el-icon> 生产计划</span>
              <span><el-icon><Box /></el-icon> 物料平衡</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 生产完工事实（B6 闭环：MES完工回流SCM） -->
        <div class="module-card" @click="navigateToModule('production-completion')">
          <div class="card-icon completion">
            <el-icon><CircleCheck /></el-icon>
          </div>
          <div class="card-info">
            <h3>生产完工事实</h3>
            <p>MES完工回流制造闭环</p>
            <div class="features-list">
              <span><el-icon><CircleCheck /></el-icon> 完工查询</span>
              <span><el-icon><Connection /></el-icon> 生产链追踪</span>
              <span><el-icon><DataLine /></el-icon> 闭环验证</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 库存优化 -->
        <div class="module-card" @click="navigateToModule('inventory-optimization')">
          <div class="card-icon inventory">
            <el-icon><Box /></el-icon>
          </div>
          <div class="card-info">
            <h3>库存优化</h3>
            <p>库存策略与健康度分析</p>
            <div class="features-list">
              <span><el-icon><DataLine /></el-icon> 安全库存</span>
              <span><el-icon><Warning /></el-icon> 呆滞分析</span>
              <span><el-icon><Refresh /></el-icon> 补货策略</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 供应链塔台 -->
        <div class="module-card" @click="navigateToModule('control-tower')">
          <div class="card-icon tower">
            <el-icon><Location /></el-icon>
          </div>
          <div class="card-info">
            <h3>供应链塔台</h3>
            <p>全链路可视化监控</p>
            <div class="features-list">
              <span><el-icon><MapLocation /></el-icon> 实时监控</span>
              <span><el-icon><WarningFilled /></el-icon> 异常预警</span>
              <span><el-icon><DataBoard /></el-icon> 绩效看板</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 物流协同 -->
        <div class="module-card" @click="navigateToModule('logistics-collaboration')">
          <div class="card-icon deliver">
            <el-icon><Van /></el-icon>
          </div>
          <div class="card-info">
            <h3>物流协同</h3>
            <p>运单跟踪与线路优化</p>
            <div class="features-list">
              <span><el-icon><Van /></el-icon> 运单管理</span>
              <span><el-icon><Location /></el-icon> 实时追踪</span>
              <span><el-icon><Share /></el-icon> 路径优化</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 供应链可视化 -->
        <div class="module-card" @click="navigateToModule('supply-chain-visualization')">
          <div class="card-icon plan">
            <el-icon><DataBoard /></el-icon>
          </div>
          <div class="card-info">
            <h3>供应链可视化</h3>
            <p>全链路数据透视与分析</p>
            <div class="features-list">
              <span><el-icon><DataBoard /></el-icon> KPI看板</span>
              <span><el-icon><Warning /></el-icon> 异常预警</span>
              <span><el-icon><DataLine /></el-icon> 对账报表</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 端到端供应链流程 -->
      <div class="process-section">
        <div class="section-header">
          <h3>
            <i class="el-icon"><Connection /></i>
            端到端供应链流程
          </h3>
        </div>
        <div class="process-flow">
          <div class="flow-step" @click="navigateToProcess('demand')">
            <div class="step-icon forecast">
              <i class="el-icon"><DataAnalysis /></i>
            </div>
            <div class="step-content">
              <div class="step-title">需求计划</div>
              <div class="step-desc">预测与S&OP</div>
            </div>
            <div class="step-arrow">
              <i class="el-icon"><Right /></i>
            </div>
          </div>
          <div class="flow-step" @click="navigateToProcess('source')">
            <div class="step-icon source">
              <i class="el-icon"><ShoppingCart /></i>
            </div>
            <div class="step-content">
              <div class="step-title">寻源采购</div>
              <div class="step-desc">供应商管理</div>
            </div>
            <div class="step-arrow">
              <i class="el-icon"><Right /></i>
            </div>
          </div>
          <div class="flow-step" @click="navigateToProcess('make')">
            <div class="step-icon make">
              <i class="el-icon"><Cpu /></i>
            </div>
            <div class="step-content">
              <div class="step-title">生产制造</div>
              <div class="step-desc">排程与执行</div>
            </div>
            <div class="step-arrow">
              <i class="el-icon"><Right /></i>
            </div>
          </div>
          <div class="flow-step" @click="navigateToProcess('deliver')">
            <div class="step-icon deliver">
              <i class="el-icon"><Van /></i>
            </div>
            <div class="step-content">
              <div class="step-title">交付物流</div>
              <div class="step-desc">仓储与配送</div>
            </div>
            <div class="step-arrow">
              <i class="el-icon"><Right /></i>
            </div>
          </div>
          <div class="flow-step" @click="navigateToProcess('service')">
            <div class="step-icon service">
              <i class="el-icon"><Headset /></i>
            </div>
            <div class="step-content">
              <div class="step-title">售后服务</div>
              <div class="step-desc">退换货与维修</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowRight,
  DataAnalysis,
  ShoppingCart,
  Cpu,
  Van,
  Headset,
  Connection,
  Right,
  User,
  List,
  TrendCharts,
  Share,
  Location,
  Box,
  Money,
  CaretTop,
  CaretBottom,
  DataLine,
  Warning,
  Refresh,
  MapLocation,
  WarningFilled,
  DataBoard,
  CircleCheck
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

const childTitle = computed(() => {
  return (route.meta?.title as string) || '功能详情'
})

/**
 * 跳转到SCM子模块页面
 * @param module 子模块路径（如 demand-forecast / planning 等）
 */
const navigateToModule = (module: string) => {
  router.push(`/home/scm/${module}`)
}

/**
 * 跳转到端到端流程对应模块
 * @param step 业务流程步骤标识
 */
const navigateToProcess = (step: string) => {
  let path = ''
  switch (step) {
    case 'demand':
      path = '/home/scm/demand-forecast'
      break
    case 'source':
      path = '/home/srm/purchase-automation'
      break
    case 'make':
      path = '/home/scm/planning'
      break
    case 'deliver':
      path = '/home/wms/outbound'
      break
    case 'service':
      path = '/home/crm/service'
      break
    default:
      return
  }
  router.push(path)
}

const goBack = () => {
  const backTo = route.meta?.backTo as string | undefined
  if (backTo) {
    router.push(backTo)
    return
  }
  router.push('/home/scm')
}
</script>

<style scoped lang="scss">
.scm-view {
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
      
      &.plan { background: #e1f3d8; color: #67c23a; }
      &.inventory { background: #e6f7ff; color: #1890ff; }
      &.delivery { background: #fdf6ec; color: #e6a23c; }
      &.cost { background: #fef0f0; color: #f56c6c; }
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
    
    &.forecast { color: #409EFF; background: #ecf5ff; }
    &.plan { color: #E6A23C; background: #fdf6ec; }
    &.inventory { color: #67C23A; background: #f0f9eb; }
    &.logistics { color: #F56C6C; background: #fef0f0; }
    &.tower { color: #909399; background: #f4f4f5; }
    &.completion { color: #13c2c2; background: #e6fffb; }
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

.process-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  
  .section-header {
    margin-bottom: 24px;
    
    h3 {
      font-size: 18px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
      
      .el-icon {
        color: var(--primary-color);
      }
    }
  }
  
  .process-flow {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    
    .flow-step {
      display: flex;
      align-items: center;
      flex: 1;
      cursor: pointer;
      transition: all 0.3s ease;
      padding: 12px;
      border-radius: 8px;
      
      &:hover {
        background-color: var(--bg-color-page);
        transform: translateY(-2px);
        
        .step-icon {
          transform: scale(1.1);
          box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }
      }
      
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
        transition: all 0.3s ease;
        
        &.forecast { background: #ecf5ff; color: #409EFF; }
        &.source { background: #f0f9eb; color: #67C23A; }
        &.make { background: #fdf6ec; color: #E6A23C; }
        &.deliver { background: #fef0f0; color: #F56C6C; }
        &.service { background: #e6f7ff; color: #1890ff; }
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
  .process-flow {
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
