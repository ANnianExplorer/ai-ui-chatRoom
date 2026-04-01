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

      <div v-for="msg in getMessages" :key="msg.id || msg.createTime + msg.sendId">
        <!-- 撤回消息统一展示 -->
        <div v-if="msg.isRecalled === 1" class="left" :class="{ right: msg.sendId === state.currentUser.id }">
          <img v-if="msg.sendId !== state.currentUser.id" :src="msg.chatAvatar" class="avatar" />
          <div class="message-wrapper">
            <div class="message-container">
              <div class="recalled-message">
                <el-icon><Delete /></el-icon>
                {{ msg.sendId === state.currentUser.id ? '你' : (msg.realName || '对方') }}撤回了一条消息
              </div>
            </div>
          </div>
          <img v-if="msg.sendId === state.currentUser.id" :src="state.currentUser.avatar" class="avatar" />
        </div>

        <!-- 接收方消息 -->
        <div class="left" v-else-if="msg.sendId !== state.currentUser.id">
          <img :src="msg.chatAvatar" class="avatar" />
          <div class="message-wrapper">
            <span v-if="chat.type === 2" class="sender-name">{{ msg.realName }}</span>
            <div class="message-container">
              <el-dropdown placement="bottom-start" trigger="contextmenu">
                <p v-if="msg.reasoningContent && msg.reasoningContent.trim() !== ''" class="reasoning-content">{{ msg.reasoningContent }}</p>

                <!-- 文本 -->
                <div v-if="msg.contentType === 'text'">
                  <p class="message">{{ msg.content }}</p>
                </div>
                <!-- 图片 -->
                <div v-else-if="msg.contentType === 'image'" class="image-wrapper">
                  <el-image :src="msg.content" :preview-src-list="getImagePreviewList(msg.content)" class="image-message" />
                </div>
                <!-- 文件 -->
                <div v-else-if="msg.contentType === 'file'" class="file-message">
                  <el-icon class="file-icon"><Document /></el-icon>
                  <div class="file-info">
                    <p class="file-name">{{ msg.fileName }}</p>
                    <p class="file-size">{{ formatFileSize(msg.fileSize) }}</p>
                  </div>
                  <el-button type="primary" size="small" @click="downloadFile(msg.content)">下载</el-button>
                </div>
                <!-- 投票 -->
                <div v-else-if="msg.contentType === 'vote'" class="vote-message" @click="openVoteDialog(msg)">
                  <div class="vote-title"><el-icon><DataAnalysis /></el-icon>{{ parseVoteContent(msg.content).title }}</div>
                  <div v-for="opt in parseVoteContent(msg.content).options" :key="opt.id" class="vote-option" :class="{selected: opt.selected}">
                    <span style="flex:1;font-size:13px">{{ opt.content }}</span>
                    <div class="vote-progress">
                      <div class="vote-progress-bar" :style="{width: getVotePercent(opt, parseVoteContent(msg.content)) + '%'}"></div>
                    </div>
                    <span class="vote-count-text">{{ opt.voteCount || 0 }}票</span>
                  </div>
                </div>
                <!-- 位置 -->
                <div v-else-if="msg.contentType === 'location'" class="location-message" @click="openLocation(msg)">
                  <span class="location-icon">📍</span>
                  <div class="location-info">
                    <div class="location-name">{{ parseLocationContent(msg.content).name || '位置分享' }}</div>
                    <div class="location-address">{{ parseLocationContent(msg.content).address || '点击查看位置' }}</div>
                  </div>
                </div>

                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleReply(msg)"><el-icon><ChatLineRound /></el-icon>回复</el-dropdown-item>
                    <el-dropdown-item @click="handleMessageCopy(msg)" v-if="msg.contentType === 'text'"><el-icon><CopyDocument /></el-icon>复制</el-dropdown-item>
                    <el-dropdown-item @click="handleMessageFavorite(msg)"><el-icon><Star /></el-icon>收藏</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div class="timestamp-wrapper">
              <span class="timestamp">{{ msg.createTime }}</span>
              <el-button :class="['favorite-btn-inline', msg.isFavorite ? 'favorite-active' : '']" @click="handleMessageFavorite(msg)" title="收藏" link>
                <el-icon><Star /></el-icon>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 发送方消息 -->
        <div class="right" v-else>
          <div class="message-wrapper">
            <div class="message-container">
              <el-dropdown placement="bottom-end" trigger="contextmenu">
                <!-- 文本 -->
                <div v-if="msg.contentType === 'text'">
                  <p class="message">{{ msg.content }}</p>
                </div>
                <!-- 图片 -->
                <div v-else-if="msg.contentType === 'image'" class="image-wrapper">
                  <el-image :src="msg.content" :preview-src-list="getImagePreviewList(msg.content)" class="image-message" />
                </div>
                <!-- 文件 -->
                <div v-else-if="msg.contentType === 'file'" class="file-message">
                  <el-icon class="file-icon"><Document /></el-icon>
                  <div class="file-info">
                    <p class="file-name">{{ msg.fileName }}</p>
                    <p class="file-size">{{ formatFileSize(msg.fileSize) }}</p>
                  </div>
                  <el-button size="small" @click="downloadFile(msg.content)" style="background:rgba(255,255,255,0.2);border:1px solid rgba(255,255,255,0.4);color:white">下载</el-button>
                </div>
                <!-- 投票 -->
                <div v-else-if="msg.contentType === 'vote'" class="vote-message" @click="openVoteDialog(msg)">
                  <div class="vote-title"><el-icon><DataAnalysis /></el-icon>{{ parseVoteContent(msg.content).title }}</div>
                  <div v-for="opt in parseVoteContent(msg.content).options" :key="opt.id" class="vote-option" :class="{selected: opt.selected}">
                    <span style="flex:1;font-size:13px">{{ opt.content }}</span>
                    <div class="vote-progress">
                      <div class="vote-progress-bar" :style="{width: getVotePercent(opt, parseVoteContent(msg.content)) + '%'}"></div>
                    </div>
                    <span class="vote-count-text">{{ opt.voteCount || 0 }}票</span>
                  </div>
                </div>
                <!-- 位置 -->
                <div v-else-if="msg.contentType === 'location'" class="location-message" @click="openLocation(msg)">
                  <span class="location-icon">📍</span>
                  <div class="location-info">
                    <div class="location-name">{{ parseLocationContent(msg.content).name || '位置分享' }}</div>
                    <div class="location-address">{{ parseLocationContent(msg.content).address || '点击查看位置' }}</div>
                  </div>
                </div>

                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="handleReply(msg)"><el-icon><ChatLineRound /></el-icon>回复</el-dropdown-item>
                    <el-dropdown-item @click="handleMessageCopy(msg)" v-if="msg.contentType === 'text'"><el-icon><CopyDocument /></el-icon>复制</el-dropdown-item>
                    <el-dropdown-item @click="handleMessageFavorite(msg)"><el-icon><Star /></el-icon>收藏</el-dropdown-item>
                    <el-dropdown-item divided @click="handleRecall(msg)" v-if="canRecall(msg)"><el-icon><Delete /></el-icon><span style="color:#f56c6c">撤回</span></el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <div class="timestamp-wrapper" style="justify-content: flex-end;">
              <span class="read-status" :class="{read: msg.isRead === 1}">
                {{ msg.isRead === 1 ? '✓✓' : '✓' }}
              </span>
              <span class="timestamp">{{ msg.createTime }}</span>
              <el-button :class="['favorite-btn-inline', msg.isFavorite ? 'favorite-active' : '']" @click="handleMessageFavorite(msg)" title="收藏" link>
                <el-icon><Star /></el-icon>
              </el-button>
            </div>
            <!-- 定时发送标签 -->
            <div v-if="msg.scheduledTime" class="scheduled-badge" style="align-self:flex-end">
              <el-icon><Clock /></el-icon> 定时 {{ msg.scheduledTime }}
            </div>
          </div>
          <img :src="state.currentUser.avatar" class="avatar" />
        </div>
      </div>

      <!-- 正在输入提示 -->
      <div class="typing-indicator" v-if="state.typingUser">
        <img :src="state.typingAvatar" style="width:28px;height:28px;border-radius:50%;object-fit:cover" />
        <span>{{ state.typingUser }} 正在输入</span>
        <div class="typing-dots">
          <span></span><span></span><span></span>
        </div>
      </div>

      <!-- 底部新消息提醒 -->
      <div class="new-msg-badge" v-if="state.newMsgCount > 0" @click="scrollToBottomAndClear">
        ↓ {{ state.newMsgCount }} 条新消息
      </div>
    </div>

    <!-- AI摘要面板 -->
    <div v-if="state.showSummary" class="ai-summary-panel">
      <div class="summary-header">
        <span class="summary-title">
          <el-icon><MagicStick /></el-icon>AI 聊天摘要
        </span>
        <el-button link size="small" @click="state.showSummary = false; state.summaryContent = ''">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      <div v-if="state.summaryLoading" style="color:#8b5cf6;font-size:13px">
        <el-icon class="is-loading"><Loading /></el-icon> 正在生成摘要...
      </div>
      <div v-else class="summary-content">{{ state.summaryContent || '暂无内容' }}</div>
    </div>

    <!-- AI辅助回复建议 -->
    <div v-if="state.aiSuggestions.length > 0 && chat.type !== 3" class="ai-suggestions" style="padding: 6px 16px 0;">
      <span style="font-size:12px;color:#9ca3af;flex-shrink:0">💡 建议：</span>
      <span
        v-for="(sug, idx) in state.aiSuggestions"
        :key="idx"
        class="ai-suggestion-chip"
        @click="state.message = sug; state.aiSuggestions = []"
      >{{ sug }}</span>
      <el-button link size="small" @click="state.aiSuggestions = []" style="color:#9ca3af">×</el-button>
    </div>

    <!-- 回复引用条 -->
    <div v-if="state.replyMsg" style="display:flex;align-items:center;gap:8px;padding:6px 16px;background:rgba(124,58,237,0.06);border-top:2px solid #7c3aed;font-size:13px;">
      <el-icon style="color:#7c3aed"><ChatLineRound /></el-icon>
      <span style="color:#7c3aed;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">
        回复 {{ state.replyMsg.realName || '你' }}：{{ state.replyMsg.content }}
      </span>
      <el-button link @click="state.replyMsg = null">×</el-button>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <!-- 工具栏 -->
      <div class="input-tools">
        <div v-if="chat.type === 3" class="ai-tools">
          <el-button :type="state.history ? 'primary' : 'default'" title="历史对话" plain @click="state.history = !state.history" size="small">历史对话</el-button>
        </div>
        
        <div class="basic-tools">
          <!-- 表情 -->
          <el-popover placement="top-start" :width="380" trigger="click">
            <Picker :data="emojiIndex" :emojiSize="22" :showPreview="false" :infiniteScroll="false" :i18n="emojiI18n"
              :pickerStyles="{ height: '320px', borderRadius: '12px', boxShadow: '0 8px 32px rgba(124,58,237,0.2)' }"
              @select="handleEmoji" />
            <template #reference>
              <el-button title="表情" plain circle size="small" style="font-size:16px">😊</el-button>
            </template>
          </el-popover>

          <!-- 更多功能 -->
          <el-dropdown v-if="chat.type !== 3" placement="top" trigger="click">
            <el-button title="更多" plain circle size="small">
              <el-icon size="16"><Plus /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <span @click="$refs.imageUpload.$el.querySelector('input[type=file]').click()">
                    <el-icon><Picture /></el-icon> 图片
                  </span>
                  <el-upload ref="imageUpload" :action="state.uploadApi" :on-success="handleImageUpload" :before-upload="beforeImageUpload" :show-file-list="false" :accept="'image/*'" name="file" :headers="state.fileHeaders" style="display: none;" />
                </el-dropdown-item>
                <el-dropdown-item>
                  <span @click="$refs.fileUpload.$el.querySelector('input[type=file]').click()">
                    <el-icon><FolderOpened /></el-icon> 文件
                  </span>
                  <el-upload ref="fileUpload" :action="state.uploadApi" :on-success="handleFileUpload" :before-upload="beforeFileUpload" :accept="'.pdf,.doc,.docx,.xls,.xlsx,.txt,.ppt,.pptx'" :show-file-list="false" :headers="state.fileHeaders" name="file" style="display: none;" />
                </el-dropdown-item>
                <el-dropdown-item v-if="chat.type === 2" @click="openCreateVoteDialog">
                  <el-icon><DataAnalysis /></el-icon> 发起投票
                </el-dropdown-item>
                <el-dropdown-item @click="sendLocationMessage">
                  <el-icon><Location /></el-icon> 位置分享
                </el-dropdown-item>
                <el-dropdown-item v-if="chat.type !== 3" @click="state.showScheduledDialog = true">
                  <el-icon><Clock /></el-icon> 定时发送
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- AI功能按钮（群聊显示） -->
          <el-dropdown v-if="chat.type === 2" placement="top" trigger="click">
            <el-button title="AI功能" size="small" style="background:linear-gradient(135deg,#7c3aed,#a855f7);color:white;border:none;border-radius:8px;padding:0 10px">
              <el-icon><MagicStick /></el-icon> AI
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="generateSummary(false)"><el-icon><Document /></el-icon> 生成摘要</el-dropdown-item>
                <el-dropdown-item @click="getAiSuggestions"><el-icon><ChatLineRound /></el-icon> 回复建议</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 输入框和发送按钮 -->
      <div class="input-main" style="display:flex;align-items:flex-end;gap:10px;width:100%;box-sizing:border-box;">
        <el-input
          v-model="state.message"
          type="textarea"
          :rows="3"
          :placeholder="chat.type === 3 ? '向AI助手提问...' : '输入消息，按 Enter 发送'"
          @keydown.enter.exact.prevent="sendMessage()"
          @keydown.shift.enter.prevent="state.message += '\n'"
          @input="handleTyping"
          resize="none"
          class="message-input"
          style="flex:1;min-width:0;"
        />
        <div style="display:flex;flex-direction:column;gap:6px;flex-shrink:0">
          <el-button
            type="primary"
            @click="sendMessage()"
            :disabled="!state.message.trim()"
            size="default"
            style="min-width:80px;height:40px;background:linear-gradient(135deg,#7c3aed,#9333ea);border:none;border-radius:10px;font-weight:600;letter-spacing:1px"
          >
            发送
          </el-button>
        </div>
      </div>
      
      <div class="hint-text" style="display:flex;justify-content:space-between;align-items:center">
        <span style="font-size:11px;color:#9ca3af">Enter 发送 · Shift+Enter 换行</span>
        <span v-if="chat.type === 2 && state.messages.length >= 20 && !state.summaryGenerated" 
          style="font-size:11px;color:#8b5cf6;cursor:pointer" 
          @click="generateSummary(true)">
          🤖 已有{{ state.messages.length }}条消息，生成AI摘要
        </span>
      </div>
    </div>

    <!-- 定时发送弹窗 -->
    <el-dialog v-model="state.showScheduledDialog" title="定时发送消息" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="消息内容">
          <el-input v-model="state.scheduledContent" type="textarea" :rows="3" placeholder="请输入要定时发送的消息" />
        </el-form-item>
        <el-form-item label="发送时间">
          <el-date-picker
            v-model="state.scheduledTime"
            type="datetime"
            placeholder="选择定时发送时间"
            style="width:100%"
            :disabled-date="(d) => d < new Date()"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="state.showScheduledDialog = false">取消</el-button>
        <el-button type="primary" @click="sendScheduledMessage" style="background:linear-gradient(135deg,#7c3aed,#9333ea);border:none">确认定时发送</el-button>
      </template>
    </el-dialog>

    <!-- 创建投票弹窗 -->
    <el-dialog v-model="state.showVoteDialog" title="发起群投票" width="480px" destroy-on-close>
      <el-form :model="state.voteForm" label-width="80px">
        <el-form-item label="投票主题">
          <el-input v-model="state.voteForm.title" placeholder="请输入投票主题" />
        </el-form-item>
        <el-form-item label="选项">
          <div style="display:flex;flex-direction:column;gap:8px;width:100%">
            <div v-for="(opt, idx) in state.voteForm.options" :key="idx" style="display:flex;gap:8px">
              <el-input v-model="state.voteForm.options[idx]" :placeholder="'选项 ' + (idx+1)" />
              <el-button link @click="state.voteForm.options.splice(idx,1)" v-if="state.voteForm.options.length > 2">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button dashed @click="state.voteForm.options.push('')" v-if="state.voteForm.options.length < 8">
              + 添加选项
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="state.voteForm.expireTime" type="datetime" placeholder="可选，不填则永不过期" style="width:100%" />
        </el-form-item>
        <el-form-item label="其他">
          <el-checkbox v-model="state.voteForm.multiChoice">允许多选</el-checkbox>
          <el-checkbox v-model="state.voteForm.anonymous" style="margin-left:16px">匿名投票</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="state.showVoteDialog = false">取消</el-button>
        <el-button type="primary" @click="createVote" style="background:linear-gradient(135deg,#7c3aed,#9333ea);border:none">发起投票</el-button>
      </template>
    </el-dialog>
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

<style scoped lang="scss">
/* ========== 全局动效 ========== */
@keyframes slideInUp {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.4; }
}
@keyframes typing-dot {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-4px); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.85); }
  to   { opacity: 1; transform: scale(1); }
}

/* ========== 聊天容器 ========== */
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: var(--chat-bg-color, #f0f2f5);
  border: 1px solid rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  border-radius: 0 12px 12px 0;
  overflow: hidden;
}

/* 空状态样式 */
.chat-container.empty-state1 {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: linear-gradient(135deg, #f5f7ff 0%, #ede9fe 100%);
}

.empty-content1 {
  text-align: center;
  animation: scaleIn 0.5s ease;
}

.empty-logo {
  margin-bottom: 24px;
}

.app-logo {
  width: 200px;
  height: auto;
  filter: drop-shadow(0 8px 24px rgba(124, 58, 237, 0.2));
  animation: pulse 3s ease-in-out infinite;
}

.empty-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #7c3aed, #a855f7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.empty-subtitle {
  font-size: 15px;
  color: #8b5cf6;
  font-weight: 400;
  opacity: 0.8;
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
  padding: 12px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f7ff 100%);
  border-bottom: 1px solid rgba(124, 58, 237, 0.12);
  box-shadow: 0 2px 12px rgba(124, 58, 237, 0.08);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.chat-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 2px 0;
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8b5cf6;
  margin: 0;
}

/* 聊天消息区域 */
.chat-window {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--chat-bg-color, #f0f2f5);
  padding: 20px 16px;
  min-height: 200px;
  position: relative;
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(124, 58, 237, 0.2);
    border-radius: 2px;
    &:hover { background: rgba(124, 58, 237, 0.4); }
  }
  scrollbar-width: thin;
  scrollbar-color: rgba(124, 58, 237, 0.2) transparent;
  transition: background-color 0.3s ease;
}

/* 输入区域 */
.input-area {
  padding: 12px 16px 10px;
  background: linear-gradient(180deg, #ffffff 0%, #faf9ff 100%);
  border-top: 1px solid rgba(124, 58, 237, 0.1);
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 -4px 16px rgba(124, 58, 237, 0.06);
}

/* 工具栏 */
.input-tools {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  padding: 2px 0;
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

/* ========== 消息气泡布局 ========== */
.left {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 10px;
  animation: slideInUp 0.25s ease;
}

.right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 10px;
  animation: slideInUp 0.25s ease;
}

/* 头像样式 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2px solid rgba(255,255,255,0.8);
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
  object-fit: cover;
}

/* 消息容器样式 */
.message-container {
  position: relative;
  max-width: 68%;
}

/* 左侧消息容器（接收方） */
.left .message-container {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-radius: 4px 18px 18px 18px;
  padding: 10px 14px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255,255,255,0.6);
  position: relative;
}

/* 右侧消息容器（发送方） */
.right .message-container {
  background: linear-gradient(135deg, #7c3aed 0%, #9333ea 100%);
  border-radius: 18px 4px 18px 18px;
  padding: 10px 14px;
  box-shadow: 0 4px 14px rgba(124, 58, 237, 0.35);
  position: relative;
}

/* 左侧气泡小三角 */
.left .message-container::before {
  content: '';
  position: absolute;
  top: 12px;
  left: -6px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 6px solid rgba(255, 255, 255, 0.92);
}

/* 右侧气泡小三角 */
.right .message-container::after {
  content: '';
  position: absolute;
  top: 12px;
  right: -6px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-left: 6px solid #9333ea;
}

/* 左侧消息包装器 */
.left .message-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-direction: column;
}

/* 右侧消息包装器 */
.right .message-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  flex-direction: column;
}

/* 文本消息内容的包装器 */
.message-container .message-wrapper {
  display: block;
  gap: 0;
}

/* 文本消息内容 - 接收方 */
.left .message {
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.6;
  margin: 0;
  font-size: 14px;
  color: #1e1b4b;
}

/* 文本消息内容 - 发送方 */
.right .message {
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.6;
  margin: 0;
  font-size: 14px;
  color: #ffffff;
}

/* 撤回消息样式 */
.recalled-message {
  color: #9ca3af;
  font-style: italic;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.right .recalled-message {
  color: rgba(255,255,255,0.65);
}

/* 发送方名称 */
.sender-name {
  font-size: 12px;
  color: #7c3aed;
  margin-bottom: 4px;
  display: block;
  font-weight: 500;
}

/* ========== 时间戳 & 收藏按钮 ========== */
.timestamp-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 5px;
}

.timestamp {
  font-size: 11px;
  color: #9ca3af;
  line-height: 1;
}

.right .timestamp {
  color: rgba(255,255,255,0.6);
}

/* 已读状态 */
.read-status {
  font-size: 11px;
  color: #9ca3af;
}

.right .read-status {
  color: rgba(255,255,255,0.65);
}

.read-status.read {
  color: #7c3aed;
}

.right .read-status.read {
  color: rgba(255,255,255,0.9);
}

.favorite-btn-inline {
  padding: 0;
  margin: 0;
  min-width: auto;
  height: auto;
  line-height: 1;
  font-size: 12px;
  color: #9ca3af;
  transition: all 0.2s;
  background: none !important;
  border: none !important;
}

.favorite-btn-inline:hover {
  color: #f59e0b;
  transform: scale(1.15);
}

.favorite-active {
  color: #f59e0b !important;
}

/* ========== 正在输入提示 ========== */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 16px 8px;
  font-size: 13px;
  color: #8b5cf6;
  animation: fadeIn 0.3s ease;
}

.typing-dots {
  display: flex;
  gap: 3px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background: #8b5cf6;
  border-radius: 50%;
  display: inline-block;
  animation: typing-dot 0.8s ease infinite;
  &:nth-child(2) { animation-delay: 0.15s; }
  &:nth-child(3) { animation-delay: 0.3s; }
}

/* ========== 底部新消息提醒 ========== */
.new-msg-badge {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #7c3aed, #9333ea);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(124, 58, 237, 0.4);
  z-index: 10;
  animation: scaleIn 0.3s ease;
  white-space: nowrap;
  transition: all 0.2s;
  &:hover { transform: translateX(-50%) scale(1.05); }
}

/* ========== AI辅助回复建议 ========== */
.ai-suggestions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 6px 0;
  animation: fadeIn 0.3s ease;
}

.ai-suggestion-chip {
  background: linear-gradient(135deg, rgba(124,58,237,0.08), rgba(147,51,234,0.12));
  border: 1px solid rgba(124, 58, 237, 0.25);
  color: #7c3aed;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    background: linear-gradient(135deg, #7c3aed, #9333ea);
    color: white;
    border-color: transparent;
    transform: translateY(-1px);
    box-shadow: 0 3px 10px rgba(124, 58, 237, 0.3);
  }
}

/* ========== AI摘要面板 ========== */
.ai-summary-panel {
  margin: 8px 16px;
  background: linear-gradient(135deg, rgba(124,58,237,0.05), rgba(147,51,234,0.08));
  border: 1px solid rgba(124, 58, 237, 0.2);
  border-radius: 12px;
  padding: 12px 16px;
  animation: slideInUp 0.3s ease;
}

.summary-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.summary-title {
  font-size: 13px;
  font-weight: 600;
  color: #7c3aed;
  display: flex;
  align-items: center;
  gap: 6px;
}

.summary-content {
  font-size: 13px;
  color: #374151;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* ========== 投票消息 ========== */
.vote-message {
  background: linear-gradient(135deg, #f5f3ff, #ede9fe);
  border: 1px solid rgba(124, 58, 237, 0.2);
  border-radius: 12px;
  padding: 14px;
  min-width: 240px;
  max-width: 300px;
}

.vote-title {
  font-size: 14px;
  font-weight: 600;
  color: #4c1d95;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.vote-option {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding: 6px 10px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid rgba(124, 58, 237, 0.15);
  transition: all 0.2s;
  &:hover { border-color: #7c3aed; background: rgba(124,58,237,0.05); }
  &.selected { border-color: #7c3aed; background: rgba(124,58,237,0.1); }
}

.vote-progress {
  flex: 1;
  height: 4px;
  background: rgba(124, 58, 237, 0.15);
  border-radius: 2px;
  overflow: hidden;
}

.vote-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #7c3aed, #a855f7);
  border-radius: 2px;
  transition: width 0.5s ease;
}

.vote-count-text {
  font-size: 12px;
  color: #7c3aed;
  white-space: nowrap;
}

/* ========== 位置消息 ========== */
.location-message {
  background: linear-gradient(135deg, #ecfdf5, #d1fae5);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 12px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 200px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2); }
}

.location-icon {
  font-size: 28px;
  flex-shrink: 0;
}

.location-info {
  flex: 1;
}

.location-name {
  font-size: 14px;
  font-weight: 600;
  color: #065f46;
}

.location-address {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

/* ========== 图片消息 ========== */
:deep(.image-message) {
  max-width: 240px;
  max-height: 200px;
  border-radius: 10px;
  display: block;
  object-fit: cover;
  cursor: zoom-in;
  transition: transform 0.2s;
  &:hover { transform: scale(1.02); }
}

/* ========== 文件消息 ========== */
.file-message {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
  min-width: 200px;
}

.file-icon {
  font-size: 32px;
  color: #7c3aed;
  flex-shrink: 0;
}

.right .file-icon { color: rgba(255,255,255,0.9); }

.file-info { flex: 1; min-width: 0; }

.file-name {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0 0 2px;
  color: #1e1b4b;
}

.right .file-name { color: white; }

.file-size {
  font-size: 11px;
  color: #9ca3af;
  margin: 0;
}

.right .file-size { color: rgba(255,255,255,0.65); }

/* ========== 群公告样式 ========== */
.group-announcement {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.08), rgba(245, 158, 11, 0.12));
  border-bottom: 1px solid rgba(245, 158, 11, 0.2);
  padding: 8px 20px;
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
  color: #f59e0b;
  font-size: 16px;
  flex-shrink: 0;
}

.announcement-text {
  color: #92400e;
  font-size: 13px;
  line-height: 1.5;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 加载历史消息 ========== */
.load-more {
  text-align: center;
  padding: 8px 0 16px;
}

/* ========== 定时发送标签 ========== */
.scheduled-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: rgba(245,158,11,0.15);
  color: #d97706;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  margin-top: 4px;
}

/* ========== 弹窗样式 ========== */
:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #7c3aed 0%, #9333ea 100%);
  border-radius: 12px 12px 0 0;
  padding: 16px 20px;
}

:deep(.el-dialog__title) {
  color: white !important;
  font-weight: 600;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
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
import request from '@/utils/request.js'

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
  isGroupOwner: false,

  // 正在输入
  typingUser: '',
  typingAvatar: '',
  typingTimer: null,

  // 新消息提醒
  newMsgCount: 0,
  isAtBottom: true,

  // 撤回相关
  recallTimeLimit: 2 * 60 * 1000, // 2分钟内可撤回

  // 回复消息
  replyMsg: null,

  // AI 摘要
  showSummary: false,
  summaryContent: '',
  summaryLoading: false,
  summaryGenerated: false,

  // AI 建议回复
  aiSuggestions: [],

  // 定时发送
  showScheduledDialog: false,
  scheduledContent: '',
  scheduledTime: null,

  // 创建投票
  showVoteDialog: false,
  voteForm: {
    title: '',
    options: ['', ''],
    expireTime: null,
    multiChoice: false,
    anonymous: false
  }
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
    if (!newVal) return

    // 处理正在输入消息
    if (newVal.type === 'TYPING' && props.chat && newVal.chatId === props.chat.chatId) {
      if (newVal.sendId !== state.currentUser.id) {
        state.typingUser = newVal.senderName || '对方'
        state.typingAvatar = newVal.senderAvatar || ''
        clearTimeout(state.typingTimer)
        state.typingTimer = setTimeout(() => { state.typingUser = '' }, 3000)
      }
      return
    }

    // 处理消息撤回
    if (newVal.type === 'RECALL') {
      const msgIndex = state.messages.findIndex(m => m.id === newVal.msgId)
      if (msgIndex >= 0) {
        state.messages[msgIndex].isRecalled = 1
        state.messages = [...state.messages]
      }
      return
    }

    // 处理系统公告
    if (newVal.type === 'SYSTEM_NOTICE') {
      ElMessage({
        message: `📢 ${newVal.title || '系统公告'}：${newVal.content}`,
        type: 'warning',
        duration: 8000,
        showClose: true
      })
      return
    }

    // 处理禁言通知
    if (newVal.type === 'MUTE_NOTICE') {
      ElMessage.warning(newVal.content)
      return
    }

    // 强制下线
    if (newVal.type === 'FORCE_LOGOUT') {
      ElMessage.error(newVal.content || '您的账号已被封禁')
      setTimeout(() => { window.location.href = '/login' }, 2000)
      return
    }

    if (props.chat && newVal.chatId === props.chat.chatId) {
      if (newVal.sendId === state.currentUser.id) return

      const messageType = ['text', 'image', 'file', 'vote', 'location']
      if (!messageType.includes(newVal.contentType)) return

      // 清除正在输入提示
      state.typingUser = ''

      state.messages.push(newVal)
      messageApi.clearUnreadMessage(newVal.chatId)

      // 自动生成摘要（超过20条）
      if (props.chat?.type === 2 && state.messages.length >= 20 && !state.summaryGenerated) {
        generateSummary(true)
      }

      // 判断是否在底部
      if (state.isAtBottom) {
        scrollToBottom()
      } else {
        state.newMsgCount++
      }
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

// 检测打字，发送 typing 事件
let typingDebounce = null
const handleTyping = () => {
  if (!props.chat || props.chat.type === 3) return
  clearTimeout(typingDebounce)
  typingDebounce = setTimeout(() => {
    websocket.sendMessage({
      type: 'TYPING',
      chatId: props.chat.chatId,
      sendId: state.currentUser.id,
      senderName: state.currentUser.realName,
      senderAvatar: state.currentUser.avatar
    })
  }, 300)
}

// 滚动到底部并清除提醒
const scrollToBottomAndClear = () => {
  state.newMsgCount = 0
  scrollToBottom()
}

// 获取图片预览列表
const getImagePreviewList = (src) => [src].filter(Boolean)

// 判断是否可撤回（2分钟内自己发送的消息）
const canRecall = (msg) => {
  if (msg.sendId !== state.currentUser.id) return false
  const createTime = new Date(msg.createTime).getTime()
  return Date.now() - createTime < state.recallTimeLimit
}

// 撤回消息
const handleRecall = (msg) => {
  ElMessageBox.confirm('确定撤回这条消息吗？', '撤回消息', {
    confirmButtonText: '撤回',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const recallData = {
      type: 'RECALL',
      msgId: msg.id,
      chatId: props.chat.chatId,
      sendId: state.currentUser.id
    }
    websocket.sendMessage(recallData)
    const msgIndex = state.messages.findIndex(m => m.id === msg.id)
    if (msgIndex >= 0) {
      state.messages[msgIndex].isRecalled = 1
      state.messages = [...state.messages]
    }
    ElMessage.success('消息已撤回')
  }).catch(() => {})
}

// 回复消息
const handleReply = (msg) => {
  state.replyMsg = msg
}

// 生成AI摘要
const generateSummary = async (auto = false) => {
  if (state.summaryLoading) return
  state.showSummary = true
  state.summaryLoading = true
  state.summaryContent = ''
  state.summaryGenerated = true

  try {
    const res = await fetch(
      import.meta.env.VITE_APP_API_URL + 'ai/summary',
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + sessionStorage.getItem('token') },
        body: JSON.stringify({ chatId: props.chat.chatId, count: Math.min(state.messages.length, 50) })
      }
    )
    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8')
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      chunk.split('\n').forEach(line => {
        if (line.startsWith('data:')) {
          const d = line.substring(5).trim()
          if (d && d !== '[DONE]') {
            try {
              const json = JSON.parse(d)
              const content = json.choices?.[0]?.delta?.content
              if (content) state.summaryContent += content
            } catch {}
          }
        }
      })
    }
  } catch (e) {
    state.summaryContent = '摘要生成失败，请重试'
  } finally {
    state.summaryLoading = false
  }
}

// 获取AI回复建议
const getAiSuggestions = async () => {
  const lastMsg = state.messages.slice(-3).map(m => m.content || '').join('\n')
  try {
    const res = await request({
      url: '/ai/suggest',
      method: 'post',
      data: { chatId: props.chat.chatId, lastMessage: lastMsg }
    })
    if (res.code === 200 && Array.isArray(res.data)) {
      state.aiSuggestions = res.data.slice(0, 3)
    }
  } catch {}
}

// 发送定时消息
const sendScheduledMessage = () => {
  if (!state.scheduledContent.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  if (!state.scheduledTime) {
    ElMessage.warning('请选择发送时间')
    return
  }
  const delay = new Date(state.scheduledTime).getTime() - Date.now()
  if (delay <= 0) {
    ElMessage.warning('请选择未来的时间')
    return
  }
  const content = state.scheduledContent
  const timeStr = new Date(state.scheduledTime).toLocaleString('zh-CN')
  ElMessage.success(`消息将在 ${timeStr} 发送`)
  state.showScheduledDialog = false
  const msg = buildMessage(content)
  msg.scheduledTime = timeStr
  state.messages.push(msg)
  setTimeout(() => {
    websocket.sendMessage(msg)
  }, delay)
  state.scheduledContent = ''
  state.scheduledTime = null
}

// 发送位置消息
const sendLocationMessage = () => {
  if (!navigator.geolocation) {
    ElMessage.warning('您的浏览器不支持定位')
    return
  }
  ElMessage.info('正在获取位置...')
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      const { latitude, longitude } = pos.coords
      const locationData = JSON.stringify({
        lat: latitude,
        lng: longitude,
        name: '我的位置',
        address: `${latitude.toFixed(4)}, ${longitude.toFixed(4)}`
      })
      const msg = buildMessage(locationData, 'location')
      websocket.sendMessage(msg)
      state.messages.push(msg)
      scrollToBottom()
    },
    () => ElMessage.error('获取位置失败，请检查浏览器定位权限')
  )
}

// 打开位置
const openLocation = (msg) => {
  const loc = parseLocationContent(msg.content)
  if (loc.lat && loc.lng) {
    window.open(`https://maps.google.com/?q=${loc.lat},${loc.lng}`, '_blank')
  }
}

// 解析位置内容
const parseLocationContent = (content) => {
  try { return JSON.parse(content) || {} }
  catch { return {} }
}

// 解析投票内容
const parseVoteContent = (content) => {
  try { return JSON.parse(content) || { title: '投票', options: [] } }
  catch { return { title: '投票', options: [] } }
}

// 计算投票百分比
const getVotePercent = (option, vote) => {
  const total = (vote.options || []).reduce((sum, o) => sum + (o.voteCount || 0), 0)
  if (total === 0) return 0
  return Math.round((option.voteCount || 0) / total * 100)
}

// 打开投票详情
const openVoteDialog = async (msg) => {
  const voteData = parseVoteContent(msg.content)
  if (voteData.id) {
    try {
      const res = await request({ url: `/vote/${voteData.id}`, method: 'get' })
      if (res.code === 200) {
        ElMessageBox.alert(
          `<b>${res.data.title}</b><br>` + (res.data.options || []).map(o =>
            `${o.selected ? '✅' : '○'} ${o.content} (${o.voteCount || 0}票)`
          ).join('<br>'),
          '投票详情',
          { dangerouslyUseHTMLString: true }
        )
      }
    } catch {}
  }
}

// 打开创建投票弹窗
const openCreateVoteDialog = () => {
  state.voteForm = { title: '', options: ['', ''], expireTime: null, multiChoice: false, anonymous: false }
  state.showVoteDialog = true
}

// 创建投票并发送
const createVote = async () => {
  if (!state.voteForm.title.trim()) {
    ElMessage.warning('请输入投票主题')
    return
  }
  const validOptions = state.voteForm.options.filter(o => o.trim())
  if (validOptions.length < 2) {
    ElMessage.warning('至少需要2个有效选项')
    return
  }
  try {
    const res = await request({
      url: '/vote/create',
      method: 'post',
      data: {
        chatId: props.chat.chatId,
        title: state.voteForm.title,
        options: validOptions,
        expireTime: state.voteForm.expireTime ? new Date(state.voteForm.expireTime).toISOString() : null,
        multiChoice: state.voteForm.multiChoice ? 1 : 0,
        anonymous: state.voteForm.anonymous ? 1 : 0
      }
    })
    if (res.code === 200) {
      const voteMsg = buildMessage(JSON.stringify(res.data), 'vote')
      websocket.sendMessage(voteMsg)
      state.messages.push(voteMsg)
      scrollToBottom()
      state.showVoteDialog = false
      ElMessage.success('投票已发起')
    }
  } catch {
    ElMessage.error('发起投票失败')
  }
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