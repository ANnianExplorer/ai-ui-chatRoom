<template>
  <div class="chat-container qq-style">
    <!-- QQ风格两栏布局 -->
    <div class="qq-layout">
      <!-- 中间聊天列表 -->
      <div class="qq-chat-list">
        <!-- 搜索栏 -->
        <div class="list-header">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索消息、群聊、联系人"
            prefix-icon="Search"
            clearable
            size="small"
            class="search-input"
          />
          <div style="display: flex; gap: 8px;">
            <el-dropdown placement="bottom" trigger="click">
              <el-button type="primary" round size="small" class="add-btn">
                <el-icon><Plus /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleAddUser">添加好友</el-dropdown-item>
                  <el-dropdown-item @click="handleAddGroup">创建群聊</el-dropdown-item>
                  <el-dropdown-item @click="handleJoinGroup">加入群聊</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>


        <!-- 聊天列表 -->
        <div style="flex: 1; overflow: hidden;">
          <ChatList 
            ref="chatListRef" 
            @getChat="getChat" 
            :search-keyword="searchKeyword"
          />
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="qq-chat-window">
        <!-- 聊天窗口 -->
        <ChatWindow :chat="chat" @show-group-member="showGroupMember" />
      </div>

      <!-- 群成员侧边栏 -->
    <GroupMemberSidebar
      :show="showMemberList"
      :chat="chat"
      @update:show="showMemberList = $event"
    />
    </div>
    
    <!-- 对话框组件 -->
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
import '@/theme/chat.scss'

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
