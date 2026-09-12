<template>
  <div class="org-employee-view">
    <div class="page-header">
      <h2>组织与员工管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/hr' }">HR系统</el-breadcrumb-item>
        <el-breadcrumb-item>组织与员工管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 组织与员工管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="组织架构" name="org-structure">
          <OrgStructureView />
        </el-tab-pane>
        <el-tab-pane label="职位管理" name="position-management">
          <PositionManagementView />
        </el-tab-pane>
        <el-tab-pane label="员工档案" name="employee-profile">
          <EmployeeProfileView />
        </el-tab-pane>
        <el-tab-pane label="员工生命周期" name="employee-lifecycle">
          <EmployeeLifecycleView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import OrgStructureView from '../OrgStructureView.vue'
import PositionManagementView from '../PositionManagementView.vue'
import EmployeeProfileView from '../EmployeeProfileView.vue'
import EmployeeLifecycleView from '../EmployeeLifecycleView.vue'

// 活跃标签
const activeTab = ref<string>('org-structure')
</script>

<style scoped>
.org-employee-view {
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
  .org-employee-view {
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