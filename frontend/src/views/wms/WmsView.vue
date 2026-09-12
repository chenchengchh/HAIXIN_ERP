<template>
  <div class="wms-view dashboard-container">
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
          <h2>WMS 仓储管理系统</h2>
          <p class="subtitle">全方位的智能仓储解决方案，实现库存精准管控与高效作业</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus" @click="goInboundNotice">
            入库通知
          </el-button>
        </div>
      </div>
      
      <!-- 数据概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon inbound">
            <el-icon><Van /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">ASN数量</div>
            <div class="stat-value">{{ asnTotal }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon outbound">
            <el-icon><Box /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">出库单数量</div>
            <div class="stat-value">{{ outboundOrderTotal }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon stock">
            <el-icon><House /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">物料种类</div>
            <div class="stat-value warning">{{ materialCount }}</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon task">
            <el-icon><List /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">待处理拣货任务</div>
            <div class="stat-value">{{ pendingPickingTaskTotal }}</div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 基础设置 -->
        <div class="module-card" @click="navigateToModule('base')">
          <div class="card-icon base">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="card-info">
            <h3>基础设置</h3>
            <p>仓库布局与规则配置中心</p>
            <div class="features-list">
              <span><el-icon><MapLocation /></el-icon> 仓库平面</span>
              <span><el-icon><Printer /></el-icon> 库位打印</span>
              <span><el-icon><Operation /></el-icon> 策略配置</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 入库管理 -->
        <div class="module-card" @click="navigateToModule('inbound')">
          <div class="card-icon inbound">
            <el-icon><Van /></el-icon>
          </div>
          <div class="card-info">
            <h3>入库管理</h3>
            <p>从收货到上架的全流程管理</p>
            <div class="features-list">
              <span><el-icon><Document /></el-icon> 到货通知</span>
              <span><el-icon><Checked /></el-icon> 质检收货</span>
              <span><el-icon><Top /></el-icon> 上架作业</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 出库管理 -->
        <div class="module-card" @click="navigateToModule('outbound')">
          <div class="card-icon outbound">
            <el-icon><Box /></el-icon>
          </div>
          <div class="card-info">
            <h3>出库管理</h3>
            <p>智能波次与高效拣货策略</p>
            <div class="features-list">
              <span><el-icon><List /></el-icon> 波次管理</span>
              <span><el-icon><Finished /></el-icon> 拣货任务</span>
              <span><el-icon><Position /></el-icon> 发货复核</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 库存中心 -->
        <div class="module-card" @click="navigateToModule('stock')">
          <div class="card-icon stock">
            <el-icon><House /></el-icon>
          </div>
          <div class="card-info">
            <h3>库存中心</h3>
            <p>实时库存监控与盘点管理</p>
            <div class="features-list">
              <span><el-icon><DataLine /></el-icon> 库存查询</span>
              <span><el-icon><Refresh /></el-icon> 移库调拨</span>
              <span><el-icon><Aim /></el-icon> 盘点管理</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 移动端作业 -->
        <div class="module-card" @click="navigateToModule('mobile')">
          <div class="card-icon mobile">
            <el-icon><Iphone /></el-icon>
          </div>
          <div class="card-info">
            <h3>移动端作业</h3>
            <p>手持PDA作业，提升现场效率</p>
            <div class="features-list">
              <span><el-icon><Aim /></el-icon> 扫码入库</span>
              <span><el-icon><ShoppingCart /></el-icon> 扫码拣货</span>
              <span><el-icon><Search /></el-icon> 移动查询</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 核心作业流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Operation /></el-icon>
          <span>核心作业流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon receive">
              <el-icon><Van /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">收货入库</div>
              <div class="step-desc">预约/卸货/质检</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon putaway">
              <el-icon><Top /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">上架存储</div>
              <div class="step-desc">策略推荐/上架</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon pick">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">拣货复核</div>
              <div class="step-desc">波次/拣货/打包</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon ship">
              <el-icon><Position /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">装车发货</div>
              <div class="step-desc">交接/发运/回单</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { unwrapPageResponse } from '@/api'
import { 
  ArrowRight, Setting, Van, Box, House, Iphone,
  MapLocation, Printer, Operation, Document, Checked, Top,
  List, Finished, Position, DataLine, Refresh, Aim,
  ShoppingCart, Search, Check, Plus, Right
} from '@element-plus/icons-vue'
import { asnApi, outboundOrderApi, inventoryAnalyticsApi, pickingTaskV1Api } from '@/api/wms'

const router = useRouter()
const route = useRoute()

// 判断是否为子路由
const isChildRoute = computed(() => {
  return route.name !== 'wms'
})

// 获取当前路由名称
const currentRouteName = computed(() => {
  return route.meta.title || 'WMS功能'
})

// 导航到模块
const navigateToModule = (module: string) => {
  router.push(`/home/wms/${module}`)
}

const goInboundNotice = () => {
  router.push({
    name: 'wms-inbound',
    query: { tab: 'asn-list', action: 'create' }
  })
}

// 返回上一级
const goBack = () => {
  router.push('/home/wms')
}

const asnTotal = ref(0)
const outboundOrderTotal = ref(0)
const materialCount = ref(0)
const pendingPickingTaskTotal = ref(0)

const extractTotal = (response: any): number => {
  return unwrapPageResponse(response).total
}

onMounted(async () => {
  try {
    const [asnRes, outboundRes, invSummaryRes, pickingPendingRes] = await Promise.all([
      asnApi.getList({ page: 0, size: 1 }),
      outboundOrderApi.getList({ page: 1, size: 1 }),
      inventoryAnalyticsApi.getSummary(),
      pickingTaskV1Api.getList({ page: 1, size: 1, status: 'pending' })
    ])
    asnTotal.value = extractTotal(asnRes)
    outboundOrderTotal.value = extractTotal(outboundRes)
    // api包装层已解包，invSummaryRes.data即为汇总payload
    materialCount.value = Number(invSummaryRes?.data?.materialCount ?? 0) || 0
    pendingPickingTaskTotal.value = extractTotal(pickingPendingRes)
  } catch (error) {
    console.error('Failed to load WMS dashboard stats:', error)
  }
})
</script>

<style scoped lang="scss">
.wms-view {
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
      
      &.inbound { background: #e6f7ff; color: #1890ff; }
      &.outbound { background: #f0f9eb; color: #67C23A; }
      &.stock { background: #fdf6ec; color: #e6a23c; }
      &.task { background: #fef0f0; color: #f56c6c; }
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
    
    &.base { color: #909399; background: #f4f4f5; }
    &.inbound { color: #409EFF; background: #ecf5ff; }
    &.outbound { color: #67C23A; background: #f0f9eb; }
    &.stock { color: #E6A23C; background: #fdf6ec; }
    &.mobile { color: #9333ea; background: #f3e8ff; }
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
        
        &.receive { background: #ecf5ff; color: #409EFF; }
        &.putaway { background: #f0f9eb; color: #67C23A; }
        &.pick { background: #fdf6ec; color: #E6A23C; }
        &.ship { background: #f3e8ff; color: #9333ea; }
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
