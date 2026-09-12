<template>
  <div class="attendance-leave-view">
    <div class="page-header">
      <h2>考勤与假期管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/hr' }">HR系统</el-breadcrumb-item>
        <el-breadcrumb-item>考勤与假期管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 考勤与假期管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="考勤数据" name="attendance-data">
          <AttendanceDataView />
        </el-tab-pane>
        <el-tab-pane label="考勤规则" name="attendance-rules">
          <AttendanceRulesView />
        </el-tab-pane>
        <el-tab-pane label="假期管理" name="leave-management">
          <LeaveManagementView />
        </el-tab-pane>
        <el-tab-pane label="考勤异常" name="attendance-exception">
          <AttendanceExceptionView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import AttendanceDataView from '../AttendanceDataView.vue'
import AttendanceRulesView from '../AttendanceRulesView.vue'
import LeaveManagementView from '../LeaveManagementView.vue'
import AttendanceExceptionView from '../AttendanceExceptionView.vue'

// 活跃标签
const activeTab = ref<string>('attendance-data')
</script>

<style scoped>
.attendance-leave-view {
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
  .attendance-leave-view {
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