<template>
  <div class="approval-view">
    <div class="page-header">
      <h2>流程审批</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/oa' }">OA系统</el-breadcrumb-item>
        <el-breadcrumb-item>流程审批</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 审批模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="跨模块审批" name="cross-module" lazy>
          <CrossModuleApprovalView />
        </el-tab-pane>
        <el-tab-pane label="待我审批" name="pending" lazy>
          <PendingApprovalView />
        </el-tab-pane>
        <el-tab-pane label="我已审批" name="approved" lazy>
          <ApprovedView />
        </el-tab-pane>
        <el-tab-pane label="我发起的" name="initiated" lazy>
          <InitiatedView />
        </el-tab-pane>
        <el-tab-pane label="流程模板" name="templates" lazy>
          <TemplateManagementView />
        </el-tab-pane>
        <el-tab-pane label="审批统计" name="statistics" lazy>
          <StatisticsView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import CrossModuleApprovalView from './CrossModuleApprovalView.vue'
import PendingApprovalView from './PendingApprovalView.vue'
import ApprovedView from './ApprovedView.vue'
import InitiatedView from './InitiatedView.vue'
import TemplateManagementView from './TemplateManagementView.vue'
import StatisticsView from './StatisticsView.vue'

// 活跃标签（默认展示跨模块审批，凸显OA贯穿全系统的核心入口）
const activeTab = ref<string>('cross-module')
</script>

<style scoped>
.approval-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 1.5rem;
}

.module-nav {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .approval-view {
    padding: 12px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
}
</style>