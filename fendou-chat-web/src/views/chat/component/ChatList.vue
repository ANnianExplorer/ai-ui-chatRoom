<template>
  <div class="chat-list-container">
    <ul class="chat-list">
      <el-empty v-if="filterChatList.length === 0" />
      <li
        v-for="item in filterChatList"
        :key="item.id"
        :class="{ active: state.activeChatId === item.chatId }"
        @click="selectChat(item.chatId)"
      >
        <div class="chat-item">
          <div class="avatar-container">
            <img :src="item.avatar" alt="" class="avatar" />
            <span v-if="item.type === 1" class="online-status" :class="isOnline(item.chatId, item.status) ? 'online' : ''"></span>
          </div>
          <div class="chat-details">
            <p class="chat-name">{{ item.remark || item.name }}</p>
            <p class="status-text" :class="isOnline(item.chatId, item.status) ? 'online' : ''" v-if="item.type === 1">
              {{ isOnline(item.chatId, item.status) ? '在线' : '离线' }}
            </p>
            <p class="status-text" v-if="item.type === 2">{{ getOnlineCount(item.chatId) }} 人在线</p>
          </div>
          <div class="badge-container">
            <el-badge :value="item.unreadCount" :max="99" class="badge" v-if="item.unreadCount > 0" />
          </div>
        </div>
      </li>
    </ul>
  </div>
  <AddFriendDialog ref="addFriendDialogRef" />
  <AddGroupDialog ref="addGroupDialogRef" />
</template>

<style scoped>
/* 在线状态样式 */
.online-status {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #999;
  border: 2px solid #fff;
}

.online-status.online {
  background-color: #13ce66;
}

.status-text.online {
  color: #13ce66;
}
</style>

<style scoped>
/* 聊天列表容器 */
.chat-list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 聊天列表 */
.chat-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  list-style: none;
  padding: 0;
  margin: 0;
}

/* 隐藏滚动条但保留滚动功能 */
.chat-list::-webkit-scrollbar {
  width: 0;
  height: 0;
  opacity: 0;
}

.chat-list {
  scrollbar-width: none;
  -ms-overflow-style: none;
}

/* 聊天项布局 */
.chat-item {
  display: flex;
  align-items: center;
  padding: 10px;
  gap: 10px;
}

/* 聊天详情区域 */
.chat-details {
  flex: 1;
}

/* 徽章容器 */
.badge-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 新消息徽章样式 */
:deep(.el-badge) {
  /* 移除Element Plus默认的额外样式 */
}

:deep(.el-badge__content) {
  /* 重置所有样式 */
  all: unset;
  /* 设置基本样式 */
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  /* 完美圆形 */
  border-radius: 50%;
  width: 20px;
  height: 20px;
  /* 居中显示 */
  display: inline-flex;
  align-items: center;
  justify-content: center;
  /* 确保没有多余边框 */
  border: none;
  outline: none;
  /* 确保不被其他样式影响 */
  box-shadow: none;
  overflow: hidden;
}

/* 确保没有固定定位影响 */
:deep(.el-badge__content.is-fixed) {
  position: static;
}
</style>

<script setup>
import { ref, computed, reactive, onMounted, watch, defineAsyncComponent, inject } from 'vue'
import { useUserStore } from '@/stores/user'
import { useChatApi } from '@/api/chat/index.js'
import { useWebsocketStore } from '@/stores/websocket.js'
import { useMessageApi } from '@/api/message/index.js'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

const AddFriendDialog = defineAsyncComponent(() => import('@/views/contact/component/AddFriend.vue'))
const addFriendDialogRef = ref(null)
const AddGroupDialog = defineAsyncComponent(() => import('@/views/contact/component/AddGroup.vue'))
const addGroupDialogRef = ref(null)

// 向父组件传值
const emit = defineEmits(['getChat'])

const route = useRoute()
const router = useRouter()

const messageApi = useMessageApi()
const userStore = useUserStore()
const websocket = useWebsocketStore()
const chatApi = useChatApi()

// 接收父组件传递的搜索关键词
const props = defineProps({
  searchKeyword: {
    type: String,
    default: ''
  }
})

const state = reactive({
  activeCollapse: ['friends', 'groups'],
  friendList: [],
  groupList: [],
  activeChatId: '',
  chatList: []
})

const getChats = async () => {
  const res = await chatApi.getChats({ pIndex: 0, pSize: -1 }, { userId: userStore.user.id })
  state.chatList = res.data.records
  console.log('聊天列表数据:', res.data.records)
  
  // 打印每个聊天项的status字段
  state.chatList.forEach(item => {
    console.log(`Chat item: ${item.name}, chatId: ${item.chatId}, type: ${item.type}, status: ${item.status}, remark: ${item.remark}`)
  })
  
  // 在聊天列表加载完成后处理选中逻辑
  const chatId = route.query.chatId
  if (chatId) {
    // 如果URL中有chatId，选中对应的聊天对象
    selectChat(chatId)
  } else if (state.chatList.length > 0) {
    // 如果没有URL参数且聊天列表不为空，默认选中第一个聊天对象（通常是机器人）
    selectChat(state.chatList[0].chatId)
  }
}

// 选择聊天项
const selectChat = (chatId) => {
  state.activeChatId = chatId

  const chat = state.chatList.find((i) => i.chatId === chatId)
  if (chat) {
    emit('getChat', chat)

    if (chat.unreadCount > 0) {
      messageApi.clearUnreadMessage(chat.chatId)
      chat.unreadCount = 0
    }
    
    // 更新URL参数，保存当前选中的chatId
    router.push({ query: { chatId } })
  } else {
    console.error('未找到对应chatId的聊天对象:', chatId)
  }
}

// 添加好友
const handleAddUser = () => {
  addFriendDialogRef.value.openDialog()
}

// 添加群聊
const handleAddGroup = () => {
  addGroupDialogRef.value.openDialog()
}

// 监听搜索
const filterChatList = computed(() => {
  return state.chatList.filter((item) => 
    (item.name && item.name.includes(props.searchKeyword)) || 
    (item.remark && item.remark.includes(props.searchKeyword))
  )
})

// 监听消息
watch(
  () => websocket.message,
  (newVal) => {
    handleMessage(newVal)
  }
)

// 消息处理
const handleMessage = (message) => {
  console.log('Received message:', message)
  
  if (message.type === 'INIT_USER_STATUS') {
    console.log('Initializing user status:', message.friends)
    websocket.handleInitUserStatus(message)
  } else if (message.type === 'GROUP_ONLINE_COUNT') {
    console.log('Updating group online count:', message)
    websocket.handleGroupOnlineCount(message)
  } else if (message.type === 'USER_STATUS') {
    console.log('Updating user status:', message.userId, message.status)
    websocket.handleUserStatus(message)
  } else if (message.type === 'ERROR') {
    ElMessage.error(message.content)
  } else {
    if (message.chatId !== state.activeChatId) {
      // 设置对应聊天消息未读数
      const item = state.chatList.find((item) => item.chatId === message.chatId)
      if (item) {
        item.unreadCount += 1
      }
    }
  }
}

// 判断用户是否在线
const isOnline = (chatId, userStatus) => {
  return websocket.isOnline(chatId, userStatus)
}

// 获取群聊在线人数
const getOnlineCount = (chatId) => {
  return websocket.getOnlineCount(chatId)
}

onMounted(async () => {
  await getChats()
})
</script>
