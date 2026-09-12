<template>
  <div class="order-view">
    <div class="page-header">
      <h2>订单管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/crm' }">CRM系统</el-breadcrumb-item>
        <el-breadcrumb-item>订单管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 订单管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="销售订单" name="sales-orders">
          <SalesOrdersView />
        </el-tab-pane>
        <el-tab-pane label="订单明细" name="order-items">
          <OrderItemsView />
        </el-tab-pane>
        <el-tab-pane label="合同管理" name="contracts">
          <ContractsView />
        </el-tab-pane>
        <el-tab-pane label="合同附件" name="contract-attachments">
          <ContractAttachmentsView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import SalesOrdersView from './SalesOrdersView.vue'
import OrderItemsView from './OrderItemsView.vue'
import ContractsView from './ContractsView.vue'
import ContractAttachmentsView from './ContractAttachmentsView.vue'

// 活跃标签
const activeTab = ref<string>('sales-orders')
</script>

<style scoped>
.order-view {
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
  .order-view {
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