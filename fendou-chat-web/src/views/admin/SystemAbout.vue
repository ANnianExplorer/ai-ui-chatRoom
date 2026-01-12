<template>
  <div class="admin-system-about">
    <div class="page-header">
      <h2>系统介绍</h2>
      <p>查看系统信息、功能和开发团队介绍</p>
    </div>

    <!-- 系统介绍内容 -->
    <el-card shadow="hover" class="about-card">
      <h3 class="section-title">系统信息</h3>
      <el-descriptions :column="2" border class="system-info">
        <el-descriptions-item label="系统名称">FenDouChat聊天系统后台管理</el-descriptions-item>
        <el-descriptions-item label="版本号">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="开发框架">SpringBoot + Vue 3 + WebSocket</el-descriptions-item>
        <el-descriptions-item label="开发语言">JAVA+Vue</el-descriptions-item>
        <el-descriptions-item label="更新时间">2026-01-11</el-descriptions-item>
        <el-descriptions-item label="版权信息">© 2026 FenDou Chat</el-descriptions-item>
      </el-descriptions>

      <h3 class="section-title" style="margin-top: 24px;">系统功能</h3>
      <div class="system-features">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card shadow="hover" class="feature-card">
              <div class="feature-icon"><User /></div>
              <h4 class="feature-title">用户管理</h4>
              <p class="feature-desc">管理系统用户，包括搜索、查看和修改用户状态</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="feature-card">
              <div class="feature-icon"><ChatDotRound /></div>
              <h4 class="feature-title">群聊管理</h4>
              <p class="feature-desc">管理系统群聊，包括查看群聊信息和群聊成员</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="feature-card">
              <div class="feature-icon"><Message /></div>
              <h4 class="feature-title">消息管理</h4>
              <p class="feature-desc">管理系统消息，包括群聊消息和用户对话</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="feature-card">
              <div class="feature-icon"><Document /></div>
              <h4 class="feature-title">文件管理</h4>
              <p class="feature-desc">管理系统文件，包括图片和文档的查看和删除</p>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="hover" class="feature-card">
              <div class="feature-icon"><Setting /></div>
              <h4 class="feature-title">系统设置</h4>
              <p class="feature-desc">查看系统信息，包括个人中心设置</p>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <h3 class="section-title" style="margin-top: 24px;">开发团队</h3>
      <div class="team-info">
        <el-card shadow="hover" class="team-card">
          <div class="team-content">
            <div class="team-logo">
              <el-avatar :size="80" src="https://img2.baidu.com/it/u=2473380155,1807338550&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500" />
            </div>
            <div class="team-details">
              <h4 class="team-name">FenDou CHAT 开发团队</h4>
              <p class="team-desc">致力于打造高效、稳定、易用的聊天系统</p>
              <div class="team-contacts">
                <span class="contact-item"><el-icon><Message /></el-icon> 技术支持：fendou520@example.com</span>
                <span class="contact-item"><el-icon><Phone /></el-icon> 联系电话：1314520</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import {
  User,
  ChatDotRound,
  Message,
  Document,
  Setting,
  Monitor,
  Message as MessageIcon,
  Phone
} from '@element-plus/icons-vue'

// 系统信息
const apiUrl = ref(import.meta.env.VITE_APP_API_URL || '未配置')
const env = ref(import.meta.env.MODE || 'development')
</script>

<style scoped lang="scss">
.admin-system-about {
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
  
  .about-card {
    margin-bottom: 20px;
  }
  
  .section-title {
    margin: 0 0 16px 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
  
  .system-info {
    margin-bottom: 20px;
  }
  
  .feature-card {
    height: 180px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    padding: 15px;
    
    .feature-icon {
      font-size: 30px;
      width: 40px;
      color: var(--el-color-primary);
      margin-bottom: 12px;
    }
    
    .feature-title {
      margin: 0 0 6px 0;
      font-size: 14px;
      font-weight: 600;
      color: #303133;
    }
    
    .feature-desc {
      margin: 0;
      font-size: 12px;
      color: #606266;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
  
  .team-content {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  
  .team-logo {
    margin-right: 20px;
  }
  
  .team-name {
    margin: 0 0 8px 0;
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }
  
  .team-desc {
    margin: 0 0 12px 0;
    font-size: 14px;
    color: #606266;
  }
  
  .team-contacts {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .contact-item {
      font-size: 14px;
      color: #606266;
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}
</style>