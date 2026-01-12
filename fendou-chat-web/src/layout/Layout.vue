<template>
  <el-container class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <el-menu :default-active="route.path" mode="horizontal" :ellipsis="false" :router="true" class="header-menu">
          <div class="logo-container" @click="router.push('/index')">
            <img class="logo" src="@/assets/logo.png" alt="logo"/>
          </div>

          <div style="flex: 1; color: white">欢迎来到FenDouChat聊天室，请开始你的聊天之旅！</div>
          
          <!-- 日期和天气组件 -->
          <DateWeather />
          
          <el-dropdown trigger="click">
            <span class="header-title">
              <el-avatar :size="30" :src="userStore.user.avatar" />
              <span style="margin-left: 8px">{{ userStore.user.realName }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-menu>
      </div>
    </el-header>

    <!-- 主内容区域 -->
    <el-container class="content-container">
      <!-- 左侧全局侧边栏 -->
      <AppSidebar
        class="sidebar-nav"
        @add-friend="handleAddUser"
        @create-group="handleAddGroup"
      />

      <!-- 右侧内容 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 对话框组件 -->
    <AddFriendDialog ref="addFriendDialogRef" />
    <AddGroupDialog ref="addGroupDialogRef" />
  </el-container>
</template>

<script setup>
import { defineAsyncComponent, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 引入侧边栏组件
import AppSidebar from './component/AppSidebar.vue'
import DateWeather from './component/DateWeather.vue'

// 异步导入对话框组件
const AddFriendDialog = defineAsyncComponent(() => import('@/views/contact/component/AddFriend.vue'))
const AddGroupDialog = defineAsyncComponent(() => import('@/views/contact/component/AddGroup.vue'))

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 对话框引用
const addFriendDialogRef = ref(null)
const addGroupDialogRef = ref(null)

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('您确定要退出登录吗?', '退出登录', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    beforeClose: (action, instance, done) => {
      if (action === 'confirm') {
        instance.confirmButtonLoading = true
        instance.confirmButtonText = '正在退出登录中...'
        setTimeout(() => {
          instance.confirmButtonLoading = false
          done()
        }, 1000)
      } else {
        done()
      }
    }
  }).then(async () => {
    await userStore.toLogout()
    ElMessage.success('成功退出登录')
    window.location.reload()
  })
}

// 添加好友
const handleAddUser = () => {
  addFriendDialogRef.value?.openDialog()
}

// 创建群聊
const handleAddGroup = () => {
  addGroupDialogRef.value?.openDialog()
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  padding: 0;
  flex-shrink: 0;
  height: auto;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  z-index: 10;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: all 0.3s ease;
  background-color: var(--nav-bg-color, #ffffff);

  &:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  }

  .header-content {
    width: 100%;
  }

  .header-menu {
    display: flex;
    align-items: center;
    background-color: transparent;
    border-bottom: none;
    width: 100%;
    justify-content: space-between;

    .logo-container {
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s ease;
      margin-right: 16px;
      height: 100%;
      
      &:hover {
        transform: translateY(-2px);
      }
    }

    .logo {
      height: 50px;
      width: auto;
      max-width: 100px;
      background: rgba(255, 255, 255, 0.9);
      padding: 4px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: all 0.3s ease;
      object-fit: contain;

      &:hover {
        transform: scale(1.05);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      }
    }

    .title {
      font-size: 18px;
      font-weight: bold;
      margin-right: 15px;
      color: var(--nav-text-color, #303133);
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }
  }

  :deep(.el-menu-item) {
    color: var(--nav-text-color, #303133);
    transition: all 0.3s ease;
    margin: 0 4px;
    border-radius: 8px;
    
    &:hover {
      background-color: var(--nav-hover-color, rgba(59, 130, 246, 0.15)) !important;
      color: var(--primary-color, #3b82f6) !important;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    &.is-active {
      color: var(--primary-color, #3b82f6) !important;
      background-color: var(--nav-active-color, rgba(59, 130, 246, 0.2)) !important;
      font-weight: 600;
      box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
    }
  }
}

.header-title {
  padding: 8px 16px;
  cursor: pointer;
  color: var(--nav-text-color, #303133);
  display: flex;
  align-items: center;
  border-radius: 25px;
  background: var(--nav-user-bg, rgba(59, 130, 246, 0.1));
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  border: 1px solid var(--nav-user-border, rgba(59, 130, 246, 0.2));
  
  &:hover {
    background: var(--nav-user-hover, rgba(59, 130, 246, 0.2));
    color: var(--primary-color, #3b82f6);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.content-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar-nav {
  flex-shrink: 0;
  height: 100%;
}

.main {
  padding: 10px;
  flex: 1;
  overflow: auto;
  background-color: #f5f5f5;
  box-sizing: border-box;
  /* 隐藏滚动条 */
  -ms-overflow-style: none;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}
</style>

<style lang="scss">
/* 全局导航栏样式 */
.el-header {
  background: var(--nav-bg-color, #ffffff);
  transition: all 0.3s ease;
}

.el-menu--horizontal {
  border-bottom: none !important;
  background-color: transparent !important;
}

/* 全局主题变量 - 与管理端一致的配色 */
:root {
  /* 管理端一致的主题色 */
  --primary-color: #3b82f6;
  /* 导航栏默认配色 - 与管理端一致的紫色渐变 */
  --nav-bg-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  --nav-text-color: #ffffff;
  --nav-hover-color: rgba(255, 255, 255, 0.2);
  --nav-active-color: rgba(255, 255, 255, 0.3);
  --nav-user-bg: rgba(255, 255, 255, 0.15);
  --nav-user-border: rgba(255, 255, 255, 0.25);
  --nav-user-hover: rgba(255, 255, 255, 0.25);
  /* 侧边栏默认配色 - 与管理端完全一致 */
  --sidebar-bg-color: #ffffff;
  --sidebar-text-color: #303133;
  --sidebar-icon-color: #303133;
  --sidebar-hover-color: rgba(59, 130, 246, 0.15);
  --sidebar-active-color: rgba(59, 130, 246, 0.25);
  --sidebar-divider-color: rgba(0, 0, 0, 0.1);
  /* 聊天框默认配色 */
  --chat-bg-color: #f7f8fa;
  --chat-header-bg: #ffffff;
  --chat-input-bg: #ffffff;
  --message-bg: #ffffff;
  --message-self-bg: #409eff;
  --message-self-text: #ffffff;
}
</style>
