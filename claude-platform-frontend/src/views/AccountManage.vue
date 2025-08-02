<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">账号管理</h1>
      <p>管理系统中的所有用户账号，包括用户信息编辑、Token额度调整和账号状态管理</p>
    </div>
    
    <!-- 搜索和操作栏 -->
    <el-card shadow="never" class="search-card">
      <div class="search-bar">
        <div class="search-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名、姓名或邮箱"
            style="width: 300px"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select
            v-model="searchRole"
            placeholder="用户角色"
            style="width: 150px"
            clearable
          >
            <el-option label="全部角色" value="" />
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="普通用户" value="ROLE_USER" />
          </el-select>
          
          <el-select
            v-model="searchStatus"
            placeholder="账号状态"
            style="width: 150px"
            clearable
          >
            <el-option label="全部状态" value="" />
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
          
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
        
        <div class="search-right">
          <el-button type="primary" @click="showCreateDialog">
            <el-icon><Plus /></el-icon>
            新建用户
          </el-button>
          
          <el-button @click="handleRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 用户列表 -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="userList"
        :loading="loading"
        stripe
        style="width: 100%"
        table-layout="auto"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="username" label="用户名" min-width="120" />
        
        <el-table-column prop="realName" label="真实姓名" min-width="100" />
        
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        
        <el-table-column prop="userRole" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.userRole)" size="small">
              {{ getRoleLabel(row.userRole) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="tokenLimit" label="Token额度" min-width="110" align="right">
          <template #default="{ row }">
            <span class="token-limit">{{ row.tokenLimit?.toLocaleString() || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="已使用" min-width="100" align="right">
          <template #default="{ row }">
            <span class="token-used">{{ (row.usedTokens || 0).toLocaleString() }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="userStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.userStatus)" size="small">
              {{ getStatusLabel(row.userStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.lastLoginTime ? formatDateTime(row.lastLoginTime) : '从未登录' }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button link size="small" @click="showEditDialog(row)">
                编辑
              </el-button>
              
              <el-button 
                link 
                size="small"
                :class="row.userStatus === 'ACTIVE' ? 'text-warning' : 'text-success'"
                @click="toggleUserStatus(row)"
              >
                {{ row.userStatus === 'ACTIVE' ? '禁用' : '启用' }}
              </el-button>
              
              <el-button link size="small" @click="resetPassword(row)">
                重置密码
              </el-button>
              
              <el-button 
                link 
                size="small" 
                class="text-danger"
                @click="deleteUser(row)"
                :disabled="row.userRole === 'ROLE_ADMIN'"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalUsers"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      :title="editMode === 'create' ? '新建用户' : '编辑用户'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="userForm.username"
            placeholder="请输入用户名"
            :disabled="editMode === 'edit'"
          />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="userForm.realName"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="userForm.email"
            placeholder="请输入邮箱地址"
          />
        </el-form-item>
        
        <el-form-item label="用户角色" prop="userRole">
          <el-radio-group v-model="userForm.userRole" class="role-radio-group">
            <el-radio label="ROLE_USER">普通用户</el-radio>
            <el-radio label="ROLE_ADMIN">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="Token额度" prop="tokenLimit">
          <el-input-number
            v-model="userForm.tokenLimit"
            :min="0"
            :max="1000000"
            :step="1000"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item v-if="editMode === 'create'" label="初始密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入初始密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="账号状态" prop="userStatus">
          <el-radio-group v-model="userForm.userStatus" class="status-radio-group">
            <el-radio label="ACTIVE">正常</el-radio>
            <el-radio label="DISABLED">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ editMode === 'create' ? '创建' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus
} from '@element-plus/icons-vue'
import api from '@/utils/api'

// 引用
const userFormRef = ref()

// 搜索条件
const searchKeyword = ref('')
const searchRole = ref('')
const searchStatus = ref('')

// 表格数据
const loading = ref(false)
const userList = ref([])
const selectedUsers = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const totalUsers = ref(0)

// 编辑对话框
const editDialogVisible = ref(false)
const editMode = ref('create') // 'create' | 'edit'
const submitting = ref(false)

// 表单数据
const userForm = reactive({
  id: '',
  username: '',
  realName: '',
  email: '',
  userRole: 'ROLE_USER',
  tokenLimit: 5000,
  password: '',
  userStatus: 'ACTIVE'
})

// 表单验证规则
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
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
  userStatus: [
    { required: true, message: '请选择账号状态', trigger: 'change' }
  ]
}

// 静态数据
const mockUsers = [
  {
    id: '1',
    username: 'admin',
    realName: '系统管理员',
    email: 'admin@claude-platform.com',
    userRole: 'ROLE_ADMIN',
    tokenLimit: 100000,
    usedTokens: 5200,
    userStatus: 'ACTIVE',
    lastLoginTime: Date.now() - 3600000,
    createdTime: Date.now() - 86400000 * 30
  },
  {
    id: '2',
    username: 'zhang3',
    realName: '张三',
    email: 'zhang3@example.com',
    userRole: 'ROLE_USER',
    tokenLimit: 5000,
    usedTokens: 3500,
    userStatus: 'ACTIVE',
    lastLoginTime: Date.now() - 7200000,
    createdTime: Date.now() - 86400000 * 15
  },
  {
    id: '3',
    username: 'li4',
    realName: '李四',
    email: 'li4@example.com',
    userRole: 'ROLE_USER',
    tokenLimit: 3000,
    usedTokens: 1200,
    userStatus: 'ACTIVE',
    lastLoginTime: Date.now() - 14400000,
    createdTime: Date.now() - 86400000 * 10
  },
  {
    id: '4',
    username: 'wang5',
    realName: '王五',
    email: 'wang5@example.com',
    userRole: 'ROLE_USER',
    tokenLimit: 5000,
    usedTokens: 5000,
    userStatus: 'DISABLED',
    lastLoginTime: Date.now() - 86400000 * 7,
    createdTime: Date.now() - 86400000 * 5
  }
]

// 工具函数
const getRoleLabel = (role) => {
  return role === 'ROLE_ADMIN' ? '管理员' : '普通用户'
}

const getRoleTagType = (role) => {
  return role === 'ROLE_ADMIN' ? 'danger' : 'primary'
}

const getStatusLabel = (status) => {
  return status === 'ACTIVE' ? '正常' : '禁用'
}

const getStatusTagType = (status) => {
  return status === 'ACTIVE' ? 'success' : 'info'
}

const formatDateTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 数据操作
const loadUserList = async () => {
  loading.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 应用搜索过滤
    let filteredUsers = [...mockUsers]
    
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      filteredUsers = filteredUsers.filter(user =>
        user.username.toLowerCase().includes(keyword) ||
        user.realName.toLowerCase().includes(keyword) ||
        user.email.toLowerCase().includes(keyword)
      )
    }
    
    if (searchRole.value) {
      filteredUsers = filteredUsers.filter(user => user.userRole === searchRole.value)
    }
    
    if (searchStatus.value) {
      filteredUsers = filteredUsers.filter(user => user.userStatus === searchStatus.value)
    }
    
    // 分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    
    userList.value = filteredUsers.slice(start, end)
    totalUsers.value = filteredUsers.length
    
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadUserList()
}

const handleReset = () => {
  searchKeyword.value = ''
  searchRole.value = ''
  searchStatus.value = ''
  currentPage.value = 1
  loadUserList()
}

const handleRefresh = () => {
  loadUserList()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadUserList()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadUserList()
}

// 表格选择
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 显示创建对话框
const showCreateDialog = () => {
  editMode.value = 'create'
  resetUserForm()
  editDialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (user) => {
  editMode.value = 'edit'
  Object.assign(userForm, {
    id: user.id,
    username: user.username,
    realName: user.realName,
    email: user.email,
    userRole: user.userRole,
    tokenLimit: user.tokenLimit,
    password: '',
    userStatus: user.userStatus
  })
  editDialogVisible.value = true
}

// 重置表单
const resetUserForm = () => {
  Object.assign(userForm, {
    id: '',
    username: '',
    realName: '',
    email: '',
    userRole: 'ROLE_USER',
    tokenLimit: 5000,
    password: '',
    userStatus: 'ACTIVE'
  })
  
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    
    submitting.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (editMode.value === 'create') {
      ElMessage.success('用户创建成功')
    } else {
      ElMessage.success('用户信息更新成功')
    }
    
    editDialogVisible.value = false
    loadUserList()
    
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitting.value = false
  }
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  const action = user.userStatus === 'ACTIVE' ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 "${user.username}" 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    user.userStatus = user.userStatus === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    ElMessage.success(`用户已${action}`)
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换用户状态失败:', error)
      ElMessage.error(`${action}失败`)
    }
  }
}

// 重置密码
const resetPassword = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${user.username}" 的密码吗？新密码将重置为 "123456"`,
      '确认重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('密码重置成功，新密码为：123456')
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

// 删除用户
const deleteUser = async (user) => {
  if (user.userRole === 'ROLE_ADMIN') {
    ElMessage.warning('不能删除管理员账号')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('用户删除成功')
    loadUserList()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

// 组件挂载
onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.search-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-right {
  display: flex;
  gap: 8px;
}

.token-limit {
  font-weight: 500;
  color: #409eff;
}

.token-used {
  color: #e6a23c;
}

.text-warning {
  color: #e6a23c !important;
}

.text-success {
  color: #67c23a !important;
}

.text-danger {
  color: #f56c6c !important;
}

.pagination-container {
  margin-top: 16px;
  text-align: right;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

:deep(.el-table .el-button.is-link) {
  padding: 0;
  margin: 0;
}

.status-radio-group,
.role-radio-group {
  display: flex;
  align-items: center;
  gap: 20px;
}

.status-radio-group :deep(.el-radio),
.role-radio-group :deep(.el-radio) {
  display: flex;
  align-items: center;
  margin-right: 0;
  white-space: nowrap;
}

.status-radio-group :deep(.el-radio__input),
.role-radio-group :deep(.el-radio__input) {
  margin-right: 8px;
}

.status-radio-group :deep(.el-radio__label),
.role-radio-group :deep(.el-radio__label) {
  padding-left: 0;
}
</style>