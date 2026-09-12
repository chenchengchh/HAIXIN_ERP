<template>
  <div class="base-setting-view">
    <div class="page-header">
      <h2>基础设置</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms">WMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms/base">基础设置</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="仓库平面图" name="warehouse-map">
          <WarehouseMap />
        </el-tab-pane>
        <el-tab-pane label="库位码打印" name="location-print">
          <LocationPrint />
        </el-tab-pane>
        <el-tab-pane label="仓库设置" name="warehouse-setting">
          <WarehouseSetting />
        </el-tab-pane>
        <el-tab-pane label="库区设置" name="zone-setting">
          <ZoneSetting />
        </el-tab-pane>
        <el-tab-pane label="库位类型设置" name="location-type-setting">
          <LocationTypeSetting />
        </el-tab-pane>
        <el-tab-pane label="物料类型设置" name="material-type-setting">
          <MaterialTypeSetting />
        </el-tab-pane>
        <el-tab-pane label="条码规则设置" name="barcode-rule-setting">
          <BarcodeRuleSetting />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
// 导入子组件
import WarehouseMap from './warehouse-map.vue';
import LocationPrint from './location-print.vue';
import WarehouseSetting from './warehouse-setting.vue';
import ZoneSetting from './zone-setting.vue';
import LocationTypeSetting from './location-type-setting.vue';
import MaterialTypeSetting from './material-type-setting.vue';
import BarcodeRuleSetting from './barcode-rule-setting.vue';

const tabLabelMap: Record<string, string> = {
  'warehouse-map': '仓库平面图',
  'location-print': '库位码打印',
  'warehouse-setting': '仓库设置',
  'zone-setting': '库区设置',
  'location-type-setting': '库位类型设置',
  'material-type-setting': '物料类型设置',
  'barcode-rule-setting': '条码规则设置'
};

// 当前激活的标签页
const activeTab = ref('warehouse-map');
</script>

<style scoped>
.base-setting-view {
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

/* 优化标签页样式 */
:deep(.el-tabs) {
  width: 100%;
}

:deep(.el-tabs__header) {
  margin-bottom: 0;
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-tabs__nav-wrap) {
  margin: 0;
}

:deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  padding: 16px 20px;
  margin-right: 24px;
  color: #606266;
  border-bottom: 2px solid transparent;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item:hover) {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.04);
}

:deep(.el-tabs__item.is-active) {
  color: #409eff;
  border-bottom-color: #409eff;
  background-color: #ffffff;
}

:deep(.el-tabs__content) {
  padding: 0;
  min-height: 500px;
}

/* 占位符内容样式 */
.placeholder-content {
  padding: 60px 40px;
  text-align: center;
  background-color: #fafafa;
  border-radius: 8px;
  margin: 20px 0;
  border: 1px dashed #e4e7ed;
  transition: all 0.3s ease;
}

.placeholder-content:hover {
  background-color: #f0f9ff;
  border-color: #c6e2ff;
}

.placeholder-content h3 {
  margin-bottom: 12px;
  color: #303133;
  font-size: 1.25rem;
  font-weight: 600;
}

.placeholder-content p {
  color: #606266;
  font-size: 1rem;
  margin: 0;
  line-height: 1.6;
}

/* 响应式设计优化 */
@media (max-width: 1200px) {
  .base-setting-view {
    padding: 0;
  }
}

@media (max-width: 768px) {
  .base-setting-view {
    padding: 0;
  }
  
  .module-nav {
    width: 100%;
  }
  
  :deep(.el-tabs__header) {
    overflow-x: auto;
    white-space: nowrap;
  }
  
  :deep(.el-tabs__item) {
    padding: 12px 16px;
    margin-right: 16px;
    font-size: 13px;
  }
  
  .placeholder-content {
    padding: 40px 20px;
    margin: 12px 0;
  }
  
  .placeholder-content h3 {
    font-size: 1.1rem;
  }
  
  .placeholder-content p {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .base-setting-view {
    padding: 0;
  }
  
  :deep(.el-tabs__item) {
    padding: 10px 12px;
    margin-right: 12px;
    font-size: 12px;
  }
  
}
</style>
