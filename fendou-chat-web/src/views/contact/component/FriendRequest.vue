<script setup>
import { reactive } from 'vue'
import { useFriendApi } from '@/api/friend/index.js'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['done'])

const friendApi = useFriendApi()
const state = reactive({
  visible: false,
  loading: false,
  page: {
    total: 0
  },
  pIndex: 1,
  pSize: 10,
  searchParams: {}
})

// 打开对话框
const openDialog = () => {
  state.visible = true
  getFriendRequestList()
}

// 获取好友请求列表
const getFriendRequestList = async () => {
  try {
    state.loading = true
    const params = { pIndex: state.pIndex, pSize: state.pSize }
    const res = await friendApi.getFriendRequests(params, state.searchParams)
    state.page = res.data
  } finally {
    state.loading = false
  }
}

// 处理分页
const handlePageChange = (pageIndex) => {
  state.pIndex = pageIndex
  getFriendRequestList()
}

// 处理分页大小
const handleSizeChange = (pageSize) => {
  state.pSize = pageSize
  getFriendRequestList()
}

// 处理好友请求
const handleFriendRequest = async (id, status) => {
  const params = { requestId: id, status: status }
  const res = await friendApi.respondFriendRequest(params)
  ElMessage.success(res.message)
  getFriendRequestList()
  emit('done')
}

defineExpose({
  openDialog
})
</script>
<template>
  <el-dialog v-model="state.visible" title="好友请求" width="600">
    <el-table
      :data="state.page.records"
      v-loading="state.loading"
      element-loading-text="正在努力加载数据中..."
      empty-text="暂无好友请求"
    >
      <el-table-column prop="senderName" label="申请人" width="120" />
      <el-table-column prop="verifyMsg" label="验证消息" min-width="150">
        <template #default="scope">
          {{ scope.row.verifyMsg || '无' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <template v-if="scope.row.status === 0">
            <el-button
              size="small"
              type="success"
              @click="handleFriendRequest(scope.row.id, 1)"
              :loading="scope.row.processing"
            >
              同意
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleFriendRequest(scope.row.id, 2)"
              :loading="scope.row.processing"
            >
              拒绝
            </el-button>
          </template>
          <template v-else-if="scope.row.status === 1">
            <el-tag type="success" effect="plain">已同意</el-tag>
          </template>
          <template v-else>
            <el-tag type="danger" effect="plain">已拒绝</el-tag>
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
