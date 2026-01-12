<template>
  <el-row :gutter="20">
    <!-- 个人信息卡片 -->
    <el-col :xs="24" :sm="12" :md="8" :lg="6">
      <el-card header="个人信息" shadow="never">
        <div class="profile-avatar">
          <el-upload
            :action="state.fileUploadApi"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
            :headers="state.fileHeaders"
            name="file"
            :show-file-list="false"
          >
            <el-avatar shape="circle" :size="150" :src="state.userForm.avatar" />
          </el-upload>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名称" label-align="right" min-width="100">{{
            state.userForm.username
          }}</el-descriptions-item>
          <el-descriptions-item label="用户邮箱" label-align="right">{{ state.userForm.email }}</el-descriptions-item>
          <el-descriptions-item label="注册时间" label-align="right">{{
            state.userForm.createTime
          }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-col>

    <!-- 基本资料卡片 -->
    <el-col :xs="24" :sm="12" :md="16" :lg="18">
      <el-card header="基本资料" shadow="never">
        <el-tabs v-model="state.activeTab">
          <!-- 个人信息标签页 -->
          <el-tab-pane label="个人信息" name="profile">
            <el-form ref="profileFormRef" :model="state.userForm" :rules="state.profileRules" label-width="80px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input v-model="state.userForm.realName" placeholder="输入您的真实姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用户性别" prop="gender">
                    <el-select v-model="state.userForm.gender" placeholder="请选择性别" style="width: 100%">
                      <el-option
                        v-for="item in state.genderSelectOptions"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="出生日期" prop="birthday">
                    <el-date-picker
                      v-model="state.userForm.birthday"
                      type="date"
                      placeholder="请选择你的出生日期"
                      format="YYYY/MM/DD"
                      value-format="YYYY-MM-DD"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号码" prop="phone">
                    <el-input v-model="state.userForm.phone" placeholder="输入您的手机" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="所在地址" prop="address">
                    <el-input v-model="state.userForm.address" placeholder="输入您的地址" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="个人简介" prop="description">
                <el-input v-model="state.userForm.description" type="textarea" :rows="3" placeholder="请输入用户简介" />
              </el-form-item>

              <el-form-item>
                <el-button @click="resetProfileForm">重置</el-button>
                <el-button type="primary" @click="saveProfile" :loading="state.savingProfile">保存</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 修改密码标签页 -->
          <el-tab-pane label="修改密码" name="password">
            <el-form
              ref="passwordFormRef"
              :model="state.passwordForm"
              :rules="state.passwordRules"
              label-width="80px"
              class="password-form"
            >
              <el-space style="width: 100%" fill>
                <el-alert type="info" show-icon :closable="false">
                  <p>注意：密码修改后，将在下次登录生效。</p>
                </el-alert>
                <el-form-item label="旧密码" prop="oldPwd">
                  <el-input
                    v-model="state.passwordForm.oldPwd"
                    type="password"
                    placeholder="输入账号的原登录密码"
                    show-password
                  />
                </el-form-item>
              </el-space>

              <el-form-item label="新密码" prop="newPwd">
                <el-input
                  v-model="state.passwordForm.newPwd"
                  type="password"
                  placeholder="输入新的密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="确认密码" prop="confirmPwd">
                <el-input
                  v-model="state.passwordForm.confirmPwd"
                  type="password"
                  placeholder="请输入正确的新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button @click="resetPasswordForm">重置</el-button>
                <el-button type="primary" @click="savePassword" :loading="state.savingPassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user.js'
import { useUserApi } from '@/api/user/index.js'

const userStore = useUserStore()
const userApi = useUserApi()

// 响应式数据
const profileFormRef = ref()
const passwordFormRef = ref()
const state = reactive({
  genderSelectOptions: [
    { text: '保密', value: 0, type: 'info' },
    { text: '男', value: 1, type: 'success' },
    { text: '女', value: 2, type: 'danger' }
  ],
  fileUploadApi: import.meta.env.VITE_APP_API_URL + 'file/upload',
  fileHeaders: {
    Authorization: 'Bearer ' + sessionStorage.getItem('token')
  },
  activeTab: 'profile',

  savingProfile: false,
  userForm: userStore.user,
  profileRules: {
    realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
    phone: [
      {
        pattern: /^1[3456789]\d{9}$/,
        message: '手机号码格式错误',
        trigger: ['blur', 'change']
      }
    ]
  },

  savingPassword: false,
  passwordForm: {
    oldPwd: '',
    newPwd: '',
    confirmPwd: ''
  },
  passwordRules: {
    oldPwd: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
    newPwd: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value === state.passwordForm.oldPwd) {
            callback(new Error('新密码不能与旧密码一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    confirmPwd: [
      { required: true, message: '请输入确认密码', trigger: 'blur' },
      {
        validator: (rule, value, callback) => {
          if (value !== state.passwordForm.newPwd) {
            callback(new Error('两次密码输入不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
})

// 头像上传相关方法
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传头像图片大小不能超过 10MB!')
    return false
  }
  return isImage && isLt10M
}

const handleAvatarSuccess = (res, file) => {
  if (res.code === 200) {
    state.userForm.avatar = res.data.filePath
    saveProfile()
  } else {
    ElMessage.error(res.message || '头像上传失败')
  }
}

const resetProfileForm = () => {
  profileFormRef.value?.resetFields()
}

const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      state.savingProfile = true
      try {
        const res = await userApi.updateProfile(state.userForm)
        ElMessage.success(res.message)
        console.log(userStore.user)
      } finally {
        state.savingProfile = false
      }
    }
  })
}

// 修改密码
const savePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      state.savingPassword = true
      try {
        const res = await userApi.updatePassword(state.passwordForm)
        ElMessage.success(res.message)
      } finally {
        state.savingPassword = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.profile-avatar {
  text-align: center;
  margin-bottom: 20px;

  :deep(.el-upload) {
    display: inline-block;
  }

  .el-avatar {
    cursor: pointer;

    &:hover {
      opacity: 0.8;
    }
  }
}
</style>
