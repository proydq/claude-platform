<template>
  <div class="chat-container">
    <!-- 左侧对话历史 -->
    <div class="chat-sidebar">
      <el-button 
        type="primary" 
        class="new-chat-btn"
        @click="startNewChat"
      >
        <el-icon><Plus /></el-icon>
        新建对话
      </el-button>
      
      <div class="chat-history">
        <div class="section-title">历史对话</div>
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          class="history-item"
          :class="{ active: currentChatId === chat.id }"
          @click="selectChat(chat.id)"
        >
          <div class="item-title">{{ chat.title }}</div>
          <div class="item-date">{{ formatDate(chat.createdTime) }}</div>
        </div>
        
        <div v-if="chatHistory.length === 0" class="empty-history">
          <el-empty description="暂无对话记录" :image-size="80" />
        </div>
      </div>
    </div>
    
    <!-- 右侧对话区域 -->
    <div class="chat-main">
      <!-- 项目目录选择器 -->
      <div class="project-section">
        <ProjectDirectorySelector 
          ref="projectSelectorRef"
          @project-selected="handleProjectSelected"
          @project-changed="handleProjectChanged"
        />
      </div>
      
      <!-- 对话头部 -->
      <div class="chat-header">
        <div class="chat-title">
          {{ currentChat?.title || '新对话' }}
        </div>
        <div class="chat-actions">
          <div class="token-usage">
            本次会话消耗: {{ currentSessionTokens }} Tokens
          </div>
          <el-button 
            v-if="currentProject"
            link 
            size="small"
            @click="showProjectInfo"
          >
            <el-icon><InfoFilled /></el-icon>
            项目信息
          </el-button>
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesRef">
        <div v-if="messages.length === 0" class="welcome-message">
          <el-icon class="welcome-icon"><ChatDotRound /></el-icon>
          <h3>欢迎使用 Claude Platform</h3>
          <p>您可以通过文字或上传文件与 Claude Code 进行对话</p>
        </div>
        
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="message.role"
        >
          <div class="message-avatar">
            <el-icon v-if="message.role === 'user'">
              <User />
            </el-icon>
            <el-icon v-else>
              <Cpu />
            </el-icon>
          </div>
          
          <div class="message-content">
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            <div class="message-time">{{ formatTime(message.timestamp) }}</div>
          </div>
        </div>
        
        <!-- 加载中消息 -->
        <div v-if="isLoading" class="message-item assistant">
          <div class="message-avatar">
            <el-icon><Cpu /></el-icon>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
            <div class="message-text">Claude 正在思考中...</div>
          </div>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="chat-input">
        <div class="input-wrapper">
          <!-- 文件上传 -->
          <el-upload
            ref="uploadRef"
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            multiple
          >
            <el-button link :icon="Paperclip" title="上传文件" />
          </el-upload>
          
          <!-- 消息输入框 -->
          <el-input
            v-model="inputMessage"
            type="textarea"
            placeholder="输入消息，按 Ctrl+Enter 发送"
            :rows="3"
            :maxlength="2000"
            show-word-limit
            resize="none"
            class="message-input"
            @keydown="handleKeydown"
          />
          
          <!-- 发送按钮 -->
          <el-button
            type="primary"
            :loading="isLoading"
            :disabled="!inputMessage.trim()"
            @click="sendMessage"
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
        
        <!-- 已上传文件列表 -->
        <div v-if="uploadedFiles.length > 0" class="uploaded-files">
          <div class="file-title">已上传文件:</div>
          <el-tag
            v-for="file in uploadedFiles"
            :key="file.id"
            closable
            @close="removeUploadedFile(file.id)"
          >
            <el-icon><Document /></el-icon>
            {{ file.fileName }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus, ChatDotRound, User, Cpu, Paperclip,
  Promotion, Document, InfoFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'
import ProjectDirectorySelector from '@/components/ProjectDirectorySelector.vue'

const userStore = useUserStore()

// 引用
const messagesRef = ref()
const uploadRef = ref()
const projectSelectorRef = ref()

// 对话相关状态
const currentChatId = ref(null)
const currentChat = ref(null)
const inputMessage = ref('')
const isLoading = ref(false)
const currentSessionTokens = ref(0)

// 项目相关状态
const currentProject = ref(null)

// 消息列表
const messages = ref([])

// 对话历史（静态数据）
const chatHistory = ref([
  {
    id: '1',
    title: '分析日志文件',
    createdTime: Date.now() - 86400000, // 1天前
    updatedTime: Date.now() - 86400000
  },
  {
    id: '2', 
    title: '代码优化建议',
    createdTime: Date.now() - 172800000, // 2天前
    updatedTime: Date.now() - 172800000
  },
  {
    id: '3',
    title: 'API 接口设计',
    createdTime: Date.now() - 259200000, // 3天前
    updatedTime: Date.now() - 259200000
  }
])

// 上传文件相关
const uploadedFiles = ref([])
const uploadAction = computed(() => '/api/file/upload')
const uploadHeaders = computed(() => ({
  'Authorization': `Bearer ${userStore.token}`
}))

// 开始新对话
const startNewChat = () => {
  currentChatId.value = null
  currentChat.value = null
  messages.value = []
  currentSessionTokens.value = 0
  uploadedFiles.value = []
  inputMessage.value = ''
}

// 选择对话
const selectChat = async (chatId) => {
  try {
    currentChatId.value = chatId
    currentChat.value = chatHistory.value.find(chat => chat.id === chatId)
    
    // 加载对话消息（静态数据）
    messages.value = [
      {
        id: '1',
        role: 'user',
        content: '请帮我分析一下这个错误日志',
        timestamp: Date.now() - 3600000
      },
      {
        id: '2',
        role: 'assistant',
        content: '我来帮您分析这个错误日志。根据您提供的信息，这个错误主要是由于...\n\n```javascript\nconsole.log("示例代码");\n```\n\n建议您采取以下解决方案：\n1. 检查配置文件\n2. 更新依赖版本\n3. 重启服务',
        timestamp: Date.now() - 3500000
      }
    ]
    
    currentSessionTokens.value = 250
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('加载对话失败:', error)
    ElMessage.error('加载对话失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return
  
  const userMessage = {
    id: Date.now().toString(),
    role: 'user',
    content: inputMessage.value.trim(),
    timestamp: Date.now()
  }
  
  messages.value.push(userMessage)
  const messageToSend = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 显示加载状态
  isLoading.value = true
  
  try {
    // 模拟 API 调用（静态响应）
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    const assistantMessage = {
      id: (Date.now() + 1).toString(),
      role: 'assistant',
      content: `我收到了您的消息："${messageToSend}"。\n\n这是一个模拟的响应。在实际应用中，这里会调用后端API与Claude Code进行交互。\n\n**建议的解决方案：**\n1. 检查相关配置\n2. 查看日志文件\n3. 重试操作\n\n如果问题持续存在，请提供更多详细信息。`,
      timestamp: Date.now()
    }
    
    messages.value.push(assistantMessage)
    currentSessionTokens.value += 150 // 模拟token消耗
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
    
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')
  } finally {
    isLoading.value = false
  }
}

// 处理键盘事件
const handleKeydown = (event) => {
  if (event.ctrlKey && event.key === 'Enter') {
    event.preventDefault()
    sendMessage()
  }
}

// 文件上传相关
const beforeUpload = (file) => {
  const isValidSize = file.size / 1024 / 1024 < 50 // 50MB限制
  if (!isValidSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response, file) => {
  uploadedFiles.value.push({
    id: Date.now().toString(),
    fileName: file.name,
    fileId: response.data?.fileId,
    filePath: response.data?.filePath
  })
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  console.error('文件上传失败:', error)
  ElMessage.error('文件上传失败')
}

const removeUploadedFile = (fileId) => {
  uploadedFiles.value = uploadedFiles.value.filter(file => file.id !== fileId)
}

// 工具函数
const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diffDays = Math.floor((now - date) / (1000 * 60 * 60 * 24))
  
  if (diffDays === 0) {
    return '今天'
  } else if (diffDays === 1) {
    return '昨天'
  } else if (diffDays < 7) {
    return `${diffDays}天前`
  } else {
    return date.toLocaleDateString()
  }
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatMessage = (content) => {
  // 简单的 Markdown 格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

const scrollToBottom = () => {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 项目选择处理
const handleProjectSelected = (project) => {
  currentProject.value = project
  console.log('项目已选择:', project)
  
  // 可以在这里添加项目相关的初始化逻辑
  // 比如获取项目的 git 状态、读取 README 等
  
  // 更新欢迎消息，显示项目信息
  if (messages.value.length === 0) {
    const welcomeMessage = {
      id: 'welcome-' + Date.now(),
      role: 'assistant',
      content: `欢迎使用 Claude Code！

**当前工作目录：** \`${project.path}\`
**项目名称：** ${project.name}

${project.details?.isGitRepo ? '✅ 检测到 Git 仓库' : ''}
${project.details?.packageManager ? `📦 包管理器: ${project.details.packageManager}` : ''}
${project.details?.framework ? `🚀 框架: ${project.details.framework}` : ''}

现在您可以：
- 询问项目相关问题
- 请求代码分析和优化建议
- 上传文件进行分析
- 获取开发建议

请告诉我您需要什么帮助！`,
      timestamp: Date.now()
    }
    messages.value.push(welcomeMessage)
    
    nextTick(() => {
      scrollToBottom()
    })
  }
}

const handleProjectChanged = (project) => {
  currentProject.value = project
  if (!project) {
    // 项目被清除，可以重置相关状态
    console.log('项目已清除')
  }
}

const showProjectInfo = () => {
  if (!currentProject.value) return
  
  const project = currentProject.value
  ElMessage({
    type: 'info',
    duration: 0,
    showClose: true,
    message: `项目: ${project.name}\n路径: ${project.path}\n最后使用: ${new Date(project.lastUsed).toLocaleString()}`,
    dangerouslyUseHTMLString: false
  })
}

// 组件挂载时初始化
onMounted(() => {
  startNewChat()
  
  // 检查是否有保存的项目
  const savedProject = localStorage.getItem('claude-current-project')
  if (savedProject) {
    try {
      currentProject.value = JSON.parse(savedProject)
    } catch (error) {
      console.error('加载保存的项目失败:', error)
    }
  }
})
</script>

<style scoped>
.project-section {
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color-page);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-light);
  background: var(--el-bg-color);
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #909399;
  margin-bottom: 12px;
}

.empty-history {
  padding: 20px 0;
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.welcome-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-item.user .message-avatar {
  background: #409eff;
  color: white;
}

.message-item.assistant .message-avatar {
  background: #f5f7fa;
  color: #409eff;
}

.message-content {
  flex: 1;
  max-width: calc(100% - 44px);
}

.message-text {
  background: #f5f7fa;
  padding: 12px 16px;
  border-radius: 8px;
  word-wrap: break-word;
  line-height: 1.5;
}

.message-item.user .message-text {
  background: #409eff;
  color: white;
}

.message-time {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
  padding-left: 16px;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: typing 1.5s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
}

.uploaded-files {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

.file-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.uploaded-files .el-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}

/* 代码块样式 */
:deep(pre) {
  background: #2c3e50 !important;
  color: #fff !important;
  padding: 12px !important;
  border-radius: 4px !important;
  overflow-x: auto !important;
  margin: 8px 0 !important;
  font-size: 14px !important;
}

:deep(code) {
  background: #f1f2f3 !important;
  color: #e83e8c !important;
  padding: 2px 4px !important;
  border-radius: 3px !important;
  font-size: 85% !important;
}
</style>