<template>
  <div class="material-index">
    <div class="page-header">
      <h2>物料档案管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/bom' }">BOM系统</el-breadcrumb-item>
        <el-breadcrumb-item>物料档案</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
        <el-tab-pane label="物料清单" name="list" />
        <el-tab-pane label="分类维护" name="category" />
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

// 根据当前路由初始化活跃标签
const activeTab = ref<string>(route.path.includes('category') ? 'category' : 'list')

// 更新活跃标签
const updateActiveTab = () => {
  if (route.path.includes('category')) {
    activeTab.value = 'category'
  } else {
    activeTab.value = 'list'
  }
}

// 处理标签点击
const handleTabClick = (tab: any) => {
  const tabName = tab.props.name || tab.name
  if (tabName === 'category') {
    router.push('/home/bom/material/category')
  } else {
    router.push('/home/bom/material/list')
  }
}

// 监听路由变化
watch(() => route.path, () => {
  updateActiveTab()
})
</script>

<style scoped>
.material-index {
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
  .material-index {
    padding: 12px;
  }
}
</style>
