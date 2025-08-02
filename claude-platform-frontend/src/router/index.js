import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { 
      title: '登录',
      requiresAuth: false 
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/layouts/Layout.vue'),
    meta: { 
      title: '仪表盘',
      requiresAuth: true 
    },
    children: [
      {
        path: '',
        name: 'Chat',
        component: () => import('@/views/Chat.vue'),
        meta: { 
          title: '使用对话',
          icon: 'ChatDotRound'
        }
      },
      {
        path: '/download',
        name: 'Download',
        component: () => import('@/views/Download.vue'),
        meta: { 
          title: '下载客户端',
          icon: 'Download'
        }
      },
      {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { 
          title: '使用统计',
          icon: 'DataAnalysis'
        }
      },
      {
        path: '/account-manage',
        name: 'AccountManage',
        component: () => import('@/views/AccountManage.vue'),
        meta: { 
          title: '账号管理',
          icon: 'User',
          requiresAdmin: true
        }
      },
      {
        path: '/create-account',
        name: 'CreateAccount',
        component: () => import('@/views/CreateAccount.vue'),
        meta: { 
          title: '开通账号',
          icon: 'UserFilled',
          requiresAdmin: true
        }
      },
      {
        path: '/system-settings',
        name: 'SystemSettings',
        component: () => import('@/views/SystemSettings.vue'),
        meta: { 
          title: '系统设置',
          icon: 'Setting',
          requiresAdmin: true
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - Claude Platform`
  }
  
  // 检查是否需要登录
  if (to.meta?.requiresAuth !== false) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
    
    // 检查管理员权限
    if (to.meta?.requiresAdmin && !userStore.isAdmin) {
      ElMessage.error('权限不足')
      next('/dashboard')
      return
    }
  }
  
  // 已登录用户访问登录页面时重定向到仪表盘
  if (to.path === '/login' && userStore.isLoggedIn) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router