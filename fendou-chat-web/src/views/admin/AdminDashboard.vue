<template>
  <div class="admin-dashboard">
    <!-- 顶部数据卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="card in statCards" :key="card.key" :style="{background: card.gradient}">
        <div class="stat-icon">{{ card.icon }}</div>
        <div class="stat-info">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
        <div class="stat-trend" :class="card.trendUp ? 'up' : 'stable'">
          {{ card.trend }}
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-grid">
      <!-- 注册趋势折线图 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">📈 近30天用户注册趋势</span>
            <el-button size="small" @click="loadCharts" :loading="loading">刷新</el-button>
          </div>
        </template>
        <div ref="registerChartRef" class="chart-container"></div>
      </el-card>

      <!-- 消息量趋势 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">💬 近30天消息量趋势</span>
          </div>
        </template>
        <div ref="messageChartRef" class="chart-container"></div>
      </el-card>

      <!-- 24小时热力图 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">🔥 活跃时段热力图（近7天）</span>
          </div>
        </template>
        <div ref="heatmapChartRef" class="chart-container"></div>
      </el-card>

      <!-- 用户活跃度分布 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">👥 用户活跃度分布</span>
          </div>
        </template>
        <div ref="activityChartRef" class="chart-container"></div>
      </el-card>

      <!-- 日活跃用户折线 -->
      <el-card class="chart-card chart-card-wide" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">📊 近7天日活跃用户（DAU）</span>
          </div>
        </template>
        <div ref="dauChartRef" class="chart-container"></div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useAdminApi } from '@/api/admin/index.js'
import request from '@/utils/request.js'

const adminApi = useAdminApi()
const loading = ref(false)

const registerChartRef = ref(null)
const messageChartRef = ref(null)
const heatmapChartRef = ref(null)
const activityChartRef = ref(null)
const dauChartRef = ref(null)

let registerChart = null
let messageChart = null
let heatmapChart = null
let activityChart = null
let dauChart = null

const statCards = reactive([
  { key: 'totalUsers', label: '总用户数', value: '-', icon: '👥', gradient: 'linear-gradient(135deg,#7c3aed,#9333ea)', trend: '↑', trendUp: true },
  { key: 'onlineUsers', label: '当前在线', value: '-', icon: '🟢', gradient: 'linear-gradient(135deg,#059669,#10b981)', trend: 'Live', trendUp: true },
  { key: 'todayMessages', label: '今日消息', value: '-', icon: '💬', gradient: 'linear-gradient(135deg,#0ea5e9,#38bdf8)', trend: '↑', trendUp: true },
  { key: 'totalMessages', label: '累计消息', value: '-', icon: '📨', gradient: 'linear-gradient(135deg,#f59e0b,#fbbf24)', trend: '—', trendUp: false }
])

const loadOverview = async () => {
  try {
    const res = await request({ url: '/admin/statistics/overview', method: 'get' })
    if (res.code === 200) {
      const data = res.data
      statCards[0].value = data.totalUsers?.toLocaleString() || '0'
      statCards[1].value = data.onlineUsers?.toLocaleString() || '0'
      statCards[2].value = data.todayMessages?.toLocaleString() || '0'
      statCards[3].value = data.totalMessages?.toLocaleString() || '0'
    }
  } catch {}
}

const PURPLE_COLORS = ['#7c3aed', '#9333ea', '#a855f7', '#c084fc', '#e879f9']
const GRADIENT_PURPLE = { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(124,58,237,0.4)' }, { offset: 1, color: 'rgba(124,58,237,0.02)' }] }

const initRegisterChart = async () => {
  const res = await request({ url: '/admin/statistics/register-trend', method: 'get' })
  if (res.code !== 200) return
  const dates = res.data.map(d => d.date)
  const counts = res.data.map(d => d.count)

  registerChart = echarts.init(registerChartRef.value)
  registerChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(30,27,75,0.9)', textStyle: { color: '#fff' } },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisLabel: { color: '#9ca3af', fontSize: 11 } },
    yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#f3f4f6' } }, axisLabel: { color: '#9ca3af' } },
    series: [{
      type: 'line', data: counts, smooth: true,
      symbol: 'circle', symbolSize: 5,
      lineStyle: { color: '#7c3aed', width: 2.5 },
      itemStyle: { color: '#7c3aed', borderColor: '#fff', borderWidth: 2 },
      areaStyle: { color: GRADIENT_PURPLE }
    }]
  })
}

const initMessageChart = async () => {
  const res = await request({ url: '/admin/statistics/message-trend', method: 'get' })
  if (res.code !== 200) return
  const dates = res.data.map(d => d.date)
  const counts = res.data.map(d => d.count)

  messageChart = echarts.init(messageChartRef.value)
  messageChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(30,27,75,0.9)', textStyle: { color: '#fff' } },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLabel: { color: '#9ca3af', fontSize: 11 } },
    yAxis: { type: 'value', axisLabel: { color: '#9ca3af' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
    series: [{
      type: 'bar', data: counts,
      itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: '#9333ea' }, { offset: 1, color: 'rgba(147,51,234,0.2)' }] }, borderRadius: [4, 4, 0, 0] },
      emphasis: { itemStyle: { color: '#7c3aed' } }
    }]
  })
}

const initHeatmapChart = async () => {
  const res = await request({ url: '/admin/statistics/hourly-heatmap', method: 'get' })
  if (res.code !== 200) return
  const hours = res.data.map(d => d.hour)
  const counts = res.data.map(d => d.count)
  const maxVal = Math.max(...counts, 1)

  heatmapChart = echarts.init(heatmapChartRef.value)
  heatmapChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: hours, axisLabel: { color: '#9ca3af', fontSize: 10, rotate: 45 } },
    yAxis: { type: 'value', axisLabel: { color: '#9ca3af' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
    visualMap: { show: false, min: 0, max: maxVal, inRange: { color: ['#ede9fe', '#7c3aed'] } },
    series: [{
      type: 'bar',
      data: counts.map((v, i) => ({ value: v, itemStyle: { color: `rgba(124,58,237,${0.15 + 0.85 * v / maxVal})`, borderRadius: [3, 3, 0, 0] } }))
    }]
  })
}

const initActivityChart = async () => {
  const res = await request({ url: '/admin/statistics/user-activity', method: 'get' })
  if (res.code !== 200) return
  const dist = res.data.activityDistribution || []

  activityChart = echarts.init(activityChartRef.value)
  activityChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#6b7280', fontSize: 11 } },
    series: [{
      type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'],
      data: dist.map((d, i) => ({ name: d.name, value: d.value, itemStyle: { color: PURPLE_COLORS[i] } })),
      label: { show: false },
      emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(124,58,237,0.3)' } }
    }]
  })

  // DAU图表
  const dailyActive = res.data.dailyActive || []
  dauChart = echarts.init(dauChartRef.value)
  dauChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(30,27,75,0.9)', textStyle: { color: '#fff' } },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: dailyActive.map(d => d.date), axisLabel: { color: '#9ca3af' } },
    yAxis: { type: 'value', axisLabel: { color: '#9ca3af' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
    series: [{
      type: 'line', data: dailyActive.map(d => d.activeUsers),
      smooth: true, symbol: 'circle', symbolSize: 7,
      lineStyle: { color: '#10b981', width: 3 },
      itemStyle: { color: '#10b981', borderColor: '#fff', borderWidth: 2 },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(16,185,129,0.35)' }, { offset: 1, color: 'rgba(16,185,129,0.02)' }] } }
    }]
  })
}

const loadCharts = async () => {
  loading.value = true
  try {
    await loadOverview()
    await initRegisterChart()
    await initMessageChart()
    await initHeatmapChart()
    await initActivityChart()
  } finally {
    loading.value = false
  }
}

const handleResize = () => {
  [registerChart, messageChart, heatmapChart, activityChart, dauChart].forEach(c => c?.resize())
}

onMounted(async () => {
  await nextTick()
  await loadCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  ;[registerChart, messageChart, heatmapChart, activityChart, dauChart].forEach(c => c?.dispose())
})
</script>

<style scoped lang="scss">
.admin-dashboard {
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
  transition: transform 0.2s, box-shadow 0.2s;
  color: white;
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 28px rgba(0,0,0,0.18);
  }
}

.stat-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.stat-info { flex: 1; }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
  margin-top: 3px;
}

.stat-trend {
  font-size: 14px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 20px;
  background: rgba(255,255,255,0.2);
  white-space: nowrap;
}

.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-card-wide {
  grid-column: 1 / -1;
}

.chart-container {
  height: 240px;
  width: 100%;
}

.chart-card-wide .chart-container {
  height: 220px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

:deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(124,58,237,0.1);
}

:deep(.el-card__body) {
  padding: 16px;
}

:deep(.el-card) {
  border-radius: 12px;
  border: 1px solid rgba(124,58,237,0.1);
  transition: box-shadow 0.2s;
  &:hover {
    box-shadow: 0 4px 20px rgba(124,58,237,0.1) !important;
  }
}

@media (max-width: 1200px) {
  .stat-cards { grid-template-columns: repeat(2, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
  .chart-card-wide { grid-column: auto; }
}
</style>
