<template>
  <div class="purchase-automation-view">
    <div class="page-header">
      <h2>采购流程自动化</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/srm' }">SRM系统</el-breadcrumb-item>
        <el-breadcrumb-item>采购流程自动化</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 采购流程自动化模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="采购申请" name="purchase-request">
          <PurchaseRequestView />
        </el-tab-pane>
        <el-tab-pane label="询价管理" name="inquiry-management">
          <InquiryView />
        </el-tab-pane>
        <el-tab-pane label="报价管理" name="quotation-management">
          <QuotationView />
        </el-tab-pane>
        <el-tab-pane label="比价分析" name="price-comparison">
          <PriceComparisonView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import PurchaseRequestView from './purchase/PurchaseRequestView.vue'
import InquiryView from './purchase/InquiryView.vue'
import QuotationView from './purchase/QuotationView.vue'
import PriceComparisonView from './purchase/PriceComparisonView.vue'

// 活跃标签
const activeTab = ref<string>('purchase-request')
</script>

<style scoped>
.purchase-automation-view {
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
  .purchase-automation-view {
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
