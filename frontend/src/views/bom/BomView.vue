<template>
  <div class="bom-view dashboard-container">
    <!-- 子路由视图 -->
    <div v-if="isChildRoute" class="child-view-container">
      <div class="page-header">
        <el-page-header @back="goBack">
          <template #content>
            <span class="text-large font-600 mr-3">功能详情</span>
          </template>
        </el-page-header>
      </div>
      <router-view />
    </div>
    
    <!-- 主视图 -->
    <div v-else class="main-dashboard">
      <div class="view-header">
        <div class="header-content">
          <h2>BOM 物料清单管理</h2>
          <p class="subtitle">产品结构数字化基座，确保设计与制造数据一致性</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus">
            新建物料
          </el-button>
        </div>
      </div>
      
      <!-- 数据概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon material">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">物料总数</div>
            <div class="stat-value">12,056</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 125 个
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon change">
            <el-icon><RefreshRight /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待审变更</div>
            <div class="stat-value warning">45</div>
            <div class="stat-sub">ECN紧急 2</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon accuracy">
            <el-icon><Aim /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">数据准确率</div>
            <div class="stat-value">99.8<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 0.1%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon component">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">标准件占比</div>
            <div class="stat-value">68.5<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 1.2%
            </div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 物料档案 -->
        <div class="module-card" @click="navigateToModule('material')">
          <div class="card-icon material">
            <el-icon><Box /></el-icon>
          </div>
          <div class="card-info">
            <h3>物料档案</h3>
            <p>建立统一的物料主数据标准</p>
            <div class="features-list">
              <span><el-icon><Ticket /></el-icon> 编码管理</span>
              <span><el-icon><Collection /></el-icon> 分类属性</span>
              <span><el-icon><Timer /></el-icon> 生命周期</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- BOM结构 -->
        <div class="module-card" @click="navigateToModule('structure')">
          <div class="card-icon structure">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="card-info">
            <h3>BOM结构</h3>
            <p>精准定义产品构成与层级</p>
            <div class="features-list">
              <span><el-icon><Operation /></el-icon> 多级BOM</span>
              <span><el-icon><Switch /></el-icon> 替代料</span>
              <span><el-icon><Van /></el-icon> 虚件管理</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 版本控制 -->
        <div class="module-card" @click="navigateToModule('version')">
          <div class="card-icon version">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="card-info">
            <h3>版本控制</h3>
            <p>严格管控技术变更与历史</p>
            <div class="features-list">
              <span><el-icon><DocumentCopy /></el-icon> 版本发布</span>
              <span><el-icon><EditPen /></el-icon> ECN变更</span>
              <span><el-icon><Clock /></el-icon> 历史追溯</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- BOM分析 -->
        <div class="module-card" @click="navigateToModule('analysis')">
          <div class="card-icon analysis">
            <el-icon><DataLine /></el-icon>
          </div>
          <div class="card-info">
            <h3>BOM分析</h3>
            <p>多维度数据洞察辅助决策</p>
            <div class="features-list">
              <span><el-icon><Search /></el-icon> 反查引用</span>
              <span><el-icon><Money /></el-icon> 成本卷算</span>
              <span><el-icon><PieChart /></el-icon> 齐套检查</span>
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
          <el-icon><Connection /></el-icon>
          <span>数据管理全流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon create">
              <el-icon><Edit /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">定义创建</div>
              <div class="step-desc">物料/BOM录入</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon review">
              <el-icon><Stamp /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">审核生效</div>
              <div class="step-desc">标准化校验/审批</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon version">
              <el-icon><DocumentCopy /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">版本发布</div>
              <div class="step-desc">基线归档/下发</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon change">
              <el-icon><RefreshRight /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">变更闭环</div>
              <div class="step-desc">ECR/ECO/ECN</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Box, 
  Connection, 
  Timer, 
  DataLine, 
  ArrowRight, 
  Right,
  Edit,
  Stamp,
  DocumentCopy,
  RefreshRight,
  Plus,
  CaretTop,
  Aim,
  Ticket,
  Collection,
  Operation,
  Switch,
  Van,
  EditPen,
  Clock,
  Search,
  Money,
  PieChart
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

const navigateToModule = (modulePath: string) => {
  router.push(`/home/bom/${modulePath}`)
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.bom-view {
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
      
      &.material { background: #e6f7ff; color: #1890ff; }
      &.change { background: #fdf6ec; color: #e6a23c; }
      &.accuracy { background: #f0f9eb; color: #67C23A; }
      &.component { background: #fef0f0; color: #f56c6c; }
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
    
    &.material { color: #409EFF; background: #ecf5ff; }
    &.structure { color: #67C23A; background: #f0f9eb; }
    &.version { color: #E6A23C; background: #fdf6ec; }
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
        
        &.create { background: #ecf5ff; color: #409EFF; }
        &.review { background: #f0f9eb; color: #67C23A; }
        &.version { background: #fdf6ec; color: #E6A23C; }
        &.change { background: #fef0f0; color: #F56C6C; }
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
