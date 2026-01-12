<template>
  <div class="settings-container">
    <div class="settings-layout">
      <!-- 左侧设置导航 -->
      <div class="settings-sidebar">
        <div class="settings-nav">
          <div 
            v-for="item in navList" 
            :key="item.key"
            class="settings-nav-item"
            :class="{ active: activeNav === item.key }"
            @click="activeNav = item.key"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧设置内容 -->
      <div class="settings-content">
        <!-- 主题设置 -->
        <div v-show="activeNav === 'theme'" class="settings-section">
          <div class="section-header">
            <h2>主题设置</h2>
            <p>自定义您的应用外观和体验</p>
          </div>
          
          <div class="settings-card">
            <div class="card-header">
              <h3>外观设置</h3>
            </div>
            <div class="card-body">
              <div class="settings-item">
                <div class="item-left">
                  <span class="item-label">系统主题色</span>
                  <span class="item-desc">选择应用的主色调</span>
                </div>
                <div class="item-right">
                  <el-color-picker 
                    v-model="themeConfig.primary" 
                    :predefine="predefineColors" 
                    @change="changeThemeColor"
                    size="large"
                  />
                </div>
              </div>
              
              <div class="settings-item">
                <div class="item-left">
                  <span class="item-label">组件大小</span>
                  <span class="item-desc">调整界面元素的尺寸</span>
                </div>
                <div class="item-right">
                  <el-radio-group v-model="themeConfig.componentSize" @change="changeComponentSize">
                    <el-radio-button value="large">大型</el-radio-button>
                    <el-radio-button value="default">默认</el-radio-button>
                    <el-radio-button value="small">小型</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
            </div>
          </div>
          
          <div class="settings-card">
            <div class="card-header">
              <h3>主题预设</h3>
            </div>
            <div class="card-body">
              <div class="theme-presets">
                <div 
                  v-for="color in themePresets" 
                  :key="color.value"
                  class="theme-preset-item"
                  @click="selectThemePreset(color.value)"
                >
                  <div class="preset-color" :style="{ background: color.gradient }"></div>
                  <span class="preset-name">{{ color.name }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <div class="settings-actions">
            <el-button type="primary" icon="Refresh" @click="resetTheme">恢复默认设置</el-button>
          </div>
        </div>

        <!-- 账号设置 -->
        <div v-show="activeNav === 'account'" class="settings-section">
          <div class="section-header">
            <h2>账号设置</h2>
            <p>管理您的账号信息和安全</p>
          </div>
          
          <div class="settings-card">
            <div class="card-header">
              <h3>个人信息</h3>
            </div>
            <div class="card-body">
              <el-tabs v-model="activeAccountTab">
                <!-- 基本信息标签页 -->
                <el-tab-pane label="基本信息" name="profile">
                  <el-form ref="profileFormRef" :model="userForm" :rules="profileRules" label-width="100px" size="large">
                    <div class="settings-form-row">
                      <el-form-item label="头像">
                        <div class="avatar-uploader">
                          <el-upload
                            :action="fileUploadApi"
                            :on-success="handleAvatarSuccess"
                            :before-upload="beforeAvatarUpload"
                            :headers="fileHeaders"
                            name="file"
                            :show-file-list="false"
                          >
                            <el-avatar shape="circle" :size="120" :src="userForm.avatar" class="avatar-preview">
                              <el-icon><User /></el-icon>
                            </el-avatar>
                          </el-upload>
                          <p class="avatar-hint">点击头像更换</p>
                        </div>
                      </el-form-item>
                    </div>
                    
                    <div class="settings-form-row">
                      <el-form-item label="真实姓名" prop="realName">
                        <el-input v-model="userForm.realName" placeholder="请输入真实姓名" />
                      </el-form-item>
                      
                      <el-form-item label="用户性别" prop="gender">
                        <el-select v-model="userForm.gender" placeholder="请选择性别" style="width: 100%">
                          <el-option
                            v-for="item in genderSelectOptions"
                            :key="item.value"
                            :label="item.text"
                            :value="item.value"
                          ></el-option>
                        </el-select>
                      </el-form-item>
                    </div>
                    
                    <div class="settings-form-row">
                      <el-form-item label="出生日期" prop="birthday">
                        <el-date-picker
                          v-model="userForm.birthday"
                          type="date"
                          placeholder="请选择您的出生日期"
                          format="YYYY/MM/DD"
                          value-format="YYYY-MM-DD"
                          style="width: 100%"
                        />
                      </el-form-item>
                      
                      <el-form-item label="手机号码" prop="phone">
                        <el-input v-model="userForm.phone" placeholder="请输入您的手机号码" />
                      </el-form-item>
                    </div>
                    
                    <div class="settings-form-row">
                      <el-form-item label="所在地址" prop="address">
                        <el-input v-model="userForm.address" placeholder="请输入您的地址" />
                      </el-form-item>
                    </div>
                    
                    <el-form-item label="个人简介" prop="description">
                      <el-input v-model="userForm.description" type="textarea" :rows="4" placeholder="请输入个人简介" />
                    </el-form-item>
                    
                    <el-form-item>
                      <el-button @click="resetProfileForm">重置</el-button>
                      <el-button type="primary" @click="saveProfile" :loading="savingProfile">保存</el-button>
                    </el-form-item>
                  </el-form>
                </el-tab-pane>
                
                <!-- 修改密码标签页 -->
                <el-tab-pane label="修改密码" name="password">
                  <el-form
                    ref="passwordFormRef"
                    :model="passwordForm"
                    :rules="passwordRules"
                    label-width="100px"
                    size="large"
                    class="password-form"
                  >
                    <el-alert type="info" show-icon :closable="false" style="margin-bottom: 20px;">
                      <p>注意：密码修改后，将在下次登录生效。</p>
                    </el-alert>
                    
                    <el-form-item label="旧密码" prop="oldPwd">
                      <el-input
                        v-model="passwordForm.oldPwd"
                        type="password"
                        placeholder="请输入原密码"
                        show-password
                      />
                    </el-form-item>
                    
                    <el-form-item label="新密码" prop="newPwd">
                      <el-input
                        v-model="passwordForm.newPwd"
                        type="password"
                        placeholder="请输入新密码"
                        show-password
                      />
                    </el-form-item>
                    
                    <el-form-item label="确认密码" prop="confirmPwd">
                      <el-input
                        v-model="passwordForm.confirmPwd"
                        type="password"
                        placeholder="请再次输入新密码"
                        show-password
                      />
                    </el-form-item>
                    
                    <el-form-item>
                      <el-button @click="resetPasswordForm">重置</el-button>
                      <el-button type="primary" @click="savePassword" :loading="savingPassword">保存</el-button>
                    </el-form-item>
                  </el-form>
                </el-tab-pane>
                
                <!-- 设备管理标签页 -->
                <el-tab-pane label="设备管理" name="devices">
                  <div class="empty-devices">
                    <el-empty description="暂无设备信息" />
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
        </div>

        <!-- 通知设置 -->
        <div v-show="activeNav === 'notification'" class="settings-section">
          <div class="section-header">
            <h2>通知设置</h2>
            <p>管理消息通知的方式和频率</p>
          </div>
          
          <div class="settings-card">
            <div class="card-header">
              <h3>消息通知</h3>
            </div>
            <div class="card-body">
              <div class="settings-item">
                <div class="item-left">
                  <span class="item-label">新消息提醒</span>
                  <span class="item-desc">收到新消息时是否通知</span>
                </div>
                <div class="item-right">
                  <el-switch v-model="notificationSettings.messageRemind" />
                </div>
              </div>
              
              <div class="settings-item">
                <div class="item-left">
                  <span class="item-label">声音提示</span>
                  <span class="item-desc">收到消息时播放提示音</span>
                </div>
                <div class="item-right">
                  <el-switch v-model="notificationSettings.sound" />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 隐私设置 -->
        <div v-show="activeNav === 'privacy'" class="settings-section">
          <div class="section-header">
            <h2>隐私设置</h2>
            <p>控制您的隐私和数据安全</p>
          </div>
          
          <div class="settings-card">
            <div class="card-header">
              <h3>隐私保护</h3>
            </div>
            <div class="card-body">
              <div class="settings-item">
                <div class="item-left">
                  <span class="item-label">在线状态</span>
                  <span class="item-desc">是否向他人显示您的在线状态</span>
                </div>
                <div class="item-right">
                  <el-switch v-model="privacySettings.showOnlineStatus" />
                </div>
              </div>
              
              <div class="settings-item">
                <div class="item-left">
                  <span class="item-label">已读回执</span>
                  <span class="item-desc">显示消息已读状态给对方</span>
                </div>
                <div class="item-right">
                  <el-switch v-model="privacySettings.readReceipt" />
                </div>
              </div>
              
              <div class="settings-item">
                <div class="item-left">
                  <span class="item-label">显示最后活动时间</span>
                  <span class="item-desc">是否显示最后活跃时间</span>
                </div>
                <div class="item-right">
                  <el-switch v-model="privacySettings.showLastActive" />
                </div>
              </div>
            </div>
          </div>
          
          <div class="settings-card">
            <div class="card-header">
              <h3>数据管理</h3>
            </div>
            <div class="card-body">
              <div class="settings-item clickable" @click="clearCache">
                <div class="item-left">
                  <span class="item-label">清除缓存</span>
                  <span class="item-desc">释放存储空间</span>
                </div>
                <div class="item-right">
                  <span class="item-value">{{ cacheSize }}</span>
                  <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 关于我们 -->
        <div v-show="activeNav === 'about'" class="settings-section">
          <div class="section-header">
            <h2>关于我们</h2>
            <p>了解更多信息</p>
          </div>
          
          <div class="settings-card about-card">
            <div class="about-logo">
              <img src="@/assets/logo.png" alt="logo" />
              <h3>FenDou CHAT 开发团队</h3>
              <p class="version">版本 v1.0.0</p>
            </div>
            
            <div class="about-info">
              <div class="info-row">
                <span class="info-label">产品介绍</span>
                <span class="info-value">一款功能丰富的FenDou CHAT 聊天应用</span>
              </div>
              <div class="info-row">
                <span class="info-label">技术栈</span>
                <span class="info-value">Spring Boot + Vue3 + WebSocket</span>
              </div>
              <div class="info-row">
                <span class="info-label">联系我们</span>
                <span class="info-value">fendou520@example.com</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { useThemeStore } from '@/stores/themeStore.js'
import { useChangeColor } from '@/utils/theme'
import { useUserStore } from '@/stores/user'
import { useUserApi } from '@/api/user/index.js'
import {
  Setting,
  User,
  Bell,
  Lock,
  InfoFilled,
  Refresh,
  ArrowRight,
  QuestionFilled,
  ChatDotRound,
  Document
} from '@element-plus/icons-vue'

const themeStore = useThemeStore()
const { themeConfig } = storeToRefs(themeStore)
const changeColor = useChangeColor()
const userStore = useUserStore()
const userApi = useUserApi()

const activeNav = ref('theme')
const activeAccountTab = ref('profile')
const profileFormRef = ref()
const passwordFormRef = ref()
const cacheSize = ref('128 MB')

// 从URL参数中获取初始激活的标签页
onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search)
  const nav = urlParams.get('activeNav')
  const tab = urlParams.get('activeTab')
  
  if (nav) {
    activeNav.value = nav
  }
  if (tab) {
    activeAccountTab.value = tab
  }
})

// 表单相关
const userForm = ref({...userStore.user})
const passwordForm = reactive({
  oldPwd: '',
  newPwd: '',
  confirmPwd: ''
})

// 性别选项
const genderSelectOptions = [
  { text: '保密', value: 0, type: 'info' },
  { text: '男', value: 1, type: 'success' },
  { text: '女', value: 2, type: 'danger' }
]

// 文件上传配置
const fileUploadApi = import.meta.env.VITE_APP_API_URL + 'file/upload'
const fileHeaders = reactive({
  Authorization: 'Bearer ' + sessionStorage.getItem('token')
})

// 加载状态
const savingProfile = ref(false)
const savingPassword = ref(false)

// 表单验证规则
const profileRules = reactive({
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    {
      pattern: /^1[3456789]\d{9}$/,
      message: '手机号码格式错误',
      trigger: ['blur', 'change']
    }
  ]
})

const passwordRules = reactive({
  oldPwd: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value === passwordForm.oldPwd) {
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
        if (value !== passwordForm.newPwd) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

const predefineColors = [
  '#409EFF',
  '#67C23A',
  '#E6A23C',
  '#F56C6C',
  '#909399',
  '#ff4500', 
  '#ff8c00', 
  '#ffd700', 
  '#90ee90', 
  '#00ced1', 
  '#1e90ff', 
  '#c71585'
]

const themePresets = [
  { name: '商务蓝', value: '#409EFF', gradient: 'linear-gradient(135deg, #409EFF, #66b1ff)' },
  { name: '清新绿', value: '#67C23A', gradient: 'linear-gradient(135deg, #67C23A, #85ce61)' },
  { name: '活力橙', value: '#E6A23C', gradient: 'linear-gradient(135deg, #E6A23C, #f0c78a)' },
  { name: '热情红', value: '#F56C6C', gradient: 'linear-gradient(135deg, #F56C6C, #f89898)' },
  { name: '神秘紫', value: '#909399', gradient: 'linear-gradient(135deg, #909399, #b4b4b8)' },
  { name: '暗夜黑', value: '#141414', gradient: 'linear-gradient(135deg, #141414, #303133)' }
]

const navList = [
  { key: 'theme', name: '主题设置', icon: Setting },
  { key: 'account', name: '账号设置', icon: User },
  { key: 'notification', name: '通知设置', icon: Bell },
  { key: 'privacy', name: '隐私设置', icon: Lock },
  { key: 'about', name: '关于我们', icon: InfoFilled }
]

const notificationSettings = reactive({
  messageRemind: true,
  sound: true,
  desktop: true,
  doNotDisturb: false
})

const privacySettings = reactive({
  showOnlineStatus: true,
  readReceipt: true,
  showLastActive: false
})

const applyDarkMode = (isDark) => {
  if (isDark) {
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

const toggleDarkMode = () => {
  applyDarkMode(themeConfig.value.darkMode)
  window.dispatchEvent(new CustomEvent('dark-mode-change', { detail: themeConfig.value.darkMode }))
  setThemeConfig()
}

const changeThemeColor = () => {
  if (!themeConfig.value.primary) return ElMessage.warning('全局主题 primary 颜色值不能为空')
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
  setThemeConfig()
  window.dispatchEvent(new CustomEvent('theme-color-change', { detail: themeConfig.value.primary }))
}

const changeComponentSize = () => {
  setThemeConfig()
}

const setThemeConfig = () => {
  localStorage.removeItem('themeConfig')
  localStorage.setItem('themeConfig', JSON.stringify(themeConfig.value))
  localStorage.setItem('themeConfigStyle', document.documentElement.style.cssText)
}

const resetTheme = () => {
  ElMessageBox.confirm('确定要恢复默认设置吗？此操作将清除所有自定义配置。', '恢复默认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('themeConfig')
    localStorage.removeItem('themeConfigStyle')
    // 重置主题时应用默认配色
    themeConfig.value = {
      primary: '#3b82f6',
      darkMode: false,
      componentSize: 'default'
    }
    // 重新应用主题样式
    changeThemeColor()
    applyDarkMode(false)
    ElMessage.success('主题已恢复默认设置')
  })
}

const selectThemePreset = (color) => {
  themeConfig.value.primary = color
  changeThemeColor()
}

// 表单重置
const resetProfileForm = () => {
  profileFormRef.value?.resetFields()
}

const resetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
}

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
    userForm.value.avatar = res.data.filePath
    saveProfile()
  } else {
    ElMessage.error(res.message || '头像上传失败')
  }
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      savingProfile.value = true
      try {
        const res = await userApi.updateProfile(userForm.value)
        ElMessage.success(res.message)
        // 更新用户信息到store
        userStore.user = userForm.value
      } finally {
        savingProfile.value = false
      }
    }
  })
}

// 修改密码
const savePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      savingPassword.value = true
      try {
        const res = await userApi.updatePassword(passwordForm)
        ElMessage.success(res.message)
        resetPasswordForm()
      } finally {
        savingPassword.value = false
      }
    }
  })
}

const clearCache = () => {
  ElMessageBox.confirm('确定要清除缓存吗？此操作不会删除您的聊天记录。', '清除缓存', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cacheSize.value = '0 MB'
    ElMessage.success('缓存已清除')
  })
}

const exportData = () => {
  ElMessage.info('数据导出功能开发中...')
}

const openLink = (type) => {
  ElMessage.info(`正在打开${type}页面...`)
}

onMounted(() => {
  nextTick(() => {
    if (themeConfig.value.darkMode) {
      toggleDarkMode()
    }
  })
})
</script>

<style lang="scss" scoped>
.settings-container {
  height: calc(100vh - 60px);
  overflow: hidden;
}

.settings-layout {
  display: flex;
  height: 100%;
  background-color: #f5f5f5;
}

.settings-sidebar {
  width: 220px;
  background-color: #fff;
  border-right: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.settings-nav {
  padding: 16px 0;
}

.settings-nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.2s;
  color: #606266;
  font-size: 14px;
  
  .el-icon {
    font-size: 18px;
  }
  
  &:hover {
    background-color: #f5f7fa;
    color: #409EFF;
  }
  
  &.active {
    background-color: #ecf5ff;
    color: #409EFF;
    border-right: 3px solid #409EFF;
  }
}

.settings-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.settings-section {
  max-width: 800px;
  margin: 0 auto;
}

.section-header {
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }
  
  p {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.settings-card {
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  
  h3 {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
    margin: 0;
  }
}

.card-body {
  padding: 8px 0;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: #fafafa;
  }
  
  &.clickable {
    cursor: pointer;
  }
}

.item-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-label {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.item-desc {
  font-size: 12px;
  color: #909399;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.item-value {
  font-size: 14px;
  color: #909399;
}

.arrow-icon {
  color: #c0c4cc;
  font-size: 16px;
}

.avatar-item {
  gap: 16px;
}

.settings-actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.theme-presets {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  padding: 16px 20px;
}

.theme-preset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.2s;
  
  &:hover {
    transform: scale(1.05);
  }
}

.preset-color {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.preset-name {
  font-size: 12px;
  color: #606266;
}

/* 表单样式 */
.settings-form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.avatar-preview {
  cursor: pointer;
  transition: opacity 0.2s;
  
  &:hover {
    opacity: 0.8;
  }
}

.avatar-hint {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.password-form {
  max-width: 500px;
}

.empty-devices {
  padding: 40px 0;
  text-align: center;
}

.about-card {
  text-align: center;
  padding: 32px 0;
}

.about-logo {
  margin-bottom: 24px;
  
  img {
    width: 80px;
    height: 80px;
    margin-bottom: 16px;
  }
  
  h3 {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }
  
  .version {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.about-info {
  max-width: 400px;
  margin: 0 auto;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
  
  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 14px;
  color: #606266;
}

.info-value {
  font-size: 14px;
  color: #303133;
}

.link-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 16px 20px;
}

.link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
  
  .el-icon {
    font-size: 24px;
    color: #409EFF;
  }
  
  span {
    font-size: 12px;
    color: #606266;
  }
  
  &:hover {
    background-color: #f5f7fa;
  }
}
</style>
