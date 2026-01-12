<template>
  <!-- 未选择聊天对象 -->
  <div v-if="!chat" class="chat-container empty-state1">
    <div class="empty-content1">
      <div class="empty-logo1">
        <img src="@/assets/logo.png" alt="logo" class="app-logo" />
        <h2 class="empty-title">欢迎来到在线聊天室</h2>
        <p class="empty-subtitle">请选择一个聊天开始对话，开启快乐的聊天体验！</p>
      </div>
    </div>
  </div>

  <!-- 已选择聊天对象 -->
  <div v-else class="chat-container selected">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-left">
        <div class="chat-info">
          <p class="chat-name">{{ chat.remark || chat.name }}</p>
          <p class="chat-status" v-if="chat.type === 2">
            <el-icon class="status-icon"><User /></el-icon>
            {{ getOnlineCount(chat.chatId) }} 人在线
          </p>
        </div>
      </div>
      <div class="header-right">
<!--        <el-button
          type="text"
          size="small"
          class="member-btn"
          @click="toggleMemberList"
          v-if="chat.type === 2"
        >
          <el-icon><User /></el-icon>
          群成员
        </el-button>-->
        <el-dropdown placement="bottom" trigger="click">
          <span class="pr15">
            <el-icon size="20"><More /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleChatMore">更多信息</el-dropdown-item>
              <el-dropdown-item v-if="chat.type === 2 && state.isGroupOwner" @click="openAnnouncementDialog">
                编辑群公告
              </el-dropdown-item>
              <el-dropdown-item divided @click="handleChatClean">
                <el-text type="danger">清空聊天</el-text>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 群公告展示区域 -->
    <div v-if="chat.type === 2" class="group-announcement">
      <div class="announcement-content">
        <el-icon class="announcement-icon"><Bell /></el-icon>
        <span class="announcement-text">{{ state.groupCall || '暂无群公告' }}</span>
      </div>
    </div>

    <!-- 聊天消息区域 -->
    <div ref="chatRef" class="chat-window">
      <!-- 历史消息加载按钮 -->
      <div class="load-more">
        <el-link
          v-if="state.pIndex * state.pSize < state.page.total"
          underline="never"
          :loading="state.loadingHistory"
          @click="loadMoreMessages"
          size="small"
        >
          {{ state.loadingHistory ? '加载中...' : '加载历史消息' }}
        </el-link>
      </div>

      <div v-for="msg in getMessages">
        <div class="left" v-if="msg.sendId !== state.currentUser.id">
          <!-- 头像 -->
          <img :src="msg.chatAvatar" class="avatar" />

          <div class="message-wrapper">
            <!-- 群聊显示发送人姓名 -->
            <span v-if="chat.type === 2" class="sender-name">{{ msg.realName }}</span>

            <!-- 消息内容 -->
            <div class="message-container">
              <el-dropdown placement="bottom-start" trigger="contextmenu">
                <!-- 推理内容 -->
                <p v-if="msg.reasoningContent && msg.reasoningContent.trim() !== ''" class="reasoning-content">
                  {{ msg.reasoningContent }}
                </p>

                <!-- 文本 -->
                <div v-if="msg.contentType === 'text'" class="message-wrapper">
                  <p class="message">{{ msg.content }}</p>
                </div>

                <!-- 图片 -->
                <div v-if="msg.contentType === 'image'" class="image-wrapper">
                  <el-image
                    :src="msg.content"
                    :preview-src-list="[msg.content, '', '']"
                    class="image-message"
                  />
                </div>

                <!-- 文件 -->
                <div v-if="msg.contentType === 'file'" class="file-message">
                  <el-icon class="file-icon">
                    <Document />
                  </el-icon>
                  <div class="file-info">
                    <p class="file-name">{{ msg.fileName }}</p>
                    <p class="file-size">{{ formatFileSize(msg.fileSize) }}</p>
                  </div>
                  <div class="file-actions">
                    <el-button type="primary" size="small" @click="downloadFile(msg.content)">下载</el-button>
                  </div>
                </div>

                <!-- 右键菜单 -->
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleMessageCopy(msg)">复制</el-dropdown-item>
                  <el-dropdown-item @click="handleMessageFavorite(msg)">收藏</el-dropdown-item>
                </el-dropdown-menu>
              </template>
              </el-dropdown>
            </div>
            
            <!-- 发送时间 - 放在message-container之外 -->
            <div class="timestamp-wrapper">
              <p class="timestamp">{{ msg.createTime }}</p>
              <el-button 
                  type="text" 
                  size="small" 
                  :class="['favorite-btn-inline', msg.isFavorite ? 'favorite-active' : '']"
                  @click="handleMessageFavorite(msg)"
                  title="收藏"
                >
                  <el-icon><Star /></el-icon>
                </el-button>
            </div>
          </div>
        </div>

        <div class="right" v-if="msg.sendId === state.currentUser.id">
          <div class="message-wrapper">
            <div class="message-container">
              <el-dropdown placement="bottom-start" trigger="contextmenu">
                <!-- 文本 -->
                <div v-if="msg.contentType === 'text'" class="message-wrapper">
                  <p class="message">{{ msg.content }}</p>
                </div>

                <!-- 图片 -->
                <div v-if="msg.contentType === 'image'" class="image-wrapper">
                  <el-image
                    :src="msg.content"
                    :preview-src-list="[msg.content, '', '']"
                    class="image-message"
                  />
                </div>

                <!-- 文件 -->
                <div v-if="msg.contentType === 'file'" class="file-message">
                  <el-icon class="file-icon">
                    <Document />
                  </el-icon>
                  <div class="file-info">
                    <p class="file-name">{{ msg.fileName }}</p>
                    <p class="file-size">{{ formatFileSize(msg.fileSize) }}</p>
                  </div>
                  <div class="file-actions">
                    <el-button type="primary" size="small" @click="downloadFile(msg.content)">下载</el-button>
                  </div>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleMessageCopy(msg)">复制</el-dropdown-item>
                    <el-dropdown-item @click="handleMessageFavorite(msg)">收藏</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            
            <!-- 发送时间 - 放在message-container之外 -->
            <div class="timestamp-wrapper">
              <p class="timestamp">{{ msg.createTime }}</p>
              <el-button 
                  type="text" 
                  size="small" 
                  :class="['favorite-btn-inline', msg.isFavorite ? 'favorite-active' : '']"
                  @click="handleMessageFavorite(msg)"
                  title="收藏"
                >
                  <el-icon><Star /></el-icon>
                </el-button>
            </div>
          </div>
          <img :src="state.currentUser.avatar" class="avatar" />
        </div>
      </div>
    </div>

    <!-- 输入区域 - 直接放在聊天窗口下方 -->
    <div class="input-area">
      <!-- 工具栏 -->
      <div class="input-tools">
        <!-- AI相关按钮 -->
        <div v-if="chat.type === 3" class="ai-tools">
                    <el-button
                      :type="state.history ? 'primary' : 'default'"
                      title="历史对话"
                      plain
                      @click="state.history = !state.history"
                      size="small"
                    >
                      历史对话
                    </el-button>

        </div>
        
        <!-- 基础工具按钮 -->
        <div class="basic-tools">
          <!-- 表情包 -->
          <el-popover placement="top-start" :width="380" trigger="click">
            <Picker
              :data="emojiIndex"
              :emojiSize="22"
              :showPreview="false"
              :infiniteScroll="false"
              :i18n="emojiI18n"
              :pickerStyles="{ height: '320px', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }"
              @select="handleEmoji"
            />
            <template #reference>
              <el-button title="表情" plain circle size="small">🤠</el-button>
            </template>
          </el-popover>

          <!-- 加号下拉菜单 -->
          <el-dropdown v-if="chat.type !== 3" placement="top" trigger="click">
            <el-button title="更多" plain circle size="small">
              <el-icon size="18">+</el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <span @click="$refs.imageUpload.$el.querySelector('input[type=file]').click()">
                    <el-icon><Picture /></el-icon> 图片
                  </span>
                  <!-- 隐藏的图片上传组件 -->
                  <el-upload
                    ref="imageUpload"
                    :action="state.uploadApi"
                    :on-success="handleImageUpload"
                    :before-upload="beforeImageUpload"
                    :show-file-list="false"
                    :accept="'image/*'"
                    name="file"
                    :headers="state.fileHeaders"
                    style="display: none;"
                  >
                  </el-upload>
                </el-dropdown-item>
                <el-dropdown-item>
                  <span @click="$refs.fileUpload.$el.querySelector('input[type=file]').click()">
                    <el-icon><FolderOpened /></el-icon> 文件
                  </span>
                  <!-- 隐藏的文件上传组件 -->
                  <el-upload
                    ref="fileUpload"
                    :action="state.uploadApi"
                    :on-success="handleFileUpload"
                    :before-upload="beforeFileUpload"
                    :accept="'.pdf,.doc,.docx,.xls,.xlsx,.txt,.ppt,.pptx'"
                    :show-file-list="false"
                    :headers="state.fileHeaders"
                    name="file"
                    style="display: none;"
                  >
                  </el-upload>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 输入框和发送按钮 -->
      <div class="input-main" style="display: flex; align-items: flex-end; gap: 12px; width: 100%; box-sizing: border-box; overflow: visible; padding-right: 10px;">
        <!-- 输入框 -->
        <el-input
          v-model="state.message"
          type="textarea"
          :rows="3"
          placeholder="请输入消息"
          @keydown.enter.prevent="sendMessage()"
          resize="none"
          class="message-input"
          style="flex: 1; min-width: 0;"
        />
        <!-- 发送按钮 -->
        <el-button 
          type="primary" 
          @click="sendMessage()" 
          :disabled="!state.message.trim()"
          size="default"
          class="send-button"
          style="white-space: nowrap; min-width: 80px; height: 36px; padding: 0 16px; flex-shrink: 0;"
        >
          发送
        </el-button>
      </div>
      
      <div class="hint-text">
        <span class="hint">按 Enter发送，Shift+Enter 换行</span>
      </div>
    </div>
    <!-- 群公告编辑弹窗 -->
    <el-dialog
      v-model="state.showAnnouncementDialog"
      title="编辑群公告"
      width="500px"
      destroy-on-close
    >
      <el-form>
        <el-form-item label="群公告">
          <el-input
            v-model="state.groupCall"
            type="textarea"
            :rows="5"
            placeholder="请输入群公告内容"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="state.showAnnouncementDialog = false">取消</el-button>
          <el-button type="primary" @click="saveGroupAnnouncement">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 聊天容器 */
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--chat-bg-color, #fff);
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
}

/* 空状态样式 */
.chat-container.empty-state1 {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh; /* 或者根据你的布局需求调整高度 */
}

.empty-content1 {
  text-align: center;
}
.chat-container.empty-state::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
              radial-gradient(circle at 80% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
              radial-gradient(circle at 40% 80%, rgba(255, 255, 255, 0.12) 0%, transparent 50%);
  z-index: 1;
}

.empty-content {
  text-align: center;
  z-index: 2;
}

.empty-logo {
  margin-bottom: 8px;
}

.app-logo {
  width: 400px;
  height: 200px;
}

.empty-title {
  font-size: 48px;
  font-weight: 600;
  margin-bottom: 11px;
  color: #1e3a8a;
  letter-spacing: 0.5px;
}

.empty-subtitle {
  font-size: 20px;
  opacity: 0.9;
  color: #1e3a8a;
  font-weight: 500;
}

/* 已选择聊天对象的容器 */
.chat-container.selected {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background-color: var(--chat-header-bg, #fff);
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

/* 聊天消息区域 */
.chat-window {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background-color: var(--chat-bg-color, #f7f8fa);
  padding: 16px;
  min-height: 200px;
  /* 隐藏滚动条但保留滚动功能 */
  &::-webkit-scrollbar {
    width: 0;
    height: 0;
    opacity: 0;
  }
  scrollbar-width: none;
  -ms-overflow-style: none;
  transition: background-color 0.3s ease;
}

/* 输入区域 */
.input-area {
  padding: 12px 16px;
  background-color: var(--chat-input-bg, #fff);
  border-top: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

/* 工具栏 */
.input-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

/* AI相关按钮 */
.ai-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 16px;
}

/* 基础工具按钮 */
.basic-tools {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 输入框和发送按钮容器 */
.input-main {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

/* 消息输入框 */
.message-input {
  flex: 1;
  min-width: 0;
}

/* 发送按钮 */
.send-button {
  white-space: nowrap;
  min-width: 80px;
  padding: 0 16px;
  height: 36px;
  flex-shrink: 0;
}

/* 提示文本 */
.hint-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 左侧消息布局 */
.left {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 12px;
}

/* 右侧消息布局 */
.right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 12px;
}

/* 消息容器样式 */
.message-container {
  position: relative;
  max-width: 70%;
}

/* 左侧消息容器 */
.left .message-container {
  background-color: #fff;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 右侧消息容器 */
.right .message-container {
  background-color: #95ec69;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 左侧消息布局中的消息包装器 */
.left .message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

/* 右侧消息布局中的消息包装器 */
.right .message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

/* 文本消息内容的包装器 */
.message-container .message-wrapper {
  display: block;
  gap: 0;
}

/* 文本消息内容 */
.message {
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.5;
  margin: 0;
}

/* 头像样式 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* 收藏按钮样式 */
.favorite-btn {
  padding: 0;
  margin: 0;
  min-width: auto;
  height: auto;
  line-height: 1;
  color: #999;
  transition: color 0.2s;
  margin-left: 8px;
}

.favorite-btn:hover {
  color: #409eff;
}

/* 日期包装器 - 包含日期和收藏按钮 */
.timestamp-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

/* 日期后面的收藏按钮 */
.favorite-btn-inline {
  padding: 0;
  margin: 0;
  min-width: auto;
  height: auto;
  line-height: 1;
  font-size: 12px;
  color: #999;
  transition: all 0.2s;
}

.favorite-btn-inline:hover {
  color: #409eff;
  transform: scale(1.1);
}

/* 收藏激活状态 */
.favorite-active {
  color: #f56c6c;
  font-weight: bold;
}

/* 群公告样式 */
.group-announcement {
  background-color: #f0f9ff;
  border-bottom: 1px solid #e0e0e0;
  padding: 12px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.announcement-content {
  display: flex;
  align-items: center;
  gap: 8px;
  max-width: 100%;
  overflow: hidden;
}

.announcement-icon {
  color: #36cfc9;
  font-size: 18px;
  flex-shrink: 0;
}

.announcement-text {
  color: #303133;
  font-size: 14px;
  line-height: 1.5;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 群公告弹窗样式 */
:deep(.el-dialog__header) {
  background-color: #fafafa;
  border-bottom: 1px solid #e0e0e0;
}

:deep(.el-dialog__title) {
  color: #303133;
  font-weight: 600;
}

:deep(.el-textarea__inner) {
  resize: vertical;
  min-height: 120px;
  border-radius: 8px;
  font-size: 14px;
}

:deep(.el-textarea__count) {
  color: #909399;
  font-size: 12px;
}
</style>

<script setup>
import { nextTick, computed, reactive, ref, watch, inject } from 'vue'
// all emoji
import data from 'emoji-mart-vue-fast/data/all.json'
import 'emoji-mart-vue-fast/css/emoji-mart.css'

import { Picker, EmojiIndex } from 'emoji-mart-vue-fast/src'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user.js'
import { useMessageApi } from '@/api/message/index.js'
import { useWebsocketStore } from '@/stores/websocket.js'
import { useGroupApi } from '@/api/group/index.js'
import { getFormatDate, formatFileSize } from '@/utils/common.js'
import { useRouter } from 'vue-router'
import useClipboard from 'vue-clipboard3'
import { useAIApi } from '@/api/ai'
import { useFavoriteApi } from '@/api/favorite/index.js'

// 父组件传递的值
const props = defineProps({
  chat: {
    type: Object,
    default: () => {}
  }
})

// 定义emit事件
const emit = defineEmits(['show-group-member'])

// 当前登录用户
const userStore = useUserStore()
const router = useRouter()
const websocket = useWebsocketStore()
const { toClipboard } = useClipboard()
const messageApi = useMessageApi()
const aiApi = useAIApi()
const favoriteApi = useFavoriteApi()
const groupApi = useGroupApi()

// 不再需要从父组件注入，直接使用websocket store

// 定义 Emoji 的 i18n
const emojiI18n = {
  search: '搜索',
  notfound: '未找到表情',
  categories: {
    search: '搜索结果',
    recent: '经常使用',
    smileys: '表情与情感',
    people: '人物与身体',
    nature: '动物与自然',
    foods: '食物与饮料',
    activity: '活动',
    places: '旅行与地理',
    objects: '物品',
    symbols: '符号标志',
    flags: '旗帜',
    custom: '自定义',
    joy: '哭笑'
  }
}
const emojiIndex = new EmojiIndex(data)
// 选中emoji
const handleEmoji = (e) => {
  state.message += e.native
}

const chatRef = ref(null)
const state = reactive({
  currentUser: userStore.user,
  messages: [],
  message: '',
  pIndex: 1,
  pSize: 8,
  page: {},
  uploadApi: import.meta.env.VITE_APP_API_URL + 'file/upload',
  isInitialLoad: true,
  loadingHistory: false,
  fileHeaders: {
    Authorization: 'Bearer ' + sessionStorage.getItem('token')
  },
  thinking: false,
  history: false,

  // 群公告相关
  groupCall: '',
  showAnnouncementDialog: false,
  isGroupOwner: false
})

// 文件上传
const handleFileUpload = (res, file) => {
  if (res.code === 200) {
    const data = res.data
    const msg = buildMessage(data.filePath, 'file', data)
    websocket.sendMessage(msg)
    state.messages.push(msg)
    scrollToBottom()
  }
}

// 图片上传前
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('请上传图片文件')
    return false
  }

  if (file.size > 1024 * 1024 * 10) {
    ElMessage.error('图片大小不能超过10M')
    return false
  }

  return true
}

// 图片上传成功处理
const handleImageUpload = (res, file) => {
  if (res.code === 200) {
    const data = res.data
    const msg = buildMessage(data.filePath, 'image', data)
    websocket.sendMessage(msg)
    state.messages.push(msg)
    scrollToBottom()
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 文件上传之前
const beforeFileUpload = (file) => {
  // 允许的文件类型
  const allowedTypes = [
    'application/pdf',
    'application/doc',
    'application/docx',
    'application/xls',
    'application/xlsx',
    'application/txt',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'text/plain'
  ]

  const isLt10M = file.size / 1024 / 1024 < 10

  // 检查文件类型
  const isAllowedType = allowedTypes.includes(file.type) || file.name.match(/\.(pdf|doc|docx|xls|xlsx|txt)$/i)

  if (!isAllowedType) {
    ElMessage.error('不支持的文件类型，请上传 PDF、Word、Excel 或文本文件!')
    return false
  }

  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }

  return true
}

// 更多信息
const handleChatMore = () => {
  router.push(`/contact/detail/${props.chat.chatId}`)
}
// 清空聊天
const handleChatClean = () => {
  ElMessageBox.confirm(`您确定要将与 ${props.chat.name} 的聊天记录清空吗？`, '清空聊天记录', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    state.messages = []
  })
}

// 切换群成员列表显示
const toggleMemberList = () => {
  emit('show-group-member', true)
}

// 获取群聊在线人数
const getOnlineCount = (chatId) => {
  // 直接使用websocket store的getter方法
  return websocket.getOnlineCount(chatId)
}

// 滚动到底部
const scrollToBottom = () => {
  if (!chatRef.value) return
  nextTick(() => {
    if (chatRef.value) {
      chatRef.value.scrollTop = chatRef.value.scrollHeight
    }
  })
}

const getMessages = computed(() => {
  return state.messages.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
})

// 获取聊天记录
const getMessage = async (chatId) => {
  const params = {
    pIndex: state.pIndex,
    pSize: state.pSize
  }
  const res = await messageApi.getMessageByChatId(chatId, params)
  if (state.messages) {
    state.messages.unshift(...res.data.records)
  } else {
    state.messages = res.data.records
  }
  state.page = res.data
  // 滚动处理
  nextTick(() => {
    if (chatRef.value) {
      if (state.isInitialLoad) {
        // 初次加载滚动到底部
        scrollToBottom()
        state.isInitialLoad = false
      } else {
        // 加载历史消息保持当前位置
        const currentScrollTop = chatRef.value.scrollTop
        const currentScrollHeight = chatRef.value.scrollHeight

        // 等待DOM更新后调整滚动位置
        nextTick(() => {
          const newScrollHeight = chatRef.value.scrollHeight
          chatRef.value.scrollTop = currentScrollTop + (newScrollHeight - currentScrollHeight)
        })
      }
    }
  })
}

// 加载历史消息
const loadMoreMessages = () => {
  if (state.pIndex * state.pSize < state.page.total) {
    try {
      state.loadingHistory = true
      state.pIndex += 1
      getMessage(props.chat.chatId)
    } finally {
      state.loadingHistory = false
    }
  } else {
    ElMessage.info('没有更多消息了')
  }
}

// 监听聊天对象
watch(
  () => props.chat,
  (val) => {
    if (val) {
      state.messages = []
      state.pIndex = 1
      state.isInitialLoad = true // 切换聊天时重置为初始加载状态
      getMessage(val.chatId)
      
      // 如果是群聊，获取群公告和群主信息
      if (val.type === 2) {
        loadGroupAnnouncement(val.chatId)
        checkIfGroupOwner(val.chatId)
      }
    }
  }
)

// 获取群公告
const loadGroupAnnouncement = async (chatId) => {
  try {
    // 解析出真正的数字groupId
    const groupId = chatId.split('-')[1]
    const res = await groupApi.getGroupAnnouncement(groupId)
    // 即使返回null，也要赋值，确保模板中能正确显示"暂无公告"
    state.groupCall = res.data || ''
  } catch (error) {
    console.error('获取群公告失败:', error)
  }
}

// 检查当前用户是否为群主
const checkIfGroupOwner = async (chatId) => {
  try {
    // 解析出真正的数字groupId
    const groupId = chatId.split('-')[1]
    const res = await groupApi.getGroupById(groupId)
    console.log("=====群主是" + res.data.createBy)
    console.log("=====当前用户是" + state.currentUser.id)
    state.isGroupOwner = res.data.createBy === state.currentUser.id
    console.log("=====是否是群主" + state.isGroupOwner)
  } catch (error) {
    console.error('获取群信息失败:', error)
    state.isGroupOwner = false
  }
}

// 打开群公告编辑弹窗
const openAnnouncementDialog = () => {
  state.showAnnouncementDialog = true
}

// 保存群公告
const saveGroupAnnouncement = async () => {
  try {
    // 解析出真正的数字groupId
    const groupId = props.chat.chatId.split('-')[1]
    console.log("群id:" + groupId)
    await groupApi.updateGroupAnnouncement({
      id: parseInt(groupId),
      groupCall: state.groupCall
    })
    ElMessage.success('群公告更新成功')
    state.showAnnouncementDialog = false
  } catch (error) {
    console.error('更新群公告失败:', error)
    ElMessage.error('更新群公告失败')
  }
}

// 下载文件
const downloadFile = (fileUrl) => {
  const link = document.createElement('a')
  link.href = fileUrl
  link.download = 'file'
  link.click()
}

// 监听接收消息
watch(
  () => websocket.message,
  (newVal) => {
    if (props.chat && newVal.chatId === props.chat.chatId) {
      // 如果自己跟自己发送消息，则不处理
      if (newVal.sendId === state.currentUser.id) {
        return
      }

      // 忽略非文本消息
      const messageType = ['text', 'image', 'file']
      if (!messageType.includes(newVal.contentType)) {
        return
      }

      state.messages.push(newVal)
      messageApi.clearUnreadMessage(newVal.chatId)

      // 滚动到底部
      scrollToBottom()
    }
  }
)

// 发送消息
const sendMessage = async () => {
  if (state.message.trim() !== '') {
    const msg = buildMessage(state.message)
    state.messages.push(msg)
    state.message = ''
    scrollToBottom()

    if (props.chat.type === 3) {
      const aiMessage = {
        chatId: props.chat.chatId,
        sendId: 0,
        chatAvatar: props.chat.avatar,
        reasoningContent: '',
        content: '',
        contentType: 'text',
        createTime: getFormatDate()
      }

      if (aiMessage.reasoningContent && aiMessage.reasoningContent !== '') {
        return
      }

      state.messages.push(aiMessage)

      const res = await fetch(aiApi.send(), {
        method: 'post',
        headers: {
          'content-type': 'application/json',
          authorization: 'Bearer ' + sessionStorage.getItem('token')
        },
        body: JSON.stringify({
          prompt: msg.content,
          thinking: state.thinking,
          history: state.history
        })
      })

      const reader = res.body.getReader()
      const decoder = new TextDecoder('utf-8')

      while (true) {
        const { done, value } = await reader.read()

        if (done) {
          // 完成后清空
          aiMessage.reasoningContent = ''
          scrollToBottom()
          break
        }

        // 解码接收到的数据
        const chunk = decoder.decode(value, { stream: true })

        // 处理 SSE 数据格式
        const lines = chunk.split('\n')
        lines.forEach((line) => {
          if (line.startsWith('data:')) {
            const data = line.substring(5).trim()

            if (data === '[DONE]') {
              // 完成后清空
              aiMessage.reasoningContent = ''
              state.messages = [...state.messages]
            } else if (data) {
              try {
                const jsonData = JSON.parse(data)

                if (jsonData.choices && jsonData.choices[0] && jsonData.choices[0].delta) {
                  const rContent = jsonData.choices[0].delta.reasoning_content || ''
                  const content = jsonData.choices[0].delta.content || ''

                  aiMessage.content += content
                  aiMessage.reasoningContent += rContent

                  state.messages = [...state.messages]

                  scrollToBottom()
                }
              } catch (e) {
                aiMessage.content = data
                state.messages = [...state.messages]
                ElMessage.error(data)
                console.log('Parse error:', e)
              }
            }
          }
        })
      }
    } else {
      websocket.sendMessage(msg)
    }
  } else {
    ElMessage.warning('不能发送空白内容！')
  }
}

// 构建消息
const buildMessage = (content, contentType, file) => {
  return {
    chatId: props.chat.chatId,
    sendId: state.currentUser.id,
    chatAvatar: state.currentUser.avatar,
    content: content,
    contentType: contentType || 'text',
    createTime: getFormatDate(),
    fileId: file?.id,
    fileName: file?.originalName,
    fileSize: file?.fileSize,
    fileType: file?.fileType,
    isFavorite: false // 新增：标记是否已收藏
  }
}

// 消息复制
const handleMessageCopy = async (message) => {
  await toClipboard(message.content)
  ElMessage.success('已复制到剪切板')
}

// 消息收藏
const handleMessageFavorite = async (message) => {
  try {
    // 构建收藏数据
    let favoriteFrom = 1; // 默认用户聊天
    if (props.chat.type === 2) {
      favoriteFrom = 2; // 群聊
    } else if (props.chat.type === 3) {
      favoriteFrom = 3; // AI聊天
    }
    
    const favoriteData = {
      collectedId: message.sendId, // 保持机器人的sendId为0，不替换为当前用户id
      favoriteFrom: favoriteFrom, // 1:用户, 2:群聊, 3:AI聊天
      favoriteType: getFavoriteType(message.contentType),
      content: message.id,
      fileName: message.fileName,
      fileSize: message.fileSize,
      fileType: message.fileType
    }
    
    const response = await favoriteApi.addFavorite(favoriteData)
    if (response.code === 200) {
      ElMessage.success('收藏成功')
      // 更新消息对象的isFavorite属性为true
      message.isFavorite = true
      // 刷新消息列表，触发UI更新
      state.messages = [...state.messages]
    } else {
      ElMessage.error('收藏失败')
    }
  } catch (error) {
    ElMessage.error('收藏失败')
    console.error('收藏消息失败:', error)
  }
}

// 获取收藏类型
const getFavoriteType = (contentType) => {
  switch (contentType) {
    case 'text':
      return 1
    case 'image':
      return 2
    case 'file':
      return 3
    default:
      return 1
  }
}
</script>
<!--<el-button-->
<!--    :type="state.thinking ? 'primary' : 'default'"-->
<!--    title="深度思考"-->
<!--    plain-->
<!--    @click="state.thinking = !state.thinking"-->
<!--    size="small"-->
<!--&gt;-->
<!--🙇思考-->
<!--</el-button>-->