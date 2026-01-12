<template>
  <el-row>
    <el-col :md="6" :lg="8" :xl="8"></el-col>
    <el-col :xs="24" :sm="24" :md="12" :lg="8" :xl="8">
      <el-empty v-if="!state.userData && !state.groupData"></el-empty>
      <el-card header="好友信息" shadow="hover" v-if="state.userData">
        <el-row justify="center" class="detail-avatar">
          <el-avatar :src="state.userData.avatar" :size="100" />
        </el-row>

        <el-descriptions :column="1" border>
          <el-descriptions-item label-align="right" min-width="150" label="用户昵称">{{
            state.userData.realName
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="用户性别">
            <el-tag type="info" effect="plain" v-if="state.userData.gender === 0">未知</el-tag>
            <el-tag type="success" effect="plain" v-else-if="state.userData.gender === 1">男</el-tag>
            <el-tag type="danger" effect="plain" v-else-if="state.userData.gender === 2">女</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="出生日期">{{
            state.userData.birthday
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="电子邮箱">{{
            state.userData.email
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="电话号码">{{
            state.userData.phone
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="所在地区">{{
            state.userData.address
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="个人介绍">{{
            state.userData.description || '这个人很懒什么都没留下。'
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="加入时间">{{
            state.userData.createTime
          }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-action">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="handleEdit">编辑信息</el-button>
          <el-button type="success" @click="sendMessage">发消息</el-button>
          <el-button type="danger" @click="deleteFriend(state.userData.id)">删除好友</el-button>
        </div>
      </el-card>

      <el-card header="群聊信息" v-if="state.groupData">
        <el-row justify="center" class="detail-avatar">
          <el-upload
            v-if="userStore.user && userStore.user.id === state.groupData.createBy"
            :action="state.fileUploadApi"
            accept="image/*"
            :show-file-list="false"
            :headers="state.fileHeaders"
            :on-success="handleAvatarSuccess"
          >
            <el-avatar :src="state.groupData.avatar" :size="100" />
          </el-upload>
          <el-avatar v-else :src="state.groupData.avatar" :size="100" />
        </el-row>

        <el-descriptions :column="1" border>
          <el-descriptions-item label-align="right" min-width="150" label="群聊名称">{{
            state.groupData.name || '未知群聊'
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="群聊备注">{{
            state.groupData.remark || '无'
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="群聊成员">
            <template v-if="state.groupData.members && state.groupData.members.length > 0">
              <span v-for="member in state.groupData.members" :key="member.id">{{ member.realName || member.username || '未知用户' }} </span>
            </template>
            <template v-else-if="state.groupData.memberCount">
              {{ state.groupData.memberCount }}人
            </template>
            <template v-else>
              未知人数
            </template>
          </el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="群聊创建人">{{
            state.groupData.createBy || '未知'
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="群聊介绍">{{
            state.groupData.description || '暂无介绍'
          }}</el-descriptions-item>
          <el-descriptions-item label-align="right" min-width="150" label="群聊创建时间">{{
            state.groupData.createTime || state.groupData.create_time || '未知'
          }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-action">
          <el-button @click="goBack">返回</el-button>
          <el-button type="success" @click="sendMessage">发消息</el-button>
          <el-button type="warning" @click="quitGroup(state.groupData.id)">退出群聊</el-button>
          <el-button
            v-if="userStore.user && userStore.user.id === state.groupData.createBy"
            type="danger"
            @click="deleteGroup(state.groupData.id)"
          >
            解散群聊
          </el-button>
        </div>
      </el-card>
    </el-col>
    <el-col :md="6" :lg="8" :xl="8"></el-col>
  </el-row>
  
  <!-- 编辑对话框组件 -->
  <EditDialog ref="editDialogRef" @done="fetchDetailData" />
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user.js'
import { useUserApi } from '@/api/user/index.js'
import { useGroupApi } from '@/api/group/index.js'
import { useFriendApi } from '@/api/friend/index.js'
// 引入编辑对话框组件
import EditDialog from './component/Edit.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 从路由参数中提取 chatId
const chatId = computed(() => route.params.chatId)

const userApi = useUserApi()
const groupApi = useGroupApi()
const friendApi = useFriendApi()

const state = reactive({
  fileUploadApi: import.meta.env.VITE_APP_API_URL + 'file/upload',
  fileHeaders: {
    Authorization: 'Bearer ' + sessionStorage.getItem('token')
  },
  userData: null,
  groupData: null
})

// 编辑对话框引用
const editDialogRef = ref(null)

// 获取详情数据
const fetchDetailData = async () => {
  if (!chatId.value) return
  // 根据 chatId 判断是用户还是群组
  if (chatId.value.startsWith('user-')) {
    let arr = chatId.value.split('-')
    let userId = arr[1]
    if (userStore.user.id == userId) {
      userId = arr[2]
    }
    const res = await userApi.getUserById(userId)
    state.userData = res.data
  } else if (chatId.value.startsWith('group-')) {
    const groupId = chatId.value.split('-')[1]
    const res = await groupApi.getGroupById(groupId)
    console.log('群聊API返回数据:', res)
    state.groupData = res.data
  }
}

// 修改头像
const handleAvatarSuccess = (res, file) => {
  if (res.code === 200) {
    state.groupData.avatar = res.data.filePath
    groupApi.updateGroup({ id: state.groupData.id, avatar: res.data.filePath })
  } else {
    ElMessage.error(res.message || '头像上传失败')
  }
}

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 发送消息
const sendMessage = () => {
  router.push(`/index?chatId=${chatId.value}`)
}

// 编辑用户信息
const handleEdit = () => {
  if (state.userData) {
    // 构造聊天项对象，符合EditDialog的期望格式
    const chatItem = {
      chatId: `user-${state.userData.id}-${userStore.user.id}`,
      remark: state.userData.remark || '',
      name: state.userData.realName
    }
    editDialogRef.value.openDialog(chatItem)
  } else if (state.groupData) {
    // 构造群聊项对象
    const chatItem = {
      chatId: `group-${state.groupData.id}`,
      remark: state.groupData.remark || '',
      name: state.groupData.name
    }
    editDialogRef.value.openDialog(chatItem)
  }
}

// 删除好友
const deleteFriend = (friendId) => {
  ElMessageBox.confirm('你确定要删除该好友吗？', '删除好友', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning',
    center: true,
    closeOnClickModal: false,
    confirmButtonClass: 'custom-confirm-btn',
    cancelButtonClass: 'custom-cancel-btn',
    boxClass: 'beautiful-message-box'
  }).then(async () => {
    const res = await friendApi.deleteFriend(friendId)
    ElMessage.success(res.message)
    router.push('/index')
  }).catch(() => {
    // 取消删除，不做任何操作
  })
}

// 退出群聊
const quitGroup = (groupId) => {
  ElMessageBox.confirm('你确定要退出该群聊吗？', '退出群聊', {
    confirmButtonText: '确认退出',
    cancelButtonText: '取消',
    type: 'warning',
    center: true,
    closeOnClickModal: false,
    confirmButtonClass: 'custom-confirm-btn',
    cancelButtonClass: 'custom-cancel-btn',
    boxClass: 'beautiful-message-box'
  }).then(async () => {
    const res = await groupApi.quitGroup(groupId)
    ElMessage.success(res.message)
    router.push('/index')
  }).catch(() => {
    // 取消退出，不做任何操作
  })
}

// 解散群聊
const deleteGroup = (groupId) => {
  ElMessageBox.confirm('你确定要解散该群聊吗？此操作不可恢复！', '解散群聊', {
    confirmButtonText: '确认解散',
    cancelButtonText: '取消',
    type: 'error',
    center: true,
    closeOnClickModal: false,
    confirmButtonClass: 'custom-danger-btn',
    cancelButtonClass: 'custom-cancel-btn',
    boxClass: 'beautiful-message-box'
  }).then(async () => {
    await groupApi.deleteGroup(groupId)
    ElMessage.success('群聊已解散')
    router.push('/index')
  }).catch(() => {
    // 取消解散，不做任何操作
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchDetailData()
})
</script>

<style scoped>
.detail-avatar {
  margin-bottom: 15px;
}

.detail-action {
  text-align: center;
  margin-top: 15px;
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* 按钮样式优化 */
.detail-action .el-button {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
}

.detail-action .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.detail-action .el-button:active {
  transform: translateY(0);
}

.detail-action .el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.detail-action .el-button--primary:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #89cfff 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.detail-action .el-button--success {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: white;
}

.detail-action .el-button--success:hover {
  background: linear-gradient(135deg, #85ce61 0%, #a0d683 100%);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

.detail-action .el-button--danger {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  color: white;
}

.detail-action .el-button--danger:hover {
  background: linear-gradient(135deg, #f78989 0%, #f9a6a6 100%);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}
</style>

<!-- 全局样式，用于美化MessageBox弹窗 -->
<style>
/* 自定义MessageBox样式 */
.beautiful-message-box {
  animation: messageBoxFadeIn 0.3s ease-out;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

/* MessageBox淡入动画 */
@keyframes messageBoxFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 自定义按钮样式 */
.custom-confirm-btn,
.custom-danger-btn,
.custom-cancel-btn {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
}

/* 确认按钮样式 */
.custom-confirm-btn {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.custom-confirm-btn:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #89cfff 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

/* 危险按钮样式 */
.custom-danger-btn {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  color: white;
}

.custom-danger-btn:hover {
  background: linear-gradient(135deg, #f78989 0%, #f9a6a6 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

/* 取消按钮样式 */
.custom-cancel-btn {
  background-color: #f0f2f5;
  color: #606266;
}

.custom-cancel-btn:hover {
  background-color: #e6e8eb;
  color: #303133;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
