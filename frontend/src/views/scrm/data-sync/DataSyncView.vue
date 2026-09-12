<template>
  <div class="under-construction">
    <el-result
      icon="info"
      title="数据同步模块开发中"
      sub-title="该功能正在开发中，敬请期待。"
    >
      <template #extra>
        <el-button type="primary" @click="goBack">返回上一页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
const goBack = () => {
  router.back()
}
</script>

<style scoped>
.under-construction {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  min-height: 400px;
  background-color: #fff;
  border-radius: 8px;
}
</style>
