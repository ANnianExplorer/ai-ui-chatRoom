<template>
  <el-config-provider :size="getThemeConfig.componentSize">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { onMounted, nextTick, computed, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/themeStore.js'
import { useChangeColor } from '@/utils/theme'

const themeStore = useThemeStore()
const { themeConfig } = storeToRefs(themeStore)
const changeColor = useChangeColor()

const getThemeConfig = computed(() => {
  return themeConfig.value
})

const applyThemeColor = () => {
  // 检查是否是用户端（不是管理端）
  const isUserSide = !window.location.pathname.startsWith('/admin')
  
  // 只有用户端才应用主题色，管理端使用默认主题色
  if (isUserSide && themeConfig.value.primary) {
    document.documentElement.style.setProperty(
      '--el-color-primary-dark-2',
      `${changeColor.getDarkColor(themeConfig.value.primary, 0.1)}`
    )
    document.documentElement.style.setProperty('--el-color-primary', themeConfig.value.primary)
    for (let i = 1; i <= 9; i++) {
      document.documentElement.style.setProperty(
        `--el-color-primary-light-${i}`,
        `${changeColor.getLightColor(themeConfig.value.primary, i / 10)}`
      )
    }
  } else {
    // 管理端使用默认主题色
    document.documentElement.style.removeProperty('--el-color-primary-dark-2')
    document.documentElement.style.removeProperty('--el-color-primary')
    for (let i = 1; i <= 9; i++) {
      document.documentElement.style.removeProperty(`--el-color-primary-light-${i}`)
    }
  }
}

const applyNavigationTheme = () => {
  // 检查是否是用户端（不是管理端）
  const isUserSide = !window.location.pathname.startsWith('/admin')
  
  // 只有用户端才应用导航主题样式，管理端使用默认样式
  if (isUserSide && themeConfig.value.primary) {
    const primaryColor = themeConfig.value.primary
    const isLight = changeColor.isLightColor(primaryColor)
    
    // 导航栏样式 - 保持固定的紫色渐变，不随主题变化
    const navBgColor = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    const navTextColor = '#ffffff'
    const navHoverColor = 'rgba(255, 255, 255, 0.2)'
    const navActiveColor = 'rgba(255, 255, 255, 0.3)'
    
    // 应用导航栏样式 - 保持固定颜色
    document.documentElement.style.setProperty('--nav-bg-color', navBgColor)
    document.documentElement.style.setProperty('--nav-text-color', navTextColor)
    document.documentElement.style.setProperty('--nav-hover-color', navHoverColor)
    document.documentElement.style.setProperty('--nav-active-color', navActiveColor)
    document.documentElement.style.setProperty('--nav-user-bg', 'rgba(255, 255, 255, 0.15)')
    document.documentElement.style.setProperty('--nav-user-border', 'rgba(255, 255, 255, 0.25)')
    document.documentElement.style.setProperty('--nav-user-hover', 'rgba(255, 255, 255, 0.25)')
    
    // 应用侧边栏样式 - 主题色影响侧边栏的强调色
    document.documentElement.style.setProperty('--sidebar-hover-color', `rgba(${changeColor.hexToRgb(primaryColor)}, 0.15)`)
    document.documentElement.style.setProperty('--sidebar-active-color', `rgba(${changeColor.hexToRgb(primaryColor)}, 0.25)`)
    
    // 应用聊天框样式
    document.documentElement.style.setProperty('--message-self-bg', primaryColor)
    document.documentElement.style.setProperty('--message-self-text', isLight ? '#000000' : '#ffffff')
  } else {
    // 管理端清除自定义样式，使用默认样式
    document.documentElement.style.removeProperty('--nav-bg-color')
    document.documentElement.style.removeProperty('--nav-text-color')
    document.documentElement.style.removeProperty('--nav-hover-color')
    document.documentElement.style.removeProperty('--nav-active-color')
    document.documentElement.style.removeProperty('--nav-user-bg')
    document.documentElement.style.removeProperty('--nav-user-border')
    document.documentElement.style.removeProperty('--nav-user-hover')
    document.documentElement.style.removeProperty('--sidebar-hover-color')
    document.documentElement.style.removeProperty('--sidebar-active-color')
    document.documentElement.style.removeProperty('--message-self-bg')
    document.documentElement.style.removeProperty('--message-self-text')
  }
}

const applyAllTheme = () => {
  applyThemeColor()
  applyNavigationTheme()
}

const applyDarkMode = (isDark) => {
  // 检查是否是用户端（不是管理端）
  const isUserSide = !window.location.pathname.startsWith('/admin')
  
  if (isDark && isUserSide) {
    // 深色模式下，整个用户端都是黑色，图标和文字都是白色
    document.documentElement.classList.add('dark')
    document.documentElement.style.setProperty('--nav-bg-color', '#1a1a1a', 'important')
    document.documentElement.style.setProperty('--nav-text-color', '#ffffff', 'important')
    document.documentElement.style.setProperty('--nav-hover-color', 'rgba(255, 255, 255, 0.15)', 'important')
    document.documentElement.style.setProperty('--nav-active-color', 'rgba(255, 255, 255, 0.25)', 'important')
    document.documentElement.style.setProperty('--nav-user-bg', 'rgba(255, 255, 255, 0.1)', 'important')
    document.documentElement.style.setProperty('--nav-user-border', 'rgba(255, 255, 255, 0.2)', 'important')
    document.documentElement.style.setProperty('--nav-user-hover', 'rgba(255, 255, 255, 0.2)', 'important')
    
    document.documentElement.style.setProperty('--sidebar-bg-color', '#1a1a1a', 'important')
    document.documentElement.style.setProperty('--sidebar-text-color', '#ffffff', 'important')
    document.documentElement.style.setProperty('--sidebar-icon-color', '#ffffff', 'important')
    document.documentElement.style.setProperty('--sidebar-hover-color', 'rgba(255, 255, 255, 0.15)', 'important')
    document.documentElement.style.setProperty('--sidebar-active-color', 'rgba(255, 255, 255, 0.25)', 'important')
    document.documentElement.style.setProperty('--sidebar-divider-color', 'rgba(255, 255, 255, 0.1)', 'important')
    
    document.documentElement.style.setProperty('--chat-bg-color', '#1a1a1a', 'important')
    document.documentElement.style.setProperty('--chat-header-bg', '#2a2a2a', 'important')
    document.documentElement.style.setProperty('--chat-input-bg', '#2a2a2a', 'important')
    document.documentElement.style.setProperty('--message-bg', '#2a2a2a', 'important')
    document.documentElement.style.setProperty('--message-self-bg', '#409eff', 'important')
    document.documentElement.style.setProperty('--message-self-text', '#ffffff', 'important')
    
    document.body.classList.add('dark-mode')
  } else {
    // 恢复正常主题样式
    document.documentElement.classList.remove('dark')
    document.body.classList.remove('dark-mode')
    // 清除深色模式样式
    document.documentElement.style.removeProperty('--nav-bg-color')
    document.documentElement.style.removeProperty('--nav-text-color')
    document.documentElement.style.removeProperty('--nav-hover-color')
    document.documentElement.style.removeProperty('--nav-active-color')
    document.documentElement.style.removeProperty('--nav-user-bg')
    document.documentElement.style.removeProperty('--nav-user-border')
    document.documentElement.style.removeProperty('--nav-user-hover')
    
    document.documentElement.style.removeProperty('--sidebar-bg-color')
    document.documentElement.style.removeProperty('--sidebar-text-color')
    document.documentElement.style.removeProperty('--sidebar-icon-color')
    document.documentElement.style.removeProperty('--sidebar-hover-color')
    document.documentElement.style.removeProperty('--sidebar-active-color')
    document.documentElement.style.removeProperty('--sidebar-divider-color')
    
    document.documentElement.style.removeProperty('--chat-bg-color')
    document.documentElement.style.removeProperty('--chat-header-bg')
    document.documentElement.style.removeProperty('--chat-input-bg')
    document.documentElement.style.removeProperty('--message-bg')
    document.documentElement.style.removeProperty('--message-self-bg')
    document.documentElement.style.removeProperty('--message-self-text')
    
    applyNavigationTheme()
  }
}

onMounted(() => {
  nextTick(() => {
    const themeConfigStr = localStorage.getItem('themeConfig')
    if (themeConfigStr) {
      themeStore.setThemeConfig(JSON.parse(themeConfigStr))
    }
    // 检查是否是管理端
    const isUserSide = !window.location.pathname.startsWith('/admin')
    // 如果是管理端，不应用深色模式
    if (!isUserSide && themeConfig.value.darkMode) {
      themeConfig.value.darkMode = false
    }
    applyAllTheme()
    applyDarkMode(themeConfig.value.darkMode)
  })
})

window.addEventListener('theme-color-change', () => {
  applyAllTheme()
})

window.addEventListener('dark-mode-change', (e) => {
  applyDarkMode(e.detail)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
}

#app {
  height: 100vh;
  background-color: #fafafa;
}
</style>
