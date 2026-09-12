<template>
  <div class="structure-index">
    <div class="page-header">
      <h2>BOM结构管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/bom' }">BOM系统</el-breadcrumb-item>
        <el-breadcrumb-item>结构管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
        <el-tab-pane label="结构编辑器" name="editor" />
        <el-tab-pane label="版本差异对比" name="compare" />
        <el-tab-pane label="结构可视化" name="visualization" />
      </el-tabs>
      <div class="tab-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 根据当前路由初始化活跃标签
const activeTab = ref<string>('editor')

const updateActiveTab = () => {
  if (route.path.includes('compare')) {
    activeTab.value = 'compare'
  } else if (route.path.includes('visualization')) {
    activeTab.value = 'visualization'
  } else {
    activeTab.value = 'editor'
  }
}

onMounted(() => {
  updateActiveTab()
})

watch(() => route.path, () => {
  updateActiveTab()
})

const handleTabClick = (tab: any) => {
  const tabName = tab.props.name || tab.name
  const pathMap: Record<string, string> = {
    editor: '/home/bom/structure/editor',
    compare: '/home/bom/structure/compare',
    visualization: '/home/bom/structure/visualization'
  }
  router.push(pathMap[tabName] ?? '/home/bom/structure/editor')
}
</script>

<style scoped>
.structure-index {
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
  .structure-index {
    padding: 12px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
  }
}
</style>