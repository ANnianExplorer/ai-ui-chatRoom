<template>
  <div class="admin-user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <p>管理系统用户，包括搜索、查看和修改用户状态</p>
    </div>

    <!-- 搜索和操作栏 -->
    <div class="operation-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/邮箱/手机号"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch" type="primary" icon="Search">搜索</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="用户状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable @change="handleSearch">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <div class="action-buttons">
        <el-button type="primary" @click="handleRefresh" icon="RefreshRight">刷新</el-button>
      </div>
    </div>

    <!-- 用户列表表格 -->
    <el-card shadow="hover" class="user-table-card">
      <el-table
        v-loading="loading"
        :data="usersList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" min-width="120">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :size="28" :src="scope.row.avatar" class="user-avatar" />
              <span class="user-name">{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="真实姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="createTime" label="注册时间" min-width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'danger'"
            >
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.salt === 'admin' ? 'primary' : 'info'">
              {{ scope.row.salt === 'admin' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleViewUser(scope.row)"
              icon="View"
            >
              查看
            </el-button>
            <el-button
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              size="small"
              @click="handleToggleStatus(scope.row)"
              :icon="scope.row.status === 1 ? 'Delete' : 'Check'"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="userDetailVisible"
      title="用户详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户ID">{{ selectedUser.id }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ selectedUser.realName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ selectedUser.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ selectedUser.createTime }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag
            :type="selectedUser.status === 1 ? 'success' : 'danger'"
          >
            {{ selectedUser.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="selectedUser.salt === 'admin' ? 'primary' : 'info'">
            {{ selectedUser.salt === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="头像" span="2">
          <el-avatar :size="80" :src="selectedUser.avatar" />
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminApi } from '@/api/admin'

// 初始化API
const adminApi = useAdminApi()

// 响应式数据
const loading = ref(false)
const usersList = ref([])
const searchForm = reactive({
  keyword: '',
  status: ''
})
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})
const selectedUsers = ref([])
const userDetailVisible = ref(false)
const selectedUser = ref({})

// 初始化数据
onMounted(() => {
  fetchUsers()
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    // 调用真实API获取用户列表
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    }
    
    const response = await adminApi.users.getUsers(params)
    if (response.code === 200) {
      usersList.value = response.data.list || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取用户列表失败')
      usersList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败，请稍后重试')
    usersList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchUsers()
}

// 刷新
const handleRefresh = () => {
  // 重置搜索条件
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.currentPage = 1
  fetchUsers()
}

// 切换用户状态
const handleToggleStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户 ${user.username} 吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用API更新用户状态
    const response = await adminApi.users.updateUserStatus(user.id, newStatus)
    if (response.code === 200) {
      ElMessage.success(`${actionText}成功`)
      fetchUsers() // 刷新用户列表
    } else {
      ElMessage.error(response.message || `${actionText}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`${actionText}用户失败:`, error)
      ElMessage.error(`${actionText}用户失败，请稍后重试`)
    }
    // 取消操作不处理
  }
}

// 查看用户详情
const handleViewUser = async (user) => {
  try {
    // 调用API获取用户详情
    const response = await adminApi.users.getUserById(user.id)
    if (response.code === 200) {
      selectedUser.value = response.data || {}
      userDetailVisible.value = true
    } else {
      ElMessage.error(response.message || '获取用户详情失败')
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败，请稍后重试')
  }
}

// 选择用户
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchUsers()
}

// 页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  fetchUsers()
}
</script>

<style scoped lang="scss">
.admin-user-management {
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
  
  .operation-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .action-buttons {
      display: flex;
      gap: 12px;
    }
  }
  
  .user-table-card {
    margin-bottom: 20px;
    overflow-x: auto; /* 添加横向滚动支持 */
    
    /* 确保表格宽度足够，不折叠 */
    :deep(.el-table) {
      min-width: 1200px; /* 设置最小宽度，确保所有列能显示 */
      width: 100%; /* 确保表格宽度填满容器 */
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .user-avatar {
      margin-right: 8px;
    }
    
    .user-name {
      font-weight: 500;
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>