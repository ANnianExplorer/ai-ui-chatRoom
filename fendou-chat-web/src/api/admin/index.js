import request from '@/utils/request.js'

export function useAdminApi() {
  return {
    // 用户管理API
    users: {
      // 获取所有用户列表
      getUsers(params) {
        return request({
          url: '/admin/users',
          method: 'get',
          params: params
        })
      },
      // 获取单个用户信息
      getUserById(id) {
        return request({
          url: `/admin/users/${id}`,
          method: 'get'
        })
      },

      // 更新用户状态
      updateUserStatus(id, status) {
        return request({
          url: `/admin/users/${id}/status`,
          method: 'put',
          data: { status }
        })
      },
      // 删除用户
      deleteUser(id) {
        return request({
          url: `/admin/users/${id}`,
          method: 'delete'
        })
      }
    },
    
    // 群聊管理API
    groups: {
      // 获取所有群聊列表
      getGroups(params) {
        return request({
          url: '/admin/groups',
          method: 'get',
          params: params
        })
      },
      // 获取单个群聊信息
      getGroupById(id) {
        return request({
          url: `/admin/groups/${id}`,
          method: 'get'
        })
      },
      // 获取群聊成员
      getGroupMembers(id) {
        return request({
          url: `/admin/groups/${id}/members`,
          method: 'get'
        })
      },
      // 更新群聊状态
      updateGroupStatus(id, status) {
        return request({
          url: `/admin/groups/${id}/status`,
          method: 'put',
          data: { status }
        })
      },
      // 删除群聊
      deleteGroup(id) {
        return request({
          url: `/admin/groups/${id}`,
          method: 'delete'
        })
      }
    },
    
    // 消息管理API
    messages: {
      // 获取群聊消息
      getGroupMessages(params) {
        return request({
          url: '/admin/messages/group',
          method: 'get',
          params: params
        })
      },
      // 获取用户对话消息
      getUserMessages(params) {
        return request({
          url: '/admin/messages/user',
          method: 'get',
          params: params
        })
      },
      // 获取机器人消息
      getRobotMessages(params) {
        return request({
          url: '/admin/messages/robot',
          method: 'get',
          params: params
        })
      },
      // 删除消息
      deleteMessage(id) {
        return request({
          url: `/admin/messages/${id}`,
          method: 'delete'
        })
      }
    },
    
    // 文件管理API
    files: {
      // 获取文件列表
      getFiles(params) {
        return request({
          url: '/admin/files',
          method: 'get',
          params: params
        })
      },
      // 获取文件详情
      getFileById(id) {
        return request({
          url: `/admin/files/${id}`,
          method: 'get'
        })
      },
      // 删除文件
      deleteFile(id) {
        return request({
          url: `/admin/files/${id}`,
          method: 'delete'
        })
      },
      // 批量删除文件
      batchDeleteFiles(ids) {
        return request({
          url: '/admin/files/batch',
          method: 'delete',
          data: { ids }
        })
      }
    },
    
    // 系统设置API
    settings: {
      // 获取系统设置
      getSettings() {
        return request({
          url: '/admin/settings',
          method: 'get'
        })
      },
      // 更新系统设置
      updateSettings(data) {
        return request({
          url: '/admin/settings',
          method: 'put',
          data: data
        })
      },
      // 更新管理员信息
      updateAdminProfile(data) {
        return request({
          url: '/admin/users/profile',
          method: 'put',
          data: data
        })
      },
      // 更新管理员密码
      updateAdminPassword(data) {
        return request({
          url: '/admin/users/password',
          method: 'put',
          data: data
        })
      },
      // 获取当前用户信息
      getAdminProfile() {
        return request({
          url: '/admin/users/info',
          method: 'get'
        })
      },
    }
  }
}