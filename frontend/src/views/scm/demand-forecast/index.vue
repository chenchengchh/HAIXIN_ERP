<template>
  <div class="demand-forecast-container">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>需求预测</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scm' }">SCM系统</el-breadcrumb-item>
        <el-breadcrumb-item>需求预测</el-breadcrumb-item>
      </el-breadcrumb>
      <p>基于历史数据和AI算法预测产品需求，支持人工调整和多维度分析</p>
    </div>

    <!-- 标签页导航 -->
    <el-tabs v-model="activeTab" type="border-card" class="module-tabs">
      <!-- 预测概览 -->
      <el-tab-pane label="预测概览" name="dashboard">
        <dashboard-component />
      </el-tab-pane>

      <!-- 预测工作台 -->
      <el-tab-pane label="预测工作台" name="worksheet">
        <worksheet-component />
      </el-tab-pane>

      <!-- 准确率分析 -->
      <el-tab-pane label="准确率分析" name="accuracy">
        <accuracy-component />
      </el-tab-pane>

      <!-- 预测配置 -->
      <el-tab-pane label="预测配置" name="configuration">
        <configuration-component />
      </el-tab-pane>

      <!-- 版本管理 -->
      <el-tab-pane label="版本管理" name="version-management">
        <version-management-component />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import DashboardComponent from '../forecast/dashboard.vue';
import WorksheetComponent from '../forecast/worksheet.vue';
import AccuracyComponent from '../forecast/accuracy.vue';
import ConfigurationComponent from './configuration.vue';
import VersionManagementComponent from './version-management.vue';

// 当前激活的标签页
const activeTab = ref('dashboard');
</script>

<style scoped>
.demand-forecast-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.module-tabs {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .demand-forecast-container {
    padding: 10px;
  }
  
  .page-header {
    margin-bottom: 10px;
  }
  
  .page-header h2 {
    font-size: 18px;
  }
}
</style>