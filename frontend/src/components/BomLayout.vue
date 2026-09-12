<template>
  <div class="bom-layout">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>{{ title }}</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/bom' }">BOM系统</el-breadcrumb-item>
        <el-breadcrumb-item v-for="(item, index) in breadcrumbItems" :key="index" :to="item.to">{{ item.label }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 模块内容 -->
    <div class="module-nav">
      <!-- 操作按钮区域 -->
      <div v-if="showActionBar" class="action-bar">
        <slot name="action-bar"></slot>
      </div>
      
      <!-- 主要内容区域 -->
      <slot></slot>
    </div>
  </div>
</template>

<script setup lang="ts">
// 组件属性与默认值
const props = withDefaults(defineProps<{
  title: string
  breadcrumbItems?: Array<{ label: string; to?: any }>
  showActionBar?: boolean
}>(), {
  breadcrumbItems: () => [],
  showActionBar: true
})
</script>

<style scoped>
/* 主容器样式 */
.bom-layout {
  padding: 24px;
  height: 100%;
  box-sizing: border-box;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2ff 100%);
}

/* 页面头部样式 */
.page-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.page-header h2 {
  margin: 0 0 10px 0;
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(to right, var(--primary-color), var(--primary-active));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

/* 模块内容样式 */
.module-nav {
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--glass-border);
  border-radius: var(--border-radius-lg);
  box-shadow: 0 8px 32px 0 var(--glass-shadow);
  min-height: calc(100% - 100px);
  padding: 24px;
}

/* 操作栏样式 */
.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bom-layout {
    padding: 12px;
  }
  
  .page-header h2 {
    font-size: 1.2rem;
  }
  
  .module-nav {
    min-height: auto;
    padding: 12px;
  }
  
  .action-bar {
    justify-content: center;
    margin-bottom: 12px;
  }
}
</style>