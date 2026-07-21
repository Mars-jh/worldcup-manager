<template>
  <div class="dashboard">
    <h2 class="page-title">仪表板 Dashboard</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in stats" :key="stat.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" :style="{ background: stat.color }">
            <el-icon :size="28"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 射手榜 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">🏆 射手榜 Top 5</span>
          </template>
          <el-table :data="topScorers" stripe size="small">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column prop="name" label="球员" />
            <el-table-column prop="position" label="位置" width="70" />
            <el-table-column prop="goals" label="进球" width="70" sortable />
            <el-table-column prop="assists" label="助攻" width="70" />
          </el-table>
        </el-card>
      </el-col>

      <!-- 各洲分布图表 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">🌍 各洲球队分布</span>
          </template>
          <div ref="chartRef" style="height: 280px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

const stats = ref([])
const topScorers = ref([])
const chartRef = ref()
let chartInstance = null

const continentLabels = {
  EUROPE: '欧洲',
  SOUTH_AMERICA: '南美洲',
  NORTH_AMERICA: '北美洲',
  AFRICA: '非洲',
  ASIA: '亚洲',
  OCEANIA: '大洋洲'
}

onMounted(async () => {
  try {
    const res = await request.get('/dashboard/stats')
    const data = res.data

    stats.value = [
      { label: '参赛球队', value: data.totalTeams, icon: 'Flag', color: '#409EFF' },
      { label: '注册球员', value: data.totalPlayers, icon: 'User', color: '#67C23A' },
      { label: '已完赛', value: data.completedMatches, icon: 'CircleCheck', color: '#E6A23C' },
      { label: '总场次', value: data.totalMatches, icon: 'Trophy', color: '#F56C6C' }
    ]

    topScorers.value = data.topScorers || []

    // 初始化 ECharts 饼图
    const dist = data.continentDistribution || {}
    const chartData = Object.entries(dist).map(([key, val]) => ({
      name: continentLabels[key] || key,
      value: val
    }))

    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} 支 ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        data: chartData,
        emphasis: {
          itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
        },
        label: { formatter: '{b}\n{c}支' }
      }]
    })

    window.addEventListener('resize', handleResize)
  } catch (e) {
    console.error('加载仪表板数据失败', e)
  }
})

onUnmounted(() => {
  if (chartInstance) chartInstance.dispose()
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  chartInstance?.resize()
}
</script>

<style scoped>
.page-title {
  font-size: 22px;
  margin-bottom: 20px;
  color: #333;
}
.stats-row {
  margin-bottom: 0;
}
.stat-card {
  display: flex;
  align-items: center;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}
.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}
.card-title {
  font-weight: bold;
  font-size: 15px;
}
</style>