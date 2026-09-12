<template>
  <div class="version-index">
    <div class="page-header">
      <h2>BOM版本管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/bom' }">BOM系统</el-breadcrumb-item>
        <el-breadcrumb-item>版本管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="版本列表" name="list" />
        <el-tab-pane label="变更历史" name="history">
          <!-- 预留变更审计 -->
        </el-tab-pane>
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
const activeTab = ref<string>('list')

// 根据当前路由同步激活标签（处理直接访问URL/浏览器前进后退场景）
const syncTabFromRoute = () => {
  activeTab.value = route.path.includes('/version/history') ? 'history' : 'list'
}
watch(() => route.path, syncTabFromRoute, { immediate: true })

// 监听标签页变化，自动跳转路由
watch(activeTab, (newTab) => {
  if (newTab === 'history' && !route.path.includes('/version/history')) {
    router.push('/home/bom/version/history')
  } else if (newTab === 'list' && !route.path.includes('/version/list')) {
    router.push('/home/bom/version/list')
  }
})
</script>

<style scoped>
.version-index {
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
  .version-index {
    padding: 12px;
  }
}
</style>