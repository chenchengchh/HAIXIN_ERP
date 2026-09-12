<script setup lang="ts">
// App.vue 主组件
</script>

<template>
  <div id="app">
    <!-- 路由视图 -->
    <router-view></router-view>
  </div>
</template>

<style scoped>
/* 确保App组件填充满父容器 */
#app {
  width: 100%;
  height: 100%;
}
</style>
