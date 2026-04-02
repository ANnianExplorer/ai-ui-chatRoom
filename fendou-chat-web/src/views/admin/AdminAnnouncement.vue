<template>
  <div class="admin-announcement">
    <div class="page-header">
      <h2>系统公告推送</h2>
      <p>向所有在线用户实时推送系统公告</p>
    </div>

    <div class="content-grid">
      <!-- 发送公告 -->
      <el-card shadow="hover">
        <template #header>
          <span class="card-title">📢 发送系统公告</span>
        </template>
        <el-form :model="form" label-width="80px">
          <el-form-item label="公告标题">
            <el-input v-model="form.title" placeholder="公告标题（可选）" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="公告内容" required>
            <el-input
              v-model="form.content"
              type="textarea"
              :rows="5"
              placeholder="请输入公告内容..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="预设模板">
            <div class="templates">
              <el-button size="small" v-for="tpl in templates" :key="tpl.title" @click="applyTemplate(tpl)" plain>
                {{ tpl.title }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
        <div style="margin-top:16px">
          <el-button
            type="primary"
            @click="sendAnnouncement"
            :loading="sending"
            size="large"
            style="background:linear-gradient(135deg,#7c3aed,#9333ea);border:none;width:100%;border-radius:10px;font-size:15px"
          >
            📡 立即向所有在线用户推送
          </el-button>
        </div>
        <div v-if="lastSent" class="last-sent">
          ✅ 上次发送：{{ lastSent }}
        </div>
      </el-card>

      <!-- 发送历史 -->
      <el-card shadow="hover">
        <template #header>
          <span class="card-title">📋 推送历史记录</span>
        </template>
        <div v-if="history.length === 0" style="text-align:center;padding:40px;color:#9ca3af">
          暂无推送记录
        </div>
        <div v-else class="history-list">
          <div v-for="(item, idx) in history" :key="idx" class="history-item">
            <div class="history-header">
              <span class="history-title">{{ item.title || '系统公告' }}</span>
              <span class="history-time">{{ item.time }}</span>
            </div>
            <div class="history-content">{{ item.content }}</div>
            <div class="history-meta">
              <el-tag size="small" type="success">已推送</el-tag>
              <span style="font-size:12px;color:#9ca3af;margin-left:8px">推送给 {{ item.onlineCount || 0 }} 人</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const sending = ref(false)
const lastSent = ref('')
const history = ref([])

const form = reactive({
  title: '',
  content: ''
})

const templates = [
  { title: '系统维护', content: '系统将于今晚 22:00-24:00 进行例行维护，请提前保存数据，期间无法正常使用。' },
  { title: '功能更新', content: '系统已更新，新增了AI摘要、消息撤回、群投票等功能，欢迎体验！' },
  { title: '节日祝福', content: '亲爱的用户，节日快乐！感谢大家一直以来的支持与陪伴 🎉' },
  { title: '安全提示', content: '请勿在聊天中发送个人隐私信息，注意账号安全，如有异常请及时联系管理员。' }
]

const applyTemplate = (tpl) => {
  form.title = tpl.title
  form.content = tpl.content
}

const sendAnnouncement = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }
  sending.value = true
  try {
    const res = await request({
      url: '/admin/actions/announce',
      method: 'post',
      data: { title: form.title, content: form.content }
    })
    if (res.code === 200) {
      ElMessage.success('公告推送成功！')
      form.title = ''
      form.content = ''
      loadHistory()
    }
  } catch {
    ElMessage.error('推送失败')
  } finally {
    sending.value = false
  }
}

const loadHistory = async () => {
  try {
    const res = await request({
      url: '/admin/actions/announcements',
      method: 'get',
      params: { page: 1, pageSize: 20 }
    })
    if (res.code === 200 && res.data && res.data.records) {
      history.value = res.data.records.map(item => ({
        title: item.title || '系统公告',
        content: item.content,
        time: item.createTime,
        onlineCount: item.onlineCount || 0
      }))
      if (history.value.length > 0) {
        lastSent.value = history.value[0].time
      }
    }
  } catch (e) {
    console.error('加载历史失败:', e)
  }
}

onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
.admin-announcement { display: flex; flex-direction: column; gap: 16px; }
.page-header h2 { font-size: 20px; font-weight: 700; color: #1e1b4b; margin: 0 0 4px; }
.page-header p { font-size: 13px; color: #6b7280; margin: 0; }

.content-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }

.card-title { font-size: 14px; font-weight: 600; color: #374151; }

.templates { display: flex; flex-wrap: wrap; gap: 8px; }

.last-sent {
  margin-top: 12px;
  padding: 8px 12px;
  background: rgba(16,185,129,0.08);
  border-radius: 8px;
  font-size: 13px;
  color: #059669;
}

.history-list { display: flex; flex-direction: column; gap: 12px; max-height: 400px; overflow-y: auto; }

.history-item {
  padding: 12px;
  background: rgba(124,58,237,0.04);
  border-radius: 10px;
  border-left: 3px solid #7c3aed;
}

.history-header { display: flex; justify-content: space-between; margin-bottom: 6px; }
.history-title { font-size: 13px; font-weight: 600; color: #1e1b4b; }
.history-time { font-size: 12px; color: #9ca3af; }
.history-content { font-size: 13px; color: #374151; line-height: 1.5; margin-bottom: 8px; }
.history-meta { display: flex; align-items: center; }

:deep(.el-card) { border-radius: 12px; border: 1px solid rgba(124,58,237,0.1); }
:deep(.el-card__header) { padding: 12px 16px; border-bottom: 1px solid rgba(124,58,237,0.1); }

@media (max-width: 900px) {
  .content-grid { grid-template-columns: 1fr; }
}
</style>
