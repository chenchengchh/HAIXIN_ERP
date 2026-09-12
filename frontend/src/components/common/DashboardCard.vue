<template>
  <el-card class="dashboard-card" shadow="hover" @click="$emit('click')">
    <template #header>
      <div class="card-header">
        <div class="header-left">
          <el-icon v-if="icon" class="header-icon">
            <component :is="icon" />
          </el-icon>
          <span class="header-title">{{ title }}</span>
        </div>
        <el-icon class="header-arrow"><ArrowRight /></el-icon>
      </div>
    </template>
    
    <div class="card-content">
      <div v-if="description" class="description">{{ description }}</div>
      <slot></slot>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ArrowRight } from '@element-plus/icons-vue'
import type { Component } from 'vue'

defineProps<{
  title: string
  icon?: Component
  description?: string
}>()

defineEmits<{
  click: []
}>()
</script>

<style scoped>
.dashboard-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
}

.dashboard-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
  font-size: 1.1rem;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 1.2em;
}

.header-arrow {
  color: #999;
}

.card-content {
  padding: 10px 0;
}

.description {
  color: #666;
  margin-bottom: 12px;
  font-size: 0.9rem;
  line-height: 1.5;
}
</style>
