import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import Layout from '@/layout/Layout.vue'
import { useUserStore } from '@/stores/user'
import { useWebsocketStore } from '@/stores/websocket'

const routes = [
  {
    path: '/',
    redirect: '/index',
    component: Layout,
    children: [
      { path: '/index', component: () => import('@/views/chat/index.vue'), name: 'Index' },
      { path: '/qq-contact', component: () => import('@/views/contact/QQContact.vue'), name: 'QQContact' },
      { path: '/qq-group', component: () => import('@/views/contact/QQGroup.vue'), name: 'QQGroup' },
      { path: '/profile', component: () => import('@/views/user/Profile.vue'), name: 'Profile' },
      { path: '/contact/detail/:chatId', component: () => import('@/views/contact/Detail.vue'), name: 'Detail' },
      { path: '/settings', component: () => import('@/views/settings/Settings.vue'), name: 'Settings' },
      { path: '/favorite', component: () => import('@/views/favorite/Index.vue'), name: 'Favorite' },
      { path: '/document', component: () => import('@/views/document/Index.vue'), name: 'Document' }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  // 管理员登录路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue')
  },
  // 后台管理路由
  {
    path: '/admin',
    name: 'Admin',
    redirect: '/admin/dashboard',
    component: () => import('@/views/admin/AdminLayout.vue'),
    children: [
      { path: 'dashboard', component: () => import('@/views/admin/AdminDashboard.vue'), name: 'AdminDashboard' },
      { path: 'users', component: () => import('@/views/admin/UserManagement.vue'), name: 'UserManagement' },
      { path: 'groups', component: () => import('@/views/admin/GroupManagement.vue'), name: 'GroupManagement' },
      { path: 'messages', component: () => import('@/views/admin/MessageManagement.vue'), name: 'MessageManagement' },
      { path: 'files', component: () => import('@/views/admin/FileManagement.vue'), name: 'FileManagement' },
      { path: 'announcement', component: () => import('@/views/admin/AdminAnnouncement.vue'), name: 'AdminAnnouncement' },
      { path: 'violation', component: () => import('@/views/admin/AdminViolation.vue'), name: 'AdminViolation' },
      { path: 'settings', component: () => import('@/views/admin/SystemSettings.vue'), name: 'SystemSettings' },
      { path: 'about', component: () => import('@/views/admin/SystemAbout.vue'), name: 'SystemAbout' }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  },
  {
    path: '/:catchAll(.*)',
    redirect: '/404'
  }
]

// 创建路由
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 进度条配置
  NProgress.configure({ showSpinner: false })
  // 开启进度条
  NProgress.start()

  // 检查是否是管理员路由
  const isAdminRoute = to.path.startsWith('/admin') && to.path !== '/admin/login'
  // 检查是否是管理员登录路由
  const isAdminLoginRoute = to.path === '/admin/login'
  
  // 获取普通用户token和管理员token
  const userToken = sessionStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')
  const isAdmin = localStorage.getItem('isAdmin') === 'true'

  // 管理员路由处理
  if (isAdminRoute) {
    // 如果是管理员路由，需要管理员token
    if (adminToken && isAdmin) {
      next()
    } else {
      // 没有管理员权限，重定向到管理员登录页面
      next(`/admin/login?redirect=${to.path}`)
    }
  } 
  // 管理员登录路由处理
  else if (isAdminLoginRoute) {
    // 如果已经是管理员，重定向到后台首页
    if (adminToken && isAdmin) {
      next('/admin')
    } else {
      // 不是管理员，继续访问登录页面
      next()
    }
  }
  // 普通用户路由处理
  else {
    // 如果是普通登录或者注册页面且没有token，则直接进入
    if ((to.path === '/login' || to.path === '/register') && !userToken) {
      next()
    } else {
      // 如果没有 token，则重定向到登录页面
      if (!userToken) {
        next(`/login?redirect=${to.path}&params=${JSON.stringify(to.query ? to.query : to.params)}`)
        sessionStorage.clear()
      }
      // 如果有 token 并且是登录或者 注册页面，则重定向到首页
      else if (userToken && (to.path === '/login' || to.path === '/register')) {
        next('/')
      }
      // 如果有 token 并且不是登录或者注册页面，则正常跳转
      else {
        // 获取用户信息
        const userStore = useUserStore()
        if (!userStore.user || !userStore.user.id) {
          await userStore.getInfo()
        }
        // console.log('user', userStore.user)

        // 建立 WebSocket 连接
        const socket = useWebsocketStore()
        await socket.connect(userStore.user.id)
        // console.log('socket', socket)

        next()
      }
    }
  }
})

router.afterEach(() => {
  // 关闭进度条
  NProgress.done()
})

export default router
