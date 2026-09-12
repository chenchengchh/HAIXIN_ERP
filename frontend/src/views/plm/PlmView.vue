<template>
  <div class="plm-view">
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
          <h2>PLM 产品全生命周期管理</h2>
          <p class="subtitle">从概念到退市的全生命周期协同管理</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus" @click="createProject">
            新建项目
          </el-button>
        </div>
      </div>
      
      <!-- 项目概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon project">
            <el-icon><Management /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">在研项目</div>
            <div class="stat-value">12</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 2
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon document">
            <el-icon><Files /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">文档总数</div>
            <div class="stat-value">3,542</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 124
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon change">
            <el-icon><Refresh /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待处理变更</div>
            <div class="stat-value warning">8</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon milestone">
            <el-icon><Flag /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本周里程碑</div>
            <div class="stat-value primary">3</div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 研发项目管理 -->
        <div class="module-card" @click="navigateToModule('project-management')">
          <div class="card-icon project">
            <el-icon><Management /></el-icon>
          </div>
          <div class="card-info">
            <h3>研发项目管理</h3>
            <p>项目进度、资源与风险管控</p>
            <div class="features-list">
              <span><el-icon><Timer /></el-icon> 进度计划</span>
              <span><el-icon><User /></el-icon> 资源分配</span>
              <span><el-icon><Warning /></el-icon> 风险管理</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 产品数据管理 -->
        <div class="module-card" @click="navigateToModule('product-data')">
          <div class="card-icon data">
            <el-icon><Folder /></el-icon>
          </div>
          <div class="card-info">
            <h3>产品数据管理</h3>
            <p>BOM、图纸与技术文档管理</p>
            <div class="features-list">
              <span><el-icon><Connection /></el-icon> BOM管理</span>
              <span><el-icon><Document /></el-icon> 图纸文档</span>
              <span><el-icon><Finished /></el-icon> 版本控制</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 工艺协同 -->
        <div class="module-card" @click="navigateToModule('process-collaboration')">
          <div class="card-icon process">
            <el-icon><Share /></el-icon>
          </div>
          <div class="card-info">
            <h3>工艺协同</h3>
            <p>设计与工艺的并行协同工作</p>
            <div class="features-list">
              <span><el-icon><Operation /></el-icon> 工艺路线</span>
              <span><el-icon><Edit /></el-icon> 设计变更</span>
              <span><el-icon><ChatDotRound /></el-icon> 在线评审</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 试制管理 -->
        <div class="module-card" @click="navigateToModule('trial-production')">
          <div class="card-icon trial">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="card-info">
            <h3>试制管理</h3>
            <p>样品试制与验证过程管理</p>
            <div class="features-list">
              <span><el-icon><Aim /></el-icon> 试制计划</span>
              <span><el-icon><DataAnalysis /></el-icon> 测试验证</span>
              <span><el-icon><DocumentChecked /></el-icon> 试制报告</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        </div>
      <!-- 产品全生命周期流程 -->
      <div class="process-section">
        <div class="section-header">
          <h3>
            <el-icon><Connection /></el-icon>
            产品全生命周期闭环
          </h3>
        </div>
        <div class="process-flow">
          <div class="flow-step" @click="navigateToProcess('requirement')">
            <div class="step-icon requirement">
              <el-icon><Document /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">需求分析</div>
              <div class="step-desc">承接 CRM 市场需求</div>
            </div>
            <div class="step-arrow"><el-icon><ArrowRight /></el-icon></div>
          </div>
          
          <div class="flow-step" @click="navigateToProcess('design')">
            <div class="step-icon design">
              <el-icon><Edit /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">产品设计</div>
              <div class="step-desc">CAD 集成与 EBOM</div>
            </div>
            <div class="step-arrow"><el-icon><ArrowRight /></el-icon></div>
          </div>
          
          <div class="flow-step" @click="navigateToProcess('process')">
            <div class="step-icon process">
              <el-icon><Operation /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">工艺规划</div>
              <div class="step-desc">MBOM 与 MES 协同</div>
            </div>
            <div class="step-arrow"><el-icon><ArrowRight /></el-icon></div>
          </div>
          
          <div class="flow-step" @click="navigateToProcess('trial')">
            <div class="step-icon trial">
              <el-icon><Tools /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">样机试制</div>
              <div class="step-desc">小批量验证与反馈</div>
            </div>
            <div class="step-arrow"><el-icon><ArrowRight /></el-icon></div>
          </div>
          
          <div class="flow-step" @click="navigateToProcess('release')">
            <div class="step-icon release">
              <el-icon><Flag /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">量产发布</div>
              <div class="step-desc">同步 ERP/SCM 主数据</div>
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
  Management,
  CaretTop,
  Files,
  Refresh,
  Flag,
  Timer,
  User,
  Warning,
  Folder,
  Connection,
  Document,
  Finished,
  Share,
  Operation,
  Edit,
  ChatDotRound,
  Tools,
  Aim,
  DataAnalysis,
  DocumentChecked
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.name !== 'plm'
})

/**
 * 跳转到PLM子模块页面
 * @param module 子模块路径（如 project-management / product-data 等）
 */
const navigateToModule = (module: string) => {
  router.push(`/home/plm/${module}`)
}

/**
 * 跳转到生命周期流程对应模块
 * @param step 流程步骤标识
 */
const navigateToProcess = (step: string) => {
  let path = ''
  switch (step) {
    case 'requirement':
      path = '/home/plm/project-management'
      break
    case 'design':
      path = '/home/plm/product-data'
      break
    case 'process':
      path = '/home/plm/process-collaboration'
      break
    case 'trial':
      path = '/home/plm/trial-production'
      break
    case 'release':
      path = '/home/plm/product-data'
      break
    default:
      return
  }
  router.push(path)
}

// 返回PLM主页面
const goBack = () => {
  // 直接导航到PLM主页面，而不是返回上一级
  router.push('/home/plm')
}

/**
 * 新建项目按钮点击行为
 * 最小可用：跳转到项目管理看板页，并附带参数触发创建弹窗
 */
const createProject = () => {
  router.push({ path: '/home/plm/project-management/dashboard', query: { create: '1' } })
}
</script>

<style scoped lang="scss">
.plm-view {
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
      
      &.project { background: #ecf5ff; color: #409EFF; }
      &.document { background: #f0f9eb; color: #67C23A; }
      &.change { background: #fdf6ec; color: #E6A23C; }
      &.milestone { background: #e6f7ff; color: #1890ff; }
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
        
        &.warning { color: #e6a23c; }
        &.primary { color: #409EFF; }
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
    
    &.project { color: #409EFF; background: #ecf5ff; }
    &.data { color: #67C23A; background: #f0f9eb; }
    &.process { color: #E6A23C; background: #fdf6ec; }
    &.trial { color: #F56C6C; background: #fef0f0; }
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
  margin-top: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  
  .section-header {
    display: flex;
    align-items: center;
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
      
      &.requirement { background: #ecf5ff; color: #409EFF; }
      &.design { background: #f0f9eb; color: #67C23A; }
      &.process { background: #fdf6ec; color: #E6A23C; }
      &.trial { background: #fef0f0; color: #F56C6C; }
      &.release { background: #e6f7ff; color: #1890ff; }
    }
    
    .step-content {
      flex: 1; // 保持内容占据剩余空间，但不强求
      
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

@media (max-width: 1024px) {
  .process-flow {
    flex-wrap: wrap;
    gap: 24px;
    padding: 0 !important;
    
    .flow-step {
      flex: 0 0 calc(50% - 12px);
      margin-bottom: 16px;
      
      .step-arrow {
        display: none;
      }
    }
  }
}

@media (max-width: 768px) {
  .process-flow {
    flex-direction: column;
    gap: 24px;
    
    .flow-step {
      width: 100%;
      flex: 0 0 100%;
      
      .step-arrow {
        transform: rotate(90deg);
        margin: 12px 0;
        display: flex !important; // 移动端可能需要显示箭头向下
      }
      
      &:last-child .step-arrow {
        display: none !important;
      }
    }
  }
}
</style>
