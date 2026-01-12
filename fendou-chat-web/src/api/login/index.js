import request from '@/utils/request.js'


export function useLoginApi() {
    return {
        register(data) {
            return request({
                url: '/auth/register',
                method: 'post',
                data: data
            })
        },
        getInfo() {
            return request({
                url: '/auth/info',
                method: 'get'
            })
        },
        logout() {
            return request({
                url: '/auth/logout',
                method: 'get'
            })
        },
        getCaptcha(randomStr) {
            return request({
                url: '/auth/captcha',
                method: 'get',
                params: {
                    randomStr
                }
            })
        },
        login(data) {
            return request({
                url: '/auth/login',
                method: 'post',
                data: data
            })
        },
    }
}