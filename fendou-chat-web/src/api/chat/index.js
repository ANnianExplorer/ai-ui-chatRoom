import request from '@/utils/request.js'

export function useChatApi(){
    return {
        // 获取聊天列表
        getChats(params, searchData){
            return request({
                url: '/chat/list',
                method: 'post',
                params: params,
                data: searchData
            })
        },

        // 清空聊天记录
        cleanChat(chatId){
            return request({
                url: '/chat/clean/'+chatId,
                method: 'delete'
            })
        }
    }
}