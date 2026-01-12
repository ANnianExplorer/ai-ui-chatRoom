<script setup>
import { reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user.js'
import { useFriendApi } from '@/api/friend/index.js'
import { ElMessage } from 'element-plus'
import { useGroupApi } from '@/api/group/index.js'

const emit = defineEmits(['done'])

const userStore = useUserStore()
const friendApi = useFriendApi()
const groupApi = useGroupApi()
const friendFormRef = ref(null)
const state = reactive({
  visible: false,
  dialogTitle: '',
  form: {},
  formRules: {},
  loading: false
})

// 打开弹窗
const openDialog = (row) => {
  state.visible = true
  if (row.chatId.startsWith('user')) {
    state.dialogTitle = '好友'
    let arr = row.chatId.split('-')
    let userId = arr[1]
    if (userStore.user.id == userId) {
      userId = arr[2]
    }
    state.form.friendId = userId
    state.form.remark = row.remark
  } else {
    state.dialogTitle = '群聊'
    state.form.groupId = row.chatId.split('-')[1]
    state.form.remark = row.remark
  }
}

// 关闭弹窗
const closeDialog = () => {
  if (friendFormRef.value) {
    friendFormRef.value.resetFields()
  }
}

// 弹窗关闭前的处理
const handleBeforeClose = () => {
  closeDialog()
  return true
}

// 提交
const submit = () => {
  if (!friendFormRef.value) return
  friendFormRef.value.validate(async (valid) => {
    if (valid) {
      state.loading = true
      try {
        let res
        if (state.dialogTitle == '群聊') {
          res = await groupApi.updateRemark(state.form)
        } else {
          res = await friendApi.updateFriend(state.form)
        }
        ElMessage.success(res.message)
        state.visible = false
        emit('done')
      } finally {
        state.loading = false
      }
    }
  })
}

// 暴露方法
defineExpose({
  openDialog
})
</script>

<template>
  <el-dialog 
    v-model="state.visible" 
    :title="`编辑${state.dialogTitle}信息`" 
    width="480px" 
    @close="closeDialog"
    center
    :show-close="true"
    :close-on-click-modal="true"
    :close-on-press-escape="true"
    :destroy-on-close="true"
    :before-close="handleBeforeClose"
    class="custom-dialog"
    custom-class="beautiful-dialog"
  >
    <el-form 
      ref="friendFormRef" 
      :model="state.form" 
      :rules="state.formRules" 
      label-width="120px"
      class="dialog-form"
    >
      <el-form-item 
        :label="`${state.dialogTitle}备注`" 
        prop="remark"
        class="form-item"
      >
        <el-input 
          v-model="state.form.remark" 
          type="textarea" 
          :placeholder="`请输入${state.dialogTitle}备注`"
          :rows="4"
          resize="none"
          class="custom-textarea"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button 
          @click="state.visible = false"
          size="large"
          class="dialog-btn cancel-btn"
        >
          取消
        </el-button>
        <el-button 
          type="primary" 
          @click="submit" 
          :loading="state.loading"
          size="large"
          class="dialog-btn save-btn"
        >
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 自定义对话框样式 */
.beautiful-dialog {
  /* 添加动画效果 */
  animation: dialogFadeIn 0.3s ease-out;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
}

/* 对话框淡入动画 */
@keyframes dialogFadeIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 表单样式 */
.dialog-form {
  padding: 20px 0;
}

.form-item {
  margin-bottom: 24px;
}

/* 自定义文本域样式 */
.custom-textarea {
  border-radius: 8px;
  transition: all 0.3s ease;
  font-size: 14px;
}

.custom-textarea:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

/* 底部按钮区域 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  background-color: #fafafa;
  border-top: 1px solid #ebeef5;
  border-radius: 0 0 12px 12px;
}

/* 自定义按钮样式 */
.dialog-btn {
  border-radius: 8px;
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
}

/* 取消按钮样式 */
.cancel-btn {
  background-color: #f0f2f5;
  color: #606266;
}

.cancel-btn:hover {
  background-color: #e6e8eb;
  color: #303133;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 保存按钮样式 */
.save-btn {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
}

.save-btn:hover {
  background: linear-gradient(135deg, #66b1ff 0%, #89cfff 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.save-btn:active {
  transform: translateY(0);
}

/* 禁用状态样式 */
.save-btn:disabled,
.save-btn.is-loading {
  background: linear-gradient(135deg, #a0cfff 0%, #c6e2ff 100%);
  transform: none;
  box-shadow: none;
}

/* 加载动画优化 */
.save-btn .el-icon-loading {
  margin-right: 6px;
}
</style>
