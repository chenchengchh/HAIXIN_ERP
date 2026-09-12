<template>
  <div class="customer-view">
    <div class="page-header">
      <h2>客户管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/crm' }">CRM系统</el-breadcrumb-item>
        <el-breadcrumb-item>客户管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 客户管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="客户列表" name="list">
          <CustomerListView />
        </el-tab-pane>
        <el-tab-pane label="客户360°视图" name="360view">
          <Customer360View />
        </el-tab-pane>
        <el-tab-pane label="客户标签" name="tags">
          <CustomerTagsView />
        </el-tab-pane>
        <el-tab-pane label="客户分类" name="categories">
          <CustomerCategoriesView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
// 导入子组件
import CustomerListView from './CustomerListView.vue'
import Customer360View from './Customer360View.vue'
import CustomerTagsView from './CustomerTagsView.vue'
import CustomerCategoriesView from './CustomerCategoriesView.vue'

const route = useRoute()
// 活跃标签，优先从query获取，默认为list
const activeTab = ref<string>(route.query.tab as string || 'list')

// 监听标签变化，更新路由query
watch(activeTab, (newTab) => {
  if (route.query.tab !== newTab) {
    // 使用replace避免历史记录堆积
    window.history.replaceState(null, '', `${window.location.pathname}?tab=${newTab}${route.query.id ? `&id=${route.query.id}` : ''}`)
  }
})

// 监听路由变化，更新活跃标签
watch(
  () => route.query.tab,
  (newTab) => {
    if (newTab && newTab !== activeTab.value) {
      activeTab.value = newTab as string
    }
  }
)

onMounted(() => {
  // 初始化时从路由获取tab参数
  if (route.query.tab) {
    activeTab.value = route.query.tab as string
  }
})
</script>

<style scoped>
.customer-view {
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

/* 模块导航 */
.module-nav {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  min-height: calc(100% - 80px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .customer-view {
    padding: 12px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
  }
}
</style>
