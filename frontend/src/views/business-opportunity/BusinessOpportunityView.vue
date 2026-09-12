<template>
  <div class="business-opportunity-view">
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
          <h2>商机管理</h2>
          <p class="subtitle">全流程商机跟踪与转化管理系统</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus">
            新建商机
          </el-button>
        </div>
      </div>

      <!-- 商机概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon opportunity">
            <el-icon><files /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本月新增商机</div>
            <div class="stat-value">128<span class="unit">个</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 15.2%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon amount">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本月商机金额</div>
            <div class="stat-value">235.6<span class="unit">万</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 8.5%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon conversion">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">商机转化率</div>
            <div class="stat-value">38<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 2.1%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon follow">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待跟进商机</div>
            <div class="stat-value warning">45<span class="unit">个</span></div>
            <div class="stat-sub">今日需跟进 12 个</div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 商机录入 -->
        <div class="module-card" @click="navigateToModule('opportunity-entry')">
          <div class="card-icon entry">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="card-info">
            <h3>商机录入</h3>
            <p>多渠道商机线索采集与录入</p>
            <div class="features-list">
              <span><el-icon><Mouse /></el-icon> 手动录入</span>
              <span><el-icon><Upload /></el-icon> 批量导入</span>
              <span><el-icon><Connection /></el-icon> 线索转化</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 商机跟踪 -->
        <div class="module-card" @click="navigateToModule('opportunity-tracking')">
          <div class="card-icon tracking">
            <el-icon><Aim /></el-icon>
          </div>
          <div class="card-info">
            <h3>商机跟踪</h3>
            <p>全生命周期商机跟进与阶段管理</p>
            <div class="features-list">
              <span><el-icon><Operation /></el-icon> 阶段管理</span>
              <span><el-icon><Notebook /></el-icon> 跟进记录</span>
              <span><el-icon><AlarmClock /></el-icon> 超时提醒</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 商机分析 -->
        <div class="module-card" @click="navigateToModule('opportunity-analysis')">
          <div class="card-icon analysis">
            <el-icon><PieChart /></el-icon>
          </div>
          <div class="card-info">
            <h3>商机分析</h3>
            <p>多维度商机数据分析与洞察</p>
            <div class="features-list">
              <span><el-icon><Filter /></el-icon> 漏斗分析</span>
              <span><el-icon><TrendCharts /></el-icon> 趋势分析</span>
              <span><el-icon><User /></el-icon> 绩效分析</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 商机预测 -->
        <div class="module-card" @click="navigateToModule('opportunity-forecast')">
          <div class="card-icon forecast">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="card-info">
            <h3>商机预测</h3>
            <p>基于AI的销售业绩智能预测</p>
            <div class="features-list">
              <span><el-icon><Money /></el-icon> 金额预测</span>
              <span><el-icon><Lightning /></el-icon> 成交概率</span>
              <span><el-icon><Document /></el-icon> 预测报表</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    
      <!-- 商机全流程闭环 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          <span>商机全流程闭环</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon lead">
              <el-icon><Search /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">线索获取</div>
              <div class="step-desc">多渠道获客</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon opportunity">
              <el-icon><Files /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">商机立项</div>
              <div class="step-desc">确认意向需求</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon negotiation">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">商务谈判</div>
              <div class="step-desc">方案报价沟通</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon closing">
              <el-icon><Stamp /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">成交签约</div>
              <div class="step-desc">合同签订回款</div>
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
  Files, 
  Money, 
  DataAnalysis, 
  Timer,
  EditPen,
  Aim,
  PieChart,
  DataLine,
  Mouse,
  Upload,
  Connection,
  Operation,
  Notebook,
  AlarmClock,
  Filter,
  TrendCharts,
  User,
  Lightning,
  Document,
  Right,
  Search,
  ChatLineRound,
  Stamp,
  CaretTop,
  CaretBottom
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

const navigateToModule = (module: string) => {
  router.push(`/home/business-opportunity/${module}`)
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.business-opportunity-view {
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
      
      &.opportunity { background: #ecf5ff; color: #409eff; }
      &.amount { background: #fdf6ec; color: #e6a23c; }
      &.conversion { background: #f0f9eb; color: #67c23a; }
      &.follow { background: #f4f4f5; color: #909399; }
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
    
    &.entry { color: #409EFF; background: #ecf5ff; }
    &.tracking { color: #e6a23c; background: #fdf6ec; }
    &.analysis { color: #67c23a; background: #f0f9eb; }
    &.forecast { color: #8b5cf6; background: #f5f3ff; }
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
        
        &.lead { background: #ecf5ff; color: #409EFF; }
        &.opportunity { background: #fdf6ec; color: #e6a23c; }
        &.negotiation { background: #f0f9eb; color: #67c23a; }
        &.closing { background: #f5f3ff; color: #8b5cf6; }
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
