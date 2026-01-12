import request from '@/utils/request.js'

export function useFriendApi() {
  return {
    // 添加好友
    addFriend(data) {
      return request({
        url: '/friend/add',
        method: 'post',
        data: data
      })
    },

    // 获取好友请求
    getFriendRequests(params, data) {
      return request({
        url: '/friend/requests',
        method: 'post',
        params: params,
        data: data
      })
    },

    // 响应好友请求
    respondFriendRequest(params) {
      return request({
        url: '/friend/respond',
        method: 'put',
        params: params
      })
    },

    // 删除好友
    deleteFriend(friendId) {
      return request({
        url: `/friend/delete/${friendId}`,
        method: 'delete'
      })
    },

    // 修改好友
    updateFriend(data) {
      return request({
        url: '/friend/update',
        method: 'put',
        data: data
      })
    },

    // 获取好友请求数
    getFriendCount() {
      return request({
        url: '/friend/req-count',
        method: 'get'
      })
    },
    // 是否是好友
    verifyIFExit(friendId) {
      return request({
        url: `/friend/verify/${friendId}`,
        method: 'get',
      })
    },
  }
}
