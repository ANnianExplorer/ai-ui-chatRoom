<template>
  <div class="admin-group-management">
    <div class="page-header">
      <h2>群聊管理</h2>
      <p>管理系统群聊，包括查看群聊信息、群聊创建人和成员信息</p>
    </div>

    <!-- 搜索和操作栏 -->
    <div class="operation-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="群聊名称/群聊ID/创建者"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button @click="handleSearch" type="primary" icon="Search">搜索</el-button>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="群聊状态">
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

    <!-- 群聊列表表格 -->
    <el-card shadow="hover" class="group-table-card">
      <el-table
        v-loading="loading"
        :data="groupsList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="群聊ID" width="120" />
        <el-table-column prop="name" label="群聊名称" min-width="180">
          <template #default="scope">
            <div class="group-info">
              <el-avatar :size="32" :src="scope.row.avatar" class="group-avatar" />
              <span class="group-name">{{ scope.row.name }}</span>
              <el-tag v-if="scope.row.isOfficial" type="primary" size="small" class="official-tag">官方</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建者" min-width="120">
          <template #default="scope">
            <div class="creator-info">
              <el-avatar :size="24" :src="scope.row.creatorAvatar" />
              <span>{{ scope.row.creator }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="成员数量" width="100">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.memberCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160" />
        <el-table-column prop="lastMessageTime" label="最后消息" min-width="160">
          <template #default="scope">
            <span class="last-message-time">{{ scope.row.lastMessageTime || '暂无消息' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="scope.row.status === 1 ? 'success' : 'danger'"
            >
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleViewGroup(scope.row)"
              icon="View"
            >
              查看详情
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

    <!-- 群聊详情对话框 -->
    <el-dialog
      v-model="groupDetailVisible"
      title="群聊详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="群聊ID">{{ selectedGroup.id }}</el-descriptions-item>
        <el-descriptions-item label="群聊名称">{{ selectedGroup.name }}</el-descriptions-item>
        <el-descriptions-item label="创建者">
          <div class="creator-detail">
            <el-avatar :size="32" :src="selectedGroup.creatorAvatar" />
            <span>{{ selectedGroup.creator }}</span>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="成员数量">{{ selectedGroup.memberCount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ selectedGroup.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="最后消息时间">{{ selectedGroup.lastMessageTime || '暂无消息' }}</el-descriptions-item>
        <el-descriptions-item label="群聊状态">
          <el-tag
            :type="selectedGroup.status === 1 ? 'success' : 'danger'"
          >
            {{ selectedGroup.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="是否官方群">
          <el-tag :type="selectedGroup.isOfficial ? 'primary' : 'info'">
            {{ selectedGroup.isOfficial ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="群聊简介" span="2">
          {{ selectedGroup.description || '暂无简介' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 群聊成员列表 -->
      <div class="group-members-section">
        <div class="members-header">
          <h3>群聊成员 ({{ filteredMembers.length }})</h3>
          <el-input
            v-model="memberSearchKeyword"
            placeholder="搜索用户名"
            clearable
            class="member-search-input"
          >
            <template #append>
              <el-button @click="handleMemberSearch" type="primary" icon="Search">搜索</el-button>
            </template>
          </el-input>
        </div>
        <el-table
          :data="filteredMembers"
          border
          style="width: 100%"
          stripe
        >
          <el-table-column prop="id" label="用户ID" width="120" />
          <el-table-column prop="username" label="用户名" min-width="150">
            <template #default="scope">
              <div class="member-info">
                <el-avatar :size="24" :src="scope.row.avatar" />
                <span>{{ scope.row.username }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.role === 'owner' ? 'primary' : 'info'">
                {{ scope.row.role === 'owner' ? '群主' : '成员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="joinTime" label="加入时间" min-width="160" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminApi } from '@/api/admin/index.js'

// 响应式数据
const loading = ref(false)
const groupsList = ref([])
const searchForm = reactive({
  keyword: '',
  status: ''
})
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})
const selectedGroups = ref([])
const groupDetailVisible = ref(false)
const selectedGroup = ref({})
const memberSearchKeyword = ref('')

// 计算属性：过滤后的成员列表
const filteredMembers = computed(() => {
  if (!memberSearchKeyword.value) {
    return selectedGroup.value.members || []
  }
  return (selectedGroup.value.members || []).filter(member => {
    return member.username && member.username.toLowerCase().includes(memberSearchKeyword.value.toLowerCase())
  })
})

// 成员搜索方法
const handleMemberSearch = () => {
  // 搜索逻辑已通过计算属性实现，这里可以添加额外的处理
}

// 初始化API
const adminApi = useAdminApi()

// 初始化数据
onMounted(() => {
  fetchGroups()
})

// 获取群聊列表
const fetchGroups = async () => {
  loading.value = true
  try {
    // 构建请求参数
    const params = {
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    }
    
    // 调用真实API
    const response = await adminApi.groups.getGroups(params)
    
    // 更新数据
    groupsList.value = response.data.list || []
    pagination.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('获取群聊列表失败: ' + (error.message || '未知错误'))
    groupsList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  fetchGroups()
}

// 刷新
const handleRefresh = () => {
  // 重置搜索条件
  searchForm.keyword = ''
  searchForm.status = ''
  pagination.currentPage = 1
  fetchGroups()
}

// 切换群聊状态
const handleToggleStatus = async (group) => {
  const newStatus = group.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'
  
  try {
    await ElMessageBox.confirm(`确定要${actionText}群聊 "${group.name}" 吗?`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用真实API
    await adminApi.groups.updateGroupStatus(group.id, newStatus)
    
    // 刷新列表
    fetchGroups()
    ElMessage.success(`${actionText}成功`)
  } catch (error) {
    // 取消操作或API调用失败
    if (error !== 'cancel') {
      ElMessage.error(`${actionText}失败: ` + (error.message || '未知错误'))
    }
  }
}

// 查看群聊详情
const handleViewGroup = async (group) => {
  try {
    // 调用API获取群聊详情
    const groupDetail = await adminApi.groups.getGroupById(group.id)
    
    // 调用API获取群聊成员
    const membersResponse = await adminApi.groups.getGroupMembers(group.id)
    
    // 更新选中的群聊信息
    selectedGroup.value = {
      ...groupDetail.data,
      members: membersResponse.data || []
    }
    
    // 显示对话框
    groupDetailVisible.value = true
  } catch (error) {
    ElMessage.error('获取群聊详情失败: ' + (error.message || '未知错误'))
  }
}

// 选择群聊
const handleSelectionChange = (selection) => {
  selectedGroups.value = selection
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  fetchGroups()
}

// 页码变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  fetchGroups()
}
</script>

<style scoped lang="scss">
.admin-group-management {
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
  
  .group-table-card {
    margin-bottom: 20px;
    
    .group-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .group-avatar {
      margin-right: 8px;
    }
    
    .group-name {
      font-weight: 500;
    }
    
    .official-tag {
      margin-left: 8px;
    }
    
    .creator-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .last-message-time {
      color: #909399;
      font-size: 13px;
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
  
  .group-members-section {
    margin-top: 24px;
    
    .members-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
      
      .member-search-input {
        width: 200px;
      }
    }
    
    .creator-detail {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    
    .member-info {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}
</style>