import request from '@/utils/request.js'


export function useUserFileApi() {
    return{
        // 获取用户文件列表
        getUserFiles(params) {
            return request({
                url: '/file/user/files',
                method: 'get',
                params: params
            })
        },
        
        // 根据ID获取文件详情
        getFileById(id) {
            return request({
                url: `/file/details/${id}`,
                method: 'get'
            })
        },
        
        // 根据创建者ID获取文件
        getUserByCreateById(createById) {
            return request({
                url: `/file/detail/${createById}`,
                method: 'get'
            })
        },
        
        // 上传文件
        uploadFile(data) {
            return request({
                url: '/file/upload',
                method: 'post',
                data: data,
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            })
        },
        
        // 删除文件
        deleteFile(id) {
            return request({
                url: `/file/${id}`,
                method: 'delete'
            })
        },
        
        // 分享文件
        shareFile(data) {
            return request({
                url: '/file/share',
                method: 'post',
                data: data
            })
        },
        
        // 获取分享文件
        getSharedFile(shareCode) {
            return request({
                url: `/file/share/${shareCode}`,
                method: 'get'
            })
        }
    }
}