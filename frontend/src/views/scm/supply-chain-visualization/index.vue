<template>
  <div class="supply-chain-visualization-container">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>供应链可视化</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scm' }">SCM系统</el-breadcrumb-item>
        <el-breadcrumb-item>供应链可视化</el-breadcrumb-item>
      </el-breadcrumb>
      <p>可视化展示供应链网络，监控关键指标，预警异常情况</p>
    </div>

    <!-- 标签页导航 -->
    <el-tabs v-model="activeTab" type="border-card" class="module-tabs">
      <!-- 供应链地图 -->
      <el-tab-pane label="供应链地图" name="map-view">
        <map-view-component />
      </el-tab-pane>

      <!-- 关键指标监控 -->
      <el-tab-pane label="关键指标监控" name="kpi-monitoring">
        <kpi-monitoring-component />
      </el-tab-pane>

      <!-- 异常预警 -->
      <el-tab-pane label="异常预警" name="exception-alert">
        <exception-alert-component />
      </el-tab-pane>

      <!-- 报表生成 -->
      <el-tab-pane label="报表生成" name="report-generation">
        <report-generation-component />
      </el-tab-pane>

      <el-tab-pane label="采购对账差异" name="purchase-reconciliation">
        <purchase-reconciliation-component />
      </el-tab-pane>

      <el-tab-pane label="质检HOLD监控" name="qc-hold-monitor">
        <qc-hold-monitor-component />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import MapViewComponent from '../control-tower/map-view.vue';
import KpiMonitoringComponent from './kpi-monitoring.vue';
import ExceptionAlertComponent from './exception-alert.vue';
import ReportGenerationComponent from './report-generation.vue';
import PurchaseReconciliationComponent from './purchase-reconciliation.vue';
import QcHoldMonitorComponent from './qc-hold-monitor.vue';

// 当前激活的标签页
const activeTab = ref('map-view');
</script>

<style scoped>
.supply-chain-visualization-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.page-header p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.module-tabs {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .supply-chain-visualization-container {
    padding: 10px;
  }
  
  .page-header {
    margin-bottom: 10px;
  }
  
  .page-header h2 {
    font-size: 18px;
  }
}
</style>
