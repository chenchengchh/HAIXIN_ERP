<template>
  <div class="platform-module">
    <div class="module-header">
      <h3><el-icon><VideoPlay /></el-icon> 快手 - 短视频获客</h3>
      <div class="actions">
        <el-button type="primary">开始采集</el-button>
        <el-button>配置规则</el-button>
      </div>
    </div>
    <div class="content-area">
      <el-empty description="快手主动获客功能开发中..."></el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { VideoPlay } from '@element-plus/icons-vue'
</script>

<style scoped>
.platform-module { padding: 20px; background: #fff; border-radius: 8px; }
.module-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 15px; }
.module-header h3 { display: flex; align-items: center; gap: 8px; margin: 0; }
</style>
