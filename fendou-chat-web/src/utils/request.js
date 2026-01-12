import axios from 'axios'
import {ElMessage, ElMessageBox} from 'element-plus'


// 创建 axios 实例
const service = axios.create({
    baseURL: import.meta.env.VITE_APP_API_URL,
    timeout: 5000
})

// 请求拦截
service.interceptors.request.use(
    (config) => {
        // 获取token，先尝试管理员token，再尝试普通用户token
        let token = localStorage.getItem("adminToken") || sessionStorage.getItem("token");
        
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }

        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截
service.interceptors.response.use(
    (response) => {
        const res = response.data

        // 如果请求状态不是 200，则判断为错误
        if (res.code !== 200) {
            // 如果 401 身份认证失败，跳转登录
            if (res.code === 401) {
                // 检查当前页面是否为管理员页面
                const isAdminPage = window.location.pathname.startsWith('/admin');
                
                // 清除所有token
                localStorage.removeItem('adminToken');
                localStorage.removeItem('isAdmin');
                sessionStorage.clear();
                
                // 显示禁用提示
                ElMessage.error(res.message || '你已经被禁用，身份认证失败，请重新登录');
                
                // 延迟1.5秒后跳转到登录页，让用户有时间看到错误信息
                setTimeout(() => {
                    // 跳转到对应的登录页
                    window.location.href = isAdminPage ? '/admin/login' : '/login';
                }, 1500);
            }else {
                // 显示错误信息给用户
                ElMessage.error(res.message || '操作失败，请重试');
                console.error('请求失败:', res.message || 'Error');
            }
            return Promise.reject(new Error(res.message || 'Error'))
        } else {
            return res
        }
    },
    (error) => {
        // 只在控制台打印错误，不显示给用户，避免页面混乱
        console.error('请求错误:', error.message);
        return Promise.reject(error)
    }
)

export default service
