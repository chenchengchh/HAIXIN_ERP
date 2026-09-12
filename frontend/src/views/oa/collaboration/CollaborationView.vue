<template>
  <div class="collaboration-view">
    <div class="page-header">
      <h2>协同办公</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/oa' }">OA系统</el-breadcrumb-item>
        <el-breadcrumb-item>协同办公</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 协同办公模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="即时沟通" name="chat">
          <ChatView />
        </el-tab-pane>
        <el-tab-pane label="日程管理" name="calendar">
          <CalendarView />
        </el-tab-pane>
        <el-tab-pane label="任务管理" name="tasks">
          <TaskManagementView />
        </el-tab-pane>
        <el-tab-pane label="会议管理" name="meetings">
          <MeetingManagementView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import ChatView from './ChatView.vue'
import CalendarView from './CalendarView.vue'
import TaskManagementView from './TaskManagementView.vue'
import MeetingManagementView from './MeetingManagementView.vue'

// 活跃标签
const activeTab = ref<string>('chat')
</script>

<style scoped>
.collaboration-view {
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
  .collaboration-view {
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