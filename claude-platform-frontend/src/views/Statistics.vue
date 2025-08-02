<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">使用统计</h1>
      <p>查看您的 Token 使用情况和对话统计</p>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="24" class="stats-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon usage">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.monthlyUsage }}</div>
              <div class="stat-label">本月使用次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon consumed">
              <el-icon><Coin /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.monthlyTokens.toLocaleString() }}</div>
              <div class="stat-label">本月 Token 消耗</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon remaining">
              <el-icon><Wallet /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.remainingTokens.toLocaleString() }}</div>
              <div class="stat-label">剩余 Token</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-item">
            <div class="stat-icon total">
              <el-icon><Medal /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stats.totalUsage }}</div>
              <div class="stat-label">累计使用次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 使用趋势图 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><TrendCharts /></el-icon>
            <span>使用趋势</span>
          </div>
          <div class="header-right">
            <el-radio-group v-model="chartPeriod" size="small">
              <el-radio-button label="7days">最近7天</el-radio-button>
              <el-radio-button label="30days">最近30天</el-radio-button>
              <el-radio-button label="90days">最近90天</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      
      <div class="chart-container">
        <div ref="chartRef" class="chart"></div>
      </div>
    </el-card>
    
    <!-- 使用记录 -->
    <el-card shadow="never" class="records-card">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <el-icon><List /></el-icon>
            <span>最近使用记录</span>
          </div>
          <div class="header-right">
            <el-button @click="refreshRecords" :loading="recordsLoading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table 
        :data="usageRecords" 
        :loading="recordsLoading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="time" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.timestamp) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="title" label="对话标题" min-width="200" />
        
        <el-table-column prop="tokensUsed" label="Token 消耗" width="120" align="right">
          <template #default="{ row }">
            <span class="token-count">{{ row.tokensUsed.toLocaleString() }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button 
              link 
              size="small"
              @click="viewRecord(row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalRecords"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="使用记录详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedRecord" class="record-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">
            {{ formatDateTime(selectedRecord.timestamp) }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            <el-tag :type="getTypeTagType(selectedRecord.type)" size="small">
              {{ getTypeLabel(selectedRecord.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Token 消耗">
            {{ selectedRecord.tokensUsed.toLocaleString() }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(selectedRecord.status)" size="small">
              {{ getStatusLabel(selectedRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="对话标题" :span="2">
            {{ selectedRecord.title }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-content">
          <h4>请求内容</h4>
          <div class="content-box">
            {{ selectedRecord.requestContent }}
          </div>
          
          <h4>响应内容</h4>
          <div class="content-box">
            {{ selectedRecord.responseContent }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, Coin, Wallet, Medal, TrendCharts,
  List, Refresh
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import api from '@/utils/api'

// 图表引用
const chartRef = ref()
let chartInstance = null

// 统计数据
const stats = reactive({
  monthlyUsage: 125,
  monthlyTokens: 3500,
  remainingTokens: 1500,
  totalUsage: 458
})

// 图表相关
const chartPeriod = ref('7days')

// 使用记录相关
const recordsLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const totalRecords = ref(0)

// 详情对话框
const detailVisible = ref(false)
const selectedRecord = ref(null)

// 静态数据
const usageRecords = ref([
  {
    id: '1',
    timestamp: Date.now() - 3600000,
    type: 'chat',
    title: '分析日志文件错误',
    tokensUsed: 285,
    status: 'success',
    requestContent: '请帮我分析一下这个错误日志，找出问题的根本原因。',
    responseContent: '根据您提供的日志分析，这个错误主要是由于内存不足导致的...'
  },
  {
    id: '2',
    timestamp: Date.now() - 7200000,
    type: 'file',
    title: '代码优化建议',
    tokensUsed: 420,
    status: 'success',
    requestContent: '上传了一个Python文件，请提供优化建议。',
    responseContent: '这段代码可以通过以下几个方面进行优化：1. 使用列表推导式...'
  },
  {
    id: '3',
    timestamp: Date.now() - 14400000,
    type: 'chat',
    title: 'API 接口设计',
    tokensUsed: 180,
    status: 'success',
    requestContent: '设计一个用户管理的REST API接口',
    responseContent: '基于RESTful设计原则，建议设计以下接口...'
  },
  {
    id: '4',
    timestamp: Date.now() - 21600000,
    type: 'file',
    title: '配置文件检查',
    tokensUsed: 95,
    status: 'failed',
    requestContent: '检查配置文件是否有语法错误',
    responseContent: '连接超时，请稍后重试'
  }
])

// 工具函数
const formatDateTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

const getTypeLabel = (type) => {
  const labels = {
    chat: '对话',
    file: '文件分析',
    system: '系统'
  }
  return labels[type] || type
}

const getTypeTagType = (type) => {
  const types = {
    chat: 'primary',
    file: 'success',
    system: 'info'
  }
  return types[type] || 'info'
}

const getStatusLabel = (status) => {
  const labels = {
    success: '成功',
    failed: '失败',
    pending: '处理中'
  }
  return labels[status] || status
}

const getStatusTagType = (status) => {
  const types = {
    success: 'success',
    failed: 'danger',
    pending: 'warning'
  }
  return types[status] || 'info'
}

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  chartInstance = echarts.init(chartRef.value)
  
  const option = {
    title: {
      text: '最近7天使用情况',
      left: 'center',
      textStyle: {
        fontSize: 16,
        color: '#606266'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['使用次数', 'Token消耗'],
      bottom: 10
    },
    xAxis: {
      type: 'category',
      data: ['01-15', '01-16', '01-17', '01-18', '01-19', '01-20', '01-21']
    },
    yAxis: [
      {
        type: 'value',
        name: '使用次数',
        position: 'left'
      },
      {
        type: 'value',
        name: 'Token消耗',
        position: 'right'
      }
    ],
    series: [
      {
        name: '使用次数',
        type: 'line',
        data: [12, 15, 8, 20, 18, 25, 22],
        smooth: true,
        itemStyle: {
          color: '#409eff'
        }
      },
      {
        name: 'Token消耗',
        type: 'bar',
        yAxisIndex: 1,
        data: [1200, 1800, 800, 2200, 2000, 2800, 2400],
        itemStyle: {
          color: '#67c23a'
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

// 更新图表数据
const updateChart = () => {
  if (!chartInstance) return
  
  const periodData = {
    '7days': {
      title: '最近7天使用情况',
      xAxis: ['01-15', '01-16', '01-17', '01-18', '01-19', '01-20', '01-21'],
      usage: [12, 15, 8, 20, 18, 25, 22],
      tokens: [1200, 1800, 800, 2200, 2000, 2800, 2400]
    },
    '30days': {
      title: '最近30天使用情况',
      xAxis: Array.from({length: 30}, (_, i) => `${i + 1}日`),
      usage: Array.from({length: 30}, () => Math.floor(Math.random() * 30) + 5),
      tokens: Array.from({length: 30}, () => Math.floor(Math.random() * 3000) + 500)
    },
    '90days': {
      title: '最近90天使用情况',
      xAxis: Array.from({length: 90}, (_, i) => `${i + 1}天`),
      usage: Array.from({length: 90}, () => Math.floor(Math.random() * 35) + 3),
      tokens: Array.from({length: 90}, () => Math.floor(Math.random() * 3500) + 300)
    }
  }
  
  const data = periodData[chartPeriod.value]
  
  chartInstance.setOption({
    title: {
      text: data.title
    },
    xAxis: {
      data: data.xAxis
    },
    series: [
      {
        data: data.usage
      },
      {
        data: data.tokens
      }
    ]
  })
}

// 刷新记录
const refreshRecords = async () => {
  recordsLoading.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('数据已刷新')
    totalRecords.value = 156 // 模拟总数
  } catch (error) {
    console.error('刷新记录失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    recordsLoading.value = false
  }
}

// 查看记录详情
const viewRecord = (record) => {
  selectedRecord.value = record
  detailVisible.value = true
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  // 重新加载数据
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  // 重新加载数据
}

// 监听图表周期变化
watch(chartPeriod, () => {
  updateChart()
})

// 组件挂载
onMounted(async () => {
  totalRecords.value = usageRecords.value.length
  
  await nextTick()
  initChart()
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (chartInstance) {
      chartInstance.resize()
    }
  })
})
</script>

<style scoped>
.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  height: 120px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-icon.usage {
  background: linear-gradient(135deg, #409eff, #66b3ff);
}

.stat-icon.consumed {
  background: linear-gradient(135deg, #e6a23c, #f0c78a);
}

.stat-icon.remaining {
  background: linear-gradient(135deg, #67c23a, #95d475);
}

.stat-icon.total {
  background: linear-gradient(135deg, #909399, #b1b3b8);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.chart-card,
.records-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.chart-container {
  height: 400px;
}

.chart {
  width: 100%;
  height: 100%;
}

.token-count {
  font-weight: 500;
  color: #e6a23c;
}

.pagination-container {
  margin-top: 16px;
  text-align: right;
}

.record-detail {
  padding: 16px 0;
}

.detail-content {
  margin-top: 24px;
}

.detail-content h4 {
  margin: 16px 0 8px 0;
  color: #2c3e50;
}

.content-box {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-size: 14px;
  line-height: 1.6;
}
</style>