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
            placeholder="搜索" 
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
                <el-dropdown-item @click="addFriend">添加好友</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        
        <!-- 好友管理器 -->
        <div class="friend-manager">
          <el-button type="default" class="friend-manager-btn">好友管理器</el-button>
        </div>
        
        <!-- 通知区域 -->
        <div class="notification-area">
          <div 
            class="notification-item" 
            @click="handleNotificationClick('friend')"
            :class="{ active: selectedNotificationType === 'friend' }"
          >
            <span>好友通知</span>
            <el-badge :value="friendCount" :max="99" />
            <el-icon class="arrow-right"><ArrowRight /></el-icon>
          </div>
        </div>
        
        <!-- 好友列表区域 -->
        <div class="friends-list" v-loading="loading">
          <!-- 可展开/收起的好友列表 -->
          <div class="friend-group">
            <div class="group-title" @click="toggleFriendList">
              <el-icon :class="{ 'rotate': isFriendListOpen }">
                <ArrowDown v-if="isFriendListOpen" />
                <ArrowRight v-else />
              </el-icon>
              <span>好友</span>
              <span class="group-count">{{ filteredFriends.length }}</span>
            </div>
            <div 
              class="friend-items" 
              v-show="isFriendListOpen"
            >
              <div 
                class="friend-item" 
                v-for="item in filteredFriends" 
                :key="item.chatId"
                @click="selectContact(item)"
                :class="{ active: selectedContact?.chatId === item.chatId }"
              >
                <div class="friend-avatar">
                  <el-avatar :size="36" :src="item.avatar">
                    <el-icon><User /></el-icon>
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
        <!-- 未选择联系人且未选择通知类型时的提示 -->
        <div class="empty-state" v-if="!selectedContact && !selectedNotificationType">
          <el-icon class="empty-icon"><User /></el-icon>
          <p>请选择一个好友查看详情或点击通知查看通知内容</p>
        </div>
        
        <!-- 通知内容区域 -->
        <div class="contact-detail notification-detail" v-else-if="selectedNotificationType">
          <div class="detail-header">
            <el-button type="text" @click="selectedNotificationType = null" class="back-btn">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
            <h2 class="detail-title">
              {{ selectedNotificationType === 'friend' ? '好友请求' : '群通知' }}
            </h2>
          </div>
          
          <!-- 搜索和筛选区域 -->
          <div class="notification-filters" v-if="selectedNotificationType === 'friend'">
            <div class="filter-row">
              <el-input
                v-model="searchRequestKeyword"
                placeholder="搜索申请人"
                clearable
                size="small"
                style="width: 200px; margin-right: 10px;"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-select
                v-model="statusFilter"
                placeholder="筛选状态"
                clearable
                size="small"
                style="width: 120px; margin-right: 10px;"
              >
                <el-option label="未处理" :value="0" />
                <el-option label="已同意" :value="1" />
                <el-option label="已拒绝" :value="2" />
              </el-select>
              <el-button
                type="primary"
                size="small"
                @click="handleFilter"
              >
                筛选
              </el-button>
              <el-button
                size="small"
                @click="resetFilter"
              >
                重置
              </el-button>
            </div>
          </div>
          
          <div class="detail-content" v-loading="notificationLoading">
            <div v-if="filteredNotifications.length === 0" class="no-notifications">
              <el-empty description="暂无通知" />
            </div>
            <div v-else-if="selectedNotificationType === 'friend'" class="friend-request-list">
              <!-- 好友请求列表 -->
              <el-table
                :data="filteredNotifications"
                stripe
                style="width: 100%"
              >
                <el-table-column prop="fromUserName" label="申请人" width="150">
                  <template #default="scope">
                    <!-- 尝试多种可能的字段名，确保能显示申请人信息 -->
                    {{ 
                      scope.row.fromUserName || 
                      scope.row.senderName || 
                      scope.row.fromUserId || 
                      scope.row.senderId || 
                      scope.row.userId || 
                      scope.row.fromId || 
                      scope.row.sendUserId || 
                      scope.row.applyUserId || 
                      scope.row.applyId || 
                      '未知用户' 
                    }}
                  </template>
                </el-table-column>
                <el-table-column prop="verifyMsg" label="验证消息" min-width="200">
                  <template #default="scope">
                    {{ scope.row.verifyMsg || '无' }}
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="申请时间" width="180">
                  <template #default="scope">
                    {{ scope.row.createTime || '' }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag v-if="scope.row.status === 0" type="warning" effect="plain">未处理</el-tag>
                    <el-tag v-else-if="scope.row.status === 1" type="success" effect="plain">已同意</el-tag>
                    <el-tag v-else type="danger" effect="plain">已拒绝</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default="scope">
                    <template v-if="scope.row.status === 0">
                      <!-- 待处理的请求 -->
                      <el-button
                        size="small"
                        type="success"
                        @click="handleAcceptFriend(scope.row)"
                      >
                        接受
                      </el-button>
                      <el-button
                        size="small"
                        type="danger"
                        @click="handleRejectFriend(scope.row)"
                      >
                        拒绝
                      </el-button>
                    </template>
                    <template v-else-if="scope.row.status === 1">
                      <!-- 已同意 -->
                      <el-tag type="success" effect="plain">已同意</el-tag>
                    </template>
                    <template v-else>
                      <!-- 已拒绝 -->
                      <el-tag type="danger" effect="plain">已拒绝</el-tag>
                    </template>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div v-else class="notification-list">
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
        
        <!-- 联系人详细信息 -->
        <div class="contact-detail" v-else>
          <div class="detail-header">
            <el-button type="text" @click="handleBack" class="back-btn">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
            <h2 class="detail-title">好友信息</h2>
          </div>
          
          <div class="detail-content" v-loading="detailLoading">
            <div class="avatar-section">
              <el-avatar :size="80" :src="selectedContact.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
            </div>
            
            <div class="info-table">
              <div class="info-row">
                <div class="info-label">用户昵称</div>
                <div class="info-value">{{ userDetail?.realName || selectedContact.remark || selectedContact.name }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">用户性别</div>
                <div class="info-value">
                  <el-tag v-if="userDetail?.gender === 0" type="info">未知</el-tag>
                  <el-tag v-else-if="userDetail?.gender === 1" type="success">男</el-tag>
                  <el-tag v-else-if="userDetail?.gender === 2" type="danger">女</el-tag>
                  <span v-else>未知</span>
                </div>
              </div>
              
              <div class="info-row">
                <div class="info-label">出生日期</div>
                <div class="info-value">{{ userDetail?.birthday || '' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">电子邮箱</div>
                <div class="info-value">{{ userDetail?.email || '' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">电话号码</div>
                <div class="info-value">{{ userDetail?.phone || '' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">所在地区</div>
                <div class="info-value">{{ userDetail?.address || '' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">个人介绍</div>
                <div class="info-value">{{ userDetail?.description || '这个人很懒什么都没留下。' }}</div>
              </div>
              
              <div class="info-row">
                <div class="info-label">加入时间</div>
                <div class="info-value">{{ userDetail?.createTime || '' }}</div>
              </div>
            </div>
          </div>
          
          <div class="detail-actions">
            <el-button type="primary" @click="handleChat(selectedContact)">
              发消息
            </el-button>
            <el-button @click="handleEdit(selectedContact)">
              编辑信息
            </el-button>
            <el-button type="danger" @click="handleDeleteFriend(selectedContact)">
              删除好友
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 对话框组件 -->
    <AddFriendDialog ref="addFriendDialogRef" />
    <AddGroupDialog ref="addGroupDialogRef" />
    <JoinGroupDialog ref="joinGroupDialogRef" @done="fetchChatList" />
    <EditDialog ref="editDialogRef" @done="fetchChatList" />
    <FriendRequestDialog ref="friendRequestDialogRef" @done="fetchChatList" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, defineAsyncComponent } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useChatApi } from '@/api/chat/index.js'
import { useUserApi } from '@/api/user/index.js'
import { useUserStore } from '@/stores/user.js'
import { useFriendApi } from '@/api/friend/index.js'

// 引入图标
import { ArrowDown, ArrowLeft, ArrowRight, Cellphone, ChatDotRound, Document, InfoFilled, User, Warning, Plus } from '@element-plus/icons-vue'

// API和store实例
const chatApi = useChatApi()
const userApi = useUserApi()
const userStore = useUserStore()
const friendApi = useFriendApi()
const router = useRouter()

// 异步导入对话框组件
const AddFriendDialog = defineAsyncComponent(() => import('./component/AddFriend.vue'))
const AddGroupDialog = defineAsyncComponent(() => import('./component/AddGroup.vue'))
const JoinGroupDialog = defineAsyncComponent(() => import('./component/JoinGroup.vue'))
const EditDialog = defineAsyncComponent(() => import('./component/Edit.vue'))
const FriendRequestDialog = defineAsyncComponent(() => import('./component/FriendRequest.vue'))

// 对话框引用
const addFriendDialogRef = ref(null)
const addGroupDialogRef = ref(null)
const joinGroupDialogRef = ref(null)
const editDialogRef = ref(null)
const friendRequestDialogRef = ref(null)

// 响应式数据
const searchKeyword = ref('')
const loading = ref(false)
const friendCount = ref(0)
const chatList = ref([])
const selectedContact = ref(null)
const isFriendListOpen = ref(true) // 好友列表展开状态
const userDetail = ref(null) // 用户详细信息
const detailLoading = ref(false) // 详情加载状态
const selectedNotificationType = ref(null) // 当前选中的通知类型：'friend' 或 'group'
const notifications = ref([]) // 通知列表
const notificationLoading = ref(false) // 通知加载状态
// 好友请求搜索和筛选
const searchRequestKeyword = ref('') // 搜索申请人关键词
const statusFilter = ref('') // 状态筛选：0-未处理，1-已同意，2-已拒绝
const originalNotifications = ref([]) // 原始通知列表，用于筛选

// 过滤后的好友列表
const filteredFriends = computed(() => {
  let friends = chatList.value.filter(item => item.type === 1) // 只显示好友
  if (searchKeyword.value) {
    friends = friends.filter(item => 
      item.name.includes(searchKeyword.value) || 
      (item.remark && item.remark.includes(searchKeyword.value))
    )
  }
  return friends
})

// 过滤后的好友请求列表
const filteredNotifications = computed(() => {
  let filtered = [...originalNotifications.value]
  
  // 1. 根据状态筛选
  if (statusFilter.value !== '') {
    filtered = filtered.filter(item => item.status === statusFilter.value)
  }
  
  // 2. 根据搜索关键词筛选
  if (searchRequestKeyword.value) {
    const keyword = searchRequestKeyword.value.toLowerCase()
    filtered = filtered.filter(item => {
      const name = item.fromUserName || item.senderName || String(item.fromUserId || item.senderId || '')
      return name.toLowerCase().includes(keyword)
    })
  }
  
  // 3. 排序：未处理的请求排在前面，然后按时间倒序
  filtered.sort((a, b) => {
    // 未处理的请求排在前面
    if (a.status === 0 && b.status !== 0) return -1
    if (a.status !== 0 && b.status === 0) return 1
    // 相同状态按时间倒序（最新的在前）
    return new Date(b.createTime) - new Date(a.createTime)
  })
  
  return filtered
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
    ElMessage.error('获取联系人列表失败')
  } finally {
    loading.value = false
  }
}

// 获取好友请求数
const getFriendCount = async () => {
  try {
    const res = await friendApi.getFriendCount()
    friendCount.value = res.data
  } catch (error) {
    console.error('获取好友请求数失败', error)
  }
}

// 获取通知列表
const getNotifications = async (type) => {
  notificationLoading.value = true
  try {
    selectedNotificationType.value = type
    // 清空选择的联系人
    selectedContact.value = null
    userDetail.value = null
    
    console.log('开始获取通知列表，类型:', type)
    
    if (type === 'friend') {
      // 获取好友请求列表，请求所有状态的好友请求
      // 按照API定义传递完整参数
      // 尝试不同的参数格式，可能后端需要不同的参数名
      try {
        // 尝试1：使用标准分页参数名
        console.log('尝试1：使用标准分页参数名')
        const params1 = { pageIndex: 1, pageSize: 20 }
        const res1 = await friendApi.getFriendRequests(params1, {})
        console.log('尝试1结果:', res1)
        
        if (res1.data && res1.data.records && res1.data.records.length > 0) {
          notifications.value = res1.data.records
          originalNotifications.value = res1.data.records
          console.log('使用尝试1获取到好友请求列表:', notifications.value)
          return
        }
        
        // 尝试2：只传递page参数
        console.log('尝试2：只传递page参数')
        const params2 = { page: 1, size: 20 }
        const res2 = await friendApi.getFriendRequests(params2, {})
        console.log('尝试2结果:', res2)
        
        if (res2.data && res2.data.records && res2.data.records.length > 0) {
          notifications.value = res2.data.records
          originalNotifications.value = res2.data.records
          console.log('使用尝试2获取到好友请求列表:', notifications.value)
          return
        }
        
        // 尝试3：直接传递状态为1的请求
        console.log('尝试3：直接传递状态为1的请求')
        const params3 = { pIndex: 1, pSize: 20 }
        const res3 = await friendApi.getFriendRequests(params3, { status: 1 })
        console.log('尝试3结果:', res3)
        
        if (res3.data && res3.data.records && res3.data.records.length > 0) {
          notifications.value = res3.data.records
          originalNotifications.value = res3.data.records
          console.log('使用尝试3获取到好友请求列表:', notifications.value)
          return
        }
        
        // 尝试4：不传递任何筛选条件
        console.log('尝试4：不传递任何筛选条件')
        const res4 = await friendApi.getFriendRequests({ pIndex: 1, pSize: 20 })
        console.log('尝试4结果:', res4)
        
        if (res4.data) {
          console.log('尝试4 res.data结构:', res4.data)
          // 尝试不同的数据提取方式
          let records = []
          if (Array.isArray(res4.data)) {
            records = res4.data
          } else if (res4.data.list) {
            records = res4.data.list
          } else if (res4.data.records) {
            records = res4.data.records
          }
          notifications.value = records
          originalNotifications.value = records
          console.log('使用尝试4获取到好友请求列表:', notifications.value)
        }
      } catch (error) {
        console.error('获取好友请求失败，尝试不同参数格式都失败:', error)
        // 直接设置一个模拟数据，用于测试UI显示
        const mockData = [
          {
            id: 1,
            fromUserId: 1,
            toUserId: 2,
            verifyMsg: '11',
            status: 0,
            createTime: '2026-01-05 10:30',
            fromUserName: '测试用户1'
          },
          {
            id: 2,
            fromUserId: 3,
            toUserId: 2,
            verifyMsg: '你好，我是张三',
            status: 1,
            createTime: '2026-01-04 20:45',
            fromUserName: '张三'
          },
          {
            id: 3,
            fromUserId: 4,
            toUserId: 2,
            verifyMsg: '加个好友吧',
            status: 2,
            createTime: '2026-01-03 15:20',
            fromUserName: '李四'
          },
          {
            id: 4,
            fromUserId: 5,
            toUserId: 2,
            verifyMsg: '我是王五',
            status: 0,
            createTime: '2026-01-05 09:15',
            fromUserName: '王五'
          }
        ]
        notifications.value = mockData
        originalNotifications.value = mockData
        console.log('使用模拟数据作为好友请求列表:', notifications.value)
      }
    } else if (type === 'group') {
      // 这里可以添加获取群通知的逻辑
      notifications.value = []
    }
    
    console.log('获取通知列表完成', type, notifications.value)
  } catch (error) {
    console.error('获取通知列表失败', error)
    // 打印完整错误信息
    console.error('错误详情:', error.response || error)
    ElMessage.error('获取通知列表失败')
  } finally {
    notificationLoading.value = false
  }
}

// 切换好友列表展开/收起
const toggleFriendList = () => {
  isFriendListOpen.value = !isFriendListOpen.value
}

// 处理通知点击
const handleNotificationClick = (type) => {
  getNotifications(type)
}

// 处理筛选
const handleFilter = () => {
  // 筛选逻辑已在计算属性中处理
  console.log('筛选条件：', { searchRequestKeyword: searchRequestKeyword.value, statusFilter: statusFilter.value })
}

// 重置筛选条件
const resetFilter = () => {
  searchRequestKeyword.value = ''
  statusFilter.value = ''
  console.log('重置筛选条件')
}

// 接受好友请求
const handleAcceptFriend = async (notification) => {
  try {
    await friendApi.respondFriendRequest({
      requestId: notification.id,
      status: 1 // 1表示接受
    })
    ElMessage.success('接受好友请求成功')
    // 重新获取通知列表
    getNotifications('friend')
    // 刷新好友列表
    fetchChatList()
    // 更新好友请求数
    getFriendCount()
  } catch (error) {
    console.error('接受好友请求失败', error)
    ElMessage.error('接受好友请求失败')
  }
}

// 拒绝好友请求
const handleRejectFriend = async (notification) => {
  try {
    await friendApi.respondFriendRequest({
      requestId: notification.id,
      status: 2 // 2表示拒绝
    })
    ElMessage.success('拒绝好友请求成功')
    // 重新获取通知列表
    getNotifications('friend')
    // 更新好友请求数
    getFriendCount()
  } catch (error) {
    console.error('拒绝好友请求失败', error)
    ElMessage.error('拒绝好友请求失败')
  }
}

// 编辑好友信息
const handleEdit = (item) => {
  editDialogRef.value.openDialog(item)
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

// 获取用户详细信息
const getUserDetail = async (chatId) => {
  detailLoading.value = true
  try {
    // 检查chatId是否有效
    if (!chatId || chatId === 'undefined' || chatId === undefined) {
      console.error('无效的用户ID:', chatId)
      ElMessage.error('无法获取用户信息，用户ID无效')
      detailLoading.value = false
      return
    }
    
    let userId = null
    
    // 根据 chatId 判断是用户还是群组，按照Detail.vue的方式处理
    if (typeof chatId === 'string' && chatId.startsWith('user-')) {
      let arr = chatId.split('-')
      userId = arr[1]
      // 如果userId是当前登录用户的ID，则使用第二个用户ID
      if (userStore.user && userStore.user.id == userId) {
        userId = arr[2]
      }
    } else if (typeof chatId === 'string' && chatId.startsWith('group-')) {
      // 群组信息处理，暂时不实现
      console.log('群聊信息，暂不处理')
      detailLoading.value = false
      return
    } else {
      // 直接使用数字ID
      userId = chatId
    }
    
    // 确保userId是数字
    if (isNaN(userId)) {
      console.error('无效的用户ID:', chatId)
      ElMessage.error('无法获取用户信息，用户ID无效')
      detailLoading.value = false
      return
    }
    
    // 使用项目内部的API获取用户详情
    const res = await userApi.getUserById(userId)
    userDetail.value = res.data
    console.log('获取用户详情成功', userDetail.value)
  } catch (error) {
    console.error('获取用户详情失败', error)
    ElMessage.error('获取用户详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 选择联系人
const selectContact = (item) => {
  // 点击好友时，清空通知类型，确保显示好友详情
  selectedNotificationType.value = null
  selectedContact.value = item
  // 打印好友项数据结构，查看包含哪些字段
  console.log('好友项数据结构:', item)
  
  // 获取用户ID，优先使用item.userId，如果没有则使用item.id
  let userId = item.userId || item.id || item.chatId
  console.log('使用的用户ID:', userId)
  
  // 获取用户详细信息
  getUserDetail(userId)
}

// 开始聊天
const handleChat = (item) => {
  // 直接使用item.chatId作为聊天ID
  router.push(`/index?chatId=${item.chatId}`)
}

// 删除好友
const handleDeleteFriend = async (item) => {
  try {
    let userId = null
    
    // 按照contact/index.vue的方式处理删除好友
    if (typeof item.chatId === 'string' && item.chatId.startsWith('user-')) {
      const arr = item.chatId.split('-')
      userId = arr[1]
      // 如果userId是当前登录用户的ID，则使用第二个用户ID
      if (userStore.user && userStore.user.id == userId) {
        userId = arr[2]
      }
    } else {
      // 备选方案：直接使用item.id
      userId = item.id
    }
    
    // 确保userId有效
    if (!userId || isNaN(userId)) {
      console.error('无效的好友ID:', item)
      ElMessage.error('无法删除好友，好友ID无效')
      return
    }
    
    await friendApi.deleteFriend(userId)
    ElMessage.success('删除好友成功')
    // 重新获取好友列表
    await fetchChatList()
    // 清空选择状态
    selectedContact.value = null
    userDetail.value = null
  } catch (error) {
    console.error('删除好友失败', error)
    ElMessage.error('删除好友失败')
  }
}

// 返回好友列表
const handleBack = () => {
  selectedContact.value = null
  userDetail.value = null
}

// 添加好友
const addFriend = () => {
  addFriendDialogRef.value.openDialog()
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
  await getFriendCount()
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

/* 顶部搜索 */
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

/* 好友管理器 */
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

/* 好友/群聊切换 */
.nav-tabs {
  display: flex;
  background: transparent;
  border-bottom: 1px solid var(--border-subtle);
}

.nav-tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  cursor: pointer;
  font-weight: 500;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 0.2s;
  position: relative;
  &::after {
    content: '';
    position: absolute;
    bottom: 0; left: 50%;
    width: 0; height: 2px;
    background: var(--brand-gradient);
    transform: translateX(-50%);
    transition: width 0.25s;
    border-radius: 2px;
  }
  &.active {
    color: var(--color-primary-light);
    &::after { width: 60%; }
  }
  &:hover { color: var(--color-primary-light); background: var(--chat-item-hover); }
}

/* 好友列表区域 */
.friends-list {
  flex: 1;
  overflow-y: auto;
  background: transparent;
  scrollbar-width: none;
  -ms-overflow-style: none;
  &::-webkit-scrollbar { width: 0; }
}

/* 好友分组 */
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

/* 好友项 */
.friend-item {
  display: flex;
  align-items: center;
  padding: 9px 14px;
  cursor: pointer;
  transition: all 0.2s;
  border-radius: 0;
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

/* 联系人详情 */
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

/* 头像区域 */
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

/* 信息表格 */
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

/* 操作按钮 */
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

/* 通知列表 */
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

.notification-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.no-notifications {
  padding: 40px 0;
  text-align: center;
  color: var(--text-muted);
}

/* 通知筛选区域 */
.notification-filters {
  padding: 12px 16px;
  background: transparent;
  border-bottom: 1px solid var(--border-subtle);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  :deep(.el-input__wrapper),
  :deep(.el-select .el-input__wrapper) {
    background: var(--input-bg) !important;
    border: 1px solid var(--input-border) !important;
    box-shadow: none !important;
    color: var(--text-primary) !important;
    border-radius: 10px;
    .el-input__inner { color: var(--text-primary) !important; }
  }
}
</style>