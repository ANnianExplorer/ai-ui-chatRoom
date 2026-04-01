<template>
  <div>
  <div class="chat-list-container">
    <ul class="chat-list">
      <el-empty v-if="filterChatList.length === 0" description="暂无会话" />
      <li
        v-for="item in filterChatList"
        :key="item.id"
        :class="{ active: state.activeChatId === item.chatId }"
        @click="selectChat(item.chatId)"
      >
        <div class="chat-item">
          <div class="avatar-container">
            <img :src="item.avatar" alt="" class="avatar" />
            <span
              class="online-status"
              :class="{
                online: item.type === 1 && isOnline(item.chatId, item.status),
                group: item.type === 2
              }"
            ></span>
          </div>
          <div class="chat-details">
            <div class="chat-header-row">
              <span class="chat-name">{{ item.remark || item.name }}</span>
              <span class="chat-time" v-if="item.lastTime">{{ item.lastTime }}</span>
            </div>
            <div class="chat-preview-row">
              <span class="chat-preview">
                <template v-if="item.type === 1">
                  <span :class="isOnline(item.chatId, item.status) ? 'online-text' : 'offline-text'">
                    {{ isOnline(item.chatId, item.status) ? '● 在线' : '○ 离线' }}
                  </span>
                </template>
                <template v-else-if="item.type === 2">
                  <span class="group-count">{{ getOnlineCount(item.chatId) }} 人在线</span>
                </template>
                <template v-else>
                  <span class="ai-text">🤖 AI助手</span>
                </template>
              </span>
              <transition name="badge-bounce">
                <el-badge :value="item.unreadCount" :max="99" v-if="item.unreadCount > 0" class="unread-badge" />
              </transition>
            </div>
          </div>
        </div>
      </li>
    </ul>
  </div>
  </div>
  <AddFriendDialog ref="addFriendDialogRef" />
  <AddGroupDialog ref="addGroupDialogRef" />
  </div>
</template>

<style scoped lang="scss">
@keyframes badge-bounce-enter {
  from { transform: scale(0); opacity: 0; }
  to   { transform: scale(1); opacity: 1; }
}

.chat-list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  list-style: none;
  padding: 4px 0;
  margin: 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(124,58,237,0.15) transparent;
  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-thumb { background: rgba(124,58,237,0.15); border-radius: 2px; }
}

.chat-list li {
  cursor: pointer;
  border-radius: 10px;
  margin: 2px 8px;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.chat-list li::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  border-radius: 0 2px 2px 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.chat-list li:hover {
  background: rgba(124, 58, 237, 0.06);
}

.chat-list li.active {
  background: linear-gradient(135deg, rgba(124,58,237,0.1), rgba(168,85,247,0.08));
}

.chat-list li.active::before {
  opacity: 1;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  gap: 10px;
}

.avatar-container {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255,255,255,0.8);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.online-status {
  position: absolute;
  bottom: 1px;
  right: 1px;
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: #9ca3af;
  border: 2px solid white;
  transition: background 0.3s;
}

.online-status.online {
  background: #10b981;
  box-shadow: 0 0 0 2px rgba(16,185,129,0.3);
  animation: pulse-green 2s infinite;
}

.online-status.group {
  background: #8b5cf6;
}

@keyframes pulse-green {
  0%, 100% { box-shadow: 0 0 0 2px rgba(16,185,129,0.3); }
  50%       { box-shadow: 0 0 0 4px rgba(16,185,129,0.1); }
}

.chat-details {
  flex: 1;
  min-width: 0;
}

.chat-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 3px;
}

.chat-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e1b4b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.active .chat-name {
  color: #7c3aed;
}

.chat-time {
  font-size: 11px;
  color: #9ca3af;
  flex-shrink: 0;
  margin-left: 8px;
}

.chat-preview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-preview {
  font-size: 12px;
  flex: 1;
  min-width: 0;
}

.online-text { color: #10b981; font-weight: 500; }
.offline-text { color: #9ca3af; }
.group-count { color: #8b5cf6; font-weight: 500; }
.ai-text { color: #7c3aed; }

.unread-badge {
  flex-shrink: 0;
  margin-left: 6px;
}

:deep(.unread-badge .el-badge__content) {
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  border: none;
  font-size: 11px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  padding: 0 4px;
  border-radius: 9px;
  box-shadow: 0 2px 6px rgba(124,58,237,0.4);
  animation: badge-bounce-enter 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.badge-bounce-enter-active { animation: badge-bounce-enter 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.badge-bounce-leave-active { transition: all 0.2s; }
.badge-bounce-leave-to { transform: scale(0); opacity: 0; }
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
