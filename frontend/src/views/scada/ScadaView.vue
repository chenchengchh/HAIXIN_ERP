<template>
  <div class="scada-view">
    <!-- 子路由视图 -->
    <div v-if="isChildRoute" class="child-router-view">
      <div class="page-header">
        <el-page-header @back="goBack">
          <template #content>
            <span class="text-large font-600 mr-3">{{ pageTitle }}</span>
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
          <h2>SCADA 监控与数据采集</h2>
          <p class="subtitle">实时监控工业现场，精准掌握设备运行状态</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Monitor" @click="navigateToModule('realtime-monitoring/flowchart')">
            监控大屏
          </el-button>
        </div>
      </div>

      <!-- 统计数据卡片 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon device">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">设备总数</div>
            <div class="stat-value">{{ stats.totalDevices }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon online">
            <el-icon><Link /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">在线设备</div>
            <div class="stat-value">{{ stats.onlineDevices }}</div>
            <div class="stat-sub">在线率 {{ stats.onlineDevicesRate }}%</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon alarm">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">当前报警</div>
            <div class="stat-value warning">{{ stats.activeAlarms }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon point">
            <el-icon><Aim /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">采集点位</div>
            <div class="stat-value">{{ stats.collectionPoints }}</div>
          </div>
        </div>
      </div>
      
      <!-- 核心功能模块 -->
      <div class="module-cards">
        <!-- 数据采集 -->
        <div class="module-card" @click="navigateToModule('data-collection')">
          <div class="card-icon collection">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="card-info">
            <h3>数据采集</h3>
            <p>多协议工业数据采集与处理</p>
            <div class="features-list">
              <span><el-icon><Switch /></el-icon> Modbus/OPC</span>
              <span><el-icon><Timer /></el-icon> 实时采集</span>
              <span><el-icon><Filter /></el-icon> 数据清洗</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 实时监控 -->
        <div class="module-card" @click="navigateToModule('realtime-monitoring')">
          <div class="card-icon monitoring">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="card-info">
            <h3>实时监控</h3>
            <p>设备运行状态实时可视化监控</p>
            <div class="features-list">
              <span><el-icon><Picture /></el-icon> 组态画面</span>
              <span><el-icon><Odometer /></el-icon> 仪表盘</span>
              <span><el-icon><TrendCharts /></el-icon> 实时曲线</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 报警管理 -->
        <div class="module-card" @click="navigateToModule('alarm-management')">
          <div class="card-icon alarm-manage">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="card-info">
            <h3>报警管理</h3>
            <p>全方位异常报警监控与处理</p>
            <div class="features-list">
              <span><el-icon><Setting /></el-icon> 阈值配置</span>
              <span><el-icon><Microphone /></el-icon> 实时推送</span>
              <span><el-icon><Histogram /></el-icon> 报警统计</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 历史数据 -->
        <div class="module-card" @click="navigateToModule('historical-data')">
          <div class="card-icon history">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="card-info">
            <h3>历史数据</h3>
            <p>海量历史数据存储与查询分析</p>
            <div class="features-list">
              <span><el-icon><Search /></el-icon> 数据检索</span>
              <span><el-icon><Download /></el-icon> 导出报表</span>
              <span><el-icon><DataAnalysis /></el-icon> 趋势分析</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 核心功能流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Operation /></el-icon>
          <span>数据采集监控流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon collection">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">数据采集</div>
              <div class="step-desc">设备/传感器/PLC</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon processing">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">数据处理</div>
              <div class="step-desc">清洗/计算/存储</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon monitoring">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">实时监控</div>
              <div class="step-desc">状态/画面/报警</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon analysis">
              <el-icon><DataAnalysis /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">分析优化</div>
              <div class="step-desc">报表/趋势/决策</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  ArrowRight, Monitor, Cpu, Link, Warning, Aim,
  Connection, Switch, Timer, Filter,
  Picture, Odometer, TrendCharts,
  Bell, Setting, Microphone, Histogram,
  DataLine, Search, Download, DataAnalysis,
  Operation, Right
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { unwrapResponseData } from '@/api'
import { scadaOverviewApi } from '@/api/scada'

// 路由实例
const router = useRouter()
const route = useRoute()

// 统计数据（默认占位，挂载后由 /overview/stats 接口刷新）
const stats = ref({
  totalDevices: 0,
  onlineDevices: 0,
  onlineDevicesRate: 0,
  activeAlarms: 0,
  collectionPoints: 0
})

// 加载SCADA概览统计数据
const loadStats = async () => {
  try {
    const response = await scadaOverviewApi.stats()
    const data = unwrapResponseData<any>(response) || {}
    const total = Number(data.totalDevices) || 0
    const online = Number(data.onlineDevices) || 0
    stats.value = {
      totalDevices: total,
      onlineDevices: online,
      onlineDevicesRate: total > 0 ? Math.round((online / total) * 1000) / 10 : 0,
      activeAlarms: Number(data.activeAlarms) || 0,
      collectionPoints: Number(data.collectionPoints) || 0
    }
  } catch (error) {
    console.error('加载SCADA统计数据失败:', error)
  }
}

onMounted(() => {
  loadStats()
})

// 页面标题
const pageTitle = computed(() => {
  const titleMap: Record<string, string> = {
    'data-collection': '数据采集',
    'realtime-monitoring': '实时监控',
    'alarm-management': '报警管理',
    'historical-data': '历史数据查询'
  }
  const moduleName = route.path.split('/').pop() || ''
  return titleMap[moduleName] || '功能详情'
})

// 判断是否是子路由
const isChildRoute = computed(() => {
  return route.path !== '/home/scada' && route.path.startsWith('/home/scada/')
})

// 返回上一级
const goBack = () => {
  router.push('/home/scada')
}

// 导航到模块详情页
const navigateToModule = (module: string) => {
  router.push(`/home/scada/${module}`)
}
</script>

<style scoped lang="scss">
.scada-view {
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
      
      &.device { background: #e6f7ff; color: #1890ff; }
      &.online { background: #f6ffed; color: #52c41a; }
      &.alarm { background: #fff2f0; color: #ff4d4f; }
      &.point { background: #fff7e6; color: #fa8c16; }
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
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-bottom: 20px;
    transition: all 0.3s ease;
    
    &.collection { background: #e6f7ff; color: #1890ff; }
    &.monitoring { background: #f0f5ff; color: #2f54eb; }
    &.alarm-manage { background: #fff2f0; color: #ff4d4f; }
    &.history { background: #f6ffed; color: #52c41a; }
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
        
        &.collection { background: #e6f7ff; color: #1890ff; }
        &.processing { background: #fff7e6; color: #fa8c16; }
        &.monitoring { background: #f6ffed; color: #52c41a; }
        &.analysis { background: #f9f0ff; color: #722ed1; }
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

.child-router-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    border-bottom: 1px solid var(--border-color-light);
    background-color: #fff;
    margin-bottom: 24px;
    
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
}

// 响应式调整
@media (max-width: 1024px) {
  .process-flow {
    flex-wrap: wrap;
    gap: 24px;
    
    .flow-step {
      flex: 0 0 calc(50% - 12px);
      min-width: unset;
      margin-bottom: 16px;
      
      &:nth-child(2) .step-arrow {
        display: none;
      }
    }
    
    .step-arrow {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .main-dashboard {
    padding: 16px;
  }
  
  .view-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    
    h2 {
      font-size: 24px;
    }
  }
  
  .stats-overview {
    grid-template-columns: 1fr;
  }
  
  .module-cards {
    grid-template-columns: 1fr;
  }
  
  .process-flow .flow-step {
    flex: 0 0 100%;
  }
}
</style>
