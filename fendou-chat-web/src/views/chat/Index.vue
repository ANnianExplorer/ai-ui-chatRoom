<template>
  <div class="chat-page">
    <div class="chat-layout">
      <!-- 左侧会话列表 -->
      <div class="chat-panel">
        <!-- 面板头部 -->
        <div class="panel-header">
          <div class="panel-title">消息</div>
          <el-dropdown placement="bottom-end" trigger="click">
            <button class="icon-btn" title="新建会话">
              <svg viewBox="0 0 24 24" fill="none" class="btn-svg">
                <line x1="12" y1="5" x2="12" y2="19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <line x1="5" y1="12" x2="19" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
            </button>
            <template #dropdown>
              <el-dropdown-menu class="panel-dropdown">
                <el-dropdown-item @click="handleAddUser">添加好友</el-dropdown-item>
                <el-dropdown-item @click="handleAddGroup">创建群聊</el-dropdown-item>
                <el-dropdown-item @click="handleJoinGroup">加入群聊</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 搜索框 -->
        <div class="search-wrap">
          <div class="search-box">
            <svg viewBox="0 0 24 24" fill="none" class="search-icon">
              <circle cx="11" cy="11" r="8" stroke="currentColor" stroke-width="2"/>
              <path d="m21 21-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            <input
              v-model="searchKeyword"
              placeholder="搜索消息、群聊、联系人"
              class="search-input"
            />
          </div>
        </div>

        <!-- 会话列表 -->
        <div class="list-body">
          <ChatList
            ref="chatListRef"
            @getChat="getChat"
            :search-keyword="searchKeyword"
          />
        </div>
      </div>

      <!-- 右侧聊天窗口 -->
      <div class="chat-window-area">
        <ChatWindow :chat="chat" @show-group-member="showGroupMember" />
      </div>

      <!-- 群成员侧边栏 -->
      <GroupMemberSidebar
        :show="showMemberList"
        :chat="chat"
        @update:show="showMemberList = $event"
      />
    </div>

    <!-- 对话框 -->
    <AddFriendDialog ref="addFriendDialogRef" />
    <AddGroupDialog ref="addGroupDialogRef" />
    <JoinGroupDialog ref="joinGroupDialogRef" />
  </div>
</template>

<script setup>
// 引入组件
import { ref, reactive, computed, onMounted, watch, provide } from 'vue'
import { useRouter } from 'vue-router'
import ChatList from './component/ChatList.vue'
import ChatWindow from './component/ChatWindow.vue'
import GroupMemberSidebar from './component/GroupMemberSidebar.vue'
import { defineAsyncComponent } from 'vue'

// 引入websocket store
import { useWebsocketStore } from '@/stores/websocket.js'

// 引入图标
import { ChatDotRound, Search, ArrowDown, Close, Plus } from '@element-plus/icons-vue'
// 引入消息提示
import { ElMessage } from 'element-plus'

// 引入API
import { useChatApi } from '@/api/chat/index.js'
import { useGroupApi } from '@/api/group/index.js'
import { useUserApi } from '@/api/user/index.js'
import { useUserStore } from '@/stores/user.js'
// import '@/theme/chat.scss'

// 异步导入对话框组件
const AddFriendDialog = defineAsyncComponent(() => import('@/views/contact/component/AddFriend.vue'))
const AddGroupDialog = defineAsyncComponent(() => import('@/views/contact/component/AddGroup.vue'))
const JoinGroupDialog = defineAsyncComponent(() => import('@/views/contact/component/JoinGroup.vue'))

// API和store实例
const chatApi = useChatApi()
const groupApi = useGroupApi()
const userApi = useUserApi()
const userStore = useUserStore()
const router = useRouter()
const websocket = useWebsocketStore()

// 对话框引用
const addFriendDialogRef = ref(null)
const addGroupDialogRef = ref(null)
const joinGroupDialogRef = ref(null)

// 当前聊天
const chat = ref()

// 搜索关键词
const searchKeyword = ref('')

// 活跃标签页
const activeTab = ref('message')

// 群成员列表显示状态
const showMemberList = ref(false)

// 真实数据状态
const chatList = ref([])
const loading = ref(false)

// 获取聊天列表数据
const fetchChatList = async () => {
  loading.value = true
  try {
    const params = { pIndex: 1, pSize: -1 } // 获取所有数据
    const searchForm = { chatType: 0 } // 0表示全部
    const res = await chatApi.getChats(params, searchForm)
    chatList.value = res.data.records
    console.log('获取聊天列表成功', chatList.value)
  } catch (error) {
    console.error('获取聊天列表失败', error)
    ElMessage.error('获取聊天列表失败')
  } finally {
    loading.value = false
  }
}

// 获取聊天信息
const getChat = (res) => {
  chat.value = res
  // 如果是群聊，默认显示群成员列表
  if (res && res.type === 2) {
    showMemberList.value = true
  } else {
    showMemberList.value = false
  }
}

// 显示/隐藏群成员列表
const showGroupMember = (show) => {
  showMemberList.value = show
}

// 添加好友
const handleAddUser = () => {
  addFriendDialogRef.value.openDialog()
}

// 创建群聊
const handleAddGroup = () => {
  addGroupDialogRef.value.openDialog()
}

// 加入群聊
const handleJoinGroup = () => {
  joinGroupDialogRef.value.openDialog()
}

// 组件挂载时获取数据
onMounted(() => {
  // 可以在挂载时预加载数据
  fetchChatList()
})
</script>

<style lang="scss" scoped>
.chat-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 10px;
  box-sizing: border-box;
}

.chat-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
  border-radius: 18px;
  box-shadow: var(--glass-shadow-lg);
  background: var(--card-bg);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid var(--glass-border);
  overflow: hidden;
}

/* 左侧面板 */
.chat-panel {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-subtle);
  background: var(--chat-list-bg);
  backdrop-filter: blur(20px);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 16px 10px;
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}

.icon-btn {
  width: 32px; height: 32px;
  border-radius: 10px;
  border: 1px solid var(--border-default);
  background: var(--color-primary-subtle);
  color: var(--color-primary);
  display: flex; align-items: center; justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--color-primary-subtle-2);
    transform: scale(1.05);
  }
}

.btn-svg { width: 16px; height: 16px; }

/* 搜索框 */
.search-wrap {
  padding: 0 10px 10px;
  flex-shrink: 0;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--input-bg);
  border: 1px solid var(--input-border);
  border-radius: 12px;
  transition: all 0.2s ease;

  &:focus-within {
    border-color: var(--input-focus-border);
    box-shadow: var(--input-focus-shadow);
  }
}

.search-icon {
  width: 15px; height: 15px;
  color: var(--text-muted);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: var(--text-primary);
  font-family: var(--font-sans);

  &::placeholder { color: var(--text-muted); }
}

.list-body {
  flex: 1;
  overflow: hidden;
}

/* 右侧窗口 */
.chat-window-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--chat-window-bg);
  min-width: 0;
}
</style>

<style lang="scss">
/* 面板下拉菜单全局覆盖 */
.panel-dropdown {
  border-radius: 12px !important;
  border: 1px solid var(--border-default) !important;
  background: var(--glass-bg-strong) !important;
  backdrop-filter: blur(16px);
  box-shadow: var(--glass-shadow) !important;
  padding: 4px !important;

  .el-dropdown-menu__item {
    border-radius: 8px !important;
    margin: 2px 0;
    font-size: 13px;
    color: var(--text-primary) !important;
    transition: all 0.2s ease;
    &:hover {
      background: var(--color-primary-subtle) !important;
      color: var(--color-primary) !important;
    }
  }
}
</style>
