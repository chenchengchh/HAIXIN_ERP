<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="layout-header">
      <div class="header-left">
        <el-button
          link
          class="menu-toggle"
          @click="toggleMenu"
        >
          <el-icon :size="20">
            <component :is="isMenuCollapsed ? 'Expand' : 'Fold'" />
          </el-icon>
        </el-button>
        <div class="logo-container">
          <img src="/starfish.svg" alt="海星数字化系统" class="logo-img" />
          <span class="logo-text">海星数字化系统</span>
        </div>
        <el-breadcrumb separator="/" class="hidden-xs-only ml-4">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="index">
            {{ item }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="header-right">
        <div class="action-items">
          <el-tooltip content="消息通知" placement="bottom">
            <el-badge :value="3" class="action-item">
              <el-icon :size="20"><Bell /></el-icon>
            </el-badge>
          </el-tooltip>
          <el-tooltip content="全屏" placement="bottom">
            <div class="action-item" @click="toggleFullScreen">
              <el-icon :size="20"><FullScreen /></el-icon>
            </div>
          </el-tooltip>
        </div>
        
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" class="user-avatar" :icon="UserFilled" src="" />
            <span class="user-name">{{ authStore.userInfo?.username || '管理员' }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu class="user-dropdown">
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>个人中心
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>系统设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <!-- 主体内容区 -->
    <div class="layout-body">
      <!-- 左侧菜单栏 -->
      <aside 
        class="layout-sidebar" 
        :class="{ 'sidebar-collapsed': isMenuCollapsed }"
      >
        <el-scrollbar>
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            :collapse="isMenuCollapsed"
            :collapse-transition="false"
            unique-opened
            background-color="transparent"
            text-color="var(--text-regular)"
            active-text-color="var(--primary-color)"
            @select="handleMenuSelect"
          >
            <el-sub-menu index="1">
              <template #title>
                <el-icon><OfficeBuilding /></el-icon>
                <span>企业资源类</span>
              </template>
              <el-menu-item index="1-1">ERP (资源计划)</el-menu-item>
              <el-menu-item index="1-2">HR (人力资源)</el-menu-item>
              <el-menu-item index="1-3">APS (生产排程)</el-menu-item>
              <el-menu-item index="1-4">OA (办公自动化)</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="2">
              <template #title>
                <el-icon><UserFilled /></el-icon>
                <span>客户与销售类</span>
              </template>
              <el-menu-item index="2-1">CRM (客户关系)</el-menu-item>
              <el-menu-item index="2-2">SCRM (社媒营销)</el-menu-item>
              <el-menu-item index="2-3">商机管理</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="3">
              <template #title>
                <el-icon><Goods /></el-icon>
                <span>供应链与采购类</span>
              </template>
              <el-menu-item index="3-1">SRM (供应商)</el-menu-item>
              <el-menu-item index="3-2">SCM (供应链)</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="4">
              <template #title>
                <el-icon><CollectionTag /></el-icon>
                <span>产品与研发类</span>
              </template>
              <el-menu-item index="4-1">PLM (生命周期)</el-menu-item>
              <el-menu-item index="4-2">BOM (物料清单)</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="5">
              <template #title>
                <el-icon><Operation /></el-icon>
                <span>生产与质量类</span>
              </template>
              <el-menu-item index="5-1">QMS (质量管理)</el-menu-item>
              <el-menu-item index="5-2">MES (制造执行)</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="6">
              <template #title>
                <el-icon><Van /></el-icon>
                <span>仓储与物流类</span>
              </template>
              <el-menu-item index="6-1">WMS (仓储管理)</el-menu-item>
              <el-menu-item index="6-2">AGV (自动导引)</el-menu-item>
              <el-menu-item index="6-3">LES (物流执行)</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="7">
              <template #title>
                <el-icon><Cpu /></el-icon>
                <span>设备与能源类</span>
              </template>
              <el-menu-item index="7-1">SCADA (监控采集)</el-menu-item>
              <el-menu-item index="7-2">EMS (能源管理)</el-menu-item>
              <el-menu-item index="7-3">EAM (设备管理)</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="8">
              <template #title>
                <el-icon><DataAnalysis /></el-icon>
                <span>数据与决策类</span>
              </template>
              <el-menu-item index="8-1">BI (商业智能)</el-menu-item>
              <el-menu-item index="8-2">AI (行业模型)</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-scrollbar>
      </aside>

      <!-- 主内容区 -->
      <main class="layout-content">
        <el-scrollbar>
          <div class="content-wrapper">
            <router-view v-slot="{ Component }">
              <transition name="fade-transform" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </el-scrollbar>
      </main>
    </div>
    
    <!-- 移动端遮罩 -->
    <div 
      class="mobile-overlay" 
      v-if="!isMenuCollapsed && isMobile"
      @click="isMenuCollapsed = true"
    ></div>
    <!-- AI语音助手悬浮球 -->
    <VoiceAssistant />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import VoiceAssistant from '../modules/voice/components/VoiceAssistant.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Menu,
  Expand,
  Fold,
  User,
  UserFilled,
  ArrowDown,
  OfficeBuilding,
  Goods,
  CollectionTag,
  Operation,
  Van,
  DataAnalysis,
  Bell,
  FullScreen,
  Setting,
  SwitchButton,
  Cpu,
  Magnet
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// State
const isMenuCollapsed = ref(false)
const isMobile = ref(false)

// Breadcrumbs computation
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  // Simple mapping if meta titles are not set
  const pathParts = route.path.split('/').filter(Boolean)
  if (pathParts.length === 0) return []
  
  // Custom mapping for demonstration
  const nameMap: Record<string, string> = {
    'home': '工作台',
    'erp': 'ERP系统',
    'hr': 'HR系统',
    'crm': 'CRM系统',
    'srm': 'SRM系统',
    // Add more as needed
  }
  
  return pathParts.map(part => nameMap[part] || part.toUpperCase())
})

// Menu active state logic
const activeMenu = computed(() => {
  const pathMap: Record<string, string> = {
    'erp': '1-1', 'hr': '1-2', 'aps': '1-3', 'oa': '1-4',
    'crm': '2-1', 
    'scrm': '2-2',
    'scrm-acquisition-active': '2-2',
    'scrm-acquisition-passive': '2-2',
    'scrm-private-traffic': '2-2',
    'scrm-customer-fission': '2-2',
    'scrm-precision-marketing': '2-2',
    'scrm-data-sync': '2-2',
    'business-opportunity': '2-3',
    'srm': '3-1', 'scm': '3-2',
    'plm': '4-1', 'bom': '4-2',
    'qms': '5-1', 'mes': '5-2',
    'wms': '6-1', 'agv': '6-2', 'les': '6-3',
    'scada': '7-1', 'ems': '7-2', 'eam': '7-3',
    'bi': '8-1', 'ai': '8-2',
  }
  
  if (route.name && pathMap[route.name as string]) {
    return pathMap[route.name as string]
  }

  const parts = route.path.split('/').filter(Boolean)
  const key = parts.find(p => pathMap[p])
  return key ? pathMap[key] : ''
})

// Menu Navigation
const handleMenuSelect = (index: string) => {
  const indexMap: Record<string, string> = {
    '1-1': '/home/erp', '1-2': '/home/hr', '1-3': '/home/aps', '1-4': '/home/oa',
    '2-1': '/home/crm', '2-3': '/home/business-opportunity',
    
    // SCRM Module
    '2-2': '/home/scrm',

    '3-1': '/home/srm', '3-2': '/home/scm',
    '4-1': '/home/plm', '4-2': '/home/bom',
    '5-1': '/home/qms', '5-2': '/home/mes',
    '6-1': '/home/wms', '6-2': '/home/agv', '6-3': '/home/les',
    '7-1': '/home/scada', '7-2': '/home/ems', '7-3': '/home/eam',
    '8-1': '/home/bi', '8-2': '/home/ai',
  }
  const path = indexMap[index]
  if (path) {
    router.push(path)
    if (isMobile.value) {
      isMenuCollapsed.value = true
    }
  }
}

const toggleMenu = () => {
  isMenuCollapsed.value = !isMenuCollapsed.value
}

// User Actions
const handleCommand = (command: string) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/settings')
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    authStore.logout()
    ElMessage.success('登出成功')
    router.push('/login')
  } catch (error) {
    // Cancelled
  }
}

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
  }
}

// Responsive Logic
const checkMobile = () => {
  const isMobileNow = window.innerWidth <= 768
  if (isMobileNow !== isMobile.value) {
    isMobile.value = isMobileNow
    isMenuCollapsed.value = isMobileNow // Auto collapse on mobile
  }
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style scoped lang="scss">
.main-layout {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: var(--bg-color);
}

.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
  padding: 0 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  box-shadow: 0 1px 4px rgba(37, 99, 235, 0.06);
  z-index: 10;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;

  .menu-toggle {
    font-size: 20px;
    color: var(--text-primary);
    &:hover {
      color: var(--primary-color);
    }
  }

  .logo-container {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-right: 20px;

    .logo-img {
      width: 34px;
      height: 34px;
      filter: drop-shadow(0 2px 4px rgba(37, 99, 235, 0.3));
    }

    .logo-text {
      font-size: 19px;
      font-weight: 700;
      letter-spacing: 1.5px;
      background: linear-gradient(90deg, #0284c7 0%, #2563eb 60%, #7c3aed 110%);
      -webkit-background-clip: text;
      background-clip: text;
      -webkit-text-fill-color: transparent;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
  
  .action-items {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .action-item {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      cursor: pointer;
      color: var(--text-regular);
      transition: all 0.3s;
      border-radius: 4px;
      
      &:hover {
        background-color: rgba(0, 0, 0, 0.025);
        color: var(--text-primary);
      }
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 4px;
    transition: background-color 0.3s;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.025);
    }
    
    .user-name {
      font-size: 14px;
      color: var(--text-primary);
      font-weight: 500;
    }
  }
}

.layout-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  position: relative;
}

.layout-sidebar {
  width: 240px;
  background-color: #fff;
  border-right: 1px solid var(--border-color);
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1) 0s;
  display: flex;
  flex-direction: column;
  z-index: 9;
  
  &.sidebar-collapsed {
    width: 64px;
    
    :deep(.el-sub-menu__title) {
      padding: 0 20px !important;
      justify-content: center;
      
      span, .el-sub-menu__icon-arrow {
        display: none;
      }
    }
  }
  
  .sidebar-menu {
    border-right: none;
    padding: 8px 0;

    :deep(.el-sub-menu__title) {
      height: 46px;
      line-height: 46px;
      margin: 4px 8px;
      border-radius: 8px;
      font-weight: 500;
    }

    :deep(.el-menu-item), :deep(.el-sub-menu__title) {
      height: 46px;
      line-height: 46px;
      margin: 4px 8px;
      border-radius: 8px;
      transition: all 0.25s ease;

      &:hover {
        background-color: var(--primary-light-9);
        color: var(--primary-color);
        transform: translateX(2px);
      }

      &.is-active {
        background: linear-gradient(90deg, rgba(14, 165, 233, 0.12) 0%, rgba(37, 99, 235, 0.1) 100%);
        color: var(--primary-color);
        font-weight: 600;
        position: relative;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 60%;
          border-radius: 2px;
          background: linear-gradient(180deg, #0ea5e9, #2563eb);
        }
      }
    }
  }
}

.layout-content {
  flex: 1;
  padding: 20px;
  background-color: var(--bg-color);
  overflow: hidden;
  position: relative;
  
  .content-wrapper {
    min-height: 100%;
  }
}

.mobile-overlay {
  position: fixed;
  top: 64px;
  left: 0;
  width: 100%;
  height: calc(100% - 64px);
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 8;
  backdrop-filter: blur(2px);
}

/* Transitions */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* Responsive */
@media (max-width: 768px) {
  .layout-header {
    padding: 0 12px;
    
    .logo-text {
      font-size: 16px;
    }
  }
  
  .layout-sidebar {
    position: absolute;
    height: 100%;
    left: 0;
    top: 0;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
    transform: translateX(0);
    overflow: hidden;
    
    &.sidebar-collapsed {
      width: 0;
      transform: translateX(-100%);
      border: none;
    }
  }
  
  .layout-content {
    padding: 12px;
  }
  
  .hidden-xs-only {
    display: none !important;
  }
}
</style>
