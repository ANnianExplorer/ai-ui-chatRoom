<template>
  <el-config-provider :size="getThemeConfig.componentSize">
    <div class="app-layout" :class="{ 'dark-mode': isDark }">
      <!-- 背景装饰层 -->
      <div class="bg-layer">
        <div class="bg-orb bg-orb-1"></div>
        <div class="bg-orb bg-orb-2"></div>
        <div class="bg-orb bg-orb-3"></div>
        <div class="bg-orb bg-orb-4"></div>
        <div class="bg-grid"></div>
      </div>
      <!-- 光标跟随光晕 -->
      <div class="cursor-follower" ref="cursorFollowerRef"></div>

      <!-- 顶部导航栏 -->
      <header class="app-header">
        <div class="header-inner">
          <!-- Logo区域 -->
          <div class="header-logo" @click="router.push('/index')">
            <div class="logo-wrapper">
              <img src="@/assets/logo.png" alt="logo" class="logo-img" />
            </div>
            <span class="logo-text">FenDouChat</span>
          </div>

          <!-- 中间欢迎文字 -->
          <div class="header-welcome">
            <span class="welcome-text">欢迎来到FenDouChat聊天室，请开始你的聊天之旅！</span>
          </div>

          <!-- 右侧操作区 -->
          <div class="header-actions">
            <!-- 日期天气 -->
            <DateWeather />

            <!-- 暗色模式切换 -->
            <div class="theme-toggle" @click="toggleDark" :title="isDark ? '切换白天模式' : '切换夜晚模式'">
              <transition name="toggle-icon">
                <svg v-if="isDark" viewBox="0 0 24 24" fill="none" class="toggle-icon">
                  <circle cx="12" cy="12" r="5" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 2v2M12 20v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M2 12h2M20 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" class="toggle-icon">
                  <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </transition>
            </div>

            <!-- 用户信息 -->
            <el-dropdown trigger="click" placement="bottom-end">
              <div class="user-pill">
                <el-avatar :size="28" :src="userStore.user.avatar" class="user-avatar" />
                <span class="user-name">{{ userStore.user.realName }}</span>
                <svg viewBox="0 0 24 24" fill="none" class="chevron-icon">
                  <path d="M6 9l6 6 6-6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="user-dropdown">
                  <el-dropdown-item @click="router.push('profile')">
                    <svg viewBox="0 0 24 24" fill="none" class="dropdown-icon"><circle cx="12" cy="8" r="4" stroke="currentColor" stroke-width="2"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <svg viewBox="0 0 24 24" fill="none" class="dropdown-icon" style="color:#ef4444"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
                    <span style="color:#ef4444">退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </header>

      <!-- 主内容区域 -->
      <div class="app-body">
        <!-- 左侧导航栏 -->
        <AppSidebar
          class="app-sidebar"
          @add-friend="handleAddUser"
          @create-group="handleAddGroup"
        />

        <!-- 主内容 -->
        <main class="app-main">
          <router-view v-slot="{ Component }">
            <transition name="page-fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </main>
      </div>

      <!-- 对话框 -->
      <AddFriendDialog ref="addFriendDialogRef" />
      <AddGroupDialog ref="addGroupDialogRef" />
    </div>
  </el-config-provider>
</template>

<script setup>
import { defineAsyncComponent, ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/themeStore.js'
import { storeToRefs } from 'pinia'

import AppSidebar from './component/AppSidebar.vue'
import DateWeather from './component/DateWeather.vue'

const AddFriendDialog = defineAsyncComponent(() => import('@/views/contact/component/AddFriend.vue'))
const AddGroupDialog = defineAsyncComponent(() => import('@/views/contact/component/AddGroup.vue'))

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const { themeConfig } = storeToRefs(themeStore)

const addFriendDialogRef = ref(null)
const addGroupDialogRef = ref(null)
const cursorFollowerRef = ref(null)

const getThemeConfig = computed(() => themeConfig.value)
const isDark = computed(() => themeConfig.value.darkMode)

/* 光标跟随光晕 */
let rafId = null
let mouseX = 0, mouseY = 0
let followerX = 0, followerY = 0

const onMouseMove = (e) => {
  mouseX = e.clientX
  mouseY = e.clientY
}

const animateCursor = () => {
  followerX += (mouseX - followerX) * 0.1
  followerY += (mouseY - followerY) * 0.1
  if (cursorFollowerRef.value) {
    cursorFollowerRef.value.style.transform = `translate(${followerX - 200}px, ${followerY - 200}px)`
  }
  rafId = requestAnimationFrame(animateCursor)
}

const toggleDark = () => {
  themeConfig.value.darkMode = !themeConfig.value.darkMode
  if (themeConfig.value.darkMode) {
    document.documentElement.classList.add('dark')
    document.body.classList.add('dark-mode')
  } else {
    document.documentElement.classList.remove('dark')
    document.body.classList.remove('dark-mode')
  }
  localStorage.setItem('themeConfig', JSON.stringify(themeConfig.value))
  window.dispatchEvent(new CustomEvent('dark-mode-change', { detail: themeConfig.value.darkMode }))
}

const handleLogout = () => {
  ElMessageBox.confirm('您确定要退出登录吗？', '退出登录', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userStore.toLogout()
    ElMessage.success('成功退出登录')
    window.location.reload()
  })
}

const handleAddUser = () => addFriendDialogRef.value?.openDialog()
const handleAddGroup = () => addGroupDialogRef.value?.openDialog()

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove, { passive: true })
  animateCursor()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  if (rafId) cancelAnimationFrame(rafId)
})
</script>

<style lang="scss" scoped>
.app-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--page-bg-gradient);
  overflow: hidden;
  position: relative;
  font-family: var(--font-sans);
}

/* 背景装饰 */
.bg-layer {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
  animation: float 8s ease-in-out infinite;
}

.bg-orb-1 {
  width: 700px; height: 700px;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.18) 0%, transparent 70%);
  top: -300px; left: -200px;
  animation-delay: 0s;
}
.bg-orb-2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(6, 182, 212, 0.12) 0%, transparent 70%);
  bottom: -200px; right: -150px;
  animation-delay: 3s;
}
.bg-orb-3 {
  width: 400px; height: 400px;
  background: radial-gradient(circle, rgba(168, 85, 247, 0.1) 0%, transparent 70%);
  top: 40%; left: 40%;
  animation-delay: 5s;
}
.bg-orb-4 {
  width: 300px; height: 300px;
  background: radial-gradient(circle, rgba(236, 72, 153, 0.08) 0%, transparent 70%);
  top: 60%; left: 10%;
  animation-delay: 2s;
  animation-duration: 12s;
}

/* 科技网格背景 */
.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(124, 58, 237, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(124, 58, 237, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
}

/* ── 顶部导航栏 ── */
.app-header {
  height: var(--nav-height, 52px);
  background: var(--nav-bg);
  backdrop-filter: blur(20px) saturate(200%);
  -webkit-backdrop-filter: blur(20px) saturate(200%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15), 0 1px 0 rgba(255,255,255,0.1) inset;
  flex-shrink: 0;
  z-index: 100;
  position: relative;
  overflow: hidden;

  /* 头部扫描线动画 */
  &::after {
    content: '';
    position: absolute;
    top: 0; left: -100%;
    width: 50%; height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.06), transparent);
    animation: header-scan 6s ease-in-out infinite;
    pointer-events: none;
  }
}
@keyframes header-scan {
  0%   { left: -60%; }
  100% { left: 160%; }
}

.header-inner {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 16px;
}

.header-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.25s ease;

  &:hover {
    opacity: 0.85;
    transform: translateX(2px);
  }
}

.logo-wrapper {
  width: 34px; height: 34px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  overflow: hidden;
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.15), inset 0 1px 0 rgba(255,255,255,0.3);
  transition: all 0.3s;
  .header-logo:hover & {
    box-shadow: 0 0 20px rgba(255, 255, 255, 0.3), 0 0 40px rgba(167, 139, 250, 0.2);
    transform: rotate(5deg) scale(1.05);
  }
}

.logo-img {
  width: 26px; height: 26px;
  object-fit: contain;
}

.logo-text {
  font-family: 'Space Grotesk', var(--font-sans);
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.header-welcome {
  flex: 1;
  text-align: center;
  overflow: hidden;

  .welcome-text {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.75);
    font-weight: 400;
    letter-spacing: 0.01em;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    display: block;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

/* 暗色模式切换 */
.theme-toggle {
  width: 36px; height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  color: #ffffff;

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    transform: scale(1.05);
  }

  .toggle-icon {
    width: 18px; height: 18px;
  }
}

.toggle-icon-enter-active,
.toggle-icon-leave-active {
  transition: all 0.2s ease;
}
.toggle-icon-enter-from { opacity: 0; transform: rotate(-90deg) scale(0.6); }
.toggle-icon-leave-to   { opacity: 0; transform: rotate(90deg) scale(0.6); }

/* 用户胶囊 */
.user-pill {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 6px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.25);
  cursor: pointer;
  transition: all 0.25s ease;
  color: #ffffff;

  &:hover {
    background: rgba(255, 255, 255, 0.28);
    transform: translateY(-1px);
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.2);
  }
}

.user-avatar { border: 2px solid rgba(255,255,255,0.4); }

.user-name {
  font-size: 13px;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chevron-icon {
  width: 14px; height: 14px;
  opacity: 0.7;
}

.dropdown-icon {
  width: 15px; height: 15px;
  margin-right: 6px;
  flex-shrink: 0;
}

/* ── 主体布局 ── */
.app-body {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.app-sidebar {
  flex-shrink: 0;
  height: 100%;
  z-index: 50;
}

.app-main {
  flex: 1;
  overflow: hidden;
  position: relative;
}

/* 页面切换过渡 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.25s ease;
}
.page-fade-enter-from {
  opacity: 0;
  transform: translateX(8px);
}
.page-fade-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

/* 浮动动画 */
@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50%       { transform: translateY(-20px) scale(1.02); }
}

/* 光标跟随光晕 */
.cursor-follower {
  position: fixed;
  top: 0; left: 0;
  width: 400px; height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.07) 0%, transparent 65%);
  pointer-events: none;
  z-index: 0;
  transition: opacity 0.3s;
  will-change: transform;
}

.dark-mode .cursor-follower {
  background: radial-gradient(circle, rgba(167, 139, 250, 0.1) 0%, transparent 65%);
}
</style>

<style lang="scss">
/* ── 全局Element Plus覆盖 ── */
.el-header {
  background: transparent !important;
}

.el-menu--horizontal {
  border-bottom: none !important;
  background-color: transparent !important;
}

.user-dropdown {
  border-radius: 12px !important;
  border: 1px solid var(--border-default) !important;
  background: var(--glass-bg-strong) !important;
  backdrop-filter: blur(20px);
  box-shadow: var(--glass-shadow-lg) !important;
  padding: 4px !important;

  .el-dropdown-menu__item {
    border-radius: 8px !important;
    margin: 2px 0;
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: var(--text-primary) !important;
    transition: all 0.2s ease;

    &:hover {
      background: var(--color-primary-subtle) !important;
      color: var(--color-primary) !important;
    }
  }
}
</style>
