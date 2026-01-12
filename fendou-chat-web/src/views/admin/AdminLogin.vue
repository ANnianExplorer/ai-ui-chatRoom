<template>
  <div class="admin-login-container">
    <div class="login-wrapper">
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        size="large"
      >
        <el-form-item>
          <div class="login-header">管理员登录</div>
        </el-form-item>
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员用户名"
            prefix-icon="User"
          ></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入管理员密码"
            prefix-icon="Lock"
            show-password
          ></el-input>
        </el-form-item>
        <el-form-item prop="code">
          <el-input
            v-model="loginForm.code"
            placeholder="请输入验证码"
            prefix-icon="Check"
            @keyup.enter="handleLogin"
            class="code"
          >
          </el-input>
          <img 
            class="img-code" 
            :src="captcha" 
            alt="验证码" 
            title="看不清换一张" 
            @click="refreshCaptcha" 
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
            native-type="submit"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLoginApi } from '@/api/login/index.js'
import { generateRandomString } from '@/utils/common.js'

const router = useRouter()
const loginFormRef = ref()
const loading = ref(false)
const captcha = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  code: '',
  randomStr: ''
})

const loginRules = reactive({
  username: [
    { required: true, message: '请输入管理员用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
})

// 获取验证码
const refreshCaptcha = async () => {
  const loginApi = useLoginApi()
  loginForm.randomStr = generateRandomString(16)
  const res = await loginApi.getCaptcha(loginForm.randomStr)
  captcha.value = res.data
}

// 初始化验证码
onMounted(async () => {
  await refreshCaptcha()
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    // 初始化登录API
    const loginApi = useLoginApi()
    
    // 调用真实登录API
    const response = await loginApi.login(loginForm)
    
    // 登录成功，保存管理员token
    localStorage.setItem('adminToken', response.data.token)
    localStorage.setItem('isAdmin', 'true')
    localStorage.setItem('userInfo', JSON.stringify(response.data.user))
    
    ElMessage.success('登录成功')
    router.push('/admin')
  } catch (error) {
    ElMessage.error('登录失败: ' + (error.response?.data?.message || error.message || '未知错误'))
    console.error('登录失败:', error)
    // 刷新验证码
    await refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.admin-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-image: url('https://images.unsplash.com/photo-1586023492125-27b2c045efd7?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  overflow: hidden;
}

.login-wrapper {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  /* 毛玻璃效果 */
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.login-wrapper:hover {
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.4);
  transform: translateY(-2px);
}

.login-header {
  font-size: 35px;
  font-weight: bold;
  margin: 0 auto 30px;
  text-align: center;
  color: #333;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.5);
}

.login-btn {
  width: 100%;
  margin-top: 25px;
  height: 45px;
  font-size: 16px;
  border-radius: 25px;
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.code {
  width: 67%;
  margin-right: 10px;
}

.img-code {
  width: 30%;
  height: 40px;
  object-fit: cover;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s ease;
  vertical-align: middle;
}

.img-code:hover {
  transform: scale(1.05);
}

/* 表单输入框样式 */
.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.3) !important;
  border: 1px solid rgba(255, 255, 255, 0.5) !important;
  border-radius: 10px !important;
  backdrop-filter: blur(5px) !important;
  -webkit-backdrop-filter: blur(5px) !important;
}

.login-form :deep(.el-input__inner) {
  color: #333 !important;
  font-size: 14px !important;
}

/* 验证码输入框容器 */
.login-form :deep(.el-form-item) {
  margin-bottom: 25px !important;
}

/* 调整验证码输入框和图片的布局 */
.login-form :deep(.el-form-item:last-of-type .el-input) {
  display: inline-block;
  vertical-align: middle;
}

.login-form :deep(.el-form-item:last-of-type img) {
  display: inline-block;
  vertical-align: middle;
}
</style>

