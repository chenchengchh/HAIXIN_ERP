<template>
  <div class="supply-chain-view">
    <div class="page-header">
      <h2>供应链管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/erp' }">ERP系统</el-breadcrumb-item>
        <el-breadcrumb-item>供应链管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 供应链模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="采购管理" name="purchase">
          <PurchaseView />
        </el-tab-pane>
        <el-tab-pane label="销售管理" name="sales">
          <SalesView />
        </el-tab-pane>
        <el-tab-pane label="库存管理" name="inventory">
          <InventoryView />
        </el-tab-pane>
        <el-tab-pane label="MRP计划" name="mrp">
          <MRPView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import PurchaseView from './PurchaseView.vue'
import SalesView from './SalesView.vue'
import InventoryView from './InventoryView.vue'
import MRPView from './MRPView.vue'

// 活跃标签
const activeTab = ref<string>('purchase')
</script>

<style scoped>
.supply-chain-view {
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
  .supply-chain-view {
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