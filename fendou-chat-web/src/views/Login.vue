<template>
  <div class="login-container">
    <!-- 左侧图片区域 -->
    <div class="login-left"></div>
    
    <!-- 右侧表单容器 -->
    <div class="login-right">
      <div class="login-wrapper">
        <el-form
          ref="loginFormRef"
          :model="state.loginForm"
          size="large"
          :rules="state.rules"
          @keydown.enter="handleLogin"
        >
          <el-form-item>
            <div class="login-header">欢迎登录FenDou聊天室</div>
          </el-form-item>
          <el-form-item prop="username">
            <el-input v-model="state.loginForm.username" placeholder="账号">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input type="password" v-model="state.loginForm.password" placeholder="密码" show-password>
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="code">
            <el-input class="code" v-model="state.loginForm.code" placeholder="验证码">
              <template #prefix>
                <el-icon><Check /> </el-icon>
              </template>
            </el-input>
            <img class="img-code" :src="state.captcha" alt="验证码" title="看不清换一张" @click="refreshCaptcha" />
          </el-form-item>
          <div class="register">没有账号？<span @click="openRegister">点击注册</span></div>

          <el-form-item>
            <el-button type="primary" class="login-btn" :loading="state.loading" @click="handleLogin()">登录</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLoginApi } from '@/api/login/index.js'
import { useUserStore } from '@/stores/user.js'
import { ElMessage } from 'element-plus'
import { generateRandomString } from '@/utils/common.js'
import '@/theme/login.scss'

const router = useRouter()
const loginApi = useLoginApi()
const userStore = useUserStore()
const loginFormRef = ref()
const state = reactive({
  loading: false,
  captcha: '',
  loginForm: {
    username: '',
    password: '',
    code: '',
    randomStr: ''
  },
  rules: {
    username: [{ required: true, message: '请输入用户账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入用户密码', trigger: 'blur' }],
    code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
  }
})

// 获取验证码
const refreshCaptcha = async () => {
  state.loginForm.randomStr = generateRandomString(16)
  const res = await loginApi.getCaptcha(state.loginForm.randomStr)
  state.captcha = res.data
}

// 渲染时钩子函数
onMounted(async () => {
  await refreshCaptcha()
})

// 注册
const openRegister = () => {
  router.push('/register')
}

// 登录
const handleLogin = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        state.loading = true
        await userStore.toLogin(state.loginForm)
        ElMessage.success('登录成功')
        await router.push('/index')
      } catch (error) {
        await refreshCaptcha()
      } finally {
        state.loading = false
      }
    }
  })
}
</script>
