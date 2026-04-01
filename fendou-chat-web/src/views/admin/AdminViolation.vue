<template>
  <div class="admin-violation">
    <div class="page-header">
      <h2>消息违规监控</h2>
      <p>AI自动审核消息内容，人工复查违规消息</p>
    </div>

    <!-- 快速审核 -->
    <el-card class="audit-card" shadow="hover">
      <template #header>
        <div class="card-header-row">
          <span class="card-title">🤖 手动AI审核</span>
        </div>
      </template>
      <div class="audit-form">
        <el-input
          v-model="auditText"
          type="textarea"
          :rows="3"
          placeholder="输入消息内容进行AI违规审核..."
          style="margin-bottom:12px"
        />
        <el-button type="primary" @click="doAudit" :loading="auditLoading"
          style="background:linear-gradient(135deg,#7c3aed,#9333ea);border:none">
          🔍 AI审核
        </el-button>
        <div v-if="auditResult !== null" class="audit-result" :class="auditResult ? 'violation' : 'safe'">
          <el-icon>{{ auditResult ? '⚠️' : '✅' }}</el-icon>
          {{ auditResult ? '⚠️ 检测到违规内容，建议删除' : '✅ 内容正常，无违规' }}
        </div>
      </div>
    </el-card>

    <!-- 消息列表（可标记违规） -->
    <el-card class="messages-card" shadow="hover">
      <template #header>
        <div class="card-header-row">
          <span class="card-title">📋 消息人工复查队列</span>
          <div style="display:flex;gap:8px">
            <el-input v-model="searchKeyword" placeholder="搜索消息内容" clearable style="width:200px" @keyup.enter="searchMessages">
              <template #append><el-button @click="searchMessages" icon="Search" /></template>
            </el-input>
            <el-button @click="loadMessages" :loading="loading" icon="RefreshRight">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="messages" v-loading="loading" border style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="chatId" label="聊天室" min-width="140" show-overflow-tooltip />
        <el-table-column label="发送者" width="100">
          <template #default="scope">
            <el-tag size="small" type="info">用户 {{ scope.row.sendId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消息内容" min-width="200">
          <template #default="scope">
            <span v-if="scope.row.contentType === 'text'" class="msg-preview">{{ scope.row.content }}</span>
            <el-tag v-else size="small">{{ scope.row.contentType === 'image' ? '🖼️ 图片' : '📎 文件' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发送时间" min-width="160" />
        <el-table-column label="AI审核" width="100">
          <template #default="scope">
            <el-button size="small" type="warning" @click="aiAuditMessage(scope.row)" :loading="scope.row.auditing" plain>
              检测
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-popconfirm title="确认删除该消息？" @confirm="deleteMessage(scope.row.id)">
              <template #reference>
                <el-button size="small" type="danger" plain>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadMessages"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { useAdminApi } from '@/api/admin/index.js'
import request from '@/utils/request.js'

const adminApi = useAdminApi()
const loading = ref(false)
const auditText = ref('')
const auditLoading = ref(false)
const auditResult = ref(null)
const searchKeyword = ref('')

const messages = ref([])
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await adminApi.messages.getGroupMessages({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value
    })
    if (res.code === 200) {
      messages.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

const searchMessages = () => {
  pagination.page = 1
  loadMessages()
}

const doAudit = async () => {
  if (!auditText.value.trim()) {
    ElMessage.warning('请输入要审核的内容')
    return
  }
  auditLoading.value = true
  try {
    const res = await request({ url: '/ai/audit', method: 'post', data: { content: auditText.value } })
    if (res.code === 200) {
      auditResult.value = res.data
    }
  } finally {
    auditLoading.value = false
  }
}

const aiAuditMessage = async (msg) => {
  if (!msg.content || msg.contentType !== 'text') {
    ElMessage.info('仅支持审核文本消息')
    return
  }
  msg.auditing = true
  try {
    const res = await request({ url: '/ai/audit', method: 'post', data: { content: msg.content } })
    if (res.code === 200) {
      if (res.data) {
        ElNotification({ title: '违规警告', message: `消息ID ${msg.id} 检测到违规内容`, type: 'warning', duration: 5000 })
      } else {
        ElMessage.success(`消息ID ${msg.id} 内容正常`)
      }
    }
  } finally {
    msg.auditing = false
  }
}

const deleteMessage = async (id) => {
  try {
    const res = await adminApi.messages.deleteMessage(id)
    if (res.code === 200) {
      ElMessage.success('消息已删除')
      loadMessages()
    }
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(loadMessages)
</script>

<style scoped>
.admin-violation {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header h2 { font-size: 20px; font-weight: 700; color: #1e1b4b; margin: 0 0 4px; }
.page-header p { font-size: 13px; color: #6b7280; margin: 0; }

.card-header-row { display: flex; align-items: center; justify-content: space-between; }
.card-title { font-size: 14px; font-weight: 600; color: #374151; }

.audit-form { padding: 4px 0; }

.audit-result {
  margin-top: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
}

.audit-result.violation {
  background: rgba(239,68,68,0.1);
  color: #dc2626;
  border: 1px solid rgba(239,68,68,0.2);
}

.audit-result.safe {
  background: rgba(16,185,129,0.1);
  color: #059669;
  border: 1px solid rgba(16,185,129,0.2);
}

.msg-preview {
  font-size: 13px;
  color: #374151;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }

:deep(.el-card) { border-radius: 12px; border: 1px solid rgba(124,58,237,0.1); }
:deep(.el-card__header) { padding: 12px 16px; border-bottom: 1px solid rgba(124,58,237,0.1); }
</style>
