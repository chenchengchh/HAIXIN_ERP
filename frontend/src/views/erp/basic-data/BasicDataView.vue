<template>
  <div class="basic-data-view">
    <SubModuleHeader title="基础数据管理" parentTitle="ERP 企业资源计划" parentPath="/home/erp" />
    
    <!-- 基础数据模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTabParent" type="card" lazy>
        <el-tab-pane label="组织架构" name="organization">
          <OrganizationView />
        </el-tab-pane>
        <el-tab-pane label="客户管理" name="customers">
          <CustomersView />
        </el-tab-pane>
        <el-tab-pane label="供应商管理" name="suppliers">
          <SuppliersView />
        </el-tab-pane>
        <el-tab-pane label="物料管理" name="materials">
          <MaterialsView />
        </el-tab-pane>
        <el-tab-pane label="仓库管理" name="warehouses">
          <WarehousesView />
        </el-tab-pane>
        <el-tab-pane label="部门管理" name="departments">
          <DepartmentsView />
        </el-tab-pane>
        <el-tab-pane label="岗位管理" name="positions">
          <PositionsView />
        </el-tab-pane>
        <el-tab-pane label="人员管理" name="employees">
          <EmployeesView />
        </el-tab-pane>
        <el-tab-pane label="物料分类" name="bom-categories">
          <BomCategoriesView />
        </el-tab-pane>
        <el-tab-pane label="BOM查询" name="boms">
          <BomView />
        </el-tab-pane>
        <el-tab-pane label="会计科目" name="accounts">
          <AccountsView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import OrganizationView from './OrganizationView.vue'
import CustomersView from './CustomersView.vue'
import SuppliersView from './SuppliersView.vue'
import MaterialsView from './MaterialsView.vue'
import AccountsView from './AccountsView.vue'
import WarehousesView from './WarehousesView.vue'
import DepartmentsView from './DepartmentsView.vue'
import PositionsView from './PositionsView.vue'
import EmployeesView from './EmployeesView.vue'
import BomCategoriesView from './BomCategoriesView.vue'
import BomView from './BomView.vue'
import SubModuleHeader from '../../../components/common/SubModuleHeader.vue'

// 活跃标签（父组件）
const activeTabParent = ref<string>('organization')
</script>

<style scoped>
.basic-data-view {
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
  .basic-data-view {
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
