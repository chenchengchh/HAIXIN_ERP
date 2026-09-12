<template>
  <div class="product-data-view">
    <!-- 页面标题和面包屑导航 -->
    <div class="page-header">
      <h2>产品数据管理</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/plm' }">PLM系统</el-breadcrumb-item>
        <el-breadcrumb-item>产品数据管理</el-breadcrumb-item>
      </el-breadcrumb>
      <p>BOM、图纸与技术文档管理，确保产品数据的完整性和一致性</p>
    </div>
    
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="物料库" name="item-list">
      </el-tab-pane>
      <el-tab-pane label="BOM编辑器" name="bom-editor">
      </el-tab-pane>
      <el-tab-pane label="文档浏览器" name="doc-viewer">
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
const activeTab = ref('item-list')

// 处理标签页切换
const handleTabChange = (tab: string) => {
  // 根据标签页切换路由
  router.push(`/home/plm/product-data/${tab}`)
}

// 更新激活的标签页
const updateActiveTab = () => {
  const pathParts = route.path.split('/')
  // 如果路径是 /home/plm/product-data，激活第一个标签页
  if (pathParts.length === 4) {
    activeTab.value = 'item-list'
  } else {
    // 否则从路径中获取最后一部分作为标签页名称
    const tab = pathParts.pop() || 'item-list'
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
.product-data-view {
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