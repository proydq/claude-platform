<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
      <p>管理系统的全局配置参数和运行策略</p>
    </div>
    
    <el-row :gutter="24">
      <!-- 设置表单 -->
      <el-col :span="16">
        <el-card shadow="never" class="settings-card">
          <el-tabs v-model="activeTab" type="border-card">
            <!-- 基础设置 -->
            <el-tab-pane label="基础设置" name="basic">
              <el-form
                ref="basicFormRef"
                :model="basicSettings"
                :rules="basicRules" 
                label-width="160px"
                size="large"
              >
                <el-form-item label="系统名称" prop="systemName">
                  <el-input
                    v-model="basicSettings.systemName"
                    placeholder="请输入系统名称"
                    style="width: 300px"
                  />
                </el-form-item>
                
                <el-form-item label="系统版本" prop="systemVersion">
                  <el-input
                    v-model="basicSettings.systemVersion"
                    placeholder="请输入系统版本号"
                    style="width: 200px"
                  />
                </el-form-item>
                
                <el-form-item label="系统描述">
                  <el-input
                    v-model="basicSettings.systemDescription"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入系统描述"
                    maxlength="500"
                    show-word-limit
                    style="width: 500px"
                  />
                </el-form-item>
                
                <el-form-item label="默认Token限制" prop="defaultTokenLimit">
                  <el-input-number
                    v-model="basicSettings.defaultTokenLimit"
                    :min="1000"
                    :max="100000"
                    :step="1000"
                    style="width: 200px"
                  />
                  <span class="unit">Tokens</span>
                  <div class="form-help">新用户的默认Token额度</div>
                </el-form-item>
                
                <el-form-item label="最大单次请求Token" prop="maxTokensPerRequest">
                  <el-input-number
                    v-model="basicSettings.maxTokensPerRequest"
                    :min="100"
                    :max="10000"
                    :step="100"
                    style="width: 200px"
                  />
                  <span class="unit">Tokens</span>
                  <div class="form-help">单次对话请求的最大Token数量</div>
                </el-form-item>
                
                <el-form-item label="系统维护模式">
                  <el-switch
                    v-model="basicSettings.maintenanceMode"
                    active-text="开启维护模式"
                    inactive-text="正常运行"
                  />
                  <div class="form-help">开启后，只有管理员可以访问系统</div>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 文件设置 -->
            <el-tab-pane label="文件设置" name="file">
              <el-form
                ref="fileFormRef"
                :model="fileSettings"
                :rules="fileRules"
                label-width="160px"
                size="large"
              >
                <el-form-item label="最大文件大小" prop="maxFileSize">
                  <el-input-number
                    v-model="fileSettings.maxFileSize"
                    :min="1"
                    :max="100"
                    :step="1"
                    style="width: 200px"
                  />
                  <span class="unit">MB</span>
                  <div class="form-help">单个文件上传的最大大小限制</div>
                </el-form-item>
                
                <el-form-item label="支持的文件类型" prop="allowedFileTypes">
                  <el-select
                    v-model="fileSettings.allowedFileTypes"
                    multiple
                    placeholder="选择支持的文件类型"
                    style="width: 400px"
                  >
                    <el-option label="文本文件 (.txt)" value="txt" />
                    <el-option label="代码文件 (.js, .py, .java 等)" value="code" />
                    <el-option label="配置文件 (.json, .yaml, .xml)" value="config" />
                    <el-option label="日志文件 (.log)" value="log" />
                    <el-option label="文档文件 (.md, .doc)" value="doc" />
                  </el-select>
                  <div class="form-help">用户可以上传的文件类型</div>
                </el-form-item>
                
                <el-form-item label="文件保存时间" prop="fileExpireHours">
                  <el-input-number
                    v-model="fileSettings.fileExpireHours"
                    :min="1"
                    :max="720"
                    :step="1"
                    style="width: 200px"
                  />
                  <span class="unit">小时</span>
                  <div class="form-help">上传文件在系统中的保存时间</div>
                </el-form-item>
                
                <el-form-item label="自动清理过期文件">
                  <el-switch
                    v-model="fileSettings.autoCleanup"
                    active-text="自动清理"
                    inactive-text="手动清理"
                  />
                  <div class="form-help">是否自动清理过期的上传文件</div>
                </el-form-item>
                
                <el-form-item label="文件存储路径" prop="uploadDir">
                  <el-input
                    v-model="fileSettings.uploadDir"
                    placeholder="文件存储目录路径"
                    style="width: 400px"
                  />
                  <div class="form-help">文件在服务器上的存储路径</div>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 安全设置 -->
            <el-tab-pane label="安全设置" name="security">
              <el-form
                ref="securityFormRef"
                :model="securitySettings"
                :rules="securityRules"
                label-width="160px"
                size="large"
              >
                <el-form-item label="JWT过期时间" prop="jwtExpiration">
                  <el-input-number
                    v-model="securitySettings.jwtExpiration"
                    :min="1"
                    :max="168"
                    :step="1"
                    style="width: 200px"
                  />
                  <span class="unit">小时</span>
                  <div class="form-help">用户登录Token的有效期</div>
                </el-form-item>
                
                <el-form-item label="密码最小长度" prop="minPasswordLength">
                  <el-input-number
                    v-model="securitySettings.minPasswordLength"
                    :min="6"
                    :max="20"
                    :step="1"
                    style="width: 200px"
                  />
                  <span class="unit">字符</span>
                  <div class="form-help">用户密码的最小长度要求</div>
                </el-form-item>
                
                <el-form-item label="强制密码复杂度">
                  <el-switch
                    v-model="securitySettings.requireStrongPassword"
                    active-text="要求复杂密码"
                    inactive-text="简单密码"
                  />
                  <div class="form-help">要求密码包含大小写字母、数字和特殊字符</div>
                </el-form-item>
                
                <el-form-item label="登录失败限制" prop="maxLoginAttempts">
                  <el-input-number
                    v-model="securitySettings.maxLoginAttempts"
                    :min="3"
                    :max="10"
                    :step="1"
                    style="width: 200px"
                  />
                  <span class="unit">次</span>
                  <div class="form-help">连续登录失败后锁定账号的次数</div>
                </el-form-item>
                
                <el-form-item label="账号锁定时间" prop="lockoutDuration">
                  <el-input-number
                    v-model="securitySettings.lockoutDuration"
                    :min="5"
                    :max="1440"
                    :step="5"
                    style="width: 200px"
                  />
                  <span class="unit">分钟</span>
                  <div class="form-help">账号被锁定的时间长度</div>
                </el-form-item>
                
                <el-form-item label="允许的跨域地址">
                  <el-input
                    v-model="securitySettings.allowedOrigins"
                    type="textarea"
                    :rows="3"
                    placeholder="每行一个域名，如：https://example.com"
                    style="width: 400px"
                  />
                  <div class="form-help">允许访问API的域名列表</div>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 数据清理 -->
            <el-tab-pane label="数据清理" name="cleanup">
              <el-form
                ref="cleanupFormRef"
                :model="cleanupSettings"
                label-width="160px"
                size="large"
              >
                <el-form-item label="对话历史保留" prop="conversationRetentionDays">
                  <el-input-number
                    v-model="cleanupSettings.conversationRetentionDays"
                    :min="7"
                    :max="365"
                    :step="1"
                    style="width: 200px"
                  />
                  <span class="unit">天</span>
                  <div class="form-help">对话记录在系统中的保留时间</div>
                </el-form-item>
                
                <el-form-item label="Token使用记录保留" prop="tokenUsageRetentionDays">
                  <el-input-number
                    v-model="cleanupSettings.tokenUsageRetentionDays"
                    :min="30"
                    :max="365"
                    :step="1"
                    style="width: 200px"
                  />
                  <span class="unit">天</span>
                  <div class="form-help">Token使用记录的保留时间</div>
                </el-form-item>
                
                <el-form-item label="自动清理时间">
                  <el-time-picker
                    v-model="cleanupSettings.autoCleanupTime"
                    placeholder="选择清理时间"
                    format="HH:mm"
                    value-format="HH:mm"
                    style="width: 200px"
                  />
                  <div class="form-help">每日自动执行数据清理的时间</div>
                </el-form-item>
                
                <el-form-item label="启用自动清理">
                  <el-switch
                    v-model="cleanupSettings.enableAutoCleanup"
                    active-text="自动清理"
                    inactive-text="手动清理"
                  />
                  <div class="form-help">是否定时自动清理过期数据</div>
                </el-form-item>
                
                <el-divider />
                
                <el-form-item label="手动清理操作">
                  <div class="manual-cleanup">
                    <el-button type="warning" @click="cleanupExpiredFiles">
                      清理过期文件
                    </el-button>
                    <el-button type="warning" @click="cleanupExpiredConversations">
                      清理过期对话
                    </el-button>
                    <el-button type="danger" @click="cleanupAllExpiredData">
                      清理所有过期数据
                    </el-button>
                  </div>
                  <div class="form-help">立即执行数据清理操作</div>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
          
          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button type="primary" size="large" :loading="saving" @click="saveSettings">
              <el-icon><Check /></el-icon>
              保存设置
            </el-button>
            
            <el-button size="large" @click="resetSettings">
              <el-icon><Refresh /></el-icon>
              重置为默认值
            </el-button>
            
            <el-button size="large" @click="exportSettings">
              <el-icon><Download /></el-icon>
              导出配置
            </el-button>
          </div>
        </el-card>
      </el-col>
      
      <!-- 系统状态 -->
      <el-col :span="8">
        <el-card shadow="never" class="status-card">
          <template #header>
            <div class="card-header">
              <el-icon><Monitor /></el-icon>
              <span>系统状态</span>
            </div>
          </template>
          
          <div class="system-status">
            <div class="status-item">
              <span class="label">系统运行状态:</span>
              <el-tag type="success" size="small">正常运行</el-tag>
            </div>
            
            <div class="status-item">
              <span class="label">在线用户数:</span>
              <span class="value">{{ systemStatus.onlineUsers }}</span>
            </div>
            
            <div class="status-item">
              <span class="label">今日请求数:</span>
              <span class="value">{{ systemStatus.todayRequests }}</span>
            </div>
            
            <div class="status-item">
              <span class="label">系统负载:</span>
              <el-progress
                :percentage="systemStatus.systemLoad"
                :color="getLoadColor(systemStatus.systemLoad)"
                :show-text="true"
              />
            </div>
            
            <div class="status-item">
              <span class="label">存储使用:</span>
              <el-progress
                :percentage="systemStatus.storageUsage"
                :color="getStorageColor(systemStatus.storageUsage)"
                :show-text="true"
              />
            </div>
          </div>
        </el-card>
        
        <!-- 最近操作日志 -->
        <el-card shadow="never" class="logs-card">
          <template #header>
            <div class="card-header">
              <el-icon><Document /></el-icon>
              <span>操作日志</span>
            </div>
          </template>
          
          <div class="operation-logs">
            <div 
              v-for="log in operationLogs"
              :key="log.id"
              class="log-item"
            >
              <div class="log-content">
                <div class="log-action">{{ log.action }}</div>
                <div class="log-time">{{ formatTime(log.timestamp) }}</div>
              </div>
              <el-tag :type="getLogType(log.level)" size="small">
                {{ log.level }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check, Refresh, Download, Monitor, Document
} from '@element-plus/icons-vue'
import api from '@/utils/api'

// 活动标签页
const activeTab = ref('basic')

// 表单引用
const basicFormRef = ref()
const fileFormRef = ref()
const securityFormRef = ref()
const cleanupFormRef = ref()

// 保存状态
const saving = ref(false)

// 基础设置
const basicSettings = reactive({
  systemName: 'Claude Platform',
  systemVersion: '1.0.0',
  systemDescription: '基于 Claude Code 的智能开发助手平台',
  defaultTokenLimit: 5000,
  maxTokensPerRequest: 4000,
  maintenanceMode: false
})

// 文件设置
const fileSettings = reactive({
  maxFileSize: 50,
  allowedFileTypes: ['txt', 'code', 'config', 'log'],
  fileExpireHours: 24,
  autoCleanup: true,
  uploadDir: './uploads'
})

// 安全设置
const securitySettings = reactive({
  jwtExpiration: 24,
  minPasswordLength: 6,
  requireStrongPassword: false,
  maxLoginAttempts: 5,
  lockoutDuration: 30,
  allowedOrigins: 'http://localhost:3000\nhttp://localhost:8081'
})

// 数据清理设置
const cleanupSettings = reactive({
  conversationRetentionDays: 30,
  tokenUsageRetentionDays: 90,
  autoCleanupTime: '02:00',
  enableAutoCleanup: true
})

// 表单验证规则
const basicRules = {
  systemName: [
    { required: true, message: '请输入系统名称', trigger: 'blur' }
  ],
  systemVersion: [
    { required: true, message: '请输入系统版本', trigger: 'blur' }
  ],
  defaultTokenLimit: [
    { required: true, message: '请输入默认Token限制', trigger: 'blur' }
  ],
  maxTokensPerRequest: [
    { required: true, message: '请输入最大单次请求Token', trigger: 'blur' }
  ]
}

const fileRules = {
  maxFileSize: [
    { required: true, message: '请输入最大文件大小', trigger: 'blur' }
  ],
  allowedFileTypes: [
    { required: true, message: '请选择支持的文件类型', trigger: 'change' }
  ],
  fileExpireHours: [
    { required: true, message: '请输入文件保存时间', trigger: 'blur' }
  ],
  uploadDir: [
    { required: true, message: '请输入文件存储路径', trigger: 'blur' }
  ]
}

const securityRules = {
  jwtExpiration: [
    { required: true, message: '请输入JWT过期时间', trigger: 'blur' }
  ],
  minPasswordLength: [
    { required: true, message: '请输入密码最小长度', trigger: 'blur' }
  ],
  maxLoginAttempts: [
    { required: true, message: '请输入登录失败限制', trigger: 'blur' }
  ],
  lockoutDuration: [
    { required: true, message: '请输入账号锁定时间', trigger: 'blur' }
  ]
}

// 系统状态
const systemStatus = reactive({
  onlineUsers: 15,
  todayRequests: 1248,
  systemLoad: 35,
  storageUsage: 68
})

// 操作日志
const operationLogs = ref([
  {
    id: '1',
    action: '修改系统默认Token限制',
    timestamp: Date.now() - 300000,
    level: 'INFO'
  },
  {
    id: '2',
    action: '清理过期文件',
    timestamp: Date.now() - 600000,
    level: 'SUCCESS'
  },
  {
    id: '3',
    action: '用户登录失败次数过多',
    timestamp: Date.now() - 900000,
    level: 'WARNING'
  },
  {
    id: '4',
    action: '启用维护模式',
    timestamp: Date.now() - 1200000,
    level: 'INFO'
  }
])

// 工具函数
const getLoadColor = (percentage) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

const getStorageColor = (percentage) => {
  if (percentage < 70) return '#67c23a'
  if (percentage < 90) return '#e6a23c'
  return '#f56c6c'
}

const getLogType = (level) => {
  const types = {
    INFO: 'info',
    SUCCESS: 'success',
    WARNING: 'warning',
    ERROR: 'danger'
  }
  return types[level] || 'info'
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 保存设置
const saveSettings = async () => {
  // 验证所有表单
  const forms = [basicFormRef.value, fileFormRef.value, securityFormRef.value]
  
  try {
    await Promise.all(forms.map(form => form?.validate()))
    
    saving.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    ElMessage.success('设置保存成功')
    
    // 添加操作日志
    operationLogs.value.unshift({
      id: Date.now().toString(),
      action: '保存系统设置',
      timestamp: Date.now(),
      level: 'SUCCESS'
    })
    
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('设置保存失败')
  } finally {
    saving.value = false
  }
}

// 重置设置
const resetSettings = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有设置为默认值吗？此操作不可恢复！',
      '确认重置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 重置为默认值
    Object.assign(basicSettings, {
      systemName: 'Claude Platform',
      systemVersion: '1.0.0',
      systemDescription: '基于 Claude Code 的智能开发助手平台',
      defaultTokenLimit: 5000,
      maxTokensPerRequest: 4000,
      maintenanceMode: false
    })
    
    Object.assign(fileSettings, {
      maxFileSize: 50,
      allowedFileTypes: ['txt', 'code', 'config', 'log'],
      fileExpireHours: 24,
      autoCleanup: true,
      uploadDir: './uploads'
    })
    
    Object.assign(securitySettings, {
      jwtExpiration: 24,
      minPasswordLength: 6,
      requireStrongPassword: false,
      maxLoginAttempts: 5,
      lockoutDuration: 30,
      allowedOrigins: 'http://localhost:3000\nhttp://localhost:8081'
    })
    
    Object.assign(cleanupSettings, {
      conversationRetentionDays: 30,
      tokenUsageRetentionDays: 90,
      autoCleanupTime: '02:00',
      enableAutoCleanup: true
    })
    
    ElMessage.success('设置已重置为默认值')
    
  } catch {
    // 用户取消
  }
}

// 导出设置
const exportSettings = () => {
  const settings = {
    basic: basicSettings,
    file: fileSettings,
    security: securitySettings,
    cleanup: cleanupSettings,
    exportTime: new Date().toISOString()
  }
  
  const dataStr = JSON.stringify(settings, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  
  const link = document.createElement('a')
  link.href = URL.createObjectURL(dataBlob)
  link.download = `claude-platform-settings-${new Date().toISOString().split('T')[0]}.json`
  link.click()
  
  ElMessage.success('配置已导出')
}

// 数据清理操作
const cleanupExpiredFiles = async () => {
  try {
    await ElMessageBox.confirm('确定要清理所有过期文件吗？', '确认清理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 模拟清理操作
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('过期文件清理完成')
    
  } catch {
    // 用户取消
  }
}

const cleanupExpiredConversations = async () => {
  try {
    await ElMessageBox.confirm('确定要清理所有过期对话记录吗？', '确认清理', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('过期对话记录清理完成')
    
  } catch {
    // 用户取消
  }
}

const cleanupAllExpiredData = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清理所有过期数据吗？包括文件、对话记录和使用统计！',
      '确认清理',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await new Promise(resolve => setTimeout(resolve, 3000))
    
    ElMessage.success('所有过期数据清理完成')
    
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  // 加载系统设置
})
</script>

<style scoped>
.settings-card {
  margin-bottom: 24px;
}

.status-card,
.logs-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.form-help {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.action-buttons {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
  text-align: center;
}

.action-buttons .el-button {
  margin: 0 8px;
}

.system-status {
  space-y: 16px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.status-item .label {
  color: #606266;
  font-size: 14px;
}

.status-item .value {
  font-weight: 500;
  color: #2c3e50;
}

.operation-logs {
  max-height: 300px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px;
  background: #f9f9f9;
  border-radius: 4px;
}

.log-content .log-action {
  font-size: 14px;
  color: #2c3e50;
  margin-bottom: 2px;
}

.log-content .log-time {
  font-size: 12px;
  color: #c0c4cc;
}

.manual-cleanup {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.manual-cleanup .el-button {
  margin-bottom: 8px;
}

:deep(.el-tabs__content) {
  padding: 24px 0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-progress) {
  width: 120px;
}
</style>