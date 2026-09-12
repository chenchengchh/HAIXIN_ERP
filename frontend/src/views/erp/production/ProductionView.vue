<template>
  <div class="production-view">
    <SubModuleHeader title="生产管理" parentTitle="ERP 企业资源计划" parentPath="/home/erp" />
    
    <!-- 生产模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="生产订单" name="orders">
          <OrdersView />
        </el-tab-pane>
        <el-tab-pane label="车间管理" name="workshop">
          <WorkshopView />
        </el-tab-pane>
        <el-tab-pane label="产能管理" name="capacity">
          <CapacityView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import OrdersView from './OrdersView.vue'
import WorkshopView from './WorkshopView.vue'
import CapacityView from './CapacityView.vue'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'

// 活跃标签
const activeTab = ref<string>('orders')
</script>

<style scoped>
.production-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}



.module-nav {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .production-view {
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