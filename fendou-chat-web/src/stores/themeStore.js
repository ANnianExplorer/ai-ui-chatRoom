// stores/themeStore.js
import { defineStore } from 'pinia'

/**
 * 布局配置
 */
export const useThemeStore = defineStore('themeConfig', {
  state: () => ({
    themeConfig: {
      // 主题色
      primary: '#409eff',
      // 深色模式
      darkMode: false,

      // 语言
      locale: 'zh-cn',
      // 组件大小
      componentSize: 'default'
    }
  }),

  actions: {
    // 设置主题配置
    setThemeConfig(themeConfig) {
      this.themeConfig = themeConfig
    }
  }
})
