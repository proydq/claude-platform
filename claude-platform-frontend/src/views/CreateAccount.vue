<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">开通账号</h1>
      <p>为用户创建新的系统账号，设置基本信息和访问权限</p>
    </div>
    
    <el-row :gutter="24">
      <!-- 表单区域 -->
      <el-col :span="16">
        <el-card shadow="never" class="form-card">
          <template #header>
            <div class="card-header">
              <el-icon><UserFilled /></el-icon>
              <span>用户信息</span>
            </div>
          </template>
          
          <el-form
            ref="createFormRef"
            :model="createForm"
            :rules="createRules"
            label-width="120px"
            size="large"
          >
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="createForm.username"
                placeholder="请输入用户名（3-50个字符）"
                clearable
                @blur="checkUsername"
              />
              <div class="form-tip">
                用户名用于登录，只能包含字母、数字和下划线
              </div>
            </el-form-item>
            
            <el-form-item label="真实姓名" prop="realName">
              <el-input
                v-model="createForm.realName"
                placeholder="请输入用户的真实姓名"
                clearable
              />
            </el-form-item>
            
            <el-form-item label="邮箱地址" prop="email">
              <el-input
                v-model="createForm.email"
                placeholder="请输入邮箱地址"
                clearable
                @blur="checkEmail"
              />
              <div class="form-tip">
                邮箱用于接收系统通知和密码重置
              </div>
            </el-form-item>
            
            <el-form-item label="用户角色" prop="userRole">
              <el-radio-group v-model="createForm.userRole">
                <el-radio label="ROLE_USER">
                  <div class="role-option">
                    <div class="role-title">普通用户</div>
                    <div class="role-desc">可以使用对话功能，查看个人统计</div>
                  </div>
                </el-radio>
                <el-radio label="ROLE_ADMIN">
                  <div class="role-option">
                    <div class="role-title">管理员</div>
                    <div class="role-desc">拥有所有权限，可以管理用户和系统设置</div>
                  </div>
                </el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="Token额度" prop="tokenLimit">
              <el-input-number
                v-model="createForm.tokenLimit"
                :min="0"
                :max="1000000"
                :step="1000"
                style="width: 200px"
              />
              <span class="token-unit">Tokens</span>
              <div class="form-tip">
                用户可用的Token总额度，用完后无法继续使用
              </div>
            </el-form-item>
            
            <el-form-item label="初始密码" prop="password">
              <el-input
                v-model="createForm.password"
                type="password"
                placeholder="请输入初始密码（至少6位）"
                show-password
                clearable
              />
              <div class="form-tip">
                <el-button link @click="generatePassword">
                  自动生成密码
                </el-button>
              </div>
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="createForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                clearable
              />
            </el-form-item>
            
            <el-form-item label="账号状态" prop="userStatus">
              <el-radio-group v-model="createForm.userStatus">
                <el-radio label="ACTIVE">正常</el-radio>
                <el-radio label="DISABLED">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="备注信息">
              <el-input
                v-model="createForm.remarks"
                type="textarea"
                :rows="3"
                placeholder="可以添加一些备注信息（可选）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item>
              <el-button 
                type="primary" 
                size="large"
                :loading="submitting"
                @click="handleCreate"
              >
                <el-icon><Check /></el-icon>
                创建账号
              </el-button>
              
              <el-button size="large" @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置表单
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      
      <!-- 预览和提示区域 -->
      <el-col :span="8">
        <!-- 账号预览 -->
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="card-header">
              <el-icon><View /></el-icon>
              <span>账号预览</span>
            </div>
          </template>
          
          <div class="account-preview">
            <div class="preview-item">
              <span class="label">用户名:</span>
              <span class="value">{{ createForm.username || '未设置' }}</span>
            </div>
            <div class="preview-item">
              <span class="label">真实姓名:</span>
              <span class="value">{{ createForm.realName || '未设置' }}</span>
            </div>
            <div class="preview-item">
              <span class="label">邮箱:</span>
              <span class="value">{{ createForm.email || '未设置' }}</span>
            </div>
            <div class="preview-item">
              <span class="label">角色:</span>
              <el-tag :type="createForm.userRole === 'ROLE_ADMIN' ? 'danger' : 'primary'" size="small">
                {{ createForm.userRole === 'ROLE_ADMIN' ? '管理员' : '普通用户' }}
              </el-tag>
            </div>
            <div class="preview-item">
              <span class="label">Token额度:</span>
              <span class="value token">{{ createForm.tokenLimit?.toLocaleString() || '0' }}</span>
            </div>
            <div class="preview-item">
              <span class="label">状态:</span>
              <el-tag :type="createForm.userStatus === 'ACTIVE' ? 'success' : 'info'" size="small">
                {{ createForm.userStatus === 'ACTIVE' ? '正常' : '禁用' }}
              </el-tag>
            </div>
          </div>
        </el-card>
        
        <!-- 操作提示 -->
        <el-card shadow="never" class="tips-card">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>创建提示</span>
            </div>
          </template>
          
          <div class="tips-content">
            <el-alert
              title="账号创建后的操作"
              type="info"
              :closable="false"
              show-icon
            >
              <template #default>
                <ol class="tips-list">
                  <li>将登录信息告知用户</li>
                  <li>建议用户首次登录后修改密码</li>
                  <li>根据需要调整Token额度</li>
                  <li>定期检查账号使用情况</li>
                </ol>
              </template>
            </el-alert>
            
            <el-divider />
            
            <div class="quick-presets">
              <h4>快速配置</h4>
              <el-button size="small" @click="applyPreset('developer')">
                开发人员配置
              </el-button>
              <el-button size="small" @click="applyPreset('tester')">
                测试人员配置
              </el-button>
              <el-button size="small" @click="applyPreset('admin')">
                管理员配置
              </el-button>
            </div>
          </div>
        </el-card>
        
        <!-- 最近创建的账号 -->
        <el-card shadow="never" class="recent-card">
          <template #header>
            <div class="card-header">
              <el-icon><Clock /></el-icon>
              <span>最近创建</span>
            </div>
          </template>
          
          <div class="recent-accounts">
            <div
              v-for="account in recentAccounts"
              :key="account.id"
              class="recent-item"
            >
              <div class="account-info">
                <div class="account-name">{{ account.username }}</div>
                <div class="account-time">{{ formatTime(account.createdTime) }}</div>
              </div>
              <el-tag :type="account.userRole === 'ROLE_ADMIN' ? 'danger' : 'primary'" size="small">
                {{ account.userRole === 'ROLE_ADMIN' ? '管理员' : '用户' }}
              </el-tag>
            </div>
            
            <div v-if="recentAccounts.length === 0" class="empty-recent">
              <el-empty description="暂无最近创建的账号" :image-size="60" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UserFilled, View, InfoFilled, Clock, Check, Refresh,
  CircleCheckFilled, CircleCloseFilled
} from '@element-plus/icons-vue'
import api from '@/utils/api'

// 引用
const createFormRef = ref()

// 表单状态
const submitting = ref(false)

// 表单数据
const createForm = reactive({
  username: '',
  realName: '',
  email: '',
  userRole: 'ROLE_USER',
  tokenLimit: 5000,
  password: '',
  confirmPassword: '',
  userStatus: 'ACTIVE',
  remarks: ''
})

// 表单验证规则
const createRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { max: 50, message: '姓名长度不能超过 50 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  userRole: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ],
  tokenLimit: [
    { required: true, message: '请输入Token额度', trigger: 'blur' },
    { type: 'number', min: 0, message: 'Token额度不能小于0', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== createForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  userStatus: [
    { required: true, message: '请选择账号状态', trigger: 'change' }
  ]
}

// 最近创建的账号
const recentAccounts = ref([])

// 检查用户名是否已存在
const checkUsername = async () => {
  if (!createForm.username) return
  
  try {
    const { data } = await api.get('/users/check-username', {
      params: { username: createForm.username }
    })
    
    if (data) {
      ElMessage.warning('用户名已存在，请更换')
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 检查邮箱是否已存在
const checkEmail = async () => {
  if (!createForm.email) return
  
  try {
    // 模拟检查邮箱
    await new Promise(resolve => setTimeout(resolve, 300))
    
    // 这里应该调用实际的API检查
    const existingEmails = ['admin@example.com', 'test@example.com']
    if (existingEmails.includes(createForm.email)) {
      ElMessage.warning('邮箱已被使用，请更换')
    }
  } catch (error) {
    console.error('检查邮箱失败:', error)
  }
}

// 生成随机密码
const generatePassword = () => {
  const chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678'
  let password = ''
  for (let i = 0; i < 8; i++) {
    password += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  createForm.password = password
  createForm.confirmPassword = password
  ElMessage.success(`已生成密码: ${password}`)
}

// 应用预设配置
const applyPreset = (preset) => {
  const presets = {
    developer: {
      userRole: 'ROLE_USER',
      tokenLimit: 10000,
      userStatus: 'ACTIVE'
    },
    tester: {
      userRole: 'ROLE_USER',
      tokenLimit: 3000,
      userStatus: 'ACTIVE'
    },
    admin: {
      userRole: 'ROLE_ADMIN',
      tokenLimit: 50000,
      userStatus: 'ACTIVE'
    }
  }
  
  const config = presets[preset]
  if (config) {
    Object.assign(createForm, config)
    ElMessage.success(`已应用${preset === 'developer' ? '开发人员' : preset === 'tester' ? '测试人员' : '管理员'}配置`)
  }
}

// 创建账号
const handleCreate = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    
    submitting.value = true
    
    // 构建请求数据
    const requestData = {
      username: createForm.username,
      realName: createForm.realName,
      email: createForm.email,
      userRole: createForm.userRole,
      tokenLimit: createForm.tokenLimit,
      password: createForm.password,
      userStatus: createForm.userStatus
    }
    
    // 调用创建用户API
    const { data } = await api.post('/users', requestData)
    
    ElMessage.success('账号创建成功！')
    
    // 添加到最近创建列表
    recentAccounts.value.unshift({
      id: data.id,
      username: data.username,
      userRole: data.userRole,
      createdTime: Date.now()
    })
    
    // 保持最多显示5个
    if (recentAccounts.value.length > 5) {
      recentAccounts.value.pop()
    }
    
    // 重置表单
    handleReset()
    
  } catch (error) {
    console.error('创建账号失败:', error)
    ElMessage.error(error.response?.data?.message || '创建账号失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  Object.assign(createForm, {
    username: '',
    realName: '',
    email: '',
    userRole: 'ROLE_USER',
    tokenLimit: 5000,
    password: '',
    confirmPassword: '',
    userStatus: 'ACTIVE',
    remarks: ''
  })
  
  if (createFormRef.value) {
    createFormRef.value.resetFields()
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  const now = Date.now()
  const diff = now - timestamp
  
  if (diff < 60000) { // 1分钟内
    return '刚刚'
  } else if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 1天内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return new Date(timestamp).toLocaleDateString()
  }
}

// 加载最近创建的用户
const loadRecentUsers = async () => {
  try {
    const { data } = await api.get('/users', {
      params: {
        page: 0,
        size: 5
      }
    })
    
    // 取前5个最新创建的用户
    recentAccounts.value = data.content?.slice(0, 5).map(user => ({
      id: user.id,
      username: user.username,
      userRole: user.userRole,
      createdTime: user.createdTime
    })) || []
  } catch (error) {
    console.error('加载最近用户失败:', error)
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadRecentUsers()
})
</script>

<style scoped>
.form-card,
.preview-card,
.tips-card,
.recent-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.role-option {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.role-title {
  font-weight: 500;
  color: #2c3e50;
}

.role-desc {
  color: #909399;
  font-size: 12px;
}

.token-unit {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.account-preview {
  space-y: 12px;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.preview-item:last-child {
  border-bottom: none;
}

.preview-item .label {
  color: #909399;
  font-size: 14px;
}

.preview-item .value {
  font-weight: 500;
  color: #2c3e50;
}

.preview-item .value.token {
  color: #409eff;
}

.tips-content {
  font-size: 14px;
}

.tips-list {
  margin: 8px 0 0 16px;
  padding: 0;
}

.tips-list li {
  margin-bottom: 4px;
  color: #606266;
}

.quick-presets h4 {
  margin: 16px 0 8px 0;
  color: #2c3e50;
  font-size: 14px;
}

.quick-presets .el-button {
  margin-right: 8px;
  margin-bottom: 8px;
}

.recent-accounts {
  max-height: 200px;
  overflow-y: auto;
}

.recent-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px;
  background: #f9f9f9;
  border-radius: 4px;
}

.account-info .account-name {
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 2px;
}

.account-info .account-time {
  font-size: 12px;
  color: #c0c4cc;
}

.empty-recent {
  padding: 20px 0;
  text-align: center;
}

:deep(.el-radio) {
  display: block;
  margin-bottom: 12px;
  height: auto;
  line-height: 1.5;
}

:deep(.el-radio__label) {
  white-space: normal;
  display: inline-flex;
  align-items: center;
}

/* 确保账号状态的单选按钮对齐 */
:deep(.el-form-item__content .el-radio__input) {
  vertical-align: middle;
}

:deep(.el-form-item__content .el-radio__inner) {
  vertical-align: middle;
}
</style>