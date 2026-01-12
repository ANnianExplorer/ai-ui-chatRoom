<script setup>
import { reactive, ref } from 'vue'
import { useGroupApi } from '@/api/group/index.js'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['done'])

const groupApi = useGroupApi()
const groupFormRef = ref(null)
const state = reactive({
  visible: false,
  groupForm: {
    name: '',
    remark: '',
    description: ''
  },
  groupRules: {
    name: [{ required: true, message: '请输入群聊名称', trigger: 'blur' }]
  },
  loading: false
})

// 打开弹窗
const openDialog = () => {
  state.visible = true
}

// 关闭弹窗
const closeDialog = () => {
  groupFormRef.value.resetFields()
}

// 提交
const submit = () => {
  if (!groupFormRef.value) return
  groupFormRef.value.validate(async (valid) => {
    if (valid) {
      state.loading = true
      try {
        const res = await groupApi.createGroup(state.groupForm)
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
  <el-dialog v-model="state.visible" title="创建群聊" width="400px" @close="closeDialog">
    <el-form ref="groupFormRef" :model="state.groupForm" :rules="state.groupRules" label-width="80">
      <el-form-item label="群聊名称" prop="name">
        <el-input v-model="state.groupForm.name" placeholder="请输入群聊名称" />
      </el-form-item>
      <el-form-item label="群聊描述">
        <el-input v-model="state.groupForm.description" type="textarea" placeholder="可选" />
      </el-form-item>
      <el-form-item label="群聊备注">
        <el-input v-model="state.groupForm.remark" type="textarea" placeholder="可选" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="state.visible = false">取消</el-button>
      <el-button type="primary" @click="submit" :loading="state.loading">创建</el-button>
    </template>
  </el-dialog>
</template>
