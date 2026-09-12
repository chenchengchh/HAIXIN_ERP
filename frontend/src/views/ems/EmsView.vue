<template>
  <div class="ems-view">
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
          <h2>EMS 能源管理</h2>
          <p class="subtitle">全方位能源监控与分析，助力企业绿色低碳发展</p>
        </div>
      </div>
      
      <!-- 能耗概览 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon power">
            <el-icon><Lightning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日用电</div>
            <div class="stat-value">{{ todayConsumption.electricity }} <span class="unit">kWh</span></div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon water">
            <el-icon><Pouring /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日用水</div>
            <div class="stat-value">{{ todayConsumption.water }} <span class="unit">m³</span></div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon gas">
            <el-icon><HotWater /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">今日用气</div>
            <div class="stat-value">{{ todayConsumption.gas }} <span class="unit">m³</span></div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon carbon">
            <el-icon><Sunny /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待处理异常</div>
            <div class="stat-value">{{ emsStore.pendingAnomaliesCount }} <span class="unit">条</span></div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 能源采集 -->
        <div class="module-card" @click="navigateToModule('energy-collection')">
          <div class="card-icon collection">
            <el-icon><Odometer /></el-icon>
          </div>
          <div class="card-info">
            <h3>能源采集</h3>
            <p>多维能源数据实时采集监控</p>
            <div class="features-list">
              <span><el-icon><Monitor /></el-icon> 实时监控</span>
              <span><el-icon><Connection /></el-icon> 设备接入</span>
              <span><el-icon><CircleCheck /></el-icon> 数据校准</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 能耗分析 -->
        <div class="module-card" @click="navigateToModule('energy-analysis')">
          <div class="card-icon analysis">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="card-info">
            <h3>能耗分析</h3>
            <p>深度能耗统计与趋势分析</p>
            <div class="features-list">
              <span><el-icon><Histogram /></el-icon> 统计报表</span>
              <span><el-icon><DataLine /></el-icon> 趋势对比</span>
              <span><el-icon><Warning /></el-icon> 异常识别</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 能源优化 -->
        <div class="module-card" @click="navigateToModule('energy-optimization')">
          <div class="card-icon optimization">
            <el-icon><Aim /></el-icon>
          </div>
          <div class="card-info">
            <h3>能源优化</h3>
            <p>智能节能策略与效果评估</p>
            <div class="features-list">
              <span><el-icon><Aim /></el-icon> 节能潜力</span>
              <span><el-icon><MagicStick /></el-icon> 优化建议</span>
              <span><el-icon><Trophy /></el-icon> 效果评估</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 报表管理 -->
        <div class="module-card" @click="navigateToModule('report-management')">
          <div class="card-icon report">
            <el-icon><Document /></el-icon>
          </div>
          <div class="card-info">
            <h3>报表管理</h3>
            <p>自动化能耗报表生成与推送</p>
            <div class="features-list">
              <span><el-icon><Files /></el-icon> 标准报表</span>
              <span><el-icon><Edit /></el-icon> 自定义</span>
              <span><el-icon><Timer /></el-icon> 定时生成</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 能源管理流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Operation /></el-icon>
          <span>能源闭环管理流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon collection">
              <el-icon><Odometer /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">监测采集</div>
              <div class="step-desc">智能仪表/传感器</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon analysis">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">统计分析</div>
              <div class="step-desc">能耗结构/成本分析</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon diagnosis">
              <el-icon><Search /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">诊断评估</div>
              <div class="step-desc">能效对标/漏洞识别</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon optimization">
              <el-icon><Aim /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">优化控制</div>
              <div class="step-desc">策略调整/设备技改</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon verification">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">持续验证</div>
              <div class="step-desc">效果验证/持续改进</div>
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
import { 
  ArrowRight, Lightning, Pouring, HotWater, Sunny,
  Odometer, Monitor, Connection,
  CircleCheck, TrendCharts, Histogram, DataLine, Warning,
  Aim, MagicStick, Trophy, Document, Files, Edit,
  Timer, Operation, Right, Search
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useEmsStore } from '@/stores/ems'

// 路由实例
const router = useRouter()
const route = useRoute()

// 状态管理
const emsStore = useEmsStore()

// 判断是否是子路由
const isChildRoute = computed(() => {
  return route.name !== 'ems'
})

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'energy-collection': '能源采集',
  'energy-analysis': '能耗分析',
  'energy-optimization': '能源优化',
  'report-management': '报表管理'
}

// 当前标签页名称
const currentTabLabel = computed(() => {
  const moduleName = route.path.split('/').pop() || ''
  return tabLabelMap[moduleName] || '功能详情'
})

/**
 * 今日各能源类型消耗汇总（基于实时数据按能源类型求和）
 * 能源类型兼容中英文（电力/electricity、水资源/water、燃气/gas）
 */
const todayConsumption = computed(() => {
  const summary = { electricity: 0, water: 0, gas: 0 }
  const typeMap: Record<string, keyof typeof summary> = {
    '电力': 'electricity', 'electricity': 'electricity',
    '水资源': 'water', 'water': 'water',
    '燃气': 'gas', 'gas': 'gas'
  }
  for (const item of emsStore.realTimeData) {
    const key = typeMap[item.energyType]
    if (key) {
      summary[key] += item.actualValue || 0
    }
  }
  return {
    electricity: summary.electricity.toLocaleString('zh-CN', { maximumFractionDigits: 1 }),
    water: summary.water.toLocaleString('zh-CN', { maximumFractionDigits: 1 }),
    gas: summary.gas.toLocaleString('zh-CN', { maximumFractionDigits: 1 })
  }
})

// 返回上一级
const goBack = () => {
  router.push('/home/ems')
}

// 导航到模块详情页
const navigateToModule = (module: string) => {
  router.push(`/home/ems/${module}`)
}

// 组件挂载时获取数据
onMounted(async () => {
  try {
    // 获取实时数据
    await emsStore.fetchRealTimeData()
    // 获取能耗异常数据
    await emsStore.fetchEnergyAnomalies()
    // 获取优化建议
    await emsStore.fetchOptimizationSuggestions()
  } catch (error) {
    console.error('获取EMS数据失败:', error)
  }
})
</script>

<style scoped lang="scss">
.ems-view {
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
  
  .header-actions {
    /* 预留header-actions样式，与HR页面保持一致 */
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
      
      &.power { background: #fff7e6; color: #fa8c16; }
      &.water { background: #e6f7ff; color: #1890ff; }
      &.gas { background: #fff2f0; color: #ff4d4f; }
      &.carbon { background: #f6ffed; color: #52c41a; }
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
        margin-bottom: 4px;
        
        .unit {
          font-size: 14px;
          font-weight: normal;
          color: var(--text-secondary);
          margin-left: 4px;
        }
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
    &.analysis { background: #f0f5ff; color: #2f54eb; }
    &.optimization { background: #f6ffed; color: #52c41a; }
    &.report { background: #fff7e6; color: #fa8c16; }
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
        &.analysis { background: #f0f5ff; color: #2f54eb; }
        &.diagnosis { background: #fff2f0; color: #ff4d4f; }
        &.optimization { background: #f6ffed; color: #52c41a; }
        &.verification { background: #fff7e6; color: #fa8c16; }
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
  
  .view-header h2 {
    font-size: 24px;
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
