import { defineStore } from 'pinia'
import { useLoginApi } from '@/api/login/index.js'

const loginApi = useLoginApi()

/**
 * 用户信息
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    user: {}
  }),
  actions: {
    // 获取用户信息
    async getInfo() {
      const res = await loginApi.getInfo()
      this.user = res.data
    },
    // 登录
    async toLogin(data) {
      const res = await loginApi.login(data)
      sessionStorage.setItem('token', res.data.token)
      sessionStorage.setItem('ws', res.data.ws)
      // 登录成功后获取用户信息
      await this.getInfo()
    },
    // 登出
    async toLogout() {
      await loginApi.logout()
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('ws')
    }
  }
})
