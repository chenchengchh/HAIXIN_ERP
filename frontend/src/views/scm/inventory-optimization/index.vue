<template>
  <div class="inventory-optimization-container">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>库存优化</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scm' }">SCM系统</el-breadcrumb-item>
        <el-breadcrumb-item>库存优化</el-breadcrumb-item>
      </el-breadcrumb>
      <p>优化库存策略，提高库存周转率，降低库存成本</p>
    </div>

    <!-- 标签页导航 -->
    <el-tabs v-model="activeTab" type="border-card" class="module-tabs">
      <!-- 库存策略配置 -->
      <el-tab-pane label="库存策略配置" name="policy">
        <policy-component />
      </el-tab-pane>

      <!-- 库存健康度分析 -->
      <el-tab-pane label="库存健康度分析" name="health-check">
        <health-check-component />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import PolicyComponent from '../inventory/policy.vue';
import HealthCheckComponent from '../inventory/health-check.vue';

// 当前激活的标签页
const activeTab = ref('policy');
</script>

<style scoped>
.inventory-optimization-container {
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
  .inventory-optimization-container {
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