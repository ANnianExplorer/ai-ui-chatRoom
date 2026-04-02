<template>
  <div class="contact-container qq-style">
    <!-- QQ风格两栏布局 -->
    <div class="qq-contact-layout">
      <!-- 左侧边栏 -->
      <div class="contact-left">
        <!-- 顶部搜索和添加按钮 -->
        <div class="contact-header">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索群聊" 
            clearable 
            @input="handleSearch" 
            size="small"
          />
          <el-dropdown placement="bottom" trigger="click">
            <el-button type="primary" round size="small" class="add-btn">
              <el-icon><Plus /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="createGroup">创建群聊</el-dropdown-item>
                <el-dropdown-item @click="joinGroup">加入群聊</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <!-- 群聊管理器 -->
        <div class="friend-manager">
          <el-button type="default" class="friend-manager-btn">群聊管理器</el-button>
        </div>
        
        <!-- 群通知区域 -->
        <div class="notification-area">
          <div 
            class="notification-item" 
            @click="handleNotificationClick()"
            :class="{ active: selectedNotificationType === 'group' }"
          >
            <span>群通知</span>
            <el-icon class="arrow-right"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 群聊列表区域 -->
        <div class="friends-list" v-loading="loading">
          <!-- 可展开/收起的群聊列表 -->
          <div class="friend-group">
            <div class="group-title" @click="toggleFriendList">
              <el-icon :class="{ 'rotate': isFriendListOpen }">
                <ArrowDown v-if="isFriendListOpen" />
                <ArrowRight v-else />
              </el-icon>
              <span>群聊</span>
              <span class="group-count">{{ filteredGroups.length }}</span>
            </div>
            <div 
              class="friend-items" 
              v-show="isFriendListOpen"
            >
              <div 
                class="friend-item" 
                v-for="item in filteredGroups" 
                :key="item.chatId"
                @click="selectGroup(item)"
                :class="{ active: selectedGroup?.chatId === item.chatId }"
              >
                <div class="friend-avatar">
                  <el-avatar :size="36" :src="item.avatar">
                    <el-icon><Collection /></el-icon>
                  </el-avatar>
                </div>
                <span class="friend-name">{{ item.remark || item.name }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧详情区域 -->
      <div class="contact-right">
        <!-- 未选择群聊且未选择通知类型时的提示 -->
        <div class="empty-state" v-if="!selectedGroup && !selectedNotificationType">
          <el-icon class="empty-icon"><Collection /></el-icon>
          <p>请选择一个群聊查看详情或点击通知查看群通知</p>
        </div>
        
        <!-- 群通知内容区域 -->
        <div class="contact-detail notification-detail" v-else-if="selectedNotificationType">
          <div class="detail-header">
            <el-button type="text" @click="selectedNotificationType = null" class="back-btn">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
            <h2 class="detail-title">群通知</h2>
          </div>
          
          <div class="detail-content" v-loading="notificationLoading">
            <div v-if="notifications.length === 0" class="no-notifications">
              <el-empty description="暂无群通知" />
            </div>
            <div class="notification-list">
              <!-- 群通知列表 -->
              <div class="notification-item-detail" v-for="(notification, index) in notifications" :key="index">
                <div class="notification-info">
                  <div class="notification-title">群通知</div>
                  <div class="notification-time">{{ notification.createTime || '' }}</div>
                </div>
                <div class="notification-body">
                  <p>{{ notification.content || '暂无内容' }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 群聊详细信息 -->
        <div class="contact-detail" v-else>
          <div class="detail-header">
            <el-button type="text" @click="handleBack" class="back-btn">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
            <h2 class="detail-title">群聊信息</h2>
          </div>
          
          <div class="detail-content" v-loading="detailLoading">
            <div class="avatar-section">
              <el-avatar :size="80" :src="selectedGroup.avatar">
                <el-icon><Collection /></el-icon>
              </el-avatar>
            </div>
            
            <div class="info-table">
              <div class="info-row">
                <div class="info-label">群聊名称</div>
                <div class="info-value">{{ selectedGroup.name }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">群聊备注</div>
                <div class="info-value">{{ selectedGroup.remark || '无' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">群聊介绍</div>
                <div class="info-value">{{ selectedGroup.description || '暂无介绍' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">群聊成员</div>
                <div class="info-value">
                  {{ selectedGroup.memberCount || selectedGroup.members?.length || '未知' }}人
                </div>
              </div>
              
              <div class="info-row">
                <div class="info-label">创建时间</div>
                <div class="info-value">{{ selectedGroup.createTime || selectedGroup.create_time || '未知' }}</div>
              </div>
            </div>
          </div>
          
          <div class="detail-actions">
            <el-button type="primary" @click="handleChat(selectedGroup)">
              发消息
            </el-button>
            <el-button @click="handleEdit(selectedGroup)">
              编辑信息
            </el-button>
            <!-- 所有成员都显示退出群聊按钮 -->
            <el-button 
              type="warning" 
              @click="handleQuitGroup(selectedGroup)"
            >
              退出群聊
            </el-button>
            <!-- 只有群主才能看到解散群聊按钮 -->
            <el-button 
              type="danger" 
              v-if="userStore.user && selectedGroup && userStore.user.id === selectedGroup.createBy" 
              @click="handleDissolveGroup(selectedGroup)"
            >
              解散群聊
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 对话框组件 -->
    <AddGroupDialog ref="addGroupDialogRef" @done="fetchChatList" />
    <EditDialog ref="editDialogRef" @done="fetchChatList" />
    <JoinGroupDialog ref="joinGroupDialogRef" @done="fetchChatList" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, defineAsyncComponent } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useChatApi } from '@/api/chat/index.js'
import { useGroupApi } from '@/api/group/index.js'
import { useUserStore } from '@/stores/user.js'

// 引入图标
import { ArrowDown, ArrowLeft, ArrowRight, Collection, User, Plus } from '@element-plus/icons-vue'

// API和store实例
const chatApi = useChatApi()
const groupApi = useGroupApi()
const userStore = useUserStore()
const router = useRouter()

// 异步导入对话框组件
const AddGroupDialog = defineAsyncComponent(() => import('./component/AddGroup.vue'))
const EditDialog = defineAsyncComponent(() => import('./component/Edit.vue'))
const JoinGroupDialog = defineAsyncComponent(() => import('./component/JoinGroup.vue'))

// 对话框引用
const addGroupDialogRef = ref(null)
const editDialogRef = ref(null)
const joinGroupDialogRef = ref(null)

// 响应式数据
const searchKeyword = ref('')
const loading = ref(false)
const chatList = ref([])
const selectedGroup = ref(null)
const isFriendListOpen = ref(true) // 群聊列表展开状态
const detailLoading = ref(false) // 详情加载状态
const selectedNotificationType = ref(null) // 当前选中的通知类型
const notifications = ref([]) // 通知列表
const notificationLoading = ref(false) // 通知加载状态

// 过滤后的群聊列表
const filteredGroups = computed(() => {
  let groups = chatList.value.filter(item => item.type === 2) // 只显示群聊
  if (searchKeyword.value) {
    groups = groups.filter(item => 
      item.name.includes(searchKeyword.value) || 
      (item.remark && item.remark.includes(searchKeyword.value)) ||
      (item.description && item.description.includes(searchKeyword.value))
    )
  }
  return groups
})

// 判断当前用户是否为群主
const isGroupOwner = computed(() => {
  console.log('群主判断调试信息:')
  console.log('userStore.user:', userStore.user)
  console.log('userStore.user.id:', userStore.user?.id)
  console.log('selectedGroup:', selectedGroup.value)
  console.log('selectedGroup.createBy:', selectedGroup.value?.createBy)
  console.log('selectedGroup.createById:', selectedGroup.value?.createById)
  console.log('selectedGroup.create_id:', selectedGroup.value?.create_id)
  
  if (!selectedGroup.value || !userStore.user) {
    console.log('群主判断结果: false (selectedGroup或userStore.user为空)')
    return false
  }
  
  const isOwner = userStore.user.id === selectedGroup.value.createBy
  console.log('群主判断结果:', isOwner)
  
  return isOwner
})

// 获取聊天列表数据
const fetchChatList = async () => {
  loading.value = true
  try {
    const params = { pIndex: 1, pSize: -1 } // 获取所有数据
    const searchForm = { chatType: 0 } // 0表示全部
    const res = await chatApi.getChats(params, searchForm)
    chatList.value = res.data.records
    console.log('获取聊天列表成功，数据结构:', chatList.value[0] || '空列表')
  } catch (error) {
    console.error('获取聊天列表失败', error)
    ElMessage.error('获取群聊列表失败')
  } finally {
    loading.value = false
  }
}

// 切换群聊列表展开/收起
const toggleFriendList = () => {
  isFriendListOpen.value = !isFriendListOpen.value
}

// 处理通知点击
const handleNotificationClick = () => {
  getNotifications()
}

// 获取通知列表
const getNotifications = async () => {
  notificationLoading.value = true
  try {
    selectedNotificationType.value = 'group'
    // 清空选择的群聊
    selectedGroup.value = null
    
    // 这里可以添加获取群通知的逻辑
    notifications.value = []
  } catch (error) {
    console.error('获取群通知列表失败', error)
    ElMessage.error('获取群通知列表失败')
  } finally {
    notificationLoading.value = false
  }
}

// 选择群聊
const selectGroup = async (item) => {
  // 点击群聊时，清空通知类型，确保显示群聊详情
  selectedNotificationType.value = null
  selectedGroup.value = item
  // 调用API获取完整的群聊信息
  detailLoading.value = true
  try {
    const groupId = item.chatId.split('-')[1]
    const res = await groupApi.getGroupById(groupId)
    console.log('群聊API返回完整数据:', res)
    console.log('群聊API返回data:', res.data)
    console.log('群聊API返回createBy:', res.data?.createBy)
    // 合并完整信息到selectedGroup
    selectedGroup.value = { ...selectedGroup.value, ...res.data }
    console.log('合并后的群聊信息:', selectedGroup.value)
  } catch (error) {
    console.error('获取群聊详情失败', error)
    ElMessage.error('获取群聊详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 返回群聊列表
const handleBack = () => {
  selectedGroup.value = null
}

// 开始聊天
const handleChat = (item) => {
  // 直接使用item.chatId作为聊天ID
  router.push(`/index?chatId=${item.chatId}`)
}

// 编辑群聊信息
const handleEdit = (item) => {
  editDialogRef.value.openDialog(item)
}

// 退出群聊
const handleQuitGroup = async (item) => {
  try {
    const groupId = item.chatId.split('-')[1]
    await groupApi.quitGroup(groupId)
    ElMessage.success('退出群聊成功')
    // 刷新群聊列表
    await fetchChatList()
    // 清空选择状态
    selectedGroup.value = null
  } catch (error) {
    console.error('退出群聊失败', error)
    ElMessage.error('退出群聊失败')
  }
}

// 解散群聊
const handleDissolveGroup = async (item) => {
  try {
    // 显示确认对话框，防止误操作
    const confirmResult = await ElMessageBox.confirm(
      `您确定要解散群聊"${item.name}"吗？此操作不可恢复！`,
      '确认解散群聊',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult === 'confirm') {
      const groupId = item.chatId.split('-')[1]
      await groupApi.deleteGroup(groupId)
      ElMessage.success('群聊解散成功')
      // 刷新群聊列表
      await fetchChatList()
      // 清空选择状态
      selectedGroup.value = null
    }
  } catch (error) {
    // 如果是用户取消操作，不显示错误信息
    if (error !== 'cancel') {
      console.error('解散群聊失败', error)
      ElMessage.error('解散群聊失败')
    }
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

// 创建群聊
const createGroup = () => {
  addGroupDialogRef.value.openDialog()
}

// 加入群聊
const joinGroup = () => {
  joinGroupDialogRef.value.openDialog()
}

// 组件挂载时获取数据
onMounted(async () => {
  await fetchChatList()
})
</script>

<style scoped lang="scss">

/* ── 容器 ── */
.contact-container {
  height: 100%;
  overflow: hidden;
  padding: 10px;
  box-sizing: border-box;
  background: transparent;
}

.qq-contact-layout {
  display: flex;
  height: 100%;
  background: var(--glass-bg);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--glass-shadow-lg);
}

/* ── 左侧边栏 ── */
.contact-left {
  width: 240px;
  background: var(--glass-bg);
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}

.contact-header {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 8px;
  background: transparent;
  border-bottom: 1px solid var(--border-subtle);
  :deep(.el-input__wrapper) {
    background: var(--input-bg);
    border: 1px solid var(--input-border);
    box-shadow: none;
    border-radius: 10px;
    transition: all 0.2s;
    &:hover, &.is-focus {
      border-color: var(--color-primary-light);
      box-shadow: var(--input-focus-shadow);
    }
  }
  :deep(.el-input__inner) {
    color: var(--text-primary);
    font-size: 13px;
    &::placeholder { color: var(--text-muted); }
  }
}

.add-btn {
  padding: 0;
  width: 32px;
  height: 32px;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--brand-gradient) !important;
  border: none !important;
  border-radius: 10px !important;
  color: #fff !important;
  transition: all 0.2s !important;
  &:hover { transform: scale(1.08); box-shadow: 0 4px 14px rgba(124, 58, 237, 0.4); }
}

.friend-manager {
  padding: 10px 12px;
  background: transparent;
  border-bottom: 1px solid var(--border-subtle);
}

.friend-manager-btn {
  width: 100%;
  background: var(--color-primary-subtle) !important;
  border: 1px solid var(--border-accent) !important;
  color: var(--color-primary-light) !important;
  border-radius: 10px !important;
  font-weight: 500;
  transition: all 0.2s !important;
  &:hover {
    background: var(--color-primary-subtle-2) !important;
    box-shadow: 0 2px 10px rgba(124, 58, 237, 0.2);
  }
}

/* 群聊列表 */
.friends-list {
  flex: 1;
  overflow-y: auto;
  background: transparent;
  scrollbar-width: none;
  -ms-overflow-style: none;
  &::-webkit-scrollbar { width: 0; }
}

.friend-group { margin-bottom: 4px; }

.group-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 14px;
  cursor: pointer;
  background: var(--bg-subtle);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  transition: background 0.2s;
  &:hover { background: var(--chat-item-hover); }
  .el-icon {
    margin-right: 6px;
    font-size: 11px;
    transition: transform 0.2s;
    &.rotate { transform: rotate(-90deg); }
  }
}

.group-count { font-size: 11px; color: var(--text-muted); font-weight: normal; }
.friend-items { transition: all 0.3s ease; }

/* 通知区域 */
.notification-area {
  background: transparent;
  border-bottom: 1px solid var(--border-subtle);
}

.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-primary);
  font-size: 13px;
  &:hover { background: var(--chat-item-hover); }
  &.active { background: var(--chat-item-active); color: var(--color-primary-light); }
  .arrow-right { font-size: 12px; color: var(--text-muted); }
}

/* 群聊项 */
.friend-item {
  display: flex;
  align-items: center;
  padding: 9px 14px;
  cursor: pointer;
  transition: all 0.2s;
  gap: 10px;
  &:hover { background: var(--chat-item-hover); }
  &.active { background: var(--chat-item-active); }
  .el-icon { font-size: 15px; color: var(--text-muted); }
}

.friend-avatar { flex-shrink: 0; }

.friend-name {
  flex: 1;
  font-size: 13px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── 右侧详情区域 ── */
.contact-right {
  flex: 1;
  background: transparent;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  scrollbar-width: none;
  -ms-overflow-style: none;
  &::-webkit-scrollbar { width: 0; }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  gap: 12px;
}

.empty-icon {
  font-size: 56px;
  opacity: 0.35;
  background: var(--brand-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 群聊详情 */
.contact-detail {
  padding: 24px;
  background: transparent;
  box-sizing: border-box;
  min-height: 100%;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-subtle);
}

.back-btn {
  margin-right: 14px;
  color: var(--text-muted) !important;
  &:hover { color: var(--color-primary-light) !important; }
}

.detail-title {
  font-size: 17px;
  color: var(--text-primary);
  font-weight: 700;
  margin: 0;
  background: var(--brand-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.detail-content { margin-bottom: 20px; }

.avatar-section {
  text-align: center;
  margin-bottom: 24px;
  padding: 24px 0 16px;
  :deep(.el-avatar) {
    margin: 0 auto;
    width: 88px !important;
    height: 88px !important;
    font-size: 34px !important;
    border: 3px solid var(--border-accent);
    box-shadow: 0 0 0 6px var(--color-primary-subtle), 0 8px 24px rgba(124, 58, 237, 0.25);
  }
}

.info-table {
  width: 100%;
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-md);
  overflow: hidden;
  padding: 4px 16px;
  box-shadow: var(--card-shadow);
}

.info-row {
  display: flex;
  padding: 11px 0;
  border-bottom: 1px solid var(--border-subtle);
  align-items: center;
  &:last-child { border-bottom: none; }
}

.info-label {
  width: 90px;
  font-weight: 600;
  color: var(--text-muted);
  text-align: right;
  padding-right: 16px;
  box-sizing: border-box;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.info-value {
  flex: 1;
  color: var(--text-primary);
  word-break: break-word;
  font-size: 14px;
  font-weight: 500;
}

.detail-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--border-subtle);
  :deep(.el-button) {
    border-radius: 12px !important;
    font-weight: 600;
    padding: 9px 24px !important;
    font-size: 13px !important;
    transition: all 0.2s !important;
    &:hover { transform: translateY(-2px); box-shadow: 0 6px 18px rgba(124, 58, 237, 0.3); }
  }
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}

.notification-item-detail {
  padding: 14px 16px;
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-md);
  border: 1px solid var(--glass-border);
  transition: all 0.2s;
  &:hover { box-shadow: var(--card-shadow); transform: translateY(-1px); }
}

.notification-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notification-title { font-weight: 600; color: var(--text-primary); font-size: 14px; }
.notification-time { font-size: 11px; color: var(--text-muted); }
.notification-body { color: var(--text-secondary); font-size: 13px; }

.no-notifications {
  padding: 40px 0;
  text-align: center;
  color: var(--text-muted);
}
</style>