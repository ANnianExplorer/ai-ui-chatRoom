<template>
  <!-- 群成员侧边栏 -->
  <div class="qq-member-sidebar" v-if="showSidebar && chat && chat.type === 2">
    <!-- 群公告 -->
    <div class="group-notice">
      <div class="notice-header">
        <h4>群公告</h4>
      </div>
      <div class="notice-content">
        <p class="notice-text">{{ groupAnnouncement || '暂无公告' }}</p>
      </div>
    </div>
    <!-- 群成员列表 -->
    <div class="group-member-panel">
      <div class="member-header">
        <div class="header-top">
          <h4>群聊成员 {{ filteredMembers.length }}</h4>
        </div>
        <div class="online-info">
          <el-icon><User /></el-icon>
          <span>在线人数: {{ onlineCount }}</span>
        </div>
        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索群成员"
            size="small"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
      <div class="member-list" v-loading="loading">
        <!-- 群成员列表内容 -->
        <el-empty v-if="filteredMembers.length === 0 && !loading" description="暂无成员" />
        <div class="member-item" v-for="member in filteredMembers" :key="member.id || member.userId || Math.random()">
          <div class="member-content" @click="handleMemberClick(member)">
            <div class="member-avatar">
              <el-avatar size="36" :src="member.avatar || member.userAvatar || ''">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="online-dot" v-if="member.status === 0 || member.online === true"></span>
            </div>
            <div class="member-info">
              <div class="member-name">
                {{ member.realName || member.username || member.nickname || member.name || '未知用户' }}
                <span class="crown-icon" v-if="member.isOwner">👑</span>
              </div>
              <div class="member-role" v-if="member.role === 0 || member.isOwner">群主</div>
              <div class="member-role" v-else-if="member.role === 1">管理员</div>
            </div>
          </div>
          <!-- 移出成员按钮，只有群主才能看到，且不能移出自己 -->
          <el-button 
            type="danger" 
            size="small" 
            plain
            v-if="groupInfo.createBy === userStore.user.id && !(member.isOwner || (member.id || member.userId) === userStore.user.id)"
            @click.stop="handleRemoveMember(member)"
          >
            移出
          </el-button>
        </div>
      </div>
    </div>
  </div>

  <!-- 用户详情弹窗组件 -->
  <UserDetailDialog
    :visible="userDetailDialogVisible"
    :member="selectedMember"
    @close="userDetailDialogVisible = false"
    @send-message="handleSendMessage"
    @add-friend="handleAddFriend"
  />
</template>

<script setup>
import { ref, watch, onMounted, inject, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Search } from '@element-plus/icons-vue'
import { useGroupApi } from '@/api/group/index.js'
import { useUserApi } from '@/api/user/index.js'
import { useUserStore } from '@/stores/user.js'
import { useWebsocketStore } from '@/stores/websocket.js'
import { useRouter } from 'vue-router'
import UserDetailDialog from './UserDetailDialog.vue'

// 定义props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  chat: {
    type: Object,
    default: () => {}
  }
})

// 定义emits
const emit = defineEmits(['update:show'])

// API和store实例
const groupApi = useGroupApi()
const userApi = useUserApi()
const userStore = useUserStore()
const websocket = useWebsocketStore()
const router = useRouter()

// 不再需要从父组件注入，直接使用websocket store

// 响应式数据
const showSidebar = ref(props.show)
const loading = ref(false)
const groupMembers = ref([])
const onlineCount = ref(0)
const groupAnnouncement = ref('') // 群公告内容
const groupInfo = ref({}) // 群信息，包含群主ID
const searchKeyword = ref('') // 搜索关键词

// 用户详情弹窗相关
const userDetailDialogVisible = ref(false)
const selectedMember = ref(null)

// 监听props.show变化
watch(
  () => props.show,
  (newVal) => {
    showSidebar.value = newVal
    if (newVal && props.chat && props.chat.type === 2) {
      getGroupMembers(props.chat.chatId)
    }
  },
  { immediate: true }
)

// 监听chat变化
watch(
  () => props.chat,
  (newChat) => {
    if (newChat && newChat.type === 2 && showSidebar.value) {
      getGroupMembers(newChat.chatId)
    }
  }
)

// 生成用户chatId
const formatUserId = (userId) => {
  if (userStore.user.id > userId) {
    return 'user-' + userId + '-' + userStore.user.id
  } else {
    return 'user-' + userStore.user.id + '-' + userId
  }
}

// 过滤后的群成员列表
const filteredMembers = computed(() => {
  if (!searchKeyword.value) {
    return groupMembers.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return groupMembers.value.filter(member => {
    const name = member.realName || member.username || member.nickname || member.name || ''
    return name.toLowerCase().includes(keyword)
  })
})

// 获取群信息
const getGroupInfo = async (groupId) => {
  try {
    // 获取群信息
    const groupRes = await groupApi.getGroupById(groupId)
    if (groupRes.data) {
      groupInfo.value = groupRes.data
    }
    //console.log('++++群信息:', groupId)
    const id = parseInt(groupId)
    // 获取群公告
    const announcementRes = await groupApi.getGroupAnnouncement(id)
    //console.log('++++群公告:', announcementRes)
    // 即使返回null，也要赋值，确保模板中能正确显示"暂无公告"
    groupAnnouncement.value = announcementRes.data || ''
    //console.log('++++群公告:', groupAnnouncement.value)
  } catch (error) {
    console.error('获取群信息或公告失败:', error)
  }
}

// 获取群成员列表
const getGroupMembers = async (chatId) => {
  loading.value = true
  try {
    // 从chatId中提取groupId，处理群聊的chatId格式：group-{groupId}
    let groupId = chatId
    if (typeof chatId === 'string' && chatId.startsWith('group-')) {
      groupId = chatId.substring(6)
    }
    
    // 确保groupId有效
    if (!groupId || groupId === 'undefined' || groupId === undefined || isNaN(groupId)) {
      console.error('无效的群聊ID:', chatId)
      ElMessage.error('无法获取群成员，群聊ID无效')
      loading.value = false
      return
    }
    
    // 获取群信息和群公告
    await getGroupInfo(groupId)
    
    const res = await groupApi.getGroupMembers(groupId)
    console.log('获取群成员列表完整响应:', res)
    
    // 检查返回的数据类型
    const memberIds = res.data || []
    console.log('群成员ID列表:', memberIds)
    
    // 如果返回的是数字ID数组，需要调用API获取完整用户信息
    if (Array.isArray(memberIds) && memberIds.length > 0 && typeof memberIds[0] === 'number') {
      console.log('群成员是数字ID数组，开始获取完整用户信息...')
      
      // 调用API获取每个用户的完整信息
      const fullMembers = []
      for (const userId of memberIds) {
        try {
          const userRes = await userApi.getUserById(userId)
          if (userRes.data) {
            // 添加isOwner字段，标记是否为群主
            const member = {
              id: userId,
              ...userRes.data,
              isOwner: userId === groupInfo.value.createBy // 群主标记
            }
            fullMembers.push(member)
          }
        } catch (error) {
          console.error(`获取用户${userId}信息失败`, error)
          // 即使获取失败，也添加一个基本的成员对象
          const member = {
            id: userId,
            username: `用户${userId}`,
            realName: `用户${userId}`,
            isOwner: userId === groupInfo.value.createBy // 群主标记
          }
          fullMembers.push(member)
        }
      }
      
      // 对群成员进行排序，群主在第一位
      fullMembers.sort((a, b) => {
        if (a.isOwner && !b.isOwner) return -1
        if (!a.isOwner && b.isOwner) return 1
        return 0
      })
      
      groupMembers.value = fullMembers
      console.log('获取到的完整群成员列表:', fullMembers)
    } else {
      // 如果返回的已经是完整的成员对象数组，添加isOwner字段并排序
      const fullMembers = memberIds.map(member => ({
        ...member,
        isOwner: (member.id || member.userId) === groupInfo.value.createBy // 群主标记
      }))
      
      // 对群成员进行排序，群主在第一位
      fullMembers.sort((a, b) => {
        if (a.isOwner && !b.isOwner) return -1
        if (!a.isOwner && b.isOwner) return 1
        return 0
      })
      
      groupMembers.value = fullMembers
    }
    
    // 从websocket store获取在线人数
    const wsOnlineCount = websocket.getOnlineCount(props.chat.chatId)
    console.log('从websocket store获取的在线人数:', wsOnlineCount)
    
    // 计算基于成员status的在线人数
    const statusOnlineCount = groupMembers.value.filter(member => member.status === 0).length
    console.log('根据成员status计算的在线人数:', statusOnlineCount)
    
    // 优先使用websocket store的数据，确保实时性
    // 确保onlineCount是数字类型
    onlineCount.value = Math.max(parseInt(wsOnlineCount) || 0, statusOnlineCount)
    
    // 如果有当前用户，至少显示1人在线
    if (onlineCount.value === 0) {
      onlineCount.value = 1
    }
    
    console.log('最终在线人数:', onlineCount.value)
    
    // 打印群成员数据结构
    console.log('群成员数据结构:', groupMembers.value[0] || '空列表')
    console.log('群成员数量:', groupMembers.value.length)
    console.log('在线人数:', onlineCount.value)
  } catch (error) {
    console.error('获取群成员列表失败', error)
    ElMessage.error('获取群成员列表失败')
  } finally {
    loading.value = false
  }
}

// 点击成员
const handleMemberClick = (member) => {
  console.log('群成员点击事件，member对象:', member)
  
  // 设置选中的成员
  selectedMember.value = member
  
  // 打开弹窗
  userDetailDialogVisible.value = true
}

// 处理发送消息事件
const handleSendMessage = (member) => {
  if (!member) {
    ElMessage.error('无法发送消息，未选择用户')
    return
  }
  
  // 获取用户ID
  let userId = member.id || member.userId
  
  // 生成chatId
  const currentUserId = userStore.user.id
  const smallerId = Math.min(currentUserId, userId)
  const largerId = Math.max(currentUserId, userId)
  const chatId = `user-${smallerId}-${largerId}`
  
  console.log('生成的chatId:', chatId)
  
  // 跳转到聊天页面
  router.push(`/index?chatId=${chatId}`)
}

// 处理添加好友事件
const handleAddFriend = (member) => {
  if (!member) {
    ElMessage.error('无法添加好友，未选择用户')
    return
  }
  
  ElMessage.info('添加好友功能开发中...')
}

// 处理移出成员事件
const handleRemoveMember = async (member) => {
  if (!member) {
    ElMessage.error('无法移出成员，未选择用户')
    return
  }
  
  try {
    // 从chatId中提取groupId，处理群聊的chatId格式：group-{groupId}
    let groupId = props.chat.chatId
    if (typeof groupId === 'string' && groupId.startsWith('group-')) {
      groupId = groupId.substring(6)
    }
    
    // 确保groupId有效
    if (!groupId || groupId === 'undefined' || groupId === undefined || isNaN(groupId)) {
      console.error('无效的群聊ID:', props.chat.chatId)
      ElMessage.error('无法移出成员，群聊ID无效')
      return
    }
    
    // 获取要移除成员的ID
    const memberId = member.id || member.userId
    
    // 调用后端接口移出成员
    await groupApi.removeGroup({
      groupId: parseInt(groupId),
      userId: memberId
    })
    
    ElMessage.success('成员已成功移出')
    
    // 刷新群成员列表
    await getGroupMembers(props.chat.chatId)
  } catch (error) {
    console.error('移出成员失败', error)
    ElMessage.error('移出成员失败，请重试')
  }
}

// 提供给父组件的方法
defineExpose({
  refresh: () => {
    if (props.chat && props.chat.type === 2) {
      getGroupMembers(props.chat.chatId)
    }
  }
})
</script>

<style scoped>
.qq-member-sidebar {
  width: 280px;
  background-color: #f5f5f5;
  border-left: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

.group-notice {
  padding: 16px;
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
}

.notice-header h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #333;
}

.notice-title {
  font-weight: bold;
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #409eff;
}

.notice-text {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.group-member-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.member-header {
  padding: 12px 16px;
  background-color: #fff;
  border-bottom: 1px solid #e0e0e0;
}

.search-box {
  margin-top: 12px;
}

.search-box :deep(.el-input__wrapper) {
  border-radius: 20px;
  border-color: #e0e0e0;
  transition: all 0.3s ease;
}

.search-box :deep(.el-input__wrapper):hover {
  border-color: #409eff;
}

.search-box :deep(.el-input__wrapper).is-focus {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  border-color: #409eff;
}

.header-top h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #333;
}

.online-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #67c23a;
}

.member-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  justify-content: space-between;
}

.member-item:hover {
  background-color: #e8e8e8;
}

.member-content {
  display: flex;
  align-items: center;
  flex: 1;
  cursor: pointer;
}

.member-avatar {
  position: relative;
  margin-right: 12px;
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  background-color: #67c23a;
  border-radius: 50%;
  border: 2px solid #fff;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  gap: 4px;
}

.crown-icon {
  font-size: 14px;
  color: #fadb14;
  margin-left: 4px;
  vertical-align: middle;
}

.member-role {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

/* 隐藏滚动条但保留滚动功能 */
.member-list::-webkit-scrollbar {
  width: 0;
  height: 0;
  opacity: 0;
}

.member-list {
  scrollbar-width: none;
  -ms-overflow-style: none;
}
</style>
