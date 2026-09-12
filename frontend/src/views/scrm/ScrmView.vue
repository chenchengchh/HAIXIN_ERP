<template>
  <div class="scrm-view">
    <!-- 子路由视图 -->
    <div v-if="isChildRoute" class="child-router-view">
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
          <h2>SCRM 社交化客户关系管理</h2>
          <p class="subtitle">连接社交生态，构建私域流量闭环，实现全渠道客户运营</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus">
            新建活动
          </el-button>
        </div>
      </div>

      <!-- 运营概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon fans">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">私域粉丝</div>
            <div class="stat-value">128,500</div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 12.5%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon active">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">日活跃度</div>
            <div class="stat-value">24.8<span class="unit">%</span></div>
            <div class="stat-trend up">
              <el-icon><CaretTop /></el-icon> 1.2%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon conversion">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">转化金额</div>
            <div class="stat-value">45.2<span class="unit">万</span></div>
            <div class="stat-trend down">
              <el-icon><CaretBottom /></el-icon> 3.4%
            </div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon risk">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">流失预警</div>
            <div class="stat-value warning">126</div>
            <div class="stat-sub">高意向客户 12</div>
          </div>
        </div>
      </div>
      
      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 主动获客 -->
        <div class="module-card" @click="navigateToModule('acquisition-active')">
          <div class="card-icon active-acq">
            <el-icon><Magnet /></el-icon>
          </div>
          <div class="card-info">
            <h3>主动获客</h3>
            <p>全网多平台线索自动采集</p>
            <div class="features-list">
              <span><el-icon><VideoCamera /></el-icon> 抖音/快手</span>
              <span><el-icon><Search /></el-icon> 关键词搜索</span>
              <span><el-icon><ChatDotSquare /></el-icon> 自动私信</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 被动获客 -->
        <div class="module-card" @click="navigateToModule('acquisition-passive')">
          <div class="card-icon passive-acq">
            <el-icon><Service /></el-icon>
          </div>
          <div class="card-info">
            <h3>被动获客</h3>
            <p>内容分发与咨询自动接待</p>
            <div class="features-list">
              <span><el-icon><Promotion /></el-icon> 内容矩阵</span>
              <span><el-icon><ChatLineRound /></el-icon> 智能客服</span>
              <span><el-icon><Bell /></el-icon> 意向提醒</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 私域流量运营 -->
        <div class="module-card" @click="navigateToModule('private-traffic')">
          <div class="card-icon traffic">
            <el-icon><ChatLineRound /></el-icon>
          </div>
          <div class="card-info">
            <h3>私域流量运营</h3>
            <p>多渠道对接与社群管理</p>
            <div class="features-list">
              <span><el-icon><Link /></el-icon> 渠道对接</span>
              <span><el-icon><PriceTag /></el-icon> 自动标签</span>
              <span><el-icon><ChatDotSquare /></el-icon> 话术库</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 客户裂变 -->
        <div class="module-card" @click="navigateToModule('customer-fission')">
          <div class="card-icon fission">
            <el-icon><Share /></el-icon>
          </div>
          <div class="card-info">
            <h3>客户裂变</h3>
            <p>社交裂变与分销增长</p>
            <div class="features-list">
              <span><el-icon><Goods /></el-icon> 邀请有礼</span>
              <span><el-icon><User /></el-icon> 拼团活动</span>
              <span><el-icon><Money /></el-icon> 分销管理</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 精准营销 -->
        <div class="module-card" @click="navigateToModule('precision-marketing')">
          <div class="card-icon marketing">
            <el-icon><Aim /></el-icon>
          </div>
          <div class="card-info">
            <h3>精准营销</h3>
            <p>基于画像的千人千面触达</p>
            <div class="features-list">
              <span><el-icon><UserFilled /></el-icon> 客户画像</span>
              <span><el-icon><Message /></el-icon> 标签推送</span>
              <span><el-icon><DataLine /></el-icon> 转化漏斗</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 数据同步 -->
        <div class="module-card" @click="navigateToModule('data-sync')">
          <div class="card-icon sync">
            <el-icon><Refresh /></el-icon>
          </div>
          <div class="card-info">
            <h3>数据同步</h3>
            <p>多平台数据互通与集成</p>
            <div class="features-list">
              <span><el-icon><Connection /></el-icon> CRM互通</span>
              <span><el-icon><ShoppingCart /></el-icon> 电商对接</span>
              <span><el-icon><Odometer /></el-icon> 埋点数据</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
      
      <!-- 私域运营闭环流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          <span>私域运营闭环流程</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon attract">
              <el-icon><Magnet /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">公域引流</div>
              <div class="step-desc">全渠道获客</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon retain">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">私域沉淀</div>
              <div class="step-desc">加粉打标签</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon nurture">
              <el-icon><ChatDotSquare /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">客户培育</div>
              <div class="step-desc">内容触达互动</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon convert">
              <el-icon><Money /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">转化成交</div>
              <div class="step-desc">促进购买复购</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>

          <div class="flow-step">
            <div class="step-icon fission-step">
              <el-icon><Share /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">裂变拉新</div>
              <div class="step-desc">口碑传播增长</div>
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
  Service,
  Promotion,
  VideoCamera,
  Search,
  Bell,
  ArrowRight, 
  ChatLineRound, 
  Share, 
  Aim, 
  Refresh,
  Connection,
  Magnet,
  UserFilled,
  ChatDotSquare,
  Money,
  Right,
  Plus,
  CaretTop,
  CaretBottom,
  Warning,
  ChatDotRound,
  Link,
  PriceTag,
  Goods,
  User,
  Message,
  DataLine,
  ShoppingCart,
  Odometer
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.name !== 'scrm'
})

const navigateToModule = (module: string) => {
  router.push(`/home/scrm/${module}`)
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.scrm-view {
  min-height: 100%;
  
  .main-dashboard {
    padding: 24px;
    background-color: var(--bg-color-page);
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
      
      &.fans { background: #ecf5ff; color: #409EFF; }
      &.active { background: #f0f9eb; color: #67C23A; }
      &.conversion { background: #fdf6ec; color: #E6A23C; }
      &.risk { background: #fef0f0; color: #F56C6C; }
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
    
    &.traffic { color: #409EFF; background: #ecf5ff; }
    &.fission { color: #8b5cf6; background: #f5f3ff; }
    &.marketing { color: #f97316; background: #fff7ed; }
    &.sync { color: #10b981; background: #ecfdf5; }
    &.active-acq { color: #F56C6C; background: #fef0f0; }
    &.passive-acq { color: #67C23A; background: #f0f9eb; }
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
        
        &.attract { background: #eff6ff; color: #3b82f6; }
        &.retain { background: #f5f3ff; color: #8b5cf6; }
        &.nurture { background: #fff7ed; color: #f97316; }
        &.convert { background: #ecfdf5; color: #10b981; }
        &.fission-step { background: #fff1f2; color: #f43f5e; }
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
