<template>
  <div class="erp-view">
    <!-- 子路由视图 -->
    <div v-if="isChildRoute" class="child-view-container">
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
          <h2>ERP 企业资源计划</h2>
          <p class="subtitle">整合企业核心资源，优化业务流程，提升运营效率</p>
        </div>
      </div>

      <!-- 经营概览统计（真实数据，来自 /api/v1/erp/dashboard/stats） -->
      <div v-loading="statsLoading" class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon finance">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">供应链业务总额</div>
            <div class="stat-value">{{ formatAmount(stats.totalSalesAmount) }}<span class="unit">元</span></div>
            <div class="stat-sub">供应链单据 {{ stats.supplyChainCount ?? 0 }} 笔</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon profit">
            <el-icon><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">财务凭证</div>
            <div class="stat-value">{{ stats.voucherCount ?? 0 }}<span class="unit">张</span></div>
            <div class="stat-sub">借方合计 {{ formatAmount(stats.totalDebit) }} 元</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon cost">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">生产订单</div>
            <div class="stat-value">{{ stats.productionCount ?? 0 }}<span class="unit">单</span></div>
            <div class="stat-sub">进行中 {{ stats.productionInProgress ?? 0 }} 单</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon assets">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">物料主数据</div>
            <div class="stat-value">{{ stats.materialCount ?? 0 }}<span class="unit">项</span></div>
            <div class="stat-sub">供应商 {{ stats.supplierCount ?? 0 }} 家 / 仓库 {{ stats.warehouseCount ?? 0 }} 个</div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 财务管理 -->
        <div class="module-card" @click="navigateToModule('finance')">
          <div class="card-icon finance">
            <el-icon><Money /></el-icon>
          </div>
          <div class="card-info">
            <h3>财务管理</h3>
            <p>全方位财务核算与资金管控中心</p>
            <div class="features-list">
              <span><el-icon><Document /></el-icon> 总账</span>
              <span><el-icon><Wallet /></el-icon> 应收应付</span>
              <span><el-icon><TrendCharts /></el-icon> 成本核算</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 供应链管理 -->
        <div class="module-card" @click="navigateToModule('supply-chain')">
          <div class="card-icon supply-chain">
            <el-icon><Van /></el-icon>
          </div>
          <div class="card-info">
            <h3>供应链管理</h3>
            <p>采购销售库存一体化协同管理</p>
            <div class="features-list">
              <span><el-icon><ShoppingCart /></el-icon> 采购</span>
              <span><el-icon><Sell /></el-icon> 销售</span>
              <span><el-icon><Box /></el-icon> 库存</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 生产管理 -->
        <div class="module-card" @click="navigateToModule('production')">
          <div class="card-icon production">
            <el-icon><Setting /></el-icon>
          </div>
          <div class="card-info">
            <h3>生产管理</h3>
            <p>生产计划与执行全过程精细管控</p>
            <div class="features-list">
              <span><el-icon><List /></el-icon> 生产订单</span>
              <span><el-icon><Operation /></el-icon> 车间管理</span>
              <span><el-icon><Timer /></el-icon> 产能规划</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 基础数据 -->
        <div class="module-card" @click="navigateToModule('basic-data')">
          <div class="card-icon basic-data">
            <el-icon><DataBoard /></el-icon>
          </div>
          <div class="card-info">
            <h3>基础数据</h3>
            <p>企业主数据标准化与规范化管理</p>
            <div class="features-list">
              <span><el-icon><User /></el-icon> 客户/供应商</span>
              <span><el-icon><Goods /></el-icon> 物料主数据</span>
              <span><el-icon><OfficeBuilding /></el-icon> 组织架构</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 核心业务流程 -->
      <div class="process-section">
        <div class="section-header">
          <h3><el-icon><Connection /></el-icon> 企业核心价值链</h3>
        </div>
        <div class="process-flow">
          <div class="flow-steps">
            <div class="flow-step">
              <div class="step-icon sale">
                <el-icon><Sell /></el-icon>
              </div>
              <div class="step-content">
                <div class="step-title">销售订单</div>
                <div class="step-desc">客户需求录入</div>
              </div>
              <div class="step-arrow"><el-icon><Right /></el-icon></div>
            </div>
            
            <div class="flow-step">
              <div class="step-icon mrp">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="step-content">
                <div class="step-title">MRP运算</div>
                <div class="step-desc">需求计划生成</div>
              </div>
              <div class="step-arrow"><el-icon><Right /></el-icon></div>
            </div>
            
            <div class="flow-step">
              <div class="step-icon purchase">
                <el-icon><ShoppingCart /></el-icon>
              </div>
              <div class="step-content">
                <div class="step-title">采购/生产</div>
                <div class="step-desc">资源获取与加工</div>
              </div>
              <div class="step-arrow"><el-icon><Right /></el-icon></div>
            </div>
            
            <div class="flow-step">
              <div class="step-icon stock">
                <el-icon><Box /></el-icon>
              </div>
              <div class="step-content">
                <div class="step-title">出入库</div>
                <div class="step-desc">库存变动记录</div>
              </div>
              <div class="step-arrow"><el-icon><Right /></el-icon></div>
            </div>
            
            <div class="flow-step">
              <div class="step-icon finance">
                <el-icon><Wallet /></el-icon>
              </div>
              <div class="step-content">
                <div class="step-title">财务结算</div>
                <div class="step-desc">收付款与核算</div>
              </div>
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
import {
  ArrowRight,
  Money,
  Van,
  Setting,
  DataBoard,
  Connection,
  Document,
  Cpu,
  ShoppingCart,
  Box,
  Wallet,
  Right,
  OfficeBuilding,
  Sell,
  List,
  Operation,
  Timer,
  User,
  Goods,
  TrendCharts
} from '@element-plus/icons-vue'
import { erpApi } from '../../api/erp'
import { unwrapResponseData } from '../../api'
import { ErrorHandler } from '../../utils/error-handler'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

const navigateToModule = (module: string) => {
  router.push(`/home/erp/${module}`)
}

const goBack = () => {
  router.back()
}

/** 仪表盘统计数据（字段与后端 /api/v1/erp/dashboard/stats 返回一致） */
const stats = ref<Record<string, any>>({})
/** 统计数据加载中状态 */
const statsLoading = ref(false)

/**
 * 格式化金额显示（千分位 + 保留两位小数）。
 * @param value 金额数值
 * @returns 格式化后的金额字符串
 */
const formatAmount = (value: any) => {
  const num = Number(value)
  if (!Number.isFinite(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * 获取ERP仪表盘统计数据。
 * <p>调用 /api/v1/erp/dashboard/stats，一次性返回生产/凭证/物料/供应链等聚合数据。</p>
 */
const fetchStats = async () => {
  statsLoading.value = true
  try {
    const response = await erpApi.dashboard.getStats()
    stats.value = unwrapResponseData<Record<string, any>>(response, {}) || {}
  } catch (error) {
    ErrorHandler.handleApiError(error)
    stats.value = {}
  } finally {
    statsLoading.value = false
  }
}

onMounted(() => {
  // 仅在主视图（非子路由）时加载统计
  if (!isChildRoute.value) {
    fetchStats()
  }
})
</script>

<style scoped lang="scss">
.erp-view {
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

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
    margin: 0;
    
    .el-icon {
      color: var(--primary-color);
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
      transform: translateY(-2px);
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
      
      &.finance { background: #e6f7ff; color: #1890ff; }
      &.profit { background: #f6ffed; color: #52c41a; }
      &.cost { background: #fff0f6; color: #eb2f96; }
      &.assets { background: #f5f3ff; color: #722ed1; }
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
        font-weight: 600;
        color: var(--text-primary);
        line-height: 1.2;
        
        .unit {
          font-size: 14px;
          font-weight: normal;
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
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
    border-color: var(--primary-light);
    
    .card-icon {
      transform: scale(1.05) rotate(3deg);
    }
    
    .arrow-icon {
      transform: translateX(4px);
      color: var(--primary-color);
    }
  }
  
  .card-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-bottom: 20px;
    transition: all 0.3s ease;
    
    &.finance { background: #e6f7ff; color: #1890ff; }
    &.supply-chain { background: #f6ffed; color: #52c41a; }
    &.production { background: #fff7e6; color: #fa8c16; }
    &.basic-data { background: #f5f3ff; color: #722ed1; }
  }
  
  .card-info {
    flex: 1;
    margin-bottom: 16px;
    
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
      gap: 8px;
      
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
      
      &.sale { background: #e6f7ff; color: #1890ff; }
      &.mrp { background: #f5f3ff; color: #722ed1; }
      &.purchase { background: #f6ffed; color: #52c41a; }
      &.stock { background: #fff7e6; color: #fa8c16; }
      &.finance { background: #fff0f6; color: #eb2f96; }
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

@media (max-width: 1024px) {
  .flow-steps {
    flex-wrap: wrap;
    gap: 24px;
    
    .flow-step {
      width: calc(50% - 12px);
      
      .step-arrow {
        transform: rotate(90deg);
        margin: 8px 0;
      }
    }
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: 1fr 1fr;
  }
  
  .module-cards {
    grid-template-columns: 1fr;
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
