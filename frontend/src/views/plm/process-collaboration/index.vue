<template>
  <div class="process-collaboration-view">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>工艺协同</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/plm' }">PLM系统</el-breadcrumb-item>
        <el-breadcrumb-item>工艺协同</el-breadcrumb-item>
      </el-breadcrumb>
      <p>设计与工艺的并行协同工作，实现产品研发与制造工艺的无缝对接</p>
    </div>
    
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="工艺路线设计" name="process-route">
      </el-tab-pane>
      <el-tab-pane label="工艺文件管理" name="process-file">
      </el-tab-pane>
      <el-tab-pane label="工艺变更管理" name="process-change">
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
const activeTab = ref('process-route')

// 处理标签页切换
const handleTabChange = (tab: string) => {
  // 根据标签页切换路由
  router.push(`/home/plm/process-collaboration/${tab}`)
}

// 更新激活的标签页
const updateActiveTab = () => {
  const pathParts = route.path.split('/')
  // 如果路径是 /home/plm/process-collaboration，激活第一个标签页
  if (pathParts.length === 4) {
    activeTab.value = 'process-route'
  } else {
    // 否则从路径中获取最后一部分作为标签页名称
    const tab = pathParts.pop() || 'process-route'
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
.process-collaboration-view {
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