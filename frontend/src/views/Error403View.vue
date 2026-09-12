<template>
  <div class="error-403-view">
    <el-card class="error-card">
      <template #header>
        <div class="card-header">
          <el-icon><WarningFilled /></el-icon>
          <span>访问受限</span>
        </div>
      </template>
      <div class="error-content">
        <el-result
          icon="warning"
          title="403 权限不足"
          sub-title="您没有权限访问该页面，请联系管理员获取权限"
        >
          <template #extra>
            <el-button type="primary" @click="handleGoHome">返回首页</el-button>
            <el-button @click="handleContactAdmin">联系管理员</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { WarningFilled } from '@element-plus/icons-vue'

// 路由实例
const router = useRouter()

// 返回首页
const handleGoHome = () => {
  router.push('/')
}

// 联系管理员
const handleContactAdmin = () => {
  // 这里可以实现联系管理员的逻辑，比如打开邮件客户端或显示管理员联系方式
  alert('请联系系统管理员获取访问权限')
}
</script>

<style scoped lang="scss">
.error-403-view {
  width: 100%;
  height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.error-card {
  width: 100%;
  max-width: 600px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  
  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 1.2rem;
    font-weight: bold;
    color: #e6a23c;
  }
}

.error-content {
  padding: 20px 0;
  text-align: center;
}

// 响应式设计
@media (max-width: 768px) {
  .error-403-view {
    padding: 10px;
  }
  
  .error-card {
    .card-header {
      font-size: 1rem;
    }
  }
  
  .error-content {
    padding: 15px 0;
  }
}
</style>