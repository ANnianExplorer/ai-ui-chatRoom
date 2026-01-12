<template>
  <div class="admin-file-management">
    <div class="page-header">
      <h2>文件管理</h2>
      <p>管理系统文件，包括图片和文档的查看、搜索和删除操作</p>
    </div>
    <!-- 文件统计信息 -->
    <div class="file-stats">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-item">
            <span class="stat-label">总文件数</span>
            <span class="stat-value">{{ totalFiles }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">图片文件</span>
            <span class="stat-value">{{ imageFilesCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">文档文件</span>
            <span class="stat-value">{{ documentFilesCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总容量</span>
            <span class="stat-value">{{ totalSize }}</span>
          </div>
        </div>
      </el-card>
    </div>
    <!-- 文件管理标签页 -->
    <el-card shadow="hover" class="file-tabs-card">
      <el-tabs v-model="activeTab" class="file-tabs">
        <!-- 图片文件 -->
        <el-tab-pane label="图片文件" name="image-files">
          <div class="tab-content">
            <!-- 图片文件搜索栏 -->
            <div class="search-section">
              <el-input
                v-model="imageFileSearch"
                placeholder="搜索图片文件名/文件路径"
                clearable
                @keyup.enter="handleImageFileSearch"
                class="search-input"
              >
                <template #append>
                  <el-button @click="handleImageFileSearch" type="primary" icon="Search">搜索</el-button>
                </template>
              </el-input>
              
              <div class="action-buttons">
                <el-button type="primary" @click="handleImageFileRefresh" icon="RefreshRight">刷新</el-button>
                <el-button type="danger" @click="handleDeleteSelectedImages" :disabled="selectedImageFiles.length === 0" icon="Delete">
                  删除选中 ({{ selectedImageFiles.length }})
                </el-button>
              </div>
            </div>

            <!-- 图片文件列表 -->
            <el-table
              v-loading="imageFilesLoading"
              :data="imageFilesList"
              border
              style="width: 100%"
              @selection-change="handleImageSelectionChange"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="文件ID" width="80" />
              <el-table-column prop="originalName" label="文件名" min-width="200">
                <template #default="scope">
                  <div class="file-info">
                    <el-icon class="file-icon">
                      <Picture />
                    </el-icon>
                    <span class="file-name">{{ scope.row.originalName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="fileType" label="文件类型" width="100">
                <template #default="scope">
                  <el-tag type="success">图片</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="fileSize" label="文件大小" width="120">
                <template #default="scope">
                  <span class="file-size">{{ formatFileSize(scope.row.fileSize || 0) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createBy" label="上传者" min-width="120">
                <template #default="scope">
                  <div class="uploader-info">
                    <el-avatar :size="24" :src="usersMap[scope.row.createBy]?.avatar"/>
                    <span>{{ usersMap[scope.row.createBy]?.username || scope.row.createBy || '未知用户' }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="上传时间" min-width="160" />
              <el-table-column prop="filePath" label="文件路径" min-width="250">
                <template #default="scope">
                  <el-tooltip :content="scope.row.filePath" placement="top">
                    <span class="file-url">{{ truncateText(scope.row.filePath, 30) }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="250">
                <template #default="scope">
                  <el-button
                    type="primary"
                    style="width: 60px"
                    @click="handlePreviewFile(scope.row)"
                    icon="View"
                  >
                    预览
                  </el-button>
                  <el-button
                    type="warning"
                    style="width: 60px"
                    @click="handleDownloadFile(scope.row)"
                    icon="Download"
                  >
                    下载
                  </el-button>
                  <el-button
                    type="danger"
                    style="width: 60px"
                    @click="handleDeleteFile(scope.row, 'image')"
                    icon="Delete"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 图片文件分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="imageFilePagination.currentPage"
                v-model:page-size="imageFilePagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="imageFilePagination.total"
                @size-change="handleImageFileSizeChange"
                @current-change="handleImageFileCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <!-- 文档文件 -->
        <el-tab-pane label="文档文件" name="document-files">
          <div class="tab-content">
            <!-- 文档文件搜索栏 -->
            <div class="search-section">
              <el-input
                v-model="documentFileSearch"
                placeholder="搜索文档文件名/文件路径"
                clearable
                @keyup.enter="handleDocumentFileSearch"
                class="search-input"
              >
                <template #append>
                  <el-button @click="handleDocumentFileSearch" type="primary" icon="Search">搜索</el-button>
                </template>
              </el-input>
              
              <div class="action-buttons">
                <el-button type="primary" @click="handleDocumentFileRefresh" icon="RefreshRight">刷新</el-button>
                <el-button type="danger" @click="handleDeleteSelectedDocuments" :disabled="selectedDocumentFiles.length === 0" icon="Delete">
                  删除选中 ({{ selectedDocumentFiles.length }})
                </el-button>
              </div>
            </div>

            <!-- 文档文件列表 -->
            <el-table
              v-loading="documentFilesLoading"
              :data="documentFilesList"
              border
              style="width: 100%"
              @selection-change="handleDocumentSelectionChange"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="id" label="文件ID" width="100" />
              <el-table-column prop="originalName" label="文件名" min-width="200">
                <template #default="scope">
                  <div class="file-info">
                    <el-icon class="file-icon">
                      <Document />
                    </el-icon>
                    <span class="file-name">{{ scope.row.originalName }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="fileType" label="文件类型" width="100">
                <template #default="scope">
                  <el-tag type="info">文档</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="fileSize" label="文件大小" width="120">
                <template #default="scope">
                  <span class="file-size">{{ formatFileSize(scope.row.fileSize || 0) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="createBy" label="上传者" min-width="120">
                <template #default="scope">
                  <div class="uploader-info">
                    <el-avatar :size="24" :src="usersMap[scope.row.createBy]?.avatar"/>
                    <span>{{ usersMap[scope.row.createBy]?.username || scope.row.createBy || '未知用户' }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="上传时间" min-width="160" />
              <el-table-column prop="filePath" label="文件路径" min-width="250">
                <template #default="scope">
                  <el-tooltip :content="scope.row.filePath" placement="top">
                    <span class="file-url">{{ truncateText(scope.row.filePath, 30) }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="240">
                <template #default="scope">
                  <el-button
                    type="primary"
                    style="width: 60px"
                    @click="handlePreviewFile(scope.row)"
                    icon="View"
                  >
                    预览
                  </el-button>
                  <el-button
                    type="warning"
                    style="width: 60px"
                    @click="handleDownloadFile(scope.row)"
                    icon="Download"
                  >
                    下载
                  </el-button>
                  <el-button
                    type="danger"
                    style="width: 60px"
                    @click="handleDeleteFile(scope.row, 'document')"
                    icon="Delete"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 文档文件分页 -->
            <div class="pagination-container">
              <el-pagination
                v-model:current-page="documentFilePagination.currentPage"
                v-model:page-size="documentFilePagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="documentFilePagination.total"
                @size-change="handleDocumentFileSizeChange"
                @current-change="handleDocumentFileCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>



    <!-- 文件预览对话框 -->
    <el-dialog
      v-model="filePreviewVisible"
      :title="`文件预览 - ${selectedFile.originalName || ''}`"
      width="80%"
      :close-on-click-modal="false"
    >
      <div class="file-preview-content">
        <!-- 图片预览 -->
        <el-image
          v-if="selectedFile.fileType && selectedFile.fileType.startsWith('image/')"
          :src="selectedFile.filePath"
          fit="contain"
          class="image-preview"
        >
          <template #error>
            <div class="image-error">
              <el-icon class="error-icon"><Picture /></el-icon>
              <span>图片加载失败</span>
            </div>
          </template>
        </el-image>
        
        <!-- 文档预览 -->
        <div v-else-if="selectedFile.fileType && !selectedFile.fileType.startsWith('image/')" class="document-preview">
          <div class="document-info">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="文件名">{{ selectedFile.originalName }}</el-descriptions-item>
              <el-descriptions-item label="文件类型">{{ selectedFile.fileType && selectedFile.fileType.startsWith('image/') ? '图片' : '文档' }}</el-descriptions-item>
              <el-descriptions-item label="文件大小">{{ formatFileSize(selectedFile.fileSize || 0) }}</el-descriptions-item>
              <el-descriptions-item label="上传者">{{ usersMap[selectedFile.createBy]?.username || selectedFile.createBy || '未知用户' }}</el-descriptions-item>
              <el-descriptions-item label="上传时间">{{ selectedFile.createTime }}</el-descriptions-item>
              <el-descriptions-item label="文件路径">{{ selectedFile.filePath }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div class="document-actions">
            <el-button type="primary" @click="handleDownloadFile(selectedFile)" icon="Download">
              下载文档
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAdminApi } from '@/api/admin/index.js'

// 响应式数据
const activeTab = ref('image-files')
const filePreviewVisible = ref(false)
const selectedFile = ref({})

// 用户信息映射，用于存储通过ID获取的用户信息
const usersMap = ref({})

// 初始化API
const adminApi = useAdminApi()

// 图片文件相关
const imageFileSearch = ref('')
const imageFilesLoading = ref(false)
const imageFilesList = ref([])
const selectedImageFiles = ref([])
const imageFilePagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 文档文件相关
const documentFileSearch = ref('')
const documentFilesLoading = ref(false)
const documentFilesList = ref([])
const selectedDocumentFiles = ref([])
const documentFilePagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 计算属性
const totalFiles = computed(() => {
  return imageFilesList.value.length + documentFilesList.value.length
})

const imageFilesCount = computed(() => {
  return imageFilesList.value.length
})

const documentFilesCount = computed(() => {
  return documentFilesList.value.length
})

const totalSize = computed(() => {
  const imageTotal = imageFilesList.value.reduce((sum, file) => sum + (file.fileSize || 0), 0)
  const documentTotal = documentFilesList.value.reduce((sum, file) => sum + (file.fileSize || 0), 0)
  return formatFileSize(imageTotal + documentTotal)
})

// 初始化数据
onMounted(() => {
  fetchImageFiles()
  fetchDocumentFiles()
})

// 获取图片文件列表
const fetchImageFiles = async () => {
  imageFilesLoading.value = true
  try {
    // 构建请求参数
    const params = {
      page: imageFilePagination.currentPage,
      pageSize: imageFilePagination.pageSize,
      keyword: imageFileSearch.value,
      type: 'image'
    }
    
    // 调用真实API
    const response = await adminApi.files.getFiles(params)
    
    // 更新数据
    imageFilesList.value = response.data.list || []
    imageFilePagination.total = response.data.total || 0
    console.log(imageFilesList)
    // 获取所有创建者ID
    const userIds = imageFilesList.value.map(file => file.createBy).filter(id => id !== undefined && id !== null && !usersMap.value[id])
    // 批量获取用户信息
    if (userIds.length > 0) {
      await fetchUsersInfo(userIds)
    }
  } catch (error) {
    ElMessage.error('获取图片文件列表失败: ' + (error.message || '未知错误'))
    imageFilesList.value = []
    imageFilePagination.total = 0
  } finally {
    imageFilesLoading.value = false
  }
}

// 获取文档文件列表
const fetchDocumentFiles = async () => {
  documentFilesLoading.value = true
  try {
    // 构建请求参数
    const params = {
      page: documentFilePagination.currentPage,
      pageSize: documentFilePagination.pageSize,
      keyword: documentFileSearch.value,
      type: 'document'
    }
    
    // 调用真实API
    const response = await adminApi.files.getFiles(params)
    
    // 更新数据
    documentFilesList.value = response.data.list || []
    documentFilePagination.total = response.data.total || 0
    
    // 获取所有创建者ID
    const userIds = documentFilesList.value.map(file => file.createBy).filter(id => id !== undefined && id !== null && !usersMap.value[id])
    // 批量获取用户信息
    if (userIds.length > 0) {
      await fetchUsersInfo(userIds)
    }
  } catch (error) {
    ElMessage.error('获取文档文件列表失败: ' + (error.message || '未知错误'))
    documentFilesList.value = []
    documentFilePagination.total = 0
  } finally {
    documentFilesLoading.value = false
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

// 通过ID获取用户信息并存储到usersMap中
const fetchUserInfo = async (userId) => {
  if (userId === undefined || userId === null || usersMap.value[userId]) return
  
  try {
    const response = await adminApi.users.getUserById(userId)
    if (response.code === 200 && response.data) {
      usersMap.value[userId] = response.data
    } else {
      // 如果用户不存在，仍然设置一个默认值以避免重复请求
      usersMap.value[userId] = { id: userId, username: '未知用户', avatar: '' }
    }
  } catch (error) {
    console.error(`获取用户信息失败: ${userId}`, error)
    // 如果请求失败，仍然设置一个默认值以避免重复请求
    usersMap.value[userId] = { id: userId, username: '未知用户', avatar: '' }
  }
}

// 批量获取用户信息
const fetchUsersInfo = async (userIds) => {
  const uniqueIds = [...new Set(userIds)]
  const promises = uniqueIds.map(id => fetchUserInfo(id))
  await Promise.all(promises)
}

// 截断文本
const truncateText = (text, maxLength) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 图片文件搜索
const handleImageFileSearch = () => {
  imageFilePagination.currentPage = 1
  fetchImageFiles()
}

// 图片文件刷新
const handleImageFileRefresh = () => {
  // 重置搜索条件
  imageFileSearch.value = ''
  imageFilePagination.currentPage = 1
  fetchImageFiles()
}

// 文档文件搜索
const handleDocumentFileSearch = () => {
  documentFilePagination.currentPage = 1
  fetchDocumentFiles()
}

// 文档文件刷新
const handleDocumentFileRefresh = () => {
  // 重置搜索条件
  documentFileSearch.value = ''
  documentFilePagination.currentPage = 1
  fetchDocumentFiles()
}

// 图片文件选择
const handleImageSelectionChange = (selection) => {
  selectedImageFiles.value = selection
}

// 文档文件选择
const handleDocumentSelectionChange = (selection) => {
  selectedDocumentFiles.value = selection
}

// 图片文件分页大小变化
const handleImageFileSizeChange = (size) => {
  imageFilePagination.pageSize = size
  imageFilePagination.currentPage = 1
  fetchImageFiles()
}

// 图片文件页码变化
const handleImageFileCurrentChange = (current) => {
  imageFilePagination.currentPage = current
  fetchImageFiles()
}

// 文档文件分页大小变化
const handleDocumentFileSizeChange = (size) => {
  documentFilePagination.pageSize = size
  documentFilePagination.currentPage = 1
  fetchDocumentFiles()
}

// 文档文件页码变化
const handleDocumentFileCurrentChange = (current) => {
  documentFilePagination.currentPage = current
  fetchDocumentFiles()
}

// 预览文件
const handlePreviewFile = (file) => {
  selectedFile.value = { ...file }
  filePreviewVisible.value = true
}

// 下载文件
const handleDownloadFile = (file) => {
  ElMessage.info(`开始下载文件: ${file.originalName}`)
  // 实际项目中这里应该触发文件下载
  window.open(file.filePath)
}

// 删除单个文件
const handleDeleteFile = async (file, fileType) => {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${file.originalName}" 吗?`, '删除文件', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 调用真实API
    await adminApi.files.deleteFile(file.id)
    
    // 刷新对应列表
    if (fileType === 'image') {
      fetchImageFiles()
    } else {
      fetchDocumentFiles()
    }
    
    ElMessage.success('文件删除成功')
  } catch (error) {
    // 取消操作或API调用失败
    if (error !== 'cancel') {
      ElMessage.error('文件删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 删除选中图片
const handleDeleteSelectedImages = async () => {
  if (selectedImageFiles.value.length === 0) {
    ElMessage.warning('请先选择要删除的图片文件')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedImageFiles.value.length} 个图片文件吗?`, '批量删除图片', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 获取选中的文件ID列表
    const selectedIds = selectedImageFiles.value.map(file => file.id)
    
    // 调用真实API进行批量删除
    await adminApi.files.batchDeleteFiles(selectedIds)
    
    // 刷新列表并清空选中状态
    fetchImageFiles()
    selectedImageFiles.value = []
    ElMessage.success('批量删除图片成功')
  } catch (error) {
    // 取消操作或API调用失败
    if (error !== 'cancel') {
      ElMessage.error('批量删除图片失败: ' + (error.message || '未知错误'))
    }
  }
}

// 删除选中文档
const handleDeleteSelectedDocuments = async () => {
  if (selectedDocumentFiles.value.length === 0) {
    ElMessage.warning('请先选择要删除的文档文件')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedDocumentFiles.value.length} 个文档文件吗?`, '批量删除文档', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 获取选中的文件ID列表
    const selectedIds = selectedDocumentFiles.value.map(file => file.id)
    
    // 调用真实API进行批量删除
    await adminApi.files.batchDeleteFiles(selectedIds)
    
    // 刷新列表并清空选中状态
    fetchDocumentFiles()
    selectedDocumentFiles.value = []
    ElMessage.success('批量删除文档成功')
  } catch (error) {
    // 取消操作或API调用失败
    if (error !== 'cancel') {
      ElMessage.error('批量删除文档失败: ' + (error.message || '未知错误'))
    }
  }
}
</script>

<style scoped lang="scss">
.admin-file-management {
  .page-header {
    margin-bottom: 24px;
    h2 {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
    p {
      margin: 0;
      font-size: 14px;
      color: #909399;
    }
  }
  
  .file-tabs-card {
    margin-bottom: 20px;
  }
  
  .file-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;
    }
  }
  
  .tab-content {
    padding: 0;
  }
  
  .search-section {
    margin-bottom: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .search-input {
    width: 100%;
    max-width: 600px;
  }
  
  .action-buttons {
    display: flex;
    gap: 12px;
  }
  
  .file-stats {
    margin-bottom: 20px;
  }
  
  .stat-card {
    .stat-content {
      display: flex;
      gap: 40px;
      justify-content: center;
      padding: 10px 0;
      
      .stat-item {
        text-align: center;
        
        .stat-label {
          display: block;
          font-size: 14px;
          color: #909399;
          margin-bottom: 8px;
        }
        
        .stat-value {
          display: block;
          font-size: 24px;
          font-weight: 600;
          color: #303133;
        }
      }
    }
  }
  
  .file-info {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .file-icon {
      font-size: 20px;
      color: #409eff;
    }
    
    .file-name {
      font-weight: 500;
    }
  }
  
  .file-size {
    color: #606266;
    font-size: 13px;
  }
  
  .uploader-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .file-url {
    color: #606266;
    font-size: 13px;
  }
  
  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
  
  .file-preview-content {
    padding: 20px 0;
    
    .image-preview {
      max-width: 100%;
      max-height: 500px;
      margin: 0 auto;
      display: block;
    }
    
    .image-error {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 300px;
      color: #909399;
      
      .error-icon {
        font-size: 48px;
        margin-bottom: 16px;
      }
    }
    
    .document-preview {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    
    .document-info {
      margin-bottom: 20px;
    }
    
    .document-actions {
      display: flex;
      justify-content: center;
    }
  }
}
</style>