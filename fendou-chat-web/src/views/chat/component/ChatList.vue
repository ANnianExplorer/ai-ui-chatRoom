<template>
  <div class="cl-root">
    <div class="chat-list-container">
      <ul class="chat-list">
        <el-empty
          v-if="filterChatList.length === 0"
          description="暂无会话"
          class="chat-empty"
        />
        <transition-group name="chat-item-anim" tag="div">
          <li
            v-for="item in filterChatList"
            :key="item.chatId"
            :class="{ active: state.activeChatId === item.chatId }"
            @click="selectChat(item.chatId)"
          >
            <div class="chat-item">
              <!-- 头像 -->
              <div class="avatar-container">
                <img :src="item.avatar" alt="" class="avatar" />
                <span
                  class="status-dot"
                  :class="{
                    self:   item.type === 0,
                    online: item.type === 1 && isOnline(item.chatId, item.status),
                    group:  item.type === 2,
                    ai:     item.type === 3
                  }"
                ></span>
              </div>

              <!-- 详情 -->
              <div class="chat-details">
                <div class="chat-header-row">
                  <span class="chat-name">{{ item.remark || item.name }}</span>
                  <span class="chat-time" v-if="item.lastTime">{{ item.lastTime }}</span>
                </div>
                <div class="chat-preview-row">
                  <span class="chat-preview">
                    <template v-if="item.type === 0">
                      <span class="tag-self">自己</span>
                    </template>
                    <template v-else-if="item.type === 1">
                      <span :class="isOnline(item.chatId, item.status) ? 'tag-online' : 'tag-offline'">
                        {{ isOnline(item.chatId, item.status) ? '在线' : '离线' }}
                      </span>
                    </template>
                    <template v-else-if="item.type === 2">
                      <span class="tag-group">{{ getOnlineCount(item.chatId) }} 人在线</span>
                    </template>
                    <template v-else>
                      <span class="tag-ai">AI 助手</span>
                    </template>
                  </span>
                  <transition name="badge-bounce">
                    <span v-if="item.unreadCount > 0" class="unread-dot">
                      {{ item.unreadCount > 99 ? '99+' : item.unreadCount }}
                    </span>
                  </transition>
                </div>
              </div>
            </div>
          </li>
        </transition-group>
      </ul>
    </div>
    <AddFriendDialog ref="addFriendDialogRef" />
    <AddGroupDialog ref="addGroupDialogRef" />
  </div>
</template>

<style scoped lang="scss">
.cl-root {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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
  padding: 6px 0;
  margin: 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(124,58,237,0.2) transparent;
  &::-webkit-scrollbar { width: 3px; }
  &::-webkit-scrollbar-thumb {
    background: rgba(124,58,237,0.2);
    border-radius: 999px;
    &:hover { background: rgba(124,58,237,0.4); }
  }
}

.chat-empty {
  margin-top: 40px;
  :deep(.el-empty__description p) {
    color: var(--text-muted) !important;
  }
}

/* 列表项 */
.chat-list li {
  cursor: pointer;
  border-radius: 12px;
  margin: 2px 8px;
  transition: all 0.22s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: var(--chat-item-hover);
    opacity: 0;
    border-radius: 12px;
    transition: opacity 0.2s ease;
  }

  &::after {
    content: '';
    position: absolute;
    left: 0; top: 20%; bottom: 20%;
    width: 3px;
    background: var(--brand-gradient-2);
    border-radius: 0 3px 3px 0;
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  &:hover {
    transform: translateX(2px);
    &::before { opacity: 1; }
  }

  &.active {
    background: var(--chat-item-active) !important;
    &::before { opacity: 0; }
    &::after  { opacity: 1; }

    .chat-name { color: var(--color-primary); }
  }
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  gap: 10px;
  position: relative;
  z-index: 1;
}

/* 头像 */
.avatar-container {
  position: relative;
  flex-shrink: 0;
}

.avatar {
  width: 44px; height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-default);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.2s ease;
}

.chat-list li:hover .avatar {
  transform: scale(1.04);
}

.status-dot {
  position: absolute;
  bottom: 1px; right: 1px;
  width: 11px; height: 11px;
  border-radius: 50%;
  background: var(--color-offline);
  border: 2px solid var(--bg-base);
  transition: background 0.3s;

  &.self {
    background: #f59e0b;
  }
  &.online {
    background: var(--color-online);
    animation: pulse-online 2.5s infinite;
  }
  &.group {
    background: var(--color-primary-light);
  }
  &.ai {
    background: var(--color-accent);
  }
}

@keyframes pulse-online {
  0%, 100% { box-shadow: 0 0 0 0 rgba(16,185,129,0.5); }
  50%       { box-shadow: 0 0 0 4px rgba(16,185,129,0); }
}

/* 详情区 */
.chat-details {
  flex: 1;
  min-width: 0;
}

.chat-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.chat-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  transition: color 0.2s;
}

.chat-time {
  font-size: 10px;
  color: var(--text-muted);
  flex-shrink: 0;
  margin-left: 8px;
}

.chat-preview-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}

.chat-preview {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

/* 标签 */
.tag-self {
  font-size: 11px;
  font-weight: 500;
  color: #d97706;
  background: rgba(245,158,11,0.12);
  padding: 1px 6px;
  border-radius: 999px;
}
.tag-online {
  font-size: 11px;
  font-weight: 500;
  color: var(--color-online);
  background: rgba(16,185,129,0.1);
  padding: 1px 6px;
  border-radius: 999px;
}
.tag-offline {
  font-size: 11px;
  color: var(--text-muted);
  background: rgba(148,163,184,0.1);
  padding: 1px 6px;
  border-radius: 999px;
}
.tag-group {
  font-size: 11px;
  font-weight: 500;
  color: var(--color-primary-light);
  background: var(--color-primary-subtle);
  padding: 1px 6px;
  border-radius: 999px;
}
.tag-ai {
  font-size: 11px;
  color: var(--color-accent);
  background: rgba(6,182,212,0.1);
  padding: 1px 6px;
  border-radius: 999px;
}

/* 未读徽标 */
.unread-dot {
  flex-shrink: 0;
  background: var(--brand-gradient-2);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  min-width: 18px;
  height: 18px;
  border-radius: 999px;
  display: flex; align-items: center; justify-content: center;
  padding: 0 5px;
  box-shadow: 0 2px 8px rgba(124,58,237,0.45);
}

/* 过渡动画 */
.badge-bounce-enter-active {
  animation: badge-in 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.badge-bounce-leave-active { transition: all 0.18s; }
.badge-bounce-leave-to { transform: scale(0); opacity: 0; }

@keyframes badge-in {
  from { transform: scale(0); opacity: 0; }
  to   { transform: scale(1); opacity: 1; }
}

.chat-item-anim-enter-active {
  animation: fadeInLeft 0.28s ease both;
}
.chat-item-anim-leave-active { transition: all 0.2s; }
.chat-item-anim-leave-to { opacity: 0; transform: translateX(-10px); }

@keyframes fadeInLeft {
  from { opacity: 0; transform: translateX(-10px); }
  to   { opacity: 1; transform: translateX(0); }
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
    // 仅对真实聊天消息（非系统通知）处理
    const realMsgTypes = ['text', 'image', 'file', 'vote', 'location']
    if (!realMsgTypes.includes(message.contentType)) return

    // 更新对应聊天的最后一条消息预览
    const chatItem = state.chatList.find((item) => item.chatId === message.chatId)
    if (chatItem) {
      chatItem.lastMessage = message.contentType === 'text'
        ? message.content
        : message.contentType === 'image' ? '[图片]'
        : message.contentType === 'file' ? '[文件]'
        : message.contentType === 'vote' ? '[投票]'
        : '[位置]'
      chatItem.lastTime = message.createTime
    }

    // 只有非本人发送的消息才增加未读计数
    if (message.sendId !== userStore.user.id && message.chatId !== state.activeChatId) {
      if (chatItem) {
        chatItem.unreadCount = (chatItem.unreadCount || 0) + 1
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
