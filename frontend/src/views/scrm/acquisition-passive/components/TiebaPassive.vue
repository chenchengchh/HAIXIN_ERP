<template>
  <div class="platform-module">
    <div class="module-header">
      <h3><el-icon><ChatDotSquare /></el-icon> 贴吧账户 - 帖子回复管理</h3>
      <div class="actions">
        <el-button type="primary">同步消息</el-button>
        <el-button>意向管理</el-button>
      </div>
    </div>
    <div class="content-area">
      <el-empty description="贴吧被动获客功能开发中..."></el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatDotSquare } from '@element-plus/icons-vue'
</script>

<style scoped>
.platform-module { padding: 20px; background: #fff; border-radius: 8px; }
.module-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 15px; }
.module-header h3 { display: flex; align-items: center; gap: 8px; margin: 0; }
</style>
