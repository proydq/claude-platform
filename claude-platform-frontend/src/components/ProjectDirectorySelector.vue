<template>
  <div class="project-selector">
    <!-- 当前项目状态 -->
    <div class="current-project" v-if="currentProject">
      <div class="project-header">
        <el-icon class="project-icon"><Folder /></el-icon>
        <div class="project-info">
          <div class="project-name">{{ currentProject.name }}</div>
          <div class="project-path">{{ currentProject.path }}</div>
        </div>
        <el-button 
          link 
          size="small" 
          class="change-btn"
          @click="showSelector = true"
        >
          <el-icon><Edit /></el-icon>
          切换项目
        </el-button>
      </div>
      
      <!-- 项目信息 -->
      <div class="project-details" v-if="currentProject.details">
        <el-tag 
          v-if="currentProject.details.isGitRepo" 
          type="success" 
          size="small"
        >
          <el-icon><Connection /></el-icon>
          Git仓库
          <span v-if="currentProject.details.gitBranch"> ({{ currentProject.details.gitBranch }})</span>
        </el-tag>
        <el-tag 
          v-if="currentProject.details.packageManager" 
          type="info" 
          size="small"
        >
          {{ currentProject.details.packageManager }}
        </el-tag>
        <el-tag 
          v-if="currentProject.details.framework"
          type="warning" 
          size="small"
        >
          {{ currentProject.details.framework }}
        </el-tag>
        <el-tag 
          v-if="currentProject.details.language"
          type="primary" 
          size="small"
        >
          {{ currentProject.details.language }}
        </el-tag>
        <el-tag 
          v-if="currentProject.details.totalFiles"
          type="info" 
          size="small"
        >
          {{ formatNumber(currentProject.details.totalFiles) }} 文件
        </el-tag>
        <el-tag 
          v-if="currentProject.details.totalLines"
          type="info" 
          size="small"
        >
          {{ formatNumber(currentProject.details.totalLines) }} 行代码
        </el-tag>
      </div>
    </div>

    <!-- 选择项目目录 -->
    <div class="no-project" v-else>
      <div class="no-project-content">
        <el-icon class="no-project-icon"><FolderOpened /></el-icon>
        <div class="no-project-text">
          <h3>选择项目目录</h3>
          <p>请选择要使用 Claude Code 的项目目录</p>
        </div>
        <el-button 
          type="primary" 
          size="large"
          @click="showSelector = true"
        >
          <el-icon><Plus /></el-icon>
          选择目录
        </el-button>
      </div>
    </div>

    <!-- 目录选择对话框 -->
    <el-dialog
      v-model="showSelector"
      title="选择项目目录"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="selector-content">
        <!-- 选择方式 -->
        <el-tabs v-model="selectMethod" class="selector-tabs">
          <el-tab-pane label="浏览目录" name="browse">
            <div class="browse-section">
              <el-alert
                title="目录选择说明"
                type="warning"
                :closable="false"
                show-icon
              >
                <template #default>
                  <div>
                    <p><strong>注意：</strong>浏览器目录选择会读取文件夹结构用于项目分析，不会上传文件内容。</p>
                    <p>推荐使用 <strong>"输入路径"</strong> 方式，更快速且不读取文件。</p>
                  </div>
                </template>
              </el-alert>
              
              <div class="browse-actions">
                <input
                  ref="directoryInput"
                  type="file"
                  webkitdirectory
                  multiple
                  style="display: none"
                  @change="handleDirectorySelect"
                />
                <el-button 
                  type="primary"
                  size="large"
                  @click="confirmDirectorySelect"
                >
                  <el-icon><FolderAdd /></el-icon>
                  选择文件夹（仅分析结构）
                </el-button>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="输入路径" name="input">
            <div class="input-section">
              <el-alert
                title="推荐方式"
                type="success"
                :closable="false"
                show-icon
              >
                <template #default>
                  <div>
                    <p><strong>推荐使用此方式：</strong>直接输入项目路径，不读取任何文件内容。</p>
                    <p>项目分析将通过后端API安全地进行，仅分析必要的项目信息。</p>
                  </div>
                </template>
              </el-alert>
              
              <el-form :model="pathForm" class="path-form">
                <el-form-item>
                  <el-input
                    v-model="pathForm.path"
                    placeholder="例如: D:\projects\my-project"
                    size="large"
                    clearable
                  >
                    <template #prepend>
                      <el-icon><Folder /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item>
                  <el-button 
                    type="primary"
                    @click="handlePathSubmit"
                    :disabled="!pathForm.path.trim()"
                  >
                    确认路径
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>

        <!-- 最近使用的项目 -->
        <div class="recent-projects" v-if="recentProjects.length > 0">
          <el-divider>最近使用</el-divider>
          <div class="recent-list">
            <div 
              v-for="project in recentProjects" 
              :key="project.path"
              class="recent-item"
              @click="selectRecentProject(project)"
            >
              <el-icon class="recent-icon"><Folder /></el-icon>
              <div class="recent-info">
                <div class="recent-name">{{ project.name }}</div>
                <div class="recent-path">{{ project.path }}</div>
              </div>
              <div class="recent-time">{{ formatTime(project.lastUsed) }}</div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showSelector = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 项目信任确认对话框 -->
    <el-dialog
      v-model="showTrustDialog"
      title="项目信任确认"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="trust-dialog">
        <div class="trust-question">
          <el-icon class="trust-icon"><QuestionFilled /></el-icon>
          <h3>Do you trust this directory?</h3>
        </div>
        
        <div class="trust-info">
          <div class="trust-path">
            <strong>路径:</strong> {{ selectedDirectory?.path }}
          </div>
          <div class="trust-warning">
            <el-alert
              title="安全提醒"
              type="warning"
              :closable="false"
              show-icon
            >
              只有信任的目录才能访问本地文件系统。请确认这是一个安全的项目目录。
            </el-alert>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="handleTrustDecline">No, I don't trust</el-button>
        <el-button type="primary" @click="handleTrustConfirm">Yes, I trust</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Folder, 
  FolderOpened, 
  FolderAdd,
  Edit, 
  Plus, 
  Connection,
  QuestionFilled
} from '@element-plus/icons-vue'
import api from '@/utils/api'

// Props and Emits
const emit = defineEmits(['project-selected', 'project-changed'])

// 响应式数据
const showSelector = ref(false)
const showTrustDialog = ref(false)
const selectMethod = ref('browse')
const currentProject = ref(null)
const selectedDirectory = ref(null)
const recentProjects = ref([])

const pathForm = reactive({
  path: ''
})

// 计算属性
const hasProject = computed(() => !!currentProject.value)

// 生命周期
onMounted(() => {
  loadRecentProjects()
  loadCurrentProject()
})

// 处理目录选择
const handleDirectorySelect = (event) => {
  const files = event.target.files
  if (files.length === 0) return

  // 获取第一个文件的路径信息
  const firstFile = files[0]
  const pathParts = firstFile.webkitRelativePath.split('/')
  const projectName = pathParts[0]
  
  // 模拟获取完整路径（实际需要通过API获取）
  const projectPath = firstFile.webkitRelativePath.replace('/' + pathParts.slice(1).join('/'), '')
  
  selectedDirectory.value = {
    name: projectName,
    path: projectPath,
    files: Array.from(files)
  }

  showSelector.value = false
  showTrustDialog.value = true
}

// 处理路径输入
const handlePathSubmit = () => {
  const path = pathForm.path.trim()
  if (!path) return

  const pathParts = path.split(/[\/\\]/)
  const projectName = pathParts[pathParts.length - 1] || pathParts[pathParts.length - 2]

  selectedDirectory.value = {
    name: projectName,
    path: path,
    files: []
  }

  showSelector.value = false
  showTrustDialog.value = true
}

// 选择最近项目
const selectRecentProject = (project) => {
  selectedDirectory.value = project
  showSelector.value = false
  showTrustDialog.value = true
}

// 处理信任确认
const handleTrustConfirm = async () => {
  if (!selectedDirectory.value) return

  try {
    // 分析项目信息
    const projectDetails = await analyzeProject(selectedDirectory.value)
    
    const project = {
      ...selectedDirectory.value,
      details: projectDetails,
      lastUsed: new Date().toISOString()
    }

    currentProject.value = project
    showTrustDialog.value = false

    // 保存到最近使用
    saveToRecentProjects(project)
    
    // 保存当前项目
    saveCurrentProject(project)

    ElMessage.success(`项目 "${project.name}" 已成功加载`)
    emit('project-selected', project)
  } catch (error) {
    console.error('项目加载失败:', error)
    ElMessage.error('项目加载失败，请重试')
  }
}

// 处理信任拒绝
const handleTrustDecline = () => {
  selectedDirectory.value = null
  showTrustDialog.value = false
  ElMessage.info('已取消项目选择')
}

// 分析项目信息
const analyzeProject = async (directory) => {
  try {
    // 调用后端API分析项目
    const response = await api.post('/project/analyze', {
      projectPath: directory.path,
      projectName: directory.name,
      excludeDirectories: [
        'node_modules', '.git', 'target', 'build', 
        'dist', '.idea', '.vscode', '*.log'
      ]
    })

    if (response.data) {
      const projectInfo = response.data
      return {
        isGitRepo: projectInfo.isGitRepo,
        gitBranch: projectInfo.gitBranch,
        gitRemoteUrl: projectInfo.gitRemoteUrl,
        packageManager: projectInfo.packageManager,
        framework: projectInfo.framework,
        language: projectInfo.language,
        languages: projectInfo.languages || [],
        totalFiles: projectInfo.totalFiles,
        totalLines: projectInfo.totalLines,
        totalSize: projectInfo.totalSize,
        fileStructure: projectInfo.fileStructure,
        configFiles: projectInfo.configFiles
      }
    }
  } catch (error) {
    console.error('项目分析API调用失败:', error)
    // 降级到本地分析
    return analyzeProjectLocally(directory)
  }
}

// 本地项目分析（作为后备方案）
const analyzeProjectLocally = (directory) => {
  const details = {
    isGitRepo: false,
    packageManager: null,
    framework: null,
    languages: []
  }

  // 检查文件类型来判断项目特征
  if (directory.files) {
    const fileNames = directory.files.map(f => f.name.toLowerCase())
    
    if (fileNames.includes('.git') || fileNames.some(f => f.includes('.git'))) {
      details.isGitRepo = true
    }
    
    if (fileNames.includes('package.json')) {
      details.packageManager = 'npm'
      details.framework = 'Node.js'
      details.languages.push('JavaScript')
    }
    
    if (fileNames.includes('pom.xml')) {
      details.packageManager = 'maven'
      details.framework = 'Java'
      details.languages.push('Java')
    }
    
    if (fileNames.includes('requirements.txt') || fileNames.includes('pyproject.toml')) {
      details.packageManager = 'pip'
      details.framework = 'Python'
      details.languages.push('Python')
    }
  }

  return details
}

// 本地存储相关
const loadRecentProjects = () => {
  const saved = localStorage.getItem('claude-recent-projects')
  if (saved) {
    recentProjects.value = JSON.parse(saved).slice(0, 5) // 最多显示5个
  }
}

const saveToRecentProjects = (project) => {
  const recent = recentProjects.value.filter(p => p.path !== project.path)
  recent.unshift(project)
  recentProjects.value = recent.slice(0, 5)
  localStorage.setItem('claude-recent-projects', JSON.stringify(recentProjects.value))
}

const loadCurrentProject = () => {
  const saved = localStorage.getItem('claude-current-project')
  if (saved) {
    currentProject.value = JSON.parse(saved)
  }
}

const saveCurrentProject = (project) => {
  localStorage.setItem('claude-current-project', JSON.stringify(project))
}

// 时间格式化
const formatTime = (timeStr) => {
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

// 数字格式化
const formatNumber = (num) => {
  if (!num) return '0'
  if (num < 1000) return num.toString()
  if (num < 1000000) return (num / 1000).toFixed(1) + 'K'
  return (num / 1000000).toFixed(1) + 'M'
}

// 暴露方法
defineExpose({
  showSelector: () => { showSelector.value = true },
  getCurrentProject: () => currentProject.value,
  clearProject: () => {
    currentProject.value = null
    localStorage.removeItem('claude-current-project')
    emit('project-changed', null)
  }
})
</script>

<style scoped>
.project-selector {
  margin-bottom: 16px;
}

.current-project {
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  padding: 16px;
  background: var(--el-bg-color-page);
}

.project-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-icon {
  font-size: 24px;
  color: var(--el-color-primary);
}

.project-info {
  flex: 1;
}

.project-name {
  font-weight: 600;
  font-size: 16px;
  color: var(--el-text-color-primary);
}

.project-path {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.change-btn {
  padding: 4px 8px;
}

.project-details {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.no-project {
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  background: var(--el-fill-color-blank);
}

.no-project-icon {
  font-size: 48px;
  color: var(--el-text-color-placeholder);
  margin-bottom: 16px;
}

.no-project-text h3 {
  margin: 0 0 8px 0;
  color: var(--el-text-color-primary);
}

.no-project-text p {
  margin: 0 0 20px 0;
  color: var(--el-text-color-secondary);
}

.selector-content {
  max-height: 60vh;
  overflow-y: auto;
}

.browse-section,
.input-section {
  padding: 20px 0;
}

.browse-actions {
  margin-top: 20px;
  text-align: center;
}

.path-form {
  margin-top: 20px;
}

.recent-projects {
  margin-top: 20px;
}

.recent-list {
  max-height: 200px;
  overflow-y: auto;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.recent-item:hover {
  background: var(--el-fill-color-light);
}

.recent-icon {
  font-size: 20px;
  color: var(--el-color-primary);
}

.recent-info {
  flex: 1;
}

.recent-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.recent-path {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.recent-time {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.trust-dialog {
  text-align: center;
}

.trust-question {
  margin-bottom: 20px;
}

.trust-icon {
  font-size: 48px;
  color: var(--el-color-warning);
  margin-bottom: 12px;
}

.trust-question h3 {
  margin: 0;
  color: var(--el-text-color-primary);
}

.trust-info {
  text-align: left;
}

.trust-path {
  margin-bottom: 16px;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  font-family: monospace;
  word-break: break-all;
}
</style>