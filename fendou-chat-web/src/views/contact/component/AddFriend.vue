<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useFriendApi } from '@/api/friend/index.js'
import { useAdminApi } from '@/api/admin/index.js'
import { useUserStore } from '@/stores/user.js'
import { ElMessage } from 'element-plus'

const friendApi = useFriendApi()
const adminApi = useAdminApi()
const userStore = useUserStore()
const friendFormRef = ref(null)

// 状态管理
const state = reactive({
  visible: false,
  loading: false,
  userList: [],
  searchKeyword: '',
  pagination: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  selectedUser: null,
  verifyMsg: ''
})

// 存储好友关系状态: { userId: isFriend }
const friendStatusMap = ref({})

// 检查用户是否已经是好友
const checkIfFriend = async (userId) => {
  try {
    const res = await friendApi.verifyIFExit(userId)
    friendStatusMap.value[userId] = res.data
    return res.data
  } catch (error) {
    console.error('检查好友关系失败:', error)
    return false
  }
}

// 批量检查好友关系
const checkFriendStatuses = async (userList) => {
  const statusMap = {}
  for (const user of userList) {
    // 特殊处理：机器人用户（ID=0）和当前用户自己不能添加为好友
    if (user.id === 0 || user.id === userStore.user?.id) {
      statusMap[user.id] = true
      continue
    }
    
    try {
      const res = await friendApi.verifyIFExit(user.id)
      statusMap[user.id] = res.data
    } catch (error) {
      console.error(`检查用户 ${user.id} 好友关系失败:`, error)
      statusMap[user.id] = false
    }
  }
  friendStatusMap.value = statusMap
}

// 打开弹窗
const openDialog = () => {
  state.visible = true
  fetchUserList()
}

// 关闭弹窗
const closeDialog = () => {
  state.selectedUser = null
  state.verifyMsg = ''
  state.searchKeyword = ''
  state.pagination.currentPage = 1
}

// 获取用户列表
const fetchUserList = async () => {
  state.loading = true
  try {
    const params = {
      page: state.pagination.currentPage,
      pageSize: state.pagination.pageSize,
      keyword: state.searchKeyword
    }
    const res = await adminApi.users.getUsers(params)
    state.userList = res.data.list || []
    state.pagination.total = res.data.total || 0
    // 批量检查好友关系
    await checkFriendStatuses(state.userList)
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error('获取用户列表失败:', error)
  } finally {
    state.loading = false
  }
}

// 搜索用户
const handleSearch = () => {
  state.pagination.currentPage = 1
  fetchUserList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  state.pagination.pageSize = size
  state.pagination.currentPage = 1
  fetchUserList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  state.pagination.currentPage = current
  fetchUserList()
}

// 选择用户
const selectUser = (user) => {
  state.selectedUser = user
}

// 发送好友请求
const sendFriendRequest = async () => {
  if (!state.selectedUser) {
    ElMessage.warning('请选择一个用户')
    return
  }
  
  state.loading = true
  try {
    const data = {
      friendInfo: state.selectedUser.username || state.selectedUser.id,
      verifyMsg: state.verifyMsg
    }
    const res = await friendApi.addFriend(data)
    ElMessage.success(res.message)
    state.visible = false
  } catch (error) {
    console.error('发送好友请求失败:', error)
    ElMessage.error(error.response?.data?.message || '已提出好友申请，请耐心等侯')
  } finally {
    state.loading = false
  }
}

// 暴露方法
defineExpose({
  openDialog
})
</script>

<template>
  <el-dialog v-model="state.visible" title="添加好友" width="600px" @close="closeDialog">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-input
        v-model="state.searchKeyword"
        placeholder="搜索用户名或ID"
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #append>
          <el-button @click="handleSearch" type="primary" icon="Search">搜索</el-button>
        </template>
      </el-input>
    </div>
    
    <!-- 用户列表 -->
    <div class="user-list-section">
      <el-table
        v-loading="state.loading"
        :data="state.userList"
        @row-click="selectUser"
        :row-class-name="(row) => row.id === state.selectedUser?.id ? 'selected-row' : ''"
        style="width: 100%"
      >
        <el-table-column prop="id" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              v-if="friendStatusMap[scope.row.id]"
              type="success"
              size="small"
              disabled
            >
              已添加
            </el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              @click="selectUser(scope.row)"
            >
              添加该用户
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="state.pagination.currentPage"
          v-model:page-size="state.pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="state.pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 验证消息 -->
    <div v-if="state.selectedUser" class="verify-section">
      <el-form-item label="验证消息" class="verify-form-item">
        <el-input
          v-model="state.verifyMsg"
          type="textarea"
          placeholder="请输入验证消息（可选）"
          rows="3"
        />
      </el-form-item>
      <!-- 显示选中的用户 -->
      <div class="selected-user-info">
        <span class="selected-user-label">正在添加：</span>
        <span class="selected-user-name">{{ state.selectedUser.username }}</span>
      </div>
    </div>
    
    <!-- 底部按钮 -->
    <template #footer>
      <el-button @click="state.visible = false">取消</el-button>
      <el-button 
        type="primary" 
        @click="sendFriendRequest" 
        :loading="state.loading"
        :disabled="!state.selectedUser"
      >
        确认添加
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.search-section {
  margin-bottom: 20px;
}

.search-input {
  width: 100%;
}

.user-list-section {
  margin-bottom: 20px;
  max-height: 400px;
  overflow-y: auto;
}

.selected-row {
  background-color: #f5f7fa;
}

.pagination-container {
  margin-top: 10px;
  text-align: right;
}

.verify-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.verify-form-item {
    margin-bottom: 15px;
  }

  .selected-user-info {
    display: flex;
    align-items: center;
    margin-top: 10px;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    font-size: 14px;
  }

  .selected-user-label {
    font-weight: 500;
    color: #606266;
    margin-right: 8px;
  }

  .selected-user-name {
    font-weight: 600;
    color: #303133;
  }
</style>
