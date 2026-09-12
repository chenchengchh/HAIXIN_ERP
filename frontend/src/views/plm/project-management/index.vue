<template>
  <div class="project-management-view">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>研发项目管理</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/plm' }">PLM系统</el-breadcrumb-item>
        <el-breadcrumb-item>研发项目管理</el-breadcrumb-item>
      </el-breadcrumb>
      <p>项目进度、资源与风险管控，实现研发项目全生命周期管理</p>
    </div>
    
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="项目概览" name="dashboard">
      </el-tab-pane>
      <el-tab-pane label="甘特图" name="gantt-view">
      </el-tab-pane>
      <el-tab-pane label="资源负载" name="resource">
      </el-tab-pane>
    </el-tabs>
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const activeTab = ref('dashboard')

// 处理标签页切换
const handleTabChange = (tab: string) => {
  // 根据标签页切换路由
  router.push(`/home/plm/project-management/${tab}`)
}

// 初始化时设置当前标签页
const updateActiveTab = () => {
  const pathParts = route.path.split('/')
  // 如果路径是 /home/plm/project-management，激活第一个标签页
  if (pathParts.length === 4) {
    activeTab.value = 'dashboard'
  } else {
    // 否则从路径中获取最后一部分作为标签页名称
    const tab = pathParts.pop() || 'dashboard'
    activeTab.value = tab
  }
}

// 组件挂载时设置
onMounted(() => {
  updateActiveTab()
})

// 监听路由变化，更新激活的标签页
watch(
  () => route.path,
  () => {
    updateActiveTab()
  }
)
</script>

<style scoped lang="scss">
.project-management-view {
  padding: 20px;
  height: 100%;
  overflow: auto;

  /* 响应式设计 */
  @media (max-width: 1024px) {
    padding: 16px;
  }

  @media (max-width: 768px) {
    padding: 12px;
  }
}
</style>