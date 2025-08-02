<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">下载客户端</h1>
      <p>下载并配置 Claude Connector 本地客户端，实现与 Claude Code 的连接</p>
    </div>
    
    <!-- 连接状态 -->
    <el-card class="status-card" shadow="never">
      <div class="connection-status">
        <div class="status-indicator">
          <el-icon 
            class="status-icon" 
            :class="connectionStatus.connected ? 'connected' : 'disconnected'"
          >
            <CircleCheckFilled v-if="connectionStatus.connected" />
            <CircleCloseFilled v-else />
          </el-icon>
          <div class="status-text">
            <div class="status-title">
              {{ connectionStatus.connected ? '已连接' : '未连接' }}
            </div>
            <div class="status-desc">
              {{ connectionStatus.connected ? '客户端正常运行中' : '请下载并启动客户端' }}
            </div>
          </div>
        </div>
        
        <el-button @click="checkConnection" :loading="checking">
          <el-icon><Refresh /></el-icon>
          检查连接
        </el-button>
      </div>
    </el-card>
    
    <!-- 系统检测 -->
    <el-card class="system-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Monitor /></el-icon>
          <span>系统信息</span>
        </div>
      </template>
      
      <div class="system-info">
        <div class="info-item">
          <span class="label">检测到您的系统:</span>
          <el-tag type="info">{{ detectedOS }}</el-tag>
        </div>
        <div class="info-item">
          <span class="label">架构:</span>
          <el-tag type="info">{{ detectedArch }}</el-tag>
        </div>
      </div>
    </el-card>
    
    <!-- 下载区域 -->
    <el-card class="download-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Download /></el-icon>
          <span>客户端下载</span>
        </div>
      </template>
      
      <!-- 推荐下载 -->
      <div class="recommended-download">
        <div class="download-item recommended">
          <div class="platform-info">
            <el-icon class="platform-icon"><Monitor /></el-icon>
            <div class="platform-details">
              <h3>{{ recommendedPlatform.name }}</h3>
              <p>{{ recommendedPlatform.description }}</p>
            </div>
          </div>
          
          <el-button 
            type="primary" 
            size="large"
            :loading="downloading === recommendedPlatform.key"
            @click="downloadClient(recommendedPlatform)"
          >
            <el-icon><Download /></el-icon>
            立即下载
          </el-button>
        </div>
      </div>
      
      <!-- 其他版本 -->
      <el-divider>其他版本</el-divider>
      
      <div class="other-downloads">
        <div 
          v-for="platform in otherPlatforms" 
          :key="platform.key"
          class="download-item"
        >
          <div class="platform-info">
            <el-icon class="platform-icon" v-html="platform.icon"></el-icon>
            <div class="platform-details">
              <h4>{{ platform.name }}</h4>
              <p>{{ platform.description }}</p>
            </div>
          </div>
          
          <el-button 
            :loading="downloading === platform.key"
            @click="downloadClient(platform)"
          >
            <el-icon><Download /></el-icon>
            下载
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 配置说明 -->
    <el-card class="config-card" shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon><Document /></el-icon>
          <span>配置说明</span>
        </div>
      </template>
      
      <el-steps :active="configStep" finish-status="success" align-center>
        <el-step title="下载客户端" description="下载对应系统的客户端程序" />
        <el-step title="解压文件" description="解压下载的压缩包到指定目录" />
        <el-step title="配置连接" description="修改配置文件中的服务器地址" />
        <el-step title="启动程序" description="运行客户端并检查连接状态" />
      </el-steps>
      
      <div class="config-details">
        <el-collapse v-model="activeCollapse">
          <el-collapse-item title="1. 下载并解压" name="1">
            <p>下载对应系统版本的客户端程序，解压到任意目录。</p>
            <p><strong>推荐路径:</strong></p>
            <ul>
              <li>Windows: <code>C:\Program Files\Claude Connector\</code></li>
              <li>macOS: <code>/Applications/Claude Connector/</code></li>
              <li>Linux: <code>/opt/claude-connector/</code></li>
            </ul>
          </el-collapse-item>
          
          <el-collapse-item title="2. 修改配置文件" name="2">
            <p>编辑 <code>config.yaml</code> 文件，设置服务器连接信息：</p>
            <el-input
              v-model="configTemplate"
              type="textarea"
              :rows="8"
              readonly
              class="config-code"
            />
            <p class="config-note">
              <el-icon><InfoFilled /></el-icon>
              请将 <code>server_url</code> 修改为实际的服务器地址
            </p>
          </el-collapse-item>
          
          <el-collapse-item title="3. 启动客户端" name="3">
            <p><strong>Windows:</strong></p>
            <p>双击运行 <code>claude-connector.exe</code> 或在命令行中运行</p>
            
            <p><strong>macOS/Linux:</strong></p>
            <el-input
              value="./claude-connector"
              readonly
              class="command-line"
            >
              <template #prepend>$</template>
            </el-input>
          </el-collapse-item>
          
          <el-collapse-item title="4. 验证连接" name="4">
            <p>客户端启动后，程序会自动连接到服务器。您可以：</p>
            <ul>
              <li>查看客户端控制台输出</li>
              <li>在本页面点击"检查连接"按钮</li>
              <li>在对话页面测试功能</li>
            </ul>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Download, Monitor, Document, Refresh,
  CircleCheckFilled, CircleCloseFilled, InfoFilled
} from '@element-plus/icons-vue'
import api from '@/utils/api'

// 连接状态
const connectionStatus = ref({
  connected: false,
  lastCheck: null
})
const checking = ref(false)
const downloading = ref(null)

// 配置说明
const configStep = ref(0)
const activeCollapse = ref(['1'])

// 系统检测
const detectedOS = ref('Unknown')
const detectedArch = ref('Unknown')

// 配置模板
const configTemplate = ref(`# Claude Connector 配置文件
server:
  # 服务器地址 (请修改为实际地址)
  url: "ws://localhost:10088/ws/chat"
  # 认证Token (从Web界面获取)
  token: "your-auth-token-here"

client:
  # 客户端ID (可选，自动生成)
  id: "claude-connector-client"
  # 重连间隔 (秒)
  reconnect_interval: 5
  # 心跳间隔 (秒)  
  heartbeat_interval: 30

logging:
  # 日志级别: debug, info, warn, error
  level: "info"
  # 日志文件路径
  file: "./logs/claude-connector.log"`)

// 支持的平台
const platforms = [
  {
    key: 'windows-x64',
    name: 'Windows 64位',
    description: '适用于 Windows 10/11 64位系统',
    icon: '<Monitor />',
    downloadUrl: '/api/download/claude-connector-windows-x64.zip'
  },
  {
    key: 'macos-intel',
    name: 'macOS Intel',
    description: '适用于 Intel 芯片的 Mac 系统',
    icon: '<Monitor />',
    downloadUrl: '/api/download/claude-connector-macos-intel.zip'
  },
  {
    key: 'macos-arm',
    name: 'macOS Apple Silicon',
    description: '适用于 M1/M2 芯片的 Mac 系统',
    icon: '<Monitor />',
    downloadUrl: '/api/download/claude-connector-macos-arm.zip'
  },
  {
    key: 'linux-x64',
    name: 'Linux 64位',
    description: '适用于 Linux x86_64 系统',
    icon: '<Monitor />',
    downloadUrl: '/api/download/claude-connector-linux-x64.tar.gz'
  }
]

// 推荐平台
const recommendedPlatform = computed(() => {
  const os = detectedOS.value.toLowerCase()
  const arch = detectedArch.value.toLowerCase()
  
  if (os.includes('windows')) {
    return platforms.find(p => p.key === 'windows-x64')
  } else if (os.includes('mac')) {
    if (arch.includes('arm') || arch.includes('apple')) {
      return platforms.find(p => p.key === 'macos-arm')
    } else {
      return platforms.find(p => p.key === 'macos-intel')
    }
  } else if (os.includes('linux')) {
    return platforms.find(p => p.key === 'linux-x64')
  }
  
  return platforms[0] // 默认返回 Windows 版本
})

// 其他平台
const otherPlatforms = computed(() => {
  return platforms.filter(p => p.key !== recommendedPlatform.value?.key)
})

// 检测系统信息
const detectSystem = () => {
  const userAgent = navigator.userAgent
  const platform = navigator.platform
  
  // 检测操作系统
  if (userAgent.includes('Windows')) {
    detectedOS.value = 'Windows'
  } else if (userAgent.includes('Mac')) {
    detectedOS.value = 'macOS'
  } else if (userAgent.includes('Linux')) {
    detectedOS.value = 'Linux'
  } else {
    detectedOS.value = 'Unknown'
  }
  
  // 检测架构
  if (platform.includes('Win32') || platform.includes('Win64')) {
    detectedArch.value = 'x64'
  } else if (platform.includes('Intel') || platform.includes('x86')) {
    detectedArch.value = 'Intel'
  } else if (platform.includes('ARM') || userAgent.includes('Apple')) {
    detectedArch.value = 'ARM'
  } else {
    detectedArch.value = 'x64'
  }
}

// 检查连接状态
const checkConnection = async () => {
  checking.value = true
  
  try {
    // 模拟检查连接状态
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 随机模拟连接状态
    connectionStatus.value = {
      connected: Math.random() > 0.5,
      lastCheck: new Date()
    }
    
    if (connectionStatus.value.connected) {
      ElMessage.success('客户端连接正常')
    } else {
      ElMessage.warning('客户端未连接，请检查客户端是否正常运行')
    }
  } catch (error) {
    console.error('检查连接失败:', error)
    ElMessage.error('检查连接失败')
  } finally {
    checking.value = false
  }
}

// 下载客户端
const downloadClient = async (platform) => {
  downloading.value = platform.key
  
  try {
    // 模拟下载过程
    ElMessage.info(`正在准备下载 ${platform.name} 版本...`)
    
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 创建下载链接
    const link = document.createElement('a')
    link.href = platform.downloadUrl
    link.download = platform.downloadUrl.split('/').pop()
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('下载开始，请查看浏览器下载进度')
    
    // 下载后更新配置步骤
    configStep.value = 1
    
  } catch (error) {
    console.error('下载失败:', error)
    ElMessage.error('下载失败，请稍后重试')
  } finally {
    downloading.value = null
  }
}

onMounted(() => {
  detectSystem()
  checkConnection()
})
</script>

<style scoped>
.page-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin-bottom: 8px;
}

.page-header p {
  color: #909399;
  font-size: 14px;
}

.status-card,
.system-card,
.download-card,
.config-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.connection-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-icon {
  font-size: 24px;
}

.status-icon.connected {
  color: #67c23a;
}

.status-icon.disconnected {
  color: #f56c6c;
}

.status-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.status-desc {
  color: #909399;
  font-size: 14px;
}

.system-info {
  display: flex;
  gap: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  color: #606266;
  font-size: 14px;
}

.recommended-download {
  margin-bottom: 24px;
}

.download-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.download-item:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.download-item.recommended {
  border-color: #409eff;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.platform-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.platform-icon {
  font-size: 32px;
  color: #409eff;
}

.platform-details h3,
.platform-details h4 {
  margin: 0 0 4px 0;
  font-weight: 500;
}

.platform-details p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.other-downloads .download-item {
  background: white;
}

.config-details {
  margin-top: 24px;
}

.config-code {
  margin: 12px 0;
}

.config-code :deep(.el-textarea__inner) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  background: #f5f7fa;
}

.config-note {
  color: #e6a23c;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
}

.command-line {
  margin: 8px 0;
}

.command-line :deep(.el-input__inner) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  background: #2c3e50;
  color: #fff;
}

.command-line :deep(.el-input-group__prepend) {
  background: #34495e;
  color: #fff;
  border-color: #34495e;
}

ul {
  margin: 8px 0;
  padding-left: 20px;
}

li {
  margin-bottom: 4px;
}

code {
  background: #f1f2f3;
  color: #e83e8c;
  padding: 2px 4px;
  border-radius: 3px;
  font-size: 85%;
}
</style>