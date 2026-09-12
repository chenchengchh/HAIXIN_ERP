<template>
  <div class="control-tower-module">
    <div class="page-header">
      <div class="header-left">
        <h2>供应链塔台</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/home/scm' }">SCM系统</el-breadcrumb-item>
          <el-breadcrumb-item>供应链塔台</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="refreshActive">刷新</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="tower-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="地图监控" name="map">
        <div class="tab-content">
          <MapView embedded />
        </div>
      </el-tab-pane>
      <el-tab-pane label="KPI看板" name="kpi">
        <div class="tab-content">
          <KpiMonitoring />
        </div>
      </el-tab-pane>
      <el-tab-pane label="异常预警" name="alert">
        <div class="tab-content">
          <ExceptionAlert />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MapView from './map-view.vue'
import KpiMonitoring from '../supply-chain-visualization/kpi-monitoring.vue'
import ExceptionAlert from '../supply-chain-visualization/exception-alert.vue'

type TabName = 'map' | 'kpi' | 'alert'

const route = useRoute()
const router = useRouter()

const activeTab = ref<TabName>((route.query.tab as TabName) || 'map')

watch(
  () => route.query.tab,
  (val) => {
    if (val === 'map' || val === 'kpi' || val === 'alert') activeTab.value = val
  }
)

function handleTabChange() {
  router.replace({
    path: route.path,
    query: {
      ...route.query,
      tab: activeTab.value
    }
  })
}

function refreshActive() {
  router.replace({ path: route.path, query: { ...route.query } })
}
</script>

<style scoped>
.control-tower-module {
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  color: #1f2329;
}

.tower-tabs {
  background: #fff;
}

.tab-content {
  padding: 16px;
}
</style>
