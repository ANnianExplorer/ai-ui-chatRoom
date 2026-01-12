import request from '@/utils/request.js'

export function useFavoriteApi(){
    return {
        // 添加收藏
        addFavorite(data){
            return request({
                url: '/favorite/add',
                method: 'post',
                data: data
            })
        },
        
        // 删除收藏
        deleteFavorite(id){
            return request({
                url: `/favorite/delete/${id}`,
                method: 'delete'
            })
        },
        
        // 获取收藏列表
        getFavoriteList(){
            return request({
                url: '/favorite/list',
                method: 'get'
            })
        },
        
        // 搜索收藏
        searchFavorites(keyword){
            return request({
                url: `/favorite/search?keyword=${keyword}`,
                method: 'get'
            })
        },
        
        // 检查是否已收藏
        checkFavorite(collectedId, favoriteFrom){
            return request({
                url: `/favorite/check?collectedId=${collectedId}&favoriteFrom=${favoriteFrom}`,
                method: 'get'
            })
        }
    }
}