<template>
  <div class="mes-view dashboard-container">
    <!-- 子路由视图 -->
    <div v-if="isChildRoute" class="child-view-container">
      <div class="page-header">
        <el-page-header @back="goBack">
          <template #content>
            <span class="text-large font-600 mr-3">{{ currentRouteName }}</span>
          </template>
        </el-page-header>
      </div>
      <router-view />
    </div>
    
    <!-- 主视图 -->
    <div v-else class="main-dashboard">
      <div class="view-header">
        <div class="header-content">
          <h2>MES 生产制造执行系统</h2>
          <p class="subtitle">连接计划与现场，实现生产过程的数字化、透明化管控</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus">
            新建工单
          </el-button>
        </div>
      </div>
      
      <!-- 数据概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon production">
            <el-icon><VideoPlay /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日产量</div>
            <div class="stat-value">2,450</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 5.2%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon oee">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">设备OEE</div>
            <div class="stat-value">85.2<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 1.5%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon wip">
            <el-icon><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">在制工单</div>
            <div class="stat-value warning">128</div>
            <div class="stat-sub">紧急订单 5</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon alert">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">异常报警</div>
            <div class="stat-value danger">3</div>
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon> 2
            </div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 生产执行 -->
        <div class="module-card" @click="navigateToModule('execution')">
          <div class="card-icon execution">
            <el-icon><VideoPlay /></el-icon>
          </div>
          <div class="card-info">
            <h3>生产执行</h3>
            <p>工单管理与生产调度执行</p>
            <div class="features-list">
              <span><el-icon><Tickets /></el-icon> 工单管理</span>
              <span><el-icon><User /></el-icon> 工序派工</span>
              <span><el-icon><Switch /></el-icon> 投料控制</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 生产报工 -->
        <div class="module-card" @click="navigateToModule('reporting')">
          <div class="card-icon reporting">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="card-info">
            <h3>生产报工</h3>
            <p>实时采集生产数据与进度</p>
            <div class="features-list">
              <span><el-icon><Timer /></el-icon> 产量汇报</span>
              <span><el-icon><CircleCheck /></el-icon> 不良品记录</span>
              <span><el-icon><Warning /></el-icon> 停机上报</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 过程监控 -->
        <div class="module-card" @click="navigateToModule('monitoring')">
          <div class="card-icon monitoring">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="card-info">
            <h3>过程监控</h3>
            <p>生产现场全流程透明化监控</p>
            <div class="features-list">
              <span><el-icon><DataLine /></el-icon> 进度看板</span>
              <span><el-icon><Cpu /></el-icon> 设备状态</span>
              <span><el-icon><WarningFilled /></el-icon> 安灯呼叫</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 在制品管理 -->
        <div class="module-card" @click="navigateToModule('wip')">
          <div class="card-icon wip">
            <el-icon><Box /></el-icon>
          </div>
          <div class="card-info">
            <h3>在制品管理</h3>
            <p>产线在制品流转与库存管控</p>
            <div class="features-list">
              <span><el-icon><Refresh /></el-icon> 工序流转</span>
              <span><el-icon><Search /></el-icon> WIP盘点</span>
              <span><el-icon><PieChart /></el-icon> 呆滞分析</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 数据采集 -->
        <div class="module-card" @click="navigateToModule('data-collection')">
          <div class="card-icon data">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="card-info">
            <h3>数据采集</h3>
            <p>IoT设备互联与自动采集</p>
            <div class="features-list">
              <span><el-icon><Link /></el-icon> 设备联网</span>
              <span><el-icon><Odometer /></el-icon> 参数采集</span>
              <span><el-icon><SetUp /></el-icon> 能源监控</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 业务流程可视化 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Operation /></el-icon>
          <span>制造执行全流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon plan">
              <el-icon><Tickets /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">计划下达</div>
              <div class="step-desc">工单接收/分解</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon dispatch">
              <el-icon><User /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">生产派工</div>
              <div class="step-desc">人员/设备分配</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon execute">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">执行反馈</div>
              <div class="step-desc">报工/安灯/采集</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon complete">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">完工入库</div>
              <div class="step-desc">质检/入库交接</div>
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
  ArrowRight, VideoPlay, EditPen, Monitor, Box, Connection,
  Tickets, User, Timer, CircleCheck, Warning,
  DataLine, Cpu, WarningFilled, Refresh, Search, PieChart,
  Link, Odometer, SetUp, Plus, CaretTop, CaretBottom,
  Right, Switch, Operation
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 判断是否为子路由
const isChildRoute = computed(() => {
  return route.name !== 'mes'
})

// 获取当前路由名称
const currentRouteName = computed(() => {
  return route.meta.title || 'MES功能'
})

// 导航到模块
const navigateToModule = (module: string) => {
  // 映射模块名到路由路径
  const pathMap: Record<string, string> = {
    'execution': 'execution',
    'reporting': 'reporting',
    'monitoring': 'monitoring',
    'wip': 'wip',
    'data-collection': 'data-collection'
  }
  const path = pathMap[module] || module
  router.push(`/home/mes/${path}`)
}

// 返回上一级
const goBack = () => {
  router.push('/home/mes')
}
</script>

<style scoped lang="scss">
.mes-view {
  padding: 24px;
  min-height: 100%;
  background-color: var(--bg-color-page);
  
  .child-view-container {
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
      
      &.production { background: #e6f7ff; color: #1890ff; }
      &.oee { background: #f0f9eb; color: #67C23A; }
      &.wip { background: #fdf6ec; color: #e6a23c; }
      &.alert { background: #fef0f0; color: #f56c6c; }
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
    
    &.execution { color: #409EFF; background: #ecf5ff; }
    &.reporting { color: #67C23A; background: #f0f9eb; }
    &.monitoring { color: #E6A23C; background: #fdf6ec; }
    &.wip { color: #9333ea; background: #f3e8ff; }
    &.data { color: #F56C6C; background: #fef0f0; }
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
        &.dispatch { background: #f0f9eb; color: #67C23A; }
        &.execute { background: #fdf6ec; color: #E6A23C; }
        &.complete { background: #f3e8ff; color: #9333ea; }
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
