<template>
  <div class="recruitment-view">
    <div class="page-header">
      <h2>招聘管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/hr' }">HR系统</el-breadcrumb-item>
        <el-breadcrumb-item>招聘管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 招聘管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="招聘需求" name="recruitment-demand">
          <RecruitmentDemandView />
        </el-tab-pane>
        <el-tab-pane label="简历筛选" name="resume-screening">
          <ResumeScreeningView />
        </el-tab-pane>
        <el-tab-pane label="面试流程" name="interview-process">
          <InterviewProcessView />
        </el-tab-pane>
        <el-tab-pane label="录用审批" name="hiring-approval">
          <HiringApprovalView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import RecruitmentDemandView from '../RecruitmentDemandView.vue'
import ResumeScreeningView from '../ResumeScreeningView.vue'
import InterviewProcessView from '../InterviewProcessView.vue'
import HiringApprovalView from '../HiringApprovalView.vue'

// 活跃标签
const activeTab = ref<string>('recruitment-demand')
</script>

<style scoped>
.recruitment-view {
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
  .recruitment-view {
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