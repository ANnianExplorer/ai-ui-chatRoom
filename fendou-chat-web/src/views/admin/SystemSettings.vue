<template>
  <div class="admin-system-settings">
    <div class="page-header">
      <h2>个人中心</h2>
      <p>管理管理员个人信息和密码</p>
    </div>

    <!-- 个人中心内容 -->
    <el-card shadow="hover" class="settings-card">
      <div class="tab-content">
        <el-card shadow="hover" class="profile-card">
          <h3 class="section-title">管理员信息</h3>
          <el-form :model="adminProfile" :rules="profileRules" ref="profileFormRef" label-width="120px" class="profile-form">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="adminProfile.username" readonly disabled />
            </el-form-item>
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="adminProfile.realName" placeholder="请输入真实姓名" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="adminProfile.email" type="email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="adminProfile.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
              >
                <img v-if="adminProfile.avatar" :src="adminProfile.avatar" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" icon="Check">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" class="password-card">
          <h3 class="section-title">修改密码</h3>
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px" class="password-form">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword" icon="RefreshRight">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminApi } from '@/api/admin/index.js'
import {
  Plus
} from '@element-plus/icons-vue'

// 初始化API
const adminApi = useAdminApi()

// 响应式数据
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

// 管理员个人信息 - 初始化为空对象，将从API获取数据
const adminProfile = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  avatar: ''
})

// 验证新密码不能与旧密码相同
const validateNewPassword = (rule, value, callback) => {
  if (value === passwordForm.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    callback()
  }
}

// 验证确认密码
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 个人信息表单验证规则
const profileRules = reactive({
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '真实姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: ['blur', 'change'] }
  ]
})

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码修改表单验证规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

// 加载管理员信息
const loadAdminProfile = async () => {
  try {
    // 调用API获取管理员信息
    const res = await adminApi.settings.getAdminProfile()
    Object.assign(adminProfile, res.data)
  } catch (error) {
    console.log('加载管理员信息失败:', error)
    // 如果获取失败，继续使用默认数据
  }
}

// 初始化数据
onMounted(() => {
  // 加载管理员信息
  loadAdminProfile()
})

// 更新管理员信息
const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 调用真实API
        await adminApi.settings.updateAdminProfile(adminProfile)
        ElMessage.success('个人信息更新成功')
      } catch (error) {
        ElMessage.error('个人信息更新失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 调用真实API
        await adminApi.settings.updateAdminPassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        
        ElMessage.success('密码修改成功')
        // 清空表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } catch (error) {
        ElMessage.error('密码修改失败: ' + (error.message || '未知错误'))
      }
    }
  })
}

// 头像上传成功处理
const handleAvatarSuccess = (response, file, fileList) => {
  // 模拟头像上传成功
  adminProfile.avatar = URL.createObjectURL(file.raw)
  ElMessage.success('头像上传成功')
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}
</script>

<style scoped lang="scss">
.admin-system-settings {
  .page-header {
    margin-bottom: 24px;
    h2 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
    p {
      margin: 0;
      font-size: 14px;
      color: #0a0a0a;
    }
  }
  
  .settings-tabs-card {
    margin-bottom: 20px;
  }
  
  .settings-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }
  }
  
  .tab-content {
    padding: 0;
  }
  
  .section-title {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  
  .profile-card,
  .password-card {
    margin-bottom: 20px;
  }
  
  .profile-form,
  .password-form {
    max-width: 600px;
  }
  
  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed var(--el-border-color);
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: var(--el-transition-duration-fast);
      display: inline-block;
      width: 120px;
      height: 120px;
      
      &:hover {
        border-color: var(--el-color-primary);
      }
    }
    
    .avatar {
      width: 120px;
      height: 120px;
      display: block;
      object-fit: cover;
    }
    
    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 120px;
      height: 120px;
      text-align: center;
      line-height: 120px;
    }
  }
  
  .system-info {
    margin-bottom: 20px;
  }
  
  .feature-card {
    height: 180px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 20px;
    
    .feature-icon {
      font-size: 48px;
      color: var(--el-color-primary);
      margin-bottom: 16px;
    }
    
    .feature-title {
      margin: 0 0 8px 0;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
    
    .feature-desc {
      margin: 0;
      font-size: 14px;
      color: #606266;
    }
  }
  
  .team-content {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  
  .team-logo {
    margin-right: 20px;
  }
  
  .team-name {
    margin: 0 0 8px 0;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
  
  .team-desc {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: #606266;
  }
  
  .team-contacts {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .contact-item {
      font-size: 14px;
      color: #606266;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}
</style>