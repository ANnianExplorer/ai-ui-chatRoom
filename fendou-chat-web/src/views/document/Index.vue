<template>
  <div class="document-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的文档</h2>
      <p>查看和管理您的文件文档</p>
    </div>
    
    <!-- 搜索和筛选 -->
    <div class="search-filter-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索文档"
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #append>
          <el-button @click="handleSearch" type="primary" icon="Search">搜索</el-button>
        </template>
      </el-input>
      
      <div class="filter-section">
        <el-select
          v-model="fileType"
          placeholder="文件类型"
          clearable
          size="small"
          class="filter-select"
          @change="handleFilter"
        >
          <el-option label="全部" value="" />
          <el-option label="图片" value="image" />
          <el-option label="文档" value="document" />
        </el-select>
        
        <el-select
          v-model="sortBy"
          placeholder="排序方式"
          size="small"
          class="filter-select"
          @change="handleSort"
        >
          <el-option label="上传时间" value="createTime" />
          <el-option label="文件大小" value="fileSize" />
        </el-select>
        
        <el-select
          v-model="sortOrder"
          placeholder="排序方向"
          size="small"
          class="filter-select"
          @change="handleSort"
        >
          <el-option label="最新优先" value="desc" />
          <el-option label="最旧优先" value="asc" />
        </el-select>
      </div>
    </div>
    
    <!-- 文档列表 -->
    <el-card shadow="hover" class="document-card">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="documentList.length === 0" class="empty-container">
        <el-empty description="暂无文档" />
      </div>
      
      <div v-else class="document-list">
        <div 
          v-for="file in documentList" 
          :key="file.id" 
          class="document-item"
        >
          <!-- 文件图标 -->
          <div class="file-icon-section">
            <el-icon class="file-icon" :size="40">
              <Document />
            </el-icon>
          </div>
          
          <!-- 文件信息 -->
          <div class="file-info-section">
            <div class="file-name">{{ file.originalName }}</div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
              <span class="file-date">{{ formatDate(file.createTime) }}</span>
            </div>
          </div>
          
          <!-- 文件操作 -->
          <div class="file-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click="downloadFile(file)"
              icon="Download"
            >
              下载
            </el-button>
            <el-button 
              type="default" 
              size="small" 
              @click="previewFile(file)"
              icon="View"
            >
              预览
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div v-if="documentList.length > 0" class="pagination-container">
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
    
    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="previewDialogVisible"
      :title="`文件预览 - ${previewFileData.fileName || ''}`"
      width="80%"
      :close-on-click-modal="false"
      append-to-body
    >
      <div class="file-preview-content">
        <!-- 图片预览 -->
        <div v-if="previewFileData.fileType === 'image'" class="image-preview-container">
          <el-image
            :src="previewFileData.filePath"
            fit="contain"
            class="image-preview"
            :preview-src-list="[previewFileData.filePath]"
          >
            <template #error>
              <div class="image-error">
                <el-icon class="error-icon"><Picture /></el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
        
        <!-- 文本预览 -->
        <pre v-else-if="previewFileData.fileType === 'text'" class="preview-text">{{ previewFileData.content }}</pre>
        
        <!-- 其他文件类型 -->
        <div v-else class="document-preview">
          <div class="document-info">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="文件名">{{ previewFileData.fileName }}</el-descriptions-item>
              <el-descriptions-item label="文件类型">{{ previewFileData.fileType }}</el-descriptions-item>
              <el-descriptions-item label="文件大小">{{ formatFileSize(previewFileData.fileSize || 0) }}</el-descriptions-item>
              <el-descriptions-item label="上传时间">{{ formatDate(previewFileData.createTime) }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div class="document-actions">
            <el-button type="primary" @click="downloadFile(previewFileData)" icon="Download">
              下载文件
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Download, View, Document, Picture } from '@element-plus/icons-vue'
import { useUserFileApi } from '@/api/userFile/index.js'
import { useUserStore } from '@/stores/user.js'

// API实例
const userFileApi = useUserFileApi()
const userStore = useUserStore()

// 响应式数据
const searchKeyword = ref('')
const documentList = ref([])
const loading = ref(false)
const fileType = ref('')
const sortBy = ref('createTime')
const sortOrder = ref('desc')

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 预览对话框数据
const previewDialogVisible = ref(false)
const previewFileData = reactive({
  filePath: '',
  fileType: '',
  content: '',
  fileName: ''
})

// 获取文档列表
const getDocumentList = async () => {
  loading.value = true
  try {
    // 获取当前登录用户ID
    const userId = userStore.user?.id || 1
    
    // 调用API根据用户ID获取文件列表
    const response = await userFileApi.getUserByCreateById(userId)
    
    if (response.code === 200) {
      let fileList = response.data || []
      
      // 实现前端搜索
      if (searchKeyword.value) {
        fileList = fileList.filter(file => 
          file.originalName?.toLowerCase().includes(searchKeyword.value.toLowerCase())
        )
      }
      
      // 实现前端筛选
      if (fileType.value) {
        fileList = fileList.filter(file => {
          const extension = file.originalName?.split('.').pop()?.toLowerCase() || ''
          switch (fileType.value) {
            case 'image':
              return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(extension)
            case 'document':
              return ['doc', 'docx', 'pdf', 'txt', 'md', 'xls', 'xlsx', 'ppt', 'pptx', 'csv'].includes(extension)
            case 'video':
              return ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm'].includes(extension)
            case 'audio':
              return ['mp3', 'wav', 'ogg', 'flac', 'm4a'].includes(extension)
            default:
              return true
          }
        })
      }
      
      // 实现前端排序
      fileList.sort((a, b) => {
        let valueA, valueB
        
        switch (sortBy.value) {
          case 'createTime':
            valueA = new Date(a.createTime || 0)
            valueB = new Date(b.createTime || 0)
            break
          case 'fileSize':
            valueA = a.fileSize || 0
            valueB = b.fileSize || 0
            break
          case 'fileName':
            valueA = a.originalName?.toLowerCase() || ''
            valueB = b.originalName?.toLowerCase() || ''
            return sortOrder.value === 'desc' ? valueB.localeCompare(valueA) : valueA.localeCompare(valueB)
          default:
            return 0
        }
        
        if (sortOrder.value === 'desc') {
          return valueB - valueA
        } else {
          return valueA - valueB
        }
      })
      
      // 实现前端分页
      const startIndex = (pagination.currentPage - 1) * pagination.pageSize
      const endIndex = startIndex + pagination.pageSize
      documentList.value = fileList.slice(startIndex, endIndex)
      pagination.total = fileList.length
    } else {
      ElMessage.error('获取文档列表失败')
    }
  } catch (error) {
    ElMessage.error('获取文档列表失败')
    console.error('获取文档列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索文档
const handleSearch = () => {
  // 实现搜索逻辑
  console.log('搜索:', searchKeyword.value)
  getDocumentList()
}

// 筛选文档
const handleFilter = () => {
  // 实现筛选逻辑
  console.log('筛选:', fileType.value)
  getDocumentList()
}

// 排序文档
const handleSort = () => {
  // 实现排序逻辑
  console.log('排序:', sortBy.value, sortOrder.value)
  getDocumentList()
}

// 下载文件
const downloadFile = (file) => {
  try {
    const link = document.createElement('a')
    link.href = file.filePath
    link.download = file.originalName
    link.click()
    ElMessage.success('下载开始')
  } catch (error) {
    ElMessage.error('下载失败')
    console.error('下载失败:', error)
  }
}

// 图片加载错误处理
const handleImageError = (event) => {
  console.error('图片加载失败:', event.target.src)
  ElMessage.error('图片加载失败，请检查图片路径是否正确')
}

// 预览文件
const previewFile = async (file) => {
  // 实现预览逻辑
  console.log('预览文件:', file)
  
  // 获取文件扩展名
  const extension = file.originalName?.split('.').pop()?.toLowerCase() || ''
  const imageExtensions = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
  const textExtensions = ['txt', 'md', 'html', 'htm', 'css', 'js', 'json', 'xml', 'csv', 'log']
  
  // 判断文件类型
  let fileType = 'other'
  if (imageExtensions.includes(extension)) {
    fileType = 'image'
  } else if (textExtensions.includes(extension)) {
    fileType = 'text'
  }
  
  if (fileType === 'other') {
    // 对于不支持预览的文件类型，提示用户下载
    ElMessageBox.confirm('当前文件类型不支持预览，是否下载？', '提示', {
      confirmButtonText: '下载',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      downloadFile(file)
    }).catch(() => {
      // 用户取消下载
    })
    return
  }
  
  try {
    // 设置预览数据
    // 直接使用文件路径，因为从控制台日志看它已经是完整URL
    const filePath = file.filePath
    console.log("图片路径:", filePath)
    previewFileData.filePath = filePath
    previewFileData.fileType = fileType
    previewFileData.fileName = file.originalName
    
    // 如果是文本文件，需要获取文件内容
    if (fileType === 'text') {
      const response = await userFileApi.getFileById(file.id)
      if (response.code === 200) {
        const fileDetail = response.data
        previewFileData.content = fileDetail.content || ''
      } else {
        ElMessage.error('获取文件内容失败')
        return
      }
    }
    
    // 打开预览对话框
    previewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('预览文件失败')
    console.error('预览文件失败:', error)
  }
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
  getDocumentList()
}

// 当前页变化
const handleCurrentChange = (current) => {
  pagination.currentPage = current
  getDocumentList()
}

// 组件挂载时获取数据
onMounted(() => {
  getDocumentList()
})
</script>

<style scoped lang="scss">
.document-container {
  padding: 16px;
  height: 100%;
  box-sizing: border-box;
  overflow-y: auto;
  font-family: var(--font-sans);
  scrollbar-width: thin;
  scrollbar-color: rgba(124,58,237,0.2) transparent;

  .page-header {
    margin-bottom: 18px;
    animation: fadeInUp 0.3s ease;

    h2 {
      margin: 0 0 4px 0;
      font-size: 22px;
      font-weight: 700;
      color: var(--text-primary);
      background: var(--brand-gradient-3);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    p { margin: 0; font-size: 13px; color: var(--text-muted); }
  }

  .search-filter-section {
    margin-bottom: 16px;
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
    padding: 14px 16px;
    background: var(--glass-bg);
    backdrop-filter: blur(16px);
    border: 1px solid var(--glass-border);
    border-radius: var(--radius-lg);
    box-shadow: var(--card-shadow);

    .search-input { max-width: 380px; flex: 1; min-width: 180px; }

    .filter-section {
      display: flex;
      gap: 10px;
      align-items: center;
    }

    .filter-select { min-width: 110px; }
  }

  .document-card {
    background: var(--glass-bg) !important;
    backdrop-filter: blur(16px) !important;
    border: 1px solid var(--glass-border) !important;
    border-radius: var(--radius-lg) !important;
    box-shadow: var(--card-shadow) !important;

    :deep(.el-card__body) { padding: 0; }

    .loading-container { padding: 24px; }
    .empty-container { padding: 40px 0; text-align: center; }

    .document-list {
      .document-item {
        padding: 14px 16px;
        border-bottom: 1px solid var(--border-subtle);
        display: flex;
        align-items: center;
        gap: 14px;
        transition: all 0.22s ease;
        cursor: pointer;
        animation: fadeInUp 0.25s ease both;

        &:last-child { border-bottom: none; }

        &:hover {
          background: var(--color-primary-subtle);
          transform: translateX(2px);
        }

        .file-icon-section {
          flex-shrink: 0;
          display: flex;
          align-items: center;
          justify-content: center;
          width: 52px; height: 52px;
          background: var(--color-primary-subtle);
          border-radius: 14px;
          border: 1px solid var(--border-accent);
          transition: all 0.2s;

          .file-icon { color: var(--color-primary); }
        }

        &:hover .file-icon-section {
          background: var(--color-primary-subtle-2);
          transform: scale(1.04);
        }

        .file-info-section {
          flex: 1;
          min-width: 0;

          .file-name {
            font-size: 14px;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 6px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .file-meta {
            display: flex;
            gap: 14px;
            font-size: 12px;
            color: var(--text-muted);

            .file-size,
            .file-date {
              display: flex;
              align-items: center;
              gap: 3px;
            }
          }
        }

        .file-actions {
          flex-shrink: 0;
          display: flex;
          gap: 6px;
          opacity: 0;
          transition: opacity 0.2s;
        }

        &:hover .file-actions { opacity: 1; }
      }
    }

    .pagination-container {
      padding: 16px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

/* 预览弹窗 */
.file-preview-content {
  .image-preview-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
    background: var(--bg-muted);
    border-radius: 12px;
    padding: 20px;
  }

  .image-preview {
    max-width: 100%; max-height: 70vh;
    object-fit: contain;
    border-radius: 8px;
    box-shadow: 0 4px 24px rgba(0,0,0,0.15);
    transition: transform 0.3s;
    &:hover { transform: scale(1.02); }
  }

  .image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100%; height: 300px;
    color: var(--text-muted);

    .error-icon { font-size: 48px; margin-bottom: 16px; }
  }

  .preview-text {
    width: 100%; max-height: 600px;
    overflow: auto;
    padding: 20px;
    background: var(--bg-base);
    border-radius: 10px;
    border: 1px solid var(--border-default);
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    white-space: pre-wrap;
    color: var(--text-primary);
  }

  .document-preview { display: flex; flex-direction: column; gap: 16px; }
  .document-info { margin-bottom: 16px; }
  .document-actions { display: flex; justify-content: center; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to   { opacity: 1; transform: translateY(0); }
}
</style>