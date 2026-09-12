<template>
  <div class="substitute-index">
    <div class="page-header">
      <h2>物料替代管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/bom' }">BOM系统</el-breadcrumb-item>
        <el-breadcrumb-item>替代管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
        <el-tab-pane label="替代物料清单" name="list" />
        <el-tab-pane label="替代规则配置" name="rules" />
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
const activeTab = ref<string>(route.path.includes('rules') ? 'rules' : 'list')

// 路由变化时同步标签状态（处理浏览器前进后退/直接访问URL场景）
const syncTabFromRoute = () => {
  activeTab.value = route.path.includes('rules') ? 'rules' : 'list'
}
watch(() => route.path, syncTabFromRoute, { immediate: true })

// 处理标签点击，跳转对应路由
const handleTabClick = (tab: any) => {
  const tabName = tab.props?.name || tab.name
  if (tabName === 'rules') {
    router.push('/home/bom/substitute/rules')
  } else {
    router.push('/home/bom/substitute/list')
  }
}
</script>

<style scoped>
.substitute-index {
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
  .substitute-index {
    padding: 12px;
  }
}
</style>
