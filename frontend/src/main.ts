import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

// 导入日志工具
import { logger } from './utils/logger'

// 导入Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入路由
import router from './router'

// 导入Pinia状态管理
import pinia from './stores'
import { installErrorCapture } from './utils/error-capture'

// 创建Vue应用
const app = createApp(App)

// 安装插件
app.use(ElementPlus)
app.use(pinia)
app.use(router)
installErrorCapture(app)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 初始化用户信息
import { useErpStore } from './stores/erp'
import { useAuthStore } from './stores/auth'

const erpStore = useErpStore()
const authStore = useAuthStore()

// 初始化认证状态
authStore.initAuth()

// 同步到ERP Store (保持兼容性)
if (authStore.userInfo) {
  // 转换authStore.userInfo为erpStore期望的UserInfo类型
  const erpUserInfo = {
    id: Number(authStore.userInfo.id || 0),
    username: authStore.userInfo.username,
    real_name: authStore.userInfo.name || authStore.userInfo.username,
    role: authStore.userInfo.roles?.[0] || 'user',
    department: authStore.userInfo.department || 'default'
  }
  erpStore.setUserInfo(erpUserInfo)
  erpStore.setPermissions(authStore.permissions, [])
}


// 挂载应用
app.mount('#app')
