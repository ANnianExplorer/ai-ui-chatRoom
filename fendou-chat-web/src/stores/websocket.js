import { defineStore } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from './user.js'

/**
 * websocket 配置
 */
export const useWebsocketStore = defineStore('websocket', {
  state: () => ({
    socket: null, // websocket 实例
    message: null, // 接收到的消息
    reconnectTimer: null, // 重连定时器
    reconnectCount: 0, // 重连次数
    maxReconnectCount: 5, // 最大重连次数
    reconnectInterval: 5000, // 重连间隔(毫秒)
    heartbeatTimer: null, // 心跳定时器
    heartbeatInterval: 30000, // 心跳间隔(毫秒)
    heartbeatTimeoutTimer: null, // 心跳超时定时器
    heartbeatTimeout: 60000, // 心跳超时时间
    lastHeartbeatAck: 0, // 上次收到心跳确认时间
    onlineUsers: {}, // 在线用户列表，key为userId，value为状态
    groupOnlineCount: [] // 群聊在线人数
  }),
  actions: {
    connect(userId) {
      if (typeof WebSocket != 'function') {
        ElMessage.error('您的浏览器不支持Websocket通信协议, 更换浏览器再次使用!')
        return
      }

      // 已连接
      if (this.isConnected()) {
        return
      }

      const ws = sessionStorage.getItem('ws')

      // 连接
      this.socket = new WebSocket(`ws://${ws}/ws?userId=${userId}`)

      // 监听打开
      this.socket.onopen = () => {
        console.log('[websocket] connected')
        this.reconnectCount = 0 // 重置重连次数
        this.lastHeartbeatAck = Date.now() // 初始化心跳确认时间
        this.startHeartbeat() // 启动心跳检测
        this.startHeartbeatCheck() // 心跳检查
        this.reconnectSuccess() // 重连成功后获取最新状态
      }

      // 接收消息
      this.socket.onmessage = (e) => {
        const data = JSON.parse(e.data)
        if (data.type === 'HEARTBEAT_ACK') {
          this.lastHeartbeatAck = Date.now()
          return
        }

        // 直接在store中处理消息，确保状态更新
        if (data.type === 'INIT_USER_STATUS') {
          this.handleInitUserStatus(data)
        } else if (data.type === 'GROUP_ONLINE_COUNT') {
          this.handleGroupOnlineCount(data)
        } else if (data.type === 'USER_STATUS') {
          this.handleUserStatus(data)
        }

        // 所有消息统一更新 message，触发 ChatWindow 的 watch
        this.message = { ...data, _ts: Date.now() }
      }

      // 监听关闭
      this.socket.onclose = () => {
        console.log('[websocket] closed')
        this.stopHeartbeat() // 停止心跳检测
        this.stopHeartbeatCheck() // 停止心跳检查
        this.reconnect(userId) // 重连
      }

      // 监听错误
      this.socket.onerror = (e) => {
        console.log('[websocket] error', e)
        this.stopHeartbeat() // 停止心跳检测
        this.stopHeartbeatCheck() // 停止心跳检查
      }
    },

    // 检查是否已连接
    isConnected() {
      return this.socket && this.socket.readyState === WebSocket.OPEN
    },

    // 发送消息
    sendMessage(message) {
      if (!this.isConnected()) {
        ElMessage.warning('websocket 连接异常，请刷新页面重试！')
        return
      }

      this.socket.send(JSON.stringify(message))
    },

    // 重连
    reconnect(userId) {
      if (this.reconnectCount >= this.maxReconnectCount) {
        ElMessage.warning('重连次数过多，请刷新页面重试！')
        return
      }

      // 增加重连次数
      this.reconnectCount++

      // 定时重连
      this.reconnectTimer = setTimeout(() => {
        console.log(`[websocket] reconnect ${this.reconnectCount}/${this.maxReconnectCount}`)
        this.connect(userId)
      }, this.reconnectInterval)
    },

    // 停止重连
    stopReconnect() {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
        this.reconnectTimer = null
      }
    },

    // 心跳检测
    startHeartbeat() {
      this.stopHeartbeat() // 先清理之前的心跳定时器
      this.heartbeatTimer = setInterval(() => {
        this.sendMessage({ contentType: 'HEARTBEAT', timestamp: Date.now() })
      }, this.heartbeatInterval)
    },

    // 心跳检查
    startHeartbeatCheck() {
      this.stopHeartbeatCheck()
      this.heartbeatTimeoutTimer = setInterval(() => {
        if (Date.now() - this.lastHeartbeatAck > this.heartbeatTimeout) {
          console.warn('[websocket] heartbeat timeout, connection may be lost')
          // 关闭连接
          if (this.socket) {
            this.socket.close()
          }
        }
      }, this.heartbeatInterval) // 每个心跳间隔检查一次
    },

    // 停止心跳检测
    stopHeartbeat() {
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer)
        this.heartbeatTimer = null
      }
    },

    // 停止心跳检查
    stopHeartbeatCheck() {
      if (this.heartbeatTimeoutTimer) {
        clearInterval(this.heartbeatTimeoutTimer)
        this.heartbeatTimeoutTimer = null
      }
    },

    // 关闭连接
    close() {
      if (this.socket) {
        this.socket.close()
      }

      this.stopReconnect()
      this.stopHeartbeat()
      this.stopHeartbeatCheck()
    },
    
    // 处理初始化用户状态
    handleInitUserStatus(message) {
      const friends = message.friends || []
      
      console.log('Initializing user status with friends:', friends)
      
      // 从userStore获取当前用户ID
      const userStore = useUserStore()
      const currentUserId = parseInt(userStore.user?.id || 0)
      
      // 设置当前用户为在线状态
      this.onlineUsers[currentUserId] = 0
      console.log(`Setting current user ${currentUserId} as online`)
      
      // 初始化所有好友的状态
      friends.forEach(friend => {
        console.log(`Setting status for userId ${friend.userId} to ${friend.status}`)
        this.onlineUsers[friend.userId] = friend.status
      })
      
      console.log('Updated onlineUsers:', this.onlineUsers)
    },
    
    // 处理用户状态更新
    handleUserStatus(message) {
      // 更新用户状态，直接使用用户ID作为key
      console.log(`Updating status for userId ${message.userId} to ${message.status}`)
      this.onlineUsers[message.userId] = message.status
      console.log('Updated onlineUsers:', this.onlineUsers)
    },
    
    // 处理群聊在线人数更新
    handleGroupOnlineCount(message) {
      const onlineCount = message.onlineCount || message.count || 0
      const index = this.groupOnlineCount.findIndex(item => item.chatId === message.chatId)
      if (index >= 0) {
        this.groupOnlineCount[index].count = onlineCount
      } else {
        this.groupOnlineCount.push({
          chatId: message.chatId,
          count: onlineCount
        })
      }
    },
    
    // 格式化用户ID为chatId
    formatUserId(userId, currentUserId) {
      // 确保userId是数字类型
      const id = parseInt(userId)
      const currentId = parseInt(currentUserId || JSON.parse(sessionStorage.getItem('userInfo'))?.id)
      
      if (isNaN(id) || isNaN(currentId)) {
        console.error('Invalid userId or currentUserId:', userId, currentUserId)
        return `user-${userId}-${currentUserId}`
      }
      
      // 直接使用用户ID，不生成聊天ID
      // 因为后端广播的是用户ID，而不是聊天ID
      return id.toString()
    },
    
    // 重连成功后获取最新状态
    reconnectSuccess() {
      // 请求获取最新状态
      this.sendMessage({ type: 'REQUEST_USER_STATUS', timestamp: Date.now() })
      this.sendMessage({ type: 'REQUEST_GROUP_ONLINE_COUNT', timestamp: Date.now() })
    }
  },
  getters: {
    isOnline: (state) => (chatId, userStatus) => {
      // 对于好友聊天（type=1），chatId格式应该是 "user-用户1-用户2"
      // 其中用户1和用户2是按大小排列的
      if (typeof chatId === 'string' && chatId.startsWith('user-')) {
        try {
          // 首先检查用户是否被禁用
          // userStatus: 0表示启用，1表示禁用，undefined表示未知
          if (userStatus === 1) {
            console.log(`User is disabled, showing offline for chatId: ${chatId}`)
            return false
          }
          
          // 提取聊天ID中的用户ID
          const parts = chatId.split('-')
          if (parts.length < 3) {
            console.error('Invalid chatId format:', chatId)
            return false
          }
          
          // 解析chatId中的两个用户ID
          const userId1 = parseInt(parts[1])
          const userId2 = parseInt(parts[2])
          
          // 从userStore获取当前用户ID
          const userStore = useUserStore()
          const currentUserId = parseInt(userStore.user?.id || 0)
          
          console.log(`Current user ID from userStore: ${currentUserId}`)
          console.log(`Chat ID parts: ${userId1}, ${userId2}`)
          
          // 确定目标用户ID：与当前用户ID不同的那个
          let targetUserId
          if (currentUserId === userId1) {
            targetUserId = userId2
          } else if (currentUserId === userId2) {
            targetUserId = userId1
          } else {
            // 简单直接的逻辑：如果当前用户ID不在chatId中，返回false
            console.warn(`Current user ID ${currentUserId} not found in chatId ${chatId}, cannot determine online status`)
            return false
          }
          
          // 从onlineUsers中获取对方用户的在线状态
          // 0表示在线，1表示离线，undefined表示未初始化
          const onlineStatus = state.onlineUsers[targetUserId]
          const isOnline = onlineStatus === 0
          
          console.log(`Checking online status for chatId: ${chatId}`)
          console.log(`Current userId: ${currentUserId}, targetUserId: ${targetUserId}`)
          console.log(`Online status for ${targetUserId}: ${onlineStatus}, isOnline: ${isOnline}`)
          
          return isOnline
        } catch (error) {
          console.error('Error checking online status:', error)
          return false
        }
      } 
      // 对于其他类型的聊天或直接用户ID，返回离线
      return false
    },
    
    getOnlineCount: (state) => (chatId) => {
      const group = state.groupOnlineCount.find(item => item.chatId === chatId)
      // 确保至少显示1人在线（当前用户）
      return group ? Math.max(parseInt(group.count) || 0, 1) : 1
    }
  }
})
