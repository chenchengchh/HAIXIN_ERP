<template>
  <div class="login-container">
    <!-- 背景装饰：漂浮气泡 -->
    <div class="bg-bubble bubble-1"></div>
    <div class="bg-bubble bubble-2"></div>
    <div class="bg-bubble bubble-3"></div>
    <div class="bg-wave"></div>

    <div class="login-content">
      <!-- 左侧品牌展示区 -->
      <div class="login-left">
        <div class="login-info">
          <div class="logo">
            <img src="/starfish.svg" alt="海星数字化系统" class="logo-img" />
            <h1>海星数字化系统</h1>
          </div>
          <p class="subtitle">一站式企业数字化转型平台<br />让数据驱动每一次决策</p>
          <ul class="features">
            <li><el-icon><Check /></el-icon> 智能数据分析与经营决策</li>
            <li><el-icon><Check /></el-icon> 全链路供应链协同管理</li>
            <li><el-icon><Check /></el-icon> 生产、库存、销售实时联动</li>
            <li><el-icon><Check /></el-icon> AI 助手赋能企业运营</li>
          </ul>
        </div>
        <!-- 海星漂浮装饰 -->
        <img src="/starfish.svg" alt="" class="deco-starfish" />
        <div class="decoration-circle"></div>
        <div class="decoration-circle circle-2"></div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="login-right">
        <div class="form-wrapper">
          <h2>欢迎回来</h2>
          <p class="form-subtitle">请登录您的账号，继续今日的工作</p>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            size="large"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
              <el-link type="primary" underline="never">忘记密码？</el-link>
            </div>

            <el-button
              type="primary"
              :loading="loading"
              class="submit-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中…' : '登 录' }}
            </el-button>
          </el-form>

          <div class="footer">
            <span>还没有账号？</span>
            <el-link type="primary" underline="never">联系管理员开通</el-link>
          </div>
        </div>
      </div>
    </div>

    <p class="copyright">© 2026 海星数字化系统 · 让企业数字化转型更简单</p>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

/**
 * 处理登录逻辑
 * 校验表单后调用认证仓库执行登录，成功后跳转至来源页或首页
 */
const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(loginForm)
        ElMessage.success('登录成功，欢迎回来！')
        const redirect = typeof route.query.redirect === 'string' && route.query.redirect.startsWith('/')
          ? route.query.redirect
          : '/'
        await router.replace(redirect)
      } catch (error: any) {
        ElMessage.error(error.message || '登录失败，请检查用户名或密码')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #e0f2fe 0%, #f0f9ff 45%, #ede9fe 100%);
}

/* 漂浮气泡装饰 */
.bg-bubble {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(56, 189, 248, 0.25), rgba(37, 99, 235, 0.08));
  animation: float 8s ease-in-out infinite;
  pointer-events: none;
}

.bubble-1 {
  width: 260px;
  height: 260px;
  top: -60px;
  right: -40px;
}

.bubble-2 {
  width: 160px;
  height: 160px;
  bottom: 12%;
  left: 6%;
  animation-delay: -3s;
}

.bubble-3 {
  width: 90px;
  height: 90px;
  top: 18%;
  left: 14%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-24px) scale(1.04); }
}

/* 底部波浪装饰 */
.bg-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 120px;
  background:
    radial-gradient(ellipse 55% 80px at 10% 110%, rgba(56, 189, 248, 0.28), transparent),
    radial-gradient(ellipse 55% 90px at 50% 120%, rgba(37, 99, 235, 0.22), transparent),
    radial-gradient(ellipse 55% 80px at 90% 110%, rgba(124, 58, 237, 0.18), transparent);
  pointer-events: none;
}

.login-content {
  display: flex;
  width: 1000px;
  max-width: calc(100vw - 40px);
  height: 620px;
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(18px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 28px;
  box-shadow:
    0 24px 48px -12px rgba(37, 99, 235, 0.22),
    0 12px 20px -8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.login-left {
  flex: 1.05;
  background: linear-gradient(150deg, #0ea5e9 0%, #2563eb 55%, #6d28d9 130%);
  padding: 56px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  color: white;
  overflow: hidden;
}

.decoration-circle {
  position: absolute;
  top: -110px;
  right: -110px;
  width: 320px;
  height: 320px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.decoration-circle.circle-2 {
  top: auto;
  right: auto;
  bottom: -80px;
  left: -60px;
  width: 220px;
  height: 220px;
  background: rgba(255, 255, 255, 0.06);
}

.deco-starfish {
  position: absolute;
  right: 28px;
  bottom: 32px;
  width: 96px;
  height: 96px;
  opacity: 0.5;
  filter: brightness(1.35) saturate(0.8);
  animation: spin-float 12s ease-in-out infinite;
}

@keyframes spin-float {
  0%, 100% { transform: rotate(-8deg) translateY(0); }
  50% { transform: rotate(10deg) translateY(-10px); }
}

.login-info {
  position: relative;
  z-index: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.logo-img {
  width: 48px;
  height: 48px;
  filter: drop-shadow(0 4px 10px rgba(0, 0, 0, 0.25));
}

.logo h1 {
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 17px;
  opacity: 0.92;
  margin-bottom: 40px;
  line-height: 1.8;
}

.features {
  list-style: none;
  padding: 0;
  margin: 0;
}

.features li {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
  font-size: 15px;
  opacity: 0.94;
}

.features .el-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.16);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  flex-shrink: 0;
}

.login-right {
  flex: 1;
  padding: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-wrapper {
  width: 100%;
  max-width: 360px;
}

.form-wrapper h2 {
  font-size: 26px;
  color: #111827;
  margin-bottom: 10px;
  font-weight: 700;
}

.form-subtitle {
  color: #6b7280;
  margin-bottom: 36px;
  font-size: 14px;
}

.login-form {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 14px;
}

.login-form :deep(.el-input__inner) {
  height: 40px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 6px;
  padding: 12px;
  height: auto;
  border: none;
  border-radius: 10px;
  background: linear-gradient(90deg, #0ea5e9 0%, #2563eb 100%);
  box-shadow: 0 8px 20px -6px rgba(37, 99, 235, 0.5);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 26px -6px rgba(37, 99, 235, 0.6);
}

.footer {
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.copyright {
  margin-top: 24px;
  font-size: 13px;
  color: #94a3b8;
  position: relative;
  z-index: 1;
}

@media (max-width: 768px) {
  .login-content {
    flex-direction: column;
    width: 92%;
    height: auto;
  }

  .login-left {
    display: none;
  }

  .login-right {
    padding: 44px 24px;
  }
}
</style>
