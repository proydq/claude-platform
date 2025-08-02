import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/utils/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.userRole === 'ROLE_ADMIN')
  
  // 登录
  const login = async (loginData) => {
    try {
      const response = await api.post('/auth/login', loginData)
      const { token: newToken, userInfo: newUserInfo } = response.data
      
      token.value = newToken
      userInfo.value = newUserInfo
      
      // 存储到 localStorage
      localStorage.setItem('token', newToken)
      localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
      
      // 设置 API 默认 header
      api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
      
      return { success: true }
    } catch (error) {
      console.error('登录失败:', error)
      return { 
        success: false, 
        message: error.response?.data?.message || '登录失败' 
      }
    }
  }
  
  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = {}
    
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    delete api.defaults.headers.common['Authorization']
  }
  
  // 检查认证状态
  const checkAuth = () => {
    if (token.value) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    }
  }
  
  // 更新用户信息
  const updateUserInfo = (newUserInfo) => {
    userInfo.value = { ...userInfo.value, ...newUserInfo }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    logout,
    checkAuth,
    updateUserInfo
  }
})