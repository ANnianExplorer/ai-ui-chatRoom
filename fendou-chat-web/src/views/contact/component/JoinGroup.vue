<script setup>
import { reactive, ref } from 'vue'
import { useGroupApi } from '@/api/group/index.js'
import { useAdminApi } from '@/api/admin/index.js'
import { useUserStore } from '@/stores/user.js'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['done'])
const groupApi = useGroupApi()
const adminApi = useAdminApi()
const userStore = useUserStore()
// 状态管理
const state = reactive({
  visible: false,
  loading: false,
  groupList: [],
  searchKeyword: '',
  pagination: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  selectedGroup: null,
  verifyMsg: ''
})

// 存储群聊关系状态: { groupId: isInGroup }
const groupStatusMap = ref({})

// 检查用户是否已经在群聊中
const checkIfInGroup = async (groupId) => {
  try {
    const res = await groupApi.verifyIFExit(groupId)
    groupStatusMap.value[groupId] = res.data
    return res.data
  } catch (error) {
    console.error('检查群聊关系失败:', error)
    return false
  }
}

// 批量检查群聊关系
const checkGroupStatuses = async (groupList) => {
  const statusMap = {}
  for (const group of groupList) {
    try {
      const res = await groupApi.verifyIFExit(group.id)
      statusMap[group.id] = res.data
    } catch (error) {
      console.error(`检查群聊 ${group.id} 关系失败:`, error)
      statusMap[group.id] = false
    }
  }
  groupStatusMap.value = statusMap
}

// 打开弹窗
const openDialog = () => {
  state.visible = true
  fetchGroupList()
}

// 关闭弹窗
const closeDialog = () => {
  state.selectedGroup = null
  state.verifyMsg = ''
  state.searchKeyword = ''
  state.pagination.currentPage = 1
}

// 获取群聊列表
const fetchGroupList = async () => {
  state.loading = true
  try {
    const params = {
      page: state.pagination.currentPage,
      pageSize: state.pagination.pageSize,
      keyword: state.searchKeyword
    }
    const res = await adminApi.groups.getGroups(params)
    state.groupList = res.data.list || []
    state.pagination.total = res.data.total || 0
    // 批量检查群聊关系
    await checkGroupStatuses(state.groupList)
  } catch (error) {
    ElMessage.error('获取群聊列表失败')
    console.error('获取群聊列表失败:', error)
  } finally {
    state.loading = false
  }
}

// 搜索群聊
const handleSearch = () => {
  state.pagination.currentPage = 1
  fetchGroupList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  state.pagination.pageSize = size
  state.pagination.currentPage = 1
  fetchGroupList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  state.pagination.currentPage = current
  fetchGroupList()
}

// 选择群聊
const selectGroup = (group) => {
  state.selectedGroup = group
}

// 加入群聊
const joinGroup = async () => {
  if (!state.selectedGroup) {
    ElMessage.warning('请选择一个群聊')
    return
  }
  
  state.loading = true
  try {
    // 构建请求参数，按照后端要求的格式
    const chatId = `group-${state.selectedGroup.id}`
    // 获取当前登录用户的ID
    const friendId = userStore.user?.id || 0
    
    // 使用params而不是data发送请求参数
    const res = await groupApi.joinGroup({
      chatId,
      friendId
    })
    ElMessage.success(res.message)
    state.visible = false
    emit('done')
  } catch (error) {
    console.error('加入群聊失败:', error)
    ElMessage.error(error.response?.data?.message || '加入群聊失败')
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
  <el-dialog v-model="state.visible" title="加入群聊" width="600px" @close="closeDialog">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-input
        v-model="state.searchKeyword"
        placeholder="搜索群聊名称或ID"
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #append>
          <el-button @click="handleSearch" type="primary" icon="Search">搜索</el-button>
        </template>
      </el-input>
    </div>
    
    <!-- 群聊列表 -->
    <div class="group-list-section">
      <el-table
        v-loading="state.loading"
        :data="state.groupList"
        @row-click="selectGroup"
        :row-class-name="(row) => row.id === state.selectedGroup?.id ? 'selected-row' : ''"
        style="width: 100%"
      >
        <el-table-column prop="id" label="群聊ID" width="80" />
        <el-table-column prop="name" label="群聊名称" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              v-if="groupStatusMap[scope.row.id]"
              type="success"
              size="small"
              disabled
            >
              已加入
            </el-button>
            <el-button
              v-else
              type="primary"
              size="small"
              @click="selectGroup(scope.row)"
            >
              选择该群聊
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
    <div v-if="state.selectedGroup" class="verify-section">
      <el-form-item label="验证消息" class="verify-form-item">
        <el-input
          v-model="state.verifyMsg"
          type="textarea"
          placeholder="请输入验证消息（可选）"
          rows="3"
        />
      </el-form-item>
      <!-- 显示选中的群聊 -->
      <div class="selected-group-info">
        <span class="selected-group-label">正在加入：</span>
        <span class="selected-group-name">{{ state.selectedGroup.name }}</span>
      </div>
    </div>
    
    <!-- 底部按钮 -->
    <template #footer>
      <el-button @click="state.visible = false">取消</el-button>
      <el-button 
        type="primary" 
        @click="joinGroup" 
        :loading="state.loading"
        :disabled="!state.selectedGroup"
      >
        确认加入
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

.group-list-section {
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

  .selected-group-info {
    display: flex;
    align-items: center;
    margin-top: 10px;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;
    font-size: 14px;
  }

  .selected-group-label {
    font-weight: 500;
    color: #606266;
    margin-right: 8px;
  }

  .selected-group-name {
    font-weight: 600;
    color: #303133;
  }
</style>