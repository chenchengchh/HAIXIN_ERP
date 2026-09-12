<template>
  <div class="hr-view">
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
          <h2>HR 人力资源</h2>
          <p class="subtitle">全员全生命周期管理，打造高效能组织与人才梯队</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus">
            新建员工
          </el-button>
        </div>
      </div>
      
      <!-- 数据概览统计 -->
      <div class="stats-overview">
        <div class="stat-item">
          <div class="stat-icon employee">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">在职员工</div>
            <div class="stat-value">{{ stats.activeEmployees }}</div>
            <div class="stat-sub">共 {{ stats.totalEmployees }} 名员工档案</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon attendance">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">考勤正常率</div>
            <div class="stat-value">{{ stats.attendanceNormalRate }}<span class="unit">%</span></div>
            <div class="stat-sub">共 {{ stats.totalAttendance }} 条考勤记录</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon recruitment">
            <el-icon><Suitcase /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">招聘需求</div>
            <div class="stat-value">{{ stats.totalDemands }}</div>
            <div class="stat-sub">简历库 {{ stats.totalResumes }} 份</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon training">
            <el-icon><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">培训计划</div>
            <div class="stat-value">{{ stats.totalTrainings }}<span class="unit">个</span></div>
            <div class="stat-sub">参与 {{ stats.totalParticipants }} 人次</div>
          </div>
        </div>
      </div>

      <!-- 核心模块卡片 -->
      <div class="module-cards">
        <!-- 组织与员工 -->
        <div class="module-card" @click="navigateToModule('org-employee')">
          <div class="card-icon org">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="card-info">
            <h3>组织与员工</h3>
            <p>组织架构与员工档案管理</p>
            <div class="features-list">
              <span><el-icon><OfficeBuilding /></el-icon> 部门管理</span>
              <span><el-icon><Postcard /></el-icon> 员工档案</span>
              <span><el-icon><Switch /></el-icon> 入转调离</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 招聘管理 -->
        <div class="module-card" @click="navigateToModule('recruitment')">
          <div class="card-icon recruit">
            <el-icon><Suitcase /></el-icon>
          </div>
          <div class="card-info">
            <h3>招聘管理</h3>
            <p>人才招聘全流程数字化管理</p>
            <div class="features-list">
              <span><el-icon><Document /></el-icon> 简历库</span>
              <span><el-icon><ChatLineSquare /></el-icon> 面试安排</span>
              <span><el-icon><Message /></el-icon> Offer管理</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 考勤与假期 -->
        <div class="module-card" @click="navigateToModule('attendance-leave')">
          <div class="card-icon attend">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="card-info">
            <h3>考勤与假期</h3>
            <p>智能排班与考勤假期管理</p>
            <div class="features-list">
              <span><el-icon><Calendar /></el-icon> 排班管理</span>
              <span><el-icon><Clock /></el-icon> 打卡记录</span>
              <span><el-icon><Coffee /></el-icon> 请假管理</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- 薪酬福利 -->
        <div class="module-card" @click="navigateToModule('compensation')">
          <div class="card-icon salary">
            <el-icon><Money /></el-icon>
          </div>
          <div class="card-info">
            <h3>薪酬福利</h3>
            <p>薪资核算与福利发放管理</p>
            <div class="features-list">
              <span><el-icon><Wallet /></el-icon> 薪资核算</span>
              <span><el-icon><Umbrella /></el-icon> 社保公积金</span>
              <span><el-icon><Tickets /></el-icon> 工资条</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>

        <!-- HR集成事件（F5 前端配套：员工事件向OA/ERP投递监控） -->
        <div class="module-card" @click="navigateToModule('integration')">
          <div class="card-icon integration">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="card-info">
            <h3>HR集成事件</h3>
            <p>员工事件投递与闭环监控</p>
            <div class="features-list">
              <span><el-icon><Connection /></el-icon> 事件查询</span>
              <span><el-icon><Promotion /></el-icon> 投递状态</span>
              <span><el-icon><DataLine /></el-icon> 闭环验证</span>
            </div>
          </div>
          <div class="card-action">
            <span class="action-text">进入模块</span>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 人才全生命周期流程 -->
      <div class="process-flow">
        <div class="section-title">
          <el-icon><Connection /></el-icon>
          <span>人才全生命周期</span>
        </div>
        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-icon recruit-step">
              <el-icon><Suitcase /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">招聘入职</div>
              <div class="step-desc">需求/面试/Offer</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon train-step">
              <el-icon><School /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">培训发展</div>
              <div class="step-desc">技能/晋升培训</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon perform-step">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">绩效考核</div>
              <div class="step-desc">目标/评估/反馈</div>
            </div>
            <div class="step-arrow"><el-icon><Right /></el-icon></div>
          </div>
          
          <div class="flow-step">
            <div class="step-icon retain-step">
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="step-content">
              <div class="step-title">薪酬激励</div>
              <div class="step-desc">调薪/奖金/期权</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { hrApi } from '../../api/hr'
import { unwrapListResponse } from '../../api'
import {
  UserFilled,
  Suitcase,
  Timer,
  Money,
  ArrowRight,
  Connection,
  Right,
  School,
  TrendCharts,
  Trophy,
  Plus,
  OfficeBuilding,
  Postcard,
  Switch,
  Document,
  ChatLineSquare,
  Message,
  Calendar,
  Clock,
  Coffee,
  Wallet,
  Umbrella,
  Tickets,
  Promotion,
  DataLine
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isChildRoute = computed(() => {
  return route.matched.length > 2
})

// Dashboard统计数据（真实API数据）
const stats = ref({
  activeEmployees: 0,
  totalEmployees: 0,
  attendanceNormalRate: 0,
  totalAttendance: 0,
  totalDemands: 0,
  totalResumes: 0,
  totalTrainings: 0,
  totalParticipants: 0
})

/**
 * 加载Dashboard统计数据
 */
const fetchStats = async () => {
  try {
    // 并发加载各模块数据（考勤使用大分页获取全量记录）
    const [empRes, attRes, demandRes, resumeRes, trainRes, partRes] = await Promise.all([
      hrApi.employee.getAll(),
      hrApi.attendance.getRecordsByPage({ page: 1, size: 1000 }),
      hrApi.recruitment.getDemands(),
      hrApi.recruitment.getResumes(),
      hrApi.training.plan.getAll(),
      hrApi.training.participant.getAll()
    ])

    // 在职员工统计（status为ACTIVE）
    const employees = unwrapListResponse<any>(empRes)
    stats.value.totalEmployees = employees.length
    stats.value.activeEmployees = employees.filter((e: any) => e.status === 'ACTIVE' || e.status === 1).length

    // 考勤正常率统计（NORMAL状态占比）
    const records = unwrapListResponse<any>(attRes)
    stats.value.totalAttendance = records.length
    if (records.length > 0) {
      const normalCount = records.filter((r: any) => r.status === 'NORMAL').length
      stats.value.attendanceNormalRate = Math.round((normalCount / records.length) * 1000) / 10
    }

    // 招聘需求与简历统计
    stats.value.totalDemands = unwrapListResponse<any>(demandRes).length
    stats.value.totalResumes = unwrapListResponse<any>(resumeRes).length

    // 培训计划与参与统计
    stats.value.totalTrainings = unwrapListResponse<any>(trainRes).length
    stats.value.totalParticipants = unwrapListResponse<any>(partRes).length
  } catch (error) {
    console.error('加载HR统计数据失败:', error)
  }
}

const navigateToModule = (module: string) => {
  router.push(`/home/hr/${module}`)
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped lang="scss">
.hr-view {
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
      
      &.employee { background: #e6f7ff; color: #1890ff; }
      &.attendance { background: #fff7e6; color: #fa8c16; }
      &.recruitment { background: #f6ffed; color: #52c41a; }
      &.training { background: #f5f3ff; color: #722ed1; }
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
    
    &.org { background: #e6f7ff; color: #1890ff; }
    &.recruit { background: #f6ffed; color: #52c41a; }
    &.attend { background: #fff7e6; color: #fa8c16; }
    &.salary { background: #fff0f6; color: #eb2f96; }
    &.integration { background: #f0f5ff; color: #2f54eb; }
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
        
        &.recruit-step { background: #e6f7ff; color: #1890ff; }
        &.train-step { background: #f6ffed; color: #52c41a; }
        &.perform-step { background: #fff7e6; color: #fa8c16; }
        &.retain-step { background: #fff0f6; color: #eb2f96; }
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
