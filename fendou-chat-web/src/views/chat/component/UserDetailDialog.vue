<template>
  <el-dialog
    v-model="dialogVisible"
    title="用户详情"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="detail-content" v-loading="loading">
      <div class="avatar-section">
        <el-avatar :size="80" :src="userData.avatar">
          <el-icon><User /></el-icon>
        </el-avatar>
      </div>
      
      <div class="info-table">
        <div class="info-row">
          <div class="info-label">用户昵称</div>
          <div class="info-value">{{ userData?.realName || userData?.username || '未知用户' }}</div>
        </div>
        
        <div class="info-row">
          <div class="info-label">用户性别</div>
          <div class="info-value">
            <el-tag v-if="userData?.gender === 0" type="info">未知</el-tag>
            <el-tag v-else-if="userData?.gender === 1" type="success">男</el-tag>
            <el-tag v-else-if="userData?.gender === 2" type="danger">女</el-tag>
            <span v-else>未知</span>
          </div>
        </div>
        
        <div class="info-row">
          <div class="info-label">出生日期</div>
          <div class="info-value">{{ userData?.birthday || '' }}</div>
        </div>
        
        <div class="info-row">
          <div class="info-label">电子邮箱</div>
          <div class="info-value">{{ userData?.email || '' }}</div>
        </div>
        
        <div class="info-row">
          <div class="info-label">电话号码</div>
          <div class="info-value">{{ userData?.phone || '' }}</div>
        </div>
        
        <div class="info-row">
          <div class="info-label">所在地区</div>
          <div class="info-value">{{ userData?.address || '' }}</div>
        </div>
        
        <div class="info-row">
          <div class="info-label">个人介绍</div>
          <div class="info-value">{{ userData?.description || '这个人很懒什么都没留下。' }}</div>
        </div>
        
        <div class="info-row">
          <div class="info-label">加入时间</div>
          <div class="info-value">{{ userData?.createTime || '' }}</div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          type="primary"
          @click="handleSendMessage"
          v-if="isFriend"
        >
          发消息
        </el-button>
        <el-button
          type="success"
          @click="handleAddFriend"
          v-else
        >
          添加好友
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import { useUserApi } from '@/api/user/index.js'
import { useFriendApi } from '@/api/friend/index.js'
import { useUserStore } from '@/stores/user.js'

// 定义props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  member: {
    type: Object,
    default: () => {}
  }
})

// 定义emits
const emit = defineEmits(['close', 'send-message', 'add-friend'])

// API实例
const userApi = useUserApi()
const friendApi = useFriendApi()
const userStore = useUserStore()

// 响应式数据
const dialogVisible = ref(props.visible)
const loading = ref(false)
const userData = ref({})
const isFriend = ref(false)

// 监听props.visible变化
watch(
  () => props.visible,
  (newVal) => {
    dialogVisible.value = newVal
    if (newVal && props.member) {
      fetchUserData(props.member)
      checkIfFriend(props.member)
    }
  },
  { immediate: true }
)

// 监听dialogVisible变化，向父组件传递事件
watch(
  () => dialogVisible.value,
  (newVal) => {
    if (!newVal) {
      emit('close')
    }
  }
)

// 获取用户详情数据
const fetchUserData = async (member) => {
  loading.value = true
  try {
    console.log('开始获取用户详情，member对象:', member)
    
    // 检查member是否有效
    if (!member) {
      console.error('无效的成员对象:', member)
      ElMessage.error('无法获取用户信息，成员对象无效')
      loading.value = false
      return
    }
    
    // 详细打印member对象的所有属性
    console.log('member对象的所有属性:', Object.keys(member))
    console.log('member.userId:', member.userId)
    console.log('member.id:', member.id)
    
    // 获取用户ID，尝试多种可能的字段名
    let userId = member.userId || member.id || member.user_id || member.UserId || member.ID
    
    // 打印获取到的userId
    console.log('从member中提取到的userId:', userId)
    
    // 确保userId有效
    if (!userId || userId === 'undefined' || userId === undefined) {
      console.error('无效的用户ID:', userId)
      ElMessage.error('无法获取用户信息，用户ID无效')
      loading.value = false
      return
    }
    
    // 确保userId是数字，转换为字符串再检查
    const userIdStr = String(userId)
    if (isNaN(Number(userIdStr))) {
      console.error('无效的用户ID格式，必须是数字:', userIdStr)
      ElMessage.error('无法获取用户信息，用户ID格式无效')
      loading.value = false
      return
    }
    
    // 转换为数字ID
    const numericUserId = Number(userIdStr)
    console.log('转换后的数字userId:', numericUserId)
    
    const res = await userApi.getUserById(numericUserId)
    userData.value = res.data
    console.log('获取用户详情成功', res.data)
  } catch (error) {
    console.error('获取用户详情失败', error)
    console.error('错误详情:', error.response || error.message || error)
    ElMessage.error('获取用户详情失败')
  } finally {
    loading.value = false
  }
}

// 检查是否为好友
const checkIfFriend = async (member) => {
  if (!member || !userStore.user?.id) {
    isFriend.value = false
    return
  }
  
  try {
    // 获取用户ID，优先使用member.userId，如果没有则使用member.id
    let userId = member.userId || member.id
    
    // 调用API检查是否为好友
    // 这里需要根据实际API调整，假设使用isFriend API
    // 实际项目中可能需要先获取好友列表，然后检查当前用户是否在列表中
    // 为了演示，这里简化处理
    isFriend.value = false
    console.log('检查是否为好友，userId:', userId)
    
    // 实际项目中应该调用API检查
    // const res = await friendApi.isFriend(userId)
    // isFriend.value = res.data
  } catch (error) {
    console.error('检查好友关系失败', error)
    isFriend.value = false
  }
}

// 关闭弹窗
const handleClose = () => {
  dialogVisible.value = false
}

// 发送消息
const handleSendMessage = () => {
  if (!props.member) {
    ElMessage.error('无法发送消息，未选择用户')
    return
  }
  
  emit('send-message', props.member)
  dialogVisible.value = false
}

// 添加好友
const handleAddFriend = () => {
  if (!props.member) {
    ElMessage.error('无法添加好友，未选择用户')
    return
  }
  
  emit('add-friend', props.member)
  dialogVisible.value = false
}
</script>

<style scoped>
.detail-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar-section {
  margin-bottom: 20px;
}

.info-table {
  width: 100%;
  max-width: 500px;
}

.info-row {
  display: flex;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.info-label {
  width: 120px;
  font-weight: 500;
  color: #606266;
  text-align: right;
  padding-right: 20px;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #303133;
  word-break: break-all;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>