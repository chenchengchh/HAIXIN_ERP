<template>
  <div class="passive-acquisition-view">
    <div class="page-header">
      <h2>被动获客模块</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/scrm' }">SCRM</el-breadcrumb-item>
        <el-breadcrumb-item>被动获客</el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 被动获客模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="抖音" name="douyin">
          <DouyinPassive />
        </el-tab-pane>
        <el-tab-pane label="小红书" name="xiaohongshu">
          <XiaohongshuPassive />
        </el-tab-pane>
        <el-tab-pane label="快手" name="kuaishou">
          <KuaishouPassive />
        </el-tab-pane>
        <el-tab-pane label="微博" name="weibo">
          <WeiboPassive />
        </el-tab-pane>
        <el-tab-pane label="贴吧" name="tieba">
          <TiebaPassive />
        </el-tab-pane>
        <el-tab-pane label="知乎" name="zhihu">
          <ZhihuPassive />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import DouyinPassive from './components/DouyinPassive.vue'
import XiaohongshuPassive from './components/XiaohongshuPassive.vue'
import KuaishouPassive from './components/KuaishouPassive.vue'
import WeiboPassive from './components/WeiboPassive.vue'
import TiebaPassive from './components/TiebaPassive.vue'
import ZhihuPassive from './components/ZhihuPassive.vue'

const route = useRoute()
const router = useRouter()
const activeTab = ref(route.query.tab as string || 'douyin')

// 监听标签变化，更新路由query
watch(activeTab, (newTab) => {
  if (route.query.tab !== newTab) {
    router.replace({ query: { ...route.query, tab: newTab } })
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
</script>

<style scoped>
.passive-acquisition-view {
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
  padding: 10px;
}
</style>
