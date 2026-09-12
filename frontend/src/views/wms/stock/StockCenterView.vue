<template>
  <div class="stock-center-view">
    <div class="page-header">
      <h2>库存中心</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms">WMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms/stock">库存中心</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="库存可视化" name="inventory-viz">
          <inventory-viz />
        </el-tab-pane>
        <el-tab-pane label="盘点录入" name="count-job">
          <count-job />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useVoiceContext } from '../../../composables/useVoiceContext'
// 导入子组件
import InventoryViz from './inventory-viz.vue';
import CountJob from './count-job.vue';

const tabLabelMap: Record<string, string> = {
  'inventory-viz': '库存可视化',
  'count-job': '盘点录入'
}

// 当前激活的标签页
const activeTab = ref('inventory-viz');

useVoiceContext({
  id: 'stock-center',
  description: '库存中心',
  actions: {
    'view_inventory': () => { activeTab.value = 'inventory-viz' },
    'count_job': () => { activeTab.value = 'count-job' },
    // Alias for Chinese commands (handled roughly by Mock NLP alias logic or explicit keys here)
    'search': () => { activeTab.value = 'inventory-viz' // Default to visualization
    }
  }
})
</script>

<style scoped>
.stock-center-view {
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
  .stock-center-view {
    padding: 0;
  }
}
</style>
