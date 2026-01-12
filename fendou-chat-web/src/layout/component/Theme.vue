<template>
  <el-drawer v-model="state.visible" size="30%" title="布局配置">
    <div class="chat-settings-item">
      <span>系统主题</span>
      <el-color-picker v-model="getThemeConfig.primary" :predefine="state.predefineColors" @change="changeThemeColor" />
    </div>

    <div class="chat-settings-item">
      <span>暗黑模式</span>
      <el-switch
        v-model="getThemeConfig.darkMode"
        active-text="开启"
        inactive-text="关闭"
        @change="toggleDarkMode"
      />
    </div>

    <div class="chat-settings-item">
      <span>组件大小</span>
      <el-select v-model="getThemeConfig.componentSize" @change="changeComponentSize" style="width: 30%">
        <el-option value="large" label="大型" />
        <el-option value="default" label="默认" />
        <el-option value="small" label="小型" />
      </el-select>
    </div>

    <div class="chat-settings-footer">
      <el-button style="width: 100%" icon="refreshLeft" @click="resetTheme">恢复默认</el-button>
    </div>
  </el-drawer>
</template>

<script setup>
import { reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/themeStore.js'
import { useChangeColor } from '@/utils/theme'

const storesThemeConfig = useThemeStore()
const { themeConfig } = storeToRefs(storesThemeConfig)
const changeColor = useChangeColor()

// 获取布局配置信息
const getThemeConfig = computed(() => {
  return themeConfig.value
})

const state = reactive({
  visible: false,
  predefineColors: ['#ff4500', '#ff8c00', '#ffd700', '#90ee90', '#00ced1', '#1e90ff', '#c71585', '#c7158577']
})

// 打开抽屉
const openDrawer = () => {
  state.visible = true
}

// 重置布局
const resetTheme = () => {
  localStorage.removeItem('themeConfig')
  localStorage.removeItem('themeConfigStyle')
  window.location.reload()
}

// 应用暗黑模式
const applyDarkMode = (isDark) => {
  // 检查是否是用户端（不是管理端）
  const isUserSide = !window.location.pathname.startsWith('/admin')
  
  if (isDark && isUserSide) {
    document.documentElement.classList.add('dark')
    document.body.classList.add('dark-mode')
    // 深色模式下，整个用户端都是黑色，图标和文字都是白色
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
  } else {
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
  }
}

// 切换暗黑模式
const toggleDarkMode = () => {
  applyDarkMode(getThemeConfig.value.darkMode)
  window.dispatchEvent(new CustomEvent('dark-mode-change', { detail: getThemeConfig.value.darkMode }))

  setThemeConfig()
}

// 修改主题颜色
const changeThemeColor = () => {
  // 检查是否是用户端（不是管理端）
  const isUserSide = !window.location.pathname.startsWith('/admin')
  
  if (!getThemeConfig.value.primary) return ElMessage.warning('全局主题 primary 颜色值不能为空')
  
  // 只有用户端才应用主题色，管理端使用默认主题色
  if (isUserSide) {
    // 颜色加深
    document.documentElement.style.setProperty(
      '--el-color-primary-dark-2',
      `${changeColor.getDarkColor(getThemeConfig.value.primary, 0.1)}`
    )
    document.documentElement.style.setProperty('--el-color-primary', getThemeConfig.value.primary)
    // 颜色变浅
    for (let i = 1; i <= 9; i++) {
      document.documentElement.style.setProperty(
        `--el-color-primary-light-${i}`,
        `${changeColor.getLightColor(getThemeConfig.value.primary, i / 10)}`
      )
    }
  }

  setThemeConfig()
}

// 修改组件大小
const changeComponentSize = () => {
  setThemeConfig()
}

// 存储布局
const setThemeConfig = () => {
  localStorage.removeItem('themeConfig')
  localStorage.setItem('themeConfig', JSON.stringify(getThemeConfig.value))
  localStorage.setItem('themeConfigStyle', document.documentElement.style.cssText)
}

onMounted(() => {
  nextTick(() => {
    // 检查是否是用户端（不是管理端）
    const isUserSide = !window.location.pathname.startsWith('/admin')
    // 如果是管理端，不应用深色模式
    if (!isUserSide && getThemeConfig.value.darkMode) {
      getThemeConfig.value.darkMode = false
    }
    if (getThemeConfig.value.darkMode) {
      toggleDarkMode()
    }
  })
})

// 暴露方法
defineExpose({
  openDrawer
})
</script>

<style lang="scss" scoped>
.chat-settings-item {
  display: flex;
  justify-content: space-between;
  margin: 16px 0;
}

.chat-settings-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
}
</style>
