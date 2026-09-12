<template>
  <div class="admin-view">
    <div class="page-header">
      <h2>行政后勤</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/oa' }">OA系统</el-breadcrumb-item>
        <el-breadcrumb-item>行政后勤</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 行政后勤模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="办公用品管理" name="supplies">
          <SuppliesManagementView />
        </el-tab-pane>
        <el-tab-pane label="会议室预约" name="meeting-rooms">
          <MeetingRoomReservationView />
        </el-tab-pane>
        <el-tab-pane label="车辆调度" name="vehicles">
          <VehicleManagementView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import SuppliesManagementView from './SuppliesManagementView.vue'
import MeetingRoomReservationView from './MeetingRoomReservationView.vue'
import VehicleManagementView from './VehicleManagementView.vue'

// 活跃标签
const activeTab = ref<string>('supplies')
</script>

<style scoped>
.admin-view {
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

.module-nav {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-view {
    padding: 12px;
  }
  
  .page-header {
    margin-bottom: 15px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
}
</style>
