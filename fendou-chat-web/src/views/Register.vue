<template>
  <div class="login-container">
    <!-- 左侧图片区域 -->
    <div class="login-left"></div>
    
    <!-- 右侧表单容器 -->
    <div class="login-right">
      <div class="login-wrapper">
        <el-form
          ref="loginFormRef"
          :model="state.registerForm"
          size="large"
          :rules="state.rules"
          @keydown.enter="handleRegister"
        >
          <el-form-item>
            <div class="login-header">注&nbsp;&nbsp;册</div>
          </el-form-item>
          <el-form-item prop="username">
            <el-input v-model="state.registerForm.username" placeholder="账号">
              <template #prefix>
                <el-icon><User /> </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input type="password" v-model="state.registerForm.password" placeholder="新密码" show-password>
              <template #prefix>
                <el-icon><Lock /> </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="checkPassword">
            <el-input type="password" v-model="state.registerForm.checkPassword" placeholder="确认密码" show-password>
              <template #prefix>
                <el-icon> <Lock /> </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="realName">
            <el-input v-model="state.registerForm.realName" placeholder="真实姓名">
              <template #prefix>
                <el-icon> <UserFilled /> </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="state.registerForm.email" placeholder="邮箱">
              <template #prefix>
                <el-icon> <Message /> </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <div class="register">已有账号？<span @click="openLogin">点击登录</span></div>
          <el-form-item>
            <el-button type="primary" class="login-btn" :loading="state.loading" @click="handleRegister()">
              注册
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLoginApi } from '@/api/login/index.js'
import '@/theme/login.scss'

const loginApi = useLoginApi()
const router = useRouter()
const loginFormRef = ref()
const state = reactive({
  loading: false,
  registerForm: {
    username: '',
    password: '',
    checkPassword: '',
    realName: '',
    email: ''
  },
  rules: {
    username: [{ required: true, message: '请输入用户账号', trigger: 'blur' }],
    password: [{ required: true, message: '请输入用户密码', trigger: 'blur' }],
    checkPassword: [
      { required: true, message: '请输入确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (!value) {
            return callback(new Error('确认密码不能为空'))
          }

          if (value !== state.registerForm.password) {
            return callback(new Error('两次输入密码不一致'))
          }

          callback()
        },
        trigger: 'blur'
      }
    ],
    realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  }
})

// 登录
const openLogin = () => {
  router.push('/login')
}

// 注册
const handleRegister = () => {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        state.loading = true
        await loginApi.register(state.registerForm)
        ElMessage.success('注册成功')
        await router.push('/login')
      } finally {
        state.loading = false
      }
    }
  })
}
</script>
