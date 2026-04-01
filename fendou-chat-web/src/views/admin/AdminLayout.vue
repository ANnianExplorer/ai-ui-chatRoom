<template>
  <el-container class="admin-layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="admin-header">
      <div class="header-content">
        <div class="header-left">
          <img class="logo" style="width: 40px; height: 40px;" src="@/assets/logo.png" alt="logo"/>
          <h1 class="admin-title">后台管理系统</h1>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" effect="light" placement="bottom-end">
            <span class="admin-user-info">
              <el-avatar :size="36" src="@/assets/admin-avatar.png" />
              <span style="margin-left: 8px; font-weight: 500;">管理员</span>
              <el-icon class="el-icon--right transition-all duration-300 rotate-0 hover:rotate-180"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout" class="transition-all duration-200 hover:bg-blue-50 hover:text-blue-600">
                  <el-icon class="mr-2"><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 主内容区域 -->
    <el-container class="admin-main-container">
      <!-- 左侧侧边栏 -->
      <el-aside class="admin-sidebar" width="180px">
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          router
          :default-openeds="['user-management', 'message-management']"
          background-color="#ffffff"
          text-color="#303133"
          active-text-color="#409eff"
          :collapse-transition="true"
        >
          <!-- 用户管理 -->
          <el-sub-menu index="user-management">
            <template #title>
              <el-icon class="menu-icon"><user /></el-icon>
              <span class="menu-text">用户管理</span>
            </template>
            <el-menu-item index="/admin/users">
              <el-icon class="menu-icon"><List /></el-icon>
              <span class="menu-text">用户列表</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 群聊管理 -->
          <el-sub-menu index="group-management">
            <template #title>
              <el-icon class="menu-icon"><chat-dot-round /></el-icon>
              <span class="menu-text">群聊管理</span>
            </template>
            <el-menu-item index="/admin/groups">
              <el-icon class="menu-icon"><List /></el-icon>
              <span class="menu-text">群聊列表</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 消息管理 -->
          <el-sub-menu index="message-management">
            <template #title>
              <el-icon class="menu-icon"><message /></el-icon>
              <span class="menu-text">消息管理</span>
            </template>
            <el-menu-item index="/admin/messages">
              <el-icon class="menu-icon"><List /></el-icon>
              <span class="menu-text">消息列表</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 文件管理 -->
          <el-sub-menu index="file-management">
            <template #title>
              <el-icon class="menu-icon"><folder /></el-icon>
              <span class="menu-text">文件管理</span>
            </template>
            <el-menu-item index="/admin/files">
              <el-icon class="menu-icon"><List /></el-icon>
              <span class="menu-text">文件列表</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 数据大屏 -->
          <el-menu-item index="/admin/dashboard">
            <el-icon class="menu-icon"><DataAnalysis /></el-icon>
            <span class="menu-text">数据大屏</span>
          </el-menu-item>

          <!-- 系统运营 -->
          <el-sub-menu index="operation">
            <template #title>
              <el-icon class="menu-icon"><Monitor /></el-icon>
              <span class="menu-text">系统运营</span>
            </template>
            <el-menu-item index="/admin/announcement">
              <el-icon class="menu-icon"><Bell /></el-icon>
              <span class="menu-text">公告推送</span>
            </el-menu-item>
            <el-menu-item index="/admin/violation">
              <el-icon class="menu-icon"><Warning /></el-icon>
              <span class="menu-text">违规监控</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 系统设置 -->
          <el-sub-menu index="system-settings">
            <template #title>
              <el-icon class="menu-icon"><setting /></el-icon>
              <span class="menu-text">系统设置</span>
            </template>
            <el-menu-item index="/admin/settings">
              <el-icon class="menu-icon"><User /></el-icon>
              <span class="menu-text">个人中心</span>
            </el-menu-item>
            <el-menu-item index="/admin/about">
              <el-icon class="menu-icon"><InfoFilled /></el-icon>
              <span class="menu-text">系统介绍</span>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-aside>

      <!-- 右侧主内容 -->
      <el-main class="admin-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  User,
  ChatDotRound,
  Message,
  Folder,
  Setting,
  List,
  SwitchButton,
  InfoFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 处理管理员退出登录
const handleLogout = () => {
  ElMessageBox.confirm('您确定要退出管理员登录吗?', '退出登录', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
    beforeClose: (action, instance, done) => {
      if (action === 'confirm') {
        instance.confirmButtonLoading = true
        instance.confirmButtonText = '正在退出...'
        setTimeout(() => {
          // 清除管理员登录信息
          localStorage.removeItem('adminToken')
          localStorage.removeItem('isAdmin')
          instance.confirmButtonLoading = false
          done()
          ElMessage.success('退出成功')
          router.push('/admin/login')
        }, 1000)
      } else {
        done()
      }
    }
  })
}
</script>

<style scoped lang="scss">
/* 全局样式变量 */
$primary-color: #3b82f6; /* 更深更饱满的蓝色 */
$primary-light: #dbeafe; /* 更饱满的浅蓝色背景 */
$primary-hover: #2563eb; /* 悬停时的深蓝色 */
$bg-color: #f8fafc;
$sidebar-bg: #ffffff;
$text-color: #1e293b;
$border-color: #e2e8f0;
$shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
$shadow-medium: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
$transition-fast: 0.2s ease;
$transition-normal: 0.3s ease;
$transition-slow: 0.5s ease;

.admin-layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: $bg-color;
  overflow: hidden;
}

/* 顶部导航栏样式 */
.admin-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: $shadow-medium;
  padding: 0 32px;
  height: 70px;
  line-height: 70px;
  display: flex;
  align-items: center;
  z-index: 100;
  transition: all $transition-normal;

  &:hover {
    box-shadow: 0 6px 20px 0 rgba(0, 0, 0, 0.12);
  }
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.logo {
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px;
  box-shadow: $shadow-light;
  transition: all $transition-normal;

  &:hover {
    transform: scale(1.1);
    box-shadow: $shadow-medium;
  }
}

.admin-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  letter-spacing: 1px;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all $transition-normal;
  color: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.2);

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-2px);
    box-shadow: $shadow-medium;
  }
}

/* 主内容区域 */
.admin-main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧侧边栏 */
.admin-sidebar {
  background-color: $sidebar-bg;
  box-shadow: 2px 0 12px 0 rgba(0, 0, 0, 0.06);
  overflow-y: auto;
  height: 100%;
  transition: all $transition-normal;
  z-index: 99;

  /* 隐藏滚动条 */
  -ms-overflow-style: none;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }

  &:hover {
    box-shadow: 4px 0 20px 0 rgba(0, 0, 0, 0.08);
  }
}

/* 菜单样式 */
.admin-menu {
  height: 100%;
  border-right: none;
  transition: all $transition-normal;

  /* 菜单容器 */
  :deep(.el-menu) {
    background-color: $sidebar-bg;
    border-right: none;
  }

  /* 菜单项和子菜单标题公共样式 */
  :deep(.el-sub-menu__title),
  :deep(.el-menu-item) {
    height: 56px;
    line-height: 56px;
    padding-left: 28px !important;
    transition: all $transition-normal;
    border-left: 4px solid transparent;
    margin: 2px 8px;
    border-radius: 8px;
    position: relative;
    overflow: hidden;
    background-color: transparent;

    /* 增强的渐变背景 */
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      width: 0;
      height: 100%;
      background: linear-gradient(90deg, rgba(59, 130, 246, 0.15), rgba(59, 130, 246, 0.05));
      transition: width $transition-normal;
      z-index: 0;
    }

    &:hover::before {
      width: 100%;
    }

    /* 图标和文字 */
    .menu-icon,
    .menu-text {
      position: relative;
      z-index: 1;
      transition: all $transition-normal;
    }

    .menu-icon {
      font-size: 22px; /* 增大图标尺寸 */
      margin-right: 14px; /* 增加图标与文字间距 */
      width: 24px; /* 固定图标宽度，确保对齐 */
      text-align: center;
      color: #64748b; /* 默认图标颜色 */
      transition: all $transition-normal;
    }

    .menu-text {
      font-size: 15px;
      font-weight: 500;
    }
  }

  /* 菜单项 */
  :deep(.el-menu-item) {
    /* 重置所有菜单项的样式，确保只有当前激活和悬停的项才有蓝色效果 */
    background-color: transparent !important;
    color: $text-color !important;
    border-left-color: transparent;
    box-shadow: none;
    
    .menu-icon {
      color: #64748b;
      font-size: 22px;
      filter: none;
    }

    &:hover {
      background-color: rgba(59, 130, 246, 0.1) !important; /* 更明显的悬停背景 */
      color: $primary-color !important;
      border-left-color: $primary-color;
      transform: translateX(3px);
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15); /* 增加悬停阴影 */

      .menu-icon {
        color: $primary-color;
        transform: scale(1.1); /* 图标悬停放大效果 */
      }
    }

    &.is-active {
      background: rgba(59, 130, 246, 0.15) !important; /* 更饱满的激活背景 */
      color: $primary-color !important;
      border-left-color: $primary-color;
      font-weight: 600;
      box-shadow: 0 3px 12px rgba(59, 130, 246, 0.2); /* 激活项阴影 */

      .menu-icon {
        color: $primary-color;
        font-size: 24px; /* 激活项图标放大 */
        filter: drop-shadow(0 2px 4px rgba(59, 130, 246, 0.3)); /* 图标阴影效果 */
      }

      &:hover {
        background-color: rgba(59, 130, 246, 0.2) !important; /* 激活状态下的悬停效果 */
      }
    }
  }

  /* 子菜单标题 */
  :deep(.el-sub-menu__title) {
    /* 重置所有子菜单标题的样式 */
    background-color: transparent !important;
    color: $text-color !important;
    border-left-color: transparent;
    box-shadow: none;
    
    .menu-icon {
      color: #64748b;
    }

    &:hover {
      background-color: rgba(59, 130, 246, 0.1) !important;
      color: $primary-color !important;
      border-left-color: $primary-color;
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);

      .menu-icon {
        color: $primary-color;
        transform: scale(1.1);
      }
    }

    /* 子菜单展开图标 */
    :deep(.el-icon-arrow-down) {
      transition: all $transition-normal;
      font-size: 16px;
      color: #94a3b8;
    }
  }

  /* 子菜单展开状态 */
  :deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
    background-color: rgba(59, 130, 246, 0.12) !important;
    color: $primary-color !important;
    border-left-color: $primary-color;
    box-shadow: 0 3px 12px rgba(59, 130, 246, 0.18);

    .menu-icon {
      color: $primary-color;
      font-size: 23px;
    }

    :deep(.el-icon-arrow-down) {
      transform: rotate(180deg);
      color: $primary-color;
    }
  }

  /* 子菜单内容 */
  :deep(.el-menu--inline) {
    background-color: transparent;
    padding: 4px 0;
  }

  /* 子菜单下的菜单项 */
  :deep(.el-sub-menu .el-menu-item) {
    padding-left: 52px !important;
    font-size: 14px;
    height: 52px;
    line-height: 52px;
    
    /* 重置子菜单下所有菜单项的样式 */
    background-color: transparent !important;
    color: $text-color !important;
    border-left-color: transparent;
    box-shadow: none;
    
    .menu-icon {
      color: #64748b;
      font-size: 20px;
      filter: none;
    }

    &:hover {
      background-color: rgba(59, 130, 246, 0.1) !important;
      color: $primary-color !important;
      border-left-color: $primary-color;
      transform: translateX(5px);
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);

      .menu-icon {
        color: $primary-color;
        transform: scale(1.05);
      }
    }

    &.is-active {
      padding-left: 48px !important;
      background-color: rgba(59, 130, 246, 0.18) !important;
      color: $primary-color !important;
      border-left-color: $primary-color;
      box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);

      .menu-icon {
        color: $primary-color;
        font-size: 22px;
        filter: drop-shadow(0 2px 4px rgba(59, 130, 246, 0.3));
      }
    }
  }
}

/* 右侧主内容 */
.admin-content {
  padding: 24px;
  background-color: $bg-color;
  overflow-y: auto;
  flex: 1;
  transition: all $transition-normal;

  /* 滚动条样式 */
  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 4px;
    transition: all $transition-fast;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
}

/* 过渡动画类 */
.transition-all {
  transition: all $transition-normal;
}

.duration-200 {
  transition-duration: 0.2s;
}

.duration-300 {
  transition-duration: 0.3s;
}

.rotate-0 {
  transform: rotate(0deg);
}

.rotate-180 {
  transform: rotate(180deg);
}

.hover\:rotate-180:hover {
  transform: rotate(180deg);
}

.hover\:bg-blue-50:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.hover\:text-blue-600:hover {
  color: $primary-color;
}

.mr-2 {
  margin-right: 8px;
}
</style>