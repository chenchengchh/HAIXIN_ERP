<template>
  <div class="crm-view">
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
          <h2>CRM 客户关系管理</h2>
          <p class="subtitle">全方位客户生命周期管理，提升销售业绩与客户满意度</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus">
            新建客户
          </el-button>
        </div>
      </div>

      <!-- 销售概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon customer">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">客户总数</div>
            <div class="stat-value">8,542</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 125
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon opportunity">
            <el-icon><Star /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">商机金额</div>
            <div class="stat-value">580<span class="unit">万</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 12.5%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon contract">
            <el-icon><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本月回款</div>
            <div class="stat-value">128.5<span class="unit">万</span></div>
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon> 5.2%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon conversion">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">转化率</div>
            <div class="stat-value">32.8<span class="unit">%</span></div>
            <div class="stat-sub">线索转商机</div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 客户管理 -->
        <div class="module-card" @click="navigateToModule('customer')">
          <div class="card-icon customer">
            <el-icon><User /></el-icon>
          </div>
          <div class="card-info">
            <h3>客户管理</h3>
            <p>客户档案与公海池管理</p>
            <div class="features-list">
              <span><el-icon><List /></el-icon> 客户列表</span>
              <span><el-icon><Connection /></el-icon> 公海池</span>
              <span><el-icon><UserFilled /></el-icon> 联系人</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 销售管理 -->
        <div class="module-card" @click="navigateToModule('sales')">
          <div class="card-icon sales">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="card-info">
            <h3>销售管理</h3>
            <p>线索商机与销售预测</p>
            <div class="features-list">
              <span><el-icon><Filter /></el-icon> 线索管理</span>
              <span><el-icon><Star /></el-icon> 商机漏斗</span>
              <span><el-icon><DataAnalysis /></el-icon> 销售预测</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 订单与合同 -->
        <div class="module-card" @click="navigateToModule('order')">
          <div class="card-icon order">
            <el-icon><Document /></el-icon>
          </div>
          <div class="card-info">
            <h3>订单与合同</h3>
            <p>合同审批与订单执行</p>
            <div class="features-list">
              <span><el-icon><Stamp /></el-icon> 合同管理</span>
              <span><el-icon><Tickets /></el-icon> 销售订单</span>
              <span><el-icon><Money /></el-icon> 回款计划</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 服务支持 -->
        <div class="module-card" @click="navigateToModule('service')">
          <div class="card-icon service">
            <el-icon><Headset /></el-icon>
          </div>
          <div class="card-info">
            <h3>服务支持</h3>
            <p>售后服务与客户关怀</p>
            <div class="features-list">
              <span><el-icon><Service /></el-icon> 工单管理</span>
              <span><el-icon><Reading /></el-icon> 知识库</span>
              <span><el-icon><ChatDotRound /></el-icon> 满意度</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- LTC 核心流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          <span>LTC (Leads to Cash) 核心流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon leads">
              <el-icon><Filter /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">线索获取</div>
              <div class="step-desc">多渠道获客</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon opportunity">
              <el-icon><Star /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">商机挖掘</div>
              <div class="step-desc">需求确认/报价</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon contract">
              <el-icon><Stamp /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">签约成交</div>
              <div class="step-desc">合同签订/归档</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon delivery">
              <el-icon><Van /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">交付履约</div>
              <div class="step-desc">产品交付/验收</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon cash">
              <el-icon><Money /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">回款结算</div>
              <div class="step-desc">发票/资金回笼</div>
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
  User, 
  TrendCharts, 
  Document, 
  Headset,
  Connection,
  Filter,
  Star,
  Stamp,
  Van,
  Money,
  Right,
  Plus,
  CaretTop,
  CaretBottom,
  Trophy,
  DataLine,
  List,
  UserFilled,
  DataAnalysis,
  Tickets,
  Service,
  Reading,
  ChatDotRound
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

const navigateToModule = (module: string) => {
  router.push(`/home/crm/${module}`)
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.crm-view {
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
      
      &.customer { background: #e1f3d8; color: #67c23a; }
      &.opportunity { background: #e6f7ff; color: #1890ff; }
      &.contract { background: #fdf6ec; color: #e6a23c; }
      &.conversion { background: #f0f9eb; color: #67C23A; }
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
    
    &.customer { color: #409EFF; background: #ecf5ff; }
    &.sales { color: #E6A23C; background: #fdf6ec; }
    &.order { color: #F56C6C; background: #fef0f0; }
    &.service { color: #67C23A; background: #f0f9eb; }
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
        
        &.leads { background: #ecf5ff; color: #409EFF; }
        &.opportunity { background: #f0f9eb; color: #67C23A; }
        &.contract { background: #fdf6ec; color: #E6A23C; }
        &.delivery { background: #fef0f0; color: #F56C6C; }
        &.cash { background: #e6f7ff; color: #1890ff; }
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
