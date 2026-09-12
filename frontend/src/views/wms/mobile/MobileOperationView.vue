<template>
  <div class="mobile-operation-view">
    <div class="page-header">
      <h2>PDA作业</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms">WMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms/mobile">PDA作业</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="上架扫描" name="in-shelve">
          <InShelveScan />
        </el-tab-pane>
        <el-tab-pane label="拣货扫描" name="out-pick">
          <OutPickScan />
        </el-tab-pane>
        <el-tab-pane label="扫码查库存" name="stock-query">
          <StockQueryScan />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

// 导入组件
import InShelveScan from './in-shelve-scan.vue';
import OutPickScan from './out-pick-scan.vue';
import StockQueryScan from './stock-query-scan.vue';

const tabLabelMap: Record<string, string> = {
  'in-shelve': '上架扫描',
  'out-pick': '拣货扫描',
  'stock-query': '扫码查库存'
}

// 当前激活的标签页
const activeTab = ref('in-shelve');
</script>

<style scoped>
.mobile-operation-view {
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
  width: 100%;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  background-color: #fafafa;
  border: 1px dashed #ccc;
  color: #999;
  font-size: 16px;
  margin: 20px;
}

/* 统一标签页样式 */
:deep(.el-tabs__header) {
  margin-bottom: 0;
}

:deep(.el-tabs__content) {
  padding: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .mobile-operation-view {
    padding: 0;
  }
}
</style>
