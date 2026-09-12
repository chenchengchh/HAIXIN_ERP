<template>
  <div class="collaboration-view">
    <SubModuleHeader title="供应商协同管理" parentTitle="SRM 供应商关系管理" parentPath="/home/srm" />
    
    <!-- 供应商协同模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="订单进度共享" name="order-progress">
          <OrderProgressView />
        </el-tab-pane>
        <el-tab-pane label="来料预报" name="material-forecast">
          <MaterialForecastView />
        </el-tab-pane>
        <el-tab-pane label="质量异议反馈" name="quality-objection">
          <QualityObjectionView />
        </el-tab-pane>
        <el-tab-pane label="对账结算协同" name="reconciliation">
          <ReconciliationView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import OrderProgressView from './OrderProgressView.vue'
import MaterialForecastView from './MaterialForecastView.vue'
import QualityObjectionView from './QualityObjectionView.vue'
import ReconciliationView from './ReconciliationView.vue'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'

// 活跃标签
const activeTab = ref<string>('order-progress')
</script>

<style scoped>
.collaboration-view {
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
  .collaboration-view {
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
