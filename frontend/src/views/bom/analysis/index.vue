<template>
  <div class="analysis-index">
    <div class="page-header">
      <h2>BOM分析工具</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/bom' }">BOM系统</el-breadcrumb-item>
        <el-breadcrumb-item>分析工具</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" @tab-change="handleTabChange">
        <el-tab-pane 
          v-for="tab in tabConfig" 
          :key="tab.name" 
          :label="tab.label" 
          :name="tab.name" 
        />
      </el-tabs>
      <div class="tab-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 标签页配置数据
const tabConfig = [
  { name: 'where-used', label: '用量反查 (Where-Used)', path: '/home/bom/analysis/where-used' },
  { name: 'cost', label: '多层成本分析', path: '/home/bom/analysis/cost' },
  { name: 'compare', label: '结构差异对比', path: '/home/bom/analysis/compare' }
]

const activeTab = ref<string>('where-used')

// 根据当前路由设置活跃标签
watch(() => route.path, (newPath) => {
  const matchedTab = tabConfig.find(tab => newPath.includes(tab.name))
  if (matchedTab) {
    activeTab.value = matchedTab.name
  }
}, { immediate: true })

// 标签切换时导航到相应路由
const handleTabChange = (tabName: string) => {
  const matchedTab = tabConfig.find(tab => tab.name === tabName)
  if (matchedTab) {
    router.push(matchedTab.path)
  }
}
</script>

<style scoped>
.analysis-index {
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
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

:deep(.el-tabs__header) {
  margin: 0;
}

:deep(.el-tabs__content) {
    padding: 0;
  }

  .tab-content {
    padding: 20px;
  }

@media (max-width: 768px) {
  .analysis-index {
    padding: 12px;
  }
}
</style>