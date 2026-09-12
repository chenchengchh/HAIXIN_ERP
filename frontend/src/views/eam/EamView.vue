<template>
  <div class="eam-view">
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
          <h2>EAM 设备资产管理</h2>
          <p class="subtitle">全生命周期设备管理，保障生产稳定运行</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus" @click="handleRepairRegistration">
            报修登记
          </el-button>
        </div>
      </div>
      
      <!-- 设备概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon assets">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">设备总数</div>
            <div class="stat-value">1,286</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 5 台
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon rate">
            <el-icon><PieChart /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">设备完好率</div>
            <div class="stat-value">98.5<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 0.2%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon repair">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待处理工单</div>
            <div class="stat-value warning">12</div>
            <div class="stat-sub">今日新增 3</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon cost">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本月维修费</div>
            <div class="stat-value">15.8<span class="unit">万</span></div>
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon> 8.5%
            </div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 设备台账 -->
        <div class="module-card" @click="navigateToModule('asset-ledger')">
          <div class="card-icon ledger">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="card-info">
            <h3>设备台账</h3>
            <p>一机一档全生命周期记录</p>
            <div class="features-list">
              <span><el-icon><Document /></el-icon> 基础信息</span>
              <span><el-icon><Folder /></el-icon> 技术资料</span>
              <span><el-icon><Link /></el-icon> BOM关联</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 维护管理 -->
        <div class="module-card" @click="navigateToModule('maintenance-management')">
          <div class="card-icon maintain">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="card-info">
            <h3>维护管理</h3>
            <p>预防性维护与故障维修</p>
            <div class="features-list">
              <span><el-icon><Calendar /></el-icon> 保养计划</span>
              <span><el-icon><List /></el-icon> 工单执行</span>
              <span><el-icon><Warning /></el-icon> 故障报修</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 备件管理 -->
        <div class="module-card" @click="navigateToModule('spare-parts-management')">
          <div class="card-icon spare">
            <el-icon><Box /></el-icon>
          </div>
          <div class="card-info">
            <h3>备件管理</h3>
            <p>备件库存与消耗控制</p>
            <div class="features-list">
              <span><el-icon><Goods /></el-icon> 备件库存</span>
              <span><el-icon><SoldOut /></el-icon> 领用归还</span>
              <span><el-icon><WarningFilled /></el-icon> 安全库存</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 绩效分析 -->
        <div class="module-card" @click="navigateToModule('asset-performance')">
          <div class="card-icon analysis">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="card-info">
            <h3>绩效分析</h3>
            <p>设备运行效率与成本分析</p>
            <div class="features-list">
              <span><el-icon><TrendCharts /></el-icon> OEE分析</span>
              <span><el-icon><Timer /></el-icon> MTBF/MTTR</span>
              <span><el-icon><Money /></el-icon> 成本分析</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 资产全生命周期 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          <span>资产全生命周期</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon buy">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">采购验收</div>
              <div class="step-desc">选型/采购/安装/验收</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon use">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">运行维护</div>
              <div class="step-desc">点巡检/保养/维修</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon manage">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">技改大修</div>
              <div class="step-desc">性能提升/寿命延长</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          <div class="flow-step">
            <div class="step-icon retire">
              <el-icon><Delete /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">报废处置</div>
              <div class="step-desc">残值评估/处置/归档</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { 
  ArrowRight, 
  Plus,
  Monitor,
  PieChart,
  Tools,
  Money,
  CaretTop,
  CaretBottom,
  Collection,
  Document,
  Folder,
  Link,
  Calendar,
  List,
  Warning,
  Box,
  Goods,
  SoldOut,
  WarningFilled,
  DataLine,
  TrendCharts,
  Timer,
  Connection,
  ShoppingCart,
  Delete,
  Right
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'asset-ledger': '设备台账',
  'maintenance-management': '维护管理',
  'asset-performance': '绩效分析',
  'spare-parts-management': '备件管理'
}

const getCurrentModuleName = () => {
  /**
   * 从当前路由路径中解析EAM子模块名称（忽略子模块下的tab路由段）。
   * @returns 子模块路由段，如：asset-ledger / maintenance-management
   */
  const segments = route.path.split('/').filter(Boolean)
  const eamIndex = segments.indexOf('eam')
  if (eamIndex === -1) return ''
  return segments[eamIndex + 1] || ''
}

// 当前标签页名称
const currentTabLabel = computed(() => {
  const moduleName = getCurrentModuleName()
  return tabLabelMap[moduleName] || '功能详情'
})

const navigateToModule = (module: string) => {
  router.push(`/home/eam/${module}`)
}

// 是否有未保存的更改（可以根据实际业务场景动态设置）
const hasUnsavedChanges = ref(false)

const goBack = async () => {
  // 如果有未保存的更改，显示确认提示
  if (hasUnsavedChanges.value) {
    try {
      await ElMessageBox.confirm('您有未保存的更改，确定要返回吗？', '确认返回', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch (error) {
      // 用户取消返回
      return
    }
  }
  
  // 直接跳转到EAM主界面
  router.push('/home/eam')
}

// 报修登记处理
const handleRepairRegistration = () => {
  console.log('点击报修登记按钮')
  // 跳转到报修页面或打开报修表单
  router.push('/home/eam/maintenance-management/fault-repair')
}
</script>

<style scoped lang="scss">
.eam-view {
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
      
      &.assets { background: #e1f3d8; color: #67c23a; }
      &.rate { background: #e6f7ff; color: #1890ff; }
      &.repair { background: #fdf6ec; color: #e6a23c; }
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
    
    &.ledger { color: #409EFF; background: #ecf5ff; }
    &.maintain { color: #E6A23C; background: #fdf6ec; }
    &.spare { color: #67C23A; background: #f0f9eb; }
    &.analysis { color: #F56C6C; background: #fef0f0; }
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
        
        &.buy { background: #ecf5ff; color: #409EFF; }
        &.use { background: #f0f9eb; color: #67C23A; }
        &.manage { background: #fdf6ec; color: #E6A23C; }
        &.retire { background: #fef0f0; color: #F56C6C; }
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
