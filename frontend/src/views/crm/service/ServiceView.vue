<template>
  <div class="service-view">
    <div class="page-header">
      <h2>客户服务</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/crm' }">CRM系统</el-breadcrumb-item>
        <el-breadcrumb-item>客户服务</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 客户服务模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="服务工单" name="tickets">
          <TicketsView />
        </el-tab-pane>
        <el-tab-pane label="工单回复" name="ticket-replies">
          <TicketRepliesView />
        </el-tab-pane>
        <el-tab-pane label="知识库" name="knowledge">
          <KnowledgeView />
        </el-tab-pane>
        <el-tab-pane label="满意度调查" name="surveys">
          <SurveysView />
        </el-tab-pane>
        <el-tab-pane label="NPS调查" name="nps">
          <NPSSurveysView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import TicketsView from './TicketsView.vue'
import TicketRepliesView from './TicketRepliesView.vue'
import KnowledgeView from './KnowledgeView.vue'
import SurveysView from './SurveysView.vue'
import NPSSurveysView from './NPSSurveysView.vue'

// 活跃标签
const activeTab = ref<string>('tickets')
</script>

<style scoped>
.service-view {
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

/* 模块导航 */
.module-nav {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .service-view {
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