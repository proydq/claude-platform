<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <div class="layout-header">
      <div class="header-left">
        <div class="logo">
          <el-icon class="logo-icon"><Cpu /></el-icon>
          Claude Platform
        </div>
      </div>
      
      <div class="header-right">
        <div class="user-info">
          <el-icon><User /></el-icon>
          <span class="username">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
          <span class="token-info">
            剩余: {{ remainingTokens.toLocaleString() }} / {{ totalTokens.toLocaleString() }}
          </span>
        </div>
        
        <el-dropdown @command="handleCommand">
          <el-button link>
            <el-icon><Setting /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人资料
              </el-dropdown-item>
              <el-dropdown-item command="changePassword">
                <el-icon><Lock /></el-icon>
                修改密码
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 主体内容 -->
    <div class="layout-main">
      <!-- 左侧菜单 -->
      <div class="layout-sidebar">
        <el-menu
          :default-active="$route.path"
          :router="true"
          unique-opened
        >
          <!-- 普通用户菜单 -->
          <el-menu-item index="/dashboard">
            <el-icon><ChatDotRound /></el-icon>
            <span>使用对话</span>
          </el-menu-item>
          
          <el-menu-item index="/download">
            <el-icon><Download /></el-icon>
            <span>下载客户端</span>
          </el-menu-item>
          
          <el-menu-item index="/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <span>使用统计</span>
          </el-menu-item>
          
          <!-- 管理员菜单 -->
          <template v-if="userStore.isAdmin">
            <el-divider style="margin: 12px 0;" />
            
            <el-menu-item index="/account-manage">
              <el-icon><UserFilled /></el-icon>
              <span>账号管理</span>
            </el-menu-item>
            
            <el-menu-item index="/create-account">
              <el-icon><Plus /></el-icon>
              <span>开通账号</span>
            </el-menu-item>
            
            <el-menu-item index="/system-settings">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </el-menu-item>
          </template>
        </el-menu>
      </div>
      
      <!-- 内容区域 -->
      <div class="layout-content">
        <router-view />
      </div>
    </div>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="changePasswordVisible"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="changePasswordVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Cpu, User, Setting, Lock, SwitchButton,
  ChatDotRound, Download, DataAnalysis,
  UserFilled, Plus
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'

const router = useRouter()
const userStore = useUserStore()

// 修改密码相关
const changePasswordVisible = ref(false)
const passwordFormRef = ref()
const passwordLoading = ref(false)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// Token 信息（静态数据，后续改为动态）
const totalTokens = computed(() => userStore.userInfo.tokenLimit || 5000)
const usedTokens = ref(1500) // 静态数据
const remainingTokens = computed(() => totalTokens.value - usedTokens.value)

// 处理下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中...')
      break
    case 'changePassword':
      changePasswordVisible.value = true
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消
  }
}

// 处理修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    
    passwordLoading.value = true
    
    await api.post('/user/change-password', {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功')
    changePasswordVisible.value = false
    
    // 重置表单
    Object.assign(passwordForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 获取用户统计信息
const fetchUserStats = async () => {
  try {
    const response = await api.get('/statistics/my/summary')
    usedTokens.value = response.data.totalUsedTokens || 0
  } catch (error) {
    console.error('获取用户统计失败:', error)
  }
}

onMounted(() => {
  fetchUserStats()
})
</script>