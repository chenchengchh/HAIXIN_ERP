<template>
  <div class="srm-view">
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
          <h2>SRM 供应商关系管理</h2>
          <p class="subtitle">构建高效、透明、协同的供应链体系</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus">
            发布询价
          </el-button>
        </div>
      </div>
      
      <!-- 采购概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon supplier">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">合作供应商</div>
            <div class="stat-value">342</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 12 家
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon purchase">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本月采购额</div>
            <div class="stat-value">2,580<span class="unit">万</span></div>
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon> 5.2%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon delivery">
            <el-icon><Van /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">准时交付率</div>
            <div class="stat-value">96.8<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 0.3%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon warning">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">风险预警</div>
            <div class="stat-value danger">3</div>
            <div class="stat-sub">资质过期/交期延误</div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 供应商准入管理 -->
        <div class="module-card" @click="navigateToModule('supplier-access')">
          <div class="card-icon access">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="card-info">
            <h3>供应商准入</h3>
            <p>全生命周期资质管理</p>
            <div class="features-list">
              <span><el-icon><CircleCheck /></el-icon> 资质审核</span>
              <span><el-icon><Rank /></el-icon> 分级管理</span>
              <span><el-icon><Remove /></el-icon> 黑白名单</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 采购流程自动化 -->
        <div class="module-card" @click="navigateToModule('purchase-automation')">
          <div class="card-icon process">
            <el-icon><ShoppingCart /></el-icon>
          </div>
          <div class="card-info">
            <h3>采购流程</h3>
            <p>从寻源到结算的自动化闭环</p>
            <div class="features-list">
              <span><el-icon><Document /></el-icon> 询价比价</span>
              <span><el-icon><Tickets /></el-icon> 订单管理</span>
              <span><el-icon><Money /></el-icon> 对账结算</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 供应商协同 -->
        <div class="module-card" @click="navigateToModule('collaboration')">
          <div class="card-icon collab">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="card-info">
            <h3>供应商协同</h3>
            <p>信息共享与高效协作</p>
            <div class="features-list">
              <span><el-icon><Bell /></el-icon> 订单协同</span>
              <span><el-icon><Box /></el-icon> 送货协同</span>
              <span><el-icon><Warning /></el-icon> 质量协同</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 绩效与风险 -->
        <div class="module-card" @click="navigateToModule('performance-risk')">
          <div class="card-icon risk">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="card-info">
            <h3>绩效与风险</h3>
            <p>数据驱动的供应商评价</p>
            <div class="features-list">
              <span><el-icon><TrendCharts /></el-icon> KPI考核</span>
              <span><el-icon><WarningFilled /></el-icon> 风险监控</span>
              <span><el-icon><Medal /></el-icon> 评级调整</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 采购业务全流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Link /></el-icon>
          <span>采购业务全流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon access">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">准入认证</div>
              <div class="step-desc">注册/审核/考察</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon source">
              <el-icon><Search /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">寻源定价</div>
              <div class="step-desc">询价/招标/合同</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon order">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">订单履约</div>
              <div class="step-desc">发货/收货/质检</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon finance">
              <el-icon><Money /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">对账结算</div>
              <div class="step-desc">开票/付款/评价</div>
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
  Plus,
  OfficeBuilding,
  Money,
  Van,
  Warning,
  UserFilled,
  CircleCheck,
  Rank,
  Remove,
  ShoppingCart,
  Document,
  Tickets,
  Connection,
  Bell,
  Box,
  DataLine,
  TrendCharts,
  WarningFilled,
  Medal,
  Link,
  Right,
  Search,
  CaretTop,
  CaretBottom
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

const navigateToModule = (module: string) => {
  let path = ''
  switch (module) {
    case 'supplier-access':
      path = '/home/srm/supplier-access'
      break
    case 'procurement-process':
      path = '/home/srm/purchase-automation'
      break
    case 'supplier-collaboration':
      path = '/home/srm/collaboration'
      break
    case 'performance-risk':
      path = '/home/srm/performance-risk'
      break
    default:
      path = `/home/srm/${module}`
  }
  router.push(path)
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.srm-view {
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
      
      &.supplier { background: #e1f3d8; color: #67c23a; }
      &.purchase { background: #e6f7ff; color: #1890ff; }
      &.delivery { background: #f0f9eb; color: #67C23A; }
      &.warning { background: #fef0f0; color: #f56c6c; }
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
    
    &.access { color: #409EFF; background: #ecf5ff; }
    &.process { color: #E6A23C; background: #fdf6ec; }
    &.collab { color: #67C23A; background: #f0f9eb; }
    &.risk { color: #F56C6C; background: #fef0f0; }
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
        
        &.access { background: #ecf5ff; color: #409EFF; }
        &.source { background: #f0f9eb; color: #67C23A; }
        &.order { background: #fdf6ec; color: #E6A23C; }
        &.finance { background: #fef0f0; color: #F56C6C; }
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