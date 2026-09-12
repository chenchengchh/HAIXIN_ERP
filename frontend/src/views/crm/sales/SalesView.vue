<template>
  <div class="sales-view">
    <div class="page-header">
      <h2>销售管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/crm' }">CRM系统</el-breadcrumb-item>
        <el-breadcrumb-item>销售管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 销售管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="线索管理" name="leads">
          <LeadsView />
        </el-tab-pane>
        <el-tab-pane label="商机管理" name="opportunities">
          <OpportunitiesView />
        </el-tab-pane>
        <el-tab-pane label="销售漏斗" name="funnel">
          <SalesFunnelView />
        </el-tab-pane>
        <el-tab-pane label="销售预测" name="forecast">
          <SalesForecastView />
        </el-tab-pane>
        <el-tab-pane label="销售目标" name="targets">
          <SalesTargetsView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import LeadsView from './LeadsView.vue'
import OpportunitiesView from './OpportunitiesView.vue'
import SalesFunnelView from './SalesFunnelView.vue'
import SalesForecastView from './SalesForecastView.vue'
import SalesTargetsView from './SalesTargetsView.vue'

// 活跃标签
const activeTab = ref<string>('leads')
</script>

<style scoped>
.sales-view {
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
  .sales-view {
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
