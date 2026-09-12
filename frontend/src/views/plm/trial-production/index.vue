<template>
  <div class="trial-production-view">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>试产管理</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/plm' }">PLM系统</el-breadcrumb-item>
        <el-breadcrumb-item>试产管理</el-breadcrumb-item>
      </el-breadcrumb>
      <p>样品试制与验证过程管理，确保产品设计的可制造性和质量</p>
    </div>
    
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="试产计划" name="trial-plan">
      </el-tab-pane>
      <el-tab-pane label="试产报告" name="trial-report">
      </el-tab-pane>
      <el-tab-pane label="质量跟踪" name="quality-track">
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
const activeTab = ref('trial-plan')

// 处理标签页切换
const handleTabChange = (tab: string) => {
  // 根据标签页切换路由
  router.push(`/home/plm/trial-production/${tab}`)
}

// 更新激活的标签页
const updateActiveTab = () => {
  const pathParts = route.path.split('/')
  // 如果路径是 /home/plm/trial-production，激活第一个标签页
  if (pathParts.length === 4) {
    activeTab.value = 'trial-plan'
  } else {
    // 否则从路径中获取最后一部分作为标签页名称
    const tab = pathParts.pop() || 'trial-plan'
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
.trial-production-view {
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