import request from '@/utils/request.js'



export function useUserApi() {
    return{
        // 根据用户Id 获取好友
        getFriends(params, data){
          return request({
              url: '/user/friends',
              method: 'post',
              params: params,
              data: data
          })
        },
        // 修改个人信息
        updateProfile(data){
            return request({
                url: '/user/update-profile',
                method: 'post',
                data: data
            })
        },

        // 修改密码
        updatePassword(data){
            return request({
                url: '/user/update-password',
                method: 'post',
                data: data
            })
        },

        // 获取用户信息
        getUserById(id) {
            return request({
                url: '/user/get/'+id,
                method: 'get'
            })
        },
        
        // 获取当前用户信息
        getCurrentUser() {
            return request({
                url: '/user/info',
                method: 'get'
            })
        }
    }
}