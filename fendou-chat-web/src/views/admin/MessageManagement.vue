<template>
  <div class="admin-message-management">
    <div class="page-header">
      <h2>消息管理</h2>
      <p>管理系统消息，包括群聊信息、用户对话信息和机器人与用户的交流信息</p>
    </div>

    <!-- 消息管理标签页 -->
    <el-card shadow="hover" class="message-tabs-card">
      <el-tabs v-model="activeTab" class="message-tabs">
        <!-- 群聊消息 -->
        <el-tab-pane label="群聊消息" name="group-messages">
          <div class="tab-content">
            <!-- 群聊消息搜索栏 -->
            <div class="search-section">
              <el-input
                v-model="groupMessageSearch" 
                placeholder="搜索群聊消息内容/群聊名称"
                clearable
                @keyup.enter="handleGroupMessageSearch"
                class="search-input"
              >
                <template #append>
                  <el-button @click="handleGroupMessageSearch" type="primary" icon="Search">搜索</el-button>
                </template>
              </el-input>
              
              <div class="filter-section">
                <el-select v-model="groupMessageFilter.groupId" placeholder="选择群聊" clearable class="filter-select">
                  <el-option
                    v-for="group in groupsList"
                    :key="group.id"
                    :label="group.name"
                    :value="group.id"
                  />
                </el-select>
                
                <el-date-picker
                  v-model="groupMessageFilter.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  placeholder="选择时间范围"
                  class="date-filter"
                />
              </div>
            </div>

            <!-- 群聊消息列表 -->
            <el-table
              v-loading="groupMessagesLoading"
              :data="groupMessagesList"
              border
              style="width: 100%"
            >
              <el-table-column prop="id" label="消息ID" width="120" />
              <el-table-column prop="groupId" label="群聊ID" width="100" />
              <el-table-column prop="groupName" label="群聊名称" min-width="150" />
              <el-table-column prop="sender" label="发送者" min-width="100">
                <template #default="scope">
                  <div class="sender-info">
                    <el-avatar :size="24" :src="scope.row.senderAvatar" />
                    <span>{{ scope.row.sender }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="content" label="消息内容" min-width="200">
                <template #default="scope">
                  <div class="message-content">
                    <span v-if="scope.row.contentType === 'text'" class="text-content">{{ scope.row.content }}</span>
                    <el-tag v-else-if="scope.row.contentType === 'image'" type="info" class="content-tag">[图片]</el-tag>
                    <el-tag v-else-if="scope.row.contentType === 'file'" type="warning" class="content-tag">[文件]</el-tag>
                    <el-tag v-else type="danger" class="content-tag">[其他]</el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="sendTime" label="发送时间" min-width="160" />
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="handleViewGroupMessageDetail(scope.row)"
                    icon="View"
                  >
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 群聊消息分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="groupMessagePagination.currentPage"
                v-model:page-size="groupMessagePagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="groupMessagePagination.total"
                @size-change="handleGroupMessageSizeChange"
                @current-change="handleGroupMessageCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 用户对话消息 -->
        <el-tab-pane label="用户对话信息" name="user-messages">
          <div class="tab-content">
            <!-- 用户对话搜索栏 -->
            <div class="search-section">
              <el-input
                v-model="userMessageSearch" 
                placeholder="搜索用户对话内容/用户名"
                clearable
                @keyup.enter="handleUserMessageSearch"
                class="search-input"
              >
                <template #append>
                  <el-button @click="handleUserMessageSearch" type="primary" icon="Search">搜索</el-button>
                </template>
              </el-input>
              
              <div class="filter-section">
                <el-select v-model="userMessageFilter.userId" placeholder="选择发送用户" clearable class="filter-select">
                  <el-option
                    v-for="user in usersList"
                    :key="user.id"
                    :label="user.username"
                    :value="user.id"
                  />
                </el-select>
              </div>
            </div>

            <!-- 用户对话列表 -->
            <el-table
              v-loading="userMessagesLoading"
              :data="userMessagesList"
              border
              style="width: 100%"
            >
              <el-table-column prop="id" label="消息ID" width="120" />
              <el-table-column prop="fromUser" label="发送者" min-width="100">
                <template #default="scope">
                  <div class="sender-info">
                    <el-avatar :size="24" :src="scope.row.fromUserAvatar" />
                    <span>{{ scope.row.fromUser }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="toUser" label="接收者" min-width="100">
                <template #default="scope">
                  <div class="sender-info">
                    <el-avatar :size="24" :src="scope.row.toUserAvatar" />
                    <span>{{ scope.row.toUser }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="content" label="消息内容" min-width="200">
                <template #default="scope">
                  <div class="message-content">
                    <span v-if="scope.row.contentType === 'text'" class="text-content">{{ scope.row.content }}</span>
                    <el-tag v-else-if="scope.row.contentType === 'image'" type="info" class="content-tag">[图片]</el-tag>
                    <el-tag v-else-if="scope.row.contentType === 'file'" type="warning" class="content-tag">[文件]</el-tag>
                    <el-tag v-else type="danger" class="content-tag">[其他]</el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="sendTime" label="发送时间" min-width="160" />
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="handleViewUserMessageDetail(scope.row)"
                    icon="View"
                  >
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 用户对话分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="userMessagePagination.currentPage"
                v-model:page-size="userMessagePagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="userMessagePagination.total"
                @size-change="handleUserMessageSizeChange"
                @current-change="handleUserMessageCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 机器人消息 -->
        <el-tab-pane label="机器人交流信息" name="robot-messages">
          <div class="tab-content">
            <!-- 机器人消息搜索栏 -->
            <div class="search-section">
              <el-input
                v-model="robotMessageSearch" 
                placeholder="搜索机器人交流内容/用户名"
                clearable
                @keyup.enter="handleRobotMessageSearch"
                class="search-input"
              >
                <template #append>
                  <el-button @click="handleRobotMessageSearch" type="primary" icon="Search">搜索</el-button>
                </template>
              </el-input>
            </div>

            <!-- 机器人消息列表 -->
            <el-table
              v-loading="robotMessagesLoading"
              :data="robotMessagesList"
              border
              style="width: 100%"
            >
              <el-table-column prop="id" label="消息ID" width="120" />
              <el-table-column prop="userId" label="用户ID" width="100" />
              <el-table-column prop="userName" label="用户" min-width="100">
                <template #default="scope">
                  <div class="sender-info">
                    <el-avatar :size="24" :src="scope.row.userAvatar" />
                    <span>{{ scope.row.userName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="content" label="用户消息" min-width="250">
                <template #default="scope">
                  <span class="text-content">{{ scope.row.content }}</span>
                </template>
              </el-table-column>
<!--              <el-table-column prop="robotResponse" label="机器人回复" min-width="250">
                <template #default="scope">
                  <span class="text-content robot-response">{{ scope.row.robotResponse }}</span>
                </template>
              </el-table-column>-->
              <el-table-column prop="sendTime" label="交互时间" min-width="160" />
              <el-table-column prop="sessionId" label="会话ID" min-width="180" />
              <el-table-column label="操作" width="120">
                <template #default="scope">
                  <el-button
                    type="primary"
                    size="small"
                    @click="handleViewRobotMessageDetail(scope.row)"
                    icon="View"
                  >
                    查看
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 机器人消息分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="robotMessagePagination.currentPage"
                v-model:page-size="robotMessagePagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="robotMessagePagination.total"
                @size-change="handleRobotMessageSizeChange"
                @current-change="handleRobotMessageCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 消息详情对话框 -->
    <el-dialog
      v-model="messageDetailVisible"
      :title="messageDetail.title"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item v-if="messageDetail.id" label="消息ID">{{ messageDetail.id }}</el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.groupName" label="群聊名称">{{ messageDetail.groupName }}</el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.fromUser" label="发送者">{{ messageDetail.fromUser }}</el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.toUser" label="接收者">{{ messageDetail.toUser }}</el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.userName" label="用户">{{ messageDetail.userName }}</el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.content" label="消息内容">{{ messageDetail.content }}</el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.robotResponse" label="机器人回复">{{ messageDetail.robotResponse }}</el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.contentType" label="消息类型">
          <el-tag :type="messageDetail.contentType === 'text' ? 'info' : messageDetail.contentType === 'image' ? 'success' : 'warning'">
            {{ messageDetail.contentType === 'text' ? '文本' : messageDetail.contentType === 'image' ? '图片' : '文件' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.sendTime" label="发送时间">{{ messageDetail.sendTime }}</el-descriptions-item>
        <el-descriptions-item v-if="messageDetail.sessionId" label="会话ID">{{ messageDetail.sessionId }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminApi } from '@/api/admin/index.js'

// 响应式数据
const activeTab = ref('group-messages')
const messageDetailVisible = ref(false)
const messageDetail = ref({})

// 初始化API
const adminApi = useAdminApi()

// 群聊列表（用于筛选）
const groupsList = ref([])

// 用户列表（用于筛选）
const usersList = ref([])

// 群聊消息相关
const groupMessageSearch = ref('')
const groupMessageFilter = reactive({
  groupId: '',
  dateRange: null
})
const groupMessagesLoading = ref(false)
const groupMessagesList = ref([])
const groupMessagePagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 用户对话相关
const userMessageSearch = ref('')
const userMessageFilter = reactive({
  userId: ''
})
const userMessagesLoading = ref(false)
const userMessagesList = ref([])
const userMessagePagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 机器人消息相关
const robotMessageSearch = ref('')
const robotMessagesLoading = ref(false)
const robotMessagesList = ref([])
const robotMessagePagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 初始化数据
onMounted(async () => {
  fetchGroupMessages()
  fetchUserMessages()
  fetchRobotMessages()
  // 加载群聊列表用于筛选
  await fetchGroupsList()
  // 加载用户列表用于筛选
  await fetchUsersList()
})

// 获取群聊列表用于筛选
const fetchGroupsList = async () => {
  try {
    const response = await adminApi.groups.getGroups({ page: 1, pageSize: 100 })
    if (response.code === 200) {
      groupsList.value = response.data.list || []
    }
  } catch (error) {
    console.error('获取群聊列表失败:', error)
  }
}

// 获取用户列表用于筛选
const fetchUsersList = async () => {
  try {
    const response = await adminApi.users.getUsers({ page: 1, pageSize: 100 })
    if (response.code === 200) {
      usersList.value = response.data.list || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 群聊消息相关方法
const fetchGroupMessages = async () => {
  groupMessagesLoading.value = true
  try {
    // 构建请求参数
    const params = {
      page: groupMessagePagination.currentPage,
      pageSize: groupMessagePagination.pageSize,
      keyword: groupMessageSearch.value,
      groupId: groupMessageFilter.groupId,
      startTime: groupMessageFilter.dateRange ? groupMessageFilter.dateRange[0] : null,
      endTime: groupMessageFilter.dateRange ? groupMessageFilter.dateRange[1] : null
    }
    
    // 调用真实API
    const response = await adminApi.messages.getGroupMessages(params)
    
    // 更新数据
    groupMessagesList.value = response.data.list || []
    groupMessagePagination.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('获取群聊消息失败: ' + (error.message || '未知错误'))
    groupMessagesList.value = []
    groupMessagePagination.total = 0
  } finally {
    groupMessagesLoading.value = false
  }
}

const handleGroupMessageSearch = () => {
  groupMessagePagination.currentPage = 1
  fetchGroupMessages()
}

const handleGroupMessageSizeChange = (size) => {
  groupMessagePagination.pageSize = size
  groupMessagePagination.currentPage = 1
  fetchGroupMessages()
}

const handleGroupMessageCurrentChange = (current) => {
  groupMessagePagination.currentPage = current
  fetchGroupMessages()
}

// 用户对话消息相关方法
const fetchUserMessages = async () => {
  userMessagesLoading.value = true
  try {
    // 构建请求参数
    const params = {
      page: userMessagePagination.currentPage,
      pageSize: userMessagePagination.pageSize,
      keyword: userMessageSearch.value,
      userId: userMessageFilter.userId
    }
    
    // 调用真实API
    const response = await adminApi.messages.getUserMessages(params)
    
    // 更新数据
    userMessagesList.value = response.data.list || []
    userMessagePagination.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('获取用户对话消息失败: ' + (error.message || '未知错误'))
    userMessagesList.value = []
    userMessagePagination.total = 0
  } finally {
    userMessagesLoading.value = false
  }
}

const handleUserMessageSearch = () => {
  userMessagePagination.currentPage = 1
  fetchUserMessages()
}

const handleUserMessageSizeChange = (size) => {
  userMessagePagination.pageSize = size
  userMessagePagination.currentPage = 1
  fetchUserMessages()
}

const handleUserMessageCurrentChange = (current) => {
  userMessagePagination.currentPage = current
  fetchUserMessages()
}

// 机器人消息相关方法
const fetchRobotMessages = async () => {
  robotMessagesLoading.value = true
  try {
    // 构建请求参数
    const params = {
      page: robotMessagePagination.currentPage,
      pageSize: robotMessagePagination.pageSize,
      keyword: robotMessageSearch.value
    }
    
    // 调用真实API
    const response = await adminApi.messages.getRobotMessages(params)
    
    // 更新数据
    robotMessagesList.value = response.data.list || []
    robotMessagePagination.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('获取机器人消息失败: ' + (error.message || '未知错误'))
    robotMessagesList.value = []
    robotMessagePagination.total = 0
  } finally {
    robotMessagesLoading.value = false
  }
}

const handleRobotMessageSearch = () => {
  robotMessagePagination.currentPage = 1
  fetchRobotMessages()
}

const handleRobotMessageSizeChange = (size) => {
  robotMessagePagination.pageSize = size
  robotMessagePagination.currentPage = 1
  fetchRobotMessages()
}

const handleRobotMessageCurrentChange = (current) => {
  robotMessagePagination.currentPage = current
  fetchRobotMessages()
}

// 查看消息详情
const handleViewGroupMessageDetail = (msg) => {
  messageDetail.value = {
    title: '群聊消息详情',
    ...msg
  }
  messageDetailVisible.value = true
}

const handleViewUserMessageDetail = (msg) => {
  messageDetail.value = {
    title: '用户对话详情',
    ...msg
  }
  messageDetailVisible.value = true
}

const handleViewRobotMessageDetail = (msg) => {
  messageDetail.value = {
    title: '机器人交流详情',
    ...msg
  }
  messageDetailVisible.value = true
}
</script>

<style scoped lang="scss">
.admin-message-management {
  .page-header {
    margin-bottom: 24px;
    h2 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
    p {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .message-tabs-card {
    margin-bottom: 20px;
  }
  
  .message-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }
  }
  
  .tab-content {
    padding: 0;
  }
  
  .search-section {
    margin-bottom: 20px;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .search-input {
    width: 100%;
    max-width: 600px;
  }
  
  .filter-section {
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  .filter-select {
    width: 200px;
  }
  
  .date-filter {
    width: 300px;
  }
  
  .sender-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .message-content {
    .text-content {
      max-width: 400px;
      word-break: break-all;
      display: inline-block;
      vertical-align: middle;
    }
    
    .content-tag {
      margin-left: 8px;
    }
    
    .robot-response {
      color: #67c23a;
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>