<template>
  <div class="quality-analysis-view">
    <!-- 统一头部组件 -->
    <SubModuleHeader title="质量分析管理" parentTitle="QMS 质量管理" parentPath="/home/qms" />
    
    <!-- 标签页导航 -->
    <div class="child-view-container">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="function-tabs">
        <el-tab-pane label="质量数据采集" name="data-collection">
          <!-- 返回按钮区域 -->
          <div v-if="isGrandChildRoute" class="page-header">
            <el-page-header @back="goBack">
              <template #content>
                <span class="text-large font-600 mr-3">{{ tabLabelMap[activeTab] }}</span>
              </template>
            </el-page-header>
          </div>
          <router-view />
        </el-tab-pane>
        <el-tab-pane label="质量统计分析" name="statistical-analysis">
          <!-- 返回按钮区域 -->
          <div v-if="isGrandChildRoute" class="page-header">
            <el-page-header @back="goBack">
              <template #content>
                <span class="text-large font-600 mr-3">{{ tabLabelMap[activeTab] }}</span>
              </template>
            </el-page-header>
          </div>
          <router-view />
        </el-tab-pane>
        <el-tab-pane label="质量报告生成" name="report-generation">
          <!-- 返回按钮区域 -->
          <div v-if="isGrandChildRoute" class="page-header">
            <el-page-header @back="goBack">
              <template #content>
                <span class="text-large font-600 mr-3">{{ tabLabelMap[activeTab] }}</span>
              </template>
            </el-page-header>
          </div>
          <router-view />
        </el-tab-pane>
        <el-tab-pane label="质量趋势预测" name="trend-forecast">
          <!-- 返回按钮区域 -->
          <div v-if="isGrandChildRoute" class="page-header">
            <el-page-header @back="goBack">
              <template #content>
                <span class="text-large font-600 mr-3">{{ tabLabelMap[activeTab] }}</span>
              </template>
            </el-page-header>
          </div>
          <router-view />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import SubModuleHeader from '@/components/common/SubModuleHeader.vue'

// 路由实例
const router = useRouter()
const route = useRoute()

// 标签页名称映射
const tabLabelMap: Record<string, string> = {
  'data-collection': '质量数据采集',
  'statistical-analysis': '质量统计分析',
  'report-generation': '质量报告生成',
  'trend-forecast': '质量趋势预测'
}

// 当前激活的标签页
const activeTab = ref<string>(route.params.feature as string || 'data-collection')

// 监听路由变化，更新激活的标签页
watch(() => route.params.feature, (newFeature) => {
  if (newFeature) {
    activeTab.value = newFeature as string
  }
})

// 是否为三级路由
const isGrandChildRoute = computed(() => {
  return route.matched.length > 3
})

// 返回功能
const goBack = () => {
  router.back()
}

// 标签页切换处理
const handleTabChange = (tab: string) => {
  router.push(`/home/qms/quality-analysis/${tab}`)
}
</script>

<style scoped>
.quality-analysis-view {
  height: 100%;
  overflow: auto;
}

/* 标签页样式 */
.function-tabs {
  background-color: var(--bg-color);
  border-radius: var(--border-radius-md);
  padding: 16px;
  box-shadow: var(--shadow-sm);
}
</style>