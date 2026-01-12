<script setup>
import { reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserApi } from '@/api/user/index.js'
import { useGroupApi } from '@/api/group/index.js'

const userApi = useUserApi()
const groupApi = useGroupApi()
const state = reactive({
  visible: false,
  loading: false,
  page: {
    total: 0,
    records: []
  },
  pIndex: 1,
  pSize: 10,
  searchParams: {},

  chatId: '',
  groupMembers: [],
  inviteLoading: false
})

// 打开对话框
const openDialog = (chatId) => {
  state.visible = true
  getFriends()
  state.chatId = chatId
  getGroupMember(chatId)
}

// 获取群聊成员
const getGroupMember = async (chatId) => {
  const res = await groupApi.getGroupMembers(chatId)
  state.groupMembers = res.data
}

// 获取好友
const getFriends = async () => {
  try {
    state.loading = true
    const params = { pIndex: state.pIndex, pSize: state.pSize }
    const res = await userApi.getFriends(params, state.searchParams)
    state.page = res.data
  } finally {
    state.loading = false
  }
}

// 处理分页
const handlePageChange = (pageIndex) => {
  state.pIndex = pageIndex
  getFriends()
}

// 处理分页大小
const handleSizeChange = (pageSize) => {
  state.pSize = pageSize
  getFriends()
}

// 判断是否是群聊成员
const isGroupMember = (userId) => {
  return state.groupMembers.indexOf(userId) !== -1
}

// 处理邀请
const handleInvite = async (row) => {
  try {
    state.inviteLoading = true
    const res = await groupApi.addGroupMember({ chatId: state.chatId, friendId: row.id })
    ElMessage.success(res.message)
    getGroupMember(state.chatId)
  } finally {
    state.inviteLoading = false
  }
}

defineExpose({
  openDialog
})
</script>
<template>
  <el-dialog v-model="state.visible" title="邀请好友">
    <el-table
      :data="state.page.records"
      v-loading="state.loading"
      element-loading-text="正在努力加载数据中..."
      empty-text="暂无好友请求"
    >
      <el-table-column prop="avatar" label="头像" min-width="100" align="center">
        <template #default="scope">
          <el-avatar :src="scope.row.avatar" />
        </template>
      </el-table-column>
      <el-table-column prop="username" label="名称" />
      <el-table-column prop="gender" label="性别">
        <template #default="scope">
          <el-tag v-if="scope.row.gender === 0" type="info" effect="plain">未知</el-tag>
          <el-tag v-if="scope.row.gender === 1" type="success" effect="plain">男</el-tag>
          <el-tag v-if="scope.row.gender === 2" type="danger" effect="plain">女</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <template v-if="isGroupMember(scope.row.id)">
            <el-tag type="warning" effect="plain">已加入</el-tag>
          </template>
          <template v-else>
            <el-popconfirm
              :title="`您确定要邀请 ${scope.row.username} 加入群聊吗？`"
              @confirm="handleInvite(scope.row)"
            >
              <template #reference>
                <el-button type="primary" :loading="state.inviteLoading"> 邀请 </el-button>
              </template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 10px"
      :background="true"
      :current-page="state.pIndex"
      :page-size="state.pSize"
      :total="state.page.total"
      :page-sizes="[1, 3, 6, 9, 12, 15, 18]"
      layout="total, sizes, ->, prev, pager, next, jumper"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </el-dialog>
</template>
