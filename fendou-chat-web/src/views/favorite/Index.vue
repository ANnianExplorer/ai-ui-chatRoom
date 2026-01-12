<template>
  <div class="favorite-container">
    <!-- 收藏页面标题 -->
    <div class="page-header">
      <h2>我的收藏</h2>
      <p>查看和管理您收藏的消息</p>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <el-card shadow="hover" class="search-card">
        <!-- 搜索栏 -->
        <div class="search-row">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索收藏的消息"
            clearable
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <!-- 筛选条件 -->
        <div class="filter-row">
          <!-- 类型筛选 -->
          <div class="filter-item">
            <span class="filter-label">类型：</span>
            <el-select
              v-model="typeFilter"
              placeholder="所有类型"
              clearable
              size="small"
              class="filter-select"
            >
              <el-option label="文本" :value="1" />
              <el-option label="图片" :value="2" />
              <el-option label="文件" :value="3" />
            </el-select>
          </div>
          
          <!-- 来源筛选 -->
          <div class="filter-item">
            <span class="filter-label">来源：</span>
            <el-select
              v-model="sourceFilter"
              placeholder="所有类型"
              clearable
              size="small"
              class="filter-select"
            >
              <el-option label="用户" :value="1" />
              <el-option label="群聊" :value="2" />
              <el-option label="AI聊天" :value="3" />
            </el-select>
          </div>
          
          <!-- 操作按钮 -->
          <div class="filter-actions">
            <el-button
              type="primary"
              size="small"
              @click="resetFilter"
            >
              重置
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    

    
    <!-- 收藏列表 -->
    <el-card shadow="hover" class="favorite-card">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="favoriteList.length === 0" class="empty-container">
        <el-empty description="暂无收藏消息" />
      </div>
      
      <div v-else class="favorite-list">
        <div 
          v-for="item in favoriteDetails" 
          :key="item.id" 
          class="favorite-item"
        >
          <!-- 收藏内容 -->
          <div class="favorite-content">
            <!-- 文本消息 -->
            <div v-if="item.favoriteType === 1" class="text-content">
              <p class="content">{{ item.contentDetail?.content || item.content }}</p>
            </div>
            
            <!-- 图片消息 -->
            <div v-else-if="item.favoriteType === 2" class="image-content">
              <el-image
                :src="item.fileUrl"
                :preview-src-list="[item.fileUrl]"
                class="favorite-image"
              />
            </div>
            
            <!-- 文件消息 -->
            <div v-else-if="item.favoriteType === 3" class="file-content">
              <el-icon class="file-icon">
                <Document />
              </el-icon>
              <div class="file-info">
                <p class="file-name">{{ item.fileName }}</p>
                <p class="file-meta">
                  <span class="file-type">{{ getFileType(item.fileUrl) }}</span>
                  <span class="file-size">{{ formatFileSize(item.fileSize) }}</span>
                </p>
              </div>
              <div class="file-actions">
                <el-button type="primary" size="small" @click="downloadFile(item.fileUrl)">下载</el-button>
              </div>
            </div>
          </div>
          
          <!-- 收藏信息 -->
          <div class="favorite-info">
            <div class="info-item">
              <el-icon class="info-icon"><Clock /></el-icon>
              <span class="info-text">{{ formatDate(item.createTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon class="info-icon"><User /></el-icon>
              <span class="info-text">{{ item.collectedName || '未知用户' }}</span>
            </div>
            <div class="info-item">
              <el-tag 
                size="small" 
                :type="item.favoriteFrom === 1 ? 'primary' : item.favoriteFrom === 2 ? 'success' : 'warning'"
              >
                {{ item.favoriteFrom === 1 ? '用户' : item.favoriteFrom === 2 ? '群聊' : 'AI聊天' }}
              </el-tag>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="favorite-actions">
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(item.id)" 
              icon="Delete"
            >
              取消收藏
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div v-if="favoriteList.length > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Delete, User, Clock, Document } from '@element-plus/icons-vue'
import { useFavoriteApi } from '@/api/favorite/index.js'
import { useMessageApi } from '@/api/message/index.js'
import { useUserApi } from '@/api/user/index.js'
import { useUserFileApi } from '@/api/userFile/index.js'

// API实例
const favoriteApi = useFavoriteApi()
const messageApi = useMessageApi()
const userApi = useUserApi()
const userFileApi = useUserFileApi()

// 响应式数据
const searchKeyword = ref('')
const favoriteList = ref([])
const originalDetails = ref([]) // 原始详情数据，用于筛选
const loading = ref(false)

// 筛选条件
const typeFilter = ref('') // 类型筛选：1-文本，2-图片，3-文件
const sourceFilter = ref('') // 来源筛选：1-用户，2-群聊

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 缓存，避免重复请求
const cache = ref({
  users: {},
  messages: {}
})

// 过滤后的收藏列表（实时本地筛选）
const favoriteDetails = computed(() => {
  let filtered = [...originalDetails.value]
  
  // 1. 根据搜索关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(item => {
      // 根据收藏类型搜索不同的内容
      if (item.favoriteType === 1) {
        // 文本消息：搜索内容
        const content = item.contentDetail?.content || ''
        return content.toLowerCase().includes(keyword)
      } else if (item.favoriteType === 2 || item.favoriteType === 3) {
        // 图片或文件：搜索文件名
        const fileName = item.fileName || ''
        return fileName.toLowerCase().includes(keyword)
      }
      return false
    })
  }
  
  // 2. 根据类型筛选
  if (typeFilter.value !== '') {
    filtered = filtered.filter(item => item.favoriteType === typeFilter.value)
  }
  
  // 3. 根据来源筛选
  if (sourceFilter.value !== '') {
    filtered = filtered.filter(item => item.favoriteFrom === sourceFilter.value)
  }
  
  // 4. 分页
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  
  // 更新总条数
  pagination.total = filtered.length
  
  return filtered.slice(start, end)
})

// 获取用户信息
const getUserInfo = async (userId) => {
  if (cache.value.users[userId]) {
    return cache.value.users[userId]
  }
  
  // 如果是机器人（userId === 0），返回默认机器人信息
  if (userId === 0) {
    const robotInfo = {
      id: 0,
      username: 'AI聊天',
      avatar: 'https://img2.baidu.com/it/u=2473380155,1807338550&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500',
      realName: 'AI聊天'
    }
    cache.value.users[userId] = robotInfo
    return robotInfo
  }
  
  try {
    const response = await userApi.getUserById(userId)
    if (response.code === 200) {
      cache.value.users[userId] = response.data
      return response.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
  return null
}

// 获取消息详情
const getMessageInfo = async (messageId) => {
  console.log('获取消息详情', messageId)

  if (cache.value.messages[messageId]) {
    return cache.value.messages[messageId]
  }
  try {
    const response = await messageApi.getMessageById(messageId)
    console.log('消息详情', response.data)
    if (response.code === 200) {
      cache.value.messages[messageId] = response.data
      return response.data
    }
  } catch (error) {
    console.error('获取消息详情失败:', error)
  }
  return null
}

// 获取收藏列表
const getFavoriteList = async () => {
  loading.value = true
  try {
    const response = await favoriteApi.getFavoriteList()
    if (response.code === 200) {
      favoriteList.value = response.data || []
      console.log('收藏列表', response.data)
      // 获取每个收藏项的详细信息
      const details = []
      for (const favorite of favoriteList.value) {
        // 获取被收藏用户信息
        const collectedUser = await getUserInfo(favorite.collectedId)
        
        // 根据收藏类型获取内容详情
        let contentDetail = null
        let fileInfo = null
        
        console.log('收藏项:', favorite)
        console.log('收藏类型:', favorite.favoriteType)
        console.log('收藏内容ID:', favorite.content)
        
        // 所有收藏类型的content字段存储的都是消息ID
        try {
          contentDetail = await getMessageInfo(favorite.content)
          console.log('消息详情:', contentDetail)
          
          // 如果是图片或文件消息，根据消息中的fileId获取文件详情
          if ((favorite.favoriteType === 2 || favorite.favoriteType === 3) && contentDetail?.fileId) {
            try {
              console.log('尝试获取文件ID:', contentDetail.fileId)
              const fileResponse = await userFileApi.getFileById(contentDetail.fileId)
              console.log('文件响应:', fileResponse)
              if (fileResponse.code === 200) {
                fileInfo = fileResponse.data
                console.log('文件详情:', fileInfo)
                console.log('原始文件名:', fileInfo?.originalName)
                console.log('文件大小:', fileInfo?.fileSize)
              } else {
                console.error('获取文件失败，响应码:', fileResponse.code)
              }
            } catch (error) {
              console.error('获取文件详情失败:', error)
            }
          }
        } catch (error) {
          console.error('获取消息详情失败:', error)
        }
        
        details.push({
          ...favorite,
          collectedName: collectedUser?.username || '未知用户',
          contentDetail,
          fileInfo,
          // 确保fileName和fileSize字段可用
          fileName: fileInfo?.originalName || contentDetail?.fileName || '未知文件名',
          fileSize: fileInfo?.fileSize || contentDetail?.fileSize || 0,
          fileUrl: fileInfo?.filePath || contentDetail?.content || ''
        })
      }
      
      // 将数据保存到原始详情数组，用于本地筛选
      originalDetails.value = details
      pagination.total = details.length
      pagination.currentPage = 1
    } else {
      ElMessage.error('获取收藏列表失败')
    }
  } catch (error) {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索收藏 - 本地实时搜索，无需调用API
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理，这里只需要重置页码
  pagination.currentPage = 1
}

// 重置筛选条件
const resetFilter = () => {
  searchKeyword.value = ''
  typeFilter.value = ''
  sourceFilter.value = ''
  pagination.currentPage = 1
}

// 下载文件
const downloadFile = (fileUrl) => {
  const link = document.createElement('a')
  link.href = fileUrl
  link.download = 'file'
  link.click()
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
}

// 获取文件类型
const getFileType = (fileUrl) => {
  if (!fileUrl) return '未知文件'
  const ext = fileUrl.split('.').pop().toLowerCase()
  const fileTypes = {
    'txt': '文本文件',
    'doc': 'Word文档',
    'docx': 'Word文档',
    'xls': 'Excel表格',
    'xlsx': 'Excel表格',
    'ppt': 'PPT演示文稿',
    'pptx': 'PPT演示文稿',
    'pdf': 'PDF文档',
    'jpg': '图片',
    'jpeg': '图片',
    'png': '图片',
    'gif': '图片',
    'bmp': '图片'
  }
  return fileTypes[ext] || '未知文件'
}

// 判断文件是否可预览
const isPreviewableFile = (fileUrl) => {
  if (!fileUrl) return false
  const ext = fileUrl.split('.').pop().toLowerCase()
  // 支持预览的文件类型
  const previewableTypes = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt']
  return previewableTypes.includes(ext)
}

// 预览文件
const previewFile = (fileUrl, fileName) => {
  // 直接下载文件，因为Office Online服务不可用
  downloadFile(fileUrl)
  // 也可以显示提示信息
  ElMessage.info('文件预览服务暂时不可用，请下载后查看')
}

// 删除收藏
const handleDelete = async (id) => {
  try {
    const response = await favoriteApi.deleteFavorite(id)
    if (response.code === 200) {
      ElMessage.success('取消收藏成功')
      // 从原始数据中移除删除的项
      originalDetails.value = originalDetails.value.filter(item => item.id !== id)
      pagination.total = originalDetails.value.length
      // 如果当前页没有数据且不是第一页，返回上一页
      if (favoriteDetails.value.length === 0 && pagination.currentPage > 1) {
        pagination.currentPage--
      }
    } else {
      ElMessage.error('取消收藏失败')
    }
  } catch (error) {
    ElMessage.error('取消收藏失败')
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getFavoriteList()
})
</script>

<style scoped lang="scss">
.favorite-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  
  .page-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0 0 8px 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }
    
    p {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .search-section {
    margin-bottom: 20px;
    
    .search-card {
      margin-bottom: 0;
      
      .search-row {
        margin-bottom: 15px;
        
        .search-input {
          width: 100%;
          max-width: none;
        }
      }
      
      .filter-row {
        display: flex;
        align-items: center;
        gap: 16px;
        flex-wrap: wrap;
        
        .filter-item {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .filter-label {
            font-size: 14px;
            color: #606266;
            white-space: nowrap;
          }
          
          .filter-select {
            width: 120px;
          }
        }
        
        .filter-actions {
          margin-left: auto;
        }
      }
    }
  }
  
  .favorite-card {
    margin-bottom: 20px;
    
    .loading-container {
      padding: 20px 0;
    }
    
    .empty-container {
      padding: 40px 0;
      text-align: center;
    }
    
    .favorite-list {
      .favorite-item {
        padding: 16px;
        border-bottom: 1px solid #ebeef5;
        
        &:last-child {
          border-bottom: none;
        }
        
        &:hover {
          background-color: #f5f7fa;
        }
        
        .favorite-content {
          margin-bottom: 12px;
          
          .text-content {
            .content {
              margin: 0;
              font-size: 14px;
              line-height: 1.5;
              color: #303133;
            }
          }
          
          .image-content {
            .favorite-image {
              max-width: 200px;
              max-height: 200px;
              border-radius: 4px;
            }
          }
          
          .file-content {
            display: flex;
            align-items: center;
            gap: 12px;
            
            .file-icon {
              font-size: 24px;
              color: #409eff;
            }
            
            .file-info {
              flex: 1;
              
              .file-name {
                margin: 0 0 4px 0;
                font-size: 14px;
                color: #303133;
              }
              
              .file-meta {
                margin: 0;
                font-size: 12px;
                color: #909399;
                display: flex;
                gap: 12px;
                
                .file-type {
                  padding: 2px 8px;
                  background-color: #ecf5ff;
                  color: #409eff;
                  border-radius: 4px;
                }
              }
            }
            
            .file-actions {
              display: flex;
              gap: 8px;
            }
          }
        }
        
        .favorite-info {
          display: flex;
          align-items: center;
          gap: 16px;
          margin-bottom: 12px;
          
          .info-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: #909399;
            
            .info-icon {
              font-size: 14px;
            }
          }
        }
        
        .favorite-actions {
          display: flex;
          justify-content: flex-end;
        }
      }
    }
    
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

</style>