<template>
  <div class="mobile-office-view">
    <!-- 移动办公模块导航 -->
    <div class="page-header">
      <h2>移动办公</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/oa' }">OA系统</el-breadcrumb-item>
        <el-breadcrumb-item>移动办公</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 移动办公模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="移动端审批" name="approval">
          <MobileApprovalView />
        </el-tab-pane>
        <el-tab-pane label="消息推送" name="messages">
          <MessagePushView />
        </el-tab-pane>
        <el-tab-pane label="考勤打卡" name="attendance">
          <AttendanceView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import MobileApprovalView from './MobileApprovalView.vue'
import MessagePushView from './MessagePushView.vue'
import AttendanceView from './AttendanceView.vue'

// 活跃标签
const activeTab = ref<string>('approval')
</script>

<style scoped>
.mobile-office-view {
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
  .mobile-office-view {
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
