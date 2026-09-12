<template>
  <div class="document-view">
    <div class="page-header">
      <h2>文档管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/oa' }">OA系统</el-breadcrumb-item>
        <el-breadcrumb-item>文档管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 文档管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="文档列表" name="list">
          <DocumentListView />
        </el-tab-pane>
        <el-tab-pane label="文档上传" name="upload">
          <DocumentUploadView />
        </el-tab-pane>
        <el-tab-pane label="分类管理" name="categories">
          <CategoryManagementView />
        </el-tab-pane>
        <el-tab-pane label="版本管理" name="versions">
          <VersionManagementView />
        </el-tab-pane>
        <el-tab-pane label="权限管理" name="permissions">
          <PermissionManagementView />
        </el-tab-pane>
        <el-tab-pane label="文档搜索" name="search">
          <DocumentSearchView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import DocumentListView from './DocumentListView.vue'
import DocumentUploadView from './DocumentUploadView.vue'
import CategoryManagementView from './CategoryManagementView.vue'
import VersionManagementView from './VersionManagementView.vue'
import PermissionManagementView from './PermissionManagementView.vue'
import DocumentSearchView from './DocumentSearchView.vue'

// 活跃标签
const activeTab = ref<string>('list')
</script>

<style scoped>
.document-view {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .document-view {
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