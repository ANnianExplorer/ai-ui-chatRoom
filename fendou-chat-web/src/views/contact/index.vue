<template>
  <el-card shadow="never">
    <!-- 搜索表单 -->
    <el-form :model="state.searchForm" :inline="true">
      <el-form-item label="名称" prop="chatName">
        <el-input v-model="state.searchForm.chatName" placeholder="请输入名称" clearable />
      </el-form-item>

      <el-form-item label="类型" prop="chatType">
        <el-radio-group v-model="state.searchForm.chatType" @change="fetchChatList">
          <el-radio-button label="全部" :value="0" />
          <el-radio-button label="好友" :value="1" />
          <el-radio-button label="群聊" :value="2" />
        </el-radio-group>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="fetchChatList">
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="resetSearch">
          <el-icon><Refresh /></el-icon> 清空
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <div style="margin-bottom: 15px">
      <el-badge :value="state.friendCount" class="mr10" :max="99">
        <el-button ref="ref2" type="primary" @click="showFriendRequests">好友请求</el-button>
      </el-badge>
      <el-button ref="ref1" type="primary" @click="addFriend">添加好友</el-button>
      <el-button type="primary" @click="createGroup">创建群聊</el-button>
      <el-button @click="state.tourOpen = true">聊天引导</el-button>
      <el-tour v-model="state.tourOpen">
        <el-tour-step :target="ref1?.$el" title="第一步" description="点击添加好友, 输入好友名称或ID, 点击发送请求" />
        <el-tour-step
          :target="ref2?.$el"
          title="第二步"
          description="点击好友请求, 查看收到的所有好友请求, 选择同意双方就是好友了可以进行对话了, 选择拒绝则无法进行对话"
        />
        <el-tour-step :target="ref3?.$el" title="第三步" description="点击聊天, 就可以进行畅快淋漓的聊天了" />
        <template #indicators="{ current, total }">
          <span>{{ current + 1 }} / {{ total }}</span>
        </template>
      </el-tour>
    </div>

    <!-- 联系人表格 -->
    <el-table
      :data="state.page.records"
      v-loading="state.loading"
      element-loading-text="正在努力加载数据中..."
      stripe
      style="width: 100%"
      row-key="chatId"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column type="index" label="序号" width="80" align="center" />
      <el-table-column prop="avatar" label="头像" min-width="100" align="center">
        <template #default="scope">
          <el-image
            style="width: 50px; height: 50px"
            fit="cover"
            :src="scope.row.avatar"
            :preview-src-list="[scope.row.avatar, '']"
            :preview-teleported="true"
          />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="120" sortable />
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.remark }}
          <span v-if="!row.remark" style="color: lightgray">暂无备注</span>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" sortable>
        <template #default="scope">
          <el-tag :type="scope.row.type === 1 ? 'primary' : scope.row.type === 2 ? 'success' : 'info'" effect="plain">
            {{ scope.row.type === 1 ? '好友' : scope.row.type === 2 ? '群聊' : '默认' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="介绍" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.description }}
          <span v-if="!row.description" style="color: lightgray">暂无描述</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" align="center">
        <template #default="{ row }">
          <el-link ref="ref3" type="primary" @click="handleChat(row)"> 聊天 </el-link>
          <el-divider direction="vertical" />
          <el-link v-if="row.type != 0 && row.type != 3" type="success" @click="handleEdit(row)"> 编辑 </el-link>
          <el-divider v-if="row.type != 0 && row.type != 3" direction="vertical" />
          <el-link v-if="row.type != 0 && row.type != 3" @click="handleDetail(row)"> 详情 </el-link>
          <el-divider v-if="row.type != 0 && row.type != 3" direction="vertical" />
          <el-link size="small" type="danger" @click="handleClean(row)"> 清空聊天 </el-link>
          <el-divider v-if="row.type != 0 && row.type != 3" direction="vertical" />
          <el-dropdown v-if="row.type != 0 && row.type != 3" trigger="click">
            <el-link type="primary" style="margin-top: 5px; padding: 3px"> 更多 </el-link>
            <template #dropdown>
              <el-dropdown-item v-if="row.type === 1" @click="handleDeleteFriend(row)">
                <el-link type="danger">删除好友</el-link>
              </el-dropdown-item>
              <el-dropdown-item v-if="row.type === 2" @click="handleInvite(row)">
                <el-link type="primary"> 邀请好友 </el-link>
              </el-dropdown-item>
              <el-dropdown-item v-if="row.type === 2" @click="handleQuitGroup(row)">
                <el-link type="danger"> 退出群聊 </el-link>
              </el-dropdown-item>
            </template>
          </el-dropdown>
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
  </el-card>

  <AddFriendDialog ref="addFriendDialogRef" />

  <AddGroupDialog ref="addGroupDialogRef" @done="fetchChatList" />

  <FriendRequestDialog ref="friendRequestDialogRef" @done="fetchData" />

  <InviteFriend ref="inviteFriendDialogRef" />

  <Edit ref="editDialogRef" @done="fetchChatList" />
</template>

<script setup>
import { ref, reactive, onMounted, defineAsyncComponent } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.js'
import { useChatApi } from '@/api/chat/index.js'
import { useFriendApi } from '@/api/friend/index.js'
import { useGroupApi } from '@/api/group/index.js'

const AddFriendDialog = defineAsyncComponent(() => import('./component/AddFriend.vue'))
const addFriendDialogRef = ref(null)

const AddGroupDialog = defineAsyncComponent(() => import('./component/AddGroup.vue'))
const addGroupDialogRef = ref(null)

const FriendRequestDialog = defineAsyncComponent(() => import('./component/FriendRequest.vue'))
const friendRequestDialogRef = ref(null)

const InviteFriend = defineAsyncComponent(() => import('./component/InviteFriend.vue'))
const inviteFriendDialogRef = ref(null)

const Edit = defineAsyncComponent(() => import('./component/Edit.vue'))
const editDialogRef = ref(null)

const router = useRouter()
const userStore = useUserStore()
const chatApi = useChatApi()
const friendApi = useFriendApi()
const groupApi = useGroupApi()

const ref1 = ref()
const ref2 = ref()
const ref3 = ref()

// 响应式数据
const state = reactive({
  loading: false,
  searchForm: {
    chatName: '',
    chatType: 0
  },
  page: {
    total: 0
  },
  pIndex: 1,
  pSize: 10,
  selectionIds: [],
  tourOpen: false,
  friendCount: 0
})

// 获取表格数据
const fetchChatList = async () => {
  state.loading = true
  try {
    const params = { pIndex: state.pIndex, pSize: state.pSize }
    const res = await chatApi.getChats(params, state.searchForm)
    state.page = res.data
  } finally {
    state.loading = false
  }
}

// 重置搜索
const resetSearch = () => {
  state.searchForm = {
    chatName: '',
    chatType: 0
  }
  fetchChatList()
}

// 多选处理
const handleSelectionChange = (val) => {
  state.selectionIds = val.map((item) => item.id)
}

// 分页处理
const handlePageChange = (val) => {
  state.pIndex = val
  fetchChatList()
}

// 分页大小处理
const handleSizeChange = (val) => {
  state.pSize = val
  fetchChatList()
}

// 聊天处理
const handleChat = (row) => {
  router.push(`/index?chatId=${row.chatId}`)
}

// 修改处理
const handleEdit = (row) => {
  editDialogRef.value.openDialog(row)
}

// 详情处理
const handleDetail = (row) => {
  router.push(`/contact/detail/${row.chatId}`)
}

// 清空聊天记录处理
const handleClean = (row) => {
  ElMessageBox.confirm(`您确定要将与 ${row.name} 的聊天记录清空吗？`, '清空聊天记录', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const res = await chatApi.cleanChat(row.chatId)
    ElMessage.success(res.message)
  })
}

// 删除好友处理
const handleDeleteFriend = (row) => {
  ElMessageBox.confirm(`您确定要删除 ${row.name} 吗？`, '删除好友', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const arr = row.chatId.split('-')
    let userId = arr[1]
    if (userId == userStore.user.id) {
      userId = row.chatId.split('-')[2]
    }
    const res = await friendApi.deleteFriend(userId)
    ElMessage.success(res.message)
    fetchChatList()
  })
}

// 退出群聊处理
const handleQuitGroup = (row) => {
  ElMessageBox.confirm(`您确定要退出 ${row.name} 吗？`, '退出群聊', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const groupId = row.chatId.split('-')[1]
    const res = await groupApi.quitGroup(groupId)
    ElMessage.success(res.message)
    fetchChatList()
  })
}

// 添加好友
const addFriend = () => {
  addFriendDialogRef.value.openDialog()
}

// 创建群聊
const createGroup = () => {
  addGroupDialogRef.value.openDialog()
}

// 好友请求
const showFriendRequests = async () => {
  friendRequestDialogRef.value.openDialog()
}

// 邀请好友
const handleInvite = async (row) => {
  inviteFriendDialogRef.value.openDialog(row.chatId)
}

// 获取好友请求数
const getFriendCount = async () => {
  const res = await friendApi.getFriendCount()
  state.friendCount = res.data
}

// 获取数据
const fetchData = async () => {
  await getFriendCount()
  await fetchChatList()
}

// 组件挂载时获取数据
onMounted(() => {
  getFriendCount()
  fetchChatList()
})
</script>
