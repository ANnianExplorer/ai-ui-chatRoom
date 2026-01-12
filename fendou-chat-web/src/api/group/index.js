import request from '@/utils/request.js'

export function useGroupApi() {
  return {
    // 根据用户Id 获取群聊
    getGroupsByUserId(userId) {
      return request({
        url: '/group/groups/' + userId,
        method: 'get'
      })
    },

    // 创建群聊
    createGroup(group) {
      return request({
        url: '/group/create',
        method: 'post',
        data: group
      })
    },

    // 获取群聊成员
    getGroupMembers(chatId) {
      return request({
        url: '/group/members/' + chatId,
        method: 'get'
      })
    },

    // 添加群聊成员
    addGroupMember(params) {
      return request({
        url: '/group/addMember',
        method: 'post',
        params: params
      })
    },

    // 获取群聊信息
    getGroupById(id) {
      return request({
        url: '/group/get/' + id,
        method: 'get'
      })
    },

    // 退出群聊
    quitGroup(id) {
      return request({
        url: '/group/quit/' + id,
        method: 'get'
      })
    },
    
    // 移除群成员
    removeGroup(data) {
      return request({
        url: '/group/remove',
        method: 'post',
        data: data
      })
    },

    // 解散群聊
    deleteGroup(id) {
      return request({
        url: '/group/delete/' + id,
        method: 'delete'
      })
    },

    // 修改群备注
    updateRemark(data) {
      return request({
        url: '/group/update-remark',
        method: 'put',
        data
      })
    },

    // 修改群聊
    updateGroup(group) {
      return request({
        url: '/group/update',
        method: 'put',
        data: group
      })
    },

    // 获取群公告
    getGroupAnnouncement(groupId) {
      return request({
        url: '/group/getCall/' + groupId,
        method: 'get'
      })
    },

    // 更新群公告
    updateGroupAnnouncement(data) {
      return request({
        url: '/group/updateCall',
        method: 'put',
        data: data
      })
    },
    
    // 获取群聊列表
    getGroupList(params) {
      return request({
        url: '/group/list',
        method: 'get',
        params: params
      })
    },
    // 是否在群聊中
    verifyIFExit(groupId) {
      return request({
        url: `/group/verifyG/${groupId}`,
        method: 'get',
      })
    },
    
    // 加入群聊
    joinGroup(params) {
      return request({
        url: '/group/join',
        method: 'post',
        params: params
      })
    }
  }
}
