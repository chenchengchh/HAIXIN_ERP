<template>
  <div class="compensation-view">
    <div class="page-header">
      <h2>薪酬福利管理</h2>
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/home/hr' }">HR系统</el-breadcrumb-item>
        <el-breadcrumb-item>薪酬福利管理</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 薪酬福利管理模块导航 -->
    <div class="module-nav">
      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="薪酬核算" name="payroll">
          <PayrollView />
        </el-tab-pane>
        <el-tab-pane label="社保公积金" name="social-security">
          <SocialSecurityView />
        </el-tab-pane>
        <el-tab-pane label="绩效奖金" name="performance-bonus">
          <PerformanceBonusView />
        </el-tab-pane>
        <el-tab-pane label="福利管理" name="benefits">
          <BenefitsView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
// 导入子组件
import PayrollView from '../PayrollView.vue'
import SocialSecurityView from '../SocialSecurityView.vue'
import PerformanceBonusView from '../PerformanceBonusView.vue'
import BenefitsView from '../BenefitsView.vue'

// 活跃标签
const activeTab = ref<string>('payroll')
</script>

<style scoped>
.compensation-view {
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
  .compensation-view {
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