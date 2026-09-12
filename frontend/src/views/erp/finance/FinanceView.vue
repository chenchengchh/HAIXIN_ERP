<template>
  <div class="finance-view">
    <SubModuleHeader title="财务管理" parentTitle="ERP 企业资源计划" parentPath="/home/erp" />
    
    <!-- 财务模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="总账管理" name="general-ledger">
          <GeneralLedgerView />
        </el-tab-pane>
        <el-tab-pane label="凭证管理" name="vouchers">
          <VoucherView />
        </el-tab-pane>
        <el-tab-pane label="应收管理" name="accounts-receivable">
          <AccountsReceivableView />
        </el-tab-pane>
        <el-tab-pane label="应付管理" name="accounts-payable">
          <AccountsPayableView />
        </el-tab-pane>
        <el-tab-pane label="成本核算" name="cost-accounting">
          <CostAccountingView />
        </el-tab-pane>
        <el-tab-pane label="固定资产" name="fixed-assets">
          <FixedAssetsView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import GeneralLedgerView from './GeneralLedgerView.vue'
import VoucherView from './VoucherView.vue'
import AccountsReceivableView from './AccountsReceivableView.vue'
import AccountsPayableView from './AccountsPayableView.vue'
import CostAccountingView from './CostAccountingView.vue'
import FixedAssetsView from './FixedAssetsView.vue'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'

// 活跃标签
const activeTab = ref<string>('general-ledger')
</script>

<style scoped>
.finance-view {
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
  .finance-view {
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
