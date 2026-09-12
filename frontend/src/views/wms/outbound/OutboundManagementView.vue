<template>
  <div class="outbound-management-view">
    <div class="page-header">
      <h2>出库管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms">WMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms/outbound">出库管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-tabs v-model="activeTab" class="content-tabs" type="card">
      <el-tab-pane label="出库单管理" name="orders">
        <div class="tab-content">
          <OutboundOrderList />
        </div>
      </el-tab-pane>
      <el-tab-pane label="波次管理" name="waves">
        <div class="tab-content">
          <WaveManager />
        </div>
      </el-tab-pane>
      <el-tab-pane label="拣货监控" name="picking">
        <div class="tab-content">
          <PickMonitor />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import WaveManager from './wave-manager.vue'
import PickMonitor from './pick-monitor.vue'
import OutboundOrderList from './outbound-order-list.vue'

const tabLabelMap: Record<string, string> = {
  'orders': '出库单管理',
  'waves': '波次管理',
  'picking': '拣货监控'
}

const activeTab = ref('orders')
</script>

<style scoped lang="scss">
.outbound-management-view {
  padding: 20px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;

  h2 {
    margin: 0 0 10px 0;
    color: #303133;
    font-size: 1.5rem;
  }
}

.content-tabs {
  width: 100%;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;

  :deep(.el-tabs__header) {
    margin-bottom: 0;
    background-color: #fafafa;
    border-bottom: 1px solid #ebeef5;
    padding: 0 20px;
  }

  :deep(.el-tabs__content) {
    padding: 16px;
    background-color: #ffffff;
  }
}

.tab-content {
  width: 100%;
}

@media (max-width: 768px) {
  .outbound-management-view {
    padding: 12px;
  }

  .content-tabs {
    :deep(.el-tabs__header) {
      padding: 0 12px;
      overflow-x: auto;
      white-space: nowrap;
    }
  }
}
</style>
