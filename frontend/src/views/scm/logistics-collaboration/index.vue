<template>
  <div class="logistics-collaboration-container">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>物流协同</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scm' }">SCM系统</el-breadcrumb-item>
        <el-breadcrumb-item>物流协同</el-breadcrumb-item>
      </el-breadcrumb>
      <p>与供应商和物流商实时协同，优化运输路径，监控运输状态</p>
    </div>

    <!-- 标签页导航 -->
    <el-tabs v-model="activeTab" type="border-card" class="module-tabs">
      <!-- 供应商协同 -->
      <el-tab-pane label="供应商协同" name="supplier-collaboration">
        <supplier-collaboration-component />
      </el-tab-pane>

      <!-- 物流商管理 -->
      <el-tab-pane label="物流商管理" name="logistics-provider">
        <logistics-provider-component />
      </el-tab-pane>

      <!-- 运输路径优化 -->
      <el-tab-pane label="运输路径优化" name="route-optimization">
        <route-optimization-component />
      </el-tab-pane>

      <!-- 实时运输监控 -->
      <el-tab-pane label="实时运输监控" name="transport-monitoring">
        <transport-monitoring-component />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import SupplierCollaborationComponent from './supplier-collaboration.vue';
import LogisticsProviderComponent from './logistics-provider.vue';
import RouteOptimizationComponent from './route-optimization.vue';
import TransportMonitoringComponent from './transport-monitoring.vue';

// 当前激活的标签页
const activeTab = ref('supplier-collaboration');
</script>

<style scoped>
.logistics-collaboration-container {
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
  .logistics-collaboration-container {
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