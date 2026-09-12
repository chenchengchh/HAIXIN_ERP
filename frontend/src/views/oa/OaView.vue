<template>
  <div class="oa-view">
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

    <!-- 主视图：OA 审批工作台 -->
    <div v-else class="main-dashboard">
      <!-- 顶部横幅 -->
      <div class="view-header">
        <div class="header-content">
          <h2>OA 审批工作台</h2>
          <p class="subtitle">统一审批入口，贯穿 CRM / SCM / ERP / WMS / MES 等全业务系统的审批闭环</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" :icon="Plus" @click="navigateToCrossModuleApproval">
            发起跨模块审批
          </el-button>
          <el-button :icon="Refresh" @click="refreshAll" :loading="statsLoading">刷新</el-button>
        </div>
      </div>

      <!-- 审批概览统计卡片（真实数据） -->
      <div class="stats-overview" v-loading="statsLoading">
        <div class="stat-item pending" @click="navigateToModule('approval')">
          <div class="stat-icon pending">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">我的待办</div>
            <div class="stat-value warning">{{ totalStats.pending }}</div>
            <div class="stat-sub">待我审批任务</div>
          </div>
        </div>
        <div class="stat-item" @click="navigateToApproved">
          <div class="stat-icon approved">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">我的已办</div>
            <div class="stat-value success">{{ totalStats.completed }}</div>
            <div class="stat-sub">已处理审批</div>
          </div>
        </div>
        <div class="stat-item" @click="navigateToInitiated">
          <div class="stat-icon initiated">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">我发起的</div>
            <div class="stat-value">{{ totalStats.initiated }}</div>
            <div class="stat-sub">审批申请数量</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon total">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">审批总数</div>
            <div class="stat-value">{{ totalStats.totalInstances }}</div>
            <div class="stat-sub">
              通过 <span class="inline-success">{{ totalStats.approved }}</span> ·
              拒绝 <span class="inline-danger">{{ totalStats.rejected }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主体区域：我的待办 Top5 快览（单栏） -->
      <div class="main-content">
        <div class="todo-panel">
          <div class="panel-header">
            <h3 class="panel-title">
              <el-icon><Bell /></el-icon>
              我的待办审批
              <el-tag v-if="todoList.length > 0" size="small" type="warning" round>{{ todoList.length }}</el-tag>
            </h3>
            <el-button link type="primary" @click="navigateToModule('approval')">
              查看全部<el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div v-loading="todoLoading">
            <div v-if="todoList.length === 0 && !todoLoading" class="empty-todo">
              <el-empty description="暂无待办审批任务" :image-size="80" />
            </div>
            <div v-else class="todo-list">
              <div
                v-for="task in todoList.slice(0, 5)"
                :key="task.id"
                class="todo-item"
              >
                <div class="todo-main">
                  <div class="todo-title">{{ task.name || '未命名任务' }}</div>
                  <div class="todo-meta">
                    <el-tag size="small" type="info" v-if="task.assigneeRole">{{ task.assigneeRole }}</el-tag>
                    <span class="meta-node">{{ task.nodeName || '-' }}</span>
                    <span class="meta-time">{{ task.createTime || '-' }}</span>
                  </div>
                </div>
                <div class="todo-actions">
                  <el-button link type="success" size="small" @click="openProcessDialog(task, 'approve')">
                    <el-icon><Check /></el-icon>通过
                  </el-button>
                  <el-button link type="danger" size="small" @click="openProcessDialog(task, 'reject')">
                    <el-icon><Close /></el-icon>拒绝
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷入口（弱化） -->
      <div class="quick-entry">
        <div class="quick-card" @click="navigateToModule('approval')">
          <div class="quick-icon approval"><el-icon><Stamp /></el-icon></div>
          <div class="quick-info">
            <div class="quick-title">流程审批</div>
            <div class="quick-desc">待办 / 已办 / 发起 / 模板 / 统计</div>
          </div>
        </div>
        <div class="quick-card" @click="navigateToModule('document')">
          <div class="quick-icon document"><el-icon><Folder /></el-icon></div>
          <div class="quick-info">
            <div class="quick-title">文档管理</div>
            <div class="quick-desc">企业知识资产全生命周期</div>
          </div>
        </div>
        <div class="quick-card" @click="navigateToModule('collaboration')">
          <div class="quick-icon collaboration"><el-icon><ChatDotRound /></el-icon></div>
          <div class="quick-info">
            <div class="quick-title">协同办公</div>
            <div class="quick-desc">日程 / 任务 / 公告通知</div>
          </div>
        </div>
        <div class="quick-card" @click="navigateToModule('admin')">
          <div class="quick-icon admin"><el-icon><OfficeBuilding /></el-icon></div>
          <div class="quick-info">
            <div class="quick-title">行政管理</div>
            <div class="quick-desc">会议 / 资产 / 车辆管理</div>
          </div>
        </div>
      </div>

      <!-- 审批操作对话框（公共组件，携带 approverId 进行权限校验） -->
      <ApprovalProcessDialog
        v-model:visible="processDialogVisible"
        :loading="processLoading"
        :action="processForm.action"
        @confirm="(comment: string) => confirmProcess(comment, onApprovalSuccess)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ArrowRight, Plus, Refresh, Bell, Check, Close, EditPen, Document,
  Stamp, Folder, ChatDotRound, OfficeBuilding
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { unifiedApprovalApi } from '@/api/oa'
import type { ApprovalTask } from '@/api/oa'
import { unwrapResponseData } from '@/api'
import { useApprovalProcess } from '@/composables/useApprovalProcess'
import ApprovalProcessDialog from '@/components/oa/ApprovalProcessDialog.vue'

const router = useRouter()
const route = useRoute()

/** 子路由判断（用于切换主视图与功能详情视图） */
const isChildRoute = computed(() => {
  return route.matched.length > 2
})

/** 总体统计数据（接入 unifiedApprovalApi.getStatistics 真实数据） */
const totalStats = reactive({
  totalInstances: 0,   // 审批总数
  pending: 0,          // 我的待办数
  completed: 0,        // 我的已办数
  initiated: 0,        // 我发起的数
  approved: 0,         // 已通过数
  rejected: 0          // 已拒绝数
})

const statsLoading = ref(false)

/** 我的待办列表（Top5 快览） */
const todoList = ref<ApprovalTask[]>([])
const todoLoading = ref(false)

/** 接入公共审批操作 composable（通过 / 拒绝 待办任务，自动携带 approverId） */
const {
  processDialogVisible, processLoading, processForm,
  openProcessDialog, confirmProcess, getCurrentUser
} = useApprovalProcess()

/**
 * 获取当前登录用户ID
 */
const currentUserId = (): number => getCurrentUser().id

/**
 * 获取审批统计数据（总体：总数/已通过/已拒绝）
 * <p>注意：后端统计字段为 totalInstances（避免与分页 total 冲突），
 * pending/completed/initiated 需结合当前用户的待办/已办/发起数量。</p>
 */
const fetchStatistics = async () => {
  statsLoading.value = true
  try {
    const response = await unifiedApprovalApi.getStatistics()
    const data = unwrapResponseData<any>(response, {}) || {}

    // 总体统计
    totalStats.totalInstances = Number(data.totalInstances ?? 0)
    totalStats.approved = Number(data.approved ?? 0)
    totalStats.rejected = Number(data.rejected ?? 0)
  } catch (error: any) {
    console.error('获取审批统计失败:', error)
    totalStats.totalInstances = 0
    totalStats.approved = 0
    totalStats.rejected = 0
    ElMessage.warning('审批统计获取失败，请稍后重试')
  } finally {
    statsLoading.value = false
  }
}

/**
 * 获取我的待办审批列表（用于工作台 Top5 快览）
 */
const fetchTodoList = async () => {
  todoLoading.value = true
  try {
    const response = await unifiedApprovalApi.getTodoList(currentUserId(), { page: 1, size: 5 })
    const data = unwrapResponseData<any>(response, {}) || {}
    todoList.value = data.records || data.list || []
    // 待办数同步到统计卡片
    totalStats.pending = Number(data.total ?? todoList.value.length)
  } catch (error: any) {
    console.error('获取待办列表失败:', error)
    todoList.value = []
    totalStats.pending = 0
  } finally {
    todoLoading.value = false
  }
}

/**
 * 获取我的已办审批数量（用于工作台统计卡片）
 */
const fetchDoneCount = async () => {
  try {
    const response = await unifiedApprovalApi.getDoneList(currentUserId(), { page: 1, size: 1 })
    const data = unwrapResponseData<any>(response, {}) || {}
    totalStats.completed = Number(data.total ?? 0)
  } catch (error) {
    console.error('获取已办数量失败:', error)
    totalStats.completed = 0
  }
}

/**
 * 获取我发起的审批数量（用于工作台统计卡片）
 */
const fetchInitiatedCount = async () => {
  try {
    const response = await unifiedApprovalApi.getInitiatedList(currentUserId(), { page: 1, size: 1 })
    const data = unwrapResponseData<any>(response, {}) || {}
    totalStats.initiated = Number(data.total ?? 0)
  } catch (error) {
    console.error('获取发起数量失败:', error)
    totalStats.initiated = 0
  }
}

/**
 * 刷新全部数据（统计 + 待办 + 已办 + 发起，串行执行确保数据一致性）
 */
const refreshAll = async () => {
  // 并行请求提升性能，但等待所有请求完成确保统计一致性
  await Promise.all([
    fetchStatistics(),
    fetchTodoList(),
    fetchDoneCount(),
    fetchInitiatedCount()
  ])
}

/**
 * 审批操作成功后的回调
 * <p>立即从待办列表中移除已处理任务（乐观更新），然后刷新全部统计数据。
 * 乐观更新可避免等待后端响应期间用户重复点击，提升交互反馈速度。</p>
 */
const onApprovalSuccess = async () => {
  // 乐观更新：立即从待办列表中移除已处理任务，减少用户感知延迟
  const processedTaskId = processForm.taskId
  if (processedTaskId) {
    todoList.value = todoList.value.filter((task) => task.id !== processedTaskId)
  }
  // 后台刷新全部统计，确保数据最终一致
  await refreshAll()
}

/** 跳转到指定模块 */
const navigateToModule = (module: string) => {
  router.push(`/home/oa/${module}`)
}

/** 跳转到跨模块审批中心 */
const navigateToCrossModuleApproval = () => {
  router.push('/home/oa/approval')
}

/** 跳转到"我已审批"标签页 */
const navigateToApproved = () => {
  router.push('/home/oa/approval')
}

/** 跳转到"我发起的"标签页 */
const navigateToInitiated = () => {
  router.push('/home/oa/approval')
}

/** 返回上一页 */
const goBack = () => {
  router.back()
}

/** 组件挂载时加载工作台数据 */
onMounted(() => {
  refreshAll()
})
</script>

<style scoped lang="scss">
.oa-view {
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

/* 顶部横幅 */
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-content {
    h2 {
      font-size: 26px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }

    .subtitle {
      font-size: 14px;
      color: var(--text-secondary);
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

/* 统计概览 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 24px;

  .stat-item {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    }

    .stat-icon {
      width: 52px;
      height: 52px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 26px;
      margin-right: 16px;
      flex-shrink: 0;

      &.pending { background: #fdf6ec; color: #e6a23c; }
      &.approved { background: #f0f9eb; color: #67c23a; }
      &.initiated { background: #ecf5ff; color: #409eff; }
      &.total { background: #f4f4f5; color: #909399; }
    }

    .stat-info {
      flex: 1;
      min-width: 0;

      .stat-label {
        font-size: 13px;
        color: var(--text-secondary);
        margin-bottom: 6px;
      }

      .stat-value {
        font-size: 26px;
        font-weight: 700;
        color: var(--text-primary);
        line-height: 1.1;

        &.warning { color: #e6a23c; }
        &.success { color: #67c23a; }
      }

      .stat-sub {
        font-size: 12px;
        color: var(--text-secondary);
        margin-top: 6px;

        .inline-success { color: #67c23a; font-weight: 600; }
        .inline-danger { color: #f56c6c; font-weight: 600; }
      }
    }
  }
}

/* 主体区域（单栏布局） */
.main-content {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

/* 待办面板 */
.todo-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid var(--border-color-light);

    .panel-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;

      .el-icon { color: var(--primary-color); }
    }
  }
}

.empty-todo {
  padding: 24px 0;
}

.todo-list {
  .todo-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 8px;
    border-radius: 8px;
    transition: background 0.2s;

    &:hover {
      background: var(--bg-color-page);
    }

    & + .todo-item {
      border-top: 1px dashed var(--border-color-light);
    }

    .todo-main {
      flex: 1;
      min-width: 0;

      .todo-title {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary);
        margin-bottom: 6px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .todo-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        color: var(--text-secondary);

        .meta-node {
          background: var(--bg-color-page);
          padding: 2px 6px;
          border-radius: 4px;
        }

        .meta-time {
          margin-left: auto;
        }
      }
    }

    .todo-actions {
      display: flex;
      gap: 4px;
      flex-shrink: 0;
      margin-left: 12px;
    }
  }
}

/* 快捷入口 */
.quick-entry {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;

  .quick-card {
    background: #fff;
    border-radius: 10px;
    padding: 16px;
    display: flex;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);

      .quick-icon { transform: scale(1.1); }
    }

    .quick-icon {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
      margin-right: 12px;
      transition: transform 0.3s ease;

      &.approval { background: #ecf5ff; color: #409eff; }
      &.document { background: #f5f3ff; color: #8b5cf6; }
      &.collaboration { background: #fff7ed; color: #f97316; }
      &.admin { background: #ecfdf5; color: #10b981; }
    }

    .quick-info {
      flex: 1;

      .quick-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-primary);
        margin-bottom: 2px;
      }

      .quick-desc {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }
}

@media (max-width: 768px) {
  .stats-overview {
    grid-template-columns: 1fr 1fr;
  }

  .view-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
