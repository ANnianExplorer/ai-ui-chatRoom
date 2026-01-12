<template>
  <div class="app-sidebar qq-nav">
    <div class="nav-items">
      <div
        class="nav-item"
        :class="{ active: activePath === '/index' }"
        @click="handleNavClick('/index')"
      >
        <el-icon><ChatDotRound /></el-icon>
        <span class="nav-text">消息</span>
      </div>
      <div
        class="nav-item"
        :class="{ active: activePath === '/qq-contact' }"
        @click="handleNavClick('/qq-contact')"
      >
        <el-icon><User /></el-icon>
        <span class="nav-text">联系人</span>
      </div>
      <div
        class="nav-item"
        :class="{ active: activePath === '/qq-group' }"
        @click="handleNavClick('/qq-group')"
      >
        <el-icon><Collection /></el-icon>
        <span class="nav-text">群聊</span>
      </div>
      <div
        class="nav-item"
        :class="{ active: activePath === '/favorite' }"
        @click="handleNavClick('/favorite')"
      >
        <el-icon><StarFilled /></el-icon>
        <span class="nav-text">收藏</span>
      </div>
      <div
        class="nav-item"
        :class="{ active: activePath === '/document' }"
        @click="handleNavClick('/document')"
      >
        <el-icon><DocumentCopy /></el-icon>
        <span class="nav-text">文档</span>
      </div>
      <div class="nav-divider"></div>
      <div
        class="nav-item"
        :class="{ active: activePath === '/settings' }"
        @click="handleNavClick('/settings')"
      >
        <el-icon><Setting /></el-icon>
        <span class="nav-text">设置</span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  User,
  Collection,
  StarFilled,
  DocumentCopy,
  Setting,
  Plus
} from '@element-plus/icons-vue'

// 定义emits
const emit = ['add-friend', 'create-group']

// router实例
const router = useRouter()
const route = useRoute()

// 当前激活的路径
const activePath = computed(() => route.path)

// 处理导航点击
const handleNavClick = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}

</script>

<style scoped>
/* 全局样式变量 */
$primary-color: #3b82f6;
$primary-light: #dbeafe;
$primary-hover: #2563eb;
$bg-color: #f8fafc;
$sidebar-bg: #ffffff;
$text-color: #1e293b;
$border-color: #e2e8f0;
$shadow-light: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
$shadow-medium: 0 4px 16px 0 rgba(0, 0, 0, 0.08);
$transition-fast: 0.2s ease;
$transition-normal: 0.3s ease;

.app-sidebar {
  width: 64px;
  height: 100%;
  background-color: var(--sidebar-bg-color, $sidebar-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0;
  box-shadow: $shadow-light;
  z-index: 20;
  border-right: 1px solid $border-color;
  flex-shrink: 0;
  transition: all $transition-normal;
}

.nav-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  padding: 8px 0;
  overflow-y: auto;
  /* 隐藏滚动条 */
  -ms-overflow-style: none;
  scrollbar-width: none;
  &::-webkit-scrollbar {
    display: none;
  }
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  cursor: pointer;
  color: var(--sidebar-text-color, $text-color);
  border-radius: 8px;
  transition: all $transition-normal;
  position: relative;
  overflow: hidden;
  background-color: transparent;
  margin: 2px 4px;
  border-left: 4px solid transparent;

  /* 渐变背景效果 - 与管理端一致 */
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

  .el-icon,
  .nav-text {
    position: relative;
    z-index: 1;
    transition: all $transition-normal;
  }

  .el-icon {
    font-size: 22px;
    margin-bottom: 4px;
    transition: all $transition-normal;
    color: var(--sidebar-icon-color, $text-color);
    width: 24px;
    text-align: center;
  }

  .nav-text {
    font-size: 11px;
    line-height: 1;
    font-weight: 500;
  }

  &:hover {
    background-color: var(--sidebar-hover-color, rgba(59, 130, 246, 0.15)) !important;
    color: $primary-color !important;
    border-left-color: $primary-color;
    transform: translateX(3px);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.15);

    .el-icon {
      color: $primary-color;
      transform: scale(1.1);
    }
  }

  &.active {
    background-color: var(--sidebar-active-color, rgba(59, 130, 246, 0.25)) !important;
    color: $primary-color !important;
    border-left-color: $primary-color;
    font-weight: 600;
    box-shadow: 0 3px 12px rgba(59, 130, 246, 0.2);

    .el-icon {
      color: $primary-color;
      font-size: 24px;
      filter: drop-shadow(0 2px 4px rgba(59, 130, 246, 0.3));
    }

    &:hover {
      background-color: rgba(59, 130, 246, 0.3) !important;
    }
  }
}

.nav-divider {
  height: 1px;
  background: var(--sidebar-divider-color, rgba(0, 0, 0, 0.1));
  margin: 8px 0;
  width: 56px;
}

.nav-footer {
  padding: 8px 0;
  border-top: 1px solid var(--sidebar-divider-color, rgba(0, 0, 0, 0.1));
  flex-shrink: 0;
}

.nav-footer .nav-item {
  height: 48px;
}

.add-btn {
  height: 48px;
  transition: all $transition-normal;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  }
}
</style>
