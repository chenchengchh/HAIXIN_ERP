<template>
  <div class="supplier-access-view">
    <SubModuleHeader title="供应商准入管理" parentTitle="SRM 供应商关系管理" parentPath="/home/srm" />
    
    <!-- 供应商准入管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="供应商列表" name="supplier-list">
          <SupplierListView />
        </el-tab-pane>
        <el-tab-pane label="资质管理" name="qualification-management">
          <SupplierQualificationView />
        </el-tab-pane>
        <el-tab-pane label="信用管理" name="credit-management">
          <SupplierCreditView />
        </el-tab-pane>
        <el-tab-pane label="黑白名单" name="blacklist-management">
          <SupplierBlacklistView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import SupplierListView from './SupplierListView.vue'
import SupplierQualificationView from './SupplierQualificationView.vue'
import SupplierCreditView from './SupplierCreditView.vue'
import SupplierBlacklistView from './SupplierBlacklistView.vue'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'

// 活跃标签
const activeTab = ref<string>('supplier-list')
</script>

<style scoped>
.supplier-access-view {
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
  .supplier-access-view {
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
