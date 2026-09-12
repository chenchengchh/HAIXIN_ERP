<template>
  <div class="sub-module-header">
    <div class="page-header">
      <h2>{{ title }}</h2>
      <el-breadcrumb aria-label="Breadcrumb" role="navigation">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: parentPath }">{{ parentTitle }}</el-breadcrumb-item>
        <el-breadcrumb-item aria-current="page">{{ title }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// Props
const props = defineProps<{
  title: string
  parentTitle?: string
  parentPath?: string
}>()

// 默认值处理
const parentTitle = computed(() => {
  return props.parentTitle || route.matched[1]?.meta?.title || ''
})

const parentPath = computed(() => {
  return props.parentPath || `/${route.matched[1]?.path}` || ''
})
</script>

<style scoped lang="scss">
.sub-module-header {
  margin-bottom: 24px;
}

.page-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color);

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .el-breadcrumb {
    font-size: 14px;

    .el-breadcrumb__inner {
      color: var(--el-text-color-secondary);

      &.is-link {
        color: var(--el-color-primary);

        &:hover {
          color: var(--el-color-primary-light-3);
        }
      }
    }

    .el-breadcrumb__separator {
      color: var(--el-text-color-placeholder);
      margin: 0 8px;
    }
  }
}
</style>