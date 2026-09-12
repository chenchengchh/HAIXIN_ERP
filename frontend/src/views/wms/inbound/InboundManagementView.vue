<template>
  <div class="inbound-management-view">
    <div class="page-header">
      <h2>入库管理</h2>
      <el-breadcrumb>
        <el-breadcrumb-item>
          <router-link to="/home">首页</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms">WMS系统</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          <router-link to="/home/wms/inbound">入库管理</router-link>
        </el-breadcrumb-item>
        <el-breadcrumb-item>
          {{ tabLabelMap[activeTab] }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <el-tabs v-model="activeTab" class="function-tabs" type="card">
      <el-tab-pane label="到货通知" name="asn-list">
        <div class="tab-content">
          <AsnList ref="asnListRef" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="收货作业" name="receive-job">
        <div class="tab-content">
          <ReceiveJob />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
// 导入子组件
import AsnList from './asn-list.vue';
import ReceiveJob from './receive-job.vue';

const tabLabelMap: Record<string, string> = {
  'asn-list': '到货通知',
  'receive-job': '收货作业'
};

// 当前激活的标签页
const activeTab = ref('asn-list');

const asnListRef = ref<any>(null);
const route = useRoute();

const applyRouteIntent = () => {
  const tab = String(route.query.tab || '');
  const action = String(route.query.action || '');
  if (tab) {
    activeTab.value = tab;
  }
  if (action === 'create') {
    activeTab.value = 'asn-list';
    setTimeout(() => {
      asnListRef.value?.openCreate?.();
    }, 0);
  }
};

onMounted(() => {
  applyRouteIntent();
});

watch(() => route.query, () => {
  applyRouteIntent();
}, { deep: true });
</script>

<style scoped>
.inbound-management-view {
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

.function-tabs {
  width: 100%;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

:deep(.function-tabs .el-tabs__header) {
  margin-bottom: 0;
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
  padding: 0 20px;
}

:deep(.function-tabs .el-tabs__content) {
  padding: 16px;
  background-color: #ffffff;
}

.tab-content {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .inbound-management-view {
    padding: 12px;
  }

  :deep(.function-tabs .el-tabs__header) {
    padding: 0 12px;
    overflow-x: auto;
    white-space: nowrap;
  }
}
</style>
