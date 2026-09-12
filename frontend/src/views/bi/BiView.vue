<template>
  <div class="bi-view">
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
          <h2>BI 商业智能</h2>
          <p class="subtitle">数据驱动决策，智能引领未来</p>
        </div>
        <div class="header-actions">
          <el-button type="primary">
            <el-icon><Document /></el-icon>
            新建报表
          </el-button>
        </div>
      </div>
      
      <!-- 数据概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon source">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">数据源数量</div>
            <div class="stat-value">12</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 2 个
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon etl">
            <el-icon><Filter /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">ETL任务</div>
            <div class="stat-value">36</div>
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon> 1 个
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon report">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">报表数量</div>
            <div class="stat-value">89</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 5 个
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon user">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">活跃用户</div>
            <div class="stat-value">236</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 18 人
            </div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 数据集成 -->
        <div class="module-card" @click="navigateToModule('data-integration')">
          <div class="card-icon integration">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="card-info">
            <h3>数据集成</h3>
            <p>多源异构数据采集与清洗</p>
            <div class="features-list">
              <span><el-icon><Link /></el-icon> 多源接入</span>
              <span><el-icon><Filter /></el-icon> 清洗转换</span>
              <span><el-icon><Coin /></el-icon> 数据仓库</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 数据分析 -->
        <div class="module-card" @click="navigateToModule('data-analysis')">
          <div class="card-icon analysis">
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="card-info">
            <h3>数据分析</h3>
            <p>多维数据挖掘与即席查询</p>
            <div class="features-list">
              <span><el-icon><Cpu /></el-icon> OLAP分析</span>
              <span><el-icon><Search /></el-icon> 即席查询</span>
              <span><el-icon><TrendCharts /></el-icon> 趋势预测</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 可视化报表 -->
        <div class="module-card" @click="navigateToModule('visualization')">
          <div class="card-icon visualization">
            <el-icon><PieChart /></el-icon>
          </div>
          <div class="card-info">
            <h3>可视化报表</h3>
            <p>丰富图表展示与仪表盘</p>
            <div class="features-list">
              <span><el-icon><Monitor /></el-icon> 仪表盘</span>
              <span><el-icon><Document /></el-icon> 报表设计</span>
              <span><el-icon><Share /></el-icon> 自动推送</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 决策支持 -->
        <div class="module-card" @click="navigateToModule('decision-support')">
          <div class="card-icon decision">
            <el-icon><Aim /></el-icon>
          </div>
          <div class="card-info">
            <h3>决策支持</h3>
            <p>智能预警与辅助决策建议</p>
            <div class="features-list">
              <span><el-icon><Bell /></el-icon> 智能预警</span>
              <span><el-icon><Reading /></el-icon> 决策建议</span>
              <span><el-icon><Odometer /></el-icon> KPI监控</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 数据处理流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Operation /></el-icon>
          <span>数据处理流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon source">
              <el-icon><Coin /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">数据源</div>
              <div class="step-desc">业务系统/日志/IoT</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon etl">
              <el-icon><Filter /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">ETL处理</div>
              <div class="step-desc">抽取/转换/加载</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon dw">
              <el-icon><DataBoard /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">数据仓库</div>
              <div class="step-desc">数据集市/ODS</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon bi">
              <el-icon><DataLine /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">分析展现</div>
              <div class="step-desc">报表/仪表盘/大屏</div>
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
  ArrowRight, Connection, Link, Filter, Coin, 
  DataAnalysis, Cpu, Search, TrendCharts,
  PieChart, Monitor, Document, Share,
  Aim, Bell, Reading, Odometer,
  Operation, Right, DataBoard, DataLine,
  User, CircleCheck, CaretTop, CaretBottom,
  Check, Refresh
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 路由实例
const router = useRouter()
const route = useRoute()

// 判断是否是子路由
const isChildRoute = computed(() => {
  return route.matched.length > 2
})

// 返回上一级
const goBack = () => {
  router.push('/home/bi')
}

// 导航到模块详情页
const navigateToModule = (module: string) => {
  router.push(`/home/bi/${module}`)
}
</script>

<style scoped lang="scss">
.bi-view {
  min-height: 100%;
  background-color: var(--bg-color-page);
  padding: 24px;
}

.main-dashboard {
  max-width: 1600px;
  margin: 0 auto;
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
  
  .header-actions {
    display: flex;
    gap: 12px;
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
      
      &.source { background: #e6f7ff; color: #1890ff; }
      &.etl { background: #fff7e6; color: #fa8c16; }
      &.report { background: #f6ffed; color: #52c41a; }
      &.user { background: #f5f3ff; color: #722ed1; }
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
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
    border-color: var(--primary-light);

    .card-icon {
      transform: scale(1.1) rotate(5deg);
    }
    
    .arrow-icon {
      transform: translateX(4px);
      color: var(--primary-color);
    }
  }
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  transition: all 0.3s ease;
  font-size: 28px;
  
  &.integration {
    background: #e6f7ff;
    color: #1890ff;
  }
  
  &.analysis {
    background: #f5f3ff;
    color: #722ed1;
  }
  
  &.visualization {
    background: #fff0f6;
    color: #eb2f96;
  }
  
  &.decision {
    background: #f6ffed;
    color: #52c41a;
  }
}

.card-info {
  flex: 1;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0 0 8px 0;
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
  border-top: 1px solid var(--border-color-light);
  padding-top: 16px;
  margin-top: auto;
  
  .action-text {
    font-size: 14px;
    color: var(--text-secondary);
  }
  
  .arrow-icon {
    transition: all 0.3s ease;
    color: var(--text-placeholder);
    font-size: 16px;
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
        
        &.source {
          background: #e6f7ff;
          color: #1890ff;
        }
        &.etl {
          background: #fff7e6;
          color: #fa8c16;
        }
        &.dw {
          background: #f6ffed;
          color: #52c41a;
        }
        &.bi {
          background: #f5f3ff;
          color: #722ed1;
        }
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



@media (max-width: 1024px) {
  .process-flow .flow-steps {
    flex-wrap: wrap;
    gap: 24px;
    padding: 0 !important;
    
    .flow-step {
      width: calc(50% - 12px);
      
      .step-arrow {
        transform: rotate(90deg);
        margin: 12px 0;
      }
    }
  }
}

@media (max-width: 768px) {
  .bi-view {
    padding: 16px;
  }
  
  .view-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .stats-overview {
    grid-template-columns: 1fr 1fr;
  }
  
  .module-cards {
    grid-template-columns: 1fr;
  }
  
  .process-flow .flow-steps {
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
