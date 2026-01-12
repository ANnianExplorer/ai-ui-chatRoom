import request from '@/utils/request'

export function useMessageApi() {
  return {
    // 根据聊天Id获取消息
    getMessageByChatId(chatId, params) {
      return request({
        url: `/message/${chatId}`,
        method: 'get',
        params: params
      })
    },

    // 清理未读消息
    clearUnreadMessage(chatId) {
      return request({
        url: `/message/markAsRead/${chatId}`,
        method: 'post'
      })
    },

    // 根据id查询消息
    getMessageById(id) {
      return request({
        url: `/message/detail/${id}`,
        method: 'get'
      })
    }
  }
}
