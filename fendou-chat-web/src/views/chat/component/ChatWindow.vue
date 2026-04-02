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
        <!-- 撤回消息：居中系统通知，不显示气泡 -->
        <div v-if="msg.isRecalled === 1" class="recall-system-notice">
          <span>
            <el-icon style="vertical-align: middle; margin-right: 3px;"><Delete /></el-icon>
            {{ msg.sendId === state.currentUser.id ? '你' : (msg.realName || '对方') }}撤回了一条消息
          </span>
        </div>

        <!-- 接收方消息 -->
        <div class="left" v-else-if="msg.sendId !== state.currentUser.id">
          <img :src="msg.chatAvatar" class="avatar" />
          <div class="message-wrapper">
            <span v-if="chat.type === 2" class="sender-name">{{ msg.realName }}</span>
            <div class="message-container">
              <!-- 回复引用块（接收方） -->
              <div v-if="getMsgInfo(msg.content).quote" class="reply-quote reply-quote-left">
                <span class="reply-quote-sender">{{ getMsgInfo(msg.content).quote.name }}</span>
                <span class="reply-quote-text">{{ getMsgInfo(msg.content).quote.text }}</span>
              </div>
              <el-dropdown placement="bottom-start" trigger="contextmenu">
                <p v-if="msg.reasoningContent && msg.reasoningContent.trim() !== ''" class="reasoning-content">{{ msg.reasoningContent }}</p>

                <!-- 文本 -->
                <div v-if="msg.contentType === 'text'">
                  <p class="message">{{ getMsgInfo(msg.content).text }}</p>
                </div>
                <!-- 图片 (接收方) -->
                <div v-else-if="msg.contentType === 'image'" class="image-wrapper">
                  <el-image
                    :src="msg.content"
                    :preview-src-list="getImagePreviewList(msg.content)"
                    preview-teleported
                    fit="cover"
                    :initial-index="0"
                  />
                </div>
                <!-- 文件 -->
                <div v-else-if="msg.contentType === 'file'" class="file-message">
                  <el-icon class="file-icon"><Document /></el-icon>
                  <div class="file-info">
                    <p class="file-name">{{ msg.fileName }}</p>
                    <p class="file-size">{{ formatFileSize(msg.fileSize) }}</p>
                  </div>
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
              <el-button v-if="msg.contentType === 'file'" class="download-btn-inline" @click="downloadFile(msg.content)" title="下载" link>
                <el-icon><Download /></el-icon>
              </el-button>
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
              <!-- 回复引用块（发送方） -->
              <div v-if="getMsgInfo(msg.content).quote" class="reply-quote reply-quote-right">
                <span class="reply-quote-sender">{{ getMsgInfo(msg.content).quote.name }}</span>
                <span class="reply-quote-text">{{ getMsgInfo(msg.content).quote.text }}</span>
              </div>
              <el-dropdown placement="bottom-end" trigger="contextmenu">
                <!-- 文本 -->
                <div v-if="msg.contentType === 'text'">
                  <p class="message">{{ getMsgInfo(msg.content).text }}</p>
                </div>
                <!-- 图片 (发送方) -->
                <div v-else-if="msg.contentType === 'image'" class="image-wrapper">
                  <el-image
                    :src="msg.content"
                    :preview-src-list="getImagePreviewList(msg.content)"
                    preview-teleported
                    fit="cover"
                    :initial-index="0"
                  />
                </div>
                <!-- 文件 -->
                <div v-else-if="msg.contentType === 'file'" class="file-message">
                  <el-icon class="file-icon"><Document /></el-icon>
                  <div class="file-info">
                    <p class="file-name">{{ msg.fileName }}</p>
                    <p class="file-size">{{ formatFileSize(msg.fileSize) }}</p>
                  </div>
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
              <!-- 非AI聊天显示发送状态；私聊额外显示已读回执 -->
              <span v-if="chat.type !== 3" class="read-status" :class="{read: msg._read && chat.type == 1}">
                {{ (msg._read && chat.type == 1) ? '✓✓' : '✓' }}
              </span>
              <span class="timestamp">{{ msg.createTime }}</span>
              <el-button v-if="msg.contentType === 'file'" class="download-btn-inline" @click="downloadFile(msg.content)" title="下载" link>
                <el-icon><Download /></el-icon>
              </el-button>
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
    <div v-if="state.aiSuggestionsLoading || state.aiSuggestions.length > 0" class="ai-suggestions" style="padding: 6px 16px 0;">
      <span style="font-size:12px;color:#9ca3af;flex-shrink:0">💡 建议：</span>
      <template v-if="state.aiSuggestionsLoading">
        <span style="font-size:12px;color:#8b5cf6;display:flex;align-items:center;gap:6px">
          <el-icon class="is-loading"><Loading /></el-icon> 思考中...
        </span>
      </template>
      <template v-else>
        <span
          v-for="(sug, idx) in state.aiSuggestions"
          :key="idx"
          class="ai-suggestion-chip"
          @click="state.message = sug; state.aiSuggestions = []"
        >{{ sug }}</span>
        <el-button link size="small" @click="state.aiSuggestions = []" style="color:#9ca3af">×</el-button>
      </template>
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
          <el-select v-model="state.currentRole" placeholder="选择AI角色" size="small" style="width: 140px; margin-left: 8px" @change="handleRoleChange">
            <el-option label="默认助手" value="" />
            <el-option label="🌹 情感大师" value="QGDS" />
            <el-option label="📚 文学大师" value="WXDS" />
            <el-option label="🎓 超级学霸" value="CXXB" />
            <el-option label="💪 健康助手" value="JKZS" />
            <el-option label="🏃 运动大师" value="YDDS" />
            <el-option label="📨 消息代理人" value="XXDLR" />
          </el-select>
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

          <!-- 图片上传 -->
          <el-tooltip v-if="chat.type !== 3" content="图片" placement="top">
            <el-button plain circle size="small"
              @click="$refs.imageUpload.$el.querySelector('input[type=file]').click()">
              <el-icon size="16"><Picture /></el-icon>
            </el-button>
          </el-tooltip>
          <el-upload ref="imageUpload" :action="state.uploadApi" :on-success="handleImageUpload"
            :before-upload="beforeImageUpload" :show-file-list="false" :accept="'image/*'"
            name="file" :headers="state.fileHeaders" style="display:none;" />

          <!-- 文件上传 -->
          <el-tooltip v-if="chat.type !== 3" content="文件" placement="top">
            <el-button plain circle size="small"
              @click="$refs.fileUpload.$el.querySelector('input[type=file]').click()">
              <el-icon size="16"><FolderOpened /></el-icon>
            </el-button>
          </el-tooltip>
          <el-upload ref="fileUpload" :action="state.uploadApi" :on-success="handleFileUpload"
            :before-upload="beforeFileUpload" :accept="'.pdf,.doc,.docx,.xls,.xlsx,.txt,.ppt,.pptx'"
            :show-file-list="false" :headers="state.fileHeaders" name="file" style="display:none;" />

          <!-- 发起投票（群聊）-->
          <el-tooltip v-if="chat.type === 2" content="发起投票" placement="top">
            <el-button plain circle size="small" @click="openCreateVoteDialog">
              <el-icon size="16"><DataAnalysis /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 位置分享 -->
          <el-tooltip v-if="chat.type !== 3" content="位置分享" placement="top">
            <el-button plain circle size="small" @click="sendLocationMessage">
              <el-icon size="16"><Location /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 定时发送 -->
          <el-tooltip v-if="chat.type !== 3" content="定时发送" placement="top">
            <el-button plain circle size="small" @click="state.showScheduledDialog = true">
              <el-icon size="16"><Clock /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- AI功能（群聊）-->
          <el-dropdown v-if="chat.type === 2" placement="top" trigger="click">
            <el-button title="AI功能" size="small"
              style="background:linear-gradient(135deg,#7c3aed,#a855f7);color:white;border:none;border-radius:8px;padding:0 10px">
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
    <!-- 投票详情/参与弹窗 -->
    <el-dialog v-model="state.voteModal.visible" title="投票" width="440px" destroy-on-close>
      <div v-if="state.voteModal.loading" style="text-align:center;padding:30px 0">
        <el-icon class="is-loading" style="font-size:28px;color:#7c3aed"><Loading /></el-icon>
        <p style="margin-top:10px;color:#9ca3af;font-size:13px">加载中...</p>
      </div>
      <template v-else-if="state.voteModal.data">
        <p style="font-weight:700;font-size:16px;margin:0 0 6px">{{ state.voteModal.data.title }}</p>
        <p style="font-size:12px;color:#9ca3af;margin:0 0 16px">
          <template v-if="state.voteModal.hasVoted">你已投票，以下是当前结果</template>
          <template v-else>{{ state.voteModal.data.multiChoice === 1 ? '多选' : '单选' }}，点击选项投票</template>
        </p>

        <!-- 已投票：展示结果进度条，同时允许改投 -->
        <div v-if="state.voteModal.hasVoted">
          <div v-for="opt in state.voteModal.data.options" :key="opt.id" style="margin-bottom:14px">
            <div style="display:flex;justify-content:space-between;font-size:14px;margin-bottom:5px">
              <span>
                <el-icon v-if="opt.selected" style="color:#7c3aed;margin-right:4px"><Check /></el-icon>
                {{ opt.content }}
              </span>
              <span style="color:#7c3aed;font-weight:600">{{ getVotePercent(opt, state.voteModal.data) }}%</span>
            </div>
            <div style="height:8px;background:#e5e7eb;border-radius:4px;overflow:hidden">
              <div :style="{width: getVotePercent(opt, state.voteModal.data) + '%'}"
                style="height:100%;background:linear-gradient(90deg,#7c3aed,#a855f7);border-radius:4px;transition:width 0.5s ease"></div>
            </div>
            <div style="font-size:11px;color:#9ca3af;margin-top:3px">{{ opt.voteCount || 0 }} 票</div>
          </div>
          <!-- 允许改投 -->
          <el-button link style="color:#7c3aed;font-size:13px;margin-top:8px" @click="state.voteModal.hasVoted = false; state.voteModal.selectedIds = []">
            <el-icon><Refresh /></el-icon> 修改我的投票
          </el-button>
        </div>

        <!-- 未投票：展示可选选项 -->
        <div v-else>
          <div
            v-for="opt in state.voteModal.data.options" :key="opt.id"
            class="vote-option"
            :class="{ selected: state.voteModal.selectedIds.includes(opt.id) }"
            @click="toggleVoteOption(opt.id)"
          >
            <span style="flex:1;font-size:14px">{{ opt.content }}</span>
            <el-icon v-if="state.voteModal.selectedIds.includes(opt.id)" style="color:#7c3aed"><Check /></el-icon>
          </div>
        </div>
      </template>

      <template #footer>
        <template v-if="!state.voteModal.hasVoted && state.voteModal.data">
          <el-button @click="state.voteModal.visible = false">取消</el-button>
          <el-button type="primary" @click="submitVote"
            :loading="state.voteModal.submitting"
            :disabled="state.voteModal.selectedIds.length === 0"
            style="background:linear-gradient(135deg,#7c3aed,#9333ea);border:none">
            确认投票
          </el-button>
        </template>
        <el-button v-else type="primary" @click="state.voteModal.visible = false"
          style="background:linear-gradient(135deg,#7c3aed,#9333ea);border:none">
          关闭
        </el-button>
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
/* ========== 动效 ========== */
@keyframes slideInUp {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50%       { opacity: 0.45; }
}
@keyframes typing-dot {
  0%, 100% { transform: translateY(0); }
  50%       { transform: translateY(-5px); }
}
@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}
@keyframes scaleIn {
  from { opacity: 0; transform: scale(0.88); }
  to   { opacity: 1; transform: scale(1); }
}

/* ========== 聊天容器 ========== */
.chat-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--chat-window-bg);
  overflow: hidden;
  font-family: var(--font-sans);
}

/* 空状态 */
.chat-container.empty-state1 {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.empty-content1 {
  text-align: center;
  animation: scaleIn 0.5s ease;
}

.empty-logo1 { margin-bottom: 24px; }

.app-logo {
  width: 180px;
  height: auto;
  filter: drop-shadow(0 8px 32px rgba(124, 58, 237, 0.3));
  animation: pulse 3.5s ease-in-out infinite;
}

.empty-title {
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 10px;
  background: var(--brand-gradient-3);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.04em;
}

.empty-subtitle {
  font-size: 14px;
  color: var(--text-muted);
  font-weight: 400;
}

/* 已选择聊天容器 */
.chat-container.selected {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
}

/* ========== 聊天头部 ========== */
.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: var(--chat-header-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  z-index: 10;
}

.chat-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 2px 0;
  letter-spacing: 0.02em;
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-primary);
  margin: 0;
}

/* ========== 消息区域 ========== */
.chat-window {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 20px 20px 10px;
  position: relative;
  scrollbar-width: thin;
  scrollbar-color: rgba(124,58,237,0.2) transparent;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-track { background: transparent; }
  &::-webkit-scrollbar-thumb {
    background: rgba(124,58,237,0.2);
    border-radius: 999px;
    &:hover { background: rgba(124,58,237,0.4); }
  }
}

/* ========== 输入区域 ========== */
.input-area {
  padding: 12px 16px 12px;
  background: var(--chat-input-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;
  transition: all 0.25s ease;
}

.input-tools {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.ai-tools {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 12px;
}

.basic-tools {
  display: flex;
  align-items: center;
  gap: 4px;
}

.input-main {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.message-input {
  flex: 1;
  min-width: 0;

  :deep(.el-textarea__inner) {
    background: var(--input-bg) !important;
    border: 1px solid var(--input-border) !important;
    border-radius: 12px !important;
    color: var(--text-primary) !important;
    font-family: var(--font-sans) !important;
    font-size: 14px !important;
    transition: all 0.2s ease !important;
    resize: none !important;

    &:focus {
      border-color: var(--input-focus-border) !important;
      box-shadow: var(--input-focus-shadow) !important;
    }
    &::placeholder { color: var(--text-muted) !important; }
  }
}

.hint-text {
  font-size: 11px;
  color: var(--text-muted);
}

/* ========== 气泡布局 ========== */
.left {
  display: flex;
  align-items: flex-start;
  margin-bottom: 18px;
  gap: 10px;
  animation: slideInUp 0.22s ease both;
}

.right {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  margin-bottom: 18px;
  gap: 10px;
  animation: slideInUp 0.22s ease both;
  position: relative;
}

.avatar {
  width: 38px; height: 38px;
  border-radius: 50%;
  flex-shrink: 0;
  border: 2px solid var(--border-default);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  object-fit: cover;
}

/* 消息包装器 */
.left .message-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  /* 左侧留出头像(38px)+间距(10px)的空间 */
  max-width: calc(100% - 48px);
  min-width: 0;
}

.right .message-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  /* 右侧同样留出头像(38px)+间距(10px)，防止内容盖住头像 */
  max-width: calc(100% - 48px);
  min-width: 0;
}

/* 消息容器 */
.message-container {
  position: relative;
  max-width: 100%;   /* 受父级 message-wrapper 约束，不再用固定70% */
  min-width: 0;
}

.left .message-container {
  background: var(--bubble-left-bg);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: 4px 16px 16px 16px;
  padding: 10px 14px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  border: 1px solid var(--bubble-left-border);

  &::before {
    content: '';
    position: absolute;
    top: 11px; left: -6px;
    border: 6px solid transparent;
    border-right-color: var(--bubble-left-bg);
    border-left: none;
  }
}

.right .message-container {
  background: var(--bubble-right-bg);
  border-radius: 16px 4px 16px 16px;
  padding: 10px 14px;
  box-shadow: 0 4px 16px rgba(124,58,237,0.35);

  &::after {
    content: '';
    position: absolute;
    top: 11px; right: -6px;
    border: 6px solid transparent;
    border-left-color: #9333ea;
    border-right: none;
  }
}

/* 文本气泡宽度限制（仅文字内容需要70%约束，特殊类型有自己的宽度） */
.left .message-container:has(.message),
.right .message-container:has(.message) {
  max-width: 70%;
}

/* 文本消息 */
.left .message {
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.65;
  margin: 0;
  font-size: 14px;
  color: var(--bubble-left-text);
}

.right .message {
  word-wrap: break-word;
  white-space: pre-wrap;
  line-height: 1.65;
  margin: 0;
  font-size: 14px;
  color: var(--bubble-right-text);
}

/* 撤回提示 */
.recall-system-notice {
  text-align: center;
  margin: 2px 0 14px;
  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    background: var(--bg-muted);
    color: var(--text-muted);
    font-size: 12px;
    padding: 4px 14px;
    border-radius: 20px;
    border: 1px solid var(--border-subtle);
  }
}

/* 回复引用块 */
.reply-quote {
  border-radius: 8px;
  padding: 6px 10px;
  margin-bottom: 6px;
  font-size: 12px;
  line-height: 1.5;
  display: flex;
  flex-direction: column;
  gap: 2px;
  opacity: 0.75;
  cursor: default;
}

.reply-quote-left {
  background: rgba(0,0,0,0.05);
  border-left: 3px solid var(--color-primary);
}

.reply-quote-right {
  background: rgba(255,255,255,0.15);
  border-left: 3px solid rgba(255,255,255,0.6);
}

.reply-quote-sender {
  font-weight: 600;
  color: var(--color-primary);
  font-size: 11px;
}

.reply-quote-right .reply-quote-sender { color: rgba(255,255,255,0.9); }

.reply-quote-text {
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 260px;
}

.reply-quote-right .reply-quote-text { color: rgba(255,255,255,0.8); }

/* 发件人名称 */
.sender-name {
  font-size: 11px;
  color: var(--color-primary);
  margin-bottom: 3px;
  display: block;
  font-weight: 600;
  letter-spacing: 0.02em;
}

/* ========== 时间戳 & 操作 ========== */
.timestamp-wrapper {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.left:hover .timestamp-wrapper,
.right:hover .timestamp-wrapper {
  opacity: 1;
}

.timestamp {
  font-size: 10px;
  color: var(--text-muted);
  line-height: 1;
}

/* 已读回执 */
.read-status {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 600;
}

.read-status.read {
  color: var(--color-primary);
}

.right .read-status.read {
  color: var(--color-primary-light);
}

.favorite-btn-inline {
  padding: 0; margin: 0;
  min-width: auto; height: auto; line-height: 1;
  font-size: 12px;
  color: var(--text-muted);
  transition: all 0.2s;
  background: none !important;
  border: none !important;
}

.favorite-btn-inline:hover { color: #f59e0b; transform: scale(1.2); }
.favorite-active { color: #f59e0b !important; }

/* ========== 正在输入 ========== */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 16px 8px;
  font-size: 12px;
  color: var(--color-primary);
  animation: fadeIn 0.3s ease;
}

.typing-dots {
  display: flex;
  gap: 3px;
  align-items: center;
}

.typing-dots span {
  width: 6px; height: 6px;
  background: var(--color-primary);
  border-radius: 50%;
  display: inline-block;
  animation: typing-dot 0.9s ease infinite;
  &:nth-child(2) { animation-delay: 0.18s; }
  &:nth-child(3) { animation-delay: 0.36s; }
}

/* ========== 新消息提醒 ========== */
.new-msg-badge {
  position: absolute;
  bottom: 16px; left: 50%;
  transform: translateX(-50%);
  background: var(--brand-gradient-2);
  color: #fff;
  padding: 7px 18px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(124,58,237,0.45);
  z-index: 10;
  animation: scaleIn 0.25s ease;
  white-space: nowrap;
  transition: all 0.2s;
  &:hover { transform: translateX(-50%) scale(1.06); }
}

/* ========== AI建议 ========== */
.ai-suggestions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  padding: 6px 0;
  animation: fadeIn 0.3s ease;
}

.ai-suggestion-chip {
  background: var(--color-primary-subtle);
  border: 1px solid var(--border-accent);
  color: var(--color-primary);
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  &:hover {
    background: var(--brand-gradient-2);
    color: #fff;
    border-color: transparent;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(124,58,237,0.35);
  }
}

/* ========== AI摘要面板 ========== */
.ai-summary-panel {
  margin: 8px 16px;
  background: var(--color-primary-subtle);
  border: 1px solid var(--border-accent);
  border-radius: 14px;
  padding: 12px 16px;
  animation: slideInUp 0.3s ease;
  backdrop-filter: blur(10px);
}

.summary-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.summary-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.summary-content {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
  white-space: pre-wrap;
}

/* ========== 投票 ========== */
.vote-message {
  background: var(--glass-bg);
  backdrop-filter: blur(12px);
  border: 1px solid var(--border-accent);
  border-radius: 14px;
  padding: 14px;
  width: 260px;
  max-width: 100%;   /* 不超过父级 message-wrapper 的宽度 */
  box-sizing: border-box;
}

.vote-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
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
  padding: 7px 10px;
  background: var(--bg-base);
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid var(--border-default);
  transition: all 0.2s;
  color: var(--text-primary);

  &:hover { border-color: var(--color-primary); background: var(--color-primary-subtle); }
  &.selected { border-color: var(--color-primary); background: var(--color-primary-subtle-2); }
}

.vote-progress {
  flex: 1;
  height: 4px;
  background: var(--border-default);
  border-radius: 2px;
  overflow: hidden;
}

.vote-progress-bar {
  height: 100%;
  background: var(--brand-gradient-2);
  border-radius: 2px;
  transition: width 0.5s ease;
}

.vote-count-text {
  font-size: 12px;
  color: var(--color-primary);
  white-space: nowrap;
}

/* ========== 位置消息 ========== */
.location-message {
  background: linear-gradient(135deg, rgba(16,185,129,0.08), rgba(5,150,105,0.06));
  border: 1px solid rgba(16,185,129,0.2);
  border-radius: 14px;
  padding: 12px 14px;
  display: flex;
  align-items: center;
  gap: 10px;
  width: 230px;
  max-width: 100%;   /* 不超过父级 message-wrapper 的宽度 */
  box-sizing: border-box;
  cursor: pointer;
  transition: all 0.2s;
  &:hover { box-shadow: 0 4px 16px rgba(16,185,129,0.2); }
}

.location-icon { font-size: 24px; flex-shrink: 0; }

.location-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.location-address {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

/* ========== 图片/位置/投票 气泡去除多余背景 ========== */
/* 这些类型有自己的样式，不需要外层气泡的渐变背景；
   max-width: 100% 使其受父级 message-wrapper 约束，不再溢出到头像 */
.left .message-container:has(.image-wrapper),
.right .message-container:has(.image-wrapper),
.left .message-container:has(.location-message),
.right .message-container:has(.location-message),
.left .message-container:has(.vote-message),
.right .message-container:has(.vote-message) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
  width: fit-content;
  max-width: 100%;
}
/* 去掉气泡三角箭头 */
.left .message-container:has(.image-wrapper)::before,
.right .message-container:has(.image-wrapper)::after,
.left .message-container:has(.location-message)::before,
.right .message-container:has(.location-message)::after,
.left .message-container:has(.vote-message)::before,
.right .message-container:has(.vote-message)::after {
  display: none !important;
}

/* 文件消息气泡宽度：受父级约束即可 */
.left .message-container:has(.file-message),
.right .message-container:has(.file-message) {
  max-width: 100%;
  width: fit-content;
}

/* ========== 图片消息 ========== */
.image-wrapper {
  line-height: 0;

  /* el-image 容器：相对约束，随父级宽度自适应 */
  :deep(.el-image) {
    max-width: min(240px, 100%);
    max-height: 200px;
    min-width: 60px;
    min-height: 50px;
    border-radius: 12px;
    overflow: hidden;
    display: block;
    cursor: zoom-in;
    transition: transform 0.22s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.22s;
    &:hover {
      transform: scale(1.03);
      box-shadow: 0 8px 28px rgba(0,0,0,0.22);
    }
  }

  /* 实际图片元素 */
  :deep(.el-image__inner) {
    max-width: min(240px, 100%);
    max-height: 200px;
    width: auto;
    height: auto;
    object-fit: cover;
    display: block;
    border-radius: 12px;
  }

  /* 加载中占位 */
  :deep(.el-image__placeholder),
  :deep(.el-image__error) {
    width: 120px;
    height: 90px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-subtle);
    border-radius: 12px;
    color: var(--text-muted);
    font-size: 12px;
  }
}

/* ========== 文件消息 ========== */
.file-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
  width: 260px;
  max-width: 100%;
  box-sizing: border-box;
}

.file-icon { font-size: 32px; color: var(--color-primary); flex-shrink: 0; }
.right .file-icon { color: rgba(255,255,255,0.9); }
.file-info { flex: 1; min-width: 0; }

.file-name {
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin: 0 0 3px;
  color: var(--bubble-left-text);
  max-width: 220px;
}

.right .file-name { color: #fff; }

.file-size { font-size: 11px; color: var(--text-muted); margin: 0; }
.right .file-size { color: rgba(255,255,255,0.65); }

/* ========== 下载按钮 ========== */
.download-btn-inline {
  padding: 0; margin: 0;
  min-width: auto; height: auto; line-height: 1;
  font-size: 13px;
  color: var(--color-accent) !important;
  transition: all 0.2s;
  background: none !important;
  border: none !important;
}
.download-btn-inline:hover { color: var(--color-primary) !important; transform: scale(1.2); }

/* ========== 群公告 ========== */
.group-announcement {
  background: linear-gradient(135deg, rgba(245,158,11,0.07), rgba(251,191,36,0.05));
  border-bottom: 1px solid rgba(245,158,11,0.2);
  padding: 8px 20px;
  flex-shrink: 0;
}

.announcement-content {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.announcement-icon { color: #f59e0b; font-size: 15px; flex-shrink: 0; }

.announcement-text {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.4;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ========== 加载更多 ========== */
.load-more {
  text-align: center;
  padding: 8px 0 16px;
  :deep(.el-link) {
    font-size: 12px;
    color: var(--color-primary);
  }
}

/* ========== 定时标签 ========== */
.scheduled-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: rgba(245,158,11,0.12);
  color: #d97706;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 12px;
  margin-top: 4px;
  /* 不超过消息包装器宽度，超长文字做截断 */
  max-width: 100%;
  box-sizing: border-box;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ========== 弹窗全局覆盖 ========== */
:deep(.el-dialog) {
  border-radius: 16px !important;
  overflow: hidden !important;
  background: var(--glass-bg-strong) !important;
  backdrop-filter: blur(24px) !important;
  border: 1px solid var(--glass-border) !important;
  box-shadow: var(--glass-shadow-lg) !important;
}

:deep(.el-dialog__header) {
  background: var(--brand-gradient-2) !important;
  padding: 14px 20px !important;
}

:deep(.el-dialog__title) {
  color: #fff !important;
  font-weight: 700 !important;
}

:deep(.el-dialog__headerbtn .el-dialog__close) {
  color: rgba(255,255,255,0.8) !important;
}

:deep(.el-textarea__inner) {
  background: var(--input-bg) !important;
  border-color: var(--input-border) !important;
  color: var(--text-primary) !important;
  resize: vertical;
  min-height: 120px;
  border-radius: 10px !important;
  font-family: var(--font-sans) !important;
}

/* AI思考内容 */
:deep(.reasoning-content) {
  background: var(--bg-muted);
  border-left: 3px solid var(--color-accent);
  padding: 8px 12px;
  border-radius: 0 8px 8px 0;
  font-size: 12px;
  color: var(--text-muted);
  font-style: italic;
  white-space: pre-wrap;
  margin-bottom: 6px;
  max-height: 120px;
  overflow-y: auto;
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

const handleRoleChange = () => {
  state.messages = []
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
  currentRole: '',

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
  aiSuggestionsLoading: false,

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
  },

  // 投票详情/参与弹窗
  voteModal: {
    visible: false,
    loading: false,
    data: null,        // { id, title, multiChoice, options:[{id,content,voteCount,selected}] }
    selectedIds: [],   // 当前选中的选项 ID 列表
    hasVoted: false,   // 当前用户是否已投票
    submitting: false
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
      state.currentRole = '' // 切换聊天时重置AI角色
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

    // 处理已读回执：对方已读，把本地发出的消息标记为已读
    if (newVal.type === 'READ_RECEIPT' && props.chat && newVal.chatId === props.chat.chatId) {
      state.messages.forEach(m => {
        if (m.sendId === state.currentUser.id && !m._read) {
          m._read = true
        }
      })
      state.messages = [...state.messages]
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

    // 处理投票结果更新（其他成员投票后广播）
    if (newVal.contentType === 'VOTE_ACTION' && props.chat && newVal.chatId === props.chat.chatId) {
      const msgIdx = state.messages.findIndex(m => {
        const v = parseVoteContent(m.content)
        return v.id === newVal.voteId
      })
      if (msgIdx !== -1) {
        const updatedMsg = { ...state.messages[msgIdx], content: JSON.stringify(newVal.voteData) }
        state.messages.splice(msgIdx, 1, updatedMsg)
        console.log('收到投票更新广播，已刷新聊天气泡')
      }
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

      // 私聊：自动向对方发送已读回执
      if (props.chat.type === 1) {
        websocket.sendMessage({
          type: 'READ_RECEIPT',
          chatId: props.chat.chatId,
          sendId: state.currentUser.id
        })
      }

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
    state.replyMsg = null  // 清除回复引用
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
          history: state.history,
          role: state.currentRole
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

/**
 * 解析消息 content：支持含回复引用的 JSON 格式，向下兼容普通文本
 * 返回 { text: string, quote: {name, text} | null }
 */
const getMsgInfo = (content) => {
  if (!content) return { text: '', quote: null }
  try {
    const obj = JSON.parse(content)
    if (obj && typeof obj === 'object' && '_t' in obj) {
      return { text: obj._t || '', quote: obj._r ? { name: obj._r.n, text: obj._r.c } : null }
    }
  } catch {}
  return { text: content, quote: null }
}

// 构建消息
const buildMessage = (content, contentType, file) => {
  let finalContent = content
  // 如果有回复引用，将引用信息嵌入 content JSON（利用现有 content 字段持久化）
  if (state.replyMsg && (!contentType || contentType === 'text')) {
    const quoteText = getMsgInfo(state.replyMsg.content).text  // 支持引用链
    const truncated = quoteText.length > 60 ? quoteText.slice(0, 60) + '…' : quoteText
    const quoteContent = state.replyMsg.contentType === 'text'
      ? truncated
      : state.replyMsg.contentType === 'image' ? '[图片]'
      : state.replyMsg.contentType === 'file' ? `[文件] ${state.replyMsg.fileName || ''}`
      : '[消息]'
    const quoteName = state.replyMsg.realName || (state.replyMsg.sendId === state.currentUser.id ? '你' : '对方')
    finalContent = JSON.stringify({ _t: content, _r: { n: quoteName, c: quoteContent } })
  }
  return {
    chatId: props.chat.chatId,
    sendId: state.currentUser.id,
    chatAvatar: state.currentUser.avatar,
    content: finalContent,
    contentType: contentType || 'text',
    createTime: getFormatDate(),
    fileId: file?.id,
    fileName: file?.originalName,
    fileSize: file?.fileSize,
    fileType: file?.fileType,
    isFavorite: false
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
  // 获取最后一条非自己发送的消息
  const othersMessages = state.messages.filter(m => m.sendId !== state.currentUser.id && m.contentType === 'text')
  if (othersMessages.length === 0) {
    ElMessage.warning('没有可回复的消息')
    return
  }
  const lastOtherMsg = othersMessages[othersMessages.length - 1]
  const lastMsgContent = lastOtherMsg.content || ''
  
  state.aiSuggestionsLoading = true
  try {
    const res = await request({
      url: '/ai/suggest',
      method: 'post',
      data: { chatId: props.chat.chatId, lastMessage: lastMsgContent }
    })
    if (res.code === 200 && Array.isArray(res.data)) {
      state.aiSuggestions = res.data.slice(0, 3)
      ElMessage.success('AI 回复建议已生成')
    } else {
      ElMessage.error('生成回复建议失败')
    }
  } catch {
    ElMessage.error('AI 服务异常，请稍后重试')
  } finally {
    state.aiSuggestionsLoading = false
  }
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
  // 使用较短格式，避免徽章文字溢出：如 "04-02 15:30"
  const d = new Date(state.scheduledTime)
  const timeStr = `${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
  ElMessage.success(`消息将在 ${timeStr} 发送`)
  state.showScheduledDialog = false

  // 构建本地展示消息（带定时徽章）
  const pendingMsg = buildMessage(content)
  pendingMsg.scheduledTime = timeStr
  pendingMsg._scheduledPending = true
  state.messages.push(pendingMsg)

  setTimeout(() => {
    // 关键修复：去掉 scheduledTime 再发送，否则后端会跳过 DB 保存
    // 同时发送方的本地气泡也要更新（移除定时徽章）
    const sendMsg = { ...pendingMsg }
    delete sendMsg.scheduledTime
    delete sendMsg._scheduledPending
    websocket.sendMessage(sendMsg)

    // 更新本地 pending 消息，移除定时徽章
    const idx = state.messages.findIndex(m => m === pendingMsg)
    if (idx !== -1) {
      state.messages[idx] = { ...state.messages[idx], scheduledTime: null, _scheduledPending: false }
    }
    scrollToBottom()
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

// 打开位置（使用高德地图）
const openLocation = (msg) => {
  const loc = parseLocationContent(msg.content)
  if (loc.lat && loc.lng) {
    // window.open(`https://maps.google.com/?q=${loc.lat},${loc.lng}`, '_blank')
    // 高德地图位置标注 URI API：https://lbs.amap.com/api/uri-api/guide/travel/marker
    window.open(`https://uri.amap.com/marker?position=${loc.lng},${loc.lat}&name=${encodeURIComponent(loc.name || '位置')}&src=FenDouChat&coordinate=wgs84&callnative=1`, '_blank')
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

// 打开投票参与/查看弹窗
const openVoteDialog = async (msg) => {
  const voteData = parseVoteContent(msg.content)
  if (!voteData.id) return

  state.voteModal.visible = true
  state.voteModal.loading = true
  state.voteModal.data = null
  state.voteModal.selectedIds = []
  state.voteModal.submitting = false
  state.voteModal.hasVoted = false

  try {
    const res = await request({ url: `/vote/${voteData.id}`, method: 'get' })
    if (res.code === 200) {
      state.voteModal.data = res.data
      // 优先使用后端返回的 hasVoted，兼容 options.selected 判断
      state.voteModal.hasVoted = res.data.hasVoted === true || (res.data.options || []).some(o => o.selected === true)
      console.log('投票详情加载:', res.data, '已投票:', state.voteModal.hasVoted)
    } else {
      ElMessage.error(res.msg || '获取投票详情失败')
      state.voteModal.visible = false
    }
  } catch (e) {
    console.error('获取投票详情失败:', e)
    ElMessage.error('获取投票详情失败')
    state.voteModal.visible = false
  } finally {
    state.voteModal.loading = false
  }
}

// 切换选项（单选 or 多选）
const toggleVoteOption = (optId) => {
  const ids = state.voteModal.selectedIds
  const idx = ids.indexOf(optId)
  if (state.voteModal.data?.multiChoice) {
    idx === -1 ? ids.push(optId) : ids.splice(idx, 1)
  } else {
    state.voteModal.selectedIds = idx === -1 ? [optId] : []
  }
}

// 提交投票 → 写入数据库
const submitVote = async () => {
  if (!state.voteModal.data || state.voteModal.selectedIds.length === 0) return
  state.voteModal.submitting = true
  try {
    const res = await request({
      url: `/vote/${state.voteModal.data.id}/vote`,
      method: 'post',
      data: { optionIds: state.voteModal.selectedIds }
    })
    if (res.code === 200) {
      ElMessage.success('投票成功！')
      // 刷新投票详情展示最新结果
      const detail = await request({ url: `/vote/${state.voteModal.data.id}`, method: 'get' })
      if (detail.code === 200) {
        state.voteModal.data = detail.data
        state.voteModal.hasVoted = detail.data.hasVoted === true || (detail.data.options || []).some(o => o.selected === true)
        state.voteModal.selectedIds = []
        
        // 同步更新聊天消息中的投票内容（展示最新票数）
        // 使用 splice 强制触发 Vue 响应式更新
        const msgIdx = state.messages.findIndex(m => {
          const v = parseVoteContent(m.content)
          return v.id === state.voteModal.data.id
        })
        if (msgIdx !== -1) {
          const updatedMsg = { ...state.messages[msgIdx], content: JSON.stringify(detail.data) }
          state.messages.splice(msgIdx, 1, updatedMsg)
        }
        
        // 通过 WebSocket 向群内其他成员广播投票更新（VOTE_ACTION 消息类型）
        const updatePayload = {
          chatId: props.chat.chatId,
          contentType: 'VOTE_ACTION',
          voteId: state.voteModal.data.id,
          voteData: detail.data
        }
        websocket.sendMessage(updatePayload)
        console.log('投票成功，广播更新到群内其他成员')
      }
    } else {
      ElMessage.error(res.msg || '投票失败')
    }
  } catch {
    ElMessage.error('投票请求失败，请稍后重试')
  } finally {
    state.voteModal.submitting = false
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