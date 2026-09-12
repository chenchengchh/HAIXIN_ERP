<template>
  <div class="planning-module">
    <div class="page-header">
      <div class="header-left">
        <h2>供应计划</h2>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/home/scm' }">SCM系统</el-breadcrumb-item>
          <el-breadcrumb-item>供应计划</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <el-button @click="refreshActive">刷新</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="planning-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="MRP运算" name="mrp">
        <div class="tab-content">
          <MrpRun embedded @view-result="openResultTab" />
        </div>
      </el-tab-pane>
      <el-tab-pane label="计划结果" name="result">
        <div class="tab-content">
          <PlanResult embedded :initial-plan-id="planId" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MrpRun from './mrp-run.vue'
import PlanResult from './plan-result.vue'

type TabName = 'mrp' | 'result'

const route = useRoute()
const router = useRouter()

const activeTab = ref<TabName>((route.query.tab as TabName) || 'mrp')

const planId = computed(() => {
  const raw = route.query.planId
  const n = typeof raw === 'string' ? Number(raw) : Array.isArray(raw) ? Number(raw[0]) : Number(raw)
  return Number.isFinite(n) && n > 0 ? n : undefined
})

watch(
  () => route.query.tab,
  (val) => {
    if (val === 'mrp' || val === 'result') activeTab.value = val
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

function openResultTab(id?: number) {
  router.replace({
    path: route.path,
    query: {
      ...route.query,
      tab: 'result',
      planId: id ?? route.query.planId
    }
  })
}

function refreshActive() {
  router.replace({ path: route.path, query: { ...route.query } })
}
</script>

<style scoped>
.planning-module {
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

.planning-tabs {
  background: #fff;
}

.tab-content {
  padding: 16px;
}
</style>
